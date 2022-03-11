package eu.trade.tck;

import org.apache.chemistry.opencmis.tck.tests.basics.BasicsTestGroup;
import org.apache.chemistry.opencmis.tck.tests.control.ControlTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.CRUDTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.ChangeTokenTest;
import org.apache.chemistry.opencmis.tck.tests.crud.ContentRangesTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CopyTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteDocumentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteFolderTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteItemTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateBigDocument;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateDocumentWithoutContent;
import org.apache.chemistry.opencmis.tck.tests.crud.DeleteTreeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.MoveTest;
import org.apache.chemistry.opencmis.tck.tests.crud.NameCharsetTest;
import org.apache.chemistry.opencmis.tck.tests.crud.OperationContextTest;
import org.apache.chemistry.opencmis.tck.tests.crud.PropertyFilterTest;
import org.apache.chemistry.opencmis.tck.tests.crud.UpdateSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.filing.FilingTestGroup;
import org.apache.chemistry.opencmis.tck.tests.filing.MultifilingTest;
import org.apache.chemistry.opencmis.tck.tests.query.QueryTestGroup;
import org.apache.chemistry.opencmis.tck.tests.types.TypesTestGroup;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningTestGroup;
import org.junit.Test;

public class ITRunTck {
	
	public static final String COMMON_PROP_FILE_PATH = "src/test/resources/tck-parameters.common.properties";
	public static final String ADMIN_PROP_FILE_PATH = "src/test/resources/tck-parameters.admin.properties";
	public static final String TESTUSER_PROP_FILE_PATH = "src/test/resources/tck-parameters.testuser.properties";
	public static final String WS_PROP_FILE_PATH = "src/test/resources/tck-parameters.ws.properties";
	public static final String ATOM_PROP_FILE_PATH = "src/test/resources/tck-parameters.atom.properties";
	
	private static final String REPORT_BASE_DIR = "target/surefire-reports/";
	private static final String REPORT_PREFIX = "tck-";
	private static final String REPORT_COMPLETE = "complete-";
	private static final String REPORT_MANDATORY = "mandatory-";
	private static final String REPORT_ADMIN = "admin-";
	private static final String REPORT_TESTUSER = "testuser-";
	private static final String REPORT_WS = "ws-";
	private static final String REPORT_ATOM = "atom-";
	private static final String REPORT_SUFFIX = "report.html";

	protected enum ReportType { COMPLETE, MANDATORY }
	protected enum ReportUser { ADMIN, TESTUSER }
	protected enum ReportBinding { WS, ATOM }
	
	protected String generateReportPath(ReportType reportType, ReportBinding reportBinding, ReportUser reportUser) {
		String path = REPORT_BASE_DIR + REPORT_PREFIX;
		path += (reportType == ReportType.COMPLETE ? REPORT_COMPLETE : REPORT_MANDATORY);
		path += (reportBinding == ReportBinding.WS ? REPORT_WS : REPORT_ATOM);
		path += (reportUser == ReportUser.ADMIN ? REPORT_ADMIN : REPORT_TESTUSER);
		path += REPORT_SUFFIX;
		return path;
	}
	
	@Test
	public void testMandatoryWebServices() {
		doTest(ReportType.MANDATORY, ReportBinding.WS);
	}

	@Test
	public void testMandatoryAtom() {
		doTest(ReportType.MANDATORY, ReportBinding.ATOM);
	}

	@Test
	public void testAllGroupsWebServices() {
		doTest(ReportType.COMPLETE, ReportBinding.WS);
	}

