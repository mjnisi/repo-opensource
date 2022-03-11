package eu.trade.repo.service.cmis.data.util;

import java.math.BigInteger;
import java.util.Iterator;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Property;

/**
 * CMIS Data general utility methods.
 */
public final class Utilities {

	private Utilities() {
	}

	/**
	 * Returns the single value of the property with the specified propertyName within the Properties.
	 * If the property is not present then returns null.
	 * 
	 * @param properties {@link Properties} The CMIS properties.
	 * @param propertyName {@link String} The property name.
	 * @param expected {@link Class<T>} Expected type for the single value.
	 * @return <T> The single value of the property with the specified propertyName within the Properties. If the property is not present then returns null.
	 * @throws IllegalArgumentException When the type of the single value does not match with the expected type.
	 */
	public static <T> T getProperty(Properties properties, String propertyName, Class<T> expected) {
		PropertyData<?> propertyData = properties.getProperties().get(propertyName);
		if (propertyData == null) {
			return null;
		}
		Object value = propertyData.getFirstValue();
		if (expected.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		throw new IllegalArgumentException(String.format("Property %s has a nonexpected type: %s; expected: %s", propertyName, value.getClass(), expected));
	}
	
	public static <T> T getPropertyTypedValue(CMISObject cmisObject, String propertyId) {
		
		T typedValue = null;
		
		Iterator<Property> it = cmisObject.getProperties().iterator();
		boolean foundProperty = false;
		while(it.hasNext()) {
			Property prop = it.next();
			String propCmisId = prop.getObjectTypeProperty().getCmisId();
			if(propertyId.equals(propCmisId)) {
				typedValue = prop.getTypedValue();
				foundProperty = true;
			}
		}
		
		if(!foundProperty) {
			throw new CmisInvalidArgumentException(String.format("Property of type %s not found on supplied object.", propertyId));
		}
		
		return typedValue;
		
	}
	
	public static boolean getChildrens(BigInteger depth) {
		return depth == null || depth.compareTo(BigInteger.ZERO) != 0;
	}

	public static BigInteger decDepth(BigInteger depth) {
		if (depth == null || depth.compareTo(BigInteger.ZERO) < 0) {
			return null;
		}
		return depth.subtract(BigInteger.ONE);
	}
}
