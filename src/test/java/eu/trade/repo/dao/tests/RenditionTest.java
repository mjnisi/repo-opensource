package eu.trade.repo.dao.tests;

import static eu.trade.repo.TestConstants.*;
import static junit.framework.Assert.*;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Rendition;

public class RenditionTest extends BaseTestClass {
	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		CMISObject o = cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTSUBFOLDER_CMISID);

		Rendition rendition = new Rendition();
		rendition.setKind("KIND");
		rendition.setMimeType("mime/tst");
		rendition.setObject(o);
		rendition.setRenditionDocumentId("docId");
		rendition.setStreamId("99");
		rendition.setTitle("new rendition");
		rendition.setHeight(10);
		rendition.setLength(20);
		rendition.setWidth(30);

		utilService.persist(rendition);

		compareTable(
				"rendition",
				"title = 'new rendition'",
				"rendition-test.xml");
	}
	@Test
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Rendition rendition = renditionSelector.getRenditionByStreamId("100");
		assertEquals("KIND", rendition.getKind());
		assertEquals("mime/tst", rendition.getMimeType());
		//assertEquals("Test Subfolder", rendition.getObject().getCmisObjectId());
		assertEquals("docId", rendition.getRenditionDocumentId());
		assertEquals("100", rendition.getStreamId());
		assertEquals("FOLDER RENDITION", rendition.getTitle());
		assertEquals(10, rendition.getHeight().intValue());
		assertEquals(20, rendition.getLength().intValue());
		assertEquals(30, rendition.getWidth().intValue());
		assertEquals(100, rendition.getId().intValue());
	}

	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Rendition rendition = renditionSelector.getRenditionByStreamId("100");

		rendition.setKind("KIND!");
		rendition.setMimeType("mime/tst!");
		rendition.setObject(cmisObjectSelector.getCMISObject(TEST_REPO_2, TESTDOC_CMISID));
		rendition.setRenditionDocumentId("docId!");
		rendition.setTitle("FOLDER RENDITION!");
		rendition.setHeight(100);
		rendition.setLength(200);
		rendition.setWidth(300);
		utilService.merge(rendition);

		rendition = renditionSelector.getRenditionByStreamId("100");
		assertEquals("KIND!", rendition.getKind());
		assertEquals("mime/tst!", rendition.getMimeType());
		//assertEquals("Test Document", rendition.getObject().getCmisObjectId());
		assertEquals("docId!", rendition.getRenditionDocumentId());
		assertEquals("100", rendition.getStreamId());
		assertEquals("FOLDER RENDITION!", rendition.getTitle());
		assertEquals(100, rendition.getHeight().intValue());
		assertEquals(200, rendition.getLength().intValue());
		assertEquals(300, rendition.getWidth().intValue());
		assertEquals(100, rendition.getId().intValue());
	}

	@Test
	public void testDelete () throws Exception {
		//THIS IS DONE THROUGH A SERVICE CALL AND NOT THROUGH THE ENTITY ... see repositoryService.deleteRepository
	}
}
