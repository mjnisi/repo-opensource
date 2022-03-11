package eu.trade.repo.index.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.IndexTaskServiceFactory;
import eu.trade.repo.index.model.IndexOperation;

public class IndexSynchronizerImplTest extends BaseLobTestClass {

	@Autowired
	private IndexSynchronizer indexSynchronizer;

	@Autowired
	private IndexTaskServiceFactory taskFactory;


	//	putInQueue

	@Test
	public void testPutInQueue() throws Exception{

		IndexTask task1 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean result = indexSynchronizer.putInQueue(task1, false);
		assertEquals(true, result);

		result = indexSynchronizer.putInQueue(task2, false);
		assertEquals(false, result);
		IndexTask taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNull(taskWaiting);

		result = indexSynchronizer.putInQueue(task2, true);
		assertEquals(false, result);
		taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNotNull(taskWaiting);

		//clean indexSynchronizer
		indexSynchronizer.doOnTaskFinished(task1);

	}


	//	doOnTaskFinished
	@Test
	public void testDoOnTaskFinished() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		IndexTask task1 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean result = indexSynchronizer.putInQueue(task1, false);
		assertEquals(true, result);

		indexSynchronizer.doOnTaskFinished(task1);

		IndexTask taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNull(taskWaiting);
	}

	@Test
	public void testDoOnTaskFinished_twoTasks() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		IndexTask task1 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean result = indexSynchronizer.putInQueue(task1, false);
		assertEquals(true, result);

		result = indexSynchronizer.putInQueue(task2, true);
		assertEquals(false, result);

		//this trigger the task2 to execute
		indexSynchronizer.doOnTaskFinished(task1);

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
				"term.object_id = '202' and term.word_id = dic.ID and term.property_id = -1 ORDER BY dic.WORD",
				"index_create_2.xml", "index_word_object term, index_word dic");

		//clean indexSynchronizer
		indexSynchronizer.doOnTaskFinished(task2);
	}



	//	isOtherTaskWaiting
	@Test
	public void testIsOtherTaskWaiting() throws Exception{

		IndexTask task1 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.v1.txt"); 
		IndexTask task3 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.v2.txt"); 

		boolean isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);


		boolean added = indexSynchronizer.putInQueue(task1, true);
		assertEquals(true, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);

		added = indexSynchronizer.putInQueue(task2, true);
		assertEquals(false, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);

		added = indexSynchronizer.putInQueue(task3, true);
		assertEquals(false, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);

		IndexTask waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(task3, waitingTask);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);

		//clean indexSynchronizer
		indexSynchronizer.doOnTaskFinished(task1);

	}


	//	getWaitingTask

	@Test
	public void testPopWaitingTask() throws Exception{

		IndexTask task1 = createTask(100, 202, IndexOperation.METADATA_INDEX, null); 
		IndexTask task2 = createTask(100, 202, IndexOperation.METADATA_INDEX, null); 
		IndexTask task3 = createTask(100, 202, IndexOperation.METADATA_INDEX, null); 

		IndexTask waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertNull(waitingTask);

		indexSynchronizer.putInQueue(task1, true);
		indexSynchronizer.putInQueue(task2, true);

		waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(task2, waitingTask);

		indexSynchronizer.putInQueue(task2, true);
		indexSynchronizer.putInQueue(task3, true);

		waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(task3, waitingTask);

		//clean indexSynchronizer
		indexSynchronizer.doOnTaskFinished(task1);
	}

	//DIFFERENT OPERATION TYPES
	@Test
	public void testPutInQueue_withDifferentOperationTypes() throws Exception{

		IndexTask task1 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task11 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean result = indexSynchronizer.putInQueue(task1, false);
		assertEquals(true, result);

		result = indexSynchronizer.putInQueue(task2, false);
		assertEquals(true, result);

		result = indexSynchronizer.putInQueue(task11, false);
		assertEquals(false, result);
		IndexTask taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertNull(taskWaiting);
		taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNull(taskWaiting);

		result = indexSynchronizer.putInQueue(task11, true);
		assertEquals(false, result);
		taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertNotNull(taskWaiting);
		taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNull(taskWaiting);

		//clean synchronizer
		indexSynchronizer.doOnTaskFinished(task1);
		indexSynchronizer.doOnTaskFinished(task2);

	}


	//	doOnTaskFinished

	@Test
	public void testDoOnTaskFinished_twoTasks_withDifferentOperationTypes() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");

		IndexTask task1 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task11 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 

		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task21 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean result = indexSynchronizer.putInQueue(task1, false);
		assertEquals(true, result);
		result = indexSynchronizer.putInQueue(task11, true);
		assertEquals(false, result);

		result = indexSynchronizer.putInQueue(task2, false);
		assertEquals(true, result);
		result = indexSynchronizer.putInQueue(task21, true);
		assertEquals(false, result);

		//this triggers the execution of the waiting task and thus the synchronizer to pass the waiting task to the executing map
		indexSynchronizer.doOnTaskFinished(task1);

		IndexTask taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertNull(taskWaiting);
		indexSynchronizer.doOnTaskFinished(task11);

		taskWaiting = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertNotNull(taskWaiting);
		assertEquals(task21, taskWaiting);

		indexSynchronizer.doOnTaskFinished(task2);
	}



	//	isOtherTaskWaiting
	@Test
	public void testIsOtherTaskWaiting_withDifferentOperationTypes() throws Exception{

		IndexTask task11 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task12 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.v1.txt"); 
		IndexTask task13 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.v2.txt"); 

		IndexTask task21 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task22 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.v1.txt"); 

		boolean isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);


		boolean added = indexSynchronizer.putInQueue(task11, true);
		assertEquals(true, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);

		added = indexSynchronizer.putInQueue(task12, true);
		assertEquals(false, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);


		added = indexSynchronizer.putInQueue(task21, true);
		assertEquals(true, added);
		added = indexSynchronizer.putInQueue(task22, true);
		assertEquals(false, added);

		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);


		added = indexSynchronizer.putInQueue(task13, true);
		assertEquals(false, added);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);

		IndexTask waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(task13, waitingTask);
		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(false, isOtherWaiting);

		isOtherWaiting = indexSynchronizer.isOtherTaskWaiting(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(true, isOtherWaiting);

		//clean synchronizer
		indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		indexSynchronizer.doOnTaskFinished(task21);
		indexSynchronizer.doOnTaskFinished(task11);

	}


	//	getWaitingTask

	@Test
	public void testPopWaitingTask_withDifferentOperationTypes() throws Exception{

		IndexTask task11 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task12 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.v1.txt"); 
		IndexTask task13 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.v2.txt"); 

		IndexTask task21 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
		IndexTask task22 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.v1.txt"); 

		IndexTask waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertNull(waitingTask);

		indexSynchronizer.putInQueue(task11, true);
		indexSynchronizer.putInQueue(task12, true);

		waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(task12, waitingTask);

		indexSynchronizer.putInQueue(task12, true);
		indexSynchronizer.putInQueue(task13, true);

		indexSynchronizer.putInQueue(task21, true);
		indexSynchronizer.putInQueue(task22, true);

		waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		assertEquals(task13, waitingTask);

		waitingTask = indexSynchronizer.popWaitingTask(IndexOperation.CONTENT_INDEX.getType(), 202);
		assertEquals(task22, waitingTask);

		//clean synchronizer
		indexSynchronizer.doOnTaskFinished(task11);
		indexSynchronizer.doOnTaskFinished(task21);

	}


	@Test
	public void testIsIndexIdle() throws Exception{
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenarioIndex_02.xml");
		IndexTask task11 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task12 = createTask(100, 202, IndexOperation.METADATA_INDEX, "file1.txt"); 
		IndexTask task21 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 

		boolean idle = indexSynchronizer.isIndexIdle();
		assertEquals(true, idle);

		indexSynchronizer.putInQueue(task11, true);
		indexSynchronizer.putInQueue(task12, true);

		idle = indexSynchronizer.isIndexIdle();
		assertEquals(false, idle);

		indexSynchronizer.popWaitingTask(IndexOperation.METADATA_INDEX.getType(), 202);
		indexSynchronizer.doOnTaskFinished(task11);
		idle = indexSynchronizer.isIndexIdle();
		assertEquals(true, idle);

		indexSynchronizer.putInQueue(task21, true);

		idle = indexSynchronizer.isIndexIdle();
		assertEquals(false, idle);

		indexSynchronizer.doOnTaskFinished(task21);
		idle = indexSynchronizer.isIndexIdle();
		assertEquals(true, idle);

		indexSynchronizer.putInQueue(task11, true);
		indexSynchronizer.putInQueue(task21, true);
		idle = indexSynchronizer.isIndexIdle();
		assertEquals(false, idle);

		indexSynchronizer.doOnTaskFinished(task11);
		indexSynchronizer.doOnTaskFinished(task21);
	}



	//PRIVATES

	private IndexTask createTask(Integer repositoryId, Integer objectId,  IndexOperation operation, String fileName) {
		IndexTask indexTask = taskFactory.getIndexService();
		indexTask.setRepositoryId(repositoryId);
		indexTask.setObjectId(objectId);
		indexTask.setOperation(operation);
		indexTask.setFileName(fileName);
		return indexTask;
	}

}
