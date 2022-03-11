package eu.trade.repo.model;

import java.io.Serializable;

/**
 * Interface for all database entities.
 * <p>
 * All entities are {@link Serializable}.
 */
public interface DBEntity extends Serializable {
	/**
	 * @return the id
	 */
	Integer getId();

	/**
	 * @param id the id to set
	 */
	void setId(Integer id);
}
