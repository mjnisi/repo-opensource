package eu.trade.repo.service.cmis.data.in;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;

/**
 * Utility class to build a {@link CMISObject}
 */
public final class CMISObjectBuilder {


	private static final Map<Class<?>, PropertyType> DEDUCED_PROPERTY_TYPES = new HashMap<Class<?>, PropertyType>() {
		private static final long serialVersionUID = 1L;
		{
			put(GregorianCalendar.class,  PropertyType.DATETIME);
			put(Date.class,  PropertyType.DATETIME);
			put(String.class,	PropertyType.STRING);
			put(BigInteger.class, PropertyType.INTEGER);
			put(Integer.class, PropertyType.INTEGER);
			put(BigDecimal.class, PropertyType.DECIMAL);
			put(Double.class, PropertyType.DECIMAL);
			put(Boolean.class, PropertyType.BOOLEAN);
		}
	};

	private CMISObjectBuilder() {
	}

	public static CMISObject build(Properties properties, String folderId) {
		CMISObject object = new CMISObject();

		// check properties
		if ( null == properties || null == properties.getProperties() ) {
			throw new CmisInvalidArgumentException("Properties must be set!");
		}

		String objectTypeId = getTypeId(properties);
		if (objectTypeId != null) {
			object.setObjectType(new ObjectType(objectTypeId));
		}

		if (folderId != null && !folderId.isEmpty()) {
			CMISObject parent = new CMISObject();
			parent.setCmisObjectId(folderId);
			object.addParent(parent);
		}

		for (Property property : buildProperties(properties)) {
			object.addProperty(property);
		}

		return object;
	}

	public static Set<Property> buildProperties(Properties properties) {
		Set<Property> propertySet = new HashSet<>();
		if (properties != null && properties.getProperties() != null) {
			for (PropertyData<?> propData : properties.getProperties().values()) {
				if( !isEmptyProperty(propData) ) {
					ObjectTypeProperty propType = new ObjectTypeProperty(propData.getId());
					List<?> valueList = propData.getValues();
	
					for (Object value : valueList) {
						propType.setPropertyType(deducePropertyType(value));
						propertySet.add(new Property(propType, value));
					}
				}
			}
		}
		return propertySet;
	}

	/**
	 * Gets the type id from a set of properties.
	 */
	private static String getTypeId(Properties properties) {
		PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
		if (typeProperty == null) {
			// When updating an object you only get a set of properties to update .. cmis:objectTypeId may not be one of them, so let the caller handle this
			return null;
		}
		if (!(typeProperty instanceof PropertyId)) {
			throw new CmisInvalidArgumentException("Type id must be set!");
		}

		String typeId = ((PropertyId) typeProperty).getFirstValue();
		if (typeId == null) {
			throw new CmisInvalidArgumentException("Type id must be set!");
		}

		return typeId;
	}

	private static boolean isEmptyProperty(PropertyData<?> prop) {
		if (prop == null || prop.getValues() == null) {
			return true;
		}

		return prop.getValues().isEmpty();
	}

	//TODO check
	private static <T> PropertyType deducePropertyType(T value){
		return DEDUCED_PROPERTY_TYPES.get(value.getClass());
	}
}
