package eu.trade.repo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity to model a property view to find out whether a {@link CMISObject} has children.
 * 
 * @author porrjai
 */
@Entity
@Table (name="children_count_view")
public class ChildrenCount implements DBEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="id", insertable=false, updatable=false)
	private Integer id;

	@Column(name="num")
	private Integer count;

	/**
	 * @return the id
	 */
	@Override
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(Integer count) {
		this.count = count;
	}
}
