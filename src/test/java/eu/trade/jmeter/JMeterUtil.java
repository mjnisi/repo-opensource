package eu.trade.jmeter;

import org.apache.chemistry.opencmis.tck.CmisTest;

import eu.trade.tck.JUnitHelper;

public final class JMeterUtil {

	public static final String PROP_FILE_PATH = "tck-parameters.properties";

	private JMeterUtil() {
	}

	public static void doTest(CmisTest test, boolean reportFailures) {
		StringBuilder builder = new StringBuilder("jmeter-report.");
		builder.append(test.getClass().getName());
		builder.append(".");
		builder.append(System.currentTimeMillis());
		builder.append(".html");
		JUnitHelper jUnitHelper = new JUnitHelper(reportFailures, false, PROP_FILE_PATH, builder.toString());
		jUnitHelper.add(test);
		jUnitHelper.run();
	}
}
