package sam.manga.samrock.urls.nnew;


import static sam.manga.samrock.urls.MangaUrlsMeta.MANGA_ID;
import static sam.manga.samrock.urls.MangaUrlsMeta.URL_TABLE_NAME;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.PrimitiveIterator.OfInt;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectScatterMap;

import sam.myutils.Checker;
import sam.myutils.HPPCUtils;
import sam.sql.JDBCHelper;
import sam.sql.sqlite.SQLiteDB;

public final class MangaUrlsUtils {
    private final SQLiteDB db; 
    private final Map<String, MangaUrlResolver> prefixes;

    public MangaUrlsUtils(SQLiteDB db) throws SQLException {
    	this.db = db;
    	this.prefixes = Collections.unmodifiableMap(MangaUrlResolver.getAll(db)); 
    }
    
    /**
     * @param mangaIds
     * @param db
     * @param mangaUrlsMeta either {@link IColumnName.MangaUrls#MANGAFOX} or {@link IColumnName.MangaUrls#MANGAHERE} 
     * @return
     * @throws SQLException
     */
    public IntObjectMap< String> getUrls(OfInt mangaIds, String urlColumn) throws SQLException{
    	return _getUrls(mangaIds, urlColumn);
    }
    public IntObjectMap< String> getAllUrls(String urlColumn) throws SQLException{
    	return _getUrls(LOAD_ALL, urlColumn);
    }
    private static final OfInt LOAD_ALL = new OfInt() {
		@Override public boolean hasNext() { throw new UnsupportedOperationException(); }
		@Override public int nextInt() { throw new UnsupportedOperationException(); }
	};
	
    private IntObjectMap< String> _getUrls(OfInt mangaIds, String urlColumn) throws SQLException {
    	Objects.requireNonNull(urlColumn);
    	Objects.requireNonNull(mangaIds);
    	
    	MangaUrlResolver prefix = prefixes.get(urlColumn);
    	if(prefix == null)
    		throw new IllegalArgumentException("unknown urlColumn: "+urlColumn);
    	
    	StringBuilder sql;
        
    	if(mangaIds == LOAD_ALL) {
    		sql = JDBCHelper.selectSQL(URL_TABLE_NAME, MANGA_ID, urlColumn);
    	} else {
    		if(Checker.isEmpty(mangaIds))
        		return new IntObjectScatterMap<>();
    		sql = JDBCHelper.selectWhereFieldInSQL(URL_TABLE_NAME, MANGA_ID, mangaIds, urlColumn);
    	}
    	
    	IntObjectScatterMap<String> result = new IntObjectScatterMap<>();
        db.iterator(sql.toString(), rs -> result.put(rs.getInt(MANGA_ID), prefix.resolve(rs.getString(urlColumn))));
        return result;
	}
    public Map<String, MangaUrlResolver> getPrefixes() {
		return Collections.unmodifiableMap(prefixes);
	}
	public static String name(String url) {
		int n = url.lastIndexOf('/');

		if(n >= 0) {
			if(n == url.length() - 1)
				url = url.substring(url.lastIndexOf('/', n - 1) + 1, n);
			else
				url = url.substring(n + 1);
		}
		return url;
	}
}
