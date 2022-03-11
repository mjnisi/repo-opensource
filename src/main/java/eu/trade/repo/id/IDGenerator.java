package eu.trade.repo.id;

/**
 * Generates a unique id used to store in the objectId.
 *
 */
public interface IDGenerator {
	String next();
	void reset();
}
