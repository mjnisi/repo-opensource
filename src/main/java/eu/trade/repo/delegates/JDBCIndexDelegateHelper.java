package eu.trade.repo.delegates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.index.IndexRuntimeException;
import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.PropertyWordObjectMap;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;
import eu.trade.repo.index.model.WordObjectDTO;


/**
 * Class responsible for accessing and modifying the permanent index database tables.
 */
public class JDBCIndexDelegateHelper extends AbstractJDBCIndexPermanentDelegate implements IndexDelegateHelper{

	private static final Logger LOG = LoggerFactory.getLogger(JDBCIndexDelegateHelper.class);

	//resultset indexes

	private static final int WORD_OBJECTS_WORD_OBJECT_ID = 2;
	private static final int WORD_OBJECTS_WORD_ID = 3;
	private static final int WORD_OBJECTS_OBJECT_ID = 4;
	private static final int WORD_OBJECTS_FREQUENCY = 5;
	private static final int WORD_OBJECTS_PROPERTY_ID = 6;
	private static final int WORD_OBJECTS_OBJ_PROP_TYPE_ID = 7;

	// COMMON ( content / metadata )
	//queries INSERT
	private static final String QUERY_INSERT_WORD = "insert into index_word (ID, REPOSITORY_ID, WORD) values(sq_index_word.NEXTVAL, :repositoryId, :word)";
	private static final String QUERY_INSERT_WORD_OBJECT = "insert into index_word_object (ID, word_id, object_id, property_id, object_type_property_id, frequency, sqrt_frequency) values(sq_index_word_object.NEXTVAL, :wordId, :objectId, :propertyId, :objPropTypeId, :frequency, :sqrtFrequency)";
	private static final String QUERY_INSERT_WORD_POSITION = "insert into index_word_position (word_object_id, position, STEP) values(:wordObjectId, :position, :step)";

	//queries DELETE
	private static final String QUERY_DELETE_WORD_POSITION_BY_WORD_OBJECT_ID_LIST =	"delete from index_word_position where word_object_id in (:wordObjectIdList)";
	private static final String QUERY_DELETE_WORD_OBJECT_BY_WORD_OBJECT_ID_LIST = "delete from index_word_object where id in (:wordObjectIdList)";

	// CONTENT (property_id is null)
	private static final String QUERY_CONTENT_WORD_OBJECTS_BY_DOCUMENT_AND_WORDLIST = 
			"select w.word, wo.id, wo.word_id, wo.object_id, wo.frequency " +
					"from index_word_object wo join index_word w on wo.word_id=w.id and wo.object_id = :objectId and w.word in (:wordList) " +
					"where wo.property_id = -1";


	// METADATA
	private static final String QUERY_METADATA_WORD_OBJECTS_BY_DOCUMENT_AND_WORDLIST = 
			"select w.word, wo.id, wo.word_id, wo.object_id, wo.frequency, wo.property_id, wo.object_type_property_id " +
					"from index_word_object wo join index_word w on wo.word_id=w.id and wo.object_id = :objectId and w.word in (:wordList) " +
					"where wo.property_id > -1";


