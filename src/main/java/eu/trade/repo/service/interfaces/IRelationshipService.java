package eu.trade.repo.service.interfaces;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.service.util.Node;
import eu.trade.repo.service.util.Page;

public interface IRelationshipService {
	
	Page<CMISObject> getObjectRelationships(String repositoryId, String objectId, RelationshipDirection relationshipDirection);
	Page<CMISObject> getObjectRelationships(String repositoryId, String objectId, IncludeRelationships includeRelationship);
	Page<CMISObject> getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, int maxItems, int skipCount);
	
	Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Set<CMISObject> cmisObjects,IncludeRelationships includeRelationship);
	Map<String, Set<CMISObject>> getRelationshipMappings(String repositoryId, Collection<Node> nodeDescendants, IncludeRelationships includeRelationship);	
	
}
