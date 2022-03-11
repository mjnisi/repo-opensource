package eu.trade.repo.security;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for authentication and authorization handlers based on the configuration.
 * <p>
 * &lt;security&gt;<br/>
 * 	&lt;authenticationHandler&gt;<br/>
 * 		&lt;name&gt;test1&lt;/name&gt;<br/>
 * 		&lt;description&gt;desc1&lt;/description&gt;<br/>
 * 		&lt;class&gt;some.class.name.implementation&lt;/class&gt;<br/>
 * 		&lt;properties&gt;<br/>
 * 			&lt;prop1&gt;value1&lt;/prop1&gt;<br/>
 * 			...<br/>
 * 			&lt;propN&gt;valueN&lt;/propN&gt;<br/>
 * 		&lt;/properties&gt;<br/>
 * 	&lt;/authenticationHandler&gt;<br/>
 * 	&lt;authorizationHandler&gt;<br/>
 * 		&lt;name&gt;test2&lt;/name&gt;<br/>
 * 		&lt;description&gt;desc2&lt;/description&gt;<br/>
 * 		&lt;class&gt;some.class.name.implementation&lt;/class&gt;<br/>
 * 		&lt;properties&gt;<br/>
 * 			&lt;prop1&gt;value1&lt;/prop1&gt;<br/>
 * 			...
 * 			&lt;propN&gt;valueN&lt;/propN&gt;<br/>
 * 		&lt;/properties&gt;<br/>
 * 	&lt;/handler&gt;<br/>
 * &lt;/security&gt;<br/>
 * 
 * 
 * @author porrjai
 *
 */
public class SecurityHandlerFactory implements SecurityHandlerDefinitions {

	private static final Logger LOG = LoggerFactory.getLogger(CallContextHolderImpl.class);
	private static final String AUTHENTICATION_KEY = "security.authenticationHandler";
	private static final String AUTHORIZATION_KEY = "security.authorizationHandler";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String ENABLED = "enabled";
	private static final String CLASS = "class";
	private static final String PROPERTIES = "properties";
	private static final String ERROR = "Unable to instantiate the handler.";

	private final Map<String, AuthenticationHandler> authenticationHandlers = new HashMap<>();
	private final Map<String, AuthorizationHandler> authorizationHandlers = new HashMap<>();

	@Autowired
	private HierarchicalConfiguration combinedConfig;

	SecurityHandlerFactory() {
		this.combinedConfig = null;
	}

	void init() {
		init(AUTHENTICATION_KEY, authenticationHandlers);
		init(AUTHORIZATION_KEY, authorizationHandlers);
	}

	AuthenticationHandler getAuthenticationHandler(String name) {
		return authenticationHandlers.get(name);
	}

	AuthorizationHandler getAuthorizationHandler(String name) {
		return authorizationHandlers.get(name);
	}

	/**
	 * @param combinedConfig the combinedConfig to set
	 */
	void setCombinedConfig(HierarchicalConfiguration combinedConfig) {
		this.combinedConfig = combinedConfig;
	}

	private <T extends SecurityHandler> void init(String key, Map<String, T> handlers) {
		List<HierarchicalConfiguration> hierarchicalConfigurations = combinedConfig.configurationsAt(key);
		if (hierarchicalConfigurations != null) {
			for (HierarchicalConfiguration hierarchicalConfiguration : hierarchicalConfigurations) {
				boolean enabled = hierarchicalConfiguration.getBoolean(ENABLED, false);
				if (enabled) {
					String name = hierarchicalConfiguration.getString(NAME);
					T handler = this.instance(name, hierarchicalConfiguration);
					try {
						handlers.put(name, handler);
					} catch (ClassCastException e) {
						LOG.error(ERROR, e);
						throw new IllegalArgumentException(ERROR, e);
					}
				}
			}
		}
	}

	private <T extends SecurityHandler> T instance(String name, HierarchicalConfiguration hierarchicalConfiguration) {
		T handler;
		String className = hierarchicalConfiguration.getString(CLASS);
		try {
			Class<T> clazz = (Class<T>) Class.forName(className);
			handler = clazz.newInstance();
			Configuration configuration = hierarchicalConfiguration.configurationAt(PROPERTIES);
			handler.init(configuration);
			handler.setName(name);
			handler.setDescription(hierarchicalConfiguration.getString(DESCRIPTION));
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LOG.error(ERROR, e);
			throw new IllegalStateException(ERROR, e);
		}
		return handler;
	}

	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinitions#getAuthenticationHandlers()
	 */
	@Override
	public Collection<? extends SecurityHandlerDefinition> getAuthenticationHandlers() {
		return authenticationHandlers.values();
	}

	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinitions#getAuthorisationHandlers()
	 */
	@Override
	public Collection<? extends SecurityHandlerDefinition> getAuthorisationHandlers() {
		return authorizationHandlers.values();
	}
}