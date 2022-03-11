package eu.trade.repo.delegates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;




public abstract class AbstractJDBCIndexPermanentDelegate {

	//params
	protected static final String PARAM_WORD_OBJECT_ID_LIST = "wordObjectIdList";
	protected static final String PARAM_REPOSITORY_ID = "repositoryId";
	protected static final String PARAM_OBJECT_ID = "objectId";
	protected static final String PARAM_PROPERTY_ID = "propertyId";
	protected static final String PARAM_OBJ_PROP_TYPE_ID = "objPropTypeId";

	protected static final String PARAM_WORD_ID = "wordId";
	protected static final String PARAM_WORD_OBJECT_ID = "wordObjectId";
	protected static final String PARAM_FREQUENCY = "frequency";
	protected static final String PARAM_SQRT_FREQUENCY = "sqrtFrequency";
	protected static final String PARAM_POSITION = "position";
	protected static final String PARAM_STEP = "step";
	protected static final String PARAM_WORD_LIST = "wordList";

	protected static final String PARAM_OBJ_PROP_TYPE_ID_LIST = "objPropTypeIdList";
	protected static final String PARAM_WORD_ID_LIST = "wordIdList";
	protected static final String PARAM_NUM = "num";
	
	protected static final String PARAM_OBJECT_ID_LIST = "objectIdList";

	@Autowired @Qualifier("jdbcTemplateIndex")
	private NamedParameterJdbcTemplate jdbcTemplate;

	protected NamedParameterJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


}
