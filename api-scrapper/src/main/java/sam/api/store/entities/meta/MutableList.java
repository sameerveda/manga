package sam.api.store.entities.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import sam.myutils.Checker;

public abstract class MutableList<T> implements IMutableList<T> {
	protected List<T> items;
	protected Object notFound;
	
	public MutableList(List<T> items) {
		this.items = items;
	}
	
	protected abstract Object keyOf(T item);
	
	@Override
	public T add(T item) {
		if(item == null)
			return null;
		
		if(items == null)  {
			items = new ArrayList<>();
			items.add(item);
			return item;
		}
		if(notFound != null && notFound.equals(keyOf(item))) {
			items.add(item);
			notFound = null;
			return item;
		}
		T p = find(keyOf(item));
		if(p == null)
			items.add(p = item);
		return p;
	}
	
	@Override
	public T find(Object key) {
		Objects.requireNonNull(key);
		if(Checker.isEmpty(items))
			return null;

		for (int i = 0; i < items.size(); i++) {
			Object s = keyOf(items.get(i));
			if(s.equals(key))
				return items.get(i);
		}
		notFound = key;
		return null;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
	@Override
	public Iterator<T> iterator() {
		if(items.isEmpty())
			return Collections.emptyIterator();
		Iterator<T> itr = items.iterator();
		
		// immutable iterator
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return itr.hasNext();
			}

			@Override
			public T next() {
				return itr.next();
			}
		};
	}

	public int size() {
		return items.size();
	}

	public List<T> asList() {
		return Collections.unmodifiableList(items);
	}
}
