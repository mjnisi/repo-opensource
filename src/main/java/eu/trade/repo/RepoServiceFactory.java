package eu.trade.repo;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
//import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.security.CallContextHolder;
import eu.trade.repo.util.Constants;

/**
 * Repository service factory.
 * 
 * @author porrjai
 */
public class RepoServiceFactory extends AbstractServiceFactory {

	@Autowired
	private CallContextHolder callContextHolder;

	@Autowired
	private ObjectFactory<CmisService> cmisServiceBeanFactory;

	@Autowired
	private Configuration combinedConfig;

	private ThreadLocal<ConformanceCmisServiceWrapper> threadLocalService;

	private static final String MAX_ITEMS_TYPES = "product.maxItemsTypes";
	private static final String DEPTH_TYPES = "product.depthTypes";
	private static final String MAX_ITEMS_OBJECTS = "product.maxItemsObjects";
	private static final String DEPTH_OBJECTS = "product.depthObjects";

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#init(java.util.Map)
	 */
	@Override
	public void init(Map<String, String> parameters) {
		threadLocalService = new ThreadLocal<>();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#getService(org.apache.chemistry.opencmis.commons.server.CallContext)
	 */
	@Override
	public CmisService getService(CallContext context) {
		callContextHolder.initContext(context);

		ConformanceCmisServiceWrapper wrapperService = threadLocalService.get();
		if (wrapperService == null) {
			wrapperService = new RepoServiceWrapper(cmisServiceBeanFactory.getObject(),
					combinedConfig.getBigInteger(MAX_ITEMS_TYPES), 
					combinedConfig.getBigInteger(DEPTH_TYPES), 
					combinedConfig.getBigInteger(MAX_ITEMS_OBJECTS), 
					combinedConfig.getBigInteger(DEPTH_OBJECTS));
			threadLocalService.set(wrapperService);
		}

		return wrapperService;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#getMaxContentSize()
	 */
	@Override
	public long getMaxContentSize() {
		if (combinedConfig != null) {
			return combinedConfig.getLong(Constants.PROPNAME_PRODUCT_MAX_CONTENT_SIZE);
		}
		return super.getMaxContentSize();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#destroy()
	 */
	@Override
	public void destroy() {
		threadLocalService = null;
		super.destroy();
	}
}
