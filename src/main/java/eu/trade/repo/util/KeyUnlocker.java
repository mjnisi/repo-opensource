package eu.trade.repo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * This utility class enable to unlock a key {@link Object} previously locked with the {@link KeySynchronizer} utility.
 * <p>
 * The unlock will be done at the end of the synchronised transaction. Please note that one instance of this class is enough for every thread.
 * 
 * @see TransactionSynchronization
 * @author porrjai
 */
public final class KeyUnlocker implements TransactionSynchronization {

	private static final Logger LOG = LoggerFactory.getLogger(KeyUnlocker.class);

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#suspend()
	 */
	@Override
	public void suspend() {
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#resume()
	 */
	@Override
	public void resume() {
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#flush()
	 */
	@Override
	public void flush() {
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCommit(boolean)
	 */
	@Override
	public void beforeCommit(boolean readOnly) {
		LOG.debug("beforeCommit");
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#beforeCompletion()
	 */
	@Override
	public void beforeCompletion() {
		LOG.debug("beforeCommit");
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#afterCommit()
	 */
	@Override
	public void afterCommit() {
		LOG.debug("afterCommit");
		// Nothing to do.
	}

	/**
	 * @see org.springframework.transaction.support.TransactionSynchronization#afterCompletion(int)
	 */
	@Override
	public void afterCompletion(int status) {
		LOG.debug("afterCompletion");
		KeySynchronizer.desynch();
	}

	/**
	 * All instances are equals and has the same hashcode (0);
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 0;
	}

	/**
	 * All instances are equals and has the same hashcode (0);
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof KeyUnlocker;
	}
}
