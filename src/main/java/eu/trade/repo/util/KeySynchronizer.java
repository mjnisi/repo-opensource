package eu.trade.repo.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronise a block of code based on an {@link Object} key.
 * <p>
 * Please note the this utility is intended to lock only one block of code using only one key. If the same thread needs to lock more than one key at the same time,
 * this class must be refactored.
 * 
 * @author porrjai
 */
public final class KeySynchronizer implements Cleanable {

	private static final Logger LOG = LoggerFactory.getLogger(KeySynchronizer.class);

	private static final Map<Object, KeySynchronizer> SYNC_MAP = new HashMap<Object, KeySynchronizer>();
	private static final ThreadLocal<Set<KeySynchronizer>> SYNC_LOCAL = new ThreadLocal<Set<KeySynchronizer>>();

	/**
	 * The factory of {@link KeySynchronizer} is intended to allow multiple threads to lock in parallel different keys without the need to be serialised by the same synchronized method.
	 * 
	 * @author porrjai
	 *
	 */
	private static final class SynchronizerFactory {

		private static final int FACTORY_SIZE = 16;
		private static final SynchronizerFactory[] FACTORIES = initFactories();

		private static SynchronizerFactory[] initFactories() {
			SynchronizerFactory[] factories = new SynchronizerFactory[FACTORY_SIZE];
			for (int i = 0; i < factories.length; i++) {
				factories[i] = new SynchronizerFactory();
			}
			return factories;
		}

		private synchronized KeySynchronizer synch(Object key) {
			KeySynchronizer synchronizer = SYNC_MAP.get(key);
			if (synchronizer == null) {
				synchronizer = new KeySynchronizer(key);
			}
			synchronizer.synch();
			return synchronizer;
		}

		private static KeySynchronizer create(Object key) {
			int hash = key.hashCode();
			int bucket = Math.abs(hash % FACTORY_SIZE);
			return FACTORIES[bucket].synch(key);
		}
	}

	private final Object key;
	private int requested = 0;
	private final AtomicBoolean inside = new AtomicBoolean();

	private KeySynchronizer(Object key) {
		this.key = key;
	}

	private synchronized void synch() {
		requested++;
		while (!inside.compareAndSet(false, true)) {
			try {
				wait();
			}
			catch (InterruptedException e) {
				// TODO: ¿ignore Exception?
				throw new IllegalStateException(e);
			}
		}
		SYNC_MAP.put(key, this);
		requested--;
	}

	private synchronized void doDesynch() {
		inside.set(false);
		notifyAll();
		if (requested == 0) {
			SYNC_MAP.remove(key);
		}
	}

	public static void synch(Object key) {
		Set<KeySynchronizer> synchronizers = SYNC_LOCAL.get();
		if (synchronizers == null) {
			synchronizers = new HashSet<>();
		}
		synchronizers.add(SynchronizerFactory.create(key));
		SYNC_LOCAL.set(synchronizers);
	}

	public static void desynch() {
		Set<KeySynchronizer> synchronizers = SYNC_LOCAL.get();
		if (synchronizers != null) {
			Iterator<KeySynchronizer> it = synchronizers.iterator();
			while(it.hasNext()) {
				KeySynchronizer keySynchronizer = it.next();
				it.remove();
				keySynchronizer.doDesynch();
			}
			SYNC_LOCAL.remove();
		}
		else {
			LOG.warn("No local thread synchronizer found. Please, be sure you call desynch() method after synch(key).");
		}
	}

	public static synchronized void clear() {
		SYNC_MAP.clear();
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public void clean() {
		clear();
	}
}