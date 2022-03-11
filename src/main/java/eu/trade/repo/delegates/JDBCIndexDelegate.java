package eu.trade.repo.delegates;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import eu.trade.repo.index.model.IWordObjectExtractor;
import eu.trade.repo.index.model.IndexObjectDTO;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.index.model.WordDTO;
import eu.trade.repo.model.IndexingState;


/**
 * Class responsible for accessing and modifying the permanent index database tables.
 */
public class JDBCIndexDelegate extends AbstractJDBCIndexPermanentDelegate implements IndexDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(JDBCIndexDelegate.class);

	//resultset indexes
	private static final int RS_INDEXES_IN_ERROR_REPOSITORY_ID = 1;
	private static final int RS_INDEXES_IN_ERROR_DOCUMENT_ID = 2;
	private static final int RS_INDEXES_IN_ERROR_FILENAME = 3;
	private static final int RS_INDEXES_IN_ERROR_FILESIZE = 4;

	private static final int RS_WORDS_WORD = 2;

	//queries LISTS
	private static final String QUERY_WORDS_BY_REPOSITORY_AND_WORDLIST = "select w.id, w.word from index_word w where w.repository_id = :repositoryId and w.word in (:wordList) order by w.word";
	private static final String QUERY_OBJECT_TYPE_PROPERTY_ID_LIST_FOR_OBJECT = "select distinct object_type_property_id from index_word_object where object_id = :objectId";

	private static final String WRAP_PAGINATED_INIT_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID = "select a.id from (select q.*, ROWNUM rn from ( ";
	private static final String WRAP_PAGINATED_END_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID = " ) q where rownum <= :end) a where rn > :start";

	private static final String QUERY_REPOSITORY_BY_OBJECT_ID_LIST = "select o.id, ot.repository_id from object o join object_type ot on o.object_type_id = ot.id where o.id in( :objectIdList )";

	//used for deleting CONTENT word objects
	private static final String QUERY_CONTENT_WORD_OBJECT_PAGE_BY_DOCUMENT_ID = 
			WRAP_PAGINATED_INIT_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID +	
			"select wo.id id from index_word_object wo where wo.object_id = :objectId and wo.property_id = -1 order by wo.id" + 
			WRAP_PAGINATED_END_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID;

	// METADATA
	//used for deleting METADATA word objects
	private static final String QUERY_METADATA_WORD_OBJECT_PAGE_BY_DOCUMENT_ID = 
			WRAP_PAGINATED_INIT_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID +	
			"select wo.id id from index_word_object wo where wo.object_id = :objectId and wo.property_id > -1 order by wo.id" + 
			WRAP_PAGINATED_END_QUERY_WORD_OBJECT_PAGE_BY_DOCUMENT_ID;


	//queries JOBS
	private static final String QUERY_ORPHAN_INDEXES_LIST = "select distinct(wo.object_id) from index_word_object wo left join object obj on wo.object_id=obj.id where obj.id is null";


	private static final String WRAP_PAGINATED_INIT_QUERY_CONTENT_INDEXES_IN_ERROR = "select repository_id, id, fileName, fileSize, ROWNUM rnum from ( ";
	private static final String WRAP_PAGINATED_INIT_QUERY_METADATA_INDEXES_IN_ERROR = "select repository_id, id, ROWNUM rnum from ( ";
	private static final String WRAP_PAGINATED_END_QUERY_INDEXES_IN_ERROR = " ) where rownum <=  :resultLimit";

	private static final String QUERY_CONTENT_INDEXES_IN_ERROR = 
			WRAP_PAGINATED_INIT_QUERY_CONTENT_INDEXES_IN_ERROR
			+ "select ot.repository_id, obj.id, pName.value fileName, pFSize.numeric_value fileSize"
			+ " from object obj"
			+ " join property pFSize on pFSize.object_id = obj.id" 
			+ " join object_type_property otpFSize on otpFSize.id = pFSize.object_type_property_id and otpFSize.local_name='" + PropertyIds.CONTENT_STREAM_LENGTH + "' "
			+ " join property pName on pName.object_id = obj.id"
			+ " join object_type_property otpName on otpName.id = pName.object_type_property_id and otpName.local_name='" + PropertyIds.CONTENT_STREAM_FILE_NAME + "' "
			+ " join object_type ot on ot.id = obj.object_type_id"
			+ " where obj.index_state_content in( "
			+ IndexingState.ERROR.getState() + ", "
			+ IndexingState.NONE.getState() + ")"
			+ " and obj.index_tries_content <= :maxIndexTries "
			+ " :FILE_SIZE_CLAUSE "
			+ " order by obj.index_tries_content, obj.id asc"
			+WRAP_PAGINATED_END_QUERY_INDEXES_IN_ERROR;

	private static final String QUERY_METADATA_INDEXES_IN_ERROR = 
			WRAP_PAGINATED_INIT_QUERY_METADATA_INDEXES_IN_ERROR
			+ "select ot.repository_id, obj.id "
			+ " from object obj"
			+ " join object_type ot on ot.id = obj.object_type_id"
			+ " where obj.index_state_metadata in( "
			+ IndexingState.ERROR.getState() + ", "
			+ IndexingState.NONE.getState() + ")"
			+ " and obj.index_tries_metadata <= :maxIndexTries "
			+ " order by obj.index_tries_content, obj.id asc"
			+WRAP_PAGINATED_END_QUERY_INDEXES_IN_ERROR;


	private static final String QUERY_INDEXES_IN_ERROR_FILESIZE_MAXLIMIT_CLAUSE = " and pFSize.numeric_value < :maxLimit ";
	private static final String QUERY_INDEXES_IN_ERROR_FILESIZE_MINLIMIT_CLAUSE = " and pFSize.numeric_value >= :minLimit ";


	@Autowired
	private IndexDelegateHelper indexDelegateHelper;

	// ================== WRITE WORDS =========================================
	/**
	 * Write a page of words into the definitive index tables.
	 * 
	 * Only the words that didn't exist in advance for that repository are inserted
	 * 
	 * @param repositoryId repository to which the cmis object belongs
	 * @param documentId  the cmis object internal id
	 * @param wordList the list of words corresponding to a page (page size according to configuration)
	 * 
	 * @return a map having the word as key and all the related database information in a WordDTO object as value. 
	 */
	public synchronized Map<String, WordDTO> writeWordPage(Integer repositoryId, Integer objectId, List<String> wordList){
		LOG.info(">>> writeWordPage for object id: {}", objectId);

		List<String> wordTransientList = new ArrayList<String>();
		wordTransientList.addAll(wordList);

		Map<String, WordDTO> dictionaryMap = null;
		try {
			dictionaryMap = obtainWordsByRepositoryIdAndList(repositoryId, wordTransientList);

			List<String> dictWordList = new ArrayList<String>();
			for (WordDTO word : dictionaryMap.values()) {
				dictWordList.add(word.getWord());
			}
			wordTransientList.removeAll(dictWordList);
			//now wordList has only new words

			indexDelegateHelper.writeWords(repositoryId, objectId, wordTransientList);

			Map<String, WordDTO> newWordsMap = obtainWordsByRepositoryIdAndList(repositoryId, wordTransientList);
			dictionaryMap.putAll(newWordsMap);

		}catch(Exception e){
			LOG.error("Error writing word page: " + e.getLocalizedMessage(), e);
			throw e;
		}
		//now dictionaryList has all the word entities associated with wordList
		return dictionaryMap;
	}

	// ================== WRITE WORD OBJECTS =========================================
	@Override
	public Map<String, ? extends IWordObjectExtractor> writeWordObjectPage(
			Integer repositoryId, Integer objectId,
			List<TransientDTO> partialWordObjectList,
			Map<String, WordDTO> wordMap, IndexOperationType operationType) {

		return indexDelegateHelper.writeWordObjectPage(repositoryId, objectId, partialWordObjectList, wordMap, operationType);
	}

	// ================== WRITE POSITIONS =========================================
	@Override
	public <T extends IWordObjectExtractor> void writeWordPositionPage(
			Integer repositoryId, Integer objectId,
			List<TransientDTO> partialWordPositionList,
			Map<String, T> wordObjectMap) {

		indexDelegateHelper.writeWordPositionPage(repositoryId, objectId, partialWordPositionList, wordObjectMap);
	}


	// ================== DELETE INDEX PART =========================================

	public List<Integer> obtainWordObjectPageToDelete(Integer objectId, int deletePageSize, IndexOperationType operationType){
		String query = isMetadata(operationType)? QUERY_METADATA_WORD_OBJECT_PAGE_BY_DOCUMENT_ID : QUERY_CONTENT_WORD_OBJECT_PAGE_BY_DOCUMENT_ID;
		return obtainWordObjectPageToDeleteInternal(objectId, 0, deletePageSize, query);
	}

	@Override
	public int deletePermanentIndexPartPage(List<Integer> wordObjectIdList) {
		return indexDelegateHelper.deletePermanentIndexPartPage(wordObjectIdList);
	}

	// ================== BACKGROUND JOBS =========================================

	/**
	 * Query the index to discover orphan indexes
	 */
	public List<Integer> obtainOrphanIndexes() {
		return getJdbcTemplate().queryForList(QUERY_ORPHAN_INDEXES_LIST, new MapSqlParameterSource(), Integer.class);
	}


	/**
	 * Query the index to discover cmis objects which are documents and have related stream that are not already indexed or
	 * they are in the indexing state "error" and have not achieve the maxAttempts number of tries.
	 */
	public List<IndexObjectDTO> obtainContentIndexesInErrorOrUnfinishedState(int maxAttempts, int resultLimit, long minFileSize, long maxFileSize) {

		StringBuilder fileSizeClauseStb = new StringBuilder();
		if( -1 < minFileSize ){
			fileSizeClauseStb.append(QUERY_INDEXES_IN_ERROR_FILESIZE_MINLIMIT_CLAUSE.replaceAll(":minLimit", String.valueOf(minFileSize)));
		}
		if( -1 < maxFileSize ){
			fileSizeClauseStb.append(QUERY_INDEXES_IN_ERROR_FILESIZE_MAXLIMIT_CLAUSE.replaceAll(":maxLimit", String.valueOf(maxFileSize)));
		}


		String sqlQuery = QUERY_CONTENT_INDEXES_IN_ERROR.replaceAll(":FILE_SIZE_CLAUSE", fileSizeClauseStb.toString());
		sqlQuery = sqlQuery.replaceAll(":maxIndexTries", String.valueOf(maxAttempts));
		sqlQuery = sqlQuery.replaceAll(":resultLimit", String.valueOf(resultLimit));


		return getJdbcTemplate().query(sqlQuery, new MapSqlParameterSource(), new ResultSetExtractor< List<IndexObjectDTO>>() {
			@Override
			public List<IndexObjectDTO> extractData(ResultSet rs) throws SQLException{
				List<IndexObjectDTO> result = new ArrayList<IndexObjectDTO>();
				while( rs.next() ){
					IndexObjectDTO dto = new IndexObjectDTO(rs.getInt(RS_INDEXES_IN_ERROR_REPOSITORY_ID), rs.getInt(RS_INDEXES_IN_ERROR_DOCUMENT_ID));
					dto.setFileName(rs.getString(RS_INDEXES_IN_ERROR_FILENAME));
					dto.setFileSize(BigInteger.valueOf(rs.getLong(RS_INDEXES_IN_ERROR_FILESIZE)));
					result.add(dto);
				}
				return result;
			}
		});
	}


	/**
	 * Query the index to discover cmis objects which are documents and have related stream that are not already indexed or
	 * they are in the indexing state "error" and have not achieve the maxAttempts number of tries.
	 */
	public List<IndexObjectDTO> obtainMetadataIndexesInErrorOrUnfinishedState(int maxAttempts, int resultLimit) {

		String sqlQuery = QUERY_METADATA_INDEXES_IN_ERROR.replaceAll(":maxIndexTries", String.valueOf(maxAttempts));
		sqlQuery = sqlQuery.replaceAll(":resultLimit", String.valueOf(resultLimit));

		return getJdbcTemplate().query(sqlQuery, new MapSqlParameterSource(), new ResultSetExtractor< List<IndexObjectDTO>>() {
			@Override
			public List<IndexObjectDTO> extractData(ResultSet rs) throws SQLException{
				List<IndexObjectDTO> result = new ArrayList<IndexObjectDTO>();
				while( rs.next() ){
					result.add(new IndexObjectDTO(rs.getInt(RS_INDEXES_IN_ERROR_REPOSITORY_ID), rs.getInt(RS_INDEXES_IN_ERROR_DOCUMENT_ID)));
				}
				return result;
			}
		});
	}


	public List<Integer> obtainObjectPropertyTypeIdListFromObjectWordObjects(Integer objectId){
		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
		return getJdbcTemplate().queryForList(QUERY_OBJECT_TYPE_PROPERTY_ID_LIST_FOR_OBJECT, namedParameters, Integer.class);
	}

	//PRIVATES

	private Map<String, WordDTO> obtainWordsByRepositoryIdAndList(final Integer repositoryId, List<String> wordList){

		if( null != wordList && 0 < wordList.size() ){
			MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_REPOSITORY_ID, repositoryId);
			namedParameters.addValue(PARAM_WORD_LIST, wordList);
			return getJdbcTemplate().query(QUERY_WORDS_BY_REPOSITORY_AND_WORDLIST, namedParameters, new ResultSetExtractor< Map<String, WordDTO>>() {
				@Override
				public Map<String, WordDTO> extractData(ResultSet rs) throws SQLException{

					Map<String, WordDTO> result = new HashMap<String, WordDTO>();
					String word = null;
					while( rs.next() ){
						word = rs.getString(RS_WORDS_WORD);
						WordDTO dto = new WordDTO(rs.getInt(1), repositoryId, word);
						result.put(word, dto);
					}
					return result;
				}
			});

		}else{
			return  new HashMap<String, WordDTO>();
		}
	}


	// ================== DELETE  =========================================
	private List<Integer> obtainWordObjectPageToDeleteInternal(Integer documentId, int start, int pageSize, String query){	
		LOG.info(">>> obtainWordObjectPageByDocumentId: documentId = {}", documentId);
		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, documentId);
		namedParameters.addValue("start", start);
		namedParameters.addValue("end", start + pageSize);

		return getJdbcTemplate().queryForList(query, namedParameters, Integer.class);
	}

	// ================== OTHER =========================================
	private boolean isMetadata(IndexOperationType operationType){
		return IndexOperationType.METADATA.equals(operationType);
	}

	@Override
	public Map<Integer, Integer> obtainRepositoryByObjectList(Set<Integer> objectIdList) {

		if( null != objectIdList && 0 < objectIdList.size() ){
			MapSqlParameterSource namedParameters = new MapSqlParameterSource();
			namedParameters.addValue(PARAM_OBJECT_ID_LIST, objectIdList);
			return getJdbcTemplate().query(QUERY_REPOSITORY_BY_OBJECT_ID_LIST, namedParameters, new ResultSetExtractor< Map<Integer, Integer>>() {
				@Override
				public Map<Integer, Integer> extractData(ResultSet rs) throws SQLException{
					Map<Integer, Integer> result = new HashMap<Integer, Integer>();
					while( rs.next() ){
						result.put(rs.getInt(1), rs.getInt(2));
					}
					return result;
				}
			});

		}else{
			return  new HashMap<Integer, Integer>();
		}
	}

}
