package eu.trade.repo.model;

import javax.persistence.CascadeType;
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
import javax.persistence.Table;


@Entity
@Table(name="permission_mapping")
@NamedQueries ({
	@NamedQuery(name="permissionMapping.all", 						query="select permm from PermissionMapping permm"),
	@NamedQuery(name="permissionMapping.by_permissionName",			query="select permm from PermissionMapping permm where permm.permission.name = :permissionName"),
	@NamedQuery(name="permissionMapping.load_by_repocmisId", 		query="select permm from PermissionMapping permm left join fetch permm.permission p left join fetch p.parent parent where permm.repository.cmisId = :repositoryId"),
	@NamedQuery(name="permissionMapping.by_repo_with_permission", 	query="select permm from PermissionMapping permm left join fetch permm.permission p where permm.repository.cmisId = :repositoryId order by permm.key"),
})
public class PermissionMapping implements DBEntity {
	private static final long serialVersionUID = 1L;
	//MODEL
	@Id
	@GeneratedValue(generator="sq_permission_mapping")
	@SequenceGenerator(sequenceName= "sq_permission_mapping", name="sq_permission_mapping")
	private Integer id;

	@Column(nullable=false, length=100)
	private String key;

	@ManyToOne (cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY)
	@JoinColumn(name="PERMISSION_ID")
	private Permission permission;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name = "REPOSITORY_ID")
	private Repository repository;

	//GETTERS/SETTERS
	@Override
	public Integer 	getId()  {	return this.id;	}
	public String 	getKey()  {	return this.key;	}
	public Permission getPermission() {	return this.permission;	}
	public Repository getRepository() {	return this.repository;	}

	@Override
	public void setId(Integer id) 	{	this.id = id;	}
	public void setKey(String key) 	{	this.key = key;	}
	public void setPermission(Permission permission) {	this.permission= permission;	}
	public void setRepository(Repository repository) {	this.repository = repository;	}

	public PermissionMapping() {
		super();
	}

	public PermissionMapping(String key, Permission permission) {
		super();
		this.key = key;
		this.permission = permission;
	}

}