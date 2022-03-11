package eu.trade.repo.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;

import eu.trade.repo.util.Utilities;

@Entity
@Table(name="object_type_property")
@NamedQueries({
	@NamedQuery(name="objectTypeProperty.by_cmisid",query = "select otp from ObjectTypeProperty otp where otp.cmisId = :cmisid and otp.objectType = (select ot from ObjectType ot where ot.cmisId = :objectType and ot.repository.cmisId = :repositoryId)"),
	@NamedQuery(name="objectTypeProperty.by_type", 	query = "select otp from ObjectTypeProperty otp where otp.objectType = (select ot from ObjectType ot where ot.cmisId = :cmisId)"),
	@NamedQuery(name="objectTypeProperty.all", query="select otp from ObjectTypeProperty otp"),
	@NamedQuery(name="ObjectTypeProperty.countById", query="select count(*) from ObjectTypeProperty otp where otp.objectType.repository.cmisId = :repositoryId and otp.objectType.cmisId != :objectTypeCmisId and otp.cmisId = :objectTypePropertyCmisId"),
	@NamedQuery(name="ObjectTypeProperty.countByQueryName", query="select count(*) from ObjectTypeProperty otp where otp.objectType.repository.cmisId = :repositoryId and otp.objectType.cmisId != :objectTypeCmisId and otp.queryName = :queryName"),
})

public class ObjectTypeProperty implements DBEntity {
	private static final long serialVersionUID = 1L;

	public enum PropertyTypeValueMapping {VALUE("value", true), NUMERIC_VALUE("numericValue", false), NORMALIZED_VALUE("normalizedValue", true);

	private final String valueField;
	private final boolean stringSubtype;

	PropertyTypeValueMapping(String valueField, boolean stringSubtype) {
		this.valueField = valueField;
		this.stringSubtype = stringSubtype;
	}

	public String getValueField() {
		return valueField;
	}
	
	public boolean isStringSubtype() {
		return stringSubtype;
	}

	@Override
	public String toString() {
		return this.valueField;
	}
	}

	private static int propertyTypeValue = 0;
	private static int propertTypeOrderValue = 1;

	private static Map<PropertyType, PropertyTypeValueMapping>  propertyTypeValueTypeMappings = new HashMap<PropertyType, PropertyTypeValueMapping>() {
		private static final long serialVersionUID = 1L;
		{
			put(PropertyType.STRING,  ObjectTypeProperty.PropertyTypeValueMapping.NORMALIZED_VALUE);
			put(PropertyType.ID,      ObjectTypeProperty.PropertyTypeValueMapping.VALUE);
			put(PropertyType.DATETIME, ObjectTypeProperty.PropertyTypeValueMapping.VALUE);
			put(PropertyType.URI, ObjectTypeProperty.PropertyTypeValueMapping.VALUE);
			put(PropertyType.HTML, ObjectTypeProperty.PropertyTypeValueMapping.VALUE);

			put(PropertyType.INTEGER, ObjectTypeProperty.PropertyTypeValueMapping.NUMERIC_VALUE);
			put(PropertyType.BOOLEAN, ObjectTypeProperty.PropertyTypeValueMapping.NUMERIC_VALUE);
			put(PropertyType.DECIMAL, ObjectTypeProperty.PropertyTypeValueMapping.NUMERIC_VALUE);
		}
	};

	private static final List<String> streamPropTypeCmisIdList = new ArrayList<String>(){
		private static final long serialVersionUID = 1L;
		{
			add(PropertyIds.CONTENT_STREAM_ID);
			add(PropertyIds.CONTENT_STREAM_FILE_NAME);
			add(PropertyIds.CONTENT_STREAM_MIME_TYPE);
			add(PropertyIds.CONTENT_STREAM_LENGTH);
		}
	};

	private static final List<String> updatePropTypeCmisIdList = new ArrayList<String>(){
		private static final long serialVersionUID = 1L;
		{
			add(PropertyIds.LAST_MODIFICATION_DATE);
			add(PropertyIds.LAST_MODIFIED_BY);
			add(PropertyIds.CHANGE_TOKEN);
		}
	};

	//MODEL
	@Id
	@GeneratedValue(generator="sq_object_type_property")
	@SequenceGenerator(sequenceName= "sq_object_type_property", name="sq_object_type_property")
	private Integer id;

	@Column(name="cmis_id")
	private String cmisId;

	@Column(name="default_value")
	private String defaultValue;

	@Column(name="display_name")
	private String displayName;

