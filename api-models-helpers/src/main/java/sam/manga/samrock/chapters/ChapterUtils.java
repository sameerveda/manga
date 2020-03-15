package sam.manga.samrock.chapters;

import static sam.manga.samrock.chapters.ChaptersMeta.CHAPTERS_TABLE_NAME;
import static sam.manga.samrock.chapters.ChaptersMeta.CHAPTER_ID;
import static sam.manga.samrock.chapters.ChaptersMeta.MANGA_ID;
import static sam.manga.samrock.chapters.ChaptersMeta.NAME;
import static sam.manga.samrock.chapters.ChaptersMeta.NUMBER;
import static sam.manga.samrock.chapters.ChaptersMeta.READ;
import static sam.manga.samrock.mangas.MangasMeta.CHAP_COUNT_PC;
import static sam.manga.samrock.mangas.MangasMeta.LAST_UPDATE_TIME;
import static sam.manga.samrock.mangas.MangasMeta.MANGAS_TABLE_NAME;
import static sam.manga.samrock.mangas.MangasMeta.READ_COUNT;
import static sam.manga.samrock.mangas.MangasMeta.UNREAD_COUNT;
import static sam.sql.JDBCHelper.insertSQL;
import static sam.sql.JDBCHelper.selectSQL;
import static sam.sql.JDBCHelper.selectWhereFieldInSQL;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectScatterMap;

import sam.manga.samrock.mangas.MinimalManga;
import sam.myutils.Checker;
import sam.myutils.HPPCUtils;
import sam.myutils.MyUtilsException;
import sam.myutils.System2;
import sam.sql.SqlConsumer;
import sam.sql.SqlFunction;
import sam.sql.sqlite.SQLiteDB;

