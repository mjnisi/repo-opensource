package eu.trade.repo.selectors;

import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.util.Constants;



public class ObjectTypePropertySelector extends BaseSelector {

	@Transactional(noRollbackFor={CmisObjectNotFoundException.class})
	public ObjectTypeProperty getObjTypeProperty(String objectType, String propCmisId, String repositoryId) {
		try {
			return getEntityManager()
					.createNamedQuery("objectTypeProperty.by_cmisid", ObjectTypeProperty.class)
					.setParameter("cmisid", propCmisId)
					.setParameter("objectType", objectType)
					.setParameter("repositoryId", repositoryId)
					.getSingleResult();
		} catch (Exception e) {
			throw new CmisObjectNotFoundException(e.getMessage(), e);
		}
	}

	public List<ObjectTypeProperty> getAllObjectTypePropertiesByType (String cmisId) {
		return getEntityManager()
				.createNamedQuery("objectTypeProperty.by_type", ObjectTypeProperty.class)
				.setParameter("cmisId", cmisId)
				.getResultList();
	}

	public List<ObjectTypeProperty> getAllObjectTypeProperties () {
		return getEntityManager()
				.createNamedQuery("objectTypeProperty.all", ObjectTypeProperty.class)
				.getResultList();
	}

	public List<ObjectTypeProperty> getAllFolderProperties () {
		return getAllObjectTypePropertiesByType(Constants.TYPE_CMIS_FOLDER);
	}

	public List<ObjectTypeProperty> getAllDocumentProperties () {
		return getAllObjectTypePropertiesByType(Constants.TYPE_CMIS_DOCUMENT);
	}

	public ObjectTypeProperty getFolderProperty(String propCmisId, String repositoryId) {
		return getObjTypeProperty( Constants.TYPE_CMIS_FOLDER, propCmisId, repositoryId);
	}

	public ObjectTypeProperty getDocumentProperty(String propCmisId, String repositoryId) {
		return getObjTypeProperty(Constants.TYPE_CMIS_DOCUMENT, propCmisId, repositoryId);
	}
	
	
	/**
	 * Returns the count of the ObjectTypeProperty with the cmidId
	 * and not the 
	 * 
	 * @param repositoryId
	 * @param cmisId
	 * @return
	 */
	public Long countObjectTypePropertyById(String repositoryId, String objectTypeCmisId, String objectTypePropertyCmisId) {
		return getEntityManager().createNamedQuery("ObjectTypeProperty.countById", Long.class)
				.setParameter("objectTypeCmisId", objectTypeCmisId)
				.setParameter("objectTypePropertyCmisId", objectTypePropertyCmisId)
				.setParameter("repositoryId", repositoryId)
				.getSingleResult();
	}
	
	/**
	 * Returns the count of the ObjectTypeProperty with the query name
	 * @param repository
	 * @param queryName
	 * @return
	 */
	public Long countObjectTypePropertyByQueryName(String repositoryId, String objectTypeCmisId, String queryName) {
		return getEntityManager().createNamedQuery("ObjectTypeProperty.countByQueryName", Long.class)
				.setParameter("objectTypeCmisId", objectTypeCmisId)
				.setParameter("queryName", queryName)
				.setParameter("repositoryId", repositoryId)
				.getSingleResult();
	}

}