	@Column(name="local_name")
	private String localName;

	@Column(name="local_namespace")
	private String localNamespace;

	@Column(name="max_length")
	private Integer maxLength;

	@Column(name="max_value")
	private Integer maxValue;

	@Column(name="min_value")
	private Integer minValue;

	@Column(name="property_type")
	private String propertyType;

	@Column(name="query_name")
	private String queryName;

	private String description;

	@Basic
	@Column(nullable=false)
	private Character queryable;
	@Basic
	@Column(nullable=false)
	private Character required;
	@Basic
	@Column(nullable=false)
	private Character orderable;
	@Basic
	@Column(name="open_choice")
	private Character openChoice;


	private String resolution;
	private String updatability;
	private String cardinality;
	private String choices;
	private Integer precision;

	private transient int inverseLevel;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_type_id")
	private ObjectType objectType;

	@OneToMany(mappedBy="objectTypeProperty")
	private List<Property> properties;

	//GETTERS/SETTERS
	public static int getPropertyTypeValue() {	return propertyTypeValue;	}
	public static int getPropertTypeOrderValue() {	return propertTypeOrderValue;	}
	public static Map<PropertyType, PropertyTypeValueMapping> getPropertyTypeValueTypeMappings() {	return propertyTypeValueTypeMappings;	}
	public static List<String> getStreamPropTypeCmisIdList(){	return streamPropTypeCmisIdList;	}
	public static List<String> getUpdatePropTypeCmisIdList(){	return updatePropTypeCmisIdList;	}
	public static void setPropertyTypeValue(int propertyTypeValue) {	ObjectTypeProperty.propertyTypeValue = propertyTypeValue;	}
	public static void setPropertTypeOrderValue(int propertTypeOrderValue) {	ObjectTypeProperty.propertTypeOrderValue = propertTypeOrderValue;	}
	public static void setPropertyTypeValueTypeMappings(Map<PropertyType, PropertyTypeValueMapping> propertyTypeValueTypeMappings) {	ObjectTypeProperty.propertyTypeValueTypeMappings = propertyTypeValueTypeMappings;	}

	@Override
	public void setId(Integer id) 								{	this.id = id;	}
	public void setCmisId(String cmisId) 						{	this.cmisId = cmisId;	}
	public void setChoices(String choices) 						{	this.choices = choices;	}
	public void setRequired(Boolean required)					{	this.required = Utilities.setBooleanValue(required);	}
	public void setMinValue(Integer minValue) 					{	this.minValue = minValue;	}
	public void setMaxValue(Integer maxValue) 					{	this.maxValue = maxValue;	}
	public void setMaxLength(Integer maxLength) 				{	this.maxLength = maxLength;	}
	public void setLocalName(String localName) 					{	this.localName = localName;	}
	public void setQueryable(Boolean queryable) 				{	this.queryable = Utilities.setBooleanValue(queryable);	}
	public void setOrderable(Boolean orderable) 				{	this.orderable = Utilities.setBooleanValue(orderable);	}
	public void setQueryName(String queryName) 					{	this.queryName = queryName;	}
	public void setPrecision(DecimalPrecision precision)		{	this.precision = precision == null ? null : precision.value().intValue();	}
	public void setOpenChoice(Boolean openChoice)				{	this.openChoice = Utilities.setBooleanValue(openChoice);	}
	public void setDisplayName(String displayName) 				{	this.displayName = displayName;	}
	public void setDescription(String description) 				{	this.description = description;	}
	public void setDefaultValue(String defaultValue)			{	this.defaultValue = defaultValue;	}
	public void setCardinality(Cardinality cardinality) 		{	this.cardinality = cardinality.value();	}
	public void setPropertyType(PropertyType propertyType)		{	this.propertyType = propertyType.value();	}
	public void setObjectType(ObjectType objectType)			{	this.objectType = objectType;	}
	public void setResolution(DateTimeResolution resolution) 	{	this.resolution = resolution == null ? null: resolution.value();	}
	public void setUpdatability(Updatability updatability)		{	this.updatability = updatability.value();	}
	public void setLocalNamespace(String localNamespace)		{	this.localNamespace = localNamespace;	}
	public void setProperties(List<Property> properties) 		{	this.properties = properties;	}
	public void setInverseLevel(Integer inverseLevel)               {       this.inverseLevel = inverseLevel;   }

