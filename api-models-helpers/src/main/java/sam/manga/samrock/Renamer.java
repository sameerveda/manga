package sam.manga.samrock;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sam.io.fileutils.FileNameSanitizer;
import sam.myutils.Checker;
import sam.string.StringUtils;

public class Renamer {
	private static FileNameSanitizer _remover;
	private static Pattern manhua;
	/*
	 * why manual work instead of regex? because i have time :]
	 */
	public static String mangaDirName(String mangaName) {
		Objects.requireNonNull(mangaName, "mangaName ='" + mangaName + "'");
		
		char[] chars = mangaName.toCharArray();
		
		FileNameSanitizer rm = remover();
		Matcher m = manhua.matcher(new CharSequence() {
			@Override
			public CharSequence subSequence(int start, int end) {
				return new String(chars, start, end);
			}
			@Override public int length() { return chars.length; }
			@Override public char charAt(int index) { return chars[index]; }
		});

		if(m.find()) {
			for (int i = m.start(); i < chars.length; i++)
				chars[i] = '\0';
		}
		
		removeHtmlEntities(chars);
		replaceWindowReservedChars(rm, chars);
		rm.remove_non_space_white_spaces(chars);
		rm.multipleSpacesToNullChars(chars);
		rm.removeNullChars(chars);
		String str = remover().trimAndCreate(chars);

		if (str.isEmpty())
			throw new NullPointerException(
					"at start mangaName: " + mangaName + " and after formatting mangaName is empty a string");

		return str;

		/*
		 * same as
		 * 
		 * private String formatDirName(String mangaName) { if(mangaName != null &&
		 * !mangaName.trim().isEmpty()){ mangaName = mangaName
		 * .replaceAll("[\\Q%_<>:\\\"/*|?\\E]", " ")//% and _ are SQL keyChars rest
		 * window reserved keyChars .replaceAll("&\\w{1,4};", " ") .replaceAll("\\s+",
		 * " ") //remove all space characters except normal single space (" ") .trim()
		 * .replaceFirst("\\.+$", ""); //replace dot char at the end of name, as if it
		 * is left, then in naming folder or file windows removes it, and file path
		 * which contains the dot at the end will give error; } return mangaName == null
		 * || mangaName.isEmpty() ? null : mangaName; }
		 * 
		 */
	}
	/**
	 * remove html entities (&{7chars};)
	 */
	private static void removeHtmlEntities(char[] chars) {
		
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&') {
                loop: for (int j = i + 1; j < i + 7 && j < chars.length; j++) { // remove &\\w{1,7};
                    if (chars[j] == ';') {//
                        for (; i <= j; i++)
                            chars[i] = ' ';
                        break loop;
                    }
                }
            continue;
            }
            if (chars[i] == '_' || chars[i] == '%') // % and _ have special meaning in text search of SQL
                chars[i] = '\0';
        }
	}
	private static void replaceWindowReservedChars(FileNameSanitizer rm, char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == ':' || rm.isWindowsReservedChar(c))
				chars[i] = c == ':' ? ' ' : '\0';
		}
	}
	private static FileNameSanitizer remover() {
		if(_remover != null) return _remover;
		
		manhua = Pattern.compile("\\s+manh(?:w|u)a\\s*$", Pattern.CASE_INSENSITIVE);   
		return _remover = new FileNameSanitizer(StandardCharsets.UTF_8);
	}
	
	private static final Pattern chapterPattern = Pattern.compile("chapter", Pattern.CASE_INSENSITIVE | Pattern.LITERAL); 
	
	public static String makeChapterFileName(double number, String chapterFileName, String mangaName) {
		final String numS = StringUtils.doubleToString(number);
		if(Checker.isEmptyTrimmed(chapterFileName))
			return numS;

		chapterFileName = Pattern.compile(mangaName.replaceFirst("Manh(?:w|u)a", ""), Pattern.LITERAL | Pattern.CASE_INSENSITIVE)
				.matcher(chapterFileName)
				.replaceFirst("")
				.replace(numS, "");

		chapterFileName = chapterPattern.matcher(chapterFileName).replaceFirst("");
		
		char[] chars = (numS +" "+chapterFileName.trim()).toCharArray();
		
		FileNameSanitizer rm = remover();

		rm.removeUnmappableChars(chars);
		rm.replaceWindowReservedChars(chars);
		rm.remove_non_space_white_spaces(chars);
		rm.removeNullChars(chars);
		
		return rm.trimAndCreate(chars);
	}
}
