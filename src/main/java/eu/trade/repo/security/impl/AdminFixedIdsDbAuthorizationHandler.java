/**
 * 
 */
package eu.trade.repo.security.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;


/**
 * {@link DbAuthorizationHandler} that also defines a list of fixed admin principal IDs. The property name is <code>adminId</code>, as in the example:
 * <p>
		&lt;authorizationHandler&gt;<br/>
			&lt;name&gt;gaca-ops&lt;/name&gt;<br/>
			&lt;description&gt;desc1&lt;/description&gt;<br/>
			&lt;class&gt;eu.trade.repo.security.impl.DbAuthorizationHandler&lt;/class&gt;<br/>
			&lt;properties&gt;<br/>
				&lt;adminId&gt;&lt;/adminId&gt;<br/>
				...
				&lt;adminId&gt;&lt;/adminId&gt;<br/>
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
 * <p>
 * For the rest of the properties applies the same as in the superclass.
 * 
 * @author porrjai
 */
public class AdminFixedIdsDbAuthorizationHandler extends DbAuthorizationHandler {

	private static final String ADMIN_ID = "adminId";

	private final Set<String> adminIds = new HashSet<>();

	/**
	 * @see eu.trade.repo.security.impl.DbAuthorizationHandler#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		super.init(configuration);
		List<Object> adminIdsList = configuration.getList(ADMIN_ID);
		for (Object object : adminIdsList) {
			adminIds.add(object.toString());
		}
	}

	/**
	 * @see eu.trade.repo.security.impl.DbAuthorizationHandler#isAdmin(java.lang.String)
	 */
	@Override
	public boolean isAdmin(String username) {
		if (adminIds.isEmpty()) {
			return false;
		}
		Set<String> userPrincipalIds = getPrincipalIds(username);
		for (String adminId : adminIds) {
			if (userPrincipalIds.contains(adminId)) {
				return true;
			}
		}
		return false;
	}
}
