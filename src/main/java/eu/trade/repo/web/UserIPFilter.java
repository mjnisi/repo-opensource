/**
 * 
 */
package eu.trade.repo.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Http Filter to store the current user request IP
 * 
 * @author porrjai
 */
public class UserIPFilter implements Filter {

	private static final ThreadLocal<String> LOCAL_IP = new ThreadLocal<>();

	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			String remoteAddress = request.getRemoteAddr();
			LOCAL_IP.set(remoteAddress);
			chain.doFilter(request, response);
		}
		finally {
			LOCAL_IP.remove();
		}
	}

	public static String getCurrentIP() {
		return LOCAL_IP.get();
	}
}
