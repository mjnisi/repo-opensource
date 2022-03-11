package eu.trade.repo.security;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;

/**
 * Interface for the Access Control Management based on ACLs.
 * <p>
 * For the time being 4 different operations has been identified:
 * <ul>
 * <li>create: apply ACL when creating a new object, filed or unfiled.</li>
 * <li>TODO update: apply ACL when creating a new version of an already existing object.</li>
 * <li>move: apply ACL when moving an object.</li>
 * <li>apply: apply ACL for an existing object on request.</li>
 * </ul>
 * 
 * @author porrjai
 */
public interface AccessControl {

	/**
	 * Manage the cmisObject's ACL when creating the object.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param cmisObject {@link CMISObject} The cmis object being created.
	 * @param parent {@link CMISObject} The cmis object's parent. Optional, can be null.
	 * @param aclToAdd {@link Set<Acl>} Set of ACL entries to be added to the cmis object. Mandatory not null, can be empty.
	 * @param aclToRemove {@link Set<Acl>} Set of ACL entries to be removed to the cmis object. Mandatory not null, can be empty.
	 * @param versioningState {@link VersioningState} The versioning state of the cmis object being created.
	 */
	void create(String repositoryId, CMISObject cmisObject, CMISObject parent, Set<Acl> aclToAdd, Set<Acl> aclToRemove, VersioningState versioningState);

	/**
	 * Manage the cmisObject's ACL when the object has been moved.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param cmisObject {@link CMISObject} The alreay moved cmis object.
	 * @param source {@link CMISObject} The cmis object's source.
	 * @param target {@link CMISObject} The cmis object's target.
	 */
	void move(String repositoryId, CMISObject cmisObject, CMISObject source, CMISObject target);

	/**
	 * Manage the new version's ACL when checkin a versionable object.
	 * <p>
	 * Note: The new version has been completely built (but the ACL) before calling this method.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param previousVersion {@link CMISObject} The cmis object previous version. If the previous version is the PWC (the object was created directly as a PWC), then null.
	 * @param newVersion {@link CMISObject} The cmis object's new version. Mandatory not null.
	 * @param aclToAdd {@link Set<Acl>} Set of ACL entries to be added to the cmis object. Mandatory not null, can be empty.
	 * @param aclToRemove {@link Set<Acl>} Set of ACL entries to be removed to the cmis object. Mandatory not null, can be empty.
	 */
	void checkin(String repositoryId, CMISObject previousVersion, CMISObject newVersion, Set<Acl> aclToAdd, Set<Acl> aclToRemove);

	/**
	 * Manage the new version's ACL when checkout a versionable object.
	 * <p>
	 * Note: The PWC has been completely built (but the ACL) before calling this method.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param previousVersion {@link CMISObject} The cmis object previous version. Mandatory not null.
	 * @param checkoutObject {@link CMISObject} The checkout cmis object. Mandatory not null.
	 */
	void checkout(String repositoryId, CMISObject previousVersion, CMISObject checkoutObject);

	/**
	 * Manage the cmisObject's ACL when the object has been filed in the target folder.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param cmisObject {@link CMISObject} The alreay moved cmis object.
	 * @param target {@link CMISObject} The cmis object's target.
	 */
	void file(String repositoryId, CMISObject cmisObject, CMISObject target);

	/**
	 * Manage the cmisObject's ACL when the object has been unfiled from the source folder.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param cmisObject {@link CMISObject} The alreay moved cmis object.
	 * @param source {@link CMISObject} The cmis object's source.
	 */
	void unfile(String repositoryId, CMISObject cmisObject, CMISObject source);

	/**
	 * Apply the requested ACL entries.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @param cmisObject {@link CMISObject} The target cmis object.
	 * @param aclToAdd {@link Set<Acl>} Set of ACL entries to be added to the cmis object. Mandatory not null, can be empty.
	 * @param aclToRemove {@link Set<Acl>} Set of ACL entries to be removed to the cmis object. Mandatory not null, can be empty.
	 * @param aclPropagation {@link AclPropagation} The requested type of ACL propagation.
	 */
	void apply(String repositoryId, CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, AclPropagation aclPropagation);
}
