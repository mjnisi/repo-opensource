package eu.trade.repo.service.interfaces;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;

import eu.trade.repo.model.Acl;

/**
 * Interface for Acl business services.
 * 
 * @author porrjai
 */
public interface IAclService {

	/**
	 * Returns the object's ACL.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObjectId {@link String} The obejct's CMIS id.
	 * @return {@link Set<Acl>} The object's ACL.
	 */
	Set<Acl> getAcl(String repositoryId, String cmisObjectId);

	/**
	 * Apply the requested modification to the object's ACL, returning the result of that modifications.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObjectId {@link String} The obejct's CMIS id.
	 * @param aclToAdd {@link Set<Acl>} The ACL entry set to be added.
	 * @param aclToRemove {@link Set<Acl>}  The ACL entry set to be removed.
	 * @param aclPropagation {@link AclPropagation} The requested type of ACL propagation.
	 * @return {@link Set<Acl>} The resulting object's ACL.
	 */
	Set<Acl> applyAcl(String repositoryId, String cmisObjectId, Set<Acl> aclToAdd, Set<Acl> aclToRemove, AclPropagation aclPropagation);

	/**
	 * Applies a new ACL to an object.
	 * <p>
	 * The ACEs provided here is supposed to be the new complete direct ACL for the object.
	 * Note: The inherited ACL cannot be removed directly with this method. The workaround is to add the inherited ACE as a direct ACE and later on remove it.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObjectId {@link String} The obejct's CMIS id.
	 * @param aces {@link Set<Acl>} The ACL entry set to be set.
	 * @param aclPropagation {@link AclPropagation} The requested type of ACL propagation.
	 * @return {@link Set<Acl>} The resulting object's ACL.
	 */
	Set<Acl> applyAcl(String repositoryId, String cmisObjectId, Set<Acl> aces, AclPropagation aclPropagation);

}
