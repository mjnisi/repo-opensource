package eu.trade.repo.model.view;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import eu.trade.repo.model.CMISObject;

/**
 * Persistence entity that models a view to resolve in a uniform way the object's path.
 * <p>
 * It can be used to retrieve the tree structure under a folder.
 * 
 * @author porrjai
 */
@Entity
@Table (name="object_path_view")
@NamedQueries({
	@NamedQuery(name="ObjectParent.getByPath", query = ObjectParent.GET_OBJECT_BY_PATH),
	@NamedQuery(name="ObjectParent.getByPathAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_FETCH_PARENTS_CHILDREN),
	@NamedQuery(name="ObjectParent.getByPathAndDepth", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH_FETCH_PARENTS_CHILDREN),
	@NamedQuery(name="ObjectParent.getByPathAndAcl", query = ObjectParent.GET_OBJECT_BY_PATH + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndAclAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndVersion", query = ObjectParent.GET_OBJECT_BY_PATH + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndVersionAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndAcl", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndAclAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH_FETCH_PARENTS_CHILDREN + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndVersion", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndVersionAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH_FETCH_PARENTS_CHILDREN + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndAclAndVersion", query = ObjectParent.GET_OBJECT_BY_PATH + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndAclAndVersionAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.VERSION_FILTER+ ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndAclAndVersion", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH + ObjectParent.ACL_FILTER + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getByPathAndDepthAndAclAndVersionAndParentsChildren", query = ObjectParent.GET_OBJECT_BY_PATH_AND_DEPTH_FETCH_PARENTS_CHILDREN + ObjectParent.ACL_FILTER + ObjectParent.VERSION_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPath", query = ObjectParent.GET_FOLDER_BY_PATH),
	@NamedQuery(name="ObjectParent.getFolderByPathAndParentsChildren", query = ObjectParent.GET_FOLDER_BY_PATH_FETCH_PARENTS_CHILDREN),
	@NamedQuery(name="ObjectParent.getFolderByPathAndDepth", query = ObjectParent.GET_FOLDER_BY_PATH + ObjectParent.FOLDER_DEPTH_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPathAndDepthAndParentsChildren", query = ObjectParent.GET_FOLDER_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.FOLDER_DEPTH_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPathAndAcl", query = ObjectParent.GET_FOLDER_BY_PATH + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPathAndAclAndParentsChildren", query = ObjectParent.GET_FOLDER_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPathAndDepthAndAcl", query = ObjectParent.GET_FOLDER_BY_PATH + ObjectParent.FOLDER_DEPTH_FILTER + ObjectParent.ACL_FILTER),
	@NamedQuery(name="ObjectParent.getFolderByPathAndDepthAndAclAndParentsChildren", query = ObjectParent.GET_FOLDER_BY_PATH_FETCH_PARENTS_CHILDREN + ObjectParent.FOLDER_DEPTH_FILTER + ObjectParent.ACL_FILTER)
})
public class ObjectParent {

	private static final String GET_BY_PATH = "select opw from ObjectParent opw inner join fetch opw.cmisObject obj left join fetch obj.properties left join fetch obj.acls left join fetch obj.childrenCount left join fetch obj.parentsCount";
	private static final String FETCH_PARENTS_CHILDREN = " left join fetch obj.parents left join fetch obj.children";
	private static final String BASE_FOLDER_FILTER = " where opw.repositoryId = :repositoryId and opw.objectPath <> :basePath and opw.objectPath like :pathSlash and opw.typeId = 'cmis:folder'";
	private static final String START_BASE_OBJECT_FILTER = " where opw.objectParentId.parentId in (select parent_op.objectParentId.objectId from ObjectParent parent_op where parent_op.repositoryId = :repositoryId and parent_op.typeId = 'cmis:folder' and (parent_op.objectPath = :basePath or (parent_op.objectPath like :pathSlash";
	private static final String BASE_OBJECT_FILTER = START_BASE_OBJECT_FILTER + ")))";
	private static final String BASE_OBJECT_DEPTH_FILTER = START_BASE_OBJECT_FILTER + " and parent_op.objectPath not like concat(:pathSlash, :depthElem))))";
	static final String GET_FOLDER_BY_PATH = GET_BY_PATH + BASE_FOLDER_FILTER;
	static final String GET_FOLDER_BY_PATH_FETCH_PARENTS_CHILDREN = GET_BY_PATH + FETCH_PARENTS_CHILDREN + BASE_FOLDER_FILTER;
	static final String GET_OBJECT_BY_PATH = GET_BY_PATH + BASE_OBJECT_FILTER;
	static final String GET_OBJECT_BY_PATH_FETCH_PARENTS_CHILDREN = GET_BY_PATH + FETCH_PARENTS_CHILDREN + BASE_OBJECT_FILTER;
	static final String GET_OBJECT_BY_PATH_AND_DEPTH = GET_BY_PATH + BASE_OBJECT_DEPTH_FILTER;
	static final String GET_OBJECT_BY_PATH_AND_DEPTH_FETCH_PARENTS_CHILDREN = GET_BY_PATH + FETCH_PARENTS_CHILDREN + BASE_OBJECT_DEPTH_FILTER;
	static final String FOLDER_DEPTH_FILTER = " and opw.objectPath not like concat(:pathSlash, :depthElem)";
	static final String ACL_FILTER = " and opw.objectParentId.objectId in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))";
	static final String VERSION_FILTER = " and exists (select 1 from ObjectVersion ov where ov.id = opw.objectParentId.objectId and ov.versionType in (:versionTypes))";

	@EmbeddedId
	private ObjectParentId objectParentId;

	@Column(name="object_path", insertable=false, updatable=false)
	private String objectPath;

	@Column(name="repository_id", insertable=false, updatable=false)
	private String repositoryId;

	@Column(name="type_id", insertable=false, updatable=false)
	private String typeId;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_id", insertable=false, updatable=false)
	private CMISObject cmisObject;

	/**
	 * @return the objectParentId
	 */
	public ObjectParentId getObjectParentId() {
		return objectParentId;
	}

	/**
	 * @return the objectPath
	 */
	public String getObjectPath() {
		return objectPath;
	}

	/**
	 * @return the repositoryId
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}

	/**
	 * @return the object
	 */
	public CMISObject getCmisObject() {
		return cmisObject;
	}

	/**
	 * @param objectParentId the objectParentId to set
	 */
	private void setObjectParentId(ObjectParentId objectParentId) {
		this.objectParentId = objectParentId;
	}

	/**
	 * @param objectPath the objectPath to set
	 */
	private void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}

	/**
	 * @param repositoryId the repositoryId to set
	 */
	private void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	private void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	/**
	 * @param cmisObject the object to set
	 */
	private void setCmisObject(CMISObject cmisObject) {
		this.cmisObject = cmisObject;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (objectParentId == null ? 0 : objectParentId.hashCode());
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
		if ( !(obj instanceof ObjectParent) ) {
			return false;
		}

		ObjectParent other = (ObjectParent) obj;
		if (objectParentId == null) {
			return false;
		}
		return objectParentId.equals(other.objectParentId);
	}
}
