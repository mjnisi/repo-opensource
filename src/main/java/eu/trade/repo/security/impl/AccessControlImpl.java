package eu.trade.repo.security.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.selectors.PermissionSelector;
import eu.trade.repo.selectors.RepositorySelector;

/**
 * Default implementation of {@link eu.trade.repo.security.AccessControl}.
 * <p>
 * In order to configure a different ACL management policy you should redefine the bean accessControl in applicationContext-security.xml.
 * 
 * @author porrjai
 * @see applicationContext-security.xml
 */
public class AccessControlImpl extends AbstractAccessControlImpl {

	private static final Logger LOG = LoggerFactory.getLogger(AccessControlImpl.class);

	private enum NewObjectType {
		CREATE,
		CHECKIN,
		CHECKIN_NEW,
		CHECKOUT
	}

	@Autowired
	private RepositorySelector repositorySelector;
	@Autowired
	private PermissionSelector permissionSelector;

	/**
	 * @see eu.trade.repo.security.AccessControl#create(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject, java.util.Set, java.util.Set, VersioningState)
	 */
	@Override
	public void create(String repositoryId, CMISObject cmisObject, CMISObject parent, Set<Acl> aclToAdd, Set<Acl> aclToRemove, VersioningState versioningState) {
		if (VersioningState.CHECKEDOUT.equals(versioningState)) {
			setNewObjectAcl(repositoryId, cmisObject, parent, aclToAdd, aclToRemove, NewObjectType.CHECKOUT);
		}
		else {
			setNewObjectAcl(repositoryId, cmisObject, parent, aclToAdd, aclToRemove, NewObjectType.CREATE);
		}
	}

	/**
	 * @see eu.trade.repo.security.AccessControl#move(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject)
	 */
	@Override
	public void move(String repositoryId, CMISObject cmisObject, CMISObject source, CMISObject target) {
		if (isAclChangeable(repositoryId, cmisObject)) {
			// Update the inherited ACL entries.
			apply(repositoryId, cmisObject, target.getAcls(), source.getAcls(), true, true);
		}
		// Else, the object's ACL is not modified.
	}

	/**
	 * If the previous version is null the object was created directly as a PWC, then this method behaves similarly as the create method.
	 * <p>
	 * Note: In this case the new object can have multiple parents.
	 * 
	 * @see eu.trade.repo.security.AccessControl#checkin(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject, java.util.Set, java.util.Set)
	 */
	@Override
	public void checkin(String repositoryId, CMISObject previousVersion, CMISObject newVersion, Set<Acl> aclToAdd, Set<Acl> aclToRemove) {
		if (previousVersion ==  null) {
			setNewObjectAcl(repositoryId, newVersion, null, aclToAdd, aclToRemove, NewObjectType.CHECKIN_NEW);
		}
		else {
			setNewObjectAcl(repositoryId, newVersion, previousVersion, aclToAdd, aclToRemove, NewObjectType.CHECKIN);
		}
	}


