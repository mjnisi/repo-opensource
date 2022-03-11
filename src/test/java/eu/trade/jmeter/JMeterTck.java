package eu.trade.jmeter;

import org.apache.chemistry.opencmis.tck.tests.basics.RepositoryInfoTest;
import org.apache.chemistry.opencmis.tck.tests.basics.RootFolderTest;
import org.apache.chemistry.opencmis.tck.tests.basics.SecurityTest;
import org.apache.chemistry.opencmis.tck.tests.control.ACLSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.BulkUpdatePropertiesTest;
import org.apache.chemistry.opencmis.tck.tests.crud.ChangeTokenTest;
import org.apache.chemistry.opencmis.tck.tests.crud.ContentRangesTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CopyTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteDocumentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteFolderTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteItemTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteRelationshipTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateBigDocument;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateDocumentWithoutContent;
import org.apache.chemistry.opencmis.tck.tests.crud.DeleteTreeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.MoveTest;
import org.apache.chemistry.opencmis.tck.tests.crud.NameCharsetTest;
import org.apache.chemistry.opencmis.tck.tests.crud.OperationContextTest;
import org.apache.chemistry.opencmis.tck.tests.crud.SetAndDeleteContentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.UpdateSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.filing.MultifilingTest;
import org.apache.chemistry.opencmis.tck.tests.filing.UnfilingTest;
import org.apache.chemistry.opencmis.tck.tests.query.ContentChangesSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.query.QueryForObject;
import org.apache.chemistry.opencmis.tck.tests.query.QueryLikeTest;
import org.apache.chemistry.opencmis.tck.tests.query.QueryRootFolderTest;
import org.apache.chemistry.opencmis.tck.tests.query.QuerySmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.CheckedOutTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersionDeleteTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.versioning.VersioningStateCreateTest;
import org.junit.Test;

public class JMeterTck {

	@Test
	public void testSecurityTest() {
		JMeterUtil.doTest(new SecurityTest(), true);
	}

	@Test
	public void testRepositoryInfoTest() {
		JMeterUtil.doTest(new RepositoryInfoTest(), true);
	}

	@Test
	public void testRootFolderTest() {
		JMeterUtil.doTest(new RootFolderTest(), false);
	}

	@Test
	public void testACLSmokeTest() {
		JMeterUtil.doTest(new ACLSmokeTest(), true);
	}

	@Test
	public void testCreateAndDeleteFolderTest() {
		JMeterUtil.doTest(new CreateAndDeleteFolderTest(), true);
	}

	@Test
	public void testCreateAndDeleteDocumentTest() {
		JMeterUtil.doTest(new CreateAndDeleteDocumentTest(), true);
	}

	@Test
	public void testCreateBigDocument() {
		JMeterUtil.doTest(new CreateBigDocument(), true);
	}

	@Test
	public void testCreateDocumentWithoutContent() {
		JMeterUtil.doTest(new CreateDocumentWithoutContent(), true);
	}

	@Test
	public void testNameCharsetTest() {
		JMeterUtil.doTest(new NameCharsetTest(), true);
	}

	@Test
	public void testCreateAndDeleteRelationshipTest() {
		JMeterUtil.doTest(new CreateAndDeleteRelationshipTest(), false);
	}

	@Test
	public void testCreateAndDeleteItemTest() {
		JMeterUtil.doTest(new CreateAndDeleteItemTest(), true);
	}

	@Test
	public void testUpdateSmokeTest() {
		JMeterUtil.doTest(new UpdateSmokeTest(), true);
	}

	@Test
	public void testBulkUpdatePropertiesTest() {
		JMeterUtil.doTest(new BulkUpdatePropertiesTest(), true);
	}

	@Test
	public void testSetAndDeleteContentTest() {
		JMeterUtil.doTest(new SetAndDeleteContentTest(), true);
	}

	@Test
	public void testChangeTokenTest() {
		JMeterUtil.doTest(new ChangeTokenTest(), true);
	}

	@Test
	public void testContentRangesTest() {
		JMeterUtil.doTest(new ContentRangesTest(), true);
	}

	@Test
	public void testCopyTest() {
		JMeterUtil.doTest(new CopyTest(), false);
	}

	@Test
	public void testMoveTest() {
		JMeterUtil.doTest(new MoveTest(), true);
	}

	@Test
	public void testDeleteTreeTest() {
		JMeterUtil.doTest(new DeleteTreeTest(), true);
	}

	@Test
	public void testOperationContextTest() {
		JMeterUtil.doTest(new OperationContextTest(), true);
	}

	@Test
	public void testMultifilingTest() {
		JMeterUtil.doTest(new MultifilingTest(), true);
	}

	@Test
	public void testUnfilingTest() {
		JMeterUtil.doTest(new UnfilingTest(), false);
	}

	@Test
	public void testQuerySmokeTest() {
		JMeterUtil.doTest(new QuerySmokeTest(), false);
	}

	@Test
	public void testQueryRootFolderTest() {
		JMeterUtil.doTest(new QueryRootFolderTest(), true);
	}

	@Test
	public void testQueryForObject() {
		JMeterUtil.doTest(new QueryForObject(), true);
	}

	@Test
	public void testQueryLikeTest() {
		JMeterUtil.doTest(new QueryLikeTest(), false);
	}

	@Test
	public void testContentChangesSmokeTest() {
		JMeterUtil.doTest(new ContentChangesSmokeTest(), true);
	}

	@Test
	public void testVersioningSmokeTest() {
		JMeterUtil.doTest(new VersioningSmokeTest(), true);
	}

	@Test
	public void testVersionDeleteTest() {
		JMeterUtil.doTest(new VersionDeleteTest(), true);
	}

	@Test
	public void testVersioningStateCreateTest() {
		JMeterUtil.doTest(new VersioningStateCreateTest(), true);
	}

	@Test
	public void testCheckedOutTest() {
		JMeterUtil.doTest(new CheckedOutTest(), false);
	}
}