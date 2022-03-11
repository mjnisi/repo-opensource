package eu.trade.repo.delegates;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.IndexObjectDTO;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;

/**
 * Defines the interaction with the permanent index.
 * 
 * 	 - register words, word-objects and word-positions belonging to a cmis object 
 *   - delete parts (metadata or content) of the permanent index
 *   - obtain orphan indexes (indexes for which its cmis object no longer exists)
 *   - obtain cmis objects whose indexes (content or metadata) are to be re-made.
 * 
 * @author abascis
 *
 */
public interface IndexDelegate {

	// write permanent index
	/**
	 * Saves the list word passed as argument to the permanent index. The implementation of this method has to take into account that
	 * words in wordList can be already registered for the given repository.
	 * 
	 * It is used while indexing a part (metadata or content) of a cmis object, to save a word page.
	 * 
	 * @param repositoryId repository the cmis object belongs
	 * @param objectId cmis object being indexed
	 * @param wordList word list to be saved.
	 * @return a map bringing together each word (string) with its complete index information.
	 */
	Map<String, WordDTO> writeWordPage(Integer repositoryId, Integer objectId, List<String> wordList);

	/**
	 * Saves the word-object list passed as argument to the permanent index. Each word-object is related to a word, to a cmis object and, depending on the index operation type (metadata or content),
	 * to a cmis object property.
	 * 
	 * It is used while indexing a part (metadata or content) of a cmis object, to save the word-object list related to a word page.
	 * 
	 * @param repositoryId repository the cmis object belongs
	 * @param objectId	cmis object being indexed
	 * @param partialWordObjectList	list of word objects to save
	 * @param wordMap map that relates a word (string) to the complete information about this index word(id)
	 * @param operationType index operation type being executed
	 * @return a map that brings together each word with the word-object/s related. When the index operation type being executed is CONTENT, the relationship between
	 * a word and word-object is one to one, while when it is METADATA, the relationship is one to multiple meaning that a different word-object is registered for each
	 * pair word-property and thus the IWordObjectExtractor is responsible of returning the appropriate word-object for each metadata property.
	 */
	Map<String, ? extends IWordObjectExtractor> writeWordObjectPage(Integer repositoryId, Integer objectId, List<TransientDTO> partialWordObjectList, Map<String, WordDTO> wordMap, IndexOperationType operationType);

	/**
	 * Saves the word-position list passed as argument to the permanent index. One word-position is saved for each token found in the text to index. The word-position
	 * contains information about the order of tokens in the indexing unit. That is, when indexing the CONTENT, the order increases all along the text. And when indexing
	 * METADATA the indexing unit considered is each metadata property.
	 * 
	 * @param repositoryId repository the cmis object belongs
	 * @param objectId cmis object being indexed
	 * @param partialWordPositionList list of word-positions to be saved
	 * @param wordObjectMap a map bringing together words and its related word-objects. 
	 */
	<T extends IWordObjectExtractor> void writeWordPositionPage(Integer repositoryId, Integer objectId, List<TransientDTO> partialWordPositionList, Map<String, T> wordObjectMap);


	// delete permanent index
	/**
	 * Obtain a page of word objects to be deleted, related to a part (metadata or content) of the index of a cmis object
	 * 
	 * @param objectId repository the cmis object belongs
	 * @param deletePageSize cmis object being indexed
	 * @param operationType index operation type being executed
	 * @return list of word-objects id to be deleted
	 */
	List<Integer> obtainWordObjectPageToDelete(Integer objectId, int deletePageSize, IndexOperationType operationType);

	/**
	 * Deletes a list of word objects and its related index_positions.
	 * @param wordObjectIdList
	 * @return
	 */
	int deletePermanentIndexPartPage(List<Integer> wordObjectIdList);


	// index background jobs
	/**
	 * Obtain the list of cmis object ids (retrieving from the permanent index) for which its cmis object no longer exists.
	 * @return list of cmis object ids.
	 */
	List<Integer> obtainOrphanIndexes();

	/**
	 * Obtain the list of cmis objects whose content indexes are to be re-made, meaning they are in ERROR or NONE states.
	 * 
	 * @param maxAttempts the maximum number of times the indexation is to be re-tried.
	 * @param resultLimit the maximum number of cmis object to be retrieved.
	 * @param minFileSize the minumum file size of the cmis object stream for the object to be retrieved
	 * @param maxFileSize the maximum file size of the cmis object stream for the object to be retrieved
	 * @return list of cmis objects.
	 */
	List<IndexObjectDTO> obtainContentIndexesInErrorOrUnfinishedState(int maxAttempts, int resultLimit, long minFileSize, long maxFileSize);

	/**
	 * Obtain the list of cmis objects whose metadata indexes are to be re-made, meaning they are in ERROR or NONE states.
	 * 
	 * @param maxAttempts the maximum number of times the indexation is to be re-tried.
	 * @param resultLimit the maximum number of cmis object to be retrieved.
	 * @return list of cmis objects.
	 */
	List<IndexObjectDTO> obtainMetadataIndexesInErrorOrUnfinishedState(int maxAttempts, int resultLimit);


	
	List<Integer> obtainObjectPropertyTypeIdListFromObjectWordObjects(Integer objectId);
	
	
	Map<Integer, Integer> obtainRepositoryByObjectList(Set<Integer> objectIdList) ;
}
