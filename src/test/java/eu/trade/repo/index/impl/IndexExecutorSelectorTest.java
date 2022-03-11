package eu.trade.repo.index.impl;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.index.IndexExecutorSelector;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.IndexTaskServiceFactory;
import eu.trade.repo.index.model.IndexOperation;

public class IndexExecutorSelectorTest  extends BaseTestClass {

	@Autowired
	private IndexExecutorSelector executorSelector;

	@Autowired
	@Qualifier("taskExecutorSmallTasks")
	private TaskExecutor smallTasksExecutor;

	@Autowired
	@Qualifier("taskExecutorLargeTasks")
	private TaskExecutor largeTasksExecutor;


	@Autowired
	@Qualifier("taskExecutorMetadataTasks")
	private TaskExecutor metadataTasksExecutor;

	@Autowired
	private IndexTaskServiceFactory taskFactory;

	@Autowired
	private IndexConfigHolder indexConfigHolder;


	@Test
	public void testGetTaskExecutor(){
		long limitSize = indexConfigHolder.getQueueSelectionLimitSize();
		IndexTask metadataTask = createTask(IndexOperation.METADATA_INDEX, 0L);
		IndexTask metadataDeleteTask = createTask(IndexOperation.METADATA_INDEX_DELETE, 0L);
		IndexTask contentSmallTask = createTask(IndexOperation.CONTENT_INDEX, limitSize - 1);
		IndexTask contentBigTask = createTask(IndexOperation.CONTENT_INDEX, limitSize + 1);

		TaskExecutor selectedExecutor = executorSelector.getTaskExecutor(metadataTask);
		assertEquals(metadataTasksExecutor, selectedExecutor);

		selectedExecutor = executorSelector.getTaskExecutor(metadataDeleteTask);
		assertEquals(metadataTasksExecutor, selectedExecutor);

		selectedExecutor = executorSelector.getTaskExecutor(contentSmallTask);
		assertEquals(smallTasksExecutor, selectedExecutor);

		selectedExecutor = executorSelector.getTaskExecutor(contentBigTask);
		assertEquals(largeTasksExecutor, selectedExecutor);
	}

	private IndexTask createTask(IndexOperation operation, long fileSize) {
		IndexTask indexTask = taskFactory.getIndexService();
		indexTask.setOperation(operation);
		indexTask.setFileSize(BigInteger.valueOf(fileSize));
		return indexTask;
	}

}
