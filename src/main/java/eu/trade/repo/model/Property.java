package eu.trade.repo.model;

import static eu.trade.repo.model.ObjectTypeProperty.PropertyTypeValueMapping.NORMALIZED_VALUE;
import static eu.trade.repo.model.ObjectTypeProperty.PropertyTypeValueMapping.VALUE;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

import eu.trade.repo.index.triggers.handlers.PropertyChangeListener;
import eu.trade.repo.search.codecs.CMISPropertyTypeCodecUtil;

@Entity  
@EntityListeners(PropertyChangeListener.class)
@NamedQueries ({
	@NamedQuery(name="property.all", query="select p from Property p"),
	@NamedQuery(name="property.by_cmisObjectId_and_queryName", query="select p from Property p left join fetch p.objectTypeProperty where p.object.cmisObjectId = :cmisObjectId and p.objectTypeProperty.queryName = :queryName"),
	@NamedQuery(name="property.valueOf", query="select p.value from Property p where p.object.cmisObjectId = :cmisObjectId and p.objectTypeProperty.cmisId = :objectTypePropId"),
	@NamedQuery(name="property.by_type", query="select p from Property p left join fetch p.object where p.objectTypeProperty.cmisId = :objectTypePropCmisId"),
	@NamedQuery(name="property.objbyPathValue", query="select p.object from Property p where p.value = :value and p.objectTypeProperty.cmisId = :cmisId"),
	@NamedQuery(name="property.by_object_and_types", query="select p from Property p join fetch p.object join fetch p.objectTypeProperty where p.object.id = :objectId and p.objectTypeProperty.cmisId in ( :objectTypePropCmisIdList )"),
	@NamedQuery(name="property.update_paths", query = "update Property set value = CONCAT(:newPathToRoot , SUBSTRING(value, :idx)) where object_type_property_id = :pathPropId and (value = :oldpath or value like CONCAT(:oldpath,'/%'))"),
	@NamedQuery(name="property.by_object_id_and_property_type", query = "select p from Property p join fetch p.objectTypeProperty where p.object.id = :objectId and p.objectTypeProperty.propertyType = :propertyType" )
})
public class Property implements DBEntity, Comparable<Property> {
	private static final long serialVersionUID = 1L;


	//MODEL
	@Id
	@GeneratedValue(generator="sq_property")
	@SequenceGenerator(sequenceName= "sq_property", name="sq_property")
	private Integer id;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_id", nullable=false)
	private CMISObject object;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_type_property_id", nullable=false)
	private ObjectTypeProperty objectTypeProperty;

	@Column(nullable=false)
	private String value;

	@Column
	private String normalizedValue;

	@Column(name="numeric_value", nullable=false)
	private BigDecimal numericValue;

	public Property() {
		super();
	}

	public Property(ObjectTypeProperty objectTypeProperty, Object value) {
		super();
		this.objectTypeProperty = objectTypeProperty;
		initValue(value);
	}

	private void initValue(Object object) {
		PropertyType propertyType = this.getObjectTypeProperty().getPropertyType();
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);

		validatePropertyValue(object, propertyType);

		if(VALUE == valueMapping) {
			setValue(CMISPropertyTypeCodecUtil.codecFor(propertyType).<String, Object>encode(object));
			setNumericValue(BigDecimal.ZERO);
		}else if(NORMALIZED_VALUE == valueMapping) {
			setValue(CMISPropertyTypeCodecUtil.codecFor(propertyType).<String, Object>encode(object));
			setNormalizedValue(CMISPropertyTypeCodecUtil.codecFor(propertyType).<String, Object>normalize(object));
			setNumericValue(BigDecimal.ZERO);
		} else {
			setValue(object.toString());
			setNumericValue(CMISPropertyTypeCodecUtil.codecFor(propertyType).<BigDecimal, Object>encode(object));
		}
	}

	private void validatePropertyValue(Object object, PropertyType propertyType) {
		if (PropertyType.STRING == propertyType && objectTypeProperty.getMaxLength() != null && ((String) object).length() > objectTypeProperty.getMaxLength()) {
			throw new CmisConstraintException(String.format("Invalid property value: %s. Value length cannot be greater than %s characters.",
					object, objectTypeProperty.getMaxLength()));
		}

		if (PropertyType.INTEGER == propertyType || PropertyType.DECIMAL == propertyType) {
			BigDecimal checkedValue = CMISPropertyTypeCodecUtil.codecFor(propertyType).encode(object);
			if (objectTypeProperty.getMaxValue() != null && checkedValue.compareTo(new BigDecimal(objectTypeProperty.getMaxValue())) == 1) {
				throw new CmisConstraintException(String.format("Invalid property value: %s. Value cannot be bigger than %s.",
						object, objectTypeProperty.getMaxLength()));
			}
			if (objectTypeProperty.getMinValue() != null && checkedValue.compareTo(new BigDecimal(objectTypeProperty.getMinValue())) == -1) {
				throw new CmisConstraintException(String.format("Invalid property value: %s. Value cannot be less than %s.",
						object, objectTypeProperty.getMaxLength()));
			}
		}
	}


	//GETTERS/SETTERS
	@Override
	public Integer getId() 		{	return this.id;	}


	public <T> T getTypedValue() {
		PropertyType propertyType = this.getObjectTypeProperty().getPropertyType();
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);

		if(VALUE == valueMapping || NORMALIZED_VALUE == valueMapping) {
			return CMISPropertyTypeCodecUtil.codecFor(propertyType).decode(getValue());
		} else {
			return CMISPropertyTypeCodecUtil.codecFor(propertyType).decode(getNumericValue());
		}
	}

	public <T> void setTypedValue(T value) {
		initValue(value);
	}

	private String getValue() 	{
		return value;
	}

	private BigDecimal getNumericValue()  {       return numericValue; }
	private String getNormalizedValue(){	return normalizedValue;	}
	public CMISObject getObject() 	{	return this.object;	}
	public ObjectTypeProperty getObjectTypeProperty() {	return this.objectTypeProperty;	}

	public void setObject(CMISObject object){	this.object = object;	}
	private void setValue(String value) 	{
		this.value = value;
	}
	@Override
	public void setId(Integer id) 		{	this.id = id;	}
	public final void setObjectTypeProperty(ObjectTypeProperty objectTypeProperty) {	this.objectTypeProperty = objectTypeProperty;	}
	private void setNumericValue(BigDecimal orderValue) { this.numericValue = orderValue; }
	private void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }


	@Override
	public String toString() {
		try {
			return id+"[("+objectTypeProperty.getId()+")"+objectTypeProperty.getCmisId()+" -> "+value+"]";
		} catch (Exception e) {
			// if cannot access object type, then print only this property info.
			return id+"["+value+"]";
		}
	}

	@Override
	public int compareTo(Property o) {
		if (this.getId()==null || o==null || o.getId()==null ) {
			return -1;
		}

		if (this.getId() > o.getId()) {
			return 1;
		}
		if (this.getId() < o.getId()) {
			return -1;
		}
		return 0;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
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
		if (!(obj instanceof Property)) {
			return false;
		}
		Property other = (Property) obj;
		if (id == null || other.id == null) {
			return false;
		}
		return id.equals(other.id);
	}
}
