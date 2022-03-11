package eu.trade.repo.service;

import static eu.trade.repo.util.Constants.BASE_TYPE_CMIS_10;
import static eu.trade.repo.util.Constants.CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY;
import static eu.trade.repo.util.Constants.CMIS_PATH_SEP;
import static eu.trade.repo.util.Constants.DEFAULT_CREATE_COUT_VERSIONLABEL;
import static eu.trade.repo.util.Constants.DEFAULT_CREATE_MAJOR_VERSIONLABEL;
import static eu.trade.repo.util.Constants.DEFAULT_CREATE_MINOR_VERSIONLABEL;
import static eu.trade.repo.util.Constants.MODE_CHECKIN;
import static eu.trade.repo.util.Constants.MODE_CHECKOUT;
import static eu.trade.repo.util.Constants.MODE_CHECKOUT_NEW;
import static eu.trade.repo.util.Constants.TYPE_CMIS_DOCUMENT;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.JDBCLobDelegate;
import eu.trade.repo.delegates.MaxSizeInputStream;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.selectors.PropertySelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.ICurrentDate;
import eu.trade.repo.util.PropertiesMap;

/**
 * 
 * @author azaridi, abascis
 *
 */
public class CMISBaseObjectService extends CMISBaseService {

	private static final Logger LOG = LoggerFactory.getLogger(CMISBaseObjectService.class);
	private static final String MYME_TYPE_REQUIRED = "The myme type of the content stream is missing. It is mandatory to provide this information.";

	@Autowired
	private ICurrentDate currentDate;
	@Autowired
	private IDGenerator generator;
	@Autowired
	private Security security;
	@Autowired
	private CMISObjectSelector objSelector;
	@Autowired
	private JDBCLobDelegate jdbcDelegate;
	@Autowired
	private RepositorySelector repositorySelector;
	@Autowired
	private PropertySelector propSelector;
	@Autowired
	private ObjectTypePropertySelector otpSelector;
	@Autowired
	private ObjectTypeSelector objectTypeSelector;
	@Autowired
	private Configuration combinedConfig;

	/**
	 * @return the currentDate
	 */
	protected ICurrentDate getCurrentDate() {
		return currentDate;
	}

	/**
	 * @return the generator
	 */
	protected IDGenerator getGenerator() {
		return generator;
	}

	/**
	 * @return the objSelector
	 */
	protected CMISObjectSelector getObjSelector() {
		return objSelector;
	}

	/**
	 * @return the repositorySelector
	 */
	protected RepositorySelector getRepositorySelector() {
		return repositorySelector;
	}

	/**
	 * @return the propSelector
	 */
	protected PropertySelector getPropSelector() {
		return propSelector;
	}

	/**
	 * @return the jdbcDelegate
	 */
	protected JDBCLobDelegate getJdbcDelegate() {
		return jdbcDelegate;
	}

	/**
	 * @return the security
	 */
	protected Security getSecurity() {
		return security;
	}

	public void setCurrentDate(ICurrentDate currentDate) {
		this.currentDate = currentDate;
	}

	public void setGenerator(IDGenerator generator) {
		this.generator = generator;
	}

	/**
	 * Returns the latest versions of a versions series.
	 * 
	 * @param versions {@link List<CMISObject>} The version series.
	 * @param major boolean if true returns the latest major version.
	 * @return {@link CMISObject} the latest versions of a versions series. Null if it is not found.
	 */
	protected CMISObject getLatestVersion(List<CMISObject> versions, boolean major) {
		String latestProperty = major ? PropertyIds.IS_LATEST_MAJOR_VERSION : PropertyIds.IS_LATEST_VERSION;
		for (CMISObject obj : versions) {
			if (!obj.isPwc() && obj.<Boolean>getPropertyValue(latestProperty)) {
				return obj;
			}
		}
		return null;
	}

	/**
	 * Get every Version of a given object (ordered by cmis:creationDate) including the PWC.
	 * <p>
	 * If the version series id is null the it retrieves the object using the object id in order to find out the version series id.
	 * 
	 * @param repositoryId {@link String} repository id.
	 * @param objectId {@link String} CMIS object id.
	 * @param versionSeriesId {@link String} Version series id.
	 * @return {@link List<CMISObject>} Every Version of a given object (ordered by cmis:creationDate) including the PWC
	 */
	protected List<CMISObject> getAllVersions(String repositoryId, String objectId, String versionSeriesId) {
		String actualVersionSeriesId = versionSeriesId;
		if (versionSeriesId == null) {
			CMISObject cmisObject = objSelector.getCMISObject(repositoryId, objectId);
			actualVersionSeriesId = getVersionSeriesId(cmisObject);
		}
		return objSelector.getAllVersions(actualVersionSeriesId,
				otpSelector.getObjTypeProperty(TYPE_CMIS_DOCUMENT, PropertyIds.VERSION_SERIES_ID, repositoryId).getId(),
				otpSelector.getObjTypeProperty(TYPE_CMIS_DOCUMENT, PropertyIds.CREATION_DATE, repositoryId).getId()
				);
	}

