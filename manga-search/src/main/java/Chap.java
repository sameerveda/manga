import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Chap implements Serializable {
	private static final long serialVersionUID = 7561090042762041553L;
	
	final double number;
	final String name;
	private String s ;
	
	public Chap(ResultSet rs) throws SQLException {
		this.number = rs.getDouble("number");
		this.name = rs.getString("name");
	}
	
	public Chap(double number, String name) {
		this.number = number;
		this.name = name;
	}

	@Override
	public String toString() {
		return s != null  ? s : (s = doubleToString(number)+": "+name);
	}
	public String doubleToString(double d) {
		if(d == (int)d)
			return String.valueOf((int)d);
		else 
			return String.valueOf(d);
	}
}