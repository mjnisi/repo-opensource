package eu.trade.repo.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.ehcache.CacheManager;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Proxy builder for {@link EntityManager} instance using {@link Proxy} and {@link InvocationHandler}.
 * <p>
 * This proxy is used to add the cacheable hint (according to the underlying persistence implementation) to the queries obtained with the method createNamedQuery (except in the case of the query name contains "native").
 * <p>
 * Also, this proxy will set the fetch size hint in the case the queryName is configured (see repo_ormConfig.xml).
 */
public final class EntityManagerProxyBuilder {

	private static final String CACHE_HINT = "orm.hintCache";
	private static final String FETCH_HINT = "orm.hintFetch";
	private static final String FETCH_SIZES = "orm.fetchSizes.fetchSize";
	private static final String FETCH_QUERY = "query";
	private static final String FETCH_SIZE = "size";
	private static final String DEFAULT_SIZE = "orm.fetchSizes.defaultSize";

	@Autowired
	private HierarchicalConfiguration combinedConfig;

	private Set<String> cacheableQueryPrefixes = null;
	private Map<String, Integer> fetchableQueries = null;
	private String cacheHint;
	private String fetchHint;

	public EntityManagerProxyBuilder() {}

	void init() {
		cacheHint = combinedConfig.getString(CACHE_HINT);
		fetchHint = combinedConfig.getString(FETCH_HINT);
		cacheableQueryPrefixes = new HashSet<String>();
		/*
		 * The cacheManager from hibernate has a funny name, so I am
		 * getting all cache names of all cache managers.
		 * 
		 * This is done once, so it's not a big overhead
		 */
		for(CacheManager cacheManager: CacheManager.ALL_CACHE_MANAGERS) {
			for (String name: cacheManager.getCacheNames()) {

				Pattern pattern = Pattern.compile("eu\\.trade\\.repo\\.model\\.(\\w+)(\\.\\w+)?");
				Matcher matcher = pattern.matcher(name);

				if (matcher.find()) {
					cacheableQueryPrefixes.add(matcher.group(1));
				}
			}
		}

		fetchableQueries = new HashMap<>();
		Integer defaultSize = combinedConfig.getInt(DEFAULT_SIZE);
		List<HierarchicalConfiguration> hierarchicalConfigurations = combinedConfig.configurationsAt(FETCH_SIZES);
		if (hierarchicalConfigurations != null) {
			for (HierarchicalConfiguration hierarchicalConfiguration : hierarchicalConfigurations) {
				String query = hierarchicalConfiguration.getString(FETCH_QUERY);
				Integer size = hierarchicalConfiguration.getInteger(FETCH_SIZE, defaultSize);
				fetchableQueries.put(query, size);
			}
		}
	}

	/**
	 * If the queryName contains native or is not prefixed with a class
	 * in the ehcache condifuration returns true
	 * 
	 * @param queryName
	 * @return
	 */
	private boolean isNamedQueryCacheable(String queryName) {
		if (queryName.contains("native")) {
			return false;
		} else {
			String queryNamePrefix = queryName.substring(0, queryName.indexOf('.'));
			return cacheableQueryPrefixes.contains(queryNamePrefix);
		}
	}

	private void setCacheable(Query query, String queryName) {
		if (isNamedQueryCacheable(queryName)) {
			query.setHint(cacheHint, true);
		}
	}

	private void setFetchSize(Query query, String queryName) {
		Integer size = fetchableQueries.get(queryName);
		if (size != null) {
			query.setHint(fetchHint, size);
		}
	}

	/**
	 * Gets an entity Manager with the query cache enabled.
	 * The methods createQuery and createNativeQuery are disabled.
	 * 
	 * @param delegate
	 * @return
	 */
	public EntityManager getInstance(final EntityManager delegate) {
		return getInstance(delegate, true);
	}

	/**
	 * Gets the entity manager with the cache enabled or disabled.
	 * 
	 * If the cache is enabled the methods createQuery and createNativeQuery are
	 * disabled.
	 * 
	 * @param delegate
	 * @param cache
	 * @return
	 */
	public EntityManager getInstance(final EntityManager delegate, boolean cache) {
		if (cache) {
			return getInstanceWithCache(delegate);
		} else {
			return delegate;
		}
	}

	private EntityManager getInstanceWithCache(final EntityManager delegate) {

		InvocationHandler h = new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
				//not allow the execution of createQuery and createNativeQuery
				if("createQuery".equals(method.getName()) || "createNativeQuery".equals(method.getName())) {
					throw new IllegalAccessException("createQuery() and createNativeQuery() are disabled, use createNamedQuery().");
				}

				Object o = method.invoke(delegate, args);
				if ("createNamedQuery".equals(method.getName()))  {
					String queryName = (String) args[0];
					Query query = (Query) o;
					setCacheable(query, queryName);
					setFetchSize(query, queryName);
				}

				return o;
			}
		};

		EntityManager proxy = (EntityManager) Proxy.newProxyInstance(delegate.getClass().getClassLoader(), new Class[] { EntityManager.class }, h);

		return proxy;
	}
}
