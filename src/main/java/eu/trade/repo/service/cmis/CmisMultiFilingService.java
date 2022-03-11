package eu.trade.repo.service.cmis;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.spi.MultiFilingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Secured;
import eu.trade.repo.service.interfaces.IMultifilingService;

/**
 * CMIS Multifiling Service implementation.
 * <p>
 * Implementation of the CMIS Multifiling services that uses the {@link IMultifilingService} to perform the needed operations.
 * 
 * @author porrjai
 */
@Transactional
public class CmisMultiFilingService implements MultiFilingService {

	@Autowired
	private IMultifilingService multifilingService;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#addObjectToFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_ADD_OBJECT_TO_FOLDER)
	public void addObjectToFolder(String repositoryId, @ApplyTo(ActionParameter.OBJECT) String objectId, @ApplyTo(ActionParameter.FOLDER) String folderId, Boolean allVersions, ExtensionsData extension) {
		multifilingService.addObjectToFolder(repositoryId, objectId, folderId, allVersions);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.MultiFilingService#removeObjectFromFolder(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_REMOVE_OBJECT_FROM_FOLDER)
	public void removeObjectFromFolder(String repositoryId, @ApplyTo(ActionParameter.OBJECT) String objectId, @ApplyTo(ActionParameter.FOLDER) String folderId, ExtensionsData extension) {
		multifilingService.removeObjectFromFolder(repositoryId, objectId, folderId);
	}
}
