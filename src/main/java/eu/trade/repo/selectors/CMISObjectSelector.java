package eu.trade.repo.selectors;

import static eu.trade.repo.util.Constants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.view.VersionType;

public class CMISObjectSelector extends BaseSelector {
	private static final String CMIS_OBJECT_ID = "cmisObjectId";
	private static final String REPOSITORY_ID = "repositoryId";
	private static final String OBJECT_ID = "objectId";
	private static final String OBJECT_TYPE_ID = "objectTypeId";
	private static final String PS_LIMIT = "orm.preparedStatementMaxParameters";
	
	private static final String SECURED = ".Secured";
	private static final String PERMISSION_IDS = "permissionIds";
	private static final String PRINCIPAL_IDS = "principalIds";
	private static final String PROPERTY_TYPES = "propertyTypes";
	private static final String VALUE = "value";
	private static final String VALUES = "values";
	private static final String VERSION_TYPES = "versionTypes";
	private static final String OBJTYPE_PROPERTY= "objectTypeProperty";
	
	@Autowired
	private Configuration combinedConfig;

	//INTERFACE
	/**
	 * Returns object parents, filtered by ACL.
	 * 
	 * @param cmisObjectId The CMISObject id.
	 * @param principalIds
	 * @param permissionIds
	 * @return {@link List} The List of parents of the specified CMISObject
	 */
	public CMISObject getObjectWithParents(String cmisObjectId, Set<String> principalIds, Set<Integer> permissionIds) {
		if (principalIds == null || permissionIds == null) { //ADMIN USER
			return getEntityManager().createNamedQuery("cmisObject.only_parents", CMISObject.class)
					.setParameter(CMIS_OBJECT_ID, cmisObjectId)
					.getSingleResult();
		}
		
		return getEntityManager().createNamedQuery("cmisObject.only_parentsACL", CMISObject.class)
				.setParameter(CMIS_OBJECT_ID, cmisObjectId)
				.setParameter(PERMISSION_IDS, permissionIds)
				.setParameter(PRINCIPAL_IDS, principalIds)
				.getSingleResult();
	}
	
	/**
	 * Executes query: select obj from CMISObject obj 
	 *                                 left join obj.properties property
	 *                                 left join fetch obj.objectType objectType
	 *                                 left join fetch objectType.repository repository
	 *                                 where
	 *                                 repository.cmisId = :repositoryId and
	 *                                 property.value = :value and
	 *                                 property.objectTypeProperty.cmisId in (:propertyTypes)
	 *                                
	 * @param repositoryId
	 * @param value
	 * @param propertyTypeCmisIds
	 * @return {@link Set<CMISObject>} The objects which have the specified value on any of the specified properties.
	 */
	public Set<CMISObject> getObjectsByPropertyTypesAndValue(String repositoryId, Set<String> values, Set<String> propertyTypeCmisIds, Set<String> principalIds, Set<Integer> permissions, int maxItems, int skipCount) {
		
		String namedQuery = "CmisObject.by_propertyTypesAndValues";
		boolean isSecured = principalIds != null && permissions != null;
		
		TypedQuery<CMISObject> query = getEntityManager().createNamedQuery(namedQuery + (isSecured?SECURED:""), CMISObject.class)
				                                     .setParameter(REPOSITORY_ID, repositoryId)
				                                     .setParameter(VALUES, values)
				                                     .setParameter(PROPERTY_TYPES, propertyTypeCmisIds);
		
		if(isSecured) {
			query.setParameter(PRINCIPAL_IDS, principalIds).setParameter(PERMISSION_IDS, permissions);
		}
		
		if(maxItems > 0) {
			query.setMaxResults(maxItems);
			if(skipCount > 0) {
				query.setFirstResult(skipCount);
			}
		}

		return new LinkedHashSet<>(query.getResultList());
	}
	
