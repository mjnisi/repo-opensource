//package eu.trade.repo.index.impl;
//
//import java.math.BigInteger;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import eu.trade.repo.BaseTestClass;
//import eu.trade.repo.index.Index;
//import eu.trade.repo.index.IndexTask;
//import eu.trade.repo.index.IndexTaskServiceFactory;
//import eu.trade.repo.index.model.IndexOperation;
//
//public class IndexImplTest extends BaseLobTestClass{
//
//	@Autowired
//	private Index index;
//
//	@Autowired
//	private IndexTaskServiceFactory taskFactory;
//
//	public void testExecuteIfAllowed_executionNotAllowed(){
//		//executeIfAllowed(IndexTask indexTask, boolean waiting)
//
//		IndexTask task1 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
//		IndexTask task2 = createTask(100, 202, IndexOperation.CONTENT_INDEX, "file1.txt"); 
//
//		index.executeOperation(100, 202, "file1.txt", BigInteger.valueOf(245), true, IndexOperation.CONTENT_INDEX);
//	}
//
//	public void testExecuteIfAllowed_executionAllowed(){
//		//executeIfAllowed(IndexTask indexTask, boolean waiting)
//	}
//
//	//PRIVATES
//
//	private IndexTask createTask(Integer repositoryId, Integer objectId,  IndexOperation operation, String fileName) {
//		IndexTask indexTask = taskFactory.getIndexService();
//		indexTask.setRepositoryId(repositoryId);
//		indexTask.setObjectId(objectId);
//		indexTask.setOperation(operation);
//		indexTask.setFileName(fileName);
//		return indexTask;
//	}
//
//}
