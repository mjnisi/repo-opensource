package eu.trade.repo.model.view;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Persistence entity that models a view to resolve in a uniform way the object's {@link VersionType}.
 * 
 * @author porrjai
 */
@Entity
@Table (name="object_version_view")
public class ObjectVersion {

	@Id
	@Column(name="object_id")
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Column(name="version_type", length=10, nullable=false)
	private VersionType versionType;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return the versionType
	 */
	public VersionType getVersionType() {
		return versionType;
	}

	/**
	 * Used by persistence.
	 * 
	 * @param id the id to set
	 */
	private void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Used by persistence.
	 * 
	 * @param versionType the versionType to set
	 */
	private void setVersionType(VersionType versionType) {
		this.versionType = versionType;
	}
}