	protected String getVersionSeriesId(CMISObject cmisObject) {
		try {
			return cmisObject.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue();
		} catch (PropertyNotFoundException e) {
			throw new CmisInvalidArgumentException(String.format("Cannot get version series id for object %s", cmisObject), e);
		}
	}

	private void renamePathTree(int pathPropId, String parentPath, String newName, String oldpath) {
		updatePaths(pathPropId, fixRootFolderPath(parentPath) + CMIS_PATH_SEP + newName, oldpath, oldpath.length() + 1);
	}

	protected void updatePathTree(int pathPropId, String newPathToRoot, String relativeRoot, String oldpath) {
		updatePaths(pathPropId, fixRootFolderPath(newPathToRoot), oldpath, oldpath.lastIndexOf(relativeRoot));
	}

	/*
	 * For every subfolder of root, replace the part of the path before the realtiveRoot with the newPathtoRoot
	 */
	private void updatePaths(int pathPropId, String newPathToRoot, String oldpath, int index) {
		getEntityManager()
		.createNamedQuery("property.update_paths")
		.setParameter("newPathToRoot", newPathToRoot)
		.setParameter("oldpath", oldpath)
		.setParameter("pathPropId", pathPropId)
		.setParameter("idx", index)
		.executeUpdate();
	}

	/**
	 * In the case of the root folder the path already ends with slash, so remove it.
	 * 
	 * @param newPathToRoot
	 * @return
	 */
	private String fixRootFolderPath(String newPathToRoot) {
		if (CMIS_PATH_SEP.equals(newPathToRoot)) {
			return "";
		}
		return newPathToRoot;
	}

	protected String updateProperties(String repositoryId, CMISObject cmisObject, Properties properties) {
		canUpdate(cmisObject);
		PropertiesMap<Object> propertiesMap = buildPropertiesMap(properties);
		persistOrMergeProperties(repositoryId, cmisObject, propertiesMap, true);
		String ans = setObjectUpdateProperties(cmisObject);
		//merge the updated properties
		merge(cmisObject);
		return ans;
	}

	private PropertiesMap<Object> buildPropertiesMap(Properties properties) {
		PropertiesMap<Object> propertiesMap = new PropertiesMap<>();
		if (properties != null && properties.getProperties() != null) {
			for (Entry<String, PropertyData<?>> propertyEntry : properties.getProperties().entrySet()) {
				propertiesMap.putAll(propertyEntry.getKey(), (List<Object>) propertyEntry.getValue().getValues());
			}
		}

		return propertiesMap;
	}

	/**
	 * Updates the subtree paths when a folder is renamed
	 * 
	 * @param cmisObject
	 * @param name
	 */
	private void updatePath(CMISObject cmisObject, String name) {
		//if obj is a folder but not the root folder, update all subfolder paths
		if (cmisObject.isFolder() && !cmisObject.isRootFolder()) {
			// Every folder has one and only one parent.
			CMISObject parentFolder = cmisObject.getParents().iterator().next();
			Property parentPathProperty;
			try {
				parentPathProperty = parentFolder.getProperty(PropertyIds.PATH);
			} catch (PropertyNotFoundException e) {
				throw new IllegalStateException("Should not happen, a folder without path property.", e);
			}
			String objectOldPath = cmisObject.getPropertyValue(PropertyIds.PATH);
			String parentPath = parentPathProperty.getTypedValue();
			renamePathTree(parentPathProperty.getObjectTypeProperty().getId(), parentPath, name, objectOldPath);
		}
	}


