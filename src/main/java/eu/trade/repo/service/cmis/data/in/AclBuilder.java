package eu.trade.repo.service.cmis.data.in;

import static eu.trade.repo.util.Constants.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.Permission;

/**
 * Utility class to build an ACL set.
 * 
 * @author porrjai
 */
public final class AclBuilder {

	private AclBuilder() {
	}

	public static Set<Acl> build(org.apache.chemistry.opencmis.commons.data.Acl acl, Set<String> permissionNames, String currentUsername) {
		Set<Acl> acls = new HashSet<>();
		if (acl != null) {
			List<Ace> aces = acl.getAces();
			if (aces != null) {
				for (Ace ace : aces) {
					List<String> permissions = ace.getPermissions();
					if (permissions != null) {
						for (String permissionName : permissions) {
							checkPermission(permissionName, permissionNames);
							Acl aclEntry = new Acl();
							aclEntry.setPermission(new Permission(permissionName));
							aclEntry.setPrincipalId(getPrincipalId(ace.getPrincipalId(), currentUsername));
							aclEntry.setIsDirect(ace.isDirect());
							acls.add(aclEntry);
						}
					}
				}
			}
		}
		return acls;
	}

	private static String getPrincipalId(String principalId, String currentUsername) {
		if (PRINCIPAL_ID_USER.equals(principalId)) {
			return currentUsername;
		}
		return principalId;
	}

	private static void checkPermission(String permissionName, Set<String> permissionNames) {
		if (!permissionNames.contains(permissionName)) {
			throw new CmisConstraintException(String.format("The permission name %s is not allowed by the repository.", permissionName));
		}
	}
}
