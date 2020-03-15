package sam.api.store.entities.meta;

import sam.manga.samrock.chapters.MinimalChapter;

public interface SChapter extends Iterable<SPage>, MinimalChapter {
	int getId();
    String getUrl();
    String getVolume();
	int size();
	IMutableList<? extends SPage> getPages();
}