	/**
	 * @param repositoryId
	 * @param value
	 * @param propertyTypeCmisIds
	 * @param principalIds
	 * @param permissions
	 * @return {@link Integer}
	 */
	public int getObjectsCountByPropertyTypesAndValue(String repositoryId, Set<String> values, Set<String> propertyTypeCmisIds, Set<String> principalIds, Set<Integer> permissions) {
		
		String namedQuery = "CmisObject.countBy_propertyTypesAndValues";
		boolean isSecured = principalIds != null && permissions != null;
		
		TypedQuery<Number> query = getEntityManager().createNamedQuery(namedQuery + (isSecured?SECURED:""), Number.class)
				                                     .setParameter(REPOSITORY_ID, repositoryId)
				                                     .setParameter(VALUES, values)
				                                     .setParameter(PROPERTY_TYPES, propertyTypeCmisIds);
		
		if(isSecured) {
			query.setParameter(PRINCIPAL_IDS, principalIds).setParameter(PERMISSION_IDS, permissions);
		}
		
		return query.getSingleResult().intValue();
	}
	
	/**
	 * 
	 * @param repositoryId
	 * @param value
	 * @param propertyTypeCmisIds
	 * @param objectTypeCmisIds
	 * @return {@link Set<CMISObject>}
	 */
	public Set<CMISObject> getObjectsByObjectTypesPropertyTypesAndValue(String repositoryId, Set<String> values, Set<String> propertyTypeCmisIds, Set<String> objectTypeCmisIds, Set<String> principalIds, Set<Integer> permissions, int maxItems, int skipCount) {
		
		String namedQuery = "CmisObject.by_ObjectType(s)PropertyTypesAndValues";
		boolean isSecured = principalIds != null && permissions != null;
		
		TypedQuery<CMISObject> query = getEntityManager().createNamedQuery(namedQuery + (isSecured?SECURED:""), CMISObject.class)
				                                         .setParameter(REPOSITORY_ID, repositoryId)
				                                         .setParameter(VALUES, values)
				                                         .setParameter(PROPERTY_TYPES, propertyTypeCmisIds)
				                                         .setParameter("objectTypes", objectTypeCmisIds);
		
		if(isSecured) {
			query.setParameter(PRINCIPAL_IDS, principalIds).setParameter(PERMISSION_IDS, permissions);
		}
		
		if(maxItems > 0) {
			query.setMaxResults(maxItems);
			if(skipCount > 0) {
				query.setFirstResult(skipCount);
			}
		}

		return new LinkedHashSet<>(query.getResultList());
	}
	
