package eu.trade.repo.index.jobs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import eu.trade.repo.index.impl.AbstractIndex;

public abstract class AbstractIndexBackgroundJob extends AbstractIndex{

	public static final String OWNER_JOB_CLEAN_ORPHAN_INDEXES = "cleanOrphanIndexes";
	public static final String OWNER_JOB_RETRY_CONTENT_INDEX_IN_ERROR = "retryContentIndexesInError"; 
	public static final String OWNER_JOB_RETRY_METADATA_INDEX_IN_ERROR = "retryMetadataIndexesInError"; 

	private String owner;

	protected String getOwner(String jobName){
		if( null == owner){
			DateFormat df = new SimpleDateFormat("HH:mm:ss");
			owner = new StringBuilder(jobName).append("_").append(df.format(Calendar.getInstance().getTime())).toString();
		}
		return owner;
	}
}
