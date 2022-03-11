package eu.trade.repo.index;

import org.springframework.core.task.TaskExecutor;


/**
 * Responsible for choosing the task executor where to send a task.
 * 
 * @author abascis
 *
 */
public interface IndexExecutorSelector {

	/**
	 * Obtains the appropriate task executor depending on the given task.
	 * 
	 * @param task
	 * @return
	 */
	TaskExecutor getTaskExecutor(IndexTask task);
}
