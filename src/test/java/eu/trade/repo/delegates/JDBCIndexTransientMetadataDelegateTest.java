//package eu.trade.repo.delegates;
//
//import static org.junit.Assert.*;
//
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.dbunit.operation.DatabaseOperation;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import eu.trade.repo.BaseLobTestClass;
//import eu.trade.repo.TestConstants;
//import eu.trade.repo.index.model.TransientDTO;
//
//
//public class JDBCIndexTransientMetadataDelegateTest extends BaseLobTestClass {
//
//	@Autowired
//	IndexTransientContentDelegate indexTransientContentDelegate;
//
//	private static final String TEST_FILE_PATH = "pdf/indexTestFile2.txt";
//	private static final String EMPTY_TEST_FILE_PATH = "pdf/emptyFile.txt";
//
//	private final int DOCUMENT_ID = 105;
//	private final int WORD_LIMIT = 5;
//
//	//*********************** processContentToTempIndex
//
//	@Test
//	public void processContentToTempIndexTest() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		Reader reader = null;
//		try{
//			reader = new InputStreamReader(getPdfFileInputStream(TEST_FILE_PATH));
//			indexTransientContentDelegate.processContentToTransientIndex(TestConstants.TEST_REPO_1_INT, DOCUMENT_ID, reader, 3, WORD_LIMIT);
//		}finally{
//			reader.close();
//		}
//		compareLobTransientTable("index_transient", "object_id=" + DOCUMENT_ID,	"index_transient_02.xml");
//
//	}
//
//	@Test
//	public void processContentToTempIndexTest_emptyFile() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		Reader reader = null;
//		try{
//			reader = new InputStreamReader(getPdfFileInputStream(EMPTY_TEST_FILE_PATH));
//			indexTransientContentDelegate.processContentToTransientIndex(TestConstants.TEST_REPO_1_INT, DOCUMENT_ID, reader, 3, WORD_LIMIT);
//		}finally{
//			reader.close();
//		}
//
//		compareLobTransientTable("index_transient", "object_id=" + DOCUMENT_ID,	"index_transient_01.xml");
//
//	}
//
//
//
//	//*********************** obtainWordPageByDocumentId
//
//	@Test
//	public void obtainWordPageByDocumentIdTest() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		// document: one two three four one six
//		// words ordered: four one six three two
//		List<String> wordList = indexTransientContentDelegate.obtainWordPageByObjectId(TestConstants.TESTDOC_CMISID_INT, 1, 20);
//		assertEquals(5, wordList.size());
//
//		wordList = indexTransientContentDelegate.obtainWordPageByObjectId(TestConstants.TESTDOC_CMISID_INT, 1, 3);
//		assertEquals(3, wordList.size());
//		assertEquals("four", wordList.get(0));
//		assertEquals("one", wordList.get(1));
//		assertEquals("six", wordList.get(2));
//
//		wordList = indexTransientContentDelegate.obtainWordPageByObjectId(TestConstants.TESTDOC_CMISID_INT, 4, 3);
//		assertEquals(2, wordList.size());
//		assertEquals("three", wordList.get(0));
//		assertEquals("two", wordList.get(1));
//
//		wordList = indexTransientContentDelegate.obtainWordPageByObjectId(TestConstants.TESTDOC_CMISID_INT, 6, 4);
//		assertEquals(0, wordList.size());
//	}
//
//	//*********************** obtainWordPageByDocumentId
//
//	@Test
//	public void obtainWordObjectPageByDocumentIdTest() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		List<String> wordList = new ArrayList<String>();
//		wordList.add("four");
//		wordList.add("one");
//		wordList.add("six");
//		wordList.add("three");
//		wordList.add("two");
//		List<TransientDTO> list = indexTransientContentDelegate.obtainWordObjectPageByObjectId(TestConstants.TESTDOC_CMISID_INT, wordList);
//		assertEquals(5, list.size());
//
//		wordList = new ArrayList<String>();
//		wordList.add("one");
//		list = indexTransientContentDelegate.obtainWordObjectPageByObjectId(TestConstants.TESTDOC_CMISID_INT, wordList);
//		assertEquals(1, list.size());
//	}
//
//
//	//*********************** obtainWordPositionPageByDocumentId
//
//	@Test
//	public void obtainWordPositionPageByDocumentIdTest() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		List<String> wordList = new ArrayList<String>();
//		wordList.add("four");
//		wordList.add("one");
//		wordList.add("six");
//		wordList.add("three");
//		wordList.add("two");
//		List<TransientDTO> list = indexTransientContentDelegate.obtainWordPositionPageByObjectId(TestConstants.TESTDOC_CMISID_INT, wordList, 1, 3);
//		assertEquals(3, list.size());
//		list = indexTransientContentDelegate.obtainWordPositionPageByObjectId(TestConstants.TESTDOC_CMISID_INT, wordList, 1, 20);
//		assertEquals(6, list.size());
//
//	}
//
//
//	//*********************** deleteTransientByDocumentId
//
//	@Test
//	public void deleteTransientByDocumentIdTest() throws Exception {
//		setLobTransientScenario(DatabaseOperation.CLEAN_INSERT, "scenarioTransient_01.xml");
//
//		int deletedRecords = indexTransientContentDelegate.deleteTransientContentIndexByObjectId(TestConstants.TESTDOC_CMISID_INT, 4);
//		assertEquals(6, deletedRecords);
//
//		compareLobTransientTable("index_transient", "object_id=" + TestConstants.TESTDOC_CMISID_INT,	"index_transient_01.xml");
//	}
//
//	//private
//	public List<String> getWordList(){
//		List<String> result = new ArrayList<String>();
//		result.add("four");
//		result.add("one");
//		result.add("six");
//		result.add("three");
//		result.add("two");
//		return result;
//	}
//
//
//}
