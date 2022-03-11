package eu.trade.repo.service.cmis.data.out;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;

import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.util.Constants;

/**
 * {@link ObjectData} implementation.
 * 
 * @author porrjai
 */
public class ObjectDataImpl implements ObjectData {

	private final CMISObject cmisObject;
	private AllowableActions allowableActions;
	private Properties properties;
	private List<ObjectData> relationships;
	private Acl acl;
	private List<CmisExtensionElement> extensions;
	private PolicyIdList policyIdList;

	private static final String DATA_NOVALUE = "";

	private ObjectDataImpl(CMISObject cmisObject) {
		this.cmisObject = cmisObject;
		setIndexExtensionData();
	}

	private void setIndexExtensionData() {
		List<CmisExtensionElement> indexExtDataSubElementList = new ArrayList<CmisExtensionElement>();
		
		CmisExtensionElement metadataNode = createIndexPartExtensionElement(IndexOperationType.METADATA, this.cmisObject.getIndexStateMetadata(), this.cmisObject.getIndexTriesMetadata());
		indexExtDataSubElementList.add(metadataNode);
		if (this.cmisObject.isDocument()) {
			CmisExtensionElement contentNode = createIndexPartExtensionElement(IndexOperationType.CONTENT, this.cmisObject.getIndexStateContent(), this.cmisObject.getIndexTriesContent());
			indexExtDataSubElementList.add(contentNode);
		}
		CmisExtensionElementImpl indexData = new CmisExtensionElementImpl(Constants.NS, "indexing", null, indexExtDataSubElementList);
		this.extensions = new ArrayList<>();
		extensions.add(indexData);
	}

	private CmisExtensionElement createIndexPartExtensionElement(IndexOperationType operationType, Integer indexState, Integer indexTries){
		String state = ( null == indexState )?  DATA_NOVALUE : indexState.toString();
		String tries = (null == indexTries)? DATA_NOVALUE : String.valueOf(indexTries);
		CmisExtensionElement indexingStateNode = new CmisExtensionElementImpl(Constants.NS, "state", null, state);
		CmisExtensionElement indexingTriesNode = new CmisExtensionElementImpl(Constants.NS, "tries", null, tries);
		return new CmisExtensionElementImpl(Constants.NS, operationType.name(), null, Arrays.asList(indexingStateNode, indexingTriesNode));
	}

	private ObjectDataImpl(CMISObject cmisObject, Boolean includePolicyIds, Boolean includeAcl, AllowableActions allowableActions, List<CMISObject> relationships, Properties properties) {
		this(cmisObject);
		this.properties = properties;
		this.allowableActions = allowableActions;
		if (Boolean.TRUE.equals(includeAcl)) {
			acl = AclBuilder.build(cmisObject);
		}
		initRelationships(relationships);
		
		
		if(includePolicyIds) {
			policyIdList = new PolicyIdListImpl();
			((PolicyIdListImpl)policyIdList).setPolicyIds(new ArrayList<String>());
			for(CMISObject policy: cmisObject.getPolicies()) {
				policyIdList.getPolicyIds().add(policy.getCmisObjectId());
			}
		}
	}		

	/**
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object
	 * @param includePolicyIds
	 * @param includeAcl
	 * @param allowableActions {@link AllowableActions} The object's allowable actions.
	 * @param relationships {@link List<CMISObject>} The related cmis objects.
	 * @param filter
	 */
	public ObjectDataImpl(CMISObject cmisObject, Boolean includePolicyIds, Boolean includeAcl, AllowableActions allowableActions, List<CMISObject> relationships, String filter) {
		this(cmisObject, includePolicyIds, includeAcl, allowableActions, relationships, PropertiesBuilder.build(cmisObject, filter));
	}

	/**
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object
	 * @param includePolicyIds
	 * @param includeAcl
	 * @param allowableActions {@link AllowableActions} The object's allowable actions.
	 * @param relationships {@link List<CMISObject>} The related cmis objects.
	 * @param filter
	 */
	public ObjectDataImpl(CMISObject cmisObject, Boolean includePolicyIds, Boolean includeAcl, AllowableActions allowableActions, List<CMISObject> relationships, Map<String, String> filter) {
		this(cmisObject, includePolicyIds, includeAcl, allowableActions, relationships, PropertiesBuilder.build(cmisObject, filter));
	}

	/**
	 * New instance. Does not include ACL nor policies information.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object
	 * @param allowableActions {@link AllowableActions} The object's allowable actions.
	 * @param relationships {@link List<CMISObject>} The related cmis objects.
	 * @param filter
	 */
	public ObjectDataImpl(CMISObject cmisObject, AllowableActions allowableActions, List<CMISObject> relationships, String filter) {
		this(cmisObject, false, false, allowableActions, relationships, filter);
	}

	/**
	 * New instance. Does not include ACL nor policies information.
	 * 
	 * @param cmisObject
	 * @param allowableActions
	 * @param relationships
	 * @param filter
	 */
	public ObjectDataImpl(CMISObject cmisObject, AllowableActions allowableActions, List<CMISObject> relationships, Map<String, String> filter) {
		this(cmisObject, false, false, allowableActions, relationships, filter);
	}

	private void initRelationships(List<CMISObject> relationships) {
		if (relationships != null) {
			this.relationships = new ArrayList<>();
			for (CMISObject relatedCmisObject : relationships) {
				this.relationships.add(new ObjectDataImpl(relatedCmisObject, false, false, null, null, PropertiesBuilder.build(relatedCmisObject)));
			}
		}
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getId()
	 */
	@Override
	public String getId() {
		return cmisObject.getCmisObjectId();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getBaseTypeId()
	 */
	@Override
	public BaseTypeId getBaseTypeId() {
		return BaseTypeId.fromValue(cmisObject.getObjectType().getBase().getCmisId());
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getProperties()
	 */
	@Override
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getAllowableActions()
	 */
	@Override
	public AllowableActions getAllowableActions() {
		return allowableActions;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getRelationships()
	 */
	@Override
	public List<ObjectData> getRelationships() {
		return relationships;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getChangeEventInfo()
	 */
	@Override
	public ChangeEventInfo getChangeEventInfo() {
		return null;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getAcl()
	 */
	@Override
	public Acl getAcl() {
		return acl;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#isExactAcl()
	 */
	@Override
	public Boolean isExactAcl() {
		if (acl == null) {
			return false;
		}
		return acl.isExact();
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getPolicyIds()
	 */
	@Override
	public PolicyIdList getPolicyIds() {
		return policyIdList;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ObjectData#getRenditions()
	 */
	@Override
	public List<RenditionData> getRenditions() {
		// TODO Add support for renditions
		return null;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.data.ExtensionsData#getExtensions()
	 */
	@Override
	public List<CmisExtensionElement> getExtensions() {
		return extensions;
	}

	/**
	 * Sets or adds the extensions to this object.
	 * 
	 * @see org.apache.chemistry.opencmis.commons.data.ExtensionsData#setExtensions(java.util.List)
	 */
	@Override
	public void setExtensions(List<CmisExtensionElement> extensions) {
		if (this.extensions == null) {
			this.extensions = extensions;
		}
		else {
			this.extensions.addAll(extensions);
		}
	}
}
