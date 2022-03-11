package eu.trade.repo.index.impl.ops;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.IndexDelegate;
import eu.trade.repo.delegates.IndexLinkedDelegate;
import eu.trade.repo.index.IndexNotSupportedException;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.IndexRuntimeException;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;


/**
 * Defines the indexing process, leaving the operations related to transient data retrieval and deletion to be implemented by concrete index type 
 * operators
 * 
 * @author abascis
 *
 */
public abstract class AbstractIndexOperator implements IndexPartOperator{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractIndexOperator.class);


	@Autowired
	//permanent index (3 index tables)
	private IndexDelegate indexDelegate;

	@Autowired
	//jpa dbs
	private IndexLinkedDelegate indexLinkedDelegate;

	@Autowired
	private IndexConfigHolder indexConfigHolder;

	/**
	 * Retrieves data to be indexed based on information provided by IndexTask, process it to obtain tokens and save them to
	 * transient index. 
	 * 
	 * @param indexTask
	 * @return
	 * @throws IndexNotSupportedException
	 */
	protected abstract boolean processToTransientIndexPart(IndexTask indexTask) throws IndexNotSupportedException;

	/**
	 * Obtains a page of different words (number = wordPageSize starting in wordStart index) contained in the transient index of objectId.
	 * 
	 * @param objectId
	 * @param wordStart
	 * @param wordPageSize
	 * @return
	 */
	protected abstract List<String> obtainWordPageFromTransientByObjectId(Integer objectId, int wordStart, int wordPageSize);



	protected abstract List<Integer> obtainObjectTypePropertyIdListFromTransientByObjectId(Integer objectId);

	/**
	 * Obtains all word objects (different words by document's content or by object's property with frequency information) that match the 
	 * word list passed as parameter
	 * 
	 * @param objectId
	 * @param wordList
	 * @return
	 */
	protected abstract List<TransientDTO> obtainWordObjectFromTransientByObjectId(Integer objectId, List<String> wordList);

	/**
	 * Obtains a page of tokens (words as they appear in the document's or property's content with information about position) that match the
	 * word list pass as parameter 
	 * 
	 * @param objectId
	 * @param wordList
	 * @param wordStart start token index
	 * @param wordPageSize number of tokens to retrieve
	 * @return
	 */
	protected abstract List<TransientDTO> obtainWordPositionPageFromTransientByObjectId(Integer objectId, List<String> wordList, int wordStart, int wordPageSize);

	/**
	 * Deletes the transient index of an index part in chuncks of deletePageSize tokens.
	 * 
	 * @param objectId
	 * @param deletePageSize
	 */
	protected abstract void deleteTransientIndexPartByObjectId(Integer objectId, int deletePageSize);


	@Override
	/**
	 * Deletes an index part of a cmis object, which involves to delete the transient and permanent index part of a cmis object.
	 * 
	 */
	public void deleteIndexPart(IndexTask indexTask) {
		cleanIndexPart(indexTask, null);
	}

	@Override
	/**
	 * Creates an index part of an object from scratch.
	 */
	public void createIndexPart(IndexTask indexTask) {
		CMISObject object = indexLinkedDelegate.getCmisObject(indexTask.getObjectId());
		if( null == object ){
			LOG.warn("Trying to index {} for object {}. It does not exist anymore, so no indexing is done.", indexTask.getOperation().getType(), indexTask.getObjectId());
			return;
		}
		IndexingState currentState = indexLinkedDelegate.getObjectIndexingState(indexTask.getObjectId(), indexTask.getOperation().getType());
		LOG.info("Indexing object {} in state {}", indexTask.getObjectId(), currentState.toString());

		currentState = cleanIndexPart(indexTask, currentState);

		if( !indexTask.hasToStop()  ){
			createIndexInternal(indexTask, currentState);
		}
	}


	/**
	 * This method cleans the data of a document, related to the index operation type performed, in all the index tables.
	 * Depending on the indexing state of the document, it will be data in all or a subset of index tables.
	 * 
	 * This method delete data only in the tables that are supposed to have data depending on the indexing state
	 * of the document.
	 * 
	 * The document indexing state is update to reflect changes, only if IndexTask.isUpdateObjectIndexInfo = true.
	 * 
	 * (Note that the switch statement does not use breaks)
	 * 
	 * @param repositoryId
	 * @param objectId
	 * @param step
	 * @return
	 */
	private IndexingState cleanIndexPart(IndexTask indexTask, IndexingState step){

		IndexingState currentState = step;
		Integer objectId = indexTask.getObjectId();
		
		// clean tmp table
		if( indexTask.hasToStop() ){
			LOG.info("Index task stopped before cleaning content for document {}", objectId);
			return currentState;
		}
		LOG.info("Cleaning content transient index for document {}", objectId);
		deleteTransientIndexPartByObjectId(objectId, indexConfigHolder.getDeletePageSize());


		//clean dictionary > docterm > position
		if( indexTask.hasToStop() ){
			LOG.info("Index task stopped after cleaning content transient index for document {}", objectId);
			return currentState;
		}
		LOG.info("Cleaning content permanent index for document {}", objectId);

		int totalDeleted = deletePermanentIndexPart(objectId, indexConfigHolder.getDeletePageSize());
		LOG.debug("Deleted {} index_word_object and index_word_positions", totalDeleted);
		currentState = IndexingState.NONE;

		if( indexTask.isUpdateObjectIndexInfo() ){
			indexLinkedDelegate.updateObjectIndexingState(indexTask.getObjectId(), currentState, indexTask.getOperation().getType());
		}
		LOG.info("Cleaning index DONE for document {}", objectId);
		return currentState;
	}


	private int deletePermanentIndexPart(Integer objectId, int deletePageSize){
		boolean done = false;
		List<Integer> wordObjectIdList = null;
		int deleted = 0;
		while( !done ){
			wordObjectIdList =  indexDelegate.obtainWordObjectPageToDelete(objectId, deletePageSize, getOperationTypeSupported());
			LOG.debug(">>>>>>>>>>>>>>>>>>>>>............ wordObjectList to DELETE SIZE: {}", wordObjectIdList.size());
			deleted += indexDelegate.deletePermanentIndexPartPage(wordObjectIdList);
			done = ( wordObjectIdList.size() < deletePageSize -1);
		}
		return deleted;
	}

	/**
	 * This method manages the indexing process. There are several steps:
	 * 
	 * + Extract content from the file or object metadata and store the tokens (each word in the document) to a temporary table
	 * + Group different words in the document and save in the dictionary table those that were not present
	 * + Group different words in the document or property and save them in the term table with their frequency
	 * + Save each token with its position in the position table
	 * + Clean the temporary table.
	 * 
	 * 
	 * The cmis object indexing state is updated after cleaning and process the whole content to reflect changes.
	 * 
	 * (Note that the switch statement does not use breaks)
	 * 
	 * (Note that this method is responsible for closing the content extractor stream)
	 * 
	 * @param repositoryId
	 * @param objectId
	 * @param step
	 */
	private void createIndexInternal(IndexTask indexTask, IndexingState step){

		IndexingState currentState = step;
		Integer objectId = indexTask.getObjectId();
		Integer repositoryId = indexTask.getRepositoryId();

		try{

			if( indexTask.hasToStop()){
				LOG.info("Index task stopped before processing content for document {}", objectId);
				return;
			}

			boolean fullProcessed = processToTransientIndexPart(indexTask);

			if( indexTask.hasToStop()){
				LOG.info("Index task stopped after processing content to temporary index for document {}", objectId);
				return;
			}

			storeToPermanentIndex(repositoryId, objectId);

			if( indexTask.hasToStop()){
				LOG.info("Index task stopped after storing to content permanent index for document {}", objectId);
				return;
			}

			//3. clean temporary index
			deleteTransientIndexPartByObjectId(objectId, indexConfigHolder.getDeletePageSize());
			currentState = fullProcessed? IndexingState.INDEXED : IndexingState.PARTIALLY_INDEXED;

		}catch(IndexNotSupportedException e){
			currentState = IndexingState.NOT_INDEXABLE;
			LOG.warn("Index not supported for object {}. Cause: {} ", indexTask.getObjectId(), e.getLocalizedMessage());

		}catch (Exception e) {
			currentState = IndexingState.ERROR;
			LOG.warn("Error indexing document: {}", indexTask.getObjectId(), e);

		}finally{
			indexLinkedDelegate.updateObjectIndexingState(objectId, currentState, indexTask.getOperation().getType());
			LOG.info("Index DONE for document {}. Result state: {}", objectId, currentState);
		}
	}


	/**
	 * This method manages the read and write data from temporary index to definitive index tables.
	 * 
	 * @param repositoryId
	 * @param objectId
	 */
	private void storeToPermanentIndex(Integer repositoryId, Integer objectId) {
		LOG.info("> storePermanentIndex.BEGIN: repository = {}, documentId = {}", repositoryId, objectId);

		try {
			int wordStart = 0;
			int wordPageSize = indexConfigHolder.getWordPageSize();

			Map<String, WordDTO> wordMap = null;
			Map<String, ? extends IWordObjectExtractor> wordObjectMap = null;

			boolean done = false;
			List<String> transientWordList = null;
			int i = 0;

			//The saving is paginated by word pages; word_number = wordObject_number; wordObject_number < wordPosition_number
			while (!done) {
				LOG.info(">>>> Writing index page {} for documentId: {} ", i++, objectId);

				//words
				transientWordList = obtainWordPageFromTransientByObjectId(objectId, wordStart, wordPageSize);

				LOG.debug(">>>> WRITE WORD page");
				wordMap = indexDelegate.writeWordPage(repositoryId, objectId, transientWordList);

				//wordobjects related
				List<TransientDTO> partialWordObjectList = obtainWordObjectFromTransientByObjectId(objectId, transientWordList);

				LOG.debug(">>>> WRITE word OBJECTS");
				wordObjectMap = indexDelegate.writeWordObjectPage(repositoryId, objectId, partialWordObjectList, wordMap, getOperationTypeSupported());

				//wordpositions related
				writeWordPositionPagesRelatedToWordList(repositoryId, objectId, transientWordList, wordObjectMap, indexConfigHolder.getWordPositionPageSize());

				done = wordMap.size() < wordPageSize -1;
				if( LOG.isDebugEnabled() ){
					LOG.debug(">>>> done? {}", done);
					LOG.debug(">>>>......... wordMap.size: {}", wordMap.size());
					LOG.debug(">>>>......... PAGE_SIZE_READ_WRITE -1: {}", wordPageSize -1);
				}
				wordStart += wordPageSize;
			}
			LOG.info("Permanent index DONE for object {}", objectId);

		} catch(Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
			throw new IndexRuntimeException(ex.getLocalizedMessage(), ex);
		}
	}


	private void writeWordPositionPagesRelatedToWordList( Integer repositoryId, Integer documentId, List<String> wordList, Map<String, ? extends IWordObjectExtractor> wordObjectMap, int pageSize){
		LOG.debug(">>> writePositionObjects for document id: {}", documentId);
		try {
			int start = 0;

			boolean done = false;
			List<TransientDTO> partialWordPositionList = null;
			int i = 0;

			while( !done ){
				LOG.debug(">>>>>>>> internal POSITION LOOP {}: ", i++);
				partialWordPositionList = obtainWordPositionPageFromTransientByObjectId(documentId, wordList, start, pageSize);
				LOG.debug(">>>>>>>>>> query for POSITIONS: start = {}; positions number read = {}", start, partialWordPositionList.size());
				indexDelegate.writeWordPositionPage(repositoryId, documentId, partialWordPositionList, wordObjectMap);
				done = partialWordPositionList.size() < pageSize -1;
				start += pageSize;
			}

		}catch(Exception e){
			LOG.error("Error executing: " + e.getLocalizedMessage(), e);
			throw e;
		}
	}


	public IndexLinkedDelegate getIndexLinkedDelegate() {
		return indexLinkedDelegate;
	}

	public IndexConfigHolder getIndexConfigHolder() {
		return indexConfigHolder;
	}

}
