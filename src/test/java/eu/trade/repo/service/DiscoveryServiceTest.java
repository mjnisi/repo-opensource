package eu.trade.repo.service;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.query.QueryResult;

public class DiscoveryServiceTest extends BaseTestClass {

	private static final boolean NORMALIZED_QUERY_FALSE = false;
	private static final boolean NORMALIZED_QUERY_TRUE = true;
	
	@Before
	public void setup() throws Exception{
		setScenario("scenario05.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "nest_dev");
	}

	@Test
	public void testQueryAllDocuments() throws Exception {
		QueryResult qr = discoveryService.query("select * from cmis:document", "nest_dev", true, 999, 0, NORMALIZED_QUERY_FALSE);
		//331 x cmis:document + 2 x nest:document
		assertEquals(333, qr.getResult().size());
	}

	@Test
	public void testQueryAllNestDocuments() throws Exception {
		QueryResult qr = discoveryService.query("select * from nest:document", "nest_dev", true, 999, 0, NORMALIZED_QUERY_FALSE);
		assertEquals(2, qr.getResult().size());
	}

	@Test
	public void testQueryAllFolders() throws Exception {
		QueryResult qr = discoveryService.query("select * from cmis:folder", "nest_dev", true, 999, 0, NORMALIZED_QUERY_FALSE);
		assertEquals(319, qr.getResult().size());
	}
	
	//normalized search
	@Test
	public void testQueryAllDocumentsCaseInsensitive() throws Exception {
		QueryResult qr = discoveryService.query("select * from cmis:document", "nest_dev", true, 999, 0, NORMALIZED_QUERY_TRUE);
		//331 x cmis:document + 2 x nest:document
		assertEquals(333, qr.getResult().size());
	}

	@Test
	public void testQueryAllNestDocumentsCaseInsensitive() throws Exception {
		QueryResult qr = discoveryService.query("select * from nest:document", "nest_dev", true, 999, 0, NORMALIZED_QUERY_TRUE);
		assertEquals(2, qr.getResult().size());
	}

	@Test
	public void testQueryAllFoldersCaseInsensitive() throws Exception {
		QueryResult qr = discoveryService.query("select * from cmis:folder", "nest_dev", true, 999, 0, NORMALIZED_QUERY_TRUE);
		assertEquals(319, qr.getResult().size());
	}

	@Test
	public void testSorting() {
		List<Integer> idsOrder = new ArrayList<>();
		idsOrder.add(50);
		idsOrder.add(10);
		idsOrder.add(60);
		idsOrder.add(20);
		idsOrder.add(30);
		idsOrder.add(90);
		idsOrder.add(70);
		idsOrder.add(40);
		idsOrder.add(80);

		CMISObject.CmisIdComparator comparator = new CMISObject.CmisIdComparator(idsOrder);

		List<CMISObject> cmisObjects = new ArrayList<>();
		cmisObjects.add(createCmisObjectWithId(10));
		cmisObjects.add(createCmisObjectWithId(20));
		cmisObjects.add(createCmisObjectWithId(30));
		cmisObjects.add(createCmisObjectWithId(40));
		cmisObjects.add(createCmisObjectWithId(50));
		cmisObjects.add(createCmisObjectWithId(60));
		cmisObjects.add(createCmisObjectWithId(70));
		cmisObjects.add(createCmisObjectWithId(80));
		cmisObjects.add(createCmisObjectWithId(90));

		//before sorting
		assertEquals(10, cmisObjects.get(0).getId().intValue());
		assertEquals(90, cmisObjects.get(8).getId().intValue());

		Collections.sort(cmisObjects, comparator);

		assertEquals(50, cmisObjects.get(0).getId().intValue());
		assertEquals(10, cmisObjects.get(1).getId().intValue());
		assertEquals(60, cmisObjects.get(2).getId().intValue());
		assertEquals(20, cmisObjects.get(3).getId().intValue());
		assertEquals(30, cmisObjects.get(4).getId().intValue());
		assertEquals(90, cmisObjects.get(5).getId().intValue());
		assertEquals(70, cmisObjects.get(6).getId().intValue());
		assertEquals(40, cmisObjects.get(7).getId().intValue());
		assertEquals(80, cmisObjects.get(8).getId().intValue());
	}

	private CMISObject createCmisObjectWithId(int id) {
		CMISObject cmisObject = new CMISObject();
		cmisObject.setId(id);
		return cmisObject;
	}
}
