package eu.trade.repo.index.jobs;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.IndexDelegate;
import eu.trade.repo.index.model.IndexOperation;


/**
 * Background job in charge of remove orphan information from the index (information about cmis objects already deleted) 
 * 
 */
public class CleanOrphanIndexesJob extends AbstractIndexBackgroundJob {
	private static final Logger LOG = LoggerFactory.getLogger(CleanOrphanIndexesJob.class);

	@Autowired
	private IndexDelegate indexDelegate;

	public void execute() {
		try {
			LOG.info("Starting clean orphan indexes service...");

			List<Integer> orphanObjectIdList = indexDelegate.obtainOrphanIndexes();
			LOG.info("Found {} orphan indexes", (null != orphanObjectIdList)? orphanObjectIdList.size() : 0); 
			registerIndexTasksForOrphanIndexList (orphanObjectIdList);

			LOG.info("Clean orphan indexes service Finished.");

		}catch(Exception e){
			LOG.error("Error cleaning orphan indexes: " + e.getLocalizedMessage(), e);

		} 
	}

	private void registerIndexTasksForOrphanIndexList(List<Integer> orphanObjectIdList){
		if( null != orphanObjectIdList ){
			for (Integer orphanObjectId : orphanObjectIdList) {
				try {
					LOG.debug("Firing index delete operation {} for cmis object: {}", IndexOperation.METADATA_INDEX_DELETE, orphanObjectId);
					executeOperation(null, orphanObjectId, null, null, false, IndexOperation.METADATA_INDEX_DELETE);
					
					LOG.debug("Firing index delete operation {} for cmis object: {}", IndexOperation.CONTENT_INDEX_DELETE, orphanObjectId);
					executeOperation(null, orphanObjectId, null, null, false, IndexOperation.CONTENT_INDEX_DELETE);
				} catch(Exception e) {
					LOG.warn("Exception trying to clean the orphan index for the object id:" + orphanObjectId, e);
				}
			}
		}
	}

	@Override
	public void executeOperation(Integer repositoryId, Integer objectId, String fileName, BigInteger fileSize, boolean updateObjectInfo, IndexOperation operation) {
		LOG.debug("Firing indexing operation {} for cmis object: {} ( file name: {})", operation, objectId, fileName);
		executeIfAllowed(repositoryId, objectId, fileName, fileSize, updateObjectInfo, operation, true, getOwner(OWNER_JOB_CLEAN_ORPHAN_INDEXES));
	}

}
