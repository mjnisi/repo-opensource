package eu.trade.repo.util;

/**
 * Interface for cleanable objects.
 * <p>
 * The main purpose is to be able to clean all cached information (v.g. thread local variables).
 * 
 * @author porrjai
 */
public interface Cleanable {

	/**
	 * Will clean all the cached resources.
	 */
	void clean();
}