	/**
	 * 
	 * @param repositoryId
	 * @param value
	 * @param propertyTypeCmisIds
	 * @param objectTypeCmisIds
	 * @param principalIds
	 * @param permissions
	 * @return {@link Integer}
	 */
	public int getObjectsCountByObjectTypesPropertyTypesAndValue(String repositoryId, Set<String> values, Set<String> propertyTypeCmisIds, Set<String> objectTypeCmisIds, Set<String> principalIds, Set<Integer> permissions) {
		
		String namedQuery = "CmisObject.countBy_ObjectType(s)PropertyTypesAndValues";
		boolean isSecured = principalIds != null && permissions != null;
		
		TypedQuery<Number> query  = getEntityManager().createNamedQuery(namedQuery + (isSecured?SECURED:""), Number.class)
				                                      .setParameter(REPOSITORY_ID, repositoryId)
				                                      .setParameter(VALUES, values)
				                                      .setParameter(PROPERTY_TYPES, propertyTypeCmisIds)
				                                      .setParameter("objectTypes", objectTypeCmisIds);
		
		if(isSecured) {
			query.setParameter(PRINCIPAL_IDS, principalIds).setParameter(PERMISSION_IDS, permissions);
		}
		
		return query.getSingleResult().intValue();
	}
	
	
	/**
	 * Get the Children objects of a given object
	 * 
	 * @param cmisObjectId
	 * @param maxItems
	 * @param skipCount
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	
	public Set<CMISObject> getObjectChildren(String cmisObjectId, int maxItems, int skipCount, Set<VersionType> versionTypes, String orderColumn, boolean isAsc) {
		TypedQuery<Integer> query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenIds", orderColumn, isAsc), Integer.class);
		}
		else {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenIdsVersion", orderColumn, isAsc), Integer.class)
					.setParameter(VERSION_TYPES, versionTypes);
		}
		setOrderbyParameters(query, orderColumn);
		List<Integer> ids = query
				.setMaxResults(maxItems)
				.setFirstResult(skipCount)
				.setParameter(OBJECT_ID, cmisObjectId).getResultList();

		return getObjectsWhereIdInListAcl(ids);
	}

	/**
	 * Get the Children objects of a given object
	 * 
	 * @param cmisObjectId {@link String} The object's cmis id.
	 * @param maxItems
	 * @param skipCount
	 * @param principalIds {@link Set<String>} Current user's set of principal ids. If null, then no security restrictions will be applied.
	 * @param permissionIds {@link Set<String>} The set of the permissions ids that satisfies certain concrete action.
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	public Set<CMISObject> getObjectChildren(String cmisObjectId, int maxItems, int skipCount, Set<String> principalIds, Set<Integer> permissionIds, Set<VersionType> versionTypes, String orderColumn, boolean isAsc) {
		if (principalIds == null) {
			return getObjectChildren(cmisObjectId, maxItems, skipCount, versionTypes, orderColumn, isAsc);
		}
		TypedQuery<Integer> query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenIdsAcl", orderColumn, isAsc), Integer.class);
		}
		else {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenIdsAclVersion", orderColumn, isAsc), Integer.class)
					.setParameter(VERSION_TYPES, versionTypes);
		}
		setOrderbyParameters(query, orderColumn);
		List<Integer> ids = query
				.setMaxResults(maxItems)
				.setFirstResult(skipCount)
				.setParameter(OBJECT_ID, cmisObjectId)
				.setParameter(PRINCIPAL_IDS, principalIds)
				.setParameter(PERMISSION_IDS, permissionIds)
				.getResultList();

		return getObjectsWhereIdInListAcl(ids);
	}


	/**
	 * Get the Children objects of a given object
	 * 
	 * @param cmisObjectId
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<CMISObject> getObjectChildren(String cmisObjectId, Set<VersionType> versionTypes, String orderColumn, boolean isAsc) {
		Query query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildren", orderColumn, isAsc));
		}
		else {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenVersion", orderColumn, isAsc))
					.setParameter(VERSION_TYPES, versionTypes);
		}
		setOrderbyParameters(query, orderColumn);
		return new LinkedHashSet<>(query
				.setParameter(OBJECT_ID, cmisObjectId)
				.getResultList());
	}

	/**
	 * Get the Children objects of a given object
	 * 
	 * @param cmisObjectId {@link String} The object's cmis id.
	 * @param principalIds {@link Set<String>} Current user's set of principal ids. If null, then no security restrictions will be applied.
	 * @param permissionIds {@link Set<String>} The set of the permissions ids that satisfies certain concrete action.
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<CMISObject> getObjectChildren(String cmisObjectId, Set<String> principalIds, Set<Integer> permissionIds, Set<VersionType> versionTypes, String orderColumn, boolean isAsc) {
		if (principalIds == null) {
			return getObjectChildren(cmisObjectId, versionTypes, orderColumn, isAsc);
		}
		Query query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenAcl", orderColumn, isAsc));
		}
		else {
			query = getEntityManager()
					.createNamedQuery(getOrderedQueryString("CmisObject.getChildrenAclVersion", orderColumn, isAsc))
					.setParameter(VERSION_TYPES, versionTypes);
		}
		setOrderbyParameters(query, orderColumn);
		return new LinkedHashSet<>(query
				.setParameter(OBJECT_ID, cmisObjectId)
				.setParameter(PRINCIPAL_IDS, principalIds)
				.setParameter(PERMISSION_IDS, permissionIds)
				.getResultList());
	}

	/**
	 * Get every Version of a given object (ordered by cmis:creationDate) including the PWC
	 * 
	 * @param versionSeriesId {@link String} The version series id.
	 * @param vseriesPropId {@link String} The {@link Constants#CMISPROP_VERSIONSERIESID} id.
	 * @param creationDatePropId {@link String} The {@link Constants#CMISPROP_CREATIONDATE} id.
	 * @return {@link List<CMISObject>} Every Version of a given object (ordered by cmis:creationDate) including the PWC
	 */
	public List<CMISObject> getAllVersions(String versionSeriesId, int vseriesPropId, int creationDatePropId) {
		return getEntityManager().
				createNamedQuery("CmisObject.getAllVersions", CMISObject.class)
				.setParameter("versionSeriesId",versionSeriesId)
				.setParameter("vseriesPropId",vseriesPropId)
				.setParameter("lastModDatePropId",creationDatePropId)
				.getResultList();
	}