	/**
	 * @see eu.trade.repo.security.AccessControl#checkout(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject)
	 */
	@Override
	public void checkout(String repositoryId, CMISObject previousVersion, CMISObject checkoutObject) {
		setNewObjectAcl(repositoryId, checkoutObject, null, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), NewObjectType.CHECKOUT);
	}

	/**
	 * @see eu.trade.repo.security.AccessControl#file(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject)
	 */
	@Override
	public void file(String repositoryId, CMISObject cmisObject, CMISObject target) {
		if (isAclChangeable(repositoryId, cmisObject)) {
			apply(repositoryId, cmisObject, target.getAcls(), Collections.<Acl>emptySet(), true, true);
		}
	}

	/**
	 * @see eu.trade.repo.security.AccessControl#unfile(java.lang.String, eu.trade.repo.model.CMISObject, eu.trade.repo.model.CMISObject)
	 */
	@Override
	public void unfile(String repositoryId, CMISObject cmisObject, CMISObject source) {
		if (isAclChangeable(repositoryId, cmisObject)) {
			apply(repositoryId, cmisObject, Collections.<Acl>emptySet(), source.getAcls(), true, true);
		}
	}

	/**
	 * @see eu.trade.repo.security.AccessControl#apply(java.lang.String, eu.trade.repo.model.CMISObject, java.util.Set, java.util.Set, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
	 */
	@Override
	public void apply(String repositoryId, CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, AclPropagation aclPropagation) {
		checkObjectType(cmisObject);
		Repository repository = repositorySelector.getRepository(repositoryId);
		AclPropagation requestedAclPropagation = getAclPropagation(repository, aclPropagation);
		// Currently only propagate and object only.
		boolean propagate = AclPropagation.PROPAGATE.equals(requestedAclPropagation);
		Map<String, Permission> permissionsByName = permissionSelector.getAllPermissionsByName(repositoryId);

		Set<Acl> actualAclToAdd = hydrateAcl(aclToAdd, cmisObject, permissionsByName);
		Set<Acl> actualAclToRemove = hydrateAcl(aclToRemove, cmisObject, permissionsByName);
		// First, do not add what are you going to remove.
		actualAclToAdd.removeAll(actualAclToRemove);
		// Third, add and remove the ACLs, propagating the changes when requested.
		apply(repositoryId, cmisObject, actualAclToAdd, actualAclToRemove, propagate, false);
	}

	private void setNewObjectAcl(String repositoryId, CMISObject cmisObject, CMISObject source, Set<Acl> aclToAdd, Set<Acl> aclToRemove, NewObjectType newObjectType) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		if (!aclToAdd.isEmpty() || !aclToRemove.isEmpty()) {
			// Check repository capabilities
			if (!CapabilityAcl.MANAGE.equals(repository.getAcl())) {
				throw new CmisConstraintException(String.format("The repository's ACL capability (%s) doesn't allow to specify any ACE for this object.", repository.getAcl()));
			}
			checkObjectType(cmisObject);
		}

		// Note: When the object type is not ACL controllable, anyone has access to that kind of objects.
		Map<String, Permission> permissionsByName = permissionSelector.getAllPermissionsByName(repositoryId);
		if ( CapabilityAcl.NONE.equals(repository.getAcl()) || !cmisObject.getObjectType().isControllableAcl() ) {
			setAcl(cmisObject, getAnyoneAllAcl(cmisObject, permissionsByName));
		}
		else {
			setNewObjectAcl(repository, cmisObject, source, aclToAdd, aclToRemove, newObjectType, permissionsByName);
		}
	}

	private void setNewObjectAcl(Repository repository, CMISObject cmisObject, CMISObject source, Set<Acl> aclToAdd, Set<Acl> aclToRemove, NewObjectType newObjectType, Map<String, Permission> permissionsByName) {
		Set<Acl> actualAclToAdd = aclToAdd;
		CMISObject actualSource = null;
		// actualAclToAdd and actualSource are set depending on the type. Note that anyway aclToAdd still can be modified.
		switch (newObjectType) {
			case CHECKIN:
				// actualSource is null: source object is the previous version. True on checkin method.
				Set<Acl> aclFromPreviousVersion = cloneAcl(source.getAcls(), cmisObject, false);
				actualAclToAdd.addAll(aclFromPreviousVersion);
				break;
			case CHECKIN_NEW:
				// actualSource is null. Note, the previous version cannot be null, so if only exists the checkout "version", that will be the previous version.
				Set<Acl> aclFromParents = cloneAcl(getParentsAcls(cmisObject), cmisObject, true);
				actualAclToAdd.addAll(aclFromParents);
				break;
			case CHECKOUT:
				// actualSource is null.
				// If specific ACL are not defined then set the ACE: [current-user - cmis:all].
				if (aclToAdd.isEmpty() && aclToRemove.isEmpty()) {
					actualAclToAdd = getUserAllAcl(cmisObject, permissionsByName);
				}
				break;
			case CREATE:
				// source object is the parent folder. True on create method
				// Discover capability may add some derived ACEs to the object
				actualSource = source;
				break;
		}
		setAcl(repository, cmisObject, actualSource, actualAclToAdd, aclToRemove, permissionsByName);
	}

	private void apply(String repositoryId, CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, boolean propagate, boolean isInherited) {
		Set<String> loggedNodes = new HashSet<String>();
		apply(loggedNodes, repositoryId, cmisObject, aclToAdd, aclToRemove, propagate, isInherited);
		if (!loggedNodes.isEmpty()) {
			LOG.info("Security evetn logged for {} nodes.", loggedNodes.size());
		}
	}

	private void apply(Set<String> loggedNodes, String repositoryId, CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> aclToRemove, boolean propagate, boolean isInherited) {
		Set<Acl> cmisObjectAcl = cmisObject.getAcls();
		Set<Acl> actualAclToRemove = aclToRemove;
		Set<Acl> actualAclToAdd = aclToAdd;

		if (!aclToRemove.isEmpty()) {
			actualAclToRemove = getAclToRemove(cmisObject, aclToRemove, isInherited);
		}
		if (!aclToAdd.isEmpty()) {
			actualAclToAdd = getAclToAdd(cmisObject, aclToAdd, actualAclToRemove, isInherited);
		}

		// Note: This check (apply acl is restricted to the current user permissions' level) is perform in the direct object only and even if the object's ACL is not changed.
		if (!isInherited && isAppliedAclBeyondCurrentUserPermissions(cmisObject, aclToRemove, aclToAdd)) {
			throw new CmisPermissionDeniedException("The operation cannot be performed. The change of some object's ACL is beyond the current user permissions. "+cmisObject.getCmisObjectId());
		}

		boolean isToChangeOrPropagate = !actualAclToRemove.isEmpty() || !actualAclToAdd.isEmpty();
		if (isToChangeOrPropagate) {
			// if changed and isInherited then check that ApplyACL is an allowableAction. If isInherited is false, the check has already been performed.
			// Note: The check must be performed before changing the ACLs.
			if (isInherited) {
				if (!isApplyAclAllowed(cmisObject)) {
					throw new CmisPermissionDeniedException("The operation cannot be performed. The indirect change of some object's ACL is not allowed. "+cmisObject.getCmisObjectId());
				}
				// Note: This check (apply acl is restricted to the current user permissions' level) is perform in the the propagated subtree only if there are changes.
				if (isAppliedAclBeyondCurrentUserPermissions(cmisObject, actualAclToRemove, actualAclToAdd)) {
					throw new CmisPermissionDeniedException("The operation cannot be performed. The change of some object's ACL is beyond the current user permissions. "+cmisObject.getCmisObjectId());
				}
			}

			// Actually change the object's ACL. At this point is possible that the actual ACL doesn't change (because getAclToAdd method is not removing the already present ACEs).
			boolean actuallyChanged = cmisObjectAcl.removeAll(actualAclToRemove);
			actuallyChanged |= cmisObjectAcl.addAll(actualAclToAdd);

			// Only needed if the ACL is real changed.
			if (actuallyChanged) {
				// Check also when is inherited to verify the change doesn't end with an empty ACL.
				checkNewAcl(cmisObject, cmisObjectAcl);
				// log security event
				logNode(loggedNodes, repositoryId, cmisObject);
			}

			if (cmisObject.isFolder()) {
				if (propagate) {
					// if changed and isFolder and propagate, then getChildren and apply to children (only new inheritance).
					for (CMISObject child : cmisObject.getChildren()) {
						apply(loggedNodes, repositoryId, child, actualAclToAdd, actualAclToRemove, true, true);
					}
				}
				else if (!actualAclToRemove.isEmpty()) {
					// if changed (removed some ace) and isFolder and not propagate (remove as objectonly), then getChildren and set as isDirect any inherited ACE that has been removed.
					for (CMISObject child : cmisObject.getChildren()) {
						Set<Acl> removedFromParent = new TreeSet<>(Acl.ACL_INHERITED_COMPARATOR);
						removedFromParent.addAll(actualAclToRemove);
						boolean modified = setAsDirect(child, removedFromParent);
						// Security events for every child will be logged if its acl are actually modified.
						if (modified) {
							logNode(loggedNodes, repositoryId, child);
						}
					}
				}
			}
		}
	}

	/**
	 * Logs a security event for the modified node and for all its parents, but only in the case the node were not previously logged.
	 * 
	 * @param loggedNodes
	 * @param repositoryId
	 * @param cmisObject
	 */
	private void logNode(Set<String> loggedNodes, String repositoryId, CMISObject cmisObject) {
		if (logSingleNode(loggedNodes, repositoryId, cmisObject)) {
			for (CMISObject parent : cmisObject.getParents()) {
				logSingleNode(loggedNodes, repositoryId, parent);
			}
		}
	}

	private boolean logSingleNode(Set<String> loggedNodes, String repositoryId, CMISObject cmisObject) {
		String cmisObjectId = cmisObject.getCmisObjectId();
		if (!loggedNodes.contains(cmisObjectId)) {
			loggedNodes.add(cmisObjectId);
			logSecurityEvent(repositoryId, cmisObject);
			return true;
		}
		return false;
	}

	private boolean setAsDirect(CMISObject child, Set<Acl> removedFromParent) {
		Set<Acl> childAcl = child.getAcls();
		Set<Acl> modifiedChildAcl = new HashSet<>();
		Iterator<Acl> childAclIt = childAcl.iterator();
		while (childAclIt.hasNext()) {
			Acl ace = childAclIt.next();
			if (!ace.getIsDirect() && removedFromParent.contains(ace)) {
				childAclIt.remove();
				ace.setIsDirect(true);
				modifiedChildAcl.add(ace);
			}
		}
		return childAcl.addAll(modifiedChildAcl);
	}

	private Set<Acl> getAclToRemove(CMISObject cmisObject, Set<Acl> source, boolean isInherited) {
		Set<Acl> aclToRemove;
		if (isInherited) {
			aclToRemove = getInheritedFrom(cmisObject.getAcls(), source);
			if (!aclToRemove.isEmpty()) {
				// Note: target folder is already one of the object's parents.
				Set<Acl> union = getParentsAcls(cmisObject);
				// Note: In order to make the removeAll method to work properly, both sets has to be based on the Acl.ACL_INHERITED_COMPARATOR
				aclToRemove.removeAll(union);
			}
		}
		else {
			aclToRemove = intersect(cmisObject.getAcls(), source);
		}
		return aclToRemove;
	}

	private Set<Acl> getAclToAdd(CMISObject cmisObject, Set<Acl> aclToAdd, Set<Acl> actualAclToRemove, boolean isInherited) {
		Set<Acl> actualAclToAdd = cloneAcl(aclToAdd, cmisObject, isInherited);
		actualAclToAdd.removeAll(actualAclToRemove);
		return actualAclToAdd;
	}

	private void setAcl(Repository repository, CMISObject cmisObject, CMISObject parent, Set<Acl> aclToAdd, Set<Acl> aclToRemove, Map<String, Permission> permissionsByName) {
		Set<Acl> actualAclToAdd = hydrateAcl(aclToAdd, cmisObject, permissionsByName);
		Set<Acl> actualAclToRemove = hydrateAcl(aclToRemove, cmisObject, permissionsByName);
		// Currently repository determined equals to propagate
		boolean objectOnly = repository.getAclPropagation().equals(AclPropagation.OBJECTONLY) || parent == null;
		if (objectOnly) {
			// remove acl from added
			actualAclToAdd.removeAll(actualAclToRemove);
			setAcl(cmisObject, actualAclToAdd);
		}
		else {
			// Get inherited ACL link to new object
			Set<Acl> cmisObjectAcl = inheritAcl(cmisObject, parent);
			// remove acl from inherited and from added
			cmisObjectAcl.removeAll(actualAclToRemove);
			actualAclToAdd.removeAll(actualAclToRemove);
			// Add the rest of the added acl
			cmisObjectAcl.addAll(actualAclToAdd);
			setAcl(cmisObject, cmisObjectAcl);
		}
	}

	private void setAcl(CMISObject cmisObject, Set<Acl> acl) {
		checkNewAcl(cmisObject, acl);

		// Note: In order to prevent problems with already persisted objects (checkout) and the orphan collections, we clean the collection and add the new Acl.
		Set<Acl> cmisObjectAcl = cmisObject.getAcls();
		cmisObjectAcl.clear();
		cmisObjectAcl.addAll(acl);
	}

	private void checkNewAcl(CMISObject cmisObject, Set<Acl> cmisObjectAcl) {
		if (cmisObjectAcl.isEmpty()) {
			throw new CmisConstraintException("The resulting ACL for the object cannot be empty, please review it: " + cmisObject);
		}
		for (Acl ace : cmisObjectAcl) {
			if (!ace.getIsDirect()) {
				if (isAclObjectOnly(cmisObject)) {
					throw new CmisConstraintException("Inherited ACE is not allowed for this object: " + cmisObject);
				}
				else if (!checkParents(cmisObject, ace)) {
					throw new CmisConstraintException(String.format("Inherited ACE %s is not present in any parent for this object: %s", ace, cmisObject));
				}
			}
		}
	}
}