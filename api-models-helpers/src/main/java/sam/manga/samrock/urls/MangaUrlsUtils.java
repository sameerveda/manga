package sam.manga.samrock.urls;

import static sam.manga.samrock.urls.MangaUrlsBaseMeta.BASE;
import static sam.manga.samrock.urls.MangaUrlsBaseMeta.COLUMN_NAME;
import static sam.manga.samrock.urls.MangaUrlsMeta.MANGAFOX;
import static sam.manga.samrock.urls.MangaUrlsMeta.MANGAHERE;
import static sam.manga.samrock.urls.MangaUrlsMeta.MANGA_ID;
import static sam.manga.samrock.urls.MangaUrlsMeta.URL_TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator.OfInt;
import java.util.function.UnaryOperator;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectScatterMap;

import sam.sql.JDBCHelper;
import sam.sql.sqlite.SQLiteDB;
public final class MangaUrlsUtils {
    private final String mangafoxBase;
    private final String mangahereBase;
    private final SQLiteDB db; 

    public MangaUrlsUtils(SQLiteDB db) throws SQLException {
        Map<String, String> baseMap = new HashMap<>();
        this.db = db;

        try(ResultSet rs = db.executeQuery("SELECT * FROM ".concat(MangaUrlsBaseMeta.URL_BASE_TABLE_NAME));
                ) {
            UnaryOperator<String> map = temp -> temp.endsWith("/") ? temp : temp.concat("/");

            while(rs.next())
                baseMap.put(rs.getString(COLUMN_NAME), map.apply(rs.getString(BASE)));
        }
        mangafoxBase = baseMap.get(MANGAFOX);
        mangahereBase = baseMap.get(MANGAHERE);
    }
    
    public String resolveWith(String url_name, String column_name) {
    	Objects.requireNonNull(url_name);
    	
    	switch (column_name) {
			case MANGAFOX: return mangafoxBase + url_name;
			case MANGAHERE: return mangahereBase + url_name;
			default: throw new IllegalArgumentException(column_name);
		}
    }

    /**
     * 
     * @param mangaIds
     * @param db
     * @param mangaUrlsMeta either {@link IColumnName.MangaUrls#MANGAFOX} or {@link IColumnName.MangaUrls#MANGAHERE} 
     * @return
     * @throws SQLException
     */
    public IntObjectMap< String> getUrls(OfInt mangaIds, String mangaUrlsMeta) throws SQLException{
        String base = MangaUrlsMeta.MANGAFOX.equals(mangaUrlsMeta) ? mangafoxBase : MangaUrlsMeta.MANGAHERE.equals(mangaUrlsMeta) ? mangahereBase : null;
        
        if(base == null)
            throw new SQLException("column name not found in db: "+mangaUrlsMeta);

        UnaryOperator<String> join = name -> name == null ? null : base + name;

        IntObjectMap< String> map = new IntObjectScatterMap<>();
        String sql = JDBCHelper.selectWhereFieldInSQL(URL_TABLE_NAME, MANGA_ID, mangaIds, mangaUrlsMeta).toString(); 

        db.iterate(sql, rs -> map.put(rs.getInt(MANGA_ID), join.apply(rs.getString(mangaUrlsMeta))));
        return map;
    }
    public List<MangaUrl> getMangaUrls(OfInt mangaIds) throws SQLException{
        List<MangaUrl> map = new ArrayList<MangaUrl>();
        db.iterate(JDBCHelper.selectWhereFieldInSQL(URL_TABLE_NAME, MANGA_ID, mangaIds, "*").toString(), 
                rs -> map.add(new MangaUrl(rs.getInt(MANGA_ID), rs.getString(MANGAFOX), rs.getString(MANGAHERE))));

        return map;
    }