	/**
	 * Return the number of children of this object
	 * 
	 * @param cmisObjectId
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	public int getObjectChildrenCount(String cmisObjectId, Set<VersionType> versionTypes) {
		TypedQuery<Number> query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery("CmisObject.getChildrenCount", Number.class);
		}
		else {
			query = getEntityManager()
					.createNamedQuery("CmisObject.getChildrenCountVersion", Number.class)
					.setParameter(VERSION_TYPES, versionTypes);
		}

		return query
				.setParameter(OBJECT_ID, cmisObjectId)
				.getSingleResult().intValue();
	}

	/**
	 * Return the number of children of this object
	 * 
	 * @param cmisObjectId
	 * @param principalIds {@link Set<String>} Current user's set of principal ids. If null, then no security restrictions will be applied.
	 * @param permissionIds {@link Set<String>} The set of the permissions ids that satisfies certain concrete action.
	 * @param versionTypes {@link Set<VersionType>} The version types to restrict the search. Null if the search is not restricted by version types.
	 * @return
	 */
	public int getObjectChildrenCount(String cmisObjectId, Set<String> principalIds, Set<Integer> permissionIds, Set<VersionType> versionTypes) {
		if (principalIds == null) {
			return getObjectChildrenCount(cmisObjectId, versionTypes);
		}
		TypedQuery<Number> query;
		if (versionTypes == null) {
			query = getEntityManager()
					.createNamedQuery("CmisObject.getChildrenCountAcl", Number.class);
		}
		else {
			query = getEntityManager()
					.createNamedQuery("CmisObject.getChildrenCountAclVersion", Number.class)
					.setParameter(VERSION_TYPES, versionTypes);
		}

		return query
				.setParameter(OBJECT_ID, cmisObjectId)
				.setParameter(PRINCIPAL_IDS, principalIds)
				.setParameter(PERMISSION_IDS, permissionIds)
				.getSingleResult().intValue();
	}

	/**
	 * Returns the root folder id of the specified repository
	 * 
	 * @param repositoryId The repository id.
	 * @return {@link String} The root folder id of the specified repository
	 */
	public String getRootFolderId(String repositoryId) {
		return getEntityManager()
				.createNamedQuery("CmisObject.getRooFolderId", String.class)
				.setParameter(REPOSITORY_ID, repositoryId)
				.getSingleResult();
	}

	/**
	 * Returns the cmis object with the specified cmis path or cmis id that belongs to the repository specified by the repositoryId.
	 * <p>
	 * This method will use the {@link CMISObjectCache} and will return the object loaded with its ACLs, its object type and its repository.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param pathOrId {@link String} The cmis object path or id.
	 * 
	 * @return {@link CMISObject} The cmis object
	 * @throws CmisObjectNotFoundException When the object is not found.
	 */
	public CMISObject getCMISObject(String repositoryId, String pathOrId) {
		if (repositoryId == null || pathOrId == null) {
			throw new IllegalArgumentException("The repository id or cmisObject path or id cannot be null.");
		}
		CMISObject cmisObject = null;
		if (pathOrId.contains(CMIS_PATH_SEP)) {
			cmisObject = resolveByCmisPath(repositoryId, pathOrId);
		}
		else {
			cmisObject = resolveByCmisObjectId(pathOrId);
		}
		if (notFound(cmisObject, repositoryId)) {
			throw new CmisObjectNotFoundException(String.format("Object %s not found in the repository %s.", pathOrId, repositoryId));
		}
		return cmisObject;
	}