	/**
	 * This method receives an object (id and objectType have to be populated) and a PropertiesMap containing the cmisId of propertyTypes as keys  and the values
	 * of that properties as values.
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 * @param propTypeCmisIdValuesMap
	 * @param userUpdate
	 */
	private <T> void persistOrMergeProperties(String repositoryId, CMISObject cmisObject, PropertiesMap<T> propTypeCmisIdValuesMap, boolean userUpdate) {
		if (!propTypeCmisIdValuesMap.isEmpty()) {
			
			Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
			
			//check secondary types, 
			List<String> secondaryTypes = (List<String>)propTypeCmisIdValuesMap.get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
			
			//if no data is coming from the client, we should keep the secondary types of the database
			if(secondaryTypes == null) { 
				secondaryTypes = new ArrayList<String>();
				
				for(ObjectType ot: cmisObject.getSecondaryTypes()) {
					secondaryTypes.add(ot.getCmisId());
				}
			}
			
			
			//clean the secondary types, this is because the user may remove secondary types in the object properties
			cmisObject.getSecondaryTypes().clear();
			
			for(String secondaryTypeId: secondaryTypes) {
				//add to the previous structure 'propertyTypeMap'
				if (objectTypeSelector.countObjectTypeByCmisId(repositoryId, secondaryTypeId) == 0) {
					throw new CmisConstraintException("Non-existent secondary type, you could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
				} else {
					ObjectType secondaryType = objectTypeSelector.getObjectTypeByCmisId(repositoryId, secondaryTypeId);
					if(!BaseTypeId.CMIS_SECONDARY.value().equals(secondaryType.getBase().getCmisId()) ||
							BaseTypeId.CMIS_SECONDARY.value().equals(secondaryType.getCmisId())	) {
						throw new CmisConstraintException("You could use only cmis:secondary subtypes for the property values cmis:secondaryObjectTypeIds.");
					}
					
					propertyTypeMap.putAll(secondaryType.getObjectTypePropertiesIncludeParents(true, true));
					
					//add to the object list of secondary types
					cmisObject.addSecondaryType(secondaryType);
				}
			}

			//remove the properties from deleted secondary types
			removeDeletedSecondaryTypesProperties(cmisObject);
			
			//check secondary type required properties
			checkSecondaryTypeRequiredProperties(cmisObject, propTypeCmisIdValuesMap);
			
			
			List<String> propTypeCmisIdList = new ArrayList<>(propTypeCmisIdValuesMap.size());
			propTypeCmisIdList.addAll(propTypeCmisIdValuesMap.keySet());
			List<Property> propListToUpdate = propSelector.getPropertiesOfTypes(cmisObject.getId(), propTypeCmisIdList);


			PropertiesMap<Property> propertiesToUpdate = new PropertiesMap<Property>();
			for (Property property : propListToUpdate) {
				propertiesToUpdate.put(property.getObjectTypeProperty().getCmisId(), property);
			}

			
			Iterator<String> it = propTypeCmisIdValuesMap.keySet().iterator();
			String propTypeCmisId = null;
			Property prop = null;
			while (it.hasNext()) {
				propTypeCmisId = it.next();
				List<T> newPropValues = propTypeCmisIdValuesMap.get(propTypeCmisId);
				List<Property> oldList = propertiesToUpdate.get(propTypeCmisId);

				// Several checks.
				checkUpdate(cmisObject, propTypeCmisId, propertyTypeMap, newPropValues, userUpdate);
				
				// If its the name property , make sure no other siblings exist with the same name, and rebuild path when needed
				if (propTypeCmisId.equals(PropertyIds.NAME)) {
					String versionSeries = cmisObject.getPropertyValue(PropertyIds.VERSION_SERIES_ID);
					// Name is a single string property
					String name = (String) newPropValues.get(0);
					for (CMISObject parent  : cmisObject.getParents()) {
						checkSiblingsNames(repositoryId, parent.getId(), parent.<String>getPropertyValue(PropertyIds.PATH), name, versionSeries);
					}
					updatePath(cmisObject, name);
				}

				// Merge properties, single or multivalued.
				if( isSingleValue(oldList) && isSingleValue(newPropValues)) {
					prop = oldList.get(0);
					Object value = newPropValues.get(0);
					if (isEmptyValue(value)) {
						// Unset the property
						cmisObject.removeProperty(prop);
						remove(prop);
					}
					else {
						//update property value
						prop.setTypedValue(newPropValues.get(0));
						merge(prop);
					}
				}
				else {
					// TODO [porrjai]: enhance the update updating pre-existent properties
					if ( null != oldList ) {
						for (Property property : oldList) {
							cmisObject.removeProperty(property);
							remove(property);
						}
					}
					for (T value : newPropValues) {
						if (!isEmptyValue(value)) {
							// Do not add empty properties.
							prop = new Property(propertyTypeMap.get(propTypeCmisId).first(), value);
							cmisObject.addProperty(prop);
							persist(prop);
						}
					}
				}
			}
		}
	}

	/**
	 * We need to clean as well the associated properties for removed secondary types.
	 * 
	 * We could have in the repository several secondary types extending a common one, for example:
	 * 
	 * trade:secondary (with property p1)
	 * trade:tag extends trade:secondary (with property p2) 
	 * trade:invoice extends trade:secondary (with property p3)
	 * 
	 * If an object has the secondary types, tag and invoice and a value for property p1,
	 * if the secondary type invoice is removed the p1 property should be kept because tag 
	 * has this as well. 
	 * 
	 * <strong>Important!</strong>, this method requires to have the secondary types collection 
	 * loaded with the new values.
	 * 
	 */
	private void removeDeletedSecondaryTypesProperties(CMISObject cmisObject) {
		
		//collect the still valid properties 
		//A: properties of the object type 
		//B: properties of the selected secondary types
		Set<String> propertyIdstoKeep = new HashSet<String>();
		propertyIdstoKeep.addAll(cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true).keySet());
		for(ObjectType secondaryType: cmisObject.getSecondaryTypes()) {
			propertyIdstoKeep.addAll((secondaryType.getObjectTypePropertiesIncludeParents(true, true).keySet()));
		}

		Set<String> propertyIdsToDelete = new HashSet<String>();
		for(Property p: cmisObject.getProperties()) {
			String propId = p.getObjectTypeProperty().getCmisId();
			if(!propertyIdstoKeep.contains(propId)) {
				propertyIdsToDelete.add(propId);
			}
		}
		for(String propId: propertyIdsToDelete) {
			cmisObject.removeProperty(propId);
		}
	}

