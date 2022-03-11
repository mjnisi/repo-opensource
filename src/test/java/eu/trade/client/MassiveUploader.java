package eu.trade.client;

import eu.trade.tck.JUnitHelper;

public class MassiveUploader {

	public static final String DEFAULT_PROP_FILE_PATH = "src/test/resources/client/uploader-parameters.properties";

	private static void doTest(String propFile) {
		StringBuilder builder = new StringBuilder("massive.uploader.report.");
		builder.append(System.currentTimeMillis());
		builder.append(".html");
		JUnitHelper jUnitHelper = new JUnitHelper(true, false, propFile, builder.toString());
		jUnitHelper.add(new MassiveUploaderUtil());
		jUnitHelper.run();
	}

	public static void main(String[] args) {
		String propFile = DEFAULT_PROP_FILE_PATH;
		if (args != null && args.length > 0) {
			propFile = args[0];
		}
		doTest(propFile);
	}
}
