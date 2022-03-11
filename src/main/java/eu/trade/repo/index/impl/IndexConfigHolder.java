package eu.trade.repo.index.impl;

import static eu.trade.repo.util.Constants.DEFAULT_ATOMIC_ENABLED;
import static eu.trade.repo.util.Constants.PROPNAME_INDEX_ATOMIC_ENABLED;

import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.trade.repo.util.ConfigUtils;
import eu.trade.repo.util.Constants;

/**
 * Manages the index configuration, providing default values when necessary.
 * 
 * It always access the commons-configuration object instead of storing the properties values to trigger reloading policies, etc.
 * 
 */
public class IndexConfigHolder {

	@Autowired @Qualifier("indexConfig")
	private Configuration config;

	//config properties
	
	
	public int getDocumentIndexWordLimitSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_DOCUMENT_WORD_LIMIT, Constants.DEFAULT_DOCUMENT_WORD_LIMIT);
	}
	
	public int getSegmentSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_PAGE_SIZES_TOKEN_SEGMENT, Constants.DEFAULT_TOKEN_SEGMENT_SIZE);
	}

	public int getWordPageSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_PAGE_SIZES_WORD_PAGE, Constants.DEFAULT_WORD_PAGE_SIZE);
	}
	
	public int getWordPositionPageSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_PAGE_SIZES_WORD_POSITION_PAGE, Constants.DEFAULT_WORD_POSITION_PAGE_SIZE);
	}

	public int getDeletePageSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_PAGE_SIZES_DELETE_PAGE, Constants.DEFAULT_DELETE_PAGE_SIZE);
	}
	
	public int getMaxIndexAttempts(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_MAX_ATTEMPTS, Constants.DEFAULT_MAX_ATTEMPTS);
	}
	
	public double getRemainingPoolQueueThreshold(){
		return ConfigUtils.getPercentage(config, Constants.PROPNAME_INDEX_QUEUE_REMAINING_THRESHOLD, Constants.DEFAULT_QUEUE_REMAINING_THRESHOLD);
	}
	
	public int getQueueSmallTasksCapacity(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_POOL_SMALL_TASKS_QUEUE_CAPACITY, Integer.MAX_VALUE);
	}
	
	public int getQueueLargeTasksCapacity(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_POOL_LARGE_TASKS_QUEUE_CAPACITY, Integer.MAX_VALUE);
	}
	
	public int getQueueMetadataTasksCapacity(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_POOL_METADATA_TASKS_QUEUE_CAPACITY, Integer.MAX_VALUE);
	}
	
	public int getQueueSelectionLimitSize(){
		return ConfigUtils.getIntPositive(config, Constants.PROPNAME_INDEX_POOL_SELECTION_LIMIT_SIZE, Constants.DEFAULT_POOL_SELECTION_LIMIT_SIZE);
	}
	
	public boolean getAtomicIndexEnabled(){
		return config.getBoolean(PROPNAME_INDEX_ATOMIC_ENABLED, DEFAULT_ATOMIC_ENABLED);
	}
}
