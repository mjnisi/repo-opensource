package eu.trade.repo.service.cmis.data.out;

import static eu.trade.repo.util.Constants.*;

import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ExtensionFeature;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.commons.configuration.Configuration;

import eu.trade.repo.model.Repository;

/**
 * Helper implementation class that provides the {@link RepositoryInfo} interface from a {@link Repository} object.
 * 
 * @author porrjai
 */
public class RepositoryInfoImpl extends NonExtensionsObject implements RepositoryInfo {

	private static final long serialVersionUID = 1L;
	private static final String VENDOR_NAME = "product.vendor";
	private static final String PRODUCT_NAME = "product.name";
	private static final String PRODUCT_VERSION = "product.version";
	private static final String THIN_CLIENT_URI = "product.thinClientUri";

	private final Repository repository;
	private final Configuration configuration;
	private String rootFolderId;
	private String latestChangeLogToken;

	private RepositoryCapabilities repositoryCapabilities;
	private AclCapabilities aclCapabilities;

	/**
	 * The repository info is build on a repository objects and from its permission definitions and mappings.
	 * <p>
	 * Some properties are extracted from the server configuration:
	 * VENDOR_NAME, PRODUCT_NAME, PRODUCT_VERSION and THIN_CLIENT_URI
	 * 
	 * @param repository {@link Repository}
	 * @param configuration {@link Configuration} The configuration properties for the server.
	 */
	public RepositoryInfoImpl(Repository repository, Configuration configuration) {
		this.repository = repository;
		this.configuration = configuration;
	}

	/**
	 * The repository info is build on a repository objects and from its permission definitions and mappings.
	 * <p>
	 * Some properties are extracted from the server configuration:
	 * VENDOR_NAME, PRODUCT_NAME, PRODUCT_VERSION and THIN_CLIENT_URI
	 * 
	 * @param repository {@link Repository}
	 * @param configuration {@link Configuration} The configuration properties for the server.
	 * @param rootFolderId {@link String} The repository's root folder id.
	 * @param permissionDefinitions {@link List}
	 * @param permissionMappings {@link List}
	 */
	public RepositoryInfoImpl(Repository repository, Configuration configuration, String rootFolderId, List<PermissionDefinition> permissionDefinitions, List<PermissionMapping> permissionMappings) {
		this(repository, configuration);
		this.repositoryCapabilities = new RepositoryCapabilitiesImpl(repository);
		if (!repository.getAcl().equals(CapabilityAcl.NONE)) {
			this.aclCapabilities = new AclCapabilitiesImpl(repository, permissionDefinitions, permissionMappings);
		}
		this.rootFolderId = rootFolderId;
	}

	/**
	 * 
	 * @param repository {@link Repository}
	 * @param configuration {@link Configuration} The configuration properties for the server.
	 * @param rootFolderId {@link String} The repository's root folder id.
	 * @param permissionDefinitions {@link List}
	 * @param permissionMappings {@link List}
	 * @param latestChangeLogToken {@link String}
	 */
	public RepositoryInfoImpl(Repository repository, Configuration configuration, String rootFolderId, List<PermissionDefinition> permissionDefinitions, List<PermissionMapping> permissionMappings, String latestChangeLogToken) {
		this(repository, configuration, rootFolderId, permissionDefinitions, permissionMappings);
		this.latestChangeLogToken = latestChangeLogToken;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getId()
	 */
	@Override
	public String getId() {
		return repository.getCmisId();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getName()
	 */
	@Override
	public String getName() {
		return repository.getName();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getDescription()
	 */
	@Override
	public String getDescription() {
		return repository.getDescription();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getVendorName()
	 */
	@Override
	public String getVendorName() {
		return configuration.getString(VENDOR_NAME);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getProductName()
	 */
	@Override
	public String getProductName() {
		return configuration.getString(PRODUCT_NAME);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getProductVersion()
	 */
	@Override
	public String getProductVersion() {
		return configuration.getString(PRODUCT_VERSION);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getRootFolderId()
	 */
	@Override
	public String getRootFolderId() {
		return rootFolderId;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getCapabilities()
	 */
	@Override
	public RepositoryCapabilities getCapabilities() {
		return repositoryCapabilities;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getAclCapabilities()
	 */
	@Override
	public AclCapabilities getAclCapabilities() {
		return aclCapabilities;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getCmisVersionSupported()
	 */
	@Override
	public String getCmisVersionSupported() {
		return CMIS_VERSION;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getThinClientUri()
	 */
	@Override
	public String getThinClientUri() {
		return configuration.getString(THIN_CLIENT_URI);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getChangesIncomplete()
	 */
	@Override
	public Boolean getChangesIncomplete() {
		// Currently all the changes. If the log is going to be truncated this value can be discovered with a query (the first even in a complete log is the creation of the root folder).
		return false;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getChangesOnType()
	 */
	@Override
	public List<BaseTypeId> getChangesOnType() {
		return BASE_TYPE_CMIS_10;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getLatestChangeLogToken()
	 */
	@Override
	public String getLatestChangeLogToken() {
		return latestChangeLogToken;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getPrincipalIdAnonymous()
	 */
	@Override
	public String getPrincipalIdAnonymous() {
		// TODO: https://webgate.ec.europa.eu/CITnet/jira/browse/TDR-65
		return PRINCIPAL_ID_ANONYMOUS;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.RepositoryInfo#getPrincipalIdAnyone()
	 */
	@Override
	public String getPrincipalIdAnyone() {
		return PRINCIPAL_ID_ANYONE;
	}

	@Override
	public CmisVersion getCmisVersion() {
		return CmisVersion.CMIS_1_1;
	}

	@Override
	public List<ExtensionFeature> getExtensionFeatures() {
		return Collections.emptyList();
	}
}
