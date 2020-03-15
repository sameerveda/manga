package sam.manga.samrock.chapters;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChapterWithId extends Chapter {
	public final int id;

	public ChapterWithId(ResultSet rs) throws SQLException {
		super(rs);
		this.id = rs.getInt(ChaptersMeta.CHAPTER_ID);
	}
}