	private boolean isEmptyValue(Object value) {
		return value == null || value.toString().isEmpty();
	}

	private boolean isEmptyValues(List<?> values) {
		if (values != null && !values.isEmpty()) {
			for (Object value : values) {
				if (!isEmptyValue(value)) {
					return false;
				}
			}
		}
		return true;
	}

	private void checkUpdate(CMISObject cmisObject, String propTypeCmisId, Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap, List<?> newValues, boolean userUpdate) {
		if (!propertyTypeMap.containsKey(propTypeCmisId)) {
			throw new CmisConstraintException(String.format("Cannot update non-existant property %s of object %s", propTypeCmisId, cmisObject.getCmisObjectId()));
		}

		ObjectTypeProperty objectTypeProperty = propertyTypeMap.get(propTypeCmisId).first();

		// Check cardinality.
		if (newValues != null && newValues.size() > 1 && objectTypeProperty.getCardinality().equals(Cardinality.SINGLE)) {
			throw new CmisConstraintException(String.format("Cannot set multiple values in a single property %s for the object %s", propTypeCmisId, cmisObject.getCmisObjectId()));
		}

		// Check updatability
		if (userUpdate) {
			boolean updatable = true;
			switch (objectTypeProperty.getUpdatability()) {
			case ONCREATE :
			case READONLY :
				updatable = false;
				break;

			case WHENCHECKEDOUT :
				updatable = cmisObject.isPwc();
				break;

			case READWRITE :
				// Empty. Is updatable
				break;
			}

			if (!updatable) {
				throw new CmisConstraintException(String.format("Cannot update non-updatable property %s of object %s", propTypeCmisId, cmisObject.getCmisObjectId()));
			}
		}

		// Required
		if (objectTypeProperty.getRequired() && isEmptyValues(newValues)) {
			throw new CmisConstraintException(String.format("Cannot unset the required property %s of object %s", propTypeCmisId, cmisObject.getCmisObjectId()));
		}
		
		
	}
	
	/**
	 * Checks id there are missing required properties in the secondary types
	 * 
	 * @param cmisObject
	 * @param clientProperties client values
	 */
	private void checkSecondaryTypeRequiredProperties(CMISObject cmisObject, PropertiesMap clientProperties) {

		Set<String> currentPropertyIds = cmisObject.getPropertyMap().keySet();
		Set<String> allPropertyIds = new HashSet<String>(currentPropertyIds);
		
		//only new non-empty properties
		for(String newPropertyId: (Iterable<String>)clientProperties.keySet()) {
			if(!isEmptyValues(clientProperties.get(newPropertyId))) {
				allPropertyIds.add(newPropertyId);
			}
		}
		
		for(ObjectType secondaryType: cmisObject.getSecondaryTypes()) {
			Set<String> requiredIds = secondaryType.getObjectTypePropertiesIncludeParents(true, false).keySet();
			
			if(!allPropertyIds.containsAll(requiredIds)) {
				throw new CmisInvalidArgumentException("Cannot update object, missing required properties of type " + secondaryType.getCmisId() + ".");
			}
			
		}
		
	}

