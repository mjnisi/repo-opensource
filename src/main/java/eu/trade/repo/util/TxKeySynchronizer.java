package eu.trade.repo.util;

/**
 * Locks an {@link Object} key in order to synchronise a block of code within a transaction.
 * <p>
 * The acquired lock will be released after the transaction completion.
 * 
 * @author porrjai
 */
public interface TxKeySynchronizer {

	/**
	 * Locks an {@link Object} key in order to synchronise a block of code within a transaction.
	 * 
	 * @param key {@link Object} The key to be locked.
	 */
	void synch(Object key);
}
