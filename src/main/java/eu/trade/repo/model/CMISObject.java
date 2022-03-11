package eu.trade.repo.model;

import static eu.trade.repo.util.Constants.CMIS_PATH_SEP;
import static eu.trade.repo.util.Constants.TYPE_CMIS_DOCUMENT;
import static eu.trade.repo.util.Constants.TYPE_CMIS_FOLDER;
import static eu.trade.repo.util.Constants.TYPE_CMIS_POLICY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Persistence;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.index.triggers.handlers.CMISObjectChangeListener;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.util.SQLConstants;

@Entity 
@EntityListeners(CMISObjectChangeListener.class)
@Table (name="Object")
@NamedQueries({
	@NamedQuery(name="cmisObject.only_acl", query = "select obj from CMISObject obj left join fetch obj.acls acl left join fetch acl.permission where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="cmisObject.only_children_properties",	query = "select obj from CMISObject obj left join fetch obj.children left join fetch obj.properties where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="CmisObject.by_propertyTypeAndValue",	query = "select obj from CMISObject obj left join obj.properties property left join fetch obj.objectType objectType left join fetch objectType.repository repository where repository.cmisId = :repositoryId and property.value = :value and property.objectTypeProperty.cmisId = :propertyTypeCmisId"),
	@NamedQuery(name="cmisObject.with_dependencies",	query = "select obj from CMISObject obj left join fetch obj.policies policies left join fetch obj.objectsUnderPolicy oup left join fetch obj.children c left join fetch obj.parents p left join fetch obj.acls acls left join fetch obj.properties props left join fetch obj.renditions rends where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="cmisObject.only_children",		query = "select obj from CMISObject obj left join fetch obj.children where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="cmisObject.only_policies",		query = "select obj from CMISObject obj left join fetch obj.policies where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="cmisObject.only_parents",			query = "select obj from CMISObject obj left join fetch obj.parents parents where obj.cmisObjectId = :cmisObjectId"),
	@NamedQuery(name="cmisObject.only_parentsACL",			query = "select obj from CMISObject obj left join fetch obj.parents parents where obj.cmisObjectId = :cmisObjectId and parents.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	
	@NamedQuery(name="CmisObject.by_propertyTypesAndValues.Secured", query = "select obj from CMISObject obj join fetch obj.properties join fetch obj.objectType objectType join fetch objectType.repository where obj.id in (select obj1.id from CMISObject obj1 join obj1.properties property join obj1.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and obj1.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)))"),
	@NamedQuery(name="CmisObject.countBy_propertyTypesAndValues.Secured", query ="select count(obj) from CMISObject obj join obj.properties property join obj.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and obj.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	
	@NamedQuery(name="CmisObject.by_ObjectType(s)PropertyTypesAndValues.Secured", query ="select obj from CMISObject obj join fetch obj.properties join fetch obj.objectType objectType join fetch objectType.repository where obj.id in (select obj1.id from CMISObject obj1 join obj1.properties property join obj1.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and objectType.cmisId in (:objectTypes) and obj1.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)))"),	
	@NamedQuery(name="CmisObject.countBy_ObjectType(s)PropertyTypesAndValues.Secured", query ="select count(obj) from CMISObject obj join obj.properties property join obj.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and objectType.cmisId in (:objectTypes) and obj.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	
	@NamedQuery(name="CmisObject.by_propertyTypesAndValues", query ="select obj from CMISObject obj join fetch obj.properties join fetch obj.objectType objectType join fetch objectType.repository where obj.id in (select obj1.id from CMISObject obj1 join obj1.properties property join obj1.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes))"),
	@NamedQuery(name="CmisObject.countBy_propertyTypesAndValues", query ="select count(obj) from CMISObject obj join obj.properties property join obj.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes)"),
	
	@NamedQuery(name="CmisObject.by_ObjectType(s)PropertyTypesAndValues", query ="select obj from CMISObject obj join fetch obj.properties join fetch obj.objectType objectType join fetch objectType.repository where obj.id in (select obj1.id from CMISObject obj1 join obj1.properties property join obj1.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and objectType.cmisId in (:objectTypes))"),	
	@NamedQuery(name="CmisObject.countBy_ObjectType(s)PropertyTypesAndValues", query ="select count(obj) from CMISObject obj join obj.properties property join obj.objectType objectType join objectType.repository repository where repository.cmisId = :repositoryId and property.value in (:values) and property.objectTypeProperty.cmisId in (:propertyTypes) and objectType.cmisId in (:objectTypes)"),

	
	@NamedQuery(name="CmisObject.getChildrenIds", 		 		query = "select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId"),
	@NamedQuery(name="CmisObject.getChildrenIdsSortASC", 		query = SQLConstants.BASEQUERY_CHILDRENIDS_ASC),
	@NamedQuery(name="CmisObject.getChildrenIdsSortDESC",		query = SQLConstants.BASEQUERY_CHILDRENIDS_DESC),
	@NamedQuery(name="CmisObject.getChildrenIdsVersion", 		query = "select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),
	@NamedQuery(name="CmisObject.getChildrenIdsVersionSortASC", query = SQLConstants.BASEQUERY_CHILDRENIDS_VERSIONASC),
	@NamedQuery(name="CmisObject.getChildrenIdsVersionSortDESC",query = SQLConstants.BASEQUERY_CHILDRENIDS_VERSIONDESC),
	@NamedQuery(name="CmisObject.getChildrenIdsAcl", 			query = "select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	@NamedQuery(name="CmisObject.getChildrenIdsAclSortASC", 	query = SQLConstants.BASEQUERY_CHILDRENIDS_ACLASC),
	@NamedQuery(name="CmisObject.getChildrenIdsAclSortDESC",	query = SQLConstants.BASEQUERY_CHILDRENIDS_ACLDESC),
	@NamedQuery(name="CmisObject.getChildrenIdsAclVersion", 	query = "select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),
	@NamedQuery(name="CmisObject.getChildrenIdsAclVersionSortASC", 	query = SQLConstants.BASEQUERY_CHILDRENIDS_ACLVERSION_ASC),
	@NamedQuery(name="CmisObject.getChildrenIdsAclVersionSortDESC", query = SQLConstants.BASEQUERY_CHILDRENIDS_ACLVERSION_DESC),

	@NamedQuery(name="CmisObject.getChildren", 			query = "select o from CMISObject o left join fetch o.properties left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where p.cmisObjectId = :objectId"),
	@NamedQuery(name="CmisObject.getChildrenSortASC", 	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId "+SQLConstants.BASEQUERY_POSTFIX_ASC),
	@NamedQuery(name="CmisObject.getChildrenSortDESC",	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId"+SQLConstants.BASEQUERY_POSTFIX_DESC),
	
	@NamedQuery(name="CmisObject.getChildrenVersion", 			query = "select o from CMISObject o left join fetch o.properties left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),
	@NamedQuery(name="CmisObject.getChildrenVersionSortASC", 	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"+SQLConstants.BASEQUERY_POSTFIX_ASC),
	@NamedQuery(name="CmisObject.getChildrenVersionSortDESC", 	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"+SQLConstants.BASEQUERY_POSTFIX_DESC),

	@NamedQuery(name="CmisObject.getChildrenAcl", 			query = "select o from CMISObject o left join fetch o.properties left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	@NamedQuery(name="CmisObject.getChildrenAclSortASC",	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"+SQLConstants.BASEQUERY_POSTFIX_ASC),
	@NamedQuery(name="CmisObject.getChildrenAclSortDESC", 	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"+SQLConstants.BASEQUERY_POSTFIX_DESC),
	
	@NamedQuery(name="CmisObject.getChildrenAclVersion",		query = "select o from CMISObject o left join fetch o.properties left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),
	@NamedQuery(name="CmisObject.getChildrenAclVersionSortASC",	query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"+SQLConstants.BASEQUERY_POSTFIX_ASC),
	@NamedQuery(name="CmisObject.getChildrenAclVersionSortDESC",query = "select o from CMISObject o left join fetch o.properties pr left join fetch o.acls left join fetch o.parents p left join fetch o.childrenCount left join fetch o.parentsCount where pr.objectTypeProperty.cmisId = :objectTypeProperty and p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"+SQLConstants.BASEQUERY_POSTFIX_DESC),	
	
	@NamedQuery(name="CmisObject.getChildrenCount",		query = "select count(o) from CMISObject o join o.parents p where p.cmisObjectId = :objectId"),
	@NamedQuery(name="CmisObject.getChildrenCountAcl",		query = "select count(o) from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds))"),
	@NamedQuery(name="CmisObject.getChildrenCountVersion",		query = "select count(o) from CMISObject o join o.parents p where p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),
	@NamedQuery(name="CmisObject.getChildrenCountAclVersion",		query = "select count(o) from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes))"),

	@NamedQuery(name="CmisObject.getRooFolderId",		query = "select o.cmisObjectId from CMISObject o where o.objectType.repository.cmisId = :repositoryId and o.objectType.base.cmisId = 'cmis:folder' and o.parents is empty"),
	@NamedQuery(name="CmisObject.getAllObjectsCount",	query = "select o.objectType.repository.cmisId, o.objectType.cmisId, count(o) from CMISObject o group by o.objectType.repository.cmisId, o.objectType.cmisId order by o.objectType.repository.cmisId, o.objectType.cmisId"),
	@NamedQuery(name="CmisObject.getObjectsCountPerRepo",	query = "select o.objectType.repository.cmisId, count(o) from CMISObject o group by o.objectType.repository.cmisId order by o.objectType.repository.cmisId"),
	@NamedQuery(name="CmisObject.getObjectsCountForRepo",	query = "select o.objectType.cmisId, count(o) from CMISObject o where o.objectType.repository.cmisId = :repositoryId group by o.objectType.cmisId order by o.objectType.cmisId"),
	@NamedQuery(name="CmisObject.getObjectContentIndexingStateCount",	query = "select o.indexStateContent, count(o) from CMISObject o where o.objectType.repository.cmisId = :repositoryId and o.indexStateContent > 0  group by o.indexStateContent order by o.indexStateContent"),
	@NamedQuery(name="CmisObject.getWaitingForIndexingContentObjectsCount",	query = "select count(p) from Property p where p.object.indexStateContent = 0 and p.objectTypeProperty.cmisId = 'cmis:contentStreamId' and p.value is not null and p.objectTypeProperty.objectType.repository.cmisId = :repositoryId"),
	@NamedQuery(name="CmisObject.getObjectMetadataIndexingStateCount",	query = "select o.indexStateMetadata, count(o) from CMISObject o where o.objectType.repository.cmisId = :repositoryId group by o.indexStateMetadata order by o.indexStateMetadata"),
	@NamedQuery(name="CmisObject.getObjectsFromIdList", query = "select o from CMISObject o left join fetch o.properties where o.id in :listOfIds"),
	@NamedQuery(name="CmisObject.getObjectsFromIdListAcl", query = "select o from CMISObject o left join fetch o.properties left join fetch o.acls left join fetch o.childrenCount left join fetch o.parentsCount where o.id in :listOfIds"),
	// Note: The getAllVersions query does not need a group by because all the properties are single valued. Additionally, the query with a group by expression ("... group by p1.object order by max(p2.value) desc") fails in Oracle
	@NamedQuery(name="CmisObject.getAllVersions", query = "select p1.object from Property p1, Property p2 where p1.objectTypeProperty.id = :vseriesPropId and p1.value = :versionSeriesId and p1.object.id = p2.object.id and p2.objectTypeProperty.id = :lastModDatePropId order by p2.value desc"),
	// Note, currently only non-version objects and latest version can be retrieved by path.
	@NamedQuery(name="CmisObject.getByPath", query = "select o from CMISObject o where o.id = (select op.objectParentId.objectId from ObjectParent op, ObjectParent parent where op.objectParentId.parentId = parent.objectParentId.objectId and op.repositoryId = :repositoryId and parent.objectPath = :parentPath and parent.typeId = 'cmis:folder' and op.objectPath = :objectPath and exists (select 1 from ObjectVersion ov where ov.id = op.objectParentId.objectId and ov.versionType in ('NON_VERSION', 'LATEST')))"),
	// Returns the number of elements with the same cmis:name ignoring the documents for the same version series if any.
	@NamedQuery(name="CmisObject.getSiblingsCount", query = "select count(o) from CMISObject o join o.parents p where p.id = :parentId and exists (select 1 from Property p where p.object.id = o.id and p.objectTypeProperty.id in (:namePropertyIds) and p.value = :name) and not exists (select 1 from Property p where p.object.id = o.id and p.objectTypeProperty.id = :versionSeriesPropertyId and p.value = :versionSeries)"),
	//list of objects of this type
	@NamedQuery(name="CmisObject.countObjectByType", query="select count(o) from CMISObject o where o.objectType.repository.cmisId = :repositoryId and o.objectType.cmisId = :objectTypeId"),
	@NamedQuery(name="CmisObject.sizeByFolder", query="select sum(p.numericValue) from Property p where p.objectTypeProperty.objectType.repository.cmisId = :repositoryId and p.objectTypeProperty.cmisId = 'cmis:contentStreamLength' and p.object.id in (select d.objectDescendantId.descendant.id from Descendant d where d.objectDescendantId.object.cmisObjectId = :objectId)")
})
@NamedNativeQueries({
	@NamedNativeQuery(name="CmisObject.hasStream-native", query="select count(s.id) as count from stream s where s.id = :objectId", resultSetMapping="countWrapper")
})
@SqlResultSetMappings({
	@SqlResultSetMapping(name="countWrapper", columns={@ColumnResult(name="count")})
})
public class CMISObject implements DBEntity, Cloneable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(CMISObject.class);

	@Transient
	private Double score = null;

	//MODEL
	@Id
	@GeneratedValue(generator="sq_object")
	@SequenceGenerator(sequenceName= "sq_object", name="sq_object")
	private Integer id;

	@OneToMany(mappedBy="object", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Acl> acls = new LinkedHashSet<Acl>();

	@OneToMany(mappedBy="scoreId.object", fetch = FetchType.LAZY)
	private Set<Score> scores = new LinkedHashSet<Score>();
	
	@OneToMany(mappedBy="objectAncestorId.object", fetch = FetchType.LAZY)
	private Set<Ancestor> ancestors = new LinkedHashSet<>();
	
	@OneToMany(mappedBy="objectDescendantId.object", fetch = FetchType.LAZY)
	private Set<Descendant> descendants = new LinkedHashSet<>();

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_type_id")
	private ObjectType objectType;

	@Column(name="cmis_object_id")
	private String cmisObjectId;

	@Column(name="index_state_content", nullable=false)
	private Integer indexStateContent = 0;

	@Column(name="index_tries_content", nullable=false)
	private Integer indexTriesContent = 0;
	
	@Column(name="index_state_metadata", nullable=false)
	private Integer indexStateMetadata = 0;

	@Column(name="index_tries_metadata", nullable=false)
	private Integer indexTriesMetadata = 0;

	@OneToMany(mappedBy="object", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private Set<Property> properties = new LinkedHashSet<Property>();

	@OneToMany(mappedBy="object")
	private List<Rendition> renditions;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="id", insertable=false, updatable=false)
	private ParentsCount parentsCount;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="id", insertable=false, updatable=false)
	private ChildrenCount childrenCount;

	//cascade removed because management is going thru children
	@ManyToMany(fetch=FetchType.LAZY, mappedBy = "children")
	private final Set<CMISObject> parents = new LinkedHashSet<CMISObject>();

	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(
			name="object_child",
			joinColumns={@JoinColumn(name="object_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="child_object_id", referencedColumnName="id")})
	private Set<CMISObject> children = new LinkedHashSet<CMISObject>();

	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(
			name="object_policy",
			joinColumns={@JoinColumn(name="object_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="policy_object_id", referencedColumnName="id")})
	private Set<CMISObject> policies = new LinkedHashSet<CMISObject>();

	@ManyToMany(fetch=FetchType.LAZY, mappedBy = "policies")
	private final Set<CMISObject> objectsUnderPolicy = new LinkedHashSet<CMISObject>();

	@OneToMany(mappedBy="object", fetch=FetchType.LAZY)
	private Set<WordObject> wordObjects = new LinkedHashSet<WordObject>();


	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(
			name="object_secondary_type",
			joinColumns={@JoinColumn(name="object_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="object_type_id", referencedColumnName="id")})
	private Set<ObjectType> secondaryTypes = new LinkedHashSet<ObjectType>();
	
	public CMISObject() {
		super();
	}
	public CMISObject(ObjectType objectType) {
		super();
		this.objectType = objectType;
	}


	//GETTERS/SETTERS
	@Override
	public Integer 		getId() 			{	return id;			}
	public Set<Acl> 	getAcls() 			{	return acls;		}
	public Set<Score> getScores()			{   return scores;			}
	public ObjectType 	getObjectType() 	{	return objectType;	}
	public String 		getCmisObjectId() 	{	return cmisObjectId;	}
	private ChildrenCount getChildrenCount() {	return childrenCount;	}
	private ParentsCount getParentsCount() 	{	return parentsCount;	}
	public Set<CMISObject> getParents() 	{	return Collections.unmodifiableSet(parents);	}
	public Set<Ancestor> getAncestors() {     return Collections.unmodifiableSet(ancestors);	}
	public Set<Descendant> getDescendants() {     return Collections.unmodifiableSet(descendants);	}
	public Set<CMISObject> getChildren() 	{ 	return Collections.unmodifiableSet(children);	}
	public Integer 		getIndexStateContent() 	{	return indexStateContent;	}
	public Integer 		getIndexTriesContent() 	{	return indexTriesContent;	}
	public Integer 		getIndexStateMetadata() 	{	return indexStateMetadata;	}
	public Integer 		getIndexTriesMetadata() 	{	return indexTriesMetadata;	}
	public Set<Property>  getProperties() 	{	return properties;	}
	public List<Rendition> getRenditions() 	{	return renditions;	}
	public Set<CMISObject> getPolicies() 	{ 	return Collections.unmodifiableSet(policies); }
	public Set<CMISObject> getObjectsUnderPolicy() { return Collections.unmodifiableSet(objectsUnderPolicy); }
	public Double getScore() {	return score;	}
	public Set<WordObject> getWordObjects() 		{	return wordObjects;	}
	public Set<ObjectType> getSecondaryTypes() 		{	return secondaryTypes; }


	@Override
	public void setId(Integer id) {	this.id = id;	}
	public void setRenditions(List<Rendition> renditions) 	{	this.renditions = renditions;	}
	private void setProperties(Set<Property> properties) 	{	this.properties = properties;	}
	public void setCmisObjectId(String cmisObjectId) {		this.cmisObjectId = cmisObjectId;	}
	public void setIndexStateContent(Integer indexStateContent) {		this.indexStateContent = indexStateContent;	}
	public void setIndexTriesContent(Integer indexTriesContent) {		this.indexTriesContent = indexTriesContent;	}
	public void setIndexStateMetadata(Integer indexStateMetadata) {		this.indexStateMetadata = indexStateMetadata;	}
	public void setIndexTriesMetadata(Integer indexTriesMetadata) {		this.indexTriesMetadata = indexTriesMetadata;	}
	public void setObjectType(ObjectType objectType) {	this.objectType = objectType;	}
	private void setAcls(Set<Acl> acls) 	{	this.acls = acls;	}
	public void setScores(Set<Score> scores) {	this.scores = scores;	}
	public void setScore(Double score) {	this.score = score;	}
	public void setChildren(Set<CMISObject> children) { this.children = children;	}
	private void setAncestors(Set<Ancestor> ancestors) { this.ancestors = ancestors; }
	private void setDescendants(Set<Descendant> descendants) { this.descendants = descendants; }
	public void setPolicies(Set<CMISObject> policies) { this.policies = policies;	}
	public void setWordObjects(Set<WordObject> wordObjects) {	this.wordObjects = wordObjects;	}
	private void setChildrenCount(ChildrenCount childrenCount) { this.childrenCount = childrenCount; }
	private void setParentsCount(ParentsCount parentsCount) { this.parentsCount = parentsCount; }
	private void setSecondaryTypes(Set<ObjectType> secondaryTypes) { this.secondaryTypes = secondaryTypes; }

	//ADDS/REMOVES
	public CMISObject addParent(CMISObject obj) {
		obj.addChild(this);
		return obj;
	}

	public CMISObject removeParent(CMISObject obj) {
		obj.removeChild(this);
		return obj;
	}

	public CMISObject addObjectUnderPolicy(CMISObject obj) {
		obj.addPolicy(this);
		return obj;
	}

	public CMISObject removeObjectFromPolicy(CMISObject obj) {
		obj.removePolicy(this);
		return obj;
	}

	public CMISObject addChild(CMISObject child) {
		children.add(child);
		child.parents.add(this);
		return child;
	}

	public CMISObject removeChild(CMISObject child) {
		children.remove(child);
		child.parents.remove(this);
		return child;
	}

	public CMISObject addPolicy(CMISObject policy) {
		policies.add(policy);
		policy.objectsUnderPolicy.add(this);
		return policy;
	}

	public CMISObject removePolicy(CMISObject policy) {
		policies.remove(policy);
		policy.objectsUnderPolicy.remove(this);
		return policy;
	}

	public Acl addAcl(Acl acl) {
		acl.setObject(this);
		getAcls().add(acl);
		return acl;
	}

	public Acl removeAcl(Acl acl) {
		getAcls().remove(acl);
		acl.setObject(null);
		return acl;
	}

	public Property addProperty(Property property) {
		property.setObject(this);
		getProperties().add(property);

		return property;
	}

	public Property removeProperty(Property property) {
		getProperties().remove(property);

		return property;
	}

	/**
	 * Remove the property with the CMIS id requested.
	 * 
	 * For multivalued properties, every property matching with the CMIS id 
	 * will be deleted. 
	 * 
	 * @param objectTypePropertyId
	 * @returns the properties removed
	 */
	public Set<Property> removeProperty(String objectTypePropertyId) {
		Set<Property> removedProperties = new LinkedHashSet<Property>();
		for (Property p : properties) {
			if (p.getObjectTypeProperty().getCmisId().equals(objectTypePropertyId)) {
				removedProperties.add(p);
			}
		}

		if (!removedProperties.isEmpty()) {
			properties.removeAll(removedProperties);
			return removedProperties;
		}
		throw new IllegalArgumentException("Cannot remove unknown property of type :" +objectTypePropertyId);
	}

	public Rendition addRendition(Rendition rendition) {
		getRenditions().add(rendition);
		rendition.setObject(this);

		return rendition;
	}

	public Rendition removeRendition(Rendition rendition) {
		getRenditions().remove(rendition);
		rendition.setObject(null);

		return rendition;
	}

	public ObjectType addSecondaryType(ObjectType objectType) {
		getSecondaryTypes().add(objectType);
		return objectType;
	}

	public ObjectType removeSecondaryType(ObjectType objectType) {
		getSecondaryTypes().remove(objectType);
		return objectType;
	}
	
	// Other public methods

	/**
	 * Returns in a lightweight way whether this object has children.
	 * 
	 * @return boolean whether this object has children.
	 */
	public boolean hasChildren() {
		ChildrenCount count = getChildrenCount();
		if (count == null || Persistence.getPersistenceUtil().isLoaded(children)) {
			return !getChildren().isEmpty();
		}
		return count.getCount() > 0;
	}

	/**
	 * Returns in a lightweight way whether this object has parents.
	 * 
	 * @return boolean whether this object has parents.
	 */
	public boolean hasParents() {
		ParentsCount count = getParentsCount();
		if (count == null || Persistence.getPersistenceUtil().isLoaded(parents)) {
			return !getParents().isEmpty();
		}
		return count.getCount() > 0;
	}

	/**
	 * Returns in a lightweight way whether this object's parents size.
	 * 
	 * @return int the parents size.
	 */
	public int getParentsSize() {
		ParentsCount count = getParentsCount();
		if (count == null || Persistence.getPersistenceUtil().isLoaded(parents)) {
			return getParents().size();
		}
		return count.getCount();
	}

	/**
	 * Returns in a lightweight way whether this object's children size.
	 * 
	 * @return int the children size.
	 */
	public int getChildrenSize() {
		ChildrenCount count = getChildrenCount();
		if (count == null || Persistence.getPersistenceUtil().isLoaded(children)) {
			return getChildren().size();
		}
		return count.getCount();
	}

	public boolean hasContentStream() {
		if (properties != null) {
			for (Property property : properties) {
				if (property.getObjectTypeProperty().getCmisId().equals(PropertyIds.CONTENT_STREAM_ID)) {
					return property.getTypedValue() != null;
				}
			}
		}
		return false;
	}

	public boolean isFolder() {
		return getObjectType().getBase().getCmisId().equals(TYPE_CMIS_FOLDER);
	}

	public boolean isPolicy() {
		return getObjectType().getBase().getCmisId().equals(TYPE_CMIS_POLICY);
	}

	public boolean isDocument() {
		return getObjectType().getBase().getCmisId().equals(TYPE_CMIS_DOCUMENT);
	}

	public boolean isRootFolder() {
		if (!isFolder()) {
			return false;
		}
		return CMIS_PATH_SEP.equals(getPropertyValue(PropertyIds.PATH));
	}

	public boolean isPwc() {
		return isDocument() && Boolean.TRUE.equals(getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
	}

	public boolean isVersionSeriesCheckout() {
		// TODO [porrjai] Fix the Acl scenarios and remove the equals: The property MUST never be null.
		return Boolean.TRUE.equals(getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));
	}

	/**
	 * Returns the path segment using the property cmis:name
	 * @return
	 */
	public String getPathSegment() {
		if (isRootFolder()) {
			// Root folder's path segment must be an empty String, but the CMIS name can't be empty for any object.
			return "";
		}
		return getPropertyValue(PropertyIds.NAME);
	}

	public <T> T getPropertyValue (String propertyName) {
		Property property;
		try {
			property = getProperty(propertyName);
		} catch (PropertyNotFoundException e) {
			return null;
		}
		return property.getTypedValue();
	}

	public Property getProperty (String propertyName) throws PropertyNotFoundException {
		Iterator<Property> it = this.getProperties().iterator();
		while (it.hasNext()) {
			Property p = it.next();
			if (p.getObjectTypeProperty().getCmisId().equals(propertyName)) {
				return p;
			}
		}
		throw new PropertyNotFoundException("Cannot find property:"+propertyName + " of object: " + this);
	}

	public List<Object> getMultipleValues(String propertyName) throws PropertyNotFoundException {
		List<Property> values = getMultiValuedProperty(propertyName);
		//sort by id
		Collections.sort(values); 
		List<Object> ans = new ArrayList<>();
		for (Property property : values) {
			ans.add(property.getTypedValue());
		}
		return ans;
	}

	public List<Property> getMultiValuedProperty (String propertyName) throws PropertyNotFoundException {
		Iterator<Property> it = this.getProperties().iterator();
		List<Property> ans =  new ArrayList<Property>();
		while (it.hasNext()) {
			Property p = it.next();
			if (p.getObjectTypeProperty().getCardinality().equals(Cardinality.SINGLE)) {
				throw new PropertyNotFoundException("Cannot get values of single cardinality property :"+propertyName+". Use getProperty instead");
			}
			if (p.getObjectTypeProperty().getCmisId().equals(propertyName)) {
				ans.add(p);
			}
		}
		if (ans.size() <= 0) {
			throw new PropertyNotFoundException("Cannot find property:"+propertyName + " of object: " + this);
		}
		return ans;
	}

	/**
	 * Get property map with the existing object properties 
	 * @return
	 */
	public Map<String, List<Property>> getPropertyMap() {
		Map<String, List<Property>> map = new LinkedHashMap<String, List<Property>>();
		for(Property p: properties) {
			String cmisId = p.getObjectTypeProperty().getCmisId();
			List<Property> properties = map.get(cmisId);
			if(properties == null) {
				properties = new ArrayList<Property>();
			}
			properties.add(p);
			map.put(cmisId, properties);
		}
		
		return Collections.unmodifiableMap(map);
	}
	
	@Override
	public String toString() {
		try {
			return id+"["+cmisObjectId+" "+objectType.getCmisId()+"]";
		} catch (Exception e) {
			// if cannot access objectType ...
			return id+"["+cmisObjectId+"]";
		}
	}

	@Override
	public CMISObject clone() throws CloneNotSupportedException {
		CMISObject ans = new CMISObject();
		// TODO [porrjai] review ACL clone to copy and renditions.

		List<String> autogeneratedPropertyIds = Arrays.asList(PropertyIds.OBJECT_ID,
				PropertyIds.BASE_TYPE_ID,
				PropertyIds.CREATED_BY,
				PropertyIds.CREATION_DATE,
				PropertyIds.LAST_MODIFIED_BY,
				PropertyIds.LAST_MODIFICATION_DATE,
				PropertyIds.CHANGE_TOKEN,

				PropertyIds.IS_IMMUTABLE,
				PropertyIds.IS_LATEST_VERSION,
				PropertyIds.IS_MAJOR_VERSION,
				PropertyIds.IS_LATEST_MAJOR_VERSION,
				PropertyIds.IS_PRIVATE_WORKING_COPY,
				PropertyIds.VERSION_LABEL,
				PropertyIds.VERSION_SERIES_ID,
				PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,
				PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,
				PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,
				PropertyIds.CHECKIN_COMMENT,
				PropertyIds.CONTENT_STREAM_LENGTH,
				PropertyIds.CONTENT_STREAM_MIME_TYPE,
				PropertyIds.CONTENT_STREAM_FILE_NAME,
				PropertyIds.CONTENT_STREAM_ID);
		
		
		for (Property property : getProperties()) {
			if(!autogeneratedPropertyIds.contains(property.getObjectTypeProperty().getCmisId())) {
				Property p = new Property(property.getObjectTypeProperty(), property.getTypedValue());
				ans.addProperty(p);
			}
		}
		ans.setObjectType(new ObjectType(this.getObjectType().getCmisId()));
		
		//id, parents etc should not be cloned
		return ans;
	}

	public CMISObject deepClone() throws CloneNotSupportedException {
		CMISObject ans = new CMISObject();
		ans.setIndexStateContent(getIndexStateContent());
		ans.setIndexTriesContent(getIndexTriesContent());
		ans.setIndexStateMetadata(getIndexStateMetadata());
		ans.setIndexTriesMetadata(getIndexTriesMetadata());
		//same reference as original object
		ans.setObjectType(getObjectType());

		for (Property p : getProperties()) {
			ObjectTypeProperty otp = p.getObjectTypeProperty();

			Object value = PropertyIds.IS_LATEST_VERSION.equals(otp.getCmisId())?false:p.getTypedValue();
			ans.addProperty(new Property(otp, value));
		}

		// Note: Acls are controlled by eu.trade.repo.security.AccessControl

		for (CMISObject parent : getParents()) {
			//keep the same references
			ans.addParent(parent);
		}

		//TODO renditions unimplemented
		return ans;
	}

	
	public void updateProperties(Set<Property> updatedProperties) {
		for (Property updateProperty : updatedProperties) {
			String updPropId = updateProperty.getObjectTypeProperty().getCmisId();
			
			boolean found = false;
			Iterator<Property> iterator = getProperties().iterator();
			while(iterator.hasNext() && !found) {
				if(updPropId.equals(iterator.next().getObjectTypeProperty().getCmisId())) {
					found = true;
				}
			}
			
			if (found) {
				try {
					getProperty(updPropId).setTypedValue(updateProperty.getTypedValue());
				} catch (PropertyNotFoundException e) {
					LOG.warn("Failed to update non-existent Property "+updPropId+" on object "+ this);
				}
			} else {
				addProperty(updateProperty);
			}
		}
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cmisObjectId == null ? 0 : cmisObjectId.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ( !(obj instanceof CMISObject) ) {
			return false;
		}
		CMISObject other = (CMISObject) obj;
		if (cmisObjectId == null) {
			return false;
		}
		return cmisObjectId.equals(other.cmisObjectId);
	}

	// INNER CLASSES
	public static class CmisIdComparator implements Comparator<CMISObject>, Serializable {
		private static final long serialVersionUID = 5589927767167670105L;
		private final List<Integer> idsOrder;

		public CmisIdComparator(List<Integer> idsOrder) {
			this.idsOrder = idsOrder;
		}

		@Override
		public int compare(CMISObject o1, CMISObject o2) {
			Integer o1Index = idsOrder.indexOf(o1.getId());
			Integer o2Index = idsOrder.indexOf(o2.getId());

			return o1Index.compareTo(o2Index);
		}
	}
}
