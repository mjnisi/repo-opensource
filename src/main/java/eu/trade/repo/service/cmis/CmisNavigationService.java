package eu.trade.repo.service.cmis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Secured;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.service.interfaces.INavigationService;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.util.Node;
import eu.trade.repo.service.util.Page;
import eu.trade.repo.service.util.Tree;

/**
 * CMIS Navigation Service implementation.
 * <p>
 * Implementation of the CMIS navigation services that uses the {@link INavigationService} to perform the needed operations.
 * 
 * <p>
 * TODO: Review security restrictions for getDescendants and getFolderTree.
 */
@Transactional
public class CmisNavigationService implements NavigationService {

	@Autowired
	private INavigationService navigationService;
	
	@Autowired
	private IRelationshipService relationshipService;	

	@Autowired
	private Security security;	

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getChildren(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override	
	@Secured(Action.CAN_GET_CHILDREN)
	public ObjectInFolderList getChildren(String repositoryId, @ApplyTo String folderId, String filter, String orderBy, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		//TODO implement orderBy
		//TODO implement rendition filter

		ObjectInFolderListImpl result = new ObjectInFolderListImpl();
		result.setObjects(new ArrayList<ObjectInFolderData>());

		int maxItemsInt = maxItems.intValue();
		Page<CMISObject> childrenPage = navigationService.getChildren(repositoryId, folderId, orderBy, maxItemsInt, skipCount.intValue());
		Set<CMISObject> children = childrenPage.getPageElements();
		int count = childrenPage.getCount();
		result.setHasMoreItems(skipCount.intValue() + children.size() < count);
		result.setNumItems(BigInteger.valueOf(count));

		Map<String, String> filterSet = PropertiesBuilder.buildFilter(filter);		
		Map<String, Set<CMISObject>> relationshipMappings = getRelationshipMappings(repositoryId, children, includeRelationships);
				
		for(CMISObject child: children) {
		
			AllowableActions allowableActions = null;
			if(Boolean.TRUE.equals(includeAllowableActions)) {
				allowableActions = security.getAllowableActions(child);
			}
		
			List<CMISObject> relationshipForObject = relationshipMappings.get(child.getCmisObjectId()) == null ? new ArrayList() : new ArrayList(relationshipMappings.get(child.getCmisObjectId())); 
														 					
			ObjectInFolderDataImpl objectInFolderData = new ObjectInFolderDataImpl(new ObjectDataImpl(child, allowableActions, relationshipForObject, filterSet));
			if(Boolean.TRUE.equals(includePathSegment)) {
				objectInFolderData.setPathSegment(child.getPathSegment());
			}
			result.getObjects().add(objectInFolderData);
		}
		return result;
	}		
	
	/**
	 * @param repositoryId
	 * @param cmisIds
	 * @param includeRelationships
	 * @return the aggregated relationships not filtered by a collection of objects  
	 */
	private Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Set<CMISObject> cmisObjects, IncludeRelationships includeRelationships) {
		return relationshipService.getRelationshipMappings(repositoryId, cmisObjects, includeRelationships);						
	}		
	
