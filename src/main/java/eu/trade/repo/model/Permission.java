package eu.trade.repo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

@Entity
@NamedQueries({
	@NamedQuery(name="permission.by_name", 	query = "select perm from Permission perm left join fetch perm.parent left join fetch perm.repository where perm.name = :name and perm.repository.cmisId = :repositoryId"),
	@NamedQuery(name="permission.all", 		query = "select perm from Permission perm left join fetch perm.parent left join fetch perm.repository where perm.repository.cmisId = :repositoryId order by perm.id")

})
public class Permission implements DBEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="sq_permission")
	@SequenceGenerator(sequenceName= "sq_permission", name="sq_permission")
	private Integer id;

	@Column(length=100)
	private String name;

	@Column(length=100)
	private String description;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="parent_id", referencedColumnName="id")
	private Permission parent;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="repository_id")
	private Repository repository;

	//GETTERS/SETTERS
	@Override
	public Integer	getId() 	{	return this.id;	}
	public String 	getName() 	{	return name;	}
	public Permission getParent() 		{	return parent; }
	public Repository getRepository() 	{	return repository; }
	public String 	getDescription()	{	return description;	}

	@Override
	public void setId(Integer id) 		 {	this.id = id;	}
	public void setName(String name) 	 {	this.name = name;}
	public void setParent (Permission p) {	this.parent = p; }
	public void setDescription(String description)	{	this.description = description;	}
	public void setRepository(Repository repository){	this.repository = repository;	}

	public Permission() {
		super();
	}

	public Permission(String name) {
		super();
		this.name = name;
	}

	public Permission(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (repository == null ? 0 : repository.hashCode());
		return result;
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ( !(obj instanceof Permission) ) {
			return false;
		}
		Permission other = (Permission) obj;
		if (name == null) {
			return false;
		}
		if (!name.equals(other.name)) {
			return false;
		}
		if (repository == null) {
			return false;
		}
		return repository.equals(other.repository);
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}