	@Test
	public void testAllGroupsAtom() {
		doTest(ReportType.COMPLETE, ReportBinding.ATOM);
	}


	
	/**
	 * 
	 * @param reportType
	 * @param reportBinding
	 */
	private void doTest(ReportType reportType, ReportBinding reportBinding) {
			//String[] propFile, String reportFile, ReportUser reportUser) {
		String[] propFiles = new String[3];
		propFiles[0] = COMMON_PROP_FILE_PATH;
		propFiles[1] = reportBinding == ReportBinding.WS ? WS_PROP_FILE_PATH : ATOM_PROP_FILE_PATH;
		
		if(reportType == ReportType.MANDATORY) {
			propFiles[2] = ADMIN_PROP_FILE_PATH;
			mandatory(ReportUser.ADMIN, propFiles, generateReportPath(reportType, reportBinding, ReportUser.ADMIN));

			propFiles[2] = TESTUSER_PROP_FILE_PATH;
			mandatory(ReportUser.TESTUSER, propFiles, generateReportPath(reportType, reportBinding, ReportUser.TESTUSER));

		} else if (reportType == ReportType.COMPLETE) {

			propFiles[2] = ADMIN_PROP_FILE_PATH;
			complete(ReportUser.ADMIN, propFiles, generateReportPath(reportType, reportBinding, ReportUser.ADMIN));

			propFiles[2] = TESTUSER_PROP_FILE_PATH;
			complete(ReportUser.TESTUSER, propFiles, generateReportPath(reportType, reportBinding, ReportUser.TESTUSER));
		}
		

	}

	private void complete(ReportUser reportUser, String[] propFiles, String reportFile) {
		// This TCK tests WON'T mark the junit as a failure in case of non compliance.
		JUnitHelper jUnitHelper = new JUnitHelper(false, true, propFiles, reportFile);
		
		jUnitHelper.add(new BasicsTestGroup());
		if(reportUser == ReportUser.ADMIN) {
			jUnitHelper.add(new TypesTestGroup());
		}
		jUnitHelper.add(new TypesTestGroup());
		jUnitHelper.add(new CRUDTestGroup());
		jUnitHelper.add(new VersioningTestGroup());
		jUnitHelper.add(new FilingTestGroup());
		jUnitHelper.add(new ControlTestGroup());
		jUnitHelper.add(new QueryTestGroup());
		jUnitHelper.run();
	}

	private void mandatory(ReportUser reportUser, String[] propFiles, String reportFile) {
		// This TCK tests WILL mark the junit as a failure in case of non compliance.
		JUnitHelper jUnitHelper = new JUnitHelper(true, true, propFiles, reportFile);
		
		jUnitHelper.add(new BasicsTestGroup());
		if(reportUser == ReportUser.ADMIN) {
			jUnitHelper.add(new TypesTestGroup());
		}
		
		// jUnitHelper.add(new CRUDTestGroup());
		jUnitHelper.add(new CreateAndDeleteFolderTest());
		jUnitHelper.add(new CreateAndDeleteDocumentTest());
		jUnitHelper.add(new CreateBigDocument());
		jUnitHelper.add(new CreateDocumentWithoutContent());
		jUnitHelper.add(new NameCharsetTest());
		//jUnitHelper.add(new CreateAndDeleteRelationshipTest());
		jUnitHelper.add(new CreateAndDeleteItemTest()); //<-- not implemented yet (CMIS 1.1)
		jUnitHelper.add(new PropertyFilterTest());
		jUnitHelper.add(new UpdateSmokeTest());
		//jUnitHelper.add(new BulkUpdatePropertiesTest()); <-- not implemented yet (CMIS 1.1)
		//jUnitHelper.add(new SetAndDeleteContentTest()); <-- not implemented yet appendContentStream(...)
		jUnitHelper.add(new ChangeTokenTest());
		jUnitHelper.add(new ContentRangesTest());
		jUnitHelper.add(new CopyTest());
		jUnitHelper.add(new MoveTest());
		jUnitHelper.add(new DeleteTreeTest());
		jUnitHelper.add(new OperationContextTest());
		
		jUnitHelper.add(new VersioningTestGroup());
		
		//jUnitHelper.add(new FilingTestGroup());
		jUnitHelper.add(new MultifilingTest());
		//jUnitHelper.add(new UnfilingTest());
		
		jUnitHelper.add(new ControlTestGroup());
		jUnitHelper.add(new QueryTestGroup());
		
		jUnitHelper.run();
	}

}
