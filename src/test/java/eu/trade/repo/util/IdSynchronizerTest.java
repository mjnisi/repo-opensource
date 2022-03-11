package eu.trade.repo.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdSynchronizerTest {

	private static final Logger LOG = LoggerFactory.getLogger(IdSynchronizerTest.class);

	private static final Map<String, Integer> ID_AMOUNTS = new HashMap<String, Integer>();
	private static final Map<String, Integer> EXPECTED_AMOUNTS = new HashMap<String, Integer>();
	private static final Random RND = new Random();

	private static final String[] NAMES = {
		"T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9",
		"T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9",
		"T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9",
		"T0", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9"};

	private static final String[] IDS = {
		"ID0", "ID0", "ID1", "ID1", "ID2", "ID2", "ID3", "ID3", "ID4", "ID4",
		"ID0", "ID0", "ID1", "ID1", "ID2", "ID2", "ID3", "ID3", "ID4", "ID4",
		"ID0", "ID0", "ID1", "ID1", "ID2", "ID2", "ID3", "ID3", "ID4", "ID4",
		"ID0", "ID0", "ID1", "ID1", "ID2", "ID2", "ID3", "ID3", "ID4", "ID4"};

	private static final int[] REPEATS = {
		10, 10, 20, 18, 50, 10, 16, 13, 10, 9,
		10, 10, 20, 18, 50, 10, 16, 13, 10, 9,
		10, 10, 20, 18, 50, 10, 16, 13, 10, 9,
		10, 10, 20, 18, 50, 10, 16, 13, 10, 9};

	private static final int[] AMOUNTS = {
		10, 50, 10, 10, 60, 10, 10, 10, 10, 10,
		10, 50, 10, 10, 60, 10, 10, 10, 10, 10,
		10, 50, 10, 10, 60, 10, 10, 10, 10, 10,
		10, 50, 10, 10, 60, 10, 10, 10, 10, 10};

	private static class TesterThread extends Thread {

		private final String name;
		private final String id;
		private final int repeat;
		private final Integer amount;

		public TesterThread(String name, String id, int repeat, int amount) {
			this.name = name;
			this.id = id;
			this.repeat = repeat;
			this.amount = amount;
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			for (int i = repeat; i > 0; i--) {
				delay();
				try {
					KeySynchronizer.synch(id);
					IdSynchronizerTest.addAmount(ID_AMOUNTS, id, amount, true);
					LOG.debug(name + "\t" + id + "\t" + ID_AMOUNTS.get(id));
				} catch (Throwable e) {
					LOG.error("THREAD SYNCH TEST ERROR", e);
				}
				finally {
					KeySynchronizer.desynch();
				}
			}
		}
	}

	private static void delay() {
		int i;
		do {
			i = RND.nextInt(100);
		} while (i != 0);
	}

	private static void addAmount(Map<String, Integer> map, String id, int amount, boolean thread) {
		Integer idAmount = map.get(id);
		if (thread) {
			delay();
		}
		if (idAmount == null) {
			idAmount = amount;
		} else {
			idAmount += amount;
		}
		map.put(id, idAmount);
	}

	@Before
	public void setUp() throws Exception {
		for (int i = 0; i < IDS.length; i++) {
			addAmount(EXPECTED_AMOUNTS, IDS[i], AMOUNTS[i] * REPEATS[i], false);
		}
	}

	@Test
	public void testSynchBatch() throws Exception {
		try {
			for (int i = 0; i < 100; i++) {
				KeySynchronizer.clear();
				ID_AMOUNTS.clear();
				doTestSynch();
			}
		}
		catch (Throwable e) {
			LOG.error("SYNCH TEST ERROR", e);
		}
	}

	private void doTestSynch() throws Exception {
		Set<Thread> threads = new HashSet<Thread>();
		for (int i = 0; i < NAMES.length; i++) {
			threads.add(new TesterThread(NAMES[i], IDS[i], REPEATS[i], AMOUNTS[i]));
		}
		for (Thread thread : threads) {
			thread.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			}
			catch (Exception e) {
			}
		}
		for (Map.Entry<String, Integer> amount : EXPECTED_AMOUNTS.entrySet()) {
			Assert.assertEquals(amount.getValue(), ID_AMOUNTS.get(amount.getKey()));
		}
	}
}