	/**
	 * Returns true if the object is null or if its repository id doesn't equals the parameter.
	 * #
	 * @param cmisObject {@link CMISObject} The cmis object.
	 * @param repositoryId
	 * @return true if the object is null or if its repository id doesn't equals the parameter.
	 */
	private boolean notFound(CMISObject cmisObject, String repositoryId) {
		if (cmisObject == null) {
			return true;
		}
		// Validate the object's repository id.
		String cmisObjectRepositoryId = cmisObject.getObjectType().getRepository().getCmisId();
		return !repositoryId.equals(cmisObjectRepositoryId);
	}

	/**
	 * Returns the object with all its lazy associations resolved/fetched.
	 * #
	 * @param cmisId {@link String} The cmis object cmis id.
	 * @return {@link CMISObject} The object uniquely identified by cmisId, with all associations fetched .
	 */
	public CMISObject loadCMISObject(String cmisId) {
		if (cmisId == null) {
			throw new IllegalArgumentException("The id cannot be null.");
		}
		return getObjectByQuery(cmisId, "cmisObject.with_dependencies");
	}

	/**
	 * Returns the object uniquely identified by the path or id.
	 * #
	 * @param cmisId The CMISObject id.
	 * @return {@link CMISObject} The cmis object
	 */
	public CMISObject getCMISObjectWithChildren(String cmisId) {
		return getObjectByQuery(cmisId, "cmisObject.only_children");
	}

	/**
	 * Returns the object uniquely identified by the path or id.
	 * The policies association resolved
	 * #
	 * @param cmisId The CMISObject id.
	 * @return {@link CMISObject} The cmis object
	 */
	public CMISObject getCMISObjectWithPolicies(String cmisId) {
		return getObjectByQuery(cmisId, "cmisObject.only_policies");
	}

	/**
	 * Returns the object uniquely identified by the path or id with the children and properties resolved.
	 * #
	 * @param cmisId The CMISObject id.
	 * @return {@link CMISObject} The cmis object
	 */
	public CMISObject getCMISObjectWithChildrenAndProperties(String cmisId) {
		return getObjectByQuery(cmisId, "cmisObject.only_children_properties");
	}


	/**
	 * Returns the object having the specified unique value for the specified property type in the specified repository.
	 * <p>
	 * NOTE: Only works with unique properties within the same repository.
	 * 
	 * @param repositoryId {@link String} The repository id.
	 * @param value {@link String} The property value.
	 * @param propertyTypeCmisId {@link String} The property's type CMIS id.
	 * 
	 * @return The object having the specified unique value for the specified property type.
	 */
	private CMISObject getObjectByPropertyValue(String repositoryId, String value, String propertyTypeCmisId) {
		try {
			List<CMISObject> ans = getEntityManager()
					.createNamedQuery("CmisObject.by_propertyTypeAndValue", CMISObject.class)
					.setParameter(REPOSITORY_ID, repositoryId)
					.setParameter(VALUE, value)
					.setParameter("propertyTypeCmisId", propertyTypeCmisId)
					.getResultList();
			if (ans.size() <= 0) {
				throw new CmisObjectNotFoundException(String.format("Cannot find Object described by property : [id: %s, value: %s].", propertyTypeCmisId, value));
			}

			return ans.get(0);
		} catch (NoResultException e) {
			throw new CmisObjectNotFoundException(String.format("Cannot find Object described by property : [id: %s, value: %s].", propertyTypeCmisId, value), e);
		}
	}


	public boolean hasStream(Integer objectId) {
		Number streamNumber;
		try {
			streamNumber = getEntityManager()
					.createNamedQuery("CmisObject.hasStream-native", Number.class)
					.setParameter(OBJECT_ID, objectId)
					.getSingleResult();
		} catch (NoResultException e) {
			return false;
		}
		return streamNumber.longValue() > 0;
	}

