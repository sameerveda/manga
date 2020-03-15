package sam.manga.samrock.mangas;

import java.nio.file.Path;

import sam.manga.samrock.chapters.MinimalChapter;

public interface MinimalManga extends Iterable<MinimalChapter> {
	public int getMangaId();
    public String getDirName();
    public Path getDirPath();
    public String getMangaName();
}
