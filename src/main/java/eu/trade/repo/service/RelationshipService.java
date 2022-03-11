package eu.trade.repo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;

import static org.apache.chemistry.opencmis.commons.enums.RelationshipDirection.*;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.util.Node;
import eu.trade.repo.service.util.Page;

public class RelationshipService extends CMISBaseService implements IRelationshipService {

	@Autowired
	private RepositorySelector repositorySelector;
	
	@Autowired
	private CMISObjectSelector cmisObjSelector;
	
	@Autowired
	private ObjectTypeSelector objTypeSelector;
	
	@Autowired
	private Security security;	
		
	@Override
	public Page<CMISObject> getObjectRelationships(String repositoryId, String cmisObjectId, RelationshipDirection relationshipDirection) {
		return  getObjectRelationships(repositoryId, cmisObjectId, true, relationshipDirection, null, -1, 0);
	}

	@Override
	public Page<CMISObject> getObjectRelationships(String repositoryId, String cmisObjectId, IncludeRelationships includeRelationship) {
		return  getObjectRelationships(repositoryId, cmisObjectId, true, includeRelationship, null, -1, 0);
	}

	@Override
	public Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Set<CMISObject> cmisObjects, IncludeRelationships includeRelationship) {
		return getRelationshipMappings(repositoryId, cmisObjects, true, includeRelationship, null, -1, 0);
	}

	@Override
	public Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Collection<Node> nodeDescendants, IncludeRelationships includeRelationship) {
		return getRelationshipMappings(repositoryId, nodeDescendants, true, includeRelationship, null, -1, 0);
		
	}

	private Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Set<CMISObject> cmisObjects, Boolean includeSubRelationshipTypes, IncludeRelationships includeRelationship, String typeId, int maxItems, int skipCount) {
 		Page<CMISObject> relationships = getObjectRelationships(repositoryId, getCmisIds(cmisObjects), includeSubRelationshipTypes, includeRelationship, typeId, maxItems, skipCount);
 		return findInMemoryRelationshipMappings(relationships.getPageElements(), includeRelationship);
	}
 	
	private Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Collection<Node> nodeDescendants, Boolean includeSubRelationshipTypes, IncludeRelationships includeRelationship, String typeId, int maxItems, int skipCount) {
 		Page<CMISObject> relationships = getObjectRelationships(repositoryId, getCmisIds(nodeDescendants), includeSubRelationshipTypes, includeRelationship, typeId, maxItems, skipCount);
 		return findInMemoryRelationshipMappings(relationships.getPageElements(), includeRelationship);
	}
	
	private Page<CMISObject> getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, IncludeRelationships includeRelationship, String typeId, int maxItems, int skipCount) {
		
		return getObjectRelationships(repositoryId, Collections.singleton(objectId), includeSubRelationshipTypes, includeRelationship, typeId, maxItems, skipCount);
	}
	
	private Page<CMISObject> getObjectRelationships(String repositoryId, Set<String> objectIds, Boolean includeSubRelationshipTypes, IncludeRelationships includeRelationship, String typeId, int maxItems, int skipCount) {
		// detect RelationshipDirection value
		RelationshipDirection relationshipDirection = getRelationDirection(includeRelationship);
		// validate RelationshipDirection value
		if(relationshipDirection == null) {
			// the client doesn't require getting the actual list of relationships, so return object page with empty collection;
			return new Page(Collections.emptySet(), 0);
		}
		
		return getObjectRelationships(repositoryId, objectIds, includeSubRelationshipTypes, relationshipDirection, typeId, maxItems, skipCount);
	}
	
	@Override
	public Page<CMISObject> getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, int maxItems, int skipCount) {
		return getObjectRelationships(repositoryId, Collections.singleton(objectId), includeSubRelationshipTypes, relationshipDirection, typeId, maxItems, skipCount);
	}	
	
	private Page<CMISObject> getObjectRelationships(String repositoryId, Set<String> objectIds, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, int maxItems, int skipCount) {
		RequestRelationship requestRelationship = getRequestRelationship(repositoryId, includeSubRelationshipTypes, relationshipDirection, typeId);
		Set<CMISObject> results = null;
		int count = 0;
		if(requestRelationship.getObjectTypeCmisIds() != null) {							
			results = cmisObjSelector.getObjectsByObjectTypesPropertyTypesAndValue(repositoryId, objectIds, 
					requestRelationship.getEndPoints(), requestRelationship.getObjectTypeCmisIds(), requestRelationship.getPrincipalIds(), requestRelationship.getPermissions(), maxItems, skipCount);
		
			count = cmisObjSelector.getObjectsCountByObjectTypesPropertyTypesAndValue(repositoryId, objectIds,
					requestRelationship.getEndPoints(), requestRelationship.getObjectTypeCmisIds(), requestRelationship.getPrincipalIds(), requestRelationship.getPermissions());
			
		} else {
			results = cmisObjSelector.getObjectsByPropertyTypesAndValue(repositoryId, objectIds, 
					requestRelationship.getEndPoints(), requestRelationship.getPrincipalIds(), requestRelationship.getPermissions(), maxItems, skipCount);
			count = cmisObjSelector.getObjectsCountByPropertyTypesAndValue(repositoryId, objectIds,
					requestRelationship.getEndPoints(), requestRelationship.getPrincipalIds(), requestRelationship.getPermissions());
		}
		
		return new Page<CMISObject>(results, count);
	}
		
	private void collectObjectTypeDescendantCmisIds(String repositoryId, ObjectType ot, Set<String> objectTypeCmisIds) {
		
		Set<ObjectType> children = objTypeSelector.getObjectTypeChildren(repositoryId, ot.getCmisId(), false);
		
		for(ObjectType child : children) {
			objectTypeCmisIds.add(child.getCmisId());
			collectObjectTypeDescendantCmisIds(repositoryId, child, objectTypeCmisIds);
		}
	}
	
	/**
	 * @param relationshipDirection
	 * @return a set collection with requested directions
	 */
	private Set<String> getEndPoints(final RelationshipDirection relationshipDirection) {
		return new HashSet<String>() {
			
			private static final long serialVersionUID = 1L;

			{
				if(relationshipDirection == SOURCE || relationshipDirection == EITHER) {
					add(PropertyIds.SOURCE_ID);
				}
				if(relationshipDirection == TARGET || relationshipDirection == EITHER) {
					add(PropertyIds.TARGET_ID);
				}
			}
			
		};
	}
	
	/**
	 * prepare the loading of relationship request parameter before triggering the actual loading
	 * @param repositoryId
	 * @param includeSubRelationshipTypes
	 * @param relationshipDirection
	 * @param typeId
	 * @return requestRelationship fully prepared
	 */
	private RequestRelationship getRequestRelationship(String repositoryId, Boolean includeSubRelationshipTypes, final RelationshipDirection relationshipDirection, String typeId) {
		
		RequestRelationship requestRelationship = new RequestRelationship();
		
		requestRelationship.setEndPoints(getEndPoints(relationshipDirection)); 
				
		Repository repository = repositorySelector.getRepository(repositoryId);
		
		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(repository.getAcl())) {
			
			requestRelationship.setPrincipalIds(security.getCallContextHolder().getPrincipalIds());
			requestRelationship.setPermissions(security.getPermissionIds(repository.getCmisId(), Action.CAN_GET_PROPERTIES));
		}			
		
		requestRelationship.setObjectTypeCmisIds(getObjectTypeCmisIds(includeSubRelationshipTypes, repositoryId, typeId));
				
		return requestRelationship;
	}
	
	/**
	 * @param includeSubRelationshipTypes
	 * @param repositoryId
	 * @param typeId
	 * @return set of objectType ids
	 */
	private Set<String> getObjectTypeCmisIds(boolean includeSubRelationshipTypes, String repositoryId, String typeId) {
		Set<String> objectTypeCmisIds = null;
		if(typeId != null) {
			objectTypeCmisIds = new HashSet<>();
			
			ObjectType ot = null;
			
			try {
				ot = objTypeSelector.getObjectTypeByCmisId(repositoryId, typeId);
				
			} catch(NoResultException nrex) {
				throw new CmisObjectNotFoundException("Specified Object-Type: " + typeId + " not found!", nrex);
			}
									
			objectTypeCmisIds.add(ot.getCmisId());
			
			if(includeSubRelationshipTypes) {
				collectObjectTypeDescendantCmisIds(repositoryId, ot, objectTypeCmisIds);
			}
							
		} 
		
		return objectTypeCmisIds;
	}
	
	/**
	 * @author stansil
	 * inner class that allows for loading the specific request parameters before triggering the relationship(s) loading
	 */
	class RequestRelationship {
		
		private Set<String> endPoints;
		private Set<String> principalIds;
		private Set<Integer> permissions;
		private Set<String> objectTypeCmisIds;
		
		public Set<String> getEndPoints() {
			return endPoints;
		}
		public void setEndPoints(Set<String> endPoints) {
			this.endPoints = endPoints;
		}
		
		public Set<String> getPrincipalIds() {
			return principalIds;
		}
		
		public void setPrincipalIds(Set<String> principalIds) {
			this.principalIds = principalIds;
		}
		
		public Set<Integer> getPermissions() {
			return permissions;
		}
		
		public void setPermissions(Set<Integer> permissions) {
			this.permissions = permissions;
		}
		
		public Set<String> getObjectTypeCmisIds() {
			return objectTypeCmisIds;
		}
		
		public void setObjectTypeCmisIds(Set<String> objectTypeCmisIds) {
			this.objectTypeCmisIds = objectTypeCmisIds;
		}				
	}
	
	private RelationshipDirection getRelationDirection(IncludeRelationships includeRelationships) {
		RelationshipDirection relationshipDirection = null;
		if (includeRelationships != null && IncludeRelationships.NONE != includeRelationships) {

			relationshipDirection = includeRelationships == IncludeRelationships.BOTH ? RelationshipDirection.EITHER
															: includeRelationships == IncludeRelationships.SOURCE ? RelationshipDirection.SOURCE
															: RelationshipDirection.TARGET;
		}
		return relationshipDirection;
	}
	
	private Map<String, Set<CMISObject>> findInMemoryRelationshipMappings(Set<CMISObject> relationships, IncludeRelationships includeRelationship) {
		Map<String, Set<CMISObject>> relationshipMappings = new HashMap();
		if(relationships != null && includeRelationship != null) {
			for (CMISObject relationship : relationships) {						
				switch (includeRelationship) {
				 	case BOTH 	: 			 	
				 		updateRelationshipMappings(getAsociatedCmisIdFromRelationship(relationship, PropertyIds.SOURCE_ID), relationship, relationshipMappings);
				 		updateRelationshipMappings(getAsociatedCmisIdFromRelationship(relationship, PropertyIds.TARGET_ID), relationship, relationshipMappings);
				 		break;
				 	case SOURCE	:			 		
				 		updateRelationshipMappings(getAsociatedCmisIdFromRelationship(relationship, PropertyIds.SOURCE_ID), relationship, relationshipMappings);
				 		break;
				 	case TARGET	:			 		
				 		updateRelationshipMappings(getAsociatedCmisIdFromRelationship(relationship, PropertyIds.TARGET_ID), relationship, relationshipMappings);
				 		break;
				 	case NONE	: // do nothing
				}
									
			}
		}
		return relationshipMappings;
	}
	
	private Set<String> getCmisIds(Set<CMISObject> cmisObjects) {
		Set<String> cmisIds = new HashSet();
		if(cmisIds != null) {
			for(CMISObject cmisObject : cmisObjects) {
				cmisIds.add(cmisObject.getCmisObjectId());
			}
		}
		return cmisIds;
	}
	
	private Set<String> getCmisIds(Collection<Node> nodeObjects) {
		Set<String> cmisIds = new HashSet();
		if(nodeObjects != null) {
			for(Node nodeObject : nodeObjects) {
				cmisIds.add(nodeObject.getCmisObject().getCmisObjectId());
			}
		}		
		return cmisIds;
	}
	
	private void updateRelationshipMappings(String cmisId, CMISObject relationship, Map<String, Set<CMISObject>> relationshipMappings) {
		
		if (relationshipMappings.get(cmisId) == null) {
			relationshipMappings.put(cmisId, new HashSet());
		}
		relationshipMappings.get(cmisId).add(relationship);
	}	
	
	private String getAsociatedCmisIdFromRelationship(CMISObject relationship, String propertyId) {
		String propertyCmisId = null;
		try {
			Property property = relationship.getProperty(propertyId);
			propertyCmisId = property.getTypedValue();
			
		} catch (PropertyNotFoundException e) {
			// TODO we will see what type should be thrown
			throw new CmisRuntimeException("The expected property " + propertyId + " wasn't found for the object " + relationship.getCmisObjectId(), e);
		}		
		return propertyCmisId;
	}

}
