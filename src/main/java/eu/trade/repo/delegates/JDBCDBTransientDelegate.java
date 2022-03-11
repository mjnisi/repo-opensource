package eu.trade.repo.delegates;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


/**
 * Class responsible for accessing and modifying the temporary index database table.
 */
public class JDBCDBTransientDelegate implements DBTransientDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(JDBCDBTransientDelegate.class);


	//queries
	private static final String SHUTDOWN_QUERY = "SHUTDOWN COMPACT";

	@Autowired @Qualifier("jdbcTemplateTransient")
	private NamedParameterJdbcTemplate jdbcTemplate;


	public void shutdown(){
		LOG.info("Before H2 shutdown");
		try{
			jdbcTemplate.update(SHUTDOWN_QUERY, new HashMap<String, Object>());
			LOG.info("H2 closed");
		}catch(Exception e){
			LOG.warn("H2 could not be closed", e);
		}
	}

}
