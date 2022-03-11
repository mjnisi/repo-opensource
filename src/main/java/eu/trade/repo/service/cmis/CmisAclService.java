package eu.trade.repo.service.cmis;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Secured;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.out.AclBuilder;
import eu.trade.repo.service.interfaces.IAclService;

/**
 * CMIS ACL Service implementation.
 * <p>
 * Implementation of the CMIS ACL services that uses the {@link IAclService} to perform the needed operations.
 * 
 * @author porrjai
 */
@Transactional
public class CmisAclService implements CompleteAclService {

	@Autowired
	private IAclService aclService;

	@Autowired
	private Security security;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.AclService#getAcl(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_ACL)
	public Acl getAcl(String repositoryId, @ApplyTo String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		Set<eu.trade.repo.model.Acl> acl = aclService.getAcl(repositoryId, objectId);
		return AclBuilder.build(acl, Boolean.TRUE.equals(onlyBasicPermissions));
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.AclService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_APPLY_ACL)
	public Acl applyAcl(String repositoryId, @ApplyTo String objectId, Acl addAces, Acl removeAces, AclPropagation aclPropagation, ExtensionsData extension) {
		Set<String> permissionNames = security.getPermissionNames(repositoryId);
		String currentUsername = security.getCallContextHolder().getUsername();
		Set<eu.trade.repo.model.Acl> aclToAdd = eu.trade.repo.service.cmis.data.in.AclBuilder.build(addAces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> aclToRemove = eu.trade.repo.service.cmis.data.in.AclBuilder.build(removeAces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> acl = aclService.applyAcl(repositoryId, objectId, aclToAdd, aclToRemove, aclPropagation);
		return AclBuilder.build(acl, false);
	}

	/**
	 * @see eu.trade.repo.service.cmis.CompleteAclService#applyAcl(java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.Acl, org.apache.chemistry.opencmis.commons.enums.AclPropagation)
	 */
	@Override
	@Secured(Action.CAN_APPLY_ACL)
	public Acl applyAcl(String repositoryId, @ApplyTo String objectId, Acl aces, AclPropagation aclPropagation) {
		Set<String> permissionNames = security.getPermissionNames(repositoryId);
		String currentUsername = security.getCallContextHolder().getUsername();
		Set<eu.trade.repo.model.Acl> aclToSet = eu.trade.repo.service.cmis.data.in.AclBuilder.build(aces, permissionNames, currentUsername);
		Set<eu.trade.repo.model.Acl> acl = aclService.applyAcl(repositoryId, objectId, aclToSet, aclPropagation);
		return AclBuilder.build(acl, false);
	}

}