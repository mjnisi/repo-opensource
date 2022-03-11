package eu.trade.repo.security;

import org.apache.commons.configuration.Configuration;

/**
 * A configurable object using {@link Configuration}
 * 
 * @author porrjai
 */
public interface Configurable {

	/**
	 * Initialize the object.
	 * 
	 * @param configuration {@link Configuration} The configuration for the initialization.
	 */
	void init(Configuration configuration);
}
