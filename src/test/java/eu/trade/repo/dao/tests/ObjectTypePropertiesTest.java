package eu.trade.repo.dao.tests;

import static junit.framework.Assert.assertEquals;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.LazyInitializationException;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;

public class ObjectTypePropertiesTest extends BaseTestClass {

	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		ObjectType base = objTypeSelector.getObjectTypeByCmisId(TestConstants.TEST_REPO_2, "cmis:document");
		
		ObjectTypeProperty property = new ObjectTypeProperty();
		property.setCmisId("prop:test1");
		property.setQueryName("test:query1");
		property.setObjectType(base);
		property.setCardinality(Cardinality.SINGLE);
		property.setChoices("choices");
		property.setOpenChoice(true);
		property.setDefaultValue("prop:test");
		property.setDescription("Name of the property");
		property.setDisplayName("prop:test");
		property.setLocalName("prop:test");
		property.setLocalNamespace("test:namespace");
		property.setOrderable(true);
		property.setQueryable(true);
		property.setRequired(true);
		property.setUpdatability(Updatability.READONLY);
		//DATE PROP
		property.setPropertyType(PropertyType.DATETIME);
		property.setResolution(DateTimeResolution.DATE);
		property.setMaxLength(null);
		property.setMaxValue(null);
		property.setMinValue(null);
		property.setPrecision(null);
		
		utilService.persist(property);
		
		property.setCmisId("prop:test2");
		property.setQueryName("test:query2");
		property.setId(null);
		//DECIMAL PROP
		property.setPropertyType(PropertyType.DECIMAL);
		property.setMaxValue(30);
		property.setMinValue(20);
		property.setPrecision(DecimalPrecision.BITS32);
		property.setMaxLength(null);
		property.setResolution(null);

		utilService.persist(property);

		property.setCmisId("prop:test3");
		property.setQueryName("test:query3");
		property.setId(null);
		//STRING PROP
		property.setPropertyType(PropertyType.STRING);
		property.setMaxValue(null);
		property.setMinValue(null);
		property.setPrecision(null);
		property.setMaxLength(20);
		property.setResolution(null);

		utilService.persist(property);

