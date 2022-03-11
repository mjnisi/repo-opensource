package eu.trade.repo.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;

import eu.trade.repo.util.Utilities;

@Entity
@Table(name="object_type")
@NamedQueries({
	@NamedQuery(name="ObjectType.objectTypeByQueryName", query = "select ot from ObjectType ot where ot.queryName = :queryname and ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.objectTypeByCmisid", query = "select ot from ObjectType ot left join fetch ot.base b where ot.cmisId = :cmisId and ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.objectTypeByCmisid_with_base_parent_properties", query="select ot from ObjectType ot left join fetch ot.parent parent left join fetch ot.base base where ot.cmisId = :cmisId and ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.children", query="select ot from ObjectType ot where ot.repository.cmisId = :repositoryId and ot.parent.cmisId = :cmisId"),
	@NamedQuery(name="ObjectType.children_with_base_parent_properties", query="select ot from ObjectType ot left join fetch ot.parent parent left join fetch ot.base base where ot.repository.cmisId = :repositoryId and ot.parent.cmisId = :cmisId"),
	@NamedQuery(name="ObjectType.children_with_dependencies", query="select ot from ObjectType ot where ot.repository.cmisId = :repositoryId and ot.parent.cmisId = :cmisId"),
	@NamedQuery(name="ObjectType.countChildren", query="select count(ot) from ObjectType ot where ot.repository.cmisId = :repositoryId and ot.parent.cmisId = :cmisId"),
	@NamedQuery(name="ObjectType.alltypes", query="select ot from ObjectType ot where ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.alltypes_with_dependencies", query="select ot from ObjectType ot where ot.repository.cmisId = :repositoryId "),
	@NamedQuery(name="ObjectType.countyByCmisId", query = "select count(ot) from ObjectType ot where ot.cmisId = :cmisId and ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.countyByQueryName", query = "select count(ot) from ObjectType ot where ot.queryName = :queryname and ot.repository.cmisId = :repositoryId"),
	@NamedQuery(name="ObjectType.baseTypes", query="select ot from ObjectType ot where ot.repository.cmisId = :repositoryId and ot.parent is null"),

})
public class ObjectType implements DBEntity {

	private static final long serialVersionUID = 1L;

