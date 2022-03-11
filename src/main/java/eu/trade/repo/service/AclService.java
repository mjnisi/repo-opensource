/**
 * 
 */
package eu.trade.repo.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.IAclService;

/**
 * ACL Service implementation.
 * 
 * @author porrjai
 */
public class AclService implements IAclService {

	@Autowired
	private RepositorySelector repositorySelector;
	@Autowired
	private CMISObjectSelector objSelector;
	@Autowired
	private Security security;

	/**
	 * @see eu.trade.repo.service.interfaces.IAclService#getAcl(java.lang.String, java.lang.String)
	 */
	@Override
	public Set<Acl> getAcl(String repositoryId, String cmisObjectId) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		if (CapabilityAcl.NONE.equals(repository.getAcl())) {
			throw new CmisConstraintException(String.format("The repository's ACL capability (%s) doesn't allow to discover any ACE for this object.", repository.getAcl()));
		}
		CMISObject cmisObject = objSelector.getCMISObject(repositoryId, cmisObjectId);
		if (!cmisObject.getObjectType().isControllableAcl()) {
			return Collections.emptySet();
		}
		return cmisObject.getAcls();
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IAclService#applyAcl(java.lang.String, java.lang.String, java.util.Set, java.util.Set, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
	 */
	@Override
	public Set<Acl> applyAcl(String repositoryId, String cmisObjectId, Set<Acl> aclToAdd, Set<Acl> aclToRemove, AclPropagation aclPropagation) {
		validateApplyAcl(repositoryId);
		CMISObject cmisObject = objSelector.getCMISObject(repositoryId, cmisObjectId);
		return doApplyAcl(repositoryId, cmisObject, aclToAdd, aclToRemove, aclPropagation);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IAclService#applyAcl(java.lang.String, java.lang.String, java.util.Set, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
	 */
	@Override
	public Set<Acl> applyAcl(String repositoryId, String cmisObjectId, Set<Acl> aces, AclPropagation aclPropagation) {
		validateApplyAcl(repositoryId);
		CMISObject cmisObject = objSelector.getCMISObject(repositoryId, cmisObjectId);
		Set<Acl> aclToRemove = getAclToRemove(cmisObject, aces);
		return doApplyAcl(repositoryId, cmisObject, aces, aclToRemove, aclPropagation);
	}

	private void validateApplyAcl(String repositoryId) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		if (!CapabilityAcl.MANAGE.equals(repository.getAcl())) {
			throw new CmisConstraintException(String.format("The repository's ACL capability (%s) doesn't allow to apply any ACE for this object.", repository.getAcl()));
		}
	}

	private Set<Acl> doApplyAcl(String repositoryId, CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, AclPropagation aclPropagation) {
		security.getAccessControl().apply(repositoryId, cmisObject, aclToAdd, aclToRemove, aclPropagation);
		return cmisObject.getAcls();
	}

	private Set<Acl> getAclToRemove(CMISObject cmisObject, Set<Acl> acesToSet) {
		Set<Acl> aclToRemove = new HashSet<>();
		if (!acesToSet.isEmpty()) {
			Set<Acl> comparableAclToSet = new TreeSet<Acl>(Acl.ACL_INHERITED_COMPARATOR);
			comparableAclToSet.addAll(acesToSet);
			for (Acl ace : cmisObject.getAcls()) {
				if (ace.getIsDirect() && !comparableAclToSet.contains(ace)) {
					aclToRemove.add(ace);
				}
			}
		}
		return aclToRemove;
	}
}