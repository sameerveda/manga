package sam.api;

/**
 * list of runnables that will be called when JVM stops
 * @author sameer
 *
 */
public interface ShutdownHooks {
	public void addShutdownHook(Runnable runnable);
}
