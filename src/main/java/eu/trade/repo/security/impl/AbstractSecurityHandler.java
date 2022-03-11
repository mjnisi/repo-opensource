/**
 * 
 */
package eu.trade.repo.security.impl;

import eu.trade.repo.security.SecurityHandler;

/**
 * Abstract common logic for all security handlers.
 * <p>
 * Basically manages the name and description properties.
 * 
 * @author porrjai
 */
public abstract class AbstractSecurityHandler implements SecurityHandler {

	private String name;
	private String description;

	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinition#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see eu.trade.repo.security.SecurityHandlerDefinition#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see eu.trade.repo.security.SecurityHandler#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see eu.trade.repo.security.SecurityHandler#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
}
