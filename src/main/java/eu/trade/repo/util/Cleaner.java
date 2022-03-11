package eu.trade.repo.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Cleanable} objects container that will clean them.
 *  
 * @author porrjai
 */
public class Cleaner implements Cleanable {

	private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);
	
	private List<Cleanable> cleanables;
	
	/**
	 * @see eu.trade.repo.util.Cleanable#clean()
	 */
	@Override
	public void clean() {
		LOG.debug("Cleaning objects...");
		List<Cleanable> toClean = getCleanables();
		if (toClean != null) {
			for (Cleanable cleanable : toClean) {
				cleanable.clean();
			}
		}
	}

	/**
	 * @return the cleanables
	 */
	private List<Cleanable> getCleanables() {
		return cleanables;
	}

	/**
	 * @param cleanables the cleanables to set
	 */
	public void setCleanables(List<Cleanable> cleanables) {
		this.cleanables = cleanables;
	}
}