	private static class TreeComparator implements Comparator<ObjectTypeProperty>, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int compare(ObjectTypeProperty otp1, ObjectTypeProperty otp2) {
			return otp1.getInverseLevel().compareTo(otp2.getInverseLevel());
		}
	}

	private static final TreeComparator TREE_COMPARATOR = new TreeComparator();

	//MODEL
	@Id
	@GeneratedValue(generator="sq_object_type")
	@SequenceGenerator(sequenceName= "sq_object_type", name="sq_object_type")
	private Integer id;

	@Column(name="content_stream_allowed", length=100)
	private String contentStreamAllowed = "notallowed";

	@Column(name="controllable_acl", nullable=false)
	private Character controllableAcl;

	@Basic
	@Column(name="controllable_policy", nullable=false)
	private Character controllablePolicy;

	@Column(name="display_name", length=100)
	private String displayName;

	@Basic
	@Column(name="fulltext_indexed", nullable=false)
	private Character fulltextIndexed;

	@Basic
	@Column(name="included_in_supertype_query", nullable=false)
	private Character includedInSupertypeQuery;

	@Column(name="local_name")
	private String localName;

	@Column(name="local_namespace")
	private String localNamespace;

	@Column(name="query_name")
	private String queryName;

	@Column(name="cmis_id")
	private String cmisId;

	@Basic
	@Column(nullable=false)
	private Character queryable;

	@Basic
	private Character versionable;

	@Basic
	@Column(nullable=false)
	private Character fileable;

	@Basic
	@Column(nullable=false)
	private Character creatable;

	@Column(length=100)
	private String description;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="base_id", referencedColumnName="id")
	private ObjectType base;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="parent_id", referencedColumnName="id")
	private ObjectType parent;

	@OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
	private List<ObjectType> children;

	@OneToMany(mappedBy="objectType", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@OrderBy("id")
	private Set<ObjectTypeProperty> objectTypeProperties = new LinkedHashSet<ObjectTypeProperty>();

	@OneToMany(mappedBy="objectType", cascade={CascadeType.MERGE, CascadeType.PERSIST}, fetch=FetchType.LAZY)
	@OrderBy("id")
	private Set<ObjectTypeRelationship> objectTypeRelationships = new LinkedHashSet<ObjectTypeRelationship>();

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="repository_id")
	private Repository repository;

	//GETTERS/SETTERS
	@Override
	public void setId(Integer id) 							{	this.id = id;	}
	public void setCmisId(String cmsmid) 					{	this.cmisId = cmsmid;	}
	public void setFileable(Boolean fileable)				{	this.fileable = Utilities.setBooleanValue(fileable);}
	public void setControllablePolicy(Boolean controllablePolicy) 	{	this.controllablePolicy  = Utilities.setBooleanValue(controllablePolicy);	}
	public void setControllableAcl(Boolean controllableAcl) {	this.controllableAcl  = Utilities.setBooleanValue(controllableAcl);	}
	public void setContentStreamAllowed(String contentStreamAllowed){	this.contentStreamAllowed = ContentStreamAllowed.fromValue(contentStreamAllowed).value();	}
	public void setContentStreamAllowed(ContentStreamAllowed contentStreamAllowed){ this.contentStreamAllowed = null != contentStreamAllowed? contentStreamAllowed.value() : null;	}
	public void setLocalNamespace(String localNamespace)	{	this.localNamespace = localNamespace;	}
	public void setParent(ObjectType parent)				{	this.parent = parent;	}
	public void setChildren(List<ObjectType> children)				{	this.children = children;	}
	public void setBase(ObjectType base) 					{	this.base = base;	}
	public void setLocalName(String localName) 				{	this.localName = localName;	}
	public void setQueryName(String queryName) 				{	this.queryName = queryName;	}
	public void setQueryable(Boolean queryable) 			{	this.queryable  = Utilities.setBooleanValue(queryable);	}
	public void setDisplayName(String displayName) 			{	this.displayName = displayName;	}
	public void setDescription(String description) 			{	this.description = description;	}
	public void setCreatable(Boolean creatable) 			{	this.creatable  = Utilities.setBooleanValue(creatable);	}
	public void setVersionable(Boolean versionable) 		{	this.versionable  = Utilities.setBooleanValue(versionable);	}
	public void setFulltextIndexed(Boolean fulltextIndexed) {	this.fulltextIndexed  = Utilities.setBooleanValue(fulltextIndexed);	}
	public void setObjectTypeProperties(Set<ObjectTypeProperty> objectTypeProperties) {	this.objectTypeProperties = objectTypeProperties;	}
	public void setObjectTypeRelationships(Set<ObjectTypeRelationship> objectTypeRelationships) {	this.objectTypeRelationships = objectTypeRelationships;	}
	public void setIncludedInSupertypeQuery(Boolean includedInSupertypeQuery) {	this.includedInSupertypeQuery  = Utilities.setBooleanValue(includedInSupertypeQuery);	}
	public void setRepository(Repository repository)		{	this.repository = repository;	}

	@Override
	public Integer  getId() 								{	return id;	}
	public String   getCmisId() 							{	return cmisId;	}
	public String   getQueryName() 							{	return queryName;	}
	public Boolean  isQueryable() 							{	return Utilities.getBooleanValue(queryable);	}
	public Boolean  isCreatable() 							{	return Utilities.getBooleanValue(creatable);	}
	public Boolean  isFileable() 							{	return Utilities.getBooleanValue(fileable);	}
	public Repository getRepository() 						{	return repository; }
	public String   getLocalName() 							{	return localName;	}
	public String   getDescription() 						{	return description;	}
	public String   getDisplayName() 						{	return displayName;	}
	public Boolean  isVersionable() 						{	return Utilities.getBooleanValue(versionable);	}
	public ObjectType 	getBase()							{	return base;	}
	public ObjectType 	getParent() 						{	return parent;	}
	public List<ObjectType> 	getChildren() 						{	return children;	}
	public ContentStreamAllowed   getContentStreamAllowed()	{	return null == contentStreamAllowed? null : ContentStreamAllowed.fromValue(contentStreamAllowed);	}
	public Boolean  isControllableAcl() 					{	return Utilities.getBooleanValue(controllableAcl);	}
	public Boolean  isControllablePolicy() 				{	return Utilities.getBooleanValue(controllablePolicy);	}
	public Boolean  isFulltextIndexed() 					{	return Utilities.getBooleanValue(fulltextIndexed);	}
	public String 	getLocalNamespace() 					{	return localNamespace;	}
	public Boolean  isIncludedInSupertypeQuery() 			{	return Utilities.getBooleanValue(includedInSupertypeQuery);	}
	public Set<ObjectTypeRelationship> getObjectTypeRelationships()	{	return objectTypeRelationships;	}
	public Set<ObjectTypeProperty> getObjectTypeProperties() {	return objectTypeProperties;	}

	//ADDS/REMOVES
	public CMISObject addObject(CMISObject object) {
		object.setObjectType(this);

		return object;
	}
	public CMISObject removeObject(CMISObject object) {
		object.setObjectType(null);

		return object;
	}
	public ObjectTypeProperty addObjectTypeProperty(ObjectTypeProperty objectTypeProperty) {
		objectTypeProperty.setObjectType(this);
		getObjectTypeProperties().add(objectTypeProperty);

		return objectTypeProperty;
	}
	public ObjectTypeProperty removeObjectTypeProperty(ObjectTypeProperty objectTypeProperty) {
		getObjectTypeProperties().remove(objectTypeProperty);
		objectTypeProperty.setObjectType(null);

		return objectTypeProperty;
	}
	public ObjectTypeRelationship addObjectTypeRelationship(ObjectTypeRelationship objectTypeRelationship) {
		objectTypeRelationship.setObjectType(this);
		getObjectTypeRelationships().add(objectTypeRelationship);

		return objectTypeRelationship;
	}
	public ObjectTypeRelationship removeObjectTypeRelationship(ObjectTypeRelationship objectTypeRelationship) {
		getObjectTypeRelationships().remove(objectTypeRelationship);
		objectTypeRelationship.setObjectType(null);

		return objectTypeRelationship;
	}

	public ObjectType(String cmisId) {
		super();
		this.cmisId = cmisId;
	}

	public ObjectType() {
		super();
	}
	
	/**
	 * Returns a Map with the properties.
	 * 
	 * The key is the cmisId of the property.
	 * The value is the ObjectTypeProperty.
	 *  
	 * @return
	 */
	public Map<String, ObjectTypeProperty> getObjectTypePropertiesMap() {
		Map<String, ObjectTypeProperty> properties = new LinkedHashMap<>();
		for(ObjectTypeProperty otp: this.getObjectTypeProperties()) {
			properties.put(otp.getCmisId(), otp);
		}
		return Collections.unmodifiableMap(properties);
	}

	//TODO: method to get Set of ALL ObjectTypeProperties

	/**
	 * Return all the properties of this object type including the inherited properties from its parents.
	 * <p>
	 * Depending on the <code>byCmisId</code> parameter the map keys will be the properties' cmisId or the properties' query name.
	 * Note that in both cases, currently it is possible to have duplicated cmisId or query names. In that case the mapped set will contain more than one element,
	 * with the first element being the nearest property type in the hierarchy to this object type.
	 * <p>
	 * TODO: Since the order for the properties set are given by the level in the hierarchy, maybe a simple LinkedHashSet could be enough (removing the comparator).
	 * TODO: By the way, who needs all the elements in the Set.
	 * 
	 * @param byCmisID boolean Whether to use the properties' cmisId as a key or the properties' query name.
	 * @param includeOptional boolean Whether to include all the properties or just he required ones.
	 * @return {@link Map<String, SortedSet<ObjectTypeProperty>>} All the properties of this object type including the inherited properties from its parents.
	 */
	public Map<String, SortedSet<ObjectTypeProperty>> getObjectTypePropertiesIncludeParents(boolean byCmisID, boolean includeOptional) {

		Map<String, SortedSet<ObjectTypeProperty>> otpMap = new LinkedHashMap<String, SortedSet<ObjectTypeProperty>>();

		ObjectType ot = this;

		int inverseLevel = 0;

		do {
			for (ObjectTypeProperty otp : ot.getObjectTypeProperties()) {
				if(otp.getRequired() || includeOptional) {
					otp.setInverseLevel(inverseLevel);

					String key = byCmisID?otp.getCmisId():otp.getQueryName();

					SortedSet<ObjectTypeProperty> otpSet = otpMap.get(key);

					if(otpSet == null) {

						otpSet = new TreeSet<ObjectTypeProperty>(TREE_COMPARATOR);

						otpMap.put(key, otpSet);
					}

					otpSet.add(otp);
				}
			}

			inverseLevel++;
			ot = ot.getParent();
		} while (ot != null);

		return otpMap;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		/*
		 * Object o could be a proxy class from Hibernate,
		 * for this reason we need to check if could be casted 
		 */
		if (o == null  || !(o instanceof ObjectType)) {
			return false;
		}

		ObjectType that = (ObjectType) o;

		/*
		 * The getter methods are used because the object
		 * could be a proxy class from Hibernate
		 */
		if (!cmisId.equals(that.getCmisId())) {
			return false;
		}
		return repository.equals(that.getRepository());
	}

	@Override
	public int hashCode() {
		int result = cmisId.hashCode();
		result = 31 * result + repository.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return id+"["+cmisId+" "+queryName+"]";
	}
}