	protected void validateUpdateStreamCapability(String repositoryId) {
		if (repositorySelector.getRepository(repositoryId).getContentStreamUpdatability().equals(CapabilityContentStreamUpdates.NONE)) {
			throw new CmisNotSupportedException(CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY);
		}
	}

	/**
	 * Creates or replace the object content stream.
	 * <p>
	 * The object must be already persisted.
	 * 
	 * @param repositoryId {@link String} The repository id
	 * @param cmisObject {@link CMISObject}
	 * @param overwriteFlag boolean
	 * @param contentStream {@link ContentStream}
	 */
	@TriggerIndex
	protected PropertiesMap<Object> setContentStream(String repositoryId, CMISObject cmisObject, boolean overwriteFlag, ContentStream contentStream ) {
		canUpdate(cmisObject);
		if( ContentStreamAllowed.NOTALLOWED.equals(cmisObject.getObjectType().getContentStreamAllowed())  ){
			throw new CmisStreamNotSupportedException("This object type does not support to be related to a stream");
		}
		boolean hasStream = objSelector.hasStream(cmisObject.getId());
		String mymeType = checkMymeType(hasStream, overwriteFlag, contentStream);

		if( hasStream ) {
			if (!overwriteFlag) {
				throw new CmisContentAlreadyExistsException("The object already has a related stream and the overwrite flag is set to false");
			}
			jdbcDelegate.deleteStream(cmisObject.getId());
		}

		int streamSize = getSizeAsInt(contentStream.getLength());
		MaxSizeInputStream maxSizeInputStream = new MaxSizeInputStream(contentStream.getStream(), streamSize, combinedConfig.getLong(Constants.PROPNAME_PRODUCT_MAX_CONTENT_SIZE));
		jdbcDelegate.saveStream(cmisObject.getId(), maxSizeInputStream, streamSize);

		PropertiesMap<Object> streamPropertyMap = new PropertiesMap<Object>();
		streamPropertyMap.put(PropertyIds.CONTENT_STREAM_ID, cmisObject.getCmisObjectId());
		streamPropertyMap.put(PropertyIds.CONTENT_STREAM_FILE_NAME, contentStream.getFileName());
		streamPropertyMap.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, mymeType);
		streamPropertyMap.put(PropertyIds.CONTENT_STREAM_LENGTH, BigInteger.valueOf(maxSizeInputStream.getSize()));

		String newChangeToken = generator.next();
		streamPropertyMap.put(PropertyIds.LAST_MODIFICATION_DATE, currentDate.getDate());
		streamPropertyMap.put(PropertyIds.LAST_MODIFIED_BY, security.getCallContextHolder().getUsername());
		streamPropertyMap.put(PropertyIds.CHANGE_TOKEN, newChangeToken);

		persistOrMergeProperties(repositoryId, cmisObject, streamPropertyMap, false);

