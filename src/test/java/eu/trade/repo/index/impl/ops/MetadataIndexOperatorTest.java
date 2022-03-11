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

public class MetadataIndexOperatorTest extends BaseLobTestClass {

	@Autowired
	private IndexPartOperator metadataIndexOperator;
	@Autowired
	private IndexSynchronizer indexSynchronizer;

	//create
	@Test
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void testCreateIndexPart_havingStreamAndMetadataIndexStateNone() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndex_03.xml");

		metadataIndexOperator.createIndexPart(new IndexTaskImpl(100, 203, IndexOperation.METADATA_INDEX, indexSynchronizer));

		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'Test Document WITH STREAM 203'",
				"index_create_3.xml");

		compareTable(
				"stream",
				"ID = '203'",
				"index_create_3.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100' order by word",
				"index_create_3.xml");

		compareTable(
				"index_word_object",
				"term.object_id = '203' and term.word_id = dic.ID ORDER BY dic.WORD, term.property_id",
				"index_create_3.xml", "index_word_object term, index_word dic");

	}

	@Test
	public void testCreateIndexPart_havingNoStreamAndMetadataIndexStateNone() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndex_03.xml");
		metadataIndexOperator.createIndexPart(new IndexTaskImpl(100, 204, IndexOperation.METADATA_INDEX, indexSynchronizer));

		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'Test Document WITHOUT STREAM 204'",
				"index_create_4.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100' order by word",
				"index_create_4.xml");

		compareTable(
				"index_word_object",
				"term.object_id = '204' and term.word_id = dic.ID ORDER BY dic.WORD, term.property_id",
				"index_create_4.xml", "index_word_object term, index_word dic");
	}

	@Test
	public void testCreateIndexPart_havingNoStreamAndMetadataIndexStateError() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndex_03.xml");

		metadataIndexOperator.createIndexPart(new IndexTaskImpl(100, 205, IndexOperation.METADATA_INDEX, indexSynchronizer));

		compareTable(
				"object",
				"CMIS_OBJECT_ID = 'Test Document in error'",
				"index_create_5.xml");

		compareTable(
				"index_word",
				"REPOSITORY_ID = '100' order by word",
				"index_create_5.xml");

		compareTable(
				"index_word_object",
				"term.object_id = '205' and term.word_id = dic.ID ORDER BY dic.WORD, term.property_id",
				"index_create_5.xml", "index_word_object term, index_word dic");
	}
	
	@Test
	public void testCreateIndexPart_havingNoProperties() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		metadataIndexOperator.createIndexPart(new IndexTaskImpl(101, 101, IndexOperation.METADATA_INDEX, indexSynchronizer));
		compareTable(
				"object",
				"ID = 101",
				"index_create_6.xml");
	}


	@Test
	public void testDeleteIndexPart() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndexPermanent_02.xml");
		metadataIndexOperator.deleteIndexPart(new IndexTaskImpl(100, 205, IndexOperation.METADATA_INDEX_DELETE, indexSynchronizer));

		compareTable(
				"object",
				"ID = 205",
				"indexHelper_05.xml");
		compareTable(
				"index_word",
				null,
				"indexHelper_05.xml");
		compareTable(
				"index_word_object",
				null,
				"indexHelper_05.xml");
		compareTable(
				"index_word_position",
				null,
				"indexHelper_05.xml");
	}


	@Test
	public void testDeleteIndexPart_havingDocumentNotIndexed() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");
		metadataIndexOperator.deleteIndexPart(new IndexTaskImpl(TestConstants.TEST_REPO_1_INT, 103, IndexOperation.METADATA_INDEX_DELETE, indexSynchronizer));

		compareTable(
				"object",
				"ID = 103",
				"indexHelper_04.xml");

	}
}