		compareTable(
				"object_type_property", 
				"cmis_id like 'prop:%'", 
				"objectTypeProperty-test.xml");
	}
	
    @Test
    @Transactional
    public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		ObjectTypeProperty prop = objTypePropSelector.getObjTypeProperty("test:document", "test:prop1", TestConstants.TEST_REPO_2);
		
		assertEquals(143, prop.getId().intValue());
		assertEquals(Cardinality.SINGLE, prop.getCardinality());
		assertEquals(false, prop.getRequired().booleanValue());
		assertEquals(true, prop.getQueryable().booleanValue());
		assertEquals(true, prop.getOrderable().booleanValue());
		assertEquals(Updatability.READONLY, prop.getUpdatability());
		assertEquals(PropertyType.ID, prop.getPropertyType());
		assertEquals("test:prop1", prop.getQueryName());
		assertEquals("test:prop1", prop.getLocalName());
		assertEquals("test:prop1", prop.getCmisId());
		assertEquals("ID of the parent folder of the folder", prop.getDescription());
		assertEquals("test:document", prop.getObjectType().getCmisId());
		
		assertEquals(null, prop.getChoices());
		assertEquals(null, prop.getDefaultValue());
		assertEquals(null, prop.getDefaultValue());
		assertEquals(null, prop.getDisplayName());
		assertEquals(null, prop.getLocalNamespace());
		assertEquals(null, prop.getMaxLength());
		assertEquals(null, prop.getMaxValue());
		assertEquals(null, prop.getMinValue());
		assertEquals(null, prop.getOpenChoice());
		assertEquals(null, prop.getPrecision());
		assertEquals(null, prop.getResolution());
		
		
    	//SINGLE GET (FOLDER)
    	String propname = PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS;
    	assertEquals(propname, objTypePropSelector.getFolderProperty(propname, TestConstants.TEST_REPO_2).getCmisId());

    	propname = PropertyIds.PATH;
    	assertEquals(propname, objTypePropSelector.getFolderProperty(propname, TestConstants.TEST_REPO_2).getCmisId());
    	    	
    	//SINGLE GET (DOC)
    	propname = "cmis:isMajorVersion";
    	assertEquals(propname, objTypePropSelector.getDocumentProperty(propname, TestConstants.TEST_REPO_2).getCmisId());

    	propname = PropertyIds.IS_IMMUTABLE;
    	assertEquals(propname, objTypePropSelector.getDocumentProperty(propname, TestConstants.TEST_REPO_2).getCmisId());
    }
    
	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	
		ObjectTypeProperty property = objTypePropSelector.getObjTypeProperty("test:document", "test:prop1", TestConstants.TEST_REPO_2);
		
		property.setCardinality(Cardinality.MULTI);
		property.setOrderable(false);
		property.setQueryable(false);
		property.setRequired(false);
		property.setOpenChoice(true);
		property.setUpdatability(Updatability.READONLY);
		property.setPropertyType(PropertyType.DECIMAL);
		property.setLocalName("prop:test");
		property.setDescription("...");
		property.setChoices("choices");
		property.setDefaultValue("prop:test");
		property.setDisplayName("prop:test");
		property.setLocalNamespace("test:namespace");
		
		property.setMaxValue(30);
		property.setMinValue(20);
		property.setPrecision(DecimalPrecision.BITS32);
		property.setMaxLength(null);
		property.setResolution(null);

		utilService.merge(property);
		
		ObjectTypeProperty prop = objTypePropSelector.getObjTypeProperty("test:document", "test:prop1", TestConstants.TEST_REPO_2);
		assertEquals(Cardinality.MULTI, prop.getCardinality());
		assertEquals(false, prop.getRequired().booleanValue());
		assertEquals(false, prop.getQueryable().booleanValue());
		assertEquals(false, prop.getOrderable().booleanValue());
		assertEquals(true, prop.getOpenChoice().booleanValue());
		assertEquals(Updatability.READONLY, prop.getUpdatability());
		assertEquals(PropertyType.DECIMAL, prop.getPropertyType());
		assertEquals("prop:test", prop.getLocalName());
		assertEquals("...", prop.getDescription());
		assertEquals("choices", prop.getChoices());
		assertEquals("prop:test", prop.getDefaultValue());
		assertEquals("prop:test", prop.getDisplayName());
		assertEquals("test:namespace", prop.getLocalNamespace());
		
		assertEquals(30, prop.getMaxValue().intValue());
		assertEquals(20, prop.getMinValue().intValue());
		assertEquals(DecimalPrecision.BITS32, prop.getPrecision());
		assertEquals(null, prop.getResolution());
		assertEquals(null, prop.getMaxLength());
	}
	
	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		assertEquals(58, objTypePropSelector.getAllObjectTypeProperties().size());
		utilService.removeDetached(objTypePropSelector.getObjTypeProperty("test:document", "test:prop1", TestConstants.TEST_REPO_2));
		assertEquals(57, objTypePropSelector.getAllObjectTypeProperties().size());
	}
	
    @Test(expected=RuntimeException.class)
    public void negativeScenarios_unknownprop() throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	String propname = "unknownProperty";
    	objTypePropSelector.getFolderProperty(propname, TestConstants.TEST_REPO_2);
    	objTypePropSelector.getDocumentProperty(propname, TestConstants.TEST_REPO_2);
    }
    @Test(expected=RuntimeException.class)
    public void negativeScenarios_wrongprop()throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	String propname = PropertyIds.IS_IMMUTABLE;
    	objTypePropSelector.getFolderProperty(propname, TestConstants.TEST_REPO_2);
    }
    @Test(expected=RuntimeException.class)
    public void negativeScenarios_wrongprop2() throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	String propname = PropertyIds.PATH; //is FOLDER property
    	objTypePropSelector.getDocumentProperty(propname, TestConstants.TEST_REPO_2);
    }
    
    @Test(expected=LazyInitializationException.class)
    public void negativeScenarios_withNoType()throws Exception {
    	setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
    	
    	ObjectTypeProperty prop = objTypePropSelector.getObjTypeProperty("test:document", "test:prop1", TestConstants.TEST_REPO_2);
    	prop.getObjectType().getCmisId();
    }
    
}
