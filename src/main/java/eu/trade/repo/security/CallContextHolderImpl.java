package eu.trade.repo.security;

import static eu.trade.repo.util.Constants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.HandlerType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.SecurityHandler;
import eu.trade.repo.model.SecurityType;
import eu.trade.repo.security.session.CallContextData;
import eu.trade.repo.security.session.SessionInfo;
import eu.trade.repo.security.session.UserKey;
import eu.trade.repo.security.session.UserSessionKey;
import eu.trade.repo.security.session.UserSessions;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.util.Cleanable;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.KeySynchronizer;
import eu.trade.repo.web.UserIPFilter;

/**
 * Holder for the call context.
 * <p>
 * As the entry point for the call, it also performs the user authentication and
 * retrieves the set of the users principal ids.
 * 
 * @see SecurityHandlerFactory
 * @author porrjai
 */
public class CallContextHolderImpl implements CallContextHolder, Cleanable, SessionInfo {

	private static final Logger LOG = LoggerFactory.getLogger(CallContextHolderImpl.class);
	private static final String ILLEGAL_STATE = "The call context has not been set for this thread.";
	private static final String ILLEGAL_LOGIN = "In order to log in the system the repository id must be set in the call context.";
	private static final String AUTHENTICATION_FAILED = "User authentication failed.";
	private static final String SECURITY_CACHE = "security.cacheEnabled";
	private static final String USER_SESSION_LIMIT = "security.userSessionLimit";
	private static final int DEFAULT_USER_SESSION_LIMIT = 4;
	private static final String USER_CACHE = "security-user";

	private final ThreadLocal<CallContext> localCallContext = new ThreadLocal<CallContext>();
	private final ThreadLocal<CallContextData> localCallContextData = new ThreadLocal<CallContextData>();
	private boolean cacheEnabled;
	private int userSessionLimit;

	@Autowired
	private SecurityHandlerFactory securityHandlerFactory;

	@Autowired
	private Configuration combinedConfig;

	@Autowired(required = false)
	private Cache securityCache;

	@Autowired
	private RepositorySelector repositorySelector;

