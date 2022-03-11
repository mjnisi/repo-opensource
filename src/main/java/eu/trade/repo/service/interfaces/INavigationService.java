
package eu.trade.repo.service.interfaces;

import java.math.BigInteger;
import java.util.Set;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.service.util.Page;
import eu.trade.repo.service.util.Tree;

public interface INavigationService {

	//one level
	Page<CMISObject> getChildren(String repositoryId, String folderId, String orderBy, int maxItems, int skipCount);

	Tree getTree(String repositoryId, String folderId, BigInteger depth, boolean includeAllowableActions, boolean onlyFolders, boolean loadParentsChildren);

	//one level (not recursive in CMIS service)
	CMISObject getObjectWithParents(String repositoryId, String objectId);

	CMISObject getFolderParent(String repositoryId, String folderId);

	Set<CMISObject> getCheckedOutDocs(String repositoryId, String folderId, String orderBy, int maxItems, int skipCount);
}
