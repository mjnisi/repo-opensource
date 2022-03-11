package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.ObjectTypeRelationship;

public class ObjectTypeRelationshipSelector extends BaseSelector {
    public List<ObjectTypeRelationship> getObjectTypeRelationships() {
    	return getEntityManager()
    			.createNamedQuery("ObjectTypeRelationship.all", ObjectTypeRelationship.class)
    			.getResultList();
    }

    public List<ObjectTypeRelationship> getObjectTypeRelationships(String cmisId) {
    	return getEntityManager()
    			.createNamedQuery("ObjectTypeRelationship.by_cmisid", ObjectTypeRelationship.class)
    			.setParameter("cmisId", cmisId)
    			.getResultList();
    }

}
