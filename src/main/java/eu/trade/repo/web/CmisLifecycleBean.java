/**
 * 
 */
package eu.trade.repo.web;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

/**
 * Chemistry integration with Spring.
 * <p>
 * Based completely in {@link http://chemistry.apache.org/java/how-to/how-to-integrate-spring.html}
 */
public class CmisLifecycleBean implements ServletContextAware, InitializingBean, DisposableBean {

	private ServletContext servletContext;
	@Autowired
	private CmisServiceFactory cmisServiceFactory;

	/**
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() {
		if (cmisServiceFactory != null) {
			cmisServiceFactory.destroy();
		}
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		if (cmisServiceFactory != null) {
			cmisServiceFactory.init(new HashMap<String, String>());
			if (servletContext != null) {
				servletContext.setAttribute(CmisRepositoryContextListener.SERVICES_FACTORY, cmisServiceFactory);
			}
		}
	}

	/**
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
