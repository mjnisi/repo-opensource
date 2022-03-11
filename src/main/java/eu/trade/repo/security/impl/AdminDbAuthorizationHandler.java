/**
 * 
 */
package eu.trade.repo.security.impl;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DbAuthorizationHandler} that also defines a query to find out if the user is an admin. The property name is <code>adminByUsernameQuery</code>, as in the example:
 * <p>
		&lt;authorizationHandler&gt;<br/>
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
				&lt;adminByUsernameQuery&gt;&lt;/adminByUsernameQuery&gt;<br/>
			&lt;/properties&gt;<br/>
		&lt;/authorizationHandler&gt;<br/>
 * <p>
 *  Also, the <code>adminByUsernameQuery</code> used must return a non empty result set if the user is admin or an empty result set if it is not. In a similar way the query must be based on only one not-named String parameter.
 *  Note: The question mark can appear multiple times but the same value will be used for all, i.e. the username..
 *  <br/>
 *  Example: <code>SELECT ROLE FROM ROLES WHERE USER = ? AND ROLE LIKE 'ADMIN%'</code>
 * <p>
 * For the rest of the properties applies the same as in the superclass.
 * 
 * @author porrjai
 */
public class AdminDbAuthorizationHandler extends DbAuthorizationHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DbAuthorizationHandler.class);
	private static final String QUERY_ERROR = "Admin user cannot be resolved due to a database error.";

	private String adminByUsernameQuery;

	/**
	 * @see eu.trade.repo.security.impl.DbAuthorizationHandler#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		super.init(configuration);
		adminByUsernameQuery = configuration.getString("adminByUsernameQuery");
	}

	/**
	 * @see eu.trade.repo.security.impl.DbAuthorizationHandler#isAdmin(java.lang.String)
	 */
	@Override
	public boolean isAdmin(String username) {
		try (Connection connection = getDataSource().getConnection()) {
			try (PreparedStatement preparedStatement = connection.prepareStatement(adminByUsernameQuery)) {
				ParameterMetaData parameterMetaData = preparedStatement.getParameterMetaData();
				int numParams = parameterMetaData.getParameterCount();
				for (int i = 1; i <= numParams; i++) {
					preparedStatement.setString(i, username);
				}
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					return resultSet.isBeforeFirst();
				}
			}
		} catch (SQLException e) {
			LOG.error(QUERY_ERROR + e.getLocalizedMessage(), e);
			throw new IllegalStateException(QUERY_ERROR, e);
		}
	}
}