	public void init() {
		cacheEnabled = combinedConfig.getBoolean(SECURITY_CACHE, Boolean.FALSE);
		if (cacheEnabled && securityCache == null) {
			LOG.warn("Security cache is enabled but no cache with the name {} has been defined. So security cache is now disabled.", USER_CACHE);
			cacheEnabled = false;
		} else {
			userSessionLimit = combinedConfig.getInt(USER_SESSION_LIMIT, DEFAULT_USER_SESSION_LIMIT);
		}
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#initContext(org.apache.chemistry.opencmis.commons.server.CallContext)
	 */
	@Override
	public void initContext(CallContext callContext) {
		localCallContext.set(callContext);
		localCallContextData.remove();
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#login()
	 */
	@Override
	public void login() {
		CallContext callContext = localCallContext.get();
		if (callContext == null) {
			throw new IllegalStateException(ILLEGAL_STATE);
		}
		String repositoryId = callContext.getRepositoryId();
		if (repositoryId == null) {
			throw new IllegalStateException(ILLEGAL_LOGIN);
		}
		UserKey userKey = getUserKey(callContext);
		CallContextData callContextData = getContextData(callContext, userKey);
		localCallContextData.set(callContextData);
	}

	/**
	 * 
	 * @param username
	 *            {@link String} username. Mandatory not null.
	 * @param password
	 *            {@link String}
	 * @return {@link UserKey}
	 */
	private UserKey getUserKey(CallContext callContext) {
		String username = callContext.getUsername();
		if (username == null) {
			throw new CmisUnauthorizedException(AUTHENTICATION_FAILED, SC_UNAUTHORIZED, AUTHENTICATION_FAILED);
		}
		String remoteIP = UserIPFilter.getCurrentIP();
		// from username get handlers names
		String[] splitUsername = username.split(PROTOCOL_SEP);
		switch (splitUsername.length) {
			case 1 :
				return new UserKey(callContext.getRepositoryId(), username, DEFAULT, remoteIP);

			case 2 :
				return new UserKey(callContext.getRepositoryId(), splitUsername[1], splitUsername[0], remoteIP);
		}
		LOG.error("Invalid username: {}", username);
		throw new CmisUnauthorizedException(AUTHENTICATION_FAILED, SC_UNAUTHORIZED, AUTHENTICATION_FAILED);

	}

	private CallContextData getContextData(CallContext callContext, UserKey userKey) {
		if (cacheEnabled) {
			return getContextDataFromCache(callContext, userKey);
		}
		return initAndValidateContextData(callContext, userKey, callContext.getPassword());
	}

	private CallContextData getContextDataFromCache(CallContext callContext, UserKey userKey) {
		LOG.debug("Retrieving context data from cache. UserKey: {}", userKey);
		CallContextData callContextData = null;
		try {
			// Synch by user key to avoid concurrency problems for different sessions/apps for the same user.
			KeySynchronizer.synch(userKey);

			UserSessions userSessions;
			UserSessionKey userSessionKey;
			String password = callContext.getPassword();
			Element value = securityCache.get(userKey);
			if (value != null) {
				LOG.debug("UserKey in cache: {}", userKey);
				userSessions = (UserSessions) value.getObjectValue();
				userSessionKey = userSessions.get(password);
				if (userSessionKey != null) {
					value = securityCache.get(userSessionKey);
					if (value != null) {
						// Complete hit.
						LOG.debug("Complete hit in cache. UserKey {}.", userKey);
						userSessions.renew(userSessionKey);
						return (CallContextData) value.getObjectValue();
					} else {
						LOG.debug("Not found in cache. UserKey {}.", userKey);
						callContextData = initAndValidateContextData(callContext, userKey, password);
						userSessions.put(userSessionKey, callContextData, securityCache);
					}
				} else {
					LOG.debug("Not found in among current user sessions. UserKey {}." , userKey);
					// init, validate and put
					callContextData = initAndValidateContextData(callContext, userKey, password);
					userSessionKey = new UserSessionKey(userKey, password);
					userSessions.put(userSessionKey, callContextData, securityCache);
				}
			} else {
				LOG.debug("UserKey not in cache: {}", userKey);
				callContextData = initAndValidateContextData(callContext, userKey, password);
				userSessions = new UserSessions(userSessionLimit);
				securityCache.put(new Element(userKey, userSessions));
				userSessionKey = new UserSessionKey(userKey, password);
				userSessions.put(userSessionKey, callContextData, securityCache);
			}

		} finally {
			KeySynchronizer.desynch();
		}

		return callContextData;
	}

	/**
	 * 
	 * @param callContext
	 *            {@link CallContext} The repository id must not be null.
	 * @param userKey
	 * @return
	 */
	private CallContextData initAndValidateContextData(CallContext callContext, UserKey userKey, String password) {
		LOG.debug("Initialising user info. UserKey {}.", userKey);
		Repository repository = repositorySelector.getRepositoryWithSecurityHandlers(userKey.getRepositoryId());
		String authenticationHandlerName= checkHandler(repository, HandlerType.AUTHENTICATION, userKey.getAuthenticationName(), repository.getAuthenticationHandler());
		boolean isSimpleSecurity = repository.getSecurityType().equals(SecurityType.SIMPLE);
		String contextUsername = authenticate(userKey.getUsername(), password, authenticationHandlerName, isSimpleSecurity);

		Set<String> principalIds = new HashSet<>();
		principalIds.add(contextUsername);
		principalIds.add(Constants.PRINCIPAL_ID_ANYONE);
		boolean isAdmin = resolveAuthorization(principalIds, repository, userKey, isSimpleSecurity);

		CallContextData callContextData = new CallContextData(callContext, contextUsername, principalIds, isAdmin);
		LOG.info(String.format("User authenticated: %s. Repository: %s. Is admin: %s", contextUsername, userKey.getRepositoryId(), isAdmin));
		LOG.debug("User principal ids: {}", principalIds);
		return callContextData;
	}

	private String checkHandler(Repository repository, HandlerType handlerType, String handlerName, String defaultHandler) {
		if (handlerName.equals(DEFAULT) || handlerName.equals(defaultHandler)) {
			// OK, the default handler.
			return defaultHandler;
		}
		// Check additional handlers.
		for (SecurityHandler securityHandler : repository.getSecurityHandlers()) {
			if (securityHandler.getHandlerType().equals(handlerType) && securityHandler.getHandlerName().equals(handlerName)) {
				return handlerName;
			}
		}
		LOG.error("Invalid security handler {} for the repository {}", handlerName, repository.getCmisId());
		throw new CmisUnauthorizedException(AUTHENTICATION_FAILED, SC_UNAUTHORIZED, AUTHENTICATION_FAILED);

	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param authenticationName
	 * @param isSimpleSecurity
	 * @return {@link String} The context domain's username.
	 */
	private String authenticate(String username, String password, String authenticationName, boolean isSimpleSecurity) {
		AuthenticationHandler authenticationHandler = securityHandlerFactory.getAuthenticationHandler(authenticationName);
		boolean autheticated = false;
		try {
			autheticated = authenticationHandler.authenticate(username, password);
		} catch (Exception e) {
			throw new CmisUnauthorizedException(AUTHENTICATION_FAILED, SC_UNAUTHORIZED, e);
		}
		if (!autheticated) {
			LOG.error("User not authenticated: {}: {}", authenticationName, username);
			throw new CmisUnauthorizedException(AUTHENTICATION_FAILED, SC_UNAUTHORIZED, AUTHENTICATION_FAILED);
		}
		if (!isSimpleSecurity) {
			return authenticationHandler.getDomain() + PROTOCOL_SEP + username;
		}
		return username;
	}

	private boolean resolveAuthorization(Set<String> principalIds, Repository repository, UserKey userKey, boolean isSimpleSecurity) {
		String username = userKey.getUsername();
		Set<AuthorizationHandler> authorizationHandlers = getAuthorizationHandlers(repository);
		for (AuthorizationHandler authorizationHandler : authorizationHandlers) {
			if (authorizationHandler.isAdmin(username)) {
				// avoid processing principal ids. Not needed for an admin user.
				return true;
			}
		}
		for (AuthorizationHandler authorizationHandler : authorizationHandlers) {
			addPrincipalIds(principalIds, authorizationHandler, username, isSimpleSecurity);
		}
		return false;
	}

	private Set<AuthorizationHandler> getAuthorizationHandlers(Repository repository) {
		Set<AuthorizationHandler> authorizationHandlers = new HashSet<>();
		AuthorizationHandler authorizationHandler = securityHandlerFactory.getAuthorizationHandler(repository.getAuthorisationHandler());
		authorizationHandlers.add(authorizationHandler);
		for (SecurityHandler securityHandler : repository.getSecurityHandlers()) {
			if (securityHandler.getHandlerType().equals(HandlerType.AUTHORISATION)) {
				authorizationHandler = securityHandlerFactory.getAuthorizationHandler(securityHandler.getHandlerName());
				authorizationHandlers.add(authorizationHandler);
			}
		}
		return authorizationHandlers;
	}

	private void addPrincipalIds(Set<String> principalIds, AuthorizationHandler authorizationHandler, String username, boolean isSimpleSecurity) {
		Set<String> basePrincipalIds = authorizationHandler.getPrincipalIds(username);
		if (isSimpleSecurity) {
			principalIds.addAll(basePrincipalIds);
		}
		else {
			// Add the authorization domain to the principal ids.
			String authorizationDomain = authorizationHandler.getDomain();
			for (String principalId : basePrincipalIds) {
				principalIds.add(authorizationDomain + PROTOCOL_SEP + principalId);
			}
		}
	}

	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public void clean() {
		localCallContextData.remove();
		LOG.debug("Call context cleaned.");
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#getRepositoryId()
	 */
	@Override
	public String getRepositoryId() {
		return geCallContextData().getCallContext().getRepositoryId();
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#getUsername()
	 */
	@Override
	public String getUsername() {
		return geCallContextData().getUsername();
	}

	@Override
	public List<UserKey> getCurrentSessions() {
		List<UserKey> currentSessions = new ArrayList<>();
		for (Object sessionKey : securityCache.getKeysWithExpiryCheck()) {
			if (sessionKey instanceof UserKey) {
				currentSessions.add((UserKey) sessionKey);
			}
		}
		return currentSessions;
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#getPrincipalIds()
	 */
	@Override
	public Set<String> getPrincipalIds() {
		return geCallContextData().getPrincipalIds();
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		return geCallContextData().isAdmin();
	}

	private CallContextData geCallContextData() {
		CallContextData callContextData = localCallContextData.get();
		if (callContextData == null) {
			login();
			callContextData = localCallContextData.get();
		}
		return callContextData;
	}

	/**
	 * @see eu.trade.repo.security.CallContextHolder#getClientCmisVersion()
	 */
	@Override
	public CmisVersion getClientCmisVersion() {
		CallContext callContext = localCallContext.get();
		return callContext.getCmisVersion();
	}
	
	
}

