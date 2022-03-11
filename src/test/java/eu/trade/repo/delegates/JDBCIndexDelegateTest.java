package eu.trade.repo.delegates;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.index.IndexRuntimeException;
import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.IndexObjectDTO;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;
import eu.trade.repo.index.model.WordObjectDTO;

public class JDBCIndexDelegateTest extends BaseLobTestClass {

	@Autowired
	IndexDelegate indexDelegate;


	@Autowired
	IndexDelegateHelper indexDelegateHelper;

	//@Autowired @Qualifier("indexDelegateHelperFake")
	//IndexDelegateHelper indexDelegateHelperThrowException;

	//supposed document: "one four three two one"


	//*********************** write WORD page
	@Test 
	public void testWriteWordPageInEmptyDictionary() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "baseIndex1.xml");
		testWriteWordPage();
	}

	@Test 
	public void testWriteWordPageInNonEmptyDictionary() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexWords_01.xml");
		testWriteWordPage();
	}

	private void testWriteWordPage() throws Exception{
		List<String> wordList = new ArrayList<String>();
		wordList.add("one");
		wordList.add("two");
		wordList.add("three");
		wordList.add("four");
		Map<String, WordDTO> wordMap = indexDelegate.writeWordPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, wordList);

		assertEquals(4, wordMap.keySet().size());
		compareLobTable("index_word", null, "index_delegate_1_wo.xml");
	}

	@Test
	public void testWriteWordPageIfWordListEmpty() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexWords_01.xml");

		Map<String, WordDTO> wordMap = indexDelegate.writeWordPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, new ArrayList<String>());
		assertEquals(0, wordMap.keySet().size());
	}


	/*@Test
	public void testWriteWordObjectPage_counters() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "index_jpaDelegate_01.xml");

		List<TransientDTO> wordList = new ArrayList<TransientDTO>();
		wordList.add(new TransientDTO("my", 1, 1, 1, 102, 100, 101));
		wordList.add(new TransientDTO("document", 1, 1, 1, 102, 100, 101));

		Map<String, WordDTO> map = new HashMap<>();
		map.put("my", new WordDTO(1, 101, "my"));
		map.put("document", new WordDTO(2, 101, "document"));

		indexDelegateHelper.writeWordObjectPage(101, 102, wordList, map, IndexOperationType.METADATA);

		compareTable(
				"index_word",
				"1 = 1 ORDER BY id",
				"index_counter_6_1.xml");

		compareTable(
				"index_word_object",
				"1 = 1 ORDER BY word_id",
				"index_counter_6_1.xml");


		compareTable(
				"index_counter_word_object",
				"counter.word_id = dic.ID ORDER BY dic.ID",
				"index_counter_6_1.xml", 
				"index_counter_word_object counter, index_word dic");
	}*/


	/*@Test
	public void testWriteWordObjectPage_countersThrowExceptionRollback() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "index_jpaDelegate_01.xml");

		List<TransientDTO> wordList = new ArrayList<TransientDTO>();
		wordList.add(new TransientDTO("my", 1, 1, 1, 102, 100, 101));
		wordList.add(new TransientDTO("document", 1, 1, 1, 102, 100, 101));

		Map<String, WordDTO> map = new HashMap<>();
		map.put("my", new WordDTO(1, 101, "my"));
		map.put("document", new WordDTO(2, 101, "document"));

		try{
			indexDelegateHelperThrowException.writeWordObjectPage(101, 102, wordList, map, IndexOperationType.METADATA);
		}catch(IndexRuntimeException e){

		}
		compareTable(
				"index_word",
				"1 = 1 ORDER BY id",
				"index_counter_6_2.xml");

		compareTable(
				"index_word_object",
				"1 = 1 ORDER BY word_id",
				"index_counter_6_2.xml");


		compareTable(
				"index_counter_word_object",
				"counter.word_id = dic.ID ORDER BY dic.ID",
				"index_counter_6_2.xml", 
				"index_counter_word_object counter, index_word dic");


	}*/


	//*********************** write WORD_OBJECT page

	@Test 
	public void testWriteWordObjectPage() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "rootObjects_01.xml");
		setLobScenario(DatabaseOperation.INSERT,"scenarioIndexWordObjects_01.xml");

		Map<String, WordDTO> wordMap = getWordMap();
		Map<String, ? extends IWordObjectExtractor> wordObjectMap = indexDelegate.writeWordObjectPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, getTransientListForWordObjects(), wordMap, IndexOperationType.CONTENT);

		assertEquals(4, wordObjectMap.keySet().size());

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY dic.ID",
				"index_delegate_1_wo.xml", 
				"index_word_object term, index_word dic");
	}

	@Test 
	public void testWriteWordObjectPageIfWordListEmpty() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexWordObjects_01.xml");

		Map<String, WordDTO> wordMap = getWordMap();
		Map<String, ? extends IWordObjectExtractor> wordObjectMap = indexDelegate.writeWordObjectPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, new ArrayList<TransientDTO>(), wordMap, IndexOperationType.CONTENT);

		assertEquals(0, wordObjectMap.keySet().size());

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY dic.ID",
				"index_delegate_2_empty.xml", 
				"index_word_object term, index_word dic");
	}

	//*********************** write WORD_POSITION page

	@Test 
	public void testWriteWordPositionPage() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexWordPositions_01.xml");

		Map<String, WordObjectDTO> wordObjectMap = getWordObjectMap();

		indexDelegate.writeWordPositionPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, getTransientListForWordPositions(), wordObjectMap);

		compareTable(
				"index_word_position",
				"term.id = pos.word_object_id ORDER BY pos.position",
				"index_delegate_3_pos.xml", 
				"index_word_object term, index_word_position pos");
	}

	@Test 
	public void testWriteWordPositionPageIfWordListEmpty() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexWordPositions_01.xml");

		Map<String, WordObjectDTO> wordObjectMap = getWordObjectMap();
		indexDelegate.writeWordPositionPage(TestConstants.TEST_REPO_1_INT, TestConstants.TESTDOC_CMISID_INT, new ArrayList<TransientDTO>(), wordObjectMap);

		compareTable(
				"index_word_position",
				"term.id = pos.word_object_id ORDER BY pos.position",
				"index_delegate_2_empty.xml", 
				"index_word_object term, index_word_position pos");
	}


	//*********************** DELETE PERMANENT INDEX (index_word_object, index_word_position)

	@Test
	public void deletePermanentIndexByDocumentIdTestMultiplePages() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");

		int deletePageSize = 2;
		boolean done = false;
		List<Integer> wordObjectIdList = null;
		int deletedRecords = 0;
		while( !done ){
			wordObjectIdList =  indexDelegate.obtainWordObjectPageToDelete(TestConstants.TESTDOC_CMISID_INT, 2, IndexOperationType.CONTENT);
			deletedRecords += indexDelegate.deletePermanentIndexPartPage(wordObjectIdList);
			done = ( wordObjectIdList.size() < deletePageSize -1);
		}

		assertEquals(9, deletedRecords);

		compareLobTable("index_word", "repository_id=" + TestConstants.TEST_REPO_1_INT, "index_delegate_4_dic.xml");

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY dic.ID",
				"index_delegate_4_dic.xml", 
				"index_word_object term, index_word dic");

		compareTable(
				"index_word_position",
				"term.id = pos.word_object_id ORDER BY pos.position",
				"index_delegate_4_dic.xml", 
				"index_word_object term, index_word_position pos");
	}

	@Test
	public void deletePermanentIndexByDocumentIdTestOnePage() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");

		List<Integer> wordObjectIdList =  indexDelegate.obtainWordObjectPageToDelete(TestConstants.TESTDOC_CMISID_INT, 4, IndexOperationType.CONTENT);
		int deletedRecords = indexDelegate.deletePermanentIndexPartPage(wordObjectIdList);
		assertEquals(9, deletedRecords);

		compareLobTable("index_word", null, "index_delegate_4_dic.xml");

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY dic.ID",
				"index_delegate_4_dic.xml", 
				"index_word_object term, index_word dic");

		compareTable(
				"index_word_position",
				"term.id = pos.word_object_id ORDER BY pos.position",
				"index_delegate_4_dic.xml", 
				"index_word_object term, index_word_position pos");
	}

	//***********************  obtainOrphanIndexes

	@Test
	public void obtainOrphanIndexesHasOrphans() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexObject_01_orphans.xml");
		List<Integer> orphanList = indexDelegate.obtainOrphanIndexes();
		assertEquals(2, orphanList.size());
	}

	@Test
	public void obtainOrphanIndexesNoOrphans() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexPermanent_01.xml");
		List<Integer> orphanList = indexDelegate.obtainOrphanIndexes();
		assertEquals(0, orphanList.size());
	}

	//***********************  obtainContentIndexesInErrorOrUnfinishedState

	@Test
	public void obtainContentIndexesInErrorOrUnfinishedState_maxAttemptsNotReached() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "baseIndex1.xml");
		setLobScenario(DatabaseOperation.INSERT, "rootObjects_01.xml");
		setLobScenario(DatabaseOperation.INSERT, "scenarioIndexObject_02_reindex.xml");

		int maxAttempts = 10;
		List<IndexObjectDTO> objectsToReIndex = indexDelegate.obtainContentIndexesInErrorOrUnfinishedState(maxAttempts, 500, -1, -1);
		assertEquals(1, objectsToReIndex.size());
	}

	@Test
	public void obtainContentIndexesInErrorOrUnfinishedState_someMaxAttemptsReached() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "baseIndex1.xml");
		setLobScenario(DatabaseOperation.INSERT, "rootObjects_01.xml");
		setLobScenario(DatabaseOperation.INSERT, "scenarioIndexObject_03_reindex.xml");
		int maxAttempts = 1;
		List<IndexObjectDTO> objectsToReIndex = indexDelegate.obtainContentIndexesInErrorOrUnfinishedState(maxAttempts, 500, -1, -1);
		assertEquals(1, objectsToReIndex.size());
	}

	//***********************  obtainMetadataIndexesInErrorOrUnfinishedState
	@Test
	public void obtainMetadatatIndexesInErrorOrUnfinishedState_maxAttemptsNotReached() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndex_03.xml");

		int maxAttempts = 10;
		List<IndexObjectDTO> objectsToReIndex = indexDelegate.obtainMetadataIndexesInErrorOrUnfinishedState(maxAttempts, 500);
		assertEquals(5, objectsToReIndex.size());
	}

	@Test
	public void obtainMetadataIndexesInErrorOrUnfinishedState_someMaxAttemptsReached() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml", "scenarioIndex_03.xml");

		int maxAttempts = 1;
		List<IndexObjectDTO> objectsToReIndex = indexDelegate.obtainMetadataIndexesInErrorOrUnfinishedState(maxAttempts, 500);
		assertEquals(4, objectsToReIndex.size());
	}


	// PRIVATES 
	private Map<String, WordDTO> getWordMap(){
		Map<String, WordDTO> wordMap = new HashMap<String, WordDTO>();
		wordMap.put("one", new WordDTO(1, TestConstants.TEST_REPO_1_INT, "one"));
		wordMap.put("two", new WordDTO(2, TestConstants.TEST_REPO_1_INT, "two"));
		wordMap.put("three", new WordDTO(3, TestConstants.TEST_REPO_1_INT, "three"));
		wordMap.put("four", new WordDTO(4, TestConstants.TEST_REPO_1_INT, "four"));
		return wordMap;
	}

	private Map<String, WordObjectDTO> getWordObjectMap(){
		Map<String, WordObjectDTO> wordObjMap = new HashMap<String, WordObjectDTO>();
		wordObjMap.put("one", new WordObjectDTO(1, 1, TestConstants.TESTDOC_CMISID_INT, 2));
		wordObjMap.put("two", new WordObjectDTO(2, 2, TestConstants.TESTDOC_CMISID_INT, 1));
		wordObjMap.put("three", new WordObjectDTO(3, 3, TestConstants.TESTDOC_CMISID_INT, 1));
		wordObjMap.put("four", new WordObjectDTO(4, 4, TestConstants.TESTDOC_CMISID_INT, 1));
		return wordObjMap;
	}

	private List<TransientDTO> getTransientListForWordObjects(){
		List<TransientDTO> transientList = new ArrayList<TransientDTO>();
		transientList.add(new TransientDTO("one", 0, 2, 0, TestConstants.TESTDOC_CMISID_INT));
		transientList.add(new TransientDTO("four", 0, 1, 0, TestConstants.TESTDOC_CMISID_INT));
		transientList.add(new TransientDTO("three", 0, 1, 0, TestConstants.TESTDOC_CMISID_INT));
		transientList.add(new TransientDTO("two", 0, 1, 0, TestConstants.TESTDOC_CMISID_INT));
		return transientList;
	}

	private List<TransientDTO> getTransientListForWordPositions(){
		List<TransientDTO> transientList = new ArrayList<TransientDTO>();
		transientList.add(new TransientDTO("one", 1, 0, 1, 0));
		transientList.add(new TransientDTO("two", 2, 0, 1, 0));
		transientList.add(new TransientDTO("three", 3, 0, 1, 0));
		transientList.add(new TransientDTO("four", 4, 0, 1, 0));
		transientList.add(new TransientDTO("one", 5, 0, 1, 0));
		return transientList;
	}


	//List<Integer> obtainOrphanIndexes();

	//List<IndexObjectDTO> obtainIndexesInErrorOrUnfinishedState();

}
