package sam.manga.mangarock;

import static sam.manga.mangarock.MangarockMangaMeta.ALIAS;
import static sam.manga.mangarock.MangarockMangaMeta.ARTWORKS;
import static sam.manga.mangarock.MangarockMangaMeta.AUTHOR;
import static sam.manga.mangarock.MangarockMangaMeta.AUTHORS;
import static sam.manga.mangarock.MangarockMangaMeta.CATEGORIES;
import static sam.manga.mangarock.MangarockMangaMeta.CHARACTERS;
import static sam.manga.mangarock.MangarockMangaMeta.COVER;
import static sam.manga.mangarock.MangarockMangaMeta.DESCRIPTION;
import static sam.manga.mangarock.MangarockMangaMeta.DIRECTION;
import static sam.manga.mangarock.MangarockMangaMeta.EXTRA;
import static sam.manga.mangarock.MangarockMangaMeta.GENRES;
import static sam.manga.mangarock.MangarockMangaMeta.ID;
import static sam.manga.mangarock.MangarockMangaMeta.LASTUPDATE;
import static sam.manga.mangarock.MangarockMangaMeta.LAST_VIEW;
import static sam.manga.mangarock.MangarockMangaMeta.MANGA_TABLE_NAME;
import static sam.manga.mangarock.MangarockMangaMeta.MRS_SERIES;
import static sam.manga.mangarock.MangarockMangaMeta.NAME;
import static sam.manga.mangarock.MangarockMangaMeta.OID;
import static sam.manga.mangarock.MangarockMangaMeta.RANK;
import static sam.manga.mangarock.MangarockMangaMeta.REMOVED;
import static sam.manga.mangarock.MangarockMangaMeta.SOURCE_ID;
import static sam.manga.mangarock.MangarockMangaMeta.STATUS;
import static sam.manga.mangarock.MangarockMangaMeta.THUMBNAILURL;
import static sam.manga.mangarock.MangarockMangaMeta.TOTALCHAPTERS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import sam.sql.sqlite.SQLiteDB;


public class MangarockManga {
	private final String author;
	private final String categories;
	private final String thumbnailUrl;
	private final String description;
	private final int source_id;
	private final String name;
	private final int totalChapters;
	private final long lastUpdate;
	private final long last_view;
	private final int id;
	private final int direction;
	private final short removed;
	private final short status;
	private final int rank;
	private final String oid;
	private final String genres;
	private final String extra;
	private final String cover;
	private final String characters;
	private final String authors;
	private final String artworks;
	private final String mrs_series;
	private final String alias;

	public MangarockManga(ResultSet rs) throws SQLException {
		this.author = rs.getString(AUTHOR);
		this.categories = rs.getString(CATEGORIES);
		this.thumbnailUrl = rs.getString(THUMBNAILURL);
		this.description = rs.getString(DESCRIPTION);
		this.source_id = rs.getInt(SOURCE_ID);
		this.name = rs.getString(NAME);
		this.totalChapters = rs.getInt(TOTALCHAPTERS);
		this.lastUpdate = rs.getLong(LASTUPDATE);
		this.last_view = rs.getLong(LAST_VIEW);
		this.id = rs.getInt(ID);
		this.direction = rs.getInt(DIRECTION);
		this.removed = rs.getShort(REMOVED);
		this.status = rs.getShort(STATUS);
		this.rank = rs.getInt(RANK);
		this.oid = rs.getString(OID);
		this.genres = rs.getString(GENRES);
		this.extra = rs.getString(EXTRA);
		this.cover = rs.getString(COVER);
		this.characters = rs.getString(CHARACTERS);
		this.authors = rs.getString(AUTHORS);
		this.artworks = rs.getString(ARTWORKS);
		this.mrs_series = rs.getString(MRS_SERIES);
		this.alias = rs.getString(ALIAS);
	}
	public MangarockManga(String author, String categories, String thumbnailUrl, String description, int source_id, String name, int totalChapters, long lastUpdate, long last_view, int id, int direction, short removed, short status, int rank, String oid, String genres, String extra, String cover, String characters, String authors, String artworks, String mrs_series, String alias){
		this.author = author;
		this.categories = categories;
		this.thumbnailUrl = thumbnailUrl;
		this.description = description;
		this.source_id = source_id;
		this.name = name;
		this.totalChapters = totalChapters;
		this.lastUpdate = lastUpdate;
		this.last_view = last_view;
		this.id = id;
		this.direction = direction;
		this.removed = removed;
		this.status = status;
		this.rank = rank;
		this.oid = oid;
		this.genres = genres;
		this.extra = extra;
		this.cover = cover;
		this.characters = characters;
		this.authors = authors;
		this.artworks = artworks;
		this.mrs_series = mrs_series;
		this.alias = alias;
	}


	public String getAuthor(){ return this.author; }
	public String getCategories(){ return this.categories; }
	public String getThumbnailUrl(){ return this.thumbnailUrl; }
	public String getDescription(){ return this.description; }
	public int getSourceId(){ return this.source_id; }
	public String getName(){ return this.name; }
	public int getTotalChapters(){ return this.totalChapters; }
	public long getLastUpdate(){ return this.lastUpdate; }
	public long getLastView(){ return this.last_view; }
	public int getId(){ return this.id; }
	public int getDirection(){ return this.direction; }
	public short getRemoved(){ return this.removed; }
	public short getStatus(){ return this.status; }
	public int getRank(){ return this.rank; }
	public String getOid(){ return this.oid; }
	public String getGenres(){ return this.genres; }
	public String getExtra(){ return this.extra; }
	public String getCover(){ return this.cover; }
	public String getCharacters(){ return this.characters; }
	public String getAuthors(){ return this.authors; }
	public String getArtworks(){ return this.artworks; }
	public String getMrsSeries(){ return this.mrs_series; }
	public String getAlias(){ return this.alias; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+MANGA_TABLE_NAME;
	public static List<MangarockManga> getAll(SQLiteDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, MangarockManga::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static MangarockManga getById(SQLiteDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, MangarockManga::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + MANGA_TABLE_NAME+"("+String.join(",", AUTHOR,CATEGORIES,THUMBNAILURL,DESCRIPTION,SOURCE_ID,NAME,TOTALCHAPTERS,LASTUPDATE,LAST_VIEW,ID,DIRECTION,REMOVED,STATUS,RANK,OID,GENRES,EXTRA,COVER,CHARACTERS,AUTHORS,ARTWORKS,MRS_SERIES,ALIAS)+") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final int insert(Iterable<MangarockManga> data, SQLiteDB db) throws SQLException {
		Iterator<MangarockManga> itr = data.iterator();  
		if(!itr.hasNext()) 
			return 0;

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			while (itr.hasNext()) {
				MangarockManga item = itr.next();
				
				p.setString(1,item.author);
				p.setString(2,item.categories);
				p.setString(3,item.thumbnailUrl);
				p.setString(4,item.description);
				p.setInt(5,item.source_id);
				p.setString(6,item.name);
				p.setInt(7,item.totalChapters);
				p.setLong(8,item.lastUpdate);
				p.setLong(9,item.last_view);
				p.setInt(10,item.id);
				p.setInt(11,item.direction);
				p.setShort(12,item.removed);
				p.setShort(13,item.status);
				p.setInt(14,item.rank);
				p.setString(15,item.oid);
				p.setString(16,item.genres);
				p.setString(17,item.extra);
				p.setString(18,item.cover);
				p.setString(19,item.characters);
				p.setString(20,item.authors);
				p.setString(21,item.artworks);
				p.setString(22,item.mrs_series);
				p.setString(23,item.alias);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

