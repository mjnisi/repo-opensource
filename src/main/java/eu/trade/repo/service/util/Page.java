package eu.trade.repo.service.util;

import java.util.Set;

/**
 * Utility class to encapsulate a, maybe paginated, collection result.
 * 
 * @author porrjai
 */
public class Page<T extends Object> {

	private final Set<T> pageElements;
	private final int count;

	/**
	 * New instance.
	 * 
	 * @param pageElements
	 * @param count
	 */
	public Page(Set<T> pageElements, int count) {
		this.pageElements = pageElements;
		this.count = count;
	}

	/**
	 * @return the pageElements
	 */
	public Set<T> getPageElements() {
		return pageElements;
	}

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

}
