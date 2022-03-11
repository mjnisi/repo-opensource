package eu.trade.tck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestProgressMonitor;
import org.apache.chemistry.opencmis.tck.CmisTestReport;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;
import org.apache.chemistry.opencmis.tck.impl.WrapperCmisTestGroup;
import org.apache.chemistry.opencmis.tck.report.HtmlReport;
import org.apache.chemistry.opencmis.tck.report.TextReport;
import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.util.Utilities;

/**
 * JUnit help class based on org.apache.chemistry.opencmis.tck.impl.JUnitHelper.
 * <p>
 * This implementation is intended to not fail on compliance test and to generate a HTML report.
 * 
 * @author porrjai
 *
 */
public class JUnitHelper {

	private static final Logger LOG = LoggerFactory.getLogger(JUnitHelper.class);

	private final boolean fail;
	private final boolean reportAll;
	private final String[] parametersPath;
	private final String reportPath;
	private final List<CmisTestGroup> groups;

	public JUnitHelper(boolean fail, boolean reportAll, String[] parametersPath, String reportPath) {
		this.fail = fail;
		this.reportAll = reportAll;
		this.parametersPath = parametersPath;
		this.reportPath = reportPath;
		this.groups = new ArrayList<CmisTestGroup>();
	}
	
	public JUnitHelper(boolean fail, boolean reportAll, String parametersPath, String reportPath) {
		this(fail, reportAll, new String[] { parametersPath }, reportPath);
	}

	public static void run(String[] parametersPath, CmisTest test) {
		try {
			JUnitRunner runner = new JUnitRunner();

			Map<String, String> parameters = new HashMap<String,String>();
			for(int i=0;i<parametersPath.length;i++) {
				parameters.putAll(IOUtils.readAllLinesAsMap(new FileInputStream(parametersPath[i])));
			}
			runner.setParameters(parameters);

			runner.addGroup(new WrapperCmisTestGroup(test));
			runner.run(new JUnitProgressMonitor());
			for (CmisTestResult result : test.getResults()) {
				if (result.getStatus().getLevel() >= CmisTestResultStatus.FAILURE.getLevel()) {
					Assert.fail(result.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error("Error running tests.", e);
			Assert.fail(e.getMessage());
		}
	}

	public void add(CmisTestGroup group) {
		groups.add(group);
	}

	public void add(CmisTest test) {
		try {
			groups.add(new WrapperCmisTestGroup(test));
		} catch (Exception e) {
			LOG.error("Error adding test: " + test, e);
			Assert.fail(e.getMessage());
		}
	}

	public void run() {
		try {
			JUnitRunner runner = new JUnitRunner();

			Map<String, String> parameters = new HashMap<String,String>();
			for(int i=0;i<parametersPath.length;i++) {
				parameters.putAll(IOUtils.readAllLinesAsMap(new FileInputStream(parametersPath[i])));
			}
			runner.setParameters(parameters);

			for (CmisTestGroup group : groups) {
				runner.addGroup(group);
			}
			runner.run(new JUnitProgressMonitor());
			if (reportAll) {
				doReports(runner);
			}

			if (fail) {
				checkForFailures(runner, reportAll);
			}
		} catch (Exception e) {
			LOG.error("Error running tests.", e);
			Assert.fail(e.getMessage());
		}
	}

	private void doReports(JUnitRunner runner) {
		Writer writer = null;
		try {
			// Text output
			CmisTestReport report = new TextReport();
			writer = new PrintWriter(System.out);
			report.createReport(runner.getParameters(), runner.getGroups(), writer);
			Utilities.close(writer);

			// Html output
			report = new HtmlReport();
			writer = getWriter();
			report.createReport(runner.getParameters(), runner.getGroups(), writer);
		} catch (Exception e) {
			LOG.error("Error doing reports.", e);
			Assert.fail(e.getMessage());
		} finally {
			Utilities.close(writer);
		}
	}

	private Writer getWriter() {
		Writer writer = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(reportPath);
			writer = new PrintWriter(fileOutputStream);
		} catch (FileNotFoundException e) {
			Utilities.close(fileOutputStream, writer);
		}
		return writer;
	}

	private void checkForFailures(JUnitRunner runner, boolean reportAll) {
		for (CmisTestGroup group : runner.getGroups()) {
			for (CmisTest test : group.getTests()) {
				for (CmisTestResult result : test.getResults()) {
					if (result.getStatus().getLevel() >= CmisTestResultStatus.FAILURE.getLevel()) {
						if (!reportAll) {
							doReports(runner);
						}
						Assert.fail(result.getMessage());
					}
				}
			}
		}
	}

	private static class JUnitRunner extends AbstractRunner {
	}

	private static class JUnitProgressMonitor implements CmisTestProgressMonitor {
		@Override
		public void startGroup(CmisTestGroup group) {
			System.out.println(group.getName() + " (" + group.getTests().size() + " tests)");
		}

		@Override
		public void endGroup(CmisTestGroup group) {
		}

		@Override
		public void startTest(CmisTest test) {
			System.out.println("  " + test.getName());
		}

		@Override
		public void endTest(CmisTest test) {
		}

		@Override
		public void message(String msg) {
			System.out.println(msg);
		}
	}
}
