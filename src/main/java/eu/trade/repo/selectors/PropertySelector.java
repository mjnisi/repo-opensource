package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.Property;

public class PropertySelector extends BaseSelector {
    public List<Property> getPropertiesOfType(String objectTypePropCmisId) {
    	return getEntityManager()
    			.createNamedQuery("property.by_type", Property.class)
    			.setParameter("objectTypePropCmisId", objectTypePropCmisId)
    			.getResultList();
    }
    
    public List<Property> getPropertiesOfTypes(Integer objectId, List<String> objectTypePropCmisIdList) {
    	return getEntityManager()
    			.createNamedQuery("property.by_object_and_types", Property.class)
    			.setParameter("objectId", objectId)
    			.setParameter("objectTypePropCmisIdList", objectTypePropCmisIdList)
    			.getResultList();
    }
    
    public List<Property> getAllProperties() {
    	return getEntityManager()
    			.createNamedQuery("property.all", Property.class)
    			.getResultList();
    }

    public String getPropertyValue(String cmisObjectId, String objectTypePropId) {
    	return getEntityManager()
    			.createNamedQuery("property.valueOf", String.class)
    			.setParameter("cmisObjectId", cmisObjectId)
    			.setParameter("objectTypePropId", objectTypePropId)
    			.getSingleResult();
    }
    
    public Property getObjectPropertyByQueryName(String cmisObjectId, String queryName) {
    	
    	return getEntityManager()
    			.createNamedQuery("property.by_cmisObjectId_and_queryName", Property.class)
    			.setParameter("cmisObjectId", cmisObjectId)
    			.setParameter("queryName", queryName)
    			.getSingleResult();
    }
}