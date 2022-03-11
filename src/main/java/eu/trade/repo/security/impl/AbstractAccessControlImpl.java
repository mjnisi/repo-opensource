package eu.trade.repo.security.impl;

import static eu.trade.repo.util.Constants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.changelog.ChangeLog;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.AccessControl;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.RepositorySelector;

/**
 * Generic methods for implementing an access control policy.
 * 
 * @author porrjai
 */
public abstract class AbstractAccessControlImpl implements AccessControl {

	@Autowired
	private RepositorySelector repositorySelector;
	@Autowired
	private Security security;
	@Autowired
	private ChangeLog changeLog;

	protected boolean isApplyAclAllowed(CMISObject cmisObject) {
		return security.isAllowableAction(cmisObject, Action.CAN_APPLY_ACL);
	}

	/**
	 * Whether the applied ACL (removed and added) specify permissions above the current user's own permissions.
	 * 
	 * @param cmisObject
	 * @param actualAclToRemove
	 * @param actualAclToAdd
	 * @return boolean whether the applied ACL (removed and added) specify permissions above the current user's own permissions.
	 */
	protected boolean isAppliedAclBeyondCurrentUserPermissions(CMISObject cmisObject, Set<Acl> aclToRemove, Set<Acl> aclToAdd) {
		Set<String> permissionNames = new HashSet<>();
		for (Acl ace : aclToRemove) {
			permissionNames.add(ace.getPermission().getName());
		}
		for (Acl ace : aclToAdd) {
			permissionNames.add(ace.getPermission().getName());
		}
		for (String permissionName : permissionNames) {
			if (!security.isSimilarGranted(cmisObject, permissionName)) {
				return true;
			}
		}
		return false;
	}