		return streamPropertyMap;
	}

	private String checkMymeType(boolean hasStream, boolean overwriteFlag, ContentStream contentStream) {
		String mymeType = contentStream.getMimeType();
		// Only in the case of append (hasStream && !overwriteFlag) the myme type restriction is not applied
		if (!hasStream || overwriteFlag) {
			if (mymeType == null || mymeType.isEmpty()) {
				throw new CmisConstraintException(MYME_TYPE_REQUIRED, MYME_TYPE_REQUIRED);
			}
		}
		return mymeType;

	}

	/**
	 * Note: Currently, the {@link org.springframework.jdbc.support.lob.LobCreator} interface force us the use of int to specify the size, limiting it up to 2Gb.
	 * 
	 * @param size
	 * @return
	 */
	private int getSizeAsInt(long size) {
		int intSize;
		try {
			intSize = new BigDecimal(size).intValueExact();
		} catch (ArithmeticException e) {
			throw new CmisStreamNotSupportedException("The stream is too big to upload: " + size, e);
		}

		return intSize;
	}

	private <T> boolean isSingleValue(List<T> list){
		return null != list && 1 == list.size();
	}

	/**
	 * Returns true if all the version in a version series should be file together.
	 * 
	 * @param repository
	 * @param cmisObject
	 * @return boolean true if all the version in a version series should be file together.
	 */
	protected boolean fileAllVersionTogether(Repository repository, CMISObject cmisObject) {
		return cmisObject.isDocument() && cmisObject.getObjectType().isVersionable() && !repository.getVersionSpecificFiling();
	}

	/**
	 * Validates the the folder is one of the object's parents.
	 * 
	 * @param cmisObject {@link CMISObject}
	 * @param folder {@link CMISObject}
	 */
	protected void validateParentFolder(CMISObject cmisObject, CMISObject folder) {
		// To avoid corner cases, check that the source is really a parent for the object.
		if (!cmisObject.getParents().contains(folder)) {
			LOG.error("The folder {} is not a parent of the object {}", folder, cmisObject);
			throw new IllegalStateException("The folder is not a parent of the object (or one of its old versions). This may happen if the version filing capability has been changed.");
		}
	}

	/**
	 * This method solves the following scenario:
	 * <p/>
	 * 1) The repository capability versionSpecificFiling was true but now is false.<BR/>
	 * 2) Some versionable objects are disaggregated in different folders.<BR/>
	 * 3) The last version of one of that objects is being moved/unfiled from the {@code parent} folder.<BR/>
	 * 4) Not all the versions of the object are in the {@code parent} folder.<BR/>
	 * <p/>
	 * For those objects this method align their parents to be the same as the latest version's parents.
	 * <p>
	 * Finally note that in this scenario, if the latest version is deleted then the new latest version can still appear in different folders (in a multifiling case).
	 * 
	 * @param versions {@link List<CMISObject>}
	 * @param parent {@link CMISObject} Parent folder.
	 */
	protected void alignVersionSeries(List<CMISObject> versions, CMISObject parent) {
		CMISObject latest = getLatestVersion(versions, false);
		if (latest == null) {
			throw new IllegalStateException("Latest version not found.");
		}
		validateParentFolder(latest, parent);
		if (!areAllAligned(versions, parent)) {
			Set<CMISObject> latestParents = latest.getParents();
			for (CMISObject version : versions) {
				if (!areEquals(latestParents, version.getParents())) {
					removeFromParents(version);
					for (CMISObject alignedParent : latestParents) {
						alignedParent.addChild(version);
					}
				}
			}
		}
	}

	private boolean areAllAligned(List<CMISObject> versions, CMISObject parent) {
		for (CMISObject version : versions) {
			if (!version.getParents().contains(parent)) {
				return false;
			}
		}
		return true;
	}

	private <T extends Object> boolean areEquals(Set<T> first, Set<T> second) {
		if (first == second) {
			return true;
		}
		if (first == null || second == null) {
			return false;
		}
		if (first.size() != second.size()) {
			return false;
		}
		for (T t : first) {
			if (!second.contains(t)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove this object from all its parents.
	 * 
	 * @param cmisObject {@link CMISObject}
	 */
	protected void removeFromParents(CMISObject cmisObject) {
		// Copy the set of parents to avoid concurrent modification exception
		Set<CMISObject> parents = new HashSet<>(cmisObject.getParents());
		for (CMISObject parent : parents) {
			parent.removeChild(cmisObject);
		}
	}

	/**
	 * Checks the parent folder allows to file objects of the given type.
	 * 
	 * @param objTypeId
	 * @param parent
	 */
	protected void checkAllowedChildren(String objTypeId, CMISObject parent) {
		List<Property> allowedChildren = null;
		try {
			allowedChildren = parent.getMultiValuedProperty(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
		} catch (PropertyNotFoundException e) {
			//no allowed children defined, valid, nothing to do
			return;
		}

		boolean found = false;
		for (Property property : allowedChildren) {
			if (property.getTypedValue().equals(objTypeId)) {
				found = true;
			}
		}
		if (!found) {
			throw new CmisInvalidArgumentException(
					String.format("Cannot create/move object of type %s under parent %s, not allowed by the parent", objTypeId, parent));
		}
	}

	/**
	 * Since the documents path are base on documents names, siblings are not allowed to have the same name to avoid path conflicts.
	 * 
	 * @param repositoryId
	 * @param newchild
	 * @param parent
	 */
	protected void validateSiblings(String repositoryId, CMISObject newchild, CMISObject parent) {
		String childName = newchild.getPathSegment();
		if (childName == null || childName.equals("")) {
			throw new CmisNameConstraintViolationException(
					"Cannot create/move object, empty name not allowed for parent "+parent.getPathSegment());
		}

		String parentPath = parent.getPropertyValue(PropertyIds.PATH);
		if (parentPath == null || parentPath.equals("")) {
			throw new CmisInvalidArgumentException(
					"Cannot validate siblings, empty/null path for parent "+parent.getPathSegment());
		}

		checkAllowedChildren(newchild.<String>getPropertyValue(PropertyIds.OBJECT_TYPE_ID), parent);
		checkSiblingsNames(repositoryId, parent.getId(), parentPath, childName, newchild.<String>getPropertyValue(PropertyIds.VERSION_SERIES_ID));
	}

	/**
	 * Since the documents path are base on documents names, siblings are not allowed to have the same name to avoid path conflicts.
	 * 
	 * @param repositoryId
	 * @param parentId
	 * @param parentPath
	 * @param childName
	 * @param versionSeries {@link String} The version series property needs to be already set for the object.
	 */
	private void checkSiblingsNames(String repositoryId, Integer parentId, String parentPath, String childName, String versionSeries) {
		String pathKey = repositoryId + parentPath + CMIS_PATH_SEP + childName;
		synch(pathKey);

		Integer versionSeriesPropertyId = otpSelector.getObjTypeProperty(BaseTypeId.CMIS_DOCUMENT.value(), PropertyIds.VERSION_SERIES_ID, repositoryId).getId();

		if (objSelector.matchingSiblingsCount(parentId, getNamePropertyIds(repositoryId), childName, versionSeriesPropertyId, versionSeries) > 0) {
			throw new CmisNameConstraintViolationException(String.format("Cannot create/move object, another object with name %s already exists for under parent %s", childName, parentPath));
		}
	}

	private Set<Integer> getNamePropertyIds(String repositoryId) {
		Set<Integer> namePropertyIds = new HashSet<>();
		for (BaseTypeId baseTypeId : BASE_TYPE_CMIS_10) {
			ObjectTypeProperty objectTypeProperty;
			try {
				objectTypeProperty = otpSelector.getObjTypeProperty(baseTypeId.value(), PropertyIds.NAME, repositoryId);
				namePropertyIds.add(objectTypeProperty.getId());
			} catch (CmisObjectNotFoundException e) {
				// Ignore it.
			}
		}
		return namePropertyIds;
	}

	protected String setObjectUpdateProperties(CMISObject object) {
		String token = generator.next();
		Date now = currentDate.getDate();
		String currentUser = security.getCallContextHolder().getUsername();

		try {
			object.getProperty(PropertyIds.LAST_MODIFIED_BY).setTypedValue(currentUser);
			object.getProperty(PropertyIds.CHANGE_TOKEN).setTypedValue(token);
			object.getProperty(PropertyIds.LAST_MODIFICATION_DATE).setTypedValue(now);
		} catch (Exception ignored) {
			//all three are created during any object creation .. should always be present
		}
		return token;
	}

	/**
	 * This method do not performs any check, only deletes the object and maybe the related index.
	 * 
	 * @param repositoryId
	 * @param cmisObject
	 */
	protected void deleteObject(String repositoryId, CMISObject cmisObject) {
		remove(cmisObject);
	}

	/**
	 * 
	 * @param obj
	 * @param currentUser	// the user checking-out the object NOT USED IF MODE != CHECKOUT
	 * @param major			// whether its a major version check-in NOT USED IF MODE != CHECKIN
	 * @param mode
	 * @param comment		// the check-in comment NOT USED IF MODE != CHECKIN
	 * @param latestVersion // the version to increment NOT USED IF MODE != CHECKIN
	 */
	protected void updateCloneProperties(CMISObject obj, String currentUser, boolean major, int mode, String latestVersion, String comment) {
		Date now = currentDate.getDate();

		try {
			//
			if (mode == MODE_CHECKOUT || mode == MODE_CHECKOUT_NEW) {
				//COMMON
				obj.getProperty(PropertyIds.CREATION_DATE).setTypedValue(now);
				obj.getProperty(PropertyIds.LAST_MODIFICATION_DATE).setTypedValue(now);
				obj.getProperty(PropertyIds.CREATED_BY).setTypedValue(currentUser);
				obj.getProperty(PropertyIds.LAST_MODIFIED_BY).setTypedValue(currentUser);
				obj.getProperty(PropertyIds.OBJECT_ID).setTypedValue(obj.getCmisObjectId());
				obj.getProperty(PropertyIds.CHANGE_TOKEN).setTypedValue(generator.next());
				//VERSION
				obj.getProperty(PropertyIds.VERSION_LABEL).setTypedValue(DEFAULT_CREATE_COUT_VERSIONLABEL);
				obj.getProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).setTypedValue(true);
				obj.getProperty(PropertyIds.IS_MAJOR_VERSION).setTypedValue(false);
				obj.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(false);
				obj.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(false);
				obj.getProperty(PropertyIds.IS_PRIVATE_WORKING_COPY).setTypedValue(true);
				//STREAM
				if (mode == MODE_CHECKOUT) {//not if its a new object
					try {
						obj.getProperty(PropertyIds.CONTENT_STREAM_ID).setTypedValue(obj.getCmisObjectId());
					} catch (PropertyNotFoundException ignored) {

					}
				}
			} else
				if (mode == MODE_CHECKIN) {
					//COMMON
					obj.getProperty(PropertyIds.LAST_MODIFICATION_DATE).setTypedValue(now);
					obj.getProperty(PropertyIds.CREATED_BY).setTypedValue(currentUser);
					obj.getProperty(PropertyIds.LAST_MODIFIED_BY).setTypedValue(currentUser);
					obj.getProperty(PropertyIds.CHANGE_TOKEN).setTypedValue(generator.next());

					//STREAM TODO: review if this should be set when there is no stream
					try {
						obj.getProperty(PropertyIds.CONTENT_STREAM_ID).setTypedValue(obj.getCmisObjectId());
					} catch (PropertyNotFoundException ignored) {
						//ignored if there's no stream
					}

					//VERSION
					Property pLabel = obj.getProperty(PropertyIds.VERSION_LABEL);
					pLabel.setTypedValue(getNextVersionLabel(major, latestVersion));
					obj.getProperty(PropertyIds.IS_MAJOR_VERSION).setTypedValue(major);
					obj.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(major);
					obj.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(true);
					obj.getProperty(PropertyIds.IS_PRIVATE_WORKING_COPY).setTypedValue(false);
					setCheckInComment (obj, comment);
				}
		} catch (PropertyNotFoundException e) {
			throw new CmisInvalidArgumentException(String.format("Object %s cannot be cloned, it missing req. properties", obj), e);
		}
	}

	/**
	 * Set the comment property of the given object
	 * @param obj, the object to be updated
	 * @param comment, the new value of the comment property
	 */
	private void setCheckInComment(CMISObject obj, String comment) {
		Property checkinCommentProperty = null;
		try {
			checkinCommentProperty = obj.getProperty(PropertyIds.CHECKIN_COMMENT);
		} catch (PropertyNotFoundException e) {
			// FIRST TIME CHECK_IN, CREATE/SET THE COMMENT PROPERTY. See below when comment != null and checkinCommentProperty == null.
		}

		if (comment == null || comment.isEmpty()) {
			// Note that if the string is null or empty the result is the same: the property is "not set"
			if (checkinCommentProperty != null) {
				obj.removeProperty(checkinCommentProperty);

			}
			// else do nothing
		}
		else if (checkinCommentProperty != null) {
			checkinCommentProperty.setTypedValue(comment);
		}
		else {
			Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
			ObjectTypeProperty cmnt = propertyTypeMap.get(PropertyIds.CHECKIN_COMMENT).first();
			checkinCommentProperty = new Property(cmnt, comment);
			obj.addProperty(checkinCommentProperty);
		}
	}

	/**
	 * Given a version , returns an incremented version string
	 * @param major
	 * @param oldVersion
	 * 
	 * @return The next logical version string
	 */
	protected String getNextVersionLabel (boolean major, String oldVersion) {
		if (!isNumber(oldVersion)) {
			//old version not a number
			if (major) {
				return DEFAULT_CREATE_MAJOR_VERSIONLABEL;
			} else {
				return DEFAULT_CREATE_MINOR_VERSIONLABEL;
			}
		}

		String[] newVersion = oldVersion.split("\\.");
		if (major) {
			newVersion[0] = Integer.parseInt(newVersion[0])+1+"";
			newVersion[1] = "0";
		} else {
			newVersion[1] = Integer.parseInt(newVersion[1])+1+"";
		}
		return newVersion[0]+"."+newVersion[1];
	}

	private boolean isNumber(String oldVersion) {
		try {
			new BigDecimal(oldVersion);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/*
	 * in order to be updatable, an object, is either the PWC of a checked out series or the latest in an non-checked out series!
	 */
	private void canUpdate(CMISObject object) {
		if (!object.isDocument()) {
			return;
		}
		try {
			boolean isSeriesCheckedout = object.getProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).getTypedValue();
			boolean isLatest = object.getProperty(PropertyIds.IS_LATEST_VERSION).getTypedValue();
			boolean isPWC = object.isPwc();

			if (isSeriesCheckedout && !isPWC) {
				throw new CmisInvalidArgumentException(String.format("%s is not the PWC of its series, cannot update properties ",  object.getCmisObjectId()));
			}

			if (!isSeriesCheckedout && !isLatest) {
				throw new CmisInvalidArgumentException(String.format("%s is not the latest in its series, cannot update properties",  object.getCmisObjectId()));
			}
		} catch (PropertyNotFoundException e1) {
			throw new CmisInvalidArgumentException(String.format("Object does not define property %s, cannot update %s", PropertyIds.IS_LATEST_VERSION, object.getCmisObjectId()), e1);
		}
	}

}
