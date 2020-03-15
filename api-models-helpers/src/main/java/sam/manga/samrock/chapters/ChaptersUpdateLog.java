package sam.manga.samrock.chapters;

import java.util.List;

import sam.manga.samrock.mangas.MinimalManga;

public interface ChaptersUpdateLog {
	List<ChapterWithId> getDelete();
	List<MinimalChapter> getNewFromData();
	List<ChapterFile> getNewParsingFile();
	List<ChapterWithId> getNoUpdate();
	
	int readCount(); 
	int unreadCount();
	int chapCountPc();
	MinimalManga getParent();
	int total();
}