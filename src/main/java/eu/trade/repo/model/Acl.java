package eu.trade.repo.model;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.Basic;
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

import eu.trade.repo.util.Utilities;

@Entity
@NamedQueries({
	@NamedQuery(name="acl.countExcludingCmis", query = "select count(acl) from Acl acl left join acl.permission permission left join permission.repository repository where repository.cmisId = :repositoryId and acl.principalId not in (:cmisDefaults)"),
	@NamedQuery(name="acl.test.all", query = "select acl from Acl acl left join fetch acl.permission permission left join fetch permission.repository"),
	@NamedQuery(name="acl.test.by_pid", query = "select acl from Acl acl left join fetch acl.permission permission left join fetch permission.repository where acl.principalId = :principalId"),
})
public class Acl implements DBEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * Comparator by principal id and permission name.
	 * 
	 * @author porrjai
	 */
	public static class AclInheritedComparator implements Comparator<Acl>, Serializable {
		private static final long serialVersionUID = 1L;

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Acl o1, Acl o2) {
			int result = o1.getPrincipalId().compareTo(o2.getPrincipalId());
			if (result == 0) {
				result = o1.getPermission().getName().compareTo(o2.getPermission().getName());
			}
			return result;
		}
	}

	/**
	 * Comparator by principal id, permission name and isDirect flag.
	 * 
	 * @author porrjai
	 */
	public static class AclComparator extends AclInheritedComparator {
		private static final long serialVersionUID = 1L;

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Acl o1, Acl o2) {
			int result = super.compare(o1, o2);
			if (result == 0) {
				result = o1.getIsDirect().compareTo(o2.getIsDirect());
			}
			return result;
		}

	}

	public static final AclInheritedComparator ACL_INHERITED_COMPARATOR = new AclInheritedComparator();
	public static final AclComparator ACL_COMPARATOR = new AclComparator();

	@Id
	@GeneratedValue(generator="sq_acl")
	@SequenceGenerator(sequenceName= "sq_acl", name="sq_acl")
	private Integer id;

	@Column(name="PRINCIPAL_ID", nullable=false, length=100)
	private String principalId;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="OBJECT_ID")
	private CMISObject object;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="PERMISSION_ID")
	private Permission permission;

	@Basic
	@Column(name="IS_DIRECT", nullable=false)
	private Character isDirect;

	/**
	 * New instance. Default constructor.
	 */
	public Acl() {
		this.isDirect = Utilities.setBooleanValue(Boolean.FALSE);
	}

	/**
	 * New instance. Copy constructor.
	 * 
	 * @param acl {@link Acl} The acl to be copied.
	 */
	public Acl(Acl acl) {
		this.isDirect = acl.isDirect;
		this.principalId = acl.principalId;
		this.permission = acl.getPermission();
	}

	@Override
	public Integer    getId() 		  {	return this.id;	}
	public String     getPrincipalId(){	return this.principalId;	}
	public CMISObject getObject() 	  {	return this.object;	}
	public Permission getPermission() {	return this.permission;	}
	public Boolean    getIsDirect()   {	return Utilities.getBooleanValue(this.isDirect);	}

	@Override
	public void setId(Integer id) 					{	this.id = id;	}
	public void setPrincipalId(String principalId) 	{	this.principalId = principalId;	}
	public void setObject(CMISObject object) 		{	this.object = object;	}
	public void setPermission(Permission permission){	this.permission = permission;	}
	public void setIsDirect(Boolean isDirect)       {	this.isDirect = Utilities.setBooleanValue(isDirect);	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (object == null ? 0 : object.hashCode());
		result = prime * result + (permission == null ? 0 : permission.hashCode());
		result = prime * result + (principalId == null ? 0 : principalId.hashCode());
		result = prime * result + (isDirect == null ? 0 : isDirect.hashCode());
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
		if ( !(obj instanceof Acl) ) {
			return false;
		}
		Acl other = (Acl) obj;
		if (object == null) {
			return false;
		}
		if (!object.equals(other.object)) {
			return false;
		}
		if (permission == null) {
			return false;
		}
		if (!permission.equals(other.permission)) {
			return false;
		}
		if (principalId == null) {
			return false;
		}
		if (!principalId.equals(other.principalId)) {
			return false;
		}
		if (isDirect == null) {
			return false;
		}
		return isDirect.equals(other.isDirect);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new StringBuilder().append("[").append(principalId).append(" - ").append(permission).append(" - ").append(isDirect).append("] (").append(object).append(")").toString();
	}
}