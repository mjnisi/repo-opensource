package eu.trade.repo.security.impl;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.security.AuthorizationHandler;

/**
 *  {@link AuthorizationHandler} implementation based on an authorization database.
 *  <p>
 *  The configuration must follow the following template:<br/>
		&lt;authorizationHandler&gt;<br/>
			&lt;enable&gt;true&lt;/enabled&gt;<br/>
			&lt;name&gt;gaca-ops&lt;/name&gt;<br/>
			&lt;description&gt;desc1&lt;/description&gt;<br/>
			&lt;class&gt;eu.trade.repo.security.impl.DbAuthorizationHandler&lt;/class&gt;<br/>
			&lt;properties&gt;<br/>
				&lt;domain&gt;&lt;/domain&gt;<br/>
				&lt;datasource&gt;<br/>
					&lt;driverClassName&gt;&lt;/driverClassName&gt;<br/>
	        		&lt;url&gt;&lt;/url&gt;<br/>
	        		&lt;username&gt;&lt;/username&gt;<br/>
	        		&lt;password&gt;&lt;/password&gt;<br/>
	        	&lt;/datasource&gt;
				&lt;authoritiesByUsernameQuery&gt;&lt;/authoritiesByUsernameQuery&gt;<br/>
			&lt;/properties&gt;<br/>
		&lt;/authorizationHandler&gt;<br/>
 *  <p>
 *  The datasource properties can be all that accepted to instantiate a {@link org.apache.commons.dbcp.BasicDataSourceFactory}, the given example are the mandatory ones.
 *  <br/>
 *  Also, the <code>authoritiesByUsernameQuery</code> used to obtain the set of principal ids must return a set of strings based on only one not-named String parameter.
 *  Note: The question mark can appear multiple times but the same value will be used for all, i.e. the username.
 *  <br/>
 *  Example: <code>SELECT ROLE FROM ROLES WHERE USER = ?</code>
 * 
 * @author porrjai
 */
public class DbAuthorizationHandler extends AbstractSecurityHandler implements AuthorizationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DbAuthorizationHandler.class);
	private static final String INIT_ERROR = "Data source cannot be created.";
	private static final String QUERY_ERROR = "Principal ids cannot be retrieved due to a database error.";

	private String authoritiesByUsernameQuery;
	private String domain;
	private DataSource dataSource;

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		try {
			Properties properties = new Properties();
			Configuration datasourceConf = configuration.subset("datasource");
			Iterator<String> keyIt = datasourceConf.getKeys();
			while (keyIt.hasNext()) {
				String key = keyIt.next();
				properties.put(key, datasourceConf.getString(key));
			}
			dataSource = BasicDataSourceFactory.createDataSource(properties);
		} catch (Exception e) {
			LOG.error(INIT_ERROR + e.getLocalizedMessage(), e);
			throw new IllegalStateException(INIT_ERROR, e);
		}

		authoritiesByUsernameQuery = configuration.getString("authoritiesByUsernameQuery");
		domain = configuration.getString("domain");
	}

	/**
	 * Will use the <code>authoritiesByUsernameQuery</code> to retrieve the set of principals ids.
	 * 
	 * @see eu.trade.repo.security.AuthorizationHandler#getPrincipalIds(java.lang.String)
	 */
	@Override
	public Set<String> getPrincipalIds(String username) {
		Set<String> principalIds = new HashSet<>();
		try (Connection connection = dataSource.getConnection()) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(authoritiesByUsernameQuery)) {
				ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
				int numParams = parameterMetaData.getParameterCount();
				for (int i = 1; i <= numParams; i++) {
					preparedStatement.setString(i, username);
				}
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					while(resultSet.next()) {
						principalIds.add(resultSet.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			LOG.error(QUERY_ERROR + e.getLocalizedMessage(), e);
			throw new IllegalStateException(QUERY_ERROR, e);
		}
		return principalIds;
	}

	/**
	 * Always false.
	 * 
	 * @see eu.trade.repo.security.AuthorizationHandler#isAdmin(java.lang.String)
	 */
	@Override
	public boolean isAdmin(String username) {
		return false;
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getDomain()
	 */
	@Override
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the dataSource
	 */
	protected DataSource getDataSource() {
		return dataSource;
	}
}
