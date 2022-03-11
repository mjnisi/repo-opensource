package eu.trade.repo.index.impl.ops;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.impl.IndexTaskImpl;
import eu.trade.repo.index.model.IndexOperation;

public class ContentIndexOperatorTest extends BaseLobTestClass {

	@Autowired
	private IndexPartOperator contentIndexOperator;
	@Autowired
	private IndexSynchronizer indexSynchronizer;

	//create
	@Test
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void testCreateIndexPart_havingStreamAndObjectIndexStateNone() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		contentIndexOperator.createIndexPart(new IndexTaskImpl(100, 202, IndexOperation.CONTENT_INDEX, indexSynchronizer));

		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'Test Document WITH STREAM'",
				"index_create_2.xml");

		compareTable(
				"stream",
				"ID = '202'",
				"index_create_2.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100' order by word",
				"index_create_2.xml");

		compareTable(
				"index_word_object",
				"term.object_id = '202' and term.word_id = dic.ID ORDER BY dic.WORD",
				"index_create_2.xml", "index_word_object term, index_word dic");

	}

	@Test
	public void testCreateIndexPart_havingNoStreamAndObjectIndexStateNone() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_01.xml");
		contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 1, IndexOperation.CONTENT_INDEX, indexSynchronizer));

		//index_state=ERROR expected
		compareTable(
				"object",
				"ID = 1",
				"indexHelper_01.xml");
	}

	@Test
	public void testCreateIndexPart_havingStreamAndObjectIndexStateTempCleaned() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndexHelper_03_errorState.xml");

		contentIndexOperator.createIndexPart(new IndexTaskImpl(100, 203, IndexOperation.CONTENT_INDEX, indexSynchronizer));

		compareTable(
				"object",
				"ID = 203",
				"indexHelper_02.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100' order by word",
				"indexHelper_02.xml");

		compareTable(
				"index_word_object",
				"term.object_id = '203' and term.word_id = dic.ID ORDER BY dic.WORD",
				"indexHelper_02.xml", "index_word_object term, index_word dic");

		compareTable(
				"index_word_position",
				"term.id = pos.word_object_id ORDER BY pos.POSITION",
				"indexHelper_02.xml", 
				"index_word_object term, index_word_position pos");
	}


	@Test
	public void testDeleteIndexPart() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");
		contentIndexOperator.deleteIndexPart(new IndexTaskImpl(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, IndexOperation.CONTENT_INDEX_DELETE, indexSynchronizer));

		compareTable(
				"object",
				"ID=" + TestConstants.TESTDOC_CMISID_INT,
				"indexHelper_03.xml");
		compareTable(
				"index_word",
				null,
				"indexHelper_03.xml");
		compareTable(
				"index_word_object",
				null,
				"indexHelper_03.xml");
		compareTable(
				"index_word_position",
				null,
				"indexHelper_03.xml");
	}


	@Test
	public void testDeleteIndexPart_havingDocumentNotIndexed() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");
		contentIndexOperator.deleteIndexPart(new IndexTaskImpl(TestConstants.TEST_REPO_1_INT, 103, IndexOperation.CONTENT_INDEX_DELETE, indexSynchronizer));

		compareTable(
				"object",
				"ID = 103",
				"indexHelper_04.xml");

	}
}
