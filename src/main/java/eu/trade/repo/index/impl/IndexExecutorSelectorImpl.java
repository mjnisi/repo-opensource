package eu.trade.repo.index.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;

import eu.trade.repo.index.IndexExecutorSelector;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;


/**
 * Select the appropriate task executor for a given index task:
 * 
 * - If the index task indicates a METADATA operation, it selects metadataTasksExecutor
 * - If the index task indicates a CONTENT operation, depending on the index.pool.selection.limitSize property value and the 
 * stream file size, it selects small or large task executor.
 * 
 * @author abascis
 *
 */
public class IndexExecutorSelectorImpl implements IndexExecutorSelector{

	@Autowired
	private IndexConfigHolder indexConfigHolder;

	@Autowired
	@Qualifier("taskExecutorSmallTasks")
	private TaskExecutor smallTasksExecutor;

	@Autowired
	@Qualifier("taskExecutorLargeTasks")
	private TaskExecutor largeTasksExecutor;


	@Autowired
	@Qualifier("taskExecutorMetadataTasks")
	private TaskExecutor metadataTasksExecutor;


	public TaskExecutor getTaskExecutor(IndexTask task){
		if ( IndexOperationType.METADATA.equals(task.getOperation().getType())){
			return metadataTasksExecutor; 
		}else if ( null == task.getFileSize() || task.getFileSize().longValue() >= indexConfigHolder.getQueueSelectionLimitSize()){
			return largeTasksExecutor;
		}
		return smallTasksExecutor;
	}
}
