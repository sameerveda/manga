package sam.manga.samrock;

import java.sql.SQLException;

import sam.manga.Env;
import sam.myutils.System2;
import sam.sql.sqlite.SQLiteDB;

public interface Samrock  {
	static SQLiteDB defaultDB() throws SQLException {
		return new SQLiteDB(System2.lookup("SAMROCK_DB_PATH", Env.SAMROCK_DB));
	}
}