public final class ChapterUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChapterUtils.class);
	public static final OfInt ALL = new OfInt() {
		@Override
		public boolean hasNext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int nextInt() {
			throw new UnsupportedOperationException();
		}
	};

	public static void selectByMangaId(SQLiteDB db, OfInt mangaIds, SqlConsumer<ResultSet> consumer, String... chaptersMeta) throws SQLException {
		db.iterate(selectWhereFieldInSQL(CHAPTERS_TABLE_NAME, ChaptersMeta.MANGA_ID, mangaIds, "*").append(';').toString(), consumer);
	}

	public static Chapter lastChapterFor(SQLiteDB db, int mangaId) throws SQLException {
		return db.findFirst(selectSQL(CHAPTERS_TABLE_NAME, "*", "max(" + NUMBER + ")")
				.append(" WHERE ").append(MANGA_ID).append('=').append(mangaId).append(';').toString(), Chapter::new);
	}

	public static IntObjectMap<Chapter> lastChapterFor(SQLiteDB db, OfInt mangaIds) throws SQLException {
		String[] fields = {"*", "max(" + NUMBER + ")"}; 
		String sql = (mangaIds == ALL ? selectSQL(CHAPTERS_TABLE_NAME, fields) : selectWhereFieldInSQL(CHAPTERS_TABLE_NAME, MANGA_ID, mangaIds, fields).append(" GROUP BY ").append(MANGA_ID)).append(';').toString();
		IntObjectMap<Chapter> map = new IntObjectScatterMap<>();
		db.iterate(sql, rs -> map.put(rs.getInt(MANGA_ID), new Chapter(rs)));
		return map;
	}

	/**
	 * 
	 * @param mangasToUpdate manga -> list(chapter) of known chapter meta
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<ChaptersUpdateLog> reset(SQLiteDB sqlite, Map<MinimalManga, List<MinimalChapter>> mangasToUpdate)
			throws SQLException, IOException {
		if (mangasToUpdate.isEmpty())
			throw new IllegalArgumentException("invalid state: mangasToUpdate is empty");

		if (mangasToUpdate.size() != mangasToUpdate.keySet().stream().mapToInt(MinimalManga::getMangaId).distinct()
				.count())
			throw new IllegalArgumentException("mangasToUpdate.size() != mangasIds.size()");

		IntObjectMap<Map<String, MinimalChapter>> suppliedMap = collectChapterNames(mangasToUpdate, MinimalManga::getMangaId);
		IntObjectMap<Map<String, ChapterWithId>> dbLoaded = collectChapterNames(getChapters(sqlite, mangasToUpdate.keySet().stream().mapToInt(MinimalManga::getMangaId).iterator()), null);
		List<ChaptersUpdateLogImpl> logs = new ArrayList<>();
		Set<String> totalCounter = new HashSet<>();

		for (MinimalManga m : mangasToUpdate.keySet()) {
			Path path = m.getDirPath();
			if (Files.notExists(path))
				throw new FileNotFoundException("manga_dir not found: " + m.getDirPath());
			else {
				ChaptersUpdateLogImpl log = new ChaptersUpdateLogImpl(m);
				Iterator<String> files = chapterFileNames(path).iterator();
				Map<String, MinimalChapter> suppliedChaps = suppliedMap.getOrDefault(m.getMangaId(),
						Collections.emptyMap());
				Map<String, ChapterWithId> dbChaps = dbLoaded.getOrDefault(m.getMangaId(), Collections.emptyMap());
				totalCounter.clear();

				while (files.hasNext()) {
					log.chapCountPc++;
					String file = files.next();
					MinimalChapter supplied = suppliedChaps.get(file);
					ChapterWithId db = dbChaps.remove(file);
					totalCounter.add(noIndexName(file));

					if (supplied == null && db == null) {
						log.newParsingFile.add(new ChapterFile(file));
					} else if (db != null) {
						if(db.isRead())
							log.readCount++;
						log.noUpdate.add(db);
					} else {
						log.newFromData.add(supplied);
					}
				}
				log.total = totalCounter.size();
				log.delete.addAll(dbChaps.values());
				logs.add(log);
			}
		}

		logs.removeIf(log -> log.delete.isEmpty() && log.newFromData.isEmpty() && log.newParsingFile.isEmpty());

		if (logs.isEmpty()) {
			return Collections.emptyList();
		}

		StringBuilder update = new StringBuilder();

		if (logs.stream().anyMatch(log -> !log.delete.isEmpty())) {
			update.append("DELETE FROM ").append(CHAPTERS_TABLE_NAME).append(" WHERE ").append(CHAPTER_ID).append(" IN(");
			logs.forEach(log -> log.delete.forEach(c -> update.append(c.id).append(',')));
			update.setCharAt(update.length() - 1, ')');
			update.append(";\n");
		}
		String format = String.format("UPDATE %s SET %s=%%s, %s=%%s, %s=%%s, %s=%s WHERE %s=%%s;\n", MANGAS_TABLE_NAME,
				READ_COUNT, UNREAD_COUNT, CHAP_COUNT_PC, LAST_UPDATE_TIME, System.currentTimeMillis(), MANGA_ID);
		Formatter fm = new Formatter(update);

		logs.forEach(log -> fm.format(format, log.readCount(), log.unreadCount(), log.chapCountPc(),
				log.parent.getMangaId()));
		fm.close();

		if (update.length() != 0) {
			String sql = update.toString();
			LOGGER.debug("{}, \nupdate executes: {}", sql, sqlite.executeUpdate(sql));
		}

		if (!logs.stream().allMatch(log -> log.newFromData.isEmpty() && log.newParsingFile.isEmpty())) {
			try (PreparedStatement ps = sqlite
					.prepareStatement(insertSQL(CHAPTERS_TABLE_NAME, MANGA_ID, NAME, NUMBER, READ))) {
				for (ChaptersUpdateLogImpl log : logs) {
					if (log.newFromData.isEmpty() && log.newParsingFile.isEmpty())
						continue;

					SqlConsumer<MinimalChapter> action = c -> {
						ps.setInt(1, log.parent.getMangaId());
						ps.setString(2, c.getFileName());
						ps.setDouble(3, c.getNumber());
						ps.setBoolean(4, false);
						ps.addBatch();
					};
					for (MinimalChapter c : log.newFromData)
						action.accept(c);
					for (MinimalChapter c : log.newParsingFile)
						action.accept(c);
				}
				LOGGER.debug("insert execute: {}", ps.executeBatch().length);
			}
		}

		List list = logs;
		return list;
	}

	private static final Pattern INDEX_PATTERN =  Pattern.compile(" - \\d\\.jpe?g$");

	public static String noIndexName(String file) {
		return file.lastIndexOf('-') < 0 ? file : INDEX_PATTERN.matcher(file).replaceFirst("");
	}

	@SuppressWarnings("unchecked")
	private static <E, F extends MinimalChapter> IntObjectScatterMap<Map<String, F>> collectChapterNames(Object source, ToIntFunction<E> idMapper) {
		IntObjectScatterMap<Map<String, F>> result = new IntObjectScatterMap<>();
		BiConsumer<Integer, List<F>> consumer = (id, list) -> result.put(id, Checker.isEmpty(list) ? Collections.emptyMap() : list.stream().collect(Collectors.toMap(MinimalChapter::getFileName, Function.identity(), (a,b) -> a)));
		if(source instanceof Map) 
			((Map<E, List<F>>) source).forEach((id, list) -> consumer.accept(idMapper.applyAsInt(id), list));
		else if(source instanceof IntObjectMap) 
			HPPCUtils.forEach((IntObjectMap<List<F>>)source, (id, list) -> consumer.accept(id, list));
		else 
			throw new IllegalArgumentException(source.getClass().getCanonicalName());
		return result;
	}

	public static IntObjectMap<List<ChapterWithId>> getChapters(SQLiteDB db, OfInt mangaIds)
			throws SQLException {
		IntObjectMap<List<ChapterWithId>> map = new IntObjectScatterMap<>();

		String sql = selectWhereFieldInSQL(CHAPTERS_TABLE_NAME, MANGA_ID, mangaIds, "*").append(';').toString();
		IntFunction<List<ChapterWithId>>  computer = i -> new ArrayList<>();

		db.iterate(sql, rs -> HPPCUtils.computeIfAbsent(map, rs.getInt(MANGA_ID), computer).add(new ChapterWithId(rs)));
		return map;
	}

	public static <E extends Chapter> List<E> getChapters(SQLiteDB db, int mangaId,
			SqlFunction<ResultSet, E> chapterMapper) throws SQLException {
		return db.collectToList(selectSQL(CHAPTERS_TABLE_NAME, "*").append(" WHERE ").append(MANGA_ID).append('=')
				.append(mangaId).append(';').toString(), chapterMapper);
	}

	public static <E extends Chapter> List<E> reset(SQLiteDB db, MinimalManga manga, Iterable<E> knownChapters,
			BiFunction<OptionalDouble, String, E> chapterMaker) throws IOException {
		Map<String, OptionalDouble> map = chapterFileNames(manga.getDirPath())
				.collect(Collectors.toMap(s -> s, MinimalChapter::parseChapterNumber));

		if (map.values().stream().anyMatch(d -> !d.isPresent())) {
			Path p = manga.getDirPath();
			System.out.println("bad files in " + p.getFileName());
			map.keySet().forEach(s -> System.out.println("  " + s));
			map.values().removeIf(Objects::isNull);
		}

		if (map.isEmpty())
			throw new IOException("no chapters found");

		for (Chapter c : knownChapters) {
			OptionalDouble number = map.get(c.getFileName());
			if (!number.isPresent()) {
				System.out.println("  delete: " + c);
				c.setDeleted(true);
			} else
				c.setNumber(number.getAsDouble());
		}


		List<E> list = new ArrayList<>();
		knownChapters.forEach(list::add);
		if (!map.isEmpty())
			map.forEach((s, t) -> list.add(chapterMaker.apply(t, s)));  // TODO have some confusion about impl
		return list;
	}

	public static Stream<String> chapterFileNames(Path mangadir, boolean walkSimple) throws IOException {
		Objects.requireNonNull(mangadir);

		if (Files.notExists(mangadir))
			throw new FileNotFoundException(mangadir.toString());

		if (walkSimple) {
			String[] array = mangadir.toFile().list();
			if (Checker.isEmpty(array))
				return Stream.empty();

			return Arrays.stream(array).filter(s -> s.endsWith(".jpeg") || s.endsWith(".jpg"));
		}

		DirectoryStream<Path> strm = Files.newDirectoryStream(mangadir);
		return StreamSupport.stream(strm.spliterator(), false).onClose(() -> MyUtilsException.toUnchecked(() -> {
			strm.close();
			return null;
		})).filter(p -> {
			if (Files.isRegularFile(p) && !MyUtilsException.toUnchecked(() -> Files.isHidden(p))) {
				return true;
			} else {
				if (LOGGER.isDebugEnabled()) {
					try {
						LOGGER.debug(String.format("Skipping File {regular-file:%s, is-hidden:%s, file-path:%s}",
								Files.isRegularFile(p), Files.isHidden(p), p));
					} catch (IOException e) {
						LOGGER.debug(String.format("Skipping File {regular-file:%s, is-hidden:%s, file-path:%s}",
								Files.isRegularFile(p), e.toString(), p));
					}
				}
			}
			return false;
		}).map(p -> p.getFileName().toString());
	}

	private static String WALK_TYPE;
	private static boolean simplewalk;

	private static Stream<String> chapterFileNames(Path mangadir) throws IOException {
		if (WALK_TYPE == null) {
			WALK_TYPE = System2.lookup("MANGA_DIR_WALK_TYPE", "simple");
			simplewalk = WALK_TYPE.equalsIgnoreCase("simple");
			if (!simplewalk && !WALK_TYPE.equalsIgnoreCase("heavy"))
				throw new IllegalStateException("unknown value: MANGA_DIR_WALK_TYPE=" + WALK_TYPE);
		}

		return chapterFileNames(mangadir, simplewalk);
	}
}
