package eu.trade.repo.service;

import static eu.trade.repo.util.Constants.CMIS_FETCH_ALL;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.model.view.VersionType;
import eu.trade.repo.query.Query;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ObjectParentSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.INavigationService;
import eu.trade.repo.service.util.Page;
import eu.trade.repo.service.util.Tree;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.Utilities;

public class NavigationService extends CMISBaseService implements INavigationService {
	private static final Logger LOG = LoggerFactory.getLogger(NavigationService.class);
	
	private static final Set<VersionType> LATEST_AND_PWC = Utilities.asSet(VersionType.NON_VERSION, VersionType.LATEST, VersionType.PWC);
	private static final String ORDERBY_SORT = "sort";
	private static final String ORDERBY_ASC = "ASC";
	private static final String ORDERBY_DESC = "DESC";
	private static final String ORDERBY_COLUMN = "order_column";

	@Autowired
	private CMISObjectSelector cmisObjectSelector;

	@Autowired
	private RepositorySelector repositorySelector;

	@Autowired
	private ObjectParentSelector objectParentSelector;

	@Autowired
	private ObjectTypePropertySelector objectTypePropertySelector;

	@Autowired
	private Query query;

	@Autowired
	private Security security;

	/**
	 * @see eu.trade.repo.service.interfaces.INavigationService#getChildren(java.lang.String, java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Page<CMISObject> getChildren(String repositoryId, String folderId, String orderBy, int maxItems, int skipCount) {
		LOG.debug(String.format("OBY:%s MAX:%s SKIP:%s", orderBy, maxItems, skipCount));
		Set<CMISObject> pageResult;
		int count;
		
		//ORDER BY 
		Map<String, String> orderByVars = validateOrderBy(orderBy);
		String orderColumn = orderByVars.get(ORDERBY_COLUMN);
		String tmpAsc = orderByVars.get(ORDERBY_SORT);
				
		boolean isAscending = (tmpAsc != null && tmpAsc.equals(ORDERBY_ASC)); 
		
		Repository repository = repositorySelector.getRepository(repositoryId);

		Set<String> principalIds = getPrincipalIds(repository);
		Set<Integer> permissionIds = getPermissionIds(repository);
		Set<VersionType> versionTypes = getVersionTypes(repository);
		if(maxItems == CMIS_FETCH_ALL) {
			pageResult = cmisObjectSelector.getObjectChildren(folderId, principalIds, permissionIds, versionTypes, orderColumn, isAscending);
			count = pageResult.size();
		}
		else {
			pageResult = cmisObjectSelector.getObjectChildren(folderId, maxItems, skipCount, principalIds, permissionIds, versionTypes, orderColumn, isAscending);
			count = cmisObjectSelector.getObjectChildrenCount(folderId, principalIds, permissionIds, versionTypes);
		}

		return new Page<>(pageResult, count);
	}

	/**
	 * @see eu.trade.repo.service.interfaces.INavigationService#getTree(java.lang.String, java.lang.String, java.math.BigInteger, boolean, boolean)
	 */
	@Override
	public Tree getTree(String repositoryId, String folderId, BigInteger depth, boolean includeAllowableActions, boolean onlyFolders, boolean loadParentsChildren) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		checkGetDescendantsOrTree(repository, onlyFolders);
		CMISObject rootFolder = cmisObjectSelector.getCMISObject(repositoryId, folderId);
		Set<ObjectParent> objectParents = getObjectParents(repository, rootFolder, depth, onlyFolders, loadParentsChildren);
		return new Tree(rootFolder, objectParents);
	}

	private Set<ObjectParent> getObjectParents(Repository repository, CMISObject rootFolder, BigInteger depth, boolean onlyFolders, boolean loadParentsChildren) {
		String basePath = rootFolder.getPropertyValue(PropertyIds.PATH);
		
		return objectParentSelector.getObjectParents(
				repository.getCmisId(), 
				basePath, 
				onlyFolders, 
				depth, 
				security.getCallContextHolder().getPrincipalIds(),				//getPrincipalIds(repository),  
				security.getPermissionIds(repository.getCmisId(), Action.CAN_GET_PROPERTIES), //getPermissionIds(repository),   
				getVersionTypes(repository), 
				loadParentsChildren);
	}
	
	@Override
	public CMISObject getObjectWithParents(String repositoryId, String objectId) {
		//TODO review if joining fetch with the parents improve the performance
		Repository repository = repositorySelector.getRepository(repositoryId);
		return cmisObjectSelector.getObjectWithParents(objectId, getPrincipalIds(repository), getPermissionIds(repository));
	}

	@Override
	public CMISObject getFolderParent(String repositoryId, String folderId) {
		CMISObject cmisObject = cmisObjectSelector.getCMISObject(repositoryId, folderId);
		if (cmisObject.isFolder()) {
			//folder can have only one parent
			Set<CMISObject> parentObjects = cmisObject.getParents();
			if(parentObjects.isEmpty()) {
				throw new CmisObjectNotFoundException("The object doesn't have any parents");				
			} else {
				return parentObjects.iterator().next();
			}		
			
		} else {
			throw new CmisInvalidArgumentException("Invalid object type. Argument 'folderId' should point to cmis:folder type.");
		}
	}

	@Override
	public Set<CMISObject> getCheckedOutDocs(String repositoryId, String folderId, String orderBy, int maxItems, int skipCount) {

		String statement = "SELECT * FROM cmis:document where cmis:versionLabel='PWC'";

		if(folderId != null && folderId.trim().length() != 0) {
			statement += " and in_folder('" + folderId + "')";
		}

		if(orderBy != null && orderBy.trim().length() != 0) {
			statement += " order by " + orderBy;
		}

		return query.executeQuery(statement, repositoryId, true, maxItems, skipCount, false).getResult();
	}

	private Map<String, String> validateOrderBy(String orderBy) {
		HashMap<String, String> ans = new HashMap<>();
		String order_by = orderBy;
		
		if (order_by == null) {
			return ans;
		}
		if (order_by.split("\\,").length > 1) {
			throw new CmisRuntimeException("Cannot order for multiple columns "+order_by);
		}
		if (order_by.endsWith(ORDERBY_ASC.toLowerCase())) {
			order_by = order_by.replace(ORDERBY_ASC.toLowerCase(), ORDERBY_ASC);
		}
		if (order_by.endsWith(ORDERBY_DESC.toLowerCase())) {
			order_by = order_by.replace(ORDERBY_DESC.toLowerCase(), ORDERBY_DESC);
		}

		String colname = null;
		if (order_by.contains(ORDERBY_ASC)) {
			ans.put(ORDERBY_SORT, ORDERBY_ASC);
			colname = order_by.substring(0, order_by.indexOf(ORDERBY_ASC)).trim();
		} else 
		if (order_by.contains(ORDERBY_DESC)) {
			ans.put(ORDERBY_SORT, ORDERBY_DESC);			
			colname = order_by.substring(0, order_by.indexOf(ORDERBY_DESC)).trim();
		} else {
			ans.put(ORDERBY_SORT, ORDERBY_ASC);
			colname = order_by.trim();
		}
		
		ans.put(ORDERBY_COLUMN, colname);
		return ans;
	}
	
	private void checkGetDescendantsOrTree(Repository repository, boolean onlyFolders) {
		boolean allowed =
				onlyFolders  && repository.getGetFolderTree() ||
				!onlyFolders && repository.getGetDescendants();

		if (!allowed) {
			throw new CmisNotSupportedException(Constants.CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY);
		}
	}

	private boolean isSecured(Repository repository) {
		return !CapabilityAcl.NONE.equals(repository.getAcl()) && !security.getCallContextHolder().isAdmin();
	}

	private Set<String> getPrincipalIds(Repository repository) {
		if (isSecured(repository)) {
			return security.getCallContextHolder().getPrincipalIds();
		}
		return null;
	}

	private Set<Integer> getPermissionIds(Repository repository) {
		if (isSecured(repository)) {
			return security.getPermissionIds(repository.getCmisId(), Action.CAN_GET_PROPERTIES);
		}
		return null;
	}

	private Set<VersionType> getVersionTypes(Repository repository) {
		if (repository.getVersionSpecificFiling()) {
			// No filter.
			return null;
		}
		return LATEST_AND_PWC;
	}
}
