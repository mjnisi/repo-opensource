/**
 * 
 */
package eu.trade.repo.service.cmis;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.AclService;

/**
 * Interface for all ACL services, including the one defined for the AtomPub binding.
 * 
 * @author porrjai
 */
public interface CompleteAclService extends AclService {

	/**
	 * Applies a new ACL to an object (for AtomPub binding).
	 * <p>
	 * Note: The inherited ACEs cannot be removed directly using this method. The workaround is to add the inherited ACE as a direct ACE and later on remove it.
	 * 
	 * @see CmisService#applyAcl(String, String, Acl, AclPropagation)
	 */
	Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation);
}
