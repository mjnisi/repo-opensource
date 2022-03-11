package eu.trade.repo.service.cmis.data.out;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.chemistry.opencmis.commons.data.CreatablePropertyTypes;
import org.apache.chemistry.opencmis.commons.data.NewTypeSettableAttributes;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.model.Repository;

/**
 * Helper implementation class that provides the {@link RepositoryCapabilities} interface from a {@link Repository} object.
 * 
 * @author porrjai
 */
class RepositoryCapabilitiesImpl extends NonExtensionsObject implements RepositoryCapabilities {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryCapabilitiesImpl.class);

	private static final long serialVersionUID = 1L;

	private final Repository repository;

	RepositoryCapabilitiesImpl(Repository repository) {
		this.repository = repository;
	}

	@Override
	public CapabilityContentStreamUpdates getContentStreamUpdatesCapability() {
		return repository.getContentStreamUpdatability();
	}

	@Override
	public CapabilityChanges getChangesCapability() {
		return repository.getChanges();
	}

	@Override
	public CapabilityRenditions getRenditionsCapability() {
		return repository.getRenditions();
	}

	@Override
	public Boolean isGetDescendantsSupported() {
		return repository.getGetDescendants();
	}

	@Override
	public Boolean isGetFolderTreeSupported() {
		return repository.getGetFolderTree();
	}

	@Override
	public Boolean isMultifilingSupported() {
		return repository.getMultifiling();
	}

	@Override
	public Boolean isUnfilingSupported() {
		return repository.getUnfiling();
	}

	@Override
	public Boolean isVersionSpecificFilingSupported() {
		return repository.getVersionSpecificFiling();
	}

	@Override
	public Boolean isPwcSearchableSupported() {
		return repository.getPwcSearchable();
	}

	@Override
	public Boolean isPwcUpdatableSupported() {
		return repository.getPwcUpdatable();
	}

	@Override
	public Boolean isAllVersionsSearchableSupported() {
		return repository.getAllVersionsSearchable();
	}

	@Override
	public CapabilityQuery getQueryCapability() {
		return repository.getQuery();
	}

	@Override
	public CapabilityJoin getJoinCapability() {
		return repository.getJoin();
	}

	@Override
	public CapabilityAcl getAclCapability() {
		return repository.getAcl();
	}

	@Override
	public CapabilityOrderBy getOrderByCapability() {
		LOG.info("getOrderByCapability(): Operation not available. CMIS 1.1");
		return null;
	}

	@Override
	public CreatablePropertyTypes getCreatablePropertyTypes() {
		CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
		creatablePropertyTypes.setCanCreate(new HashSet<PropertyType>(Arrays.asList(PropertyType.values())));
		return creatablePropertyTypes;
	}

	@Override
	public NewTypeSettableAttributes getNewTypeSettableAttributes() {
		NewTypeSettableAttributesImpl newTypeSettableAttributes = new NewTypeSettableAttributesImpl();
		newTypeSettableAttributes.setCanSetId(true);
		newTypeSettableAttributes.setCanSetLocalName(true);
		newTypeSettableAttributes.setCanSetLocalNamespace(true);
		newTypeSettableAttributes.setCanSetDisplayName(true);
		newTypeSettableAttributes.setCanSetQueryName(true);
		newTypeSettableAttributes.setCanSetDescription(true);
		newTypeSettableAttributes.setCanSetCreatable(true);
		newTypeSettableAttributes.setCanSetFileable(true);
		newTypeSettableAttributes.setCanSetQueryable(true);
		newTypeSettableAttributes.setCanSetFulltextIndexed(true);
		newTypeSettableAttributes.setCanSetIncludedInSupertypeQuery(true);
		newTypeSettableAttributes.setCanSetControllablePolicy(true);
		newTypeSettableAttributes.setCanSetControllableAcl(true);
		return newTypeSettableAttributes;
	}
}
