package sam.manga.mangarock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import sam.manga.Env;
import sam.myutils.System2;
import sam.sql.sqlite.SQLiteDB;

public interface Mangarock {
	static Path dbPath() {
		Path p = Paths.get(System2.lookup("MANGAROCK_DB", Env.MANGAROCK_INPUT_DB));
		return Files.exists(p) ? p : Paths.get(Env.MANGAROCK_DB_BACKUP);
	}
	static SQLiteDB defaultDB() throws SQLException {
		return new SQLiteDB(dbPath());
	}
}
