package eu.trade.repo.stats;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MethodStatsInfoTest {

	@Test
	public void testAddingInvocationTimes() throws InterruptedException {
		MethodStatsInfo info = new MethodStatsInfo("bestMethodEver");
		info.addElapsedTime(100, 1000);
		info.addElapsedTime(200, 2000);
		info.addElapsedTime(600, 3000);
		info.addElapsedTime(400, 4000);

		assertEquals(400, info.getAvgInvocationTimeInSpecifiedPeriod(100, 4100));
		assertEquals(500, info.getAvgInvocationTimeInSpecifiedPeriod(1200, 4100));
		assertEquals(400, info.getAvgInvocationTimeInSpecifiedPeriod(2200, 4100));
		assertEquals(325, info.getAvgInvocationTimeInSpecifiedPeriod(3200, 4100));
	}

	@Test
	public void testRemoveEntriesOlderThan() throws InterruptedException {
		MethodStatsInfo info = new MethodStatsInfo("bestMethodEver");
		info.addElapsedTime(100, 1000);
		info.addElapsedTime(200, 2000);
		info.addElapsedTime(600, 3000);
		info.addElapsedTime(400, 4000);
		info.addElapsedTime(400, 5000);
		info.addElapsedTime(400, 6000);

		assertEquals(6, info.getInvocationTimes().size());
		info.removeEntriesOlderThan(4500, 6000);
		assertEquals(5, info.getInvocationTimes().size());
		info.removeEntriesOlderThan(500, 6000);
		assertEquals(1, info.getInvocationTimes().size());
	}
}
