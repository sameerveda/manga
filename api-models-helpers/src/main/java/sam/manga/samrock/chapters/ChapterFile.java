package sam.manga.samrock.chapters;

public class ChapterFile implements MinimalChapter {
	private double number = Integer.MIN_VALUE;
	private String title = null;
	private final String filename;

	public ChapterFile(String filename) {
		this.filename = filename;
	}

	@Override
	public double getNumber() {
		if (number == Integer.MIN_VALUE)
			number = MinimalChapter.parseChapterNumber(filename).orElse(-1);
		return number;
	}

	@Override
	public String getTitle() {
		if (title == null)
			title = MinimalChapter.getTitleFromFileName(filename);
		return title;
	}

	@Override
	public String getFileName() {
		return filename;
	}
}