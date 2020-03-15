package sam.api.chapter;

import java.util.Arrays;
import java.util.BitSet;

public class ChapterFilter extends ChapterFilterBase {
	private BitSet bitset;
	private double[] array;
	private int index;

	private static final int BITSET_MAX = 500;

	public ChapterFilter(int manga_id) {
		super(manga_id);
	}
	public void add(double value) {
		check();

		int n = (int)value;
		
		if(value == n) 
			addInt(n);
		 else 
			addDouble(value);
	}
	private void addDouble(double value) {
		check();

		if(array == null)
			array = new double[10];

		if(index >= array.length)
			array = Arrays.copyOf(array, (int)(array.length*1.5));

		array[index++] = value; 
	}
	private void addInt(int n) {
		check();

		if(n >= BITSET_MAX)
			addDouble(n);
		else {
			if(bitset == null)
				bitset = new BitSet();

			bitset.set(n);
		}
	}
	@Override
	public void setCompleted() {
		if(complete)
			return;

		super.setCompleted();

		if(array != null) {
			array = Arrays.copyOf(array, index);
			Arrays.sort(array);
		}
	}

	@Override
	public boolean test(double value) {
		if(!complete)
			throw new IllegalStateException("not completed");

		int n = (int)value;
		if(n == value && n < BITSET_MAX)
			return bitset != null && bitset.get(n);

		if(array != null)
			return (Arrays.binarySearch(array, value) >= 0);

		return false;
	}
}
