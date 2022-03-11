package eu.trade.repo.selectors;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eu.trade.repo.model.ObjectType;

public class ObjectTypeSelector extends BaseSelector {
	private static final String REPO_ID = "repositoryId";
	private static final String CMIS_ID = "cmisId";
	private static final String QUERY_NAME = "queryname";

	/**
	 * NOTE: Throws UNCHECKED NoResultException if ObjectType doesn't exist
	 * 
	 * @param repositoryId
	 * @param queryname
	 * @param resolveParents
	 * @return
	 */
	public ObjectType getObjectTypeByQueryName(String repositoryId, String queryname, boolean resolveParents) {

		ObjectType objectType = getEntityManager().createNamedQuery("ObjectType.objectTypeByQueryName", ObjectType.class)
				.setParameter(QUERY_NAME, queryname)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();

		if(resolveParents) {
			resolveParent(objectType);
		}

		return objectType;
	}

	public ObjectType getObjectTypeByCmisId(String repositoryId, String cmisId) {
		return getEntityManager()
				.createNamedQuery("ObjectType.objectTypeByCmisid", ObjectType.class)
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();
	}

	/**
	 * 
	 * @param repositoryId
	 * @param cmisId
	 * @return
	 */
	public ObjectType getObjectTypeByCmisIdWithProperties(String repositoryId, String cmisId) {
		ObjectType objectType = getEntityManager()
				.createNamedQuery("ObjectType.objectTypeByCmisid_with_base_parent_properties", ObjectType.class)
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();
		objectType.getObjectTypeProperties().size();
		objectType.getObjectTypeRelationships().size();
		return objectType;
	}

	/**
	 * Returns the set of the object type's children.
	 * <p>
	 * If <code>includeProperties</code> is true, then the object has to load all of its properties (direct or inherited)
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param cmisId {@link String} The {@link ObjectType} cmis id.
	 * @param includeProperties boolean If true, then the object type must be loaded with the property type definitions.
	 * @return {@link Set<ObjectType>} The object type's children.
	 */
	public Set<ObjectType> getObjectTypeChildren(String repositoryId, String cmisId, boolean includeProperties) {
		String namedQuery = includeProperties ? "ObjectType.children_with_base_parent_properties" : "ObjectType.children";
		List<ObjectType> resultList = getEntityManager().createNamedQuery(namedQuery, ObjectType.class)
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.getResultList();
		if (includeProperties) {
			for (ObjectType objectType : resultList) {
				resolveParent(objectType);
			}
		}
		return new LinkedHashSet<>(resultList);
	}

	private void resolveParent(ObjectType objectType) {
		ObjectType parent = objectType.getParent();
		if (parent != null) {
			// Access to the set size to resolve the lazy association.
			parent.getObjectTypeProperties().size();
			resolveParent(parent);
		}
	}

	public Set<ObjectType> getObjectTypes(String repositoryId, boolean includeLazyLoadedDependencies) {
		List<ObjectType> resultList = (includeLazyLoadedDependencies ? getEntityManager().createNamedQuery("ObjectType.alltypes_with_dependencies", ObjectType.class) : getEntityManager().createNamedQuery("ObjectType.alltypes", ObjectType.class))
				.setParameter(REPO_ID, repositoryId)
				.getResultList();
		return new LinkedHashSet<>(resultList);
	}

	/**
	 * Gets the base types defined in the repository
	 * 
	 * @param repositoryId
	 * @return
	 */
	public Set<ObjectType> getBaseObjectTypes(String repositoryId) {
		List<ObjectType> resultList = getEntityManager().createNamedQuery(
				"ObjectType.baseTypes", ObjectType.class)
					.setParameter(REPO_ID, repositoryId)
					.getResultList();
		return new LinkedHashSet<>(resultList);
	}

	
	/**
	 * TODO Review
	 * @param repositoryId
	 * @param cmisId
	 * @param includeLazyLoadedDependencies
	 * @param offset
	 * @param limit
	 * @return
	 */
	public Set<ObjectType> getObjectTypeChildren(String repositoryId, String cmisId, boolean includeLazyLoadedDependencies, int offset, int limit) {
		List<ObjectType> resultList = (includeLazyLoadedDependencies ? getEntityManager().createNamedQuery("ObjectType.children_with_dependencies", ObjectType.class) : getEntityManager().createNamedQuery("ObjectType.children", ObjectType.class))
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();
		return new LinkedHashSet<>(resultList);
	}

	/**
	 * Count the number of subtypes of a given repository/type
	 * 
	 * @param repositoryId
	 * @param cmisId
	 * @return
	 */
	public Long countObjectTypeChildren(String repositoryId, String cmisId) {
		return getEntityManager().createNamedQuery("ObjectType.countChildren", Long.class)
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();
	}

	
	/**
	 * Count the number of types with the CMIS id 
	 * 
	 * @param repositoryId
	 * @param cmidId
	 * @return
	 */
	public Long countObjectTypeByCmisId(String repositoryId, String cmisId) {
		return getEntityManager().createNamedQuery("ObjectType.countyByCmisId", Long.class)
				.setParameter(CMIS_ID, cmisId)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();
	}

	/**
	 * Count the number of types with the query name 
	 * 
	 * @param repositoryId
	 * @param queryName
	 * @return
	 */
	public Long countObjectTypeByQueryName(String repositoryId, String queryName) {
		return getEntityManager().createNamedQuery("ObjectType.countyByQueryName", Long.class)
				.setParameter(QUERY_NAME, queryName)
				.setParameter(REPO_ID, repositoryId)
				.getSingleResult();
	}

}
