package sam.manga.samrock.chapters;

import java.util.OptionalDouble;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MinimalChapter { 
	public double getNumber();
	public String getTitle();
	public String getFileName();

	public static String getTitleFromFileName(String filename) {
		if(filename == null)
			return filename;
		if(filename.endsWith(".jpeg"))
			return filename.substring(0, filename.length() - 5);
		if(filename.endsWith(".jpg"))
			return filename.substring(0, filename.length() - 4);

		return filename;
	}
	
	static Pattern NUMBER_EXTRACT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");
	
	public static OptionalDouble parseChapterNumber(String fileName) {
		Matcher m = NUMBER_EXTRACT_PATTERN.matcher(fileName);
		
		if(m.find()) {
			try {
				return OptionalDouble.of(Double.parseDouble(m.group(1)));
			} catch (NumberFormatException e) { }
		}
		return OptionalDouble.empty();
	}
}