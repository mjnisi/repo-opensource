package eu.trade.repo.service.interfaces;

import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.spi.Holder;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Property;


/**
 * Interface for versioning objects
 */
public interface IVersioningService {
	/**
	 * Checks out a CMIS Object specified by its CMIS id. Creates a new version and adds it to the object's versio series.
	 * 
	 * @param repositoryId 	{@link String} The repository id
	 * @param objectId		{@link String} The CMIS Object id
	 * @param contentCopied {@link String} Deep clone for the new version
	 */
	void checkOut(String repositoryId, Holder<String> objectId, Holder<Boolean> contentCopied) ;

	void cancelCheckOut(String repositoryId, String objectId);

	void checkIn(String repositoryId, Holder<String> objectId, boolean major, Properties properties, ContentStream contentStream,
			String checkinComment, List<String> policies, Set<Acl> addAces, Set<Acl> removeAces);

	CMISObject getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, boolean major) ;

	Set<Property> getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major, String filter) ;

	List<CMISObject> getAllVersions(String repositoryId, String objectId, String versionSeriesId) ;
}
