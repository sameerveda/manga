package sam.api.store;

import java.util.List;

import sam.api.store.entities.meta.SManga;

public interface Store {
	public List<? extends SManga> read() throws StoreException;
	public void save(List<? extends SManga> mangas) throws StoreException;
}
