package eu.trade.repo.service.cmis.data.out;

import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;

import eu.trade.repo.model.Property;

/**
 * {@link PropertyData} wrapper to handle multivalued properties.
 * <p>
 * 
 * @author porrjai
 */
public class PropertyDataWrapper<T> {

	private final PropertyData<T> propertyData;
	private final Cardinality cardinality;

	/**
	 * New instance
	 * 
	 * @param propertyData
	 * @param cardinality
	 */
	PropertyDataWrapper(PropertyData<T> propertyData, Cardinality cardinality) {
		this.propertyData = propertyData;
		this.cardinality = cardinality;
	}

	void addValue(Property property) {
		if (cardinality.equals(Cardinality.SINGLE)) {
			throw new IllegalArgumentException(String.format("Trying to assign twice the value for a single property type: [id: %s, display name: %s]",  propertyData.getId(), propertyData.getDisplayName()));
		}
		propertyData.getValues().add(property.<T>getTypedValue());
	}

	/**
	 * @return the propertyData
	 */
	PropertyData<T> getPropertyData() {
		return propertyData;
	}
}
