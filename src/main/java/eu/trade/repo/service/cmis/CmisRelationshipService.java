package eu.trade.repo.service.cmis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Secured;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.util.Page;

@Transactional
public class CmisRelationshipService implements RelationshipService {

	@Autowired
	private IRelationshipService relationshipService;
	
	@Autowired
	private Security security;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.RelationshipService#getObjectRelationships(java.lang.String, java.lang.String, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.RelationshipDirection, java.lang.String, java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@Secured(Action.CAN_GET_OBJECT_RELATIONSHIPS)
	public ObjectList getObjectRelationships(String repositoryId, @ApplyTo String objectId, Boolean includeSubRelationshipTypes, RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		
		Page<CMISObject> page = relationshipService.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes, relationshipDirection, typeId, maxItems.intValue(), skipCount.intValue());
		
		ObjectListImpl relationships = new ObjectListImpl();
		List<ObjectData> objects = new ArrayList<>();
		
		Set<CMISObject> results = page.getPageElements();
		int count = page.getCount();
		
		relationships.setHasMoreItems(skipCount.intValue() + results.size() < count);
		relationships.setNumItems(BigInteger.valueOf(count));
		
		for(CMISObject cmisObj : results) {

			AllowableActions allowableActions = null;
			if(Boolean.TRUE.equals(includeAllowableActions)) {
				allowableActions = security.getAllowableActions(cmisObj);
			}

			objects.add(new ObjectDataImpl(cmisObj, allowableActions, null, filter));
		}

		relationships.setObjects(objects);

		return relationships;
		
	}
}
