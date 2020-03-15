package sam.manga.samrock.chapters;

import java.util.ArrayList;
import java.util.List;

import sam.manga.samrock.mangas.MinimalManga;

class ChaptersUpdateLogImpl implements ChaptersUpdateLog {
	public final MinimalManga parent;
	public final List<ChapterWithId> delete = new ArrayList<>();
	public final List<MinimalChapter> newFromData = new ArrayList<>();
	public final List<ChapterFile> newParsingFile = new ArrayList<>();
	public final List<ChapterWithId> noUpdate = new ArrayList<>();
	public int readCount, total, chapCountPc;

	public ChaptersUpdateLogImpl(MinimalManga parent) {
		this.parent = parent;
	}

	@Override
	public List<ChapterWithId> getDelete() {
		return delete;
	}

	@Override
	public List<MinimalChapter> getNewFromData() {
		return newFromData;
	}

	@Override
	public List<ChapterFile> getNewParsingFile() {
		return newParsingFile;
	}

	@Override
	public List<ChapterWithId> getNoUpdate() {
		return noUpdate;
	}

	@Override
	public int readCount() {
		return readCount;
	}
	@Override
	public int total() {
		return total;
	}

	@Override
	public int unreadCount() {
		return chapCountPc() - readCount();
	}

	@Override
	public int chapCountPc() {
		return chapCountPc;
	}

	@Override
	public MinimalManga getParent() {
		return parent;
	}
}