	public List<?> getAllObjectsCount() {
		return getEntityManager()
				.createNamedQuery("CmisObject.getAllObjectsCount")
				.getResultList();
	}

	public List<?> getAllObjectsCountPerRepo() {
		return getEntityManager()
				.createNamedQuery("CmisObject.getObjectsCountPerRepo")
				.getResultList();
	}

	public List<?> getObjectsCountForRepository(String repoId) {
		return getEntityManager()
				.createNamedQuery("CmisObject.getObjectsCountForRepo").setParameter(REPOSITORY_ID, repoId)
				.getResultList();
	}

	public long getNumberOfObjectsWaitingForContentIndexing(String repoId) {
		return getEntityManager().createNamedQuery("CmisObject.getWaitingForIndexingContentObjectsCount", Number.class)
				.setParameter(REPOSITORY_ID, repoId).getSingleResult().longValue();
	}

	public List<?> getObjectCountWithContentIndexingStatus(String repoId) {
		return getEntityManager().createNamedQuery("CmisObject.getObjectContentIndexingStateCount")
				.setParameter(REPOSITORY_ID, repoId).getResultList();
	}
	
	public List<?> getObjectCountWithMetadataIndexingStatus(String repoId) {
		return getEntityManager().createNamedQuery("CmisObject.getObjectMetadataIndexingStateCount")
				.setParameter(REPOSITORY_ID, repoId).getResultList();
	}

	public Set<CMISObject> getObjectsWhereIdInList(List<Integer> collectionOfIds) {
		return getObjectsWhereIdInList(collectionOfIds, "CmisObject.getObjectsFromIdList");
	}

	private Set<CMISObject> getObjectsWhereIdInListAcl(List<Integer> collectionOfIds) {
		return getObjectsWhereIdInList(collectionOfIds, "CmisObject.getObjectsFromIdListAcl");
	}

	private Set<CMISObject> getObjectsWhereIdInList(List<Integer> collectionOfIds, String queryName) {
		if (collectionOfIds.isEmpty()) {
			return Collections.emptySet();
		}
		List<List<Integer>> inPages = getInPages(collectionOfIds);
		List<CMISObject> result = new ArrayList<>();
		for (List<Integer> inPage : inPages) {
			result.addAll(getEntityManager()
					.createNamedQuery(queryName, CMISObject.class)
					.setParameter("listOfIds", inPage)
					.getResultList());
		}
		
		Collections.sort(result, new CMISObject.CmisIdComparator(collectionOfIds));
		return new LinkedHashSet<>(result);
	}

	private List<List<Integer>> getInPages(List<Integer> collectionOfIds) {
		List<List<Integer>> inPages = new ArrayList<>();
		int limit = combinedConfig.getInt(PS_LIMIT);
		if (limit == 0) {
			// No limit, so do not split in pages.
			inPages.add(collectionOfIds);
		}
		else {
			// Split in pages.
			int size = collectionOfIds.size();
			int from, to;
			for (from = 0, to = limit; to < size; from += limit, to += limit) {
				inPages.add(collectionOfIds.subList(from, to));
			}
			inPages.add(collectionOfIds.subList(from, size));
		}
		return inPages;
	}

	//PRIVATE
	private String getOrderedQueryString(String namedQuery, String orderColumn, boolean isAsc) {
		String ans = namedQuery;
		if (orderColumn != null && orderColumn.length() > 0) {
			ans = ans.concat("Sort").concat((isAsc) ? "ASC" : "DESC");
		} 
		return ans;
	}

	private Query setOrderbyParameters(Query query, String orderColumn) {
		if (orderColumn != null && orderColumn.length() > 0) {
			query.setParameter(OBJTYPE_PROPERTY, orderColumn);
		}
		return query;
	}
	
