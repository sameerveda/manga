	package sam.api.chapter;

import java.util.function.DoublePredicate;

public abstract class ChapterFilterBase implements DoublePredicate {
	public static final DoublePredicate ALL_ACCEPT_FILTER = new DoublePredicate() {
		@Override
		public boolean test(double value) {
			return true;
		}
		@Override
		public String toString() {
			return "[ALL]";
		}
	};
	
	public final int manga_id;
	protected boolean complete;
	
	public ChapterFilterBase(int manga_id) {
		this.manga_id = manga_id;
	}
	
	protected boolean check() {
		if(complete)
			throw new IllegalStateException("closed to modifications");
		return true;
	}
	
	public void setCompleted() {
		complete = true;
	}
	@Override
	public int hashCode() {
		return manga_id;
	}
}
