package eu.trade.jmeter;

import org.junit.Test;

import eu.trade.repo.util.data.MBeanValueCollector;

public class JMeterStats {

	public static final String PROP_FILE_PATH = "jmeter.stats.properties";

	@Test
	public void testCollectStats() {
		new MBeanValueCollector(PROP_FILE_PATH).start();
	}
}
