/**
 * 
 */
package eu.trade.repo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * {@link TxKeySynchronizer} implementation based on Spring {@link TransactionSynchronizationManager}
 * 
 * @author porrjai
 */
public class TxKeySynchronizerImpl implements TxKeySynchronizer {

	private static final Logger LOG = LoggerFactory.getLogger(TxKeySynchronizerImpl.class);

	private final KeyUnlocker keyUnlocker = new KeyUnlocker();

	/**
	 * @see eu.trade.repo.util.TxKeySynchronizer#synch(java.lang.Object)
	 */
	@Override
	public void synch(Object key) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			LOG.error("Trying to synch an object key {} in a non synchonizable context.", key);
			throw new IllegalStateException("Trying to synch an object key in a non synchonizable context.");
		}
		if (!TransactionSynchronizationManager.getSynchronizations().contains(keyUnlocker)) {
			TransactionSynchronizationManager.registerSynchronization(keyUnlocker);
		}
		KeySynchronizer.synch(key);
	}

}