    public MangaUrl getMangaUrl(int mangaId) throws SQLException {
        return db.executeQuery(
        		JDBCHelper.selectWhereFieldEqSQL(URL_TABLE_NAME, MANGA_ID, mangaId, new String[]{"*"}).toString(), 
                rs -> !rs.next() ? null : new MangaUrl(rs.getInt(MANGA_ID), rs.getString(MANGAFOX), rs.getString(MANGAHERE)));
    }
    public MangaUrl parseMangaUrl(int mangaId, String url) throws SQLException{
        return parseMangaUrl(mangaId, url, null);
    }    
    public MangaUrl parseMangaUrl(int mangaId, String url1, String url2) throws SQLException {
        String[] s1 = split(url1);
        String[] s2 = split(url2);

        if(s1 == null && s2 == null)
            return null;

        String fox = null, here = null;

        if(!(s1 == null || s2 == null) && Objects.equals(s1[0], s2[0]) && !Objects.equals(s1[1], s2[1]))
            throw new SQLException("colliding urls: "+s1[1] +",  "+ s2[1]);
        
        if(s1 != null) {
            fox = s1[0] == mangafoxBase ? s1[1] : null;
            here = s1[0] == mangahereBase ? s1[1] : null;
        }
        if(s2 != null) {
            fox = s2[0] == mangafoxBase ? s2[1] : fox;
            here = s2[0] == mangahereBase ? s2[1] : here;
        }
        return new MangaUrl(mangaId, fox, here);
    }
    private String[] split(String url) {
        if(url == null)
            return null;

        int start = url.lastIndexOf('/');
        if(start < 0)
            return null;

        int end = url.length() - 1;

        if(start == end)
            start = url.lastIndexOf('/', --end);

        if(start < 0 || start >= end)
            return null;

        String base = url.substring(0, start + 1);
        String name = url.substring(start + 1, end + 1);

        if(base.equals(mangafoxBase))
            return new String[] {mangafoxBase, name};
        if(base.equals(mangahereBase))
            return new String[] {mangahereBase, name};

        return null;
    }
    public int commitMangaUrls(List<MangaUrl> urls) throws SQLException {
        IntObjectMap< MangaUrl> map = new IntObjectScatterMap<>();
        getMangaUrls(urls.stream().mapToInt(MangaUrl::getMangaId).iterator()).forEach(m -> map.put(m.getMangaId(), m));

        try(PreparedStatement insert = db.prepareStatement(JDBCHelper.insertSQL(URL_TABLE_NAME, MANGA_ID, MANGAFOX, MANGAHERE));
                PreparedStatement set = db.prepareStatement(JDBCHelper.updatePreparedSql(URL_TABLE_NAME, MANGAFOX, MANGAHERE).append(" WHERE ").append(MANGA_ID).append("=?;").toString());
                ) {
            boolean insertB = false, setB = false; 
            for (MangaUrl _new : urls) {
                MangaUrl old = map.get(_new.mangaId);
                if(old == null) {
                    insertB = true;
                    insert.setInt(1, _new.mangaId);
                    insert.setString(2, _new.getMangafoxName());
                    insert.setString(3, _new.getMangahereName());
                    insert.addBatch();
                } else {
                    setB = true;
                    set.setString(1, _new.getMangafoxName() == null ? old.getMangafoxName() : _new.getMangafoxName());
                    set.setString(2, _new.getMangahereName() == null ? old.getMangahereName() : _new.getMangahereName());
                    set.setInt(3, _new.mangaId);
                    set.addBatch();
                }
            }
            int i = 0;
            if(insertB)
                i += insert.executeBatch().length;
            if(setB)
                i += set.executeBatch().length;

            return  i;
        }
    }
    public class MangaUrl {
        final int mangaId;
        private final String mangafox, mangahere;

        MangaUrl(int id, String mangafox, String mangahere) {
            this.mangaId = id;
            this.mangafox = mangafox;
            this.mangahere = mangahere;
        }
        public int getMangaId() {
            return mangaId;
        }
        public String getMangafoxUrl() {
            return mangafox == null ? null : mangafoxBase + mangafox;
        }
        public String getMangahereUrl() {
            return mangahere == null ? null : mangahereBase + mangahere;
        }
        public String getMangafoxName() {
            return mangafox;
        }
        public String getMangahereName() {
            return mangahere;
        }
        public String getName(String MangaUrlsMeta) {
            switch (MangaUrlsMeta) {
                case MANGAFOX:
                    return getMangafoxName();
                case MANGAHERE:
                    return getMangahereName();
            }
            return null;
        }
        public String getUrl(String MangaUrlsMeta) {
            switch (MangaUrlsMeta) {
                case MANGAFOX:
                    return getMangafoxUrl();
                case MANGAHERE:
                    return getMangahereUrl();
            }
            return null;
        }
        @Override
        public String toString() {
            return "MangaUrl [id=" + mangaId + ", mangafox=" + getMangafoxUrl() + ", mangahere=" + getMangahereUrl() + "]";
        }
    }
}
