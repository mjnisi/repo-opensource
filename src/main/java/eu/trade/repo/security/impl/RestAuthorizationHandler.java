package eu.trade.repo.security.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import eu.trade.repo.security.AuthorizationHandler;

/**
 * Rest authorisation handler.
 * 
 * Example:
 * 
 * <pre>
 *  	&lt;authorizationHandler&gt;
 *           &lt;name&gt;ulysse-dev&lt;/name&gt;
 *           &lt;enabled&gt;true&lt;/enabled&gt;
 *           &lt;description&gt;The set of Principal Ids for the user are defined by Ulysse&lt;/description&gt;
 *           &lt;class&gt;eu.trade.repo.security.impl.RestAuthorizationHandler&lt;/class&gt;
 *           &lt;properties&gt;
 *               &lt;domain&gt;ulysse&lt;/domain&gt;
 *               &lt;enpoint&gt;http://serverdev.trade.cec.eu.int:8080/ulyssesvc/get/groups/{username}&lt;/enpoint&gt;
 *               &lt;emptySetOnError&gt;true&lt;/emptySetOnError&gt;
 *               &lt;readTimeout&gt;100&lt;/readTimeout&gt;
 *               &lt;connectTimeout&gt;2000&lt;/connectTimeout&gt;
 *           &lt;/properties&gt;
 *       &lt;/authorizationHandler&gt;
 * </pre>
 * @author martjoe
 */
public class RestAuthorizationHandler extends AbstractSecurityHandler implements AuthorizationHandler {

	protected static final Logger LOG = LoggerFactory.getLogger(RestAuthorizationHandler.class);
	protected static final String ERROR = "Error trying to execute rest call.";

	
	private String domain;
	protected String endpoint;
	protected boolean emptySetOnError;
	protected static RestTemplate restTemplate;

	/**
	 * @see eu.trade.repo.security.Configurable#init(org.apache.commons.configuration.Configuration)
	 */
	@Override
	public void init(Configuration configuration) {
		domain = configuration.getString("domain");
		endpoint = configuration.getString("endpoint");
		emptySetOnError = Boolean.parseBoolean(configuration.getString("emptySetOnError", "true"));
		
		restTemplate = new RestTemplate();
		SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
		requestFactory.setReadTimeout(Integer.valueOf(configuration.getString("readTimeout")));
		requestFactory.setConnectTimeout(Integer.valueOf(configuration.getString("connectTimeout")));
	}

	/**
	 * @see eu.trade.repo.security.AuthorizationHandler#getPrincipalIds(java.lang.String)
	 */
	@Override
	public Set<String> getPrincipalIds(String username) {
    	
		Map<String, Object> variables = new HashMap<String, Object>(1);
        variables.put("username", username);

        try {
        	String[] response = restTemplate.getForObject(endpoint, String[].class, variables);
            return new HashSet<String>(Arrays.asList(response));
        } catch (Exception e) {
        	LOG.error(ERROR + e.getLocalizedMessage(), e);
        	if(emptySetOnError) {
        		return Collections.emptySet();
        	} else {
        		throw new IllegalStateException(ERROR, e);
        	}
        }
        
	}

	/**
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
}