	// ================== WRITE WORD OBJECTS =========================================
	/**
	 * Write a page of wordObjects into the definitive index tables.
	 * The wordObjects are related to a cmis object and they are all the different words of that document in addition to its measures
	 * 
	 * @param repositoryId repository to which the cmis object belongs
	 * @param documentId  the cmis object internal id
	 * @param partialWordObjectList the list of transientDTO read from the temporary index table corresponding to a page of words and grouped by different word
	 * @param wordMap map containing all of the words of a page and related database information
	 * 
	 * @return a map having the word as key and all the information related to wordobjects in a WordObjectDTO object as value. 
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public  Map<String, ? extends IWordObjectExtractor> writeWordObjectPage(Integer repositoryId, Integer objectId, List<TransientDTO> partialWordObjectList, final Map<String, WordDTO> wordMap, IndexOperationType operationType){
		LOG.info(">>> writeWordObjects for object id: {}", objectId);
		try {
			List<Map<String, ?>> batch = new ArrayList<Map<String, ?>>();

			Integer propertyId = null;
			Integer objectTypePropertyId = null;
			for (TransientDTO item : partialWordObjectList) {
				propertyId = item.getPropertyId();
				objectTypePropertyId = item.getObjPropTypeId();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(PARAM_WORD_ID, wordMap.get(item.getWord()).getId());
				map.put(PARAM_OBJECT_ID, objectId);
				map.put(PARAM_PROPERTY_ID, (null == propertyId)? Integer.valueOf(-1) : propertyId);
				map.put(PARAM_OBJ_PROP_TYPE_ID, (null == objectTypePropertyId)? Integer.valueOf(-1) : objectTypePropertyId);
				map.put(PARAM_FREQUENCY, item.getFrequency());
				map.put(PARAM_SQRT_FREQUENCY, Math.sqrt(item.getFrequency()));
				batch.add(map);
			}

			Map<String, Object>[] array = new HashMap[partialWordObjectList.size()];
			getJdbcTemplate().batchUpdate(
					QUERY_INSERT_WORD_OBJECT,
					batch.toArray(array));

		} catch(Exception e) {
			LOG.error("Error writing word object page: " + e.getLocalizedMessage(), e);
			throw new IndexRuntimeException(e.getLocalizedMessage(), e);
		}
		return isMetadata(operationType)? obtainMetadataWordObjectList(objectId, wordMap) : obtainContentWordObjectList(objectId, wordMap);
	}

	// ================== WRITE WORD POSITIONS =========================================
	/**
	 * Write a page of wordPositions into the definitive index tables.
	 * 
	 * @param repositoryId repository to which the cmis object belongs
	 * @param documentId  the cmis object internal id
	 * @param partialWordPositionList the list of transientDTO read from the temporary index table corresponding to a page of words and not grouped
	 * @param wordObjectMap map containing all of the words of a page related to the WordObject information
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public <T extends IWordObjectExtractor> void writeWordPositionPage(Integer repositoryId, Integer documentId, List<TransientDTO> partialWordPositionList, Map<String, T> wordObjectMap){
		try {
			List<Map<String, ?>> batch = new ArrayList<Map<String, ?>>();
			for (TransientDTO item : partialWordPositionList) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(PARAM_WORD_OBJECT_ID, wordObjectMap.get(item.getWord()).getIdFromTransientItem(item));
				map.put(PARAM_POSITION, item.getPosition());
				map.put(PARAM_STEP, item.getStep());
				map.put(PARAM_PROPERTY_ID, item.getPropertyId());
				batch.add(map);
			}

			Map<String, Object>[] array = new HashMap[partialWordPositionList.size()];
			getJdbcTemplate().batchUpdate(
					QUERY_INSERT_WORD_POSITION,
					batch.toArray(array));

		} catch(Exception e) {
			LOG.error("Error writing word position page: " + e.getLocalizedMessage(), e);
			throw new RuntimeException(e.getLocalizedMessage(), e);
		}
	}

	// ================== DELETE INDEX PART =========================================
	/**
	 * Delete a page of the index related to a cmis object 
	 * 
	 * @param documentId  the cmis object internal id
	 * @param deletePageSize maximum number of records to delete
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deletePermanentIndexPartPage(List<Integer> wordObjectIdList){
		int totalDeleted = 0;
		LOG.debug(">>>>>>>>>>>>>>>>>>>>>............ wordObjectList to DELETE SIZE: {}", wordObjectIdList.size());
		totalDeleted += deleteByWordObjectIdList(wordObjectIdList, QUERY_DELETE_WORD_POSITION_BY_WORD_OBJECT_ID_LIST);
		totalDeleted += deleteByWordObjectIdList(wordObjectIdList, QUERY_DELETE_WORD_OBJECT_BY_WORD_OBJECT_ID_LIST);
		return totalDeleted;
	}

	//PRIVATES
	// ================== WORDS =========================================
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void writeWords(Integer repositoryId, Integer documentId, List<String> wordList){
		LOG.info(">>> writeWords for document id: {}", documentId);

		if( null != wordList && 0 < wordList.size() ){
			try {
				List<WordDTO> batchData = new ArrayList<WordDTO>();
				for (String word : wordList) {
					if( null != word ){
						batchData.add(new WordDTO(null, repositoryId, word));
					}
				}
				SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(batchData.toArray());
				getJdbcTemplate().batchUpdate(
						QUERY_INSERT_WORD,
						batch);

			} catch(Exception e) {
				LOG.error("Error executing: " + e.getLocalizedMessage(), e);
				throw new IndexRuntimeException(e.getLocalizedMessage(), e);
			}
		}
	}




	// ================== WORD OBJECTS =========================================

	private Map<String, WordObjectDTO> obtainContentWordObjectList(final Integer objectId, Map<String, WordDTO> wordMap){
		if( null != wordMap && 0 < wordMap.size() ){
			MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
			namedParameters.addValue(PARAM_WORD_LIST, wordMap.keySet());
			return getJdbcTemplate().query(QUERY_CONTENT_WORD_OBJECTS_BY_DOCUMENT_AND_WORDLIST, namedParameters, new ResultSetExtractor< Map<String, WordObjectDTO>>() {

				@Override
				public Map<String, WordObjectDTO> extractData(ResultSet rs) throws SQLException{

					Map<String, WordObjectDTO> result = new HashMap<String, WordObjectDTO>();

					String word = null;
					while( rs.next() ){
						word = rs.getString(1);
						WordObjectDTO wo = new WordObjectDTO(rs.getInt(WORD_OBJECTS_WORD_OBJECT_ID), rs.getInt(WORD_OBJECTS_WORD_ID), rs.getInt(WORD_OBJECTS_OBJECT_ID), rs.getInt(WORD_OBJECTS_FREQUENCY));
						result.put(word, wo);
					}
					return result;
				}
			});
		}else{
			return  new HashMap<String, WordObjectDTO>();
		}
	}


	private Map<String, PropertyWordObjectMap> obtainMetadataWordObjectList(final Integer documentId, Map<String, WordDTO> wordMap){
		if( null != wordMap && 0 < wordMap.size() ){
			MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, documentId);
			namedParameters.addValue(PARAM_WORD_LIST, wordMap.keySet());
			return getJdbcTemplate().query(QUERY_METADATA_WORD_OBJECTS_BY_DOCUMENT_AND_WORDLIST, namedParameters, new ResultSetExtractor< Map<String, PropertyWordObjectMap> >() {
				@Override
				public Map<String, PropertyWordObjectMap> extractData(ResultSet rs) throws SQLException{
					Map<String, PropertyWordObjectMap> result = new HashMap<String, PropertyWordObjectMap>();
					String word = null;
					Integer propertyId = null;
					while( rs.next() ){
						word = rs.getString(1);
						propertyId = rs.getInt(WORD_OBJECTS_PROPERTY_ID);
						WordObjectDTO wo = new WordObjectDTO(rs.getInt(WORD_OBJECTS_WORD_OBJECT_ID), rs.getInt(WORD_OBJECTS_WORD_ID), rs.getInt(WORD_OBJECTS_OBJECT_ID), propertyId, rs.getInt(WORD_OBJECTS_OBJ_PROP_TYPE_ID), rs.getInt(WORD_OBJECTS_FREQUENCY));
						if( null == result.get(word)){
							result.put(word, new PropertyWordObjectMap());
						}
						result.get(word).put(propertyId, wo);
					}
					return result;
				}
			});
		}else{
			return  new HashMap<String, PropertyWordObjectMap>();
		}
	}


	// ================== DELETE  =========================================


	private int deleteByWordObjectIdList(List<Integer> wordObjectIdList, String query){
		LOG.info(">>> deleteByWordObjectIdList. {}", query);
		int result = 0;
		if( null != wordObjectIdList && 0 < wordObjectIdList.size() ){
			try {
				MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_WORD_OBJECT_ID_LIST, wordObjectIdList);
				result = getJdbcTemplate().update(query,namedParameters);

			} catch(Exception e) {
				LOG.error("Error executing: " + e.getLocalizedMessage(), e);
				throw new IndexRuntimeException(e.getLocalizedMessage(), e);
			}
		}
		return result;
	}

	// ================== OTHER =========================================

	private boolean isMetadata(IndexOperationType operationType){
		return IndexOperationType.METADATA.equals(operationType);
	}
}
