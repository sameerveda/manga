package sam.api.store.entities.meta;

import sam.manga.samrock.mangas.MinimalManga;

public interface SManga extends MinimalManga {
	int getId();
    String[] getUrls();
    IMutableList<? extends SChapter> getChapters();
    int size();
}