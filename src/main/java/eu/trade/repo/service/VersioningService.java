package eu.trade.repo.service;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.spi.Holder;

import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.service.interfaces.IVersioningService;
import eu.trade.repo.util.Constants;

/**
 * 
 * @author azaridi
 *
 */
public class VersioningService extends CMISBaseObjectService implements IVersioningService {

	/**
	 * @see eu.trade.repo.service.interfaces.IVersioningService#checkOut(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, org.apache.chemistry.opencmis.commons.spi.Holder)
	 */
	@TriggerIndex
	@Override
	public void checkOut(String repositoryId, Holder<String> objectId, Holder<Boolean> contentCopied) {

		//VALIDATE
		CMISObject object = getObjSelector().getCMISObject(repositoryId, objectId.getValue());
		canCheckOut(object);

		//CREATE PWC
		CMISObject pwc = null;
		try {
			pwc = object.deepClone();
			pwc.setCmisObjectId(getGenerator().next());
		} catch (CloneNotSupportedException e) {
			throw new CmisInvalidArgumentException("Cannot clone, " + object, e);
		}

		//UPDATE PROPERTIES OF PWC
		String currentUser = getSecurity().getCallContextHolder().getUsername();
		updateCloneProperties(pwc, currentUser, false, Constants.MODE_CHECKOUT, null, null);

		//PERSIST PWC
		//through JPA
		persist(pwc);

		for(CMISObject parent : pwc.getParents()) {
			merge(parent);
		}

		flush();
		//stream , through JDBC
		getJdbcDelegate().copyStream(object.getId(), pwc.getId());

		//UPDATE PROPS COMMON TO EVRRYONE IN THE SERIES
		batchUpdateVersionCommonProperties(repositoryId, objectId.getValue(), pwc, currentUser, Constants.MODE_CHECKOUT, null);

		getSecurity().getAccessControl().checkout(repositoryId, object, pwc);

		//RETURN PWC ID
		if(contentCopied != null) {
			contentCopied.setValue(true);
		}
		objectId.setValue(pwc.getCmisObjectId());
	}

	@TriggerIndex
	@Override
	public void cancelCheckOut(String repositoryId, String objectId) {

		CMISObject cmisObject = getObjSelector().getCMISObject(repositoryId, objectId);
		if (!cmisObject.isPwc()) {
			throw new CmisPermissionDeniedException("The object is not a PWC: " + cmisObject);
		}

		//THE SERIES ID
		String versionSeriesId = cmisObject.getPropertyValue(PropertyIds.VERSION_SERIES_ID);

		// FOR EVERY OBJECT IN THE SERIES
		long max = 0;
		CMISObject latest = null;
		for (CMISObject version : getAllVersions(repositoryId, objectId, versionSeriesId)) {
			//DELETE THE PWC
			if (version.isPwc()) {
				deleteObject(repositoryId, version);
			} else {
				//UPDATE THE PROPERTIES OF OTHER OBJETS IN THE SERIES
				try {
					long time = ((GregorianCalendar)version.getProperty(PropertyIds.LAST_MODIFICATION_DATE).getTypedValue()).getTimeInMillis();
					if (time >= max) {
						max = time;
						latest = version;
					}
				} catch (PropertyNotFoundException ignored) {
					//SHOULD NEVER HAPPERN - LASTMODE_DATE is generated at creation
				}
				Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = version.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
				version.removeProperty(propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).first().getCmisId());
				version.removeProperty(propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).first().getCmisId());
				try {
					version.getProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).setTypedValue(false);
				} catch (PropertyNotFoundException ignored) {
					//SHOULD NEVER HAPPEN
				}
				merge(version);
			}
		}
		//set the latest in the series
		//if cancelling checkout of document created as checked-out, there are no previous versions, so latest == null
		if(latest != null) {
			try {
				latest.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(true);
				if (latest.getPropertyValue(PropertyIds.IS_MAJOR_VERSION)) {
					latest.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(true);
				}
			} catch (PropertyNotFoundException ignored) {
				//SHOULD NEVER HAPPERN - LASTMODE_DATE is generated at creation
			}
			merge(latest);
		}
	}

	/**
	 * @see eu.trade.repo.service.interfaces.IVersioningService#checkIn(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, org.apache.chemistry.opencmis.commons.data.Properties, org.apache.chemistry.opencmis.commons.data.ContentStream, java.lang.String, java.util.List, Set, Set)
	 */
	@TriggerIndex
	@Override
	public void checkIn(String repositoryId, Holder<String> objectId, boolean major, Properties properties, ContentStream contentStream, String checkinComment, List<String> policies, Set<Acl> addAces, Set<Acl> removeAces) {

		// The objecId could be other in the version series than the PWC, so we need to check it
		CMISObject object = getObjSelector().getCMISObject(repositoryId, objectId.getValue());
		String pwcId = object.getPropertyValue(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID);

		if (pwcId == null) {
			throw new CmisInvalidArgumentException(String.format("Version series for %s is not checked-out, cannot get PWC",  objectId.getValue()));
		}

		if (!object.getCmisObjectId().equals(pwcId)) {
			throw new CmisInvalidArgumentException(String.format("Please use the PWC to check-in a Version series",  objectId.getValue()));
		}

		if (properties != null) {
			updateProperties(repositoryId, object, properties);
		}

		if (contentStream != null) {
			setContentStream(repositoryId, object, true, contentStream);
		}

		String versionSeriesId = object.getPropertyValue(PropertyIds.VERSION_SERIES_ID);
		CMISObject latestInSeries = getObjectOfLatestVersion(repositoryId, objectId.getValue(), versionSeriesId, major);
		String latestVersion = getLatesVersion(object, latestInSeries);

		String currentUser = getSecurity().getCallContextHolder().getUsername();
		updateCloneProperties(object, currentUser, major, Constants.MODE_CHECKIN, latestVersion, checkinComment);
		merge(object);

		batchUpdateVersionCommonProperties(repositoryId, objectId.getValue(), latestInSeries, currentUser, Constants.MODE_CHECKIN, major);
		getSecurity().getAccessControl().checkin(repositoryId, latestInSeries, object, addAces, removeAces);
	}

	private String getLatesVersion(CMISObject object, CMISObject latestInSeries) {
		if (latestInSeries == null) {
			return object.getPropertyValue(PropertyIds.VERSION_LABEL);
		}
		return latestInSeries.getPropertyValue(PropertyIds.VERSION_LABEL);
	}

	@Override
	public CMISObject getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, boolean major) {
		return getLatestVersion(getAllVersions(repositoryId, objectId, versionSeriesId), major);
	}

	@Override
	public Set<Property> getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter) {
		return getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major).getProperties();
	}

	@Override
	public List<CMISObject> getAllVersions(String repositoryId, String objectId, String versionSeriesId) {
		return super.getAllVersions(repositoryId, objectId, versionSeriesId);
	}

	/**
	 * Returns the PWC object in a checked-out version series
	 * 
	 * @param repositoryId
	 * @param objectId
	 * @param filter
	 * @param includeAllowableActions
	 * @param extension
	 * @return The pwc object in the series
	 */
	public CMISObject getSeriesPWC (String repositoryId, String objectId,String filter, Boolean includeAllowableActions,  ExtensionsData extension) {
		CMISObject obj = getObjSelector().getCMISObject(repositoryId, objectId);
		String versionSeriesId = obj.getPropertyValue(PropertyIds.VERSION_SERIES_ID);

		if (obj.isVersionSeriesCheckout()) {
			for (CMISObject version : getAllVersions(repositoryId, objectId, versionSeriesId)) {
				if (version.isPwc()) {
					return version;
				}
			}
		}
		throw new CmisInvalidArgumentException(String.format("Version series for %s is not checked-out, cannot get PWC",  obj.getCmisObjectId()));
	}


	//PRIVATE
	/**
	 * Check if the supplied object can be checked out.
	 * #
	 * @param cmisId The CMISObject to be checked out.
	 * @return {@link String} The version series id of the object that is to be checked-out
	 */
	private void canCheckOut(CMISObject object) {
		//object should be versionable
		if (!object.getObjectType().isVersionable()) {
			throw new CmisInvalidArgumentException(String.format("Object is not versionable, cannot check out: %s",  object.getCmisObjectId()));
		}

		try {
			String versionSeriesId = object.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue();
			boolean latest= object.getProperty(PropertyIds.IS_LATEST_VERSION).getTypedValue();
			boolean checkedout = object.getProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).getTypedValue();

			//versionId should be valid
			if (versionSeriesId.length()  <= 0) {
				throw new CmisInvalidArgumentException(String.format("Invalid Version Series id assigned, cannot check out %s",  object.getCmisObjectId()));
			}
			//series must NOT be checked-out
			if (checkedout) {
				throw new CmisInvalidArgumentException(String.format("The series is already checked out by %s, cannot check out document",
						object.getProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).getTypedValue()));
			}

			if (!latest) {
				throw new CmisInvalidArgumentException(String.format("Object is not the latest in its series, cannot check out %s",  object.getCmisObjectId()));
			}
		} catch (PropertyNotFoundException e) {
			//version id property should be present
			throw new CmisInvalidArgumentException(String.format("Object doesnt have a Version Series or a Latest object id assigned, cannot check out %s",  object.getCmisObjectId()), e);
		}
	}
	/**
	 * Updates the CHECKEDOUT, CHECKOUT_BY and CHECKEDOUT_ID for all objects in the corresponding version series
	 * 
	 * @param seriesId The object's version series
	 * Not BATCH at this version , going through allVersions objects ...
	 */
	private void batchUpdateVersionCommonProperties(String repositoryId, String objectId, CMISObject refObject, String currentUser, int mode, Boolean major) {
		List<CMISObject> seriesObjs = getAllVersions(repositoryId, objectId, null);

		try {
			for (CMISObject obj : seriesObjs) {
				Map<String, SortedSet<ObjectTypeProperty>> propertyTypeMap = null;
				obj.getProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT).setTypedValue(mode == Constants.MODE_CHECKOUT);
				switch (mode) {
				case Constants.MODE_CHECKOUT :
					// refObject is the PWC
					try {
						obj.getProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).setTypedValue(currentUser);
						obj.getProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).setTypedValue(refObject.getCmisObjectId());
					} catch (PropertyNotFoundException e) {
						//THESE ARE UNSET AT CHECK IN OR FIRST TIME CHECKOUT
						propertyTypeMap = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
						ObjectTypeProperty vsico = propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).first();
						Property versionSeriesIdCOBy = new Property(vsico,currentUser);
						obj.addProperty(versionSeriesIdCOBy);

						propertyTypeMap = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
						ObjectTypeProperty vsicoId = propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).first();
						Property versionSeriesIdCOId = new Property(vsicoId,refObject.getCmisObjectId());
						obj.addProperty(versionSeriesIdCOId);

					}
					break;

				case Constants.MODE_CHECKIN :
					// refObject is the old version (if exists)
					propertyTypeMap = obj.getObjectType().getObjectTypePropertiesIncludeParents(true, true);

					obj.removeProperty(propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY).first().getCmisId());
					obj.removeProperty(propertyTypeMap.get(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID).first().getCmisId());

					if (!obj.getCmisObjectId().equals(objectId)) {//the checked in pwc IS the latest in the series, set everyone else to false
						obj.getProperty(PropertyIds.IS_LATEST_VERSION).setTypedValue(false);
					}
					if (refObject != null && obj.getId().equals(refObject.getId())) {
						if (major) {//if not a major version check-in, let the latest major flag unchanged.
							//Note, (obj = refObject = current latest major in series (before the check-in))
							obj.getProperty(PropertyIds.IS_LATEST_MAJOR_VERSION).setTypedValue(false);
						}
					}
					break;

				default :
					// Nothing to do currently. Only checkout and checkin are calling this method.
					break;
				}
				merge(obj);
			}
		} catch (PropertyNotFoundException e) {
			throw new CmisInvalidArgumentException(String.format("Object %s cannot be checked out, it is missing req. property %s", refObject, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT), e);
		}
	}
}