	@Override
	public Integer 			getId() 			{	return id;	}
	public String 			getChoices() 		{	return choices;	}
	public String 			getCmisId() 		{	return cmisId;	}
	public Cardinality 		getCardinality()	{	return Cardinality.fromValue(cardinality);	}
	public String 			getDescription()	{	return description;	}
	public String 			getDisplayName()	{	return displayName;	}
	public String 			getLocalName() 		{	return localName;	}
	public Integer 			getMaxLength() 		{	return maxLength;	}
	public Integer 			getMaxValue() 		{	return maxValue;	}
	public Integer 			getMinValue()		{	return minValue;	}
	public Boolean 			getOpenChoice() 	{	return Utilities.getBooleanValue(openChoice);	}
	public DecimalPrecision getPrecision() 		{	return precision == null ? null : DecimalPrecision.fromValue(BigInteger.valueOf(precision));	}
	public String 			getQueryName() 		{	return queryName;	}
	public Boolean 			getOrderable() 		{	return Utilities.getBooleanValue(orderable);	}
	public Boolean 			getQueryable() 		{	return Utilities.getBooleanValue(queryable);	}
	public Boolean 			getRequired() 		{	return Utilities.getBooleanValue(required);	}
	public String 			getDefaultValue() 	{	return defaultValue;	}
	public PropertyType		getPropertyType() 	{	return PropertyType.fromValue(propertyType);	}
	public DateTimeResolution 	getResolution() {	return resolution == null ? null : DateTimeResolution.fromValue(resolution);	}
	public Updatability 	getUpdatability() 	{	return Updatability.fromValue(updatability);	}
	public String 			getLocalNamespace() {	return localNamespace;	}
	public ObjectType 		getObjectType() 	{	return objectType;	}
	public List<Property> 	getProperties() 	{	return properties;	}
	public Integer getInverseLevel()                { return inverseLevel;}

	//ADDS/REMOVES
	public Property addProperty(Property property) {
		getProperties().add(property);
		property.setObjectTypeProperty(this);

		return property;
	}

	@Override
	public String toString() {
		return id+"("+inverseLevel+")"+"["+cmisId+","+queryName+"]";
	}
	public Property removeProperty(Property property) {
		getProperties().remove(property);
		property.setObjectTypeProperty(null);

		return property;
	}
	public ObjectTypeProperty(String cmisId) {
		super();
		this.cmisId = cmisId;
	}

	public ObjectTypeProperty() {
		super();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ObjectTypeProperty that = (ObjectTypeProperty) o;

		if (!cmisId.equals(that.cmisId)) {
			return false;
		}
		return objectType.equals(that.objectType);
	}

	@Override
	public int hashCode() {
		int result = cmisId.hashCode();
		result = 31 * result + objectType.hashCode();
		return result;
	}

	public boolean isInherited() {
		return inverseLevel > 0;
	}
	
	
	/**
	 * Compare all the attributes of the object type property
	 */
	public static class ObjectTypePropertyComparator implements Comparator<ObjectTypeProperty>, Serializable {
		
		private static final long serialVersionUID = 8366366082537974643L;
		
		private static final List<String> METHOD_NAMES = Collections.unmodifiableList(Arrays.asList("getId", "getObjectType", "getCmisId", "getLocalName", "getLocalNamespace",
				"getQueryName", "getDisplayName", "getDescription", "getPropertyType",
				"getCardinality", "getUpdatability", "getRequired", "getQueryable",
				"getOrderable", "getChoices", "getOpenChoice", "getDefaultValue",
				"getMinValue", "getMaxValue", "getResolution", "getPrecision",
				"getMaxLength"));
		
		@Override
		public int compare(ObjectTypeProperty o1, ObjectTypeProperty o2) {
			
			try {
				Method[] methods = ObjectTypeProperty.class.getDeclaredMethods();
				int diff = 0;
	        	for(Method method: methods) {
	        		if(METHOD_NAMES.contains(method.getName())) {
	        			Object property1 = method.invoke(o1);
	        			Object property2 = method.invoke(o2);
	        			if(!equalsOrNulls(property1, property2)) {
			            	diff++;
			            }
	        		}
	        	}
	        	return diff;
			} catch (IllegalAccessException | InvocationTargetException e) {
				return 1;
			}
		}
		
		private boolean equalsOrNulls(Object o1, Object o2) {
			if(o1 == o2) {
				return true;
			}

			return (o1 == null || o1.equals(o2)) && (o2 == null || o2.equals(o1));
		}
	}
	
	public static final Comparator FULL_COMPARATOR = new ObjectTypeProperty.ObjectTypePropertyComparator();
}
