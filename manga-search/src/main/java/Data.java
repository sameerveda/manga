import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Data implements Serializable {
	private static final long serialVersionUID = -6364050856823688552L;
	
	public final int manga_id;
	public final String manga_name;
	public final String manga_name_lowercased;
	public final String last_chapter;
	
	public Data(ResultSet rs) throws SQLException {
		this.manga_id = rs.getInt("manga_id");
		this.manga_name = rs.getString("manga_name");
		this.manga_name_lowercased = manga_name.toLowerCase();
		this.last_chapter = rs.getString("name");
	}
	
	public Data(int manga_id, String manga_name, String last_chapter) {
		this.manga_id = manga_id;
		this.manga_name = manga_name;
		this.manga_name_lowercased = manga_name.toLowerCase();
		this.last_chapter = last_chapter;
		
	}



	public String getDirnameLowercased() {
		return manga_name_lowercased;
	}
	@Override
	public String toString() {
		return "Data [manga_id=" + manga_id + ", manga_name=" + manga_name + ", last_chapter=" + last_chapter + "]";
	}
}
