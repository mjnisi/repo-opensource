package eu.trade.repo.index.impl.ops;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.IndexTransientContentDelegate;
import eu.trade.repo.index.IndexNotSupportedException;
import eu.trade.repo.index.IndexRuntimeException;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.extractor.ContentExtractor;
import eu.trade.repo.index.extractor.ContentExtractorStreamResult;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;


/**
 * Operator that process the CONTENT index part of a cmis object.
 * 
 * Defines how to register and retrieve data from the transient index when IndexTask.operationtype is CONTENT. 
 * 
 * @author abascis
 *
 */
public class ContentIndexOperator extends AbstractIndexOperator{

	private static final Logger LOG = LoggerFactory.getLogger(ContentIndexOperator.class);


	@Autowired
	private ContentExtractor contentExtractor;

	@Autowired
	//transient index (h2)
	private IndexTransientContentDelegate indexTransientContentDelegate;
	
	
	
	@Override
	public IndexOperationType getOperationTypeSupported() {
		return IndexOperationType.CONTENT;
	}

	
	@Override
	protected boolean processToTransientIndexPart(IndexTask indexTask) throws IndexNotSupportedException {
		boolean fullProcessed = true;
		ContentExtractorStreamResult contentResult = null;
		try {
			LOG.info("Processing document content to temporary index for document {}", indexTask.getObjectId());
			contentResult = contentExtractor.extractContent(indexTask.getObjectId(), indexTask.getFileName());
			if( null == contentResult ) {
				throw new IndexRuntimeException("Error while extracting the document content (objectId = " + indexTask.getObjectId() + ")");
			}

			//1. Process the content into the temporary index
			fullProcessed = indexTransientContentDelegate.processContentToTransientIndex(indexTask.getRepositoryId(), indexTask.getObjectId(), contentResult.getContentReader(), getIndexConfigHolder().getSegmentSize(), getIndexConfigHolder().getDocumentIndexWordLimitSize());

			LOG.info("Content temporary index DONE for document {}. Full processed: {}", indexTask.getObjectId(), fullProcessed);

		} catch (IndexNotSupportedException ie) {
			LOG.debug(ie.getLocalizedMessage(), ie);
			throw ie;
			
		} catch (Exception e) {
			LOG.info(e.getMessage(), e);
			throw new IndexRuntimeException("Error indexing content: " + e.getLocalizedMessage(), e);

		} finally {
			if( null != contentResult && null != contentResult.getContentReader() ) {
				try {
					contentResult.getContentReader().close();
				} catch(IOException e) {
					LOG.error(e.getMessage(), e);
					throw new IndexRuntimeException("Error closing the indexing stream: " + e.getLocalizedMessage(), e);
				}
			}
		}
		return fullProcessed;
	}


	@Override
	protected List<String> obtainWordPageFromTransientByObjectId(Integer objectId, int wordStart, int wordPageSize) {
		return indexTransientContentDelegate.obtainWordPageByObjectId(objectId, wordStart, wordPageSize);
	}

	@Override
	protected List<TransientDTO> obtainWordObjectFromTransientByObjectId(Integer objectId, List<String> wordList) {
		return indexTransientContentDelegate.obtainWordObjectPageByObjectId(objectId, wordList);
	}

	@Override
	protected List<TransientDTO> obtainWordPositionPageFromTransientByObjectId(Integer objectId, List<String> wordList, int wordStart, int wordPageSize) {
		return indexTransientContentDelegate.obtainWordPositionPageByObjectId(objectId, wordList, wordStart, wordPageSize);
	}

	@Override
	protected void deleteTransientIndexPartByObjectId(Integer objectId,	int deletePageSize) {
		indexTransientContentDelegate.deleteTransientContentIndexByObjectId(objectId, getIndexConfigHolder().getDeletePageSize());
	}


	@Override
	protected List<Integer> obtainObjectTypePropertyIdListFromTransientByObjectId(Integer objectId) {
		return Arrays.asList(Integer.valueOf(-1));
	}

}