	/**
	 * 
	 * @param repositoryId
	 * @param nodeDescendants
	 * @param includeRelationships
	 * @return the aggregated relationships not filtered by a collection of objects
	 */
	private Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Collection<Node> nodeDescendants, IncludeRelationships includeRelationships) {
		return relationshipService.getRelationshipMappings(repositoryId, nodeDescendants, includeRelationships);						
	}
		
	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getDescendants(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_DESCENDANTS)
	public List<ObjectInFolderContainer> getDescendants(String repositoryId, @ApplyTo String folderId, BigInteger depth, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return getTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, includePathSegment, false);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getFolderTree(java.lang.String, java.lang.String, java.math.BigInteger, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_DESCENDANTS)
	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, @ApplyTo String folderId, BigInteger depth, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		return getTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, includePathSegment, true);
	}

	private List<ObjectInFolderContainer> getTree(String repositoryId, @ApplyTo String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, boolean includePathSegment, boolean onlyFolders) {
		Map<String, String> filterSet = PropertiesBuilder.buildFilter(filter);

		//TODO implement include rendition filter
		Map<Integer, ObjectData> objectDataMap = new HashMap<>();
		Tree tree = navigationService.getTree(repositoryId, folderId, depth, Boolean.TRUE.equals(includeAllowableActions), onlyFolders, false);
		Collection<Node> nodeDescendants = tree.getDescendants();
				
		Map<String, Set<CMISObject>> relationshipMappings = getRelationshipMappings(repositoryId, nodeDescendants, includeRelationships);
				
		for(Node node: nodeDescendants) {
			CMISObject cmisObject = node.getCmisObject();
			AllowableActions allowableActions = null;
			
			if(includeAllowableActions) {
				allowableActions = security.getAllowableActions(cmisObject);
			}
			
			List<CMISObject> relationshipForObject = relationshipMappings.get(cmisObject.getCmisObjectId()) == null ? new ArrayList() : new ArrayList(relationshipMappings.get(cmisObject.getCmisObjectId()));
			
			objectDataMap.put(node.getKey(), new ObjectDataImpl(cmisObject, allowableActions, relationshipForObject, filterSet));
		}

		return buildTree(tree.getRoot(), objectDataMap, Boolean.TRUE.equals(includePathSegment));
	}

	private List<ObjectInFolderContainer> buildTree(Node node, Map<Integer, ObjectData> objectDataMap, boolean includePathSegment) {
		List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
		for (Node child : node.getChildren()) {
			ObjectData objectData = objectDataMap.get(child.getKey());
			ObjectInFolderDataImpl oifd = new ObjectInFolderDataImpl(objectData);
			ObjectInFolderContainerImpl oifc = new ObjectInFolderContainerImpl(oifd);
			if(includePathSegment) {
				oifd.setPathSegment(child.getCmisObject().getPathSegment());
			}
			if (!child.getChildren().isEmpty()) {
				oifc.setChildren(buildTree(child, objectDataMap, includePathSegment));
			}
			result.add(oifc);
		}
		return result;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getObjectParents(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_OBJECT_PARENTS)
	public List<ObjectParentData> getObjectParents(String repositoryId, @ApplyTo String objectId, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includeRelativePathSegment, ExtensionsData extension) {
		Map<String, String> filterSet = PropertiesBuilder.buildFilter(filter);

		//TODO implement rendition filter
		List<ObjectParentData> result = new ArrayList<ObjectParentData>();
		CMISObject cmisObject = navigationService.getObjectWithParents(repositoryId, objectId);		
		
		Map<String, Set<CMISObject>> relationshipMappings = getRelationshipMappings(repositoryId, cmisObject.getParents(), includeRelationships);
		for(CMISObject parent: cmisObject.getParents()) {
			AllowableActions allowableActions = null;
			
			if(Boolean.TRUE.equals(includeAllowableActions)) {
				allowableActions = security.getAllowableActions(parent);
			}
			
			List<CMISObject> relationshipForObject = relationshipMappings.get(parent.getCmisObjectId()) == null ? new ArrayList() : new ArrayList(relationshipMappings.get(parent.getCmisObjectId()));
			
			ObjectParentDataImpl opd = new ObjectParentDataImpl(new ObjectDataImpl(parent, allowableActions, relationshipForObject, filterSet));

			if(Boolean.TRUE.equals(includeRelativePathSegment)) {
				opd.setRelativePathSegment(cmisObject.getPathSegment());
			}

			result.add(opd);
		}

		return result;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getFolderParent(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_FOLDER_PARENT)
	public ObjectData getFolderParent(String repositoryId, @ApplyTo String folderId, String filter, ExtensionsData extension) {
		CMISObject parent = navigationService.getFolderParent(repositoryId, folderId);
		//allowable actions included? as no parameter in the service, no allowable actions
		return new ObjectDataImpl(parent, null, null, filter);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.NavigationService#getCheckedOutDocs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_CHILDREN)
	public ObjectList getCheckedOutDocs(String repositoryId, @ApplyTo(mandatory=false) String folderId, String filter, String orderBy, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

		Set<CMISObject> checkedOutCMISObjects = navigationService.getCheckedOutDocs(repositoryId, folderId, orderBy, maxItems.intValue(), skipCount.intValue());

		ObjectList checkedOutDocs = new ObjectListImpl();

		List<ObjectData> objects = new ArrayList<>();
		
		Map<String, Set<CMISObject>> relationshipMappings = getRelationshipMappings(repositoryId, checkedOutCMISObjects, includeRelationships);
		for(CMISObject cmisObj : checkedOutCMISObjects) {

			AllowableActions allowableActions = null;			
			if(Boolean.TRUE.equals(includeAllowableActions)) {
				allowableActions = security.getAllowableActions(cmisObj);
			}
			
			List<CMISObject> relationshipForObject = relationshipMappings.get(cmisObj.getCmisObjectId()) == null ? new ArrayList() : new ArrayList(relationshipMappings.get(cmisObj.getCmisObjectId()));
			objects.add(new ObjectDataImpl(cmisObj, allowableActions, relationshipForObject, filter));
		}

		((ObjectListImpl) checkedOutDocs).setObjects(objects);

		//TODO: paging...

		return checkedOutDocs;
	}

}