	protected boolean isAclChangeable(String repositoryId, CMISObject cmisObject) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		// Currently repository determined equals to propagate
		return !CapabilityAcl.NONE.equals(repository.getAcl()) && !repository.getAclPropagation().equals(AclPropagation.OBJECTONLY) && cmisObject.getObjectType().isControllableAcl();
	}

	protected boolean isAclObjectOnly(CMISObject cmisObject) {
		Repository repository = cmisObject.getObjectType().getRepository();
		return repository.getAclPropagation().equals(AclPropagation.OBJECTONLY);
	}

	protected AclPropagation getAclPropagation(Repository repository, AclPropagation aclPropagation) {
		AclPropagation repositoryAclPropagation = repository.getAclPropagation();
		switch (repositoryAclPropagation) {
			case REPOSITORYDETERMINED :
			case PROPAGATE :
				if (AclPropagation.OBJECTONLY.equals(aclPropagation)) {
					return AclPropagation.OBJECTONLY;
				}
				return AclPropagation.PROPAGATE;

			case OBJECTONLY :
				if (AclPropagation.PROPAGATE.equals(aclPropagation)) {
					throw new CmisConstraintException("The value for acl propagation doesn't match the values as returned via 'getAclCapabilities'");
				}
				return AclPropagation.OBJECTONLY;

			default :
				throw new IllegalStateException("Unexpected value for repository's ACL propagation: " + repositoryAclPropagation);
		}
	}

	protected void checkObjectType(CMISObject cmisObject) {
		// Check object type
		if (!cmisObject.getObjectType().isControllableAcl()) {
			throw new CmisConstraintException(String.format("The object type (%s) doesn't allow to specify any ACE for this object.", cmisObject.getObjectType()));
		}
	}

	/**
	 * Returns the union of all ACEs from the object's parents. This method returns an ordered set based on the Acl.ACL_INHERITED_COMPARATOR.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object.
	 * @return {@link Set<Acl>} the union of all ACEs from the object's parents.
	 */
	protected Set<Acl> getParentsAcls(CMISObject cmisObject) {
		Set<Acl> union = new TreeSet<>(Acl.ACL_INHERITED_COMPARATOR);
		for (CMISObject parent : cmisObject.getParents()) {
			union.addAll(parent.getAcls());
		}
		return union;
	}

	protected Set<Acl> intersect(Set<Acl> setA, Set<Acl> setB) {
		Set<Acl> intersection = new TreeSet<>(Acl.ACL_COMPARATOR);
		Set<Acl> treeSetB = new TreeSet<>(Acl.ACL_COMPARATOR);
		treeSetB.addAll(setB);
		for (Acl ace : setA) {
			if (treeSetB.contains(ace)) {
				// Ace has the same principal Id, permission and isDirect flag
				intersection.add(ace);
			}
		}
		return intersection;
	}

	protected Set<Acl> getInheritedFrom(Set<Acl> setA, Set<Acl> setB) {
		Set<Acl> inherited = new TreeSet<>(Acl.ACL_INHERITED_COMPARATOR);
		Set<Acl> treeSetB = new TreeSet<>(Acl.ACL_INHERITED_COMPARATOR);
		treeSetB.addAll(setB);
		for (Acl ace : setA) {
			if (!ace.getIsDirect() && treeSetB.contains(ace)) {
				// If the ace is inherited and the same principal Id and permission is found in the other set
				inherited.add(ace);
			}
		}
		return inherited;
	}

	protected Set<Acl> getAllAcl(CMISObject cmisObject, String principalId, Map<String, Permission> permissionsByName) {
		Acl cmisAnyoneAllAcl = new Acl();
		cmisAnyoneAllAcl.setIsDirect(true);
		cmisAnyoneAllAcl.setPermission(permissionsByName.get(CMIS_ALL));
		cmisAnyoneAllAcl.setPrincipalId(principalId);
		cmisAnyoneAllAcl.setObject(cmisObject);
		return Collections.singleton(cmisAnyoneAllAcl);
	}

	protected Set<Acl> getUserAllAcl(CMISObject cmisObject, Map<String, Permission> permissionsByName) {
		return getAllAcl(cmisObject, security.getCallContextHolder().getUsername(), permissionsByName);
	}

	protected Set<Acl> getAnyoneAllAcl(CMISObject cmisObject, Map<String, Permission> permissionsByName) {
		return getAllAcl(cmisObject, PRINCIPAL_ID_ANYONE, permissionsByName);
	}

	protected boolean checkParents(CMISObject cmisObject, Acl ace) {
		for (CMISObject parent : cmisObject.getParents()) {
			for (Acl parentAce : parent.getAcls()) {
				if (Acl.ACL_INHERITED_COMPARATOR.compare(ace, parentAce) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	protected Set<Acl> hydrateAcl(Set<Acl> acl, CMISObject cmisObject, Map<String, Permission> permissionsByName) {
		/* WARN: Since we are changing the Acl objects we cannot work directly with the "acl" set (specially for a HashSet):
		 * - Reason:
		 *           The set implementation can have precalculated values to check the set elements equality (is the case of the HashSet also the hashcode to find the store bucket). Therefore,
		 *           any time you change any attribute of an element within a Set, the best practice is always to force the Set to do the equality check again.
		 * - Option 1: Remove every object from the set and add it again.
		 * - Option 2 (preferred): create a new set.
		 */
		Set<Acl> hydratedAcl = new HashSet<>(acl.size());
		for (Acl ace : acl) {
			ace.setObject(cmisObject);
			String permissionName = ace.getPermission().getName();
			Permission permission = permissionsByName.get(permissionName);
			ace.setPermission(permission);
			hydratedAcl.add(ace);
		}
		return hydratedAcl;
	}

	protected Set<Acl> inheritAcl(CMISObject cmisObject, CMISObject parent) {
		Set<Acl> parentAcl = parent.getAcls();
		return cloneAcl(parentAcl, cmisObject, true);
	}

	protected Set<Acl> cloneAcl(Set<Acl> acl, CMISObject cmisObject, boolean isInherited) {
		if (acl == null || acl.isEmpty()) {
			return new HashSet<>();
		}
		Set<Acl> clonedAcl = new HashSet<>(acl.size());
		for (Acl ace : acl) {
			Acl clonedAce = new Acl(ace);
			clonedAce.setObject(cmisObject);
			if (isInherited) {
				clonedAce.setIsDirect(false);
			}
			clonedAcl.add(clonedAce);
		}

		return clonedAcl;
	}

	protected void logSecurityEvent(String repositoryId, CMISObject cmisObject) {
		changeLog.security(repositoryId, cmisObject.getCmisObjectId());
	}
}
