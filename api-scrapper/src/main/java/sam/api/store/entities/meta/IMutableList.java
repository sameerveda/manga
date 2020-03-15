package sam.api.store.entities.meta;

public interface IMutableList<T> extends Iterable<T> {
	public T add(T p);
	public T find(Object key);
	// public void remove(P p);
}
