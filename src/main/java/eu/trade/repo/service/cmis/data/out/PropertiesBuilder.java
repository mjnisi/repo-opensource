package eu.trade.repo.service.cmis.data.out;

import static eu.trade.repo.util.Constants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;

/**
 * Helper class to build the {@link Properties} collection of a {@link CMISObject}.
 * 
 * @author porrjai
 */
public final class PropertiesBuilder {

	private static final String FILTER_SEP = ",";
	private static final String FILTER_ALL = "*";

	private PropertiesBuilder() {
	}

	/**
	 * Builds the set of filtered properties' query names.
	 * 
	 * @param filter {@link String} Comma separated list of filtered properties' query names.
	 * @return {@link Set<String>} set of filtered properties' query names.
	 */
	public static Map<String, String> buildFilter(String filter) {
		if (filter == null || filter.isEmpty() || filter.equals(FILTER_ALL)) {
			return null;
		}
		String[] filteredQueryNames = filter.split(FILTER_SEP);

		Map<String, String> filterMap = new HashMap<>();
		for(String filteredQueryName : filteredQueryNames) {
			filterMap.put(filteredQueryName, null);
		}

		return filterMap;
	}

	/**
	 * Returns the object's properties.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object
	 * @param filter {@link String} Comma separated list of filtered properties' query names.
	 * @return {@link Properties} the object's properties.
	 */
	public static Properties build(CMISObject cmisObject, String filter) {
		return build(cmisObject, buildFilter(filter));
	}
	
	public static Properties build(CMISObject cmisObject) {
		return build(cmisObject, "");
	}

	private static Map<String, String> checkFilter(Map<String, String> filter) {
		if (filter != null && filter.containsKey(FILTER_ALL)) {
			return null;
		}
		return filter;
	}
	/**
	 * Returns the object's properties.
	 * 
	 * @param cmisObject {@link CMISObject} The cmis object
	 * @param filter {@link Set<String>} set of filtered properties' query names.
	 * @return {@link Properties} the object's properties.
	 */
	public static Properties build(CMISObject cmisObject, Map<String, String> filter) {
		Map<String, String> checkedFilter = checkFilter(filter);
		Map<Integer, PropertyDataWrapper<?>> properties = new HashMap<>();
		for (Property property : cmisObject.getProperties()) {
			ObjectTypeProperty objectTypeProperty = property.getObjectTypeProperty();
			if (isInFilter(objectTypeProperty, checkedFilter)) {
				Integer objectTypePropertyId = objectTypeProperty.getId();
				PropertyDataWrapper<?> propertyDataWrapper = properties.get(objectTypePropertyId);
				if (propertyDataWrapper == null) {
					properties.put(objectTypePropertyId, getPropertyData(property, objectTypeProperty, filter));
				}
				else {
					propertyDataWrapper.addValue(property);
				}
			}
		}
		PropertiesImpl propertiesImpl = new PropertiesImpl();
		for (PropertyDataWrapper<?> propertyDataWrapper : properties.values()) {
			propertiesImpl.addProperty(propertyDataWrapper.getPropertyData());
		}

		//ADD SCORE
		if(filter != null && filter.containsKey(CMIS_QUERY_SCORE)) {

			String scorePropertyName = filter.get(CMIS_QUERY_SCORE);
			if(scorePropertyName == null) {
				scorePropertyName = CMIS_SCORE_PROP;
			}

			AbstractPropertyData<BigDecimal> scoreData = new PropertyDecimalImpl();

			scoreData.setId(CMIS_SCORE_PROP);
			scoreData.setDisplayName(CMIS_SCORE_PROP);
			scoreData.setLocalName(CMIS_SCORE_PROP);
			scoreData.setQueryName(scorePropertyName);
			scoreData.setValue(new BigDecimal(cmisObject.getScore()));

			propertiesImpl.addProperty(scoreData);
		}

		fillWithNotSetProperties(cmisObject, propertiesImpl, checkedFilter);
		return propertiesImpl;
	}

	private static void fillWithNotSetProperties(CMISObject cmisObject, PropertiesImpl propertiesImpl, Map<String, String> filter) {
		Map<String, PropertyData<?>> currentPropertiesByCmisId = propertiesImpl.getProperties();
		Map<String, SortedSet<ObjectTypeProperty>> allPropertiesByCmisId = cmisObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true);
		for (Map.Entry<String, SortedSet<ObjectTypeProperty>> allPropertiesEntry : allPropertiesByCmisId.entrySet()) {
			if (!currentPropertiesByCmisId.containsKey(allPropertiesEntry.getKey())) {
				ObjectTypeProperty notSetObjectTypeProperty = allPropertiesEntry.getValue().first();
				if (isInFilter(notSetObjectTypeProperty, filter)) {
					PropertyData<?> notSetPropertyData = getPropertyDataImpl(notSetObjectTypeProperty, filter);
					propertiesImpl.addProperty(notSetPropertyData);
				}
			}
		}
	}

	private static boolean isInFilter(ObjectTypeProperty objectTypeProperty, Map<String, String> filter) {
		return filter == null || filter.containsKey(objectTypeProperty.getQueryName());
	}

	private static PropertyDataWrapper<?> getPropertyData(Property property, ObjectTypeProperty objectTypeProperty, Map<String, String> filter) {
		return fillPropertyData(getPropertyDataImpl(objectTypeProperty, filter), property, objectTypeProperty);
	}

	private static AbstractPropertyData<?> getPropertyDataImpl(ObjectTypeProperty objectTypeProperty, Map<String, String> filter) {
		AbstractPropertyData<?> propertyData = instancePropertyDataImpl(objectTypeProperty);
		propertyData.setId(objectTypeProperty.getCmisId());
		propertyData.setDisplayName(objectTypeProperty.getDisplayName());
		propertyData.setLocalName(objectTypeProperty.getLocalName());

		String queryName = objectTypeProperty.getQueryName();
		String alias = null;
		if(filter != null && filter.containsKey(queryName)) {
			alias = filter.get(queryName);
		}
		propertyData.setQueryName(alias != null ? alias : queryName);

		return propertyData;
	}

	private static AbstractPropertyData<?> instancePropertyDataImpl(ObjectTypeProperty objectTypeProperty) {
		PropertyType propertyType = objectTypeProperty.getPropertyType();
		switch (propertyType) {
			case BOOLEAN:
				return new PropertyBooleanImpl();

			case DATETIME:
				return new PropertyDateTimeImpl();

			case DECIMAL:
				return new PropertyDecimalImpl();

			case HTML:
				return new PropertyHtmlImpl();

			case ID:
				return new PropertyIdImpl();

			case INTEGER:
				return new PropertyIntegerImpl();

			case STRING:
				return new PropertyStringImpl();

			case URI:
				return new PropertyUriImpl();
		}
		throw new IllegalArgumentException("Unknown datatype! Spec change?");
	}

	private static <T, E extends AbstractPropertyData<T>> PropertyDataWrapper<T> fillPropertyData(E propertyData, Property property, ObjectTypeProperty objectTypeProperty) {
		Cardinality cardinality = objectTypeProperty.getCardinality();
		PropertyDataWrapper<T> propertyDataWrapper = new PropertyDataWrapper<>(propertyData, cardinality);
		if (cardinality.equals(Cardinality.SINGLE)) {
			propertyData.setValue(property.<T>getTypedValue());
		}
		else {
			List<T> values = new ArrayList<>();
			values.add(property.<T>getTypedValue());
			propertyData.setValues(values);
		}
		return propertyDataWrapper;
	}
}
