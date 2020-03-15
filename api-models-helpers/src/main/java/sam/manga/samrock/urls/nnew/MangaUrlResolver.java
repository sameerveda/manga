package sam.manga.samrock.urls.nnew;

import static sam.manga.samrock.urls.MangaUrlsBaseMeta.BASE;
import static sam.manga.samrock.urls.MangaUrlsBaseMeta.COLUMN_NAME;
import static sam.manga.samrock.urls.MangaUrlsBaseMeta.URL_BASE_TABLE_NAME;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import sam.myutils.Checker;
import sam.sql.JDBCHelper;
import sam.sql.sqlite.SQLiteDB;


public final class MangaUrlResolver implements Function<String, String> {
	final String column_name;
	final String base;

	MangaUrlResolver(ResultSet rs) throws SQLException {
		this.column_name = rs.getString(COLUMN_NAME);
		String s = rs.getString(BASE);
		this.base = s.charAt(s.length() - 1) == '/' ? s : s.concat("/");
	}
	MangaUrlResolver(String url, String column_name) throws MalformedURLException{
		Checker.assertTrue(Checker.isEmptyTrimmed(column_name), () -> "bad value for column_name:\""+column_name+"\"");
		
		url = url.replace('\\', '/');
		this.column_name = column_name;

		if(!(url.startsWith("http:") || url.startsWith("https:")))
			throw new IllegalArgumentException("unsupported protocol: "+new URL(url).getProtocol());

		if(url.charAt(url.length() - 1) != '/')
			url = url.concat("/");

		this.base = url;
	}
	
	@Override
	public String apply(String suffix) {
		return resolve(suffix);
	}
	
	public String resolve(String suffix) {
		if(Checker.isEmptyTrimmed(suffix))
			throw new IllegalArgumentException("bad value for suffix: \""+suffix+"\"");
		return  base.concat(suffix.charAt(0) == '/' ? suffix.substring(1) : suffix);
	}

	public String getColumnName(){ return this.column_name; }
	public String getBase(){ return this.base; }

	/**
	 * @param db
	 * @return Map(column_name, UrlsPrefixImpl)
	 * @throws SQLException
	 */
	public static  Map<String, MangaUrlResolver> getAll(SQLiteDB db) throws SQLException{
		return get(db);
	}
	
	/**
	 * @param db
	 * @return Map(column_name, UrlsPrefixImpl)
	 * @throws SQLException
	 */
	public static  Map<String, MangaUrlResolver> get(SQLiteDB db, String... columnNames) throws SQLException{
		String condition = ";";
		if(Checker.isNotEmpty(columnNames)) 
			condition = " WHERE " + COLUMN_NAME + " IN('" + String.join("','", columnNames) + "');";
		
		return db.collectToMap("SELECT * FROM " + URL_BASE_TABLE_NAME + condition, rs -> rs.getString(COLUMN_NAME), MangaUrlResolver::new);
	}
	
	static int insert(Iterable<MangaUrlResolver> data, SQLiteDB db)  throws SQLException {
		Objects.requireNonNull(data);
		
		Iterator<MangaUrlResolver> itr = data.iterator();
		if(!itr.hasNext())
			return 0;
		
		try(PreparedStatement p = db.prepareStatement(JDBCHelper.insertSQL(URL_BASE_TABLE_NAME, COLUMN_NAME, BASE))) {
			while (itr.hasNext()) {
				MangaUrlResolver u = itr.next();
				p.setString(1,u.column_name);
				p.setString(2,u.base);
				p.addBatch();
			} 
			return p.executeBatch().length;
		}
	} 
}
