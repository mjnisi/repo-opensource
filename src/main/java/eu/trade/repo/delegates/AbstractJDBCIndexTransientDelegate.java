package eu.trade.repo.delegates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.trade.repo.index.model.TransientDTO;


/**
 * Class responsible for accessing and modifying the temporary index database table.
 */
public abstract class AbstractJDBCIndexTransientDelegate implements IndexTransientDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractJDBCIndexTransientDelegate.class);


	//params
	public static final String PARAM_OBJECT_ID = "objectId";
	public static final String PARAM_START = "start";
	public static final String PARAM_END = "end";
	public static final String PARAM_WORD_LIST = "wordList";

	//resultset indexes
	private static final int RS_WORD_OBJECTS_WORD = 1;
	private static final int RS_WORD_OBJECTS_FREQUENCY = 2;
	private static final int RS_WORD_OBJECTS_PROPERTYID = 3;
	private static final int RS_WORD_OBJECTS_OBJPROPTYPEID = 4;

	private static final int RS_WORD_POSITIONS_WORD = 1;
	private static final int RS_WORD_POSITIONS_POSITION = 2;
	private static final int RS_WORD_POSITIONS_STEP = 3;
	private static final int RS_WORD_POSITIONS_OBJECTID = 4;
	private static final int RS_WORD_POSITIONS_PROPERTYID = 5;
	private static final int RS_WORD_POSITIONS_OBJPROPTYPEID = 6;

	//queries
	public static final String WRAP_PAGINATED_INIT_TRANSIENT_WORDS_BY_OBJECT_ID = "select a.word from ( select q.word, ROWNUM rn from ( ";
	public static final String WRAP_PAGINATED_END_TRANSIENT_WORDS_BY_OBJECT_ID = " ) q where rownum <= :end) a where rn > :start";
	
	public static final String WRAP_PAGINATED_INIT_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST = "select * from (select q.*, ROWNUM rn from ( ";
	public static final String WRAP_PAGINATED_END_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST = " ) q where rownum <= :end) where rn > :start";
	

	@Autowired @Qualifier("jdbcTxManagerTransient")
	private DataSourceTransactionManager transactionManager;

	@Autowired @Qualifier("jdbcTemplateTransient")
	private NamedParameterJdbcTemplate jdbcTemplate;


	/**
	 * @return Query to retrieve a word page from transient index.
	 * Query parameters needed: PARAM_OBJECT_ID, PARAM_START, PARAM_END
	 * Query has to be like 'select word ...'
	 * 
	 */
	protected abstract String getSelectWordQuery();
	/**
	 * @return Query to retrieve  word objects  from transient index related to the list of words passed as parameter.
	 * Query parameters needed: PARAM_OBJECT_ID, PARAM_WORD_LIST
	 * Query has to be like 'select word, frequency, propertyId ...'
	 * 
	 */
	protected abstract String getSelectWordObjectQuery();
	/**
	 * @return Query to retrieve a word position page from transient index.
	 * Query parameters needed: PARAM_OBJECT_ID, PARAM_WORD_LIST, PARAM_START, PARAM_END
	 * Query has to be like 'select word, position, step, objectId, propertyId ...'
	 * 
	 */
	protected abstract String getSelectWordPositionQuery();
	
	
	protected abstract String getSelectObjectTypePropertyIdListQuery();
	
	/**
	 * @return Query to retrieve a word page from transient index.
	 * Query parameters needed: PARAM_OBJECT_ID, PARAM_WORD_LIST, PARAM_START, PARAM_END
	 * Query has to be like 'select word, position, step, objectId, propertyId ...'
	 * 
	 */
	protected abstract String getDeleteTransientQuery();
	

	public List<String> obtainWordPageByObjectId(Integer objectId, int start, int pageSize){
		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
		namedParameters.addValue(PARAM_START, start);
		namedParameters.addValue(PARAM_END, start + pageSize);

		return jdbcTemplate.query(getSelectWordQuery(), namedParameters, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString(1);
			}

		});
	}

	public List<TransientDTO> obtainWordObjectPageByObjectId(Integer objectId, List<String> wordList){

		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
		namedParameters.addValue(PARAM_WORD_LIST, wordList);

		return jdbcTemplate.query(getSelectWordObjectQuery(), namedParameters, new RowMapper<TransientDTO>() {
			@Override
			public TransientDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new TransientDTO(rs.getString(RS_WORD_OBJECTS_WORD), 0, rs.getInt(RS_WORD_OBJECTS_FREQUENCY), 0, 0, rs.getInt(RS_WORD_OBJECTS_PROPERTYID), rs.getInt(RS_WORD_OBJECTS_OBJPROPTYPEID));
			}

		});
	}

	public List<TransientDTO> obtainWordPositionPageByObjectId(Integer objectId, List<String> wordList, int start, int pageSize){

		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
		namedParameters.addValue(PARAM_WORD_LIST, wordList);
		namedParameters.addValue(PARAM_START, start);
		namedParameters.addValue(PARAM_END, start + pageSize);

		return jdbcTemplate.query(getSelectWordPositionQuery(), namedParameters, new RowMapper<TransientDTO>() {
			@Override
			public TransientDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new TransientDTO(rs.getString(RS_WORD_POSITIONS_WORD), rs.getInt(RS_WORD_POSITIONS_POSITION), 0, rs.getInt(RS_WORD_POSITIONS_STEP), rs.getInt(RS_WORD_POSITIONS_OBJECTID), rs.getInt(RS_WORD_POSITIONS_PROPERTYID), rs.getInt(RS_WORD_POSITIONS_OBJPROPTYPEID));
			}

		});
	}
	
	public List<Integer> obtainObjectTypePropertyIdListFromTransientByObjectId(Integer objectId){

		MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, objectId);
		
		return jdbcTemplate.query(getSelectObjectTypePropertyIdListQuery(), namedParameters, new RowMapper<Integer>() {
			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getInt(1);
			}

		});
	}
	
	public int deleteTransientContentIndexByObjectId(Integer objectId, int deletePageSize){
		LOG.info(">>> deleteTransientContentIndexByObjectId: documentId = {} by pages (page size = {})", objectId, deletePageSize);
		int start = 0;
		int totalDeleted = 0;

		boolean done = false;
		while( !done ){
			int partialDelete = deleteTransientByDocumentId(objectId, start, deletePageSize);
			totalDeleted += partialDelete;

			done = (partialDelete < deletePageSize -1);
			start += deletePageSize;
		}

		return totalDeleted;
	}

	protected abstract Analyzer createFullTextAnalyzer();



	private int deleteTransientByDocumentId(Integer documentId, int start, int pageSize){
		LOG.debug(">>> deleteTransientByDocumentId one page starting at {}: documentId = {}", start, documentId);
		int result = 0;
		TransactionStatus txStatus = null;
		try {
			txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
			LOG.debug("Is new TRANSACTION: {}", txStatus.isNewTransaction());
			MapSqlParameterSource namedParameters = new MapSqlParameterSource(PARAM_OBJECT_ID, documentId);
			namedParameters.addValue(PARAM_START, start);
			namedParameters.addValue(PARAM_END, start + pageSize);
			result = jdbcTemplate.update(getDeleteTransientQuery(), namedParameters);
			commit(txStatus);
		} catch(Exception e) {
			LOG.error("Error executing: " + e.getLocalizedMessage(), e);
			rollback(txStatus);
			throw e;
		}
		return result;
	}


	/**
	 * Write a page into definitive index tables
	 * @param callbackData
	 * @param readList
	 * @return
	 */
	protected int[] writeTransientSegment(String query, Integer objectId, List<TransientDTO> readList){
		LOG.debug(">>> writeTransientSegment for object id: {}", objectId);
		int[] result = null;
		TransactionStatus txStatus = null;

		try {
			txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
			SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(readList.toArray());
			result = jdbcTemplate.batchUpdate(
					query,
					batch);

			commit(txStatus);

		}catch(Exception e){
			LOG.error("Error executing: " + e.getLocalizedMessage(), e);
			rollback(txStatus);
			throw e;
		}
		return result;
	}

	private void commit(TransactionStatus txStatus) {
		if (txStatus != null) {
			try{
				transactionManager.commit(txStatus);
			}catch(TransactionException te){
				LOG.error("Error commiting: " + te.getLocalizedMessage(), te);
				throw te;
			}
		}
	}

	private void rollback(TransactionStatus txStatus) {
		if (txStatus != null) {
			try{
				transactionManager.rollback(txStatus);
			}catch(TransactionException te){
				LOG.error("Error rolling back: " + te.getLocalizedMessage(), te);
				throw te;
			}
		}
	}

}