	private CMISObject resolveByCmisObjectId(String cmisObjectId) {
		return getObjectByQuery(cmisObjectId, "cmisObject.only_acl");
	}

	private CMISObject resolveByCmisPath(String repositoryId, String cmisPath) {
		// Root folder: path = "/"
		if (cmisPath.equals(CMIS_PATH_SEP)) {
			return getObjectByPropertyValue(repositoryId, CMIS_PATH_SEP, PropertyIds.PATH);
		}

		try {
			List<CMISObject> ans = getEntityManager().createNamedQuery("CmisObject.getByPath", CMISObject.class)
					.setParameter(REPOSITORY_ID, repositoryId)
					.setParameter("parentPath", getParentPath(cmisPath))
					.setParameter("objectPath", cmisPath)
					.getResultList();

			if (ans.isEmpty()) {
				throw new CmisObjectNotFoundException("Cannot find Object described by path : " + cmisPath);
			}
			return ans.get(0);
		} catch (NoResultException e) {
			throw new CmisObjectNotFoundException("Cannot find Object described by path : " + cmisPath, e);
		}

	}

	private String getParentPath(String cmisPath) {
		int sepLastIndex = cmisPath.lastIndexOf(CMIS_PATH_SEP);
		if (sepLastIndex == 0) {
			//special case, child of root
			return CMIS_PATH_SEP;
		}
		return cmisPath.substring(0, sepLastIndex);
	}

	private CMISObject getObjectByQuery(String cmisId, String queryName) {
		try {
			List<CMISObject> ans = getEntityManager()
					.createNamedQuery(queryName, CMISObject.class)
					.setParameter(CMIS_OBJECT_ID, cmisId)
					.getResultList();

			if (ans.isEmpty()) {
				throw new CmisObjectNotFoundException("Cannot find Object with cmisId: " + cmisId);
			}
			return ans.get(0);
		} catch (NoResultException e) {
			throw new CmisObjectNotFoundException("Cannot find Object with cmisId: " + cmisId, e);
		}
	}

	/**
	 * Returns the number of siblings with the same cmis:name (ignoring the documents for the same version series if any).
	 * 
	 * @param parentId
	 * @param namePropertyIds
	 * @param name
	 * @param versionSeriesPropertyId
	 * @param versionSeries
	 * @return int the number of siblings with the same cmis:name (ignoring the documents for the same version series if any).
	 */
	public int matchingSiblingsCount(Integer parentId, Set<Integer> namePropertyIds, String name, Integer versionSeriesPropertyId, String versionSeries) {
		return getEntityManager()
				.createNamedQuery("CmisObject.getSiblingsCount", Number.class)
				.setParameter("parentId", parentId)
				.setParameter("namePropertyIds", namePropertyIds)
				.setParameter("name", name)
				.setParameter("versionSeriesPropertyId", versionSeriesPropertyId)
				.setParameter("versionSeries", versionSeries)
				.getSingleResult().intValue();
	}
	
	
	/**
	 * Count the number of objects of the requested type
	 *  
	 * @param repositoryId
	 * @param objectTypeId
	 * @return
	 */
	public Long countObjectByType(String repositoryId, String objectTypeId) {
		return getEntityManager().createNamedQuery("CmisObject.countObjectByType", Long.class)
				.setParameter(OBJECT_TYPE_ID, objectTypeId)
				.setParameter(REPOSITORY_ID, repositoryId)
				.getSingleResult();
	}
	
	/**
	 * Calculate the size of a folder (considering all the descendent objects)
	 * 
	 * @param repositoryId
	 * @param objectId
	 */
	public BigDecimal getFolderSize(String repositoryId, String objectId) {
		BigDecimal size = getEntityManager().createNamedQuery("CmisObject.sizeByFolder", BigDecimal.class)
		.setParameter(REPOSITORY_ID, repositoryId)
		.setParameter(OBJECT_ID, objectId).getSingleResult();
		
		//the size will be null if there are no documents inside the folder requested
		if (size == null) {
			size = BigDecimal.ZERO;
		}
		return size;
	}

}
