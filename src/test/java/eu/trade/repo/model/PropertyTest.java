package eu.trade.repo.model;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import static org.junit.Assert.assertNotNull;

public class PropertyTest extends BaseTestClass {

	@Test(expected = CmisConstraintException.class)
	public void testStringPropertyValidation() {

		ObjectTypeProperty propertyType = new ObjectTypeProperty();
		propertyType.setCmisId(PropertyIds.NAME);
		propertyType.setQueryName(PropertyIds.NAME);
		propertyType.setPropertyType(PropertyType.STRING);

		//used for numeric values validation
		propertyType.setMaxValue(50);
		propertyType.setMinValue(20);

		//used for string values validation
		propertyType.setMaxLength(10);

		assertNotNull(new Property(propertyType, "Document1"));
		new Property(propertyType, "ToLongName_ShouldThrowException");
	}

	@Test
	public void testStringPropertyValidation_MaxLengthNotSet() {

		ObjectTypeProperty propertyType = new ObjectTypeProperty();
		propertyType.setCmisId(PropertyIds.NAME);
		propertyType.setQueryName(PropertyIds.NAME);
		propertyType.setPropertyType(PropertyType.STRING);

		//used for numeric values validation
		propertyType.setMaxValue(50);
		propertyType.setMinValue(20);

		//max length not set - no validation should be done
		propertyType.setMaxLength(null);

		assertNotNull(new Property(propertyType, "Document1"));
		assertNotNull(new Property(propertyType, "LongName_but_property_maxLength_notSet"));
	}

	@Test(expected = CmisConstraintException.class)
	public void testNumericPropertyToBigValidation() {
		ObjectTypeProperty propertyType = new ObjectTypeProperty();
		propertyType.setCmisId(PropertyIds.NAME);
		propertyType.setQueryName(PropertyIds.NAME);
		propertyType.setPropertyType(PropertyType.INTEGER);

		//used for numeric values validation
		propertyType.setMaxValue(50);
		propertyType.setMinValue(20);

		//used for string values validation
		propertyType.setMaxLength(10);

		assertNotNull(new Property(propertyType, 30));
		new Property(propertyType, 2000); //value to big
	}

	@Test
	public void testNumericProperty_MaxMinValueNotSet() {
		ObjectTypeProperty propertyType = new ObjectTypeProperty();
		propertyType.setCmisId(PropertyIds.NAME);
		propertyType.setQueryName(PropertyIds.NAME);
		propertyType.setPropertyType(PropertyType.INTEGER);

		//used for numeric values validation
		propertyType.setMaxValue(null);
		propertyType.setMinValue(null);

		//used for string values validation
		propertyType.setMaxLength(10);

		//max min value not set, not exception expected
		assertNotNull(new Property(propertyType, 30));
		assertNotNull(new Property(propertyType, 2000));
	}

	@Test(expected = CmisConstraintException.class)
	public void testNumericPropertyToSmallValidation() {
		ObjectTypeProperty propertyType = new ObjectTypeProperty();
		propertyType.setCmisId(PropertyIds.NAME);
		propertyType.setQueryName(PropertyIds.NAME);
		propertyType.setPropertyType(PropertyType.DECIMAL);

		//used for numeric values validation
		propertyType.setMaxValue(50);
		propertyType.setMinValue(20);

		//used for string values validation
		propertyType.setMaxLength(10);

		assertNotNull(new Property(propertyType, 30.0));
		new Property(propertyType, 5.5); //value to small
	}
}
