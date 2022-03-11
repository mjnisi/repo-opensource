package eu.trade.repo.service.cmis.data.out;

import static eu.trade.repo.util.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.Principal;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Permission;

/**
 * Helper class to build the {@link Acl} collection of a {@link CMISObject}.
 * 
 * @author porrjai
 */
public final class AclBuilder {

	private AclBuilder() {
	}

	/**
	 * Build sthe object access control list according to the chemistry interface.
	 * 
	 * @param acl {@link Set<eu.trade.repo.model.Acl>} The cmis object ACL.
	 * @param onlyBasicPermissions {@link Boolean} Wheter to return the ACL expressed only based om the CMIS basic permission.
	 * @return {@link Acl} The object access control list.
	 */

	public static Acl build(Set<eu.trade.repo.model.Acl> acl, boolean onlyBasicPermissions) {
		AccessControlListImpl accessControlListImpl = new AccessControlListImpl(getAces(acl, onlyBasicPermissions));
		// The ACL always is not exact because admins can access to all the objects ignoring the ACL.
		accessControlListImpl.setExact(false);
		return accessControlListImpl;
	}

	/**
	 * Builds the object access control list according to the chemistry interface.
	 * <p>
	 * Returns the ACL expressed on the repository permission definitions.
	 * 
	 * @param cmisObject {@link CMISObject} the cmis object.
	 * @return {@link Acl} The object access control list.
	 */
	public static Acl build(CMISObject cmisObject) {
		return build(cmisObject.getAcls(), false);
	}

	private static List<Ace> getAces(Set<eu.trade.repo.model.Acl> acl, boolean onlyBasicPermissions) {
		List<Ace> aces = new ArrayList<>();
		addAces(aces, acl, onlyBasicPermissions, true);
		addAces(aces, acl, onlyBasicPermissions, false);
		return aces;
	}

	private static void addAces(List<Ace> aces, Set<eu.trade.repo.model.Acl> acl, boolean onlyBasicPermissions, boolean isDirect) {
		Map<String, List<eu.trade.repo.model.Acl>> acesByPrincipal = getAcesByPrincipal(acl, isDirect);
		for (Map.Entry<String, List<eu.trade.repo.model.Acl>> aceByPrincipal : acesByPrincipal.entrySet()) {
			Principal principal = new AccessControlPrincipalDataImpl(aceByPrincipal.getKey());
			AccessControlEntryImpl accessControlEntryImpl = new AccessControlEntryImpl();
			accessControlEntryImpl.setPrincipal(principal);
			Set<String> permissionsSet = new HashSet<>();
			// add permissions (if all isDirect then isDirect)
			for (eu.trade.repo.model.Acl aclEntry : aceByPrincipal.getValue()) {
				if (onlyBasicPermissions) {
					permissionsSet.add(getBasicPermission(aclEntry.getPermission()));
				}
				else {
					permissionsSet.add(aclEntry.getPermission().getName());
				}

			}
			accessControlEntryImpl.setPermissions(new ArrayList<>(permissionsSet));
			accessControlEntryImpl.setDirect(isDirect);
			aces.add(accessControlEntryImpl);
		}
	}

	private static Map<String, List<eu.trade.repo.model.Acl>> getAcesByPrincipal(Set<eu.trade.repo.model.Acl> acl, boolean isDirect) {
		Map<String, List<eu.trade.repo.model.Acl>> acesByPrincipal = new HashMap<>();
		if (acl != null) {
			for (eu.trade.repo.model.Acl ace : acl) {
				if (ace.getIsDirect().equals(isDirect)) {
					String principalId = ace.getPrincipalId();
					List<eu.trade.repo.model.Acl> aceByPrincipal = acesByPrincipal.get(principalId);
					if (aceByPrincipal == null) {
						aceByPrincipal = new ArrayList<>();
						acesByPrincipal.put(principalId, aceByPrincipal);
					}
					aceByPrincipal.add(ace);
				}
			}
		}
		return acesByPrincipal;
	}

	private static String getBasicPermission(Permission permission) {
		// TODO [porrjai] To be implemented in a better way (translation field or something)
		// Currently, the repository defined permissions MUST extend the basic permissions (i.e. a basic permission will be the ancestor of any defined permission).
		Permission currentPermission = permission;
		while (currentPermission != null) {
			String name = permission.getName();
			if (CMIS_BASIC_PERMISSIONS.contains(name)) {
				return name;
			}
			currentPermission = currentPermission.getParent();
		}

		// To avoid problems in the case of a permission that has no basic ancestor.
		return permission.getName();
	}

}
