package eu.trade.repo.index.impl.ops;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.IndexTransientMetadataDelegate;
import eu.trade.repo.index.IndexNotSupportedException;
import eu.trade.repo.index.IndexRuntimeException;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.model.Property;

/**
 * Operator that process the METADATA index part of a cmis object.
 * 
 * Defines how to register and retrieve data from the transient index when IndexTask.operationtype is METADATA. 
 * 
 */
public class MetadataIndexOperator extends AbstractIndexOperator{

	private static final Logger LOG = LoggerFactory.getLogger(MetadataIndexOperator.class);

	@Autowired
	private IndexTransientMetadataDelegate indexTransientMetadataDelegate;

	
	@Override
	public IndexOperationType getOperationTypeSupported() {
		return IndexOperationType.METADATA;
	}
	
	
	@Override
	protected boolean processToTransientIndexPart(IndexTask indexTask) throws IndexNotSupportedException {
		try {
			LOG.info("Processing document metadata to temporary index for document {}", indexTask.getObjectId());
			List<Property> propertyList = getIndexLinkedDelegate().obtainPropertyListByObjectId(indexTask.getObjectId());
			if( null == propertyList || 0 == propertyList.size() ) {
				throw new IndexRuntimeException("Error while retrieving the object properties (objectId = " + indexTask.getObjectId() + ")");
			}

			//1. Process the content into the temporary index
			indexTransientMetadataDelegate.processMetadataToTransientIndex(indexTask.getRepositoryId(), indexTask.getObjectId(), propertyList);

			LOG.info("Metadata temporary index DONE for document {}.", indexTask.getObjectId());

		} catch (IndexNotSupportedException ie) {
			LOG.debug(ie.getLocalizedMessage(), ie);
			throw ie;
			
		} catch (Exception e) {
			LOG.info(e.getLocalizedMessage(), e);
			throw new IndexRuntimeException("Error indexing metadata: " + e.getLocalizedMessage(), e);
		} 
		return true;
	}

	
	
	@Override
	protected List<String> obtainWordPageFromTransientByObjectId(Integer objectId, int wordStart, int wordPageSize) {
		return indexTransientMetadataDelegate.obtainWordPageByObjectId(objectId, wordStart, wordPageSize);
	}

	@Override
	protected List<TransientDTO> obtainWordObjectFromTransientByObjectId(Integer objectId, List<String> wordList) {
		return indexTransientMetadataDelegate.obtainWordObjectPageByObjectId(objectId, wordList);
	}

	@Override
	protected List<TransientDTO> obtainWordPositionPageFromTransientByObjectId(Integer objectId, List<String> wordList, int wordStart, int wordPageSize) {
		return indexTransientMetadataDelegate.obtainWordPositionPageByObjectId(objectId, wordList, wordStart, wordPageSize);
	}
	
	@Override
	protected void deleteTransientIndexPartByObjectId(Integer objectId, int deletePageSize) {
		indexTransientMetadataDelegate.deleteTransientContentIndexByObjectId(objectId, deletePageSize);
	}
	
	@Override
	protected List<Integer> obtainObjectTypePropertyIdListFromTransientByObjectId(Integer objectId) {
		return indexTransientMetadataDelegate.obtainObjectTypePropertyIdListFromTransientByObjectId(objectId);
	}
}
