package eu.trade.repo.service.cmis.data.in;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.DateTimeResolution;
import org.apache.chemistry.opencmis.commons.enums.DecimalPrecision;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RelationshipTypeDefinitionImpl;
import org.junit.Assert;
import org.junit.Test;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectTypeRelationship;
import eu.trade.repo.model.ObjectTypeRelationship.RelationshipType;
import eu.trade.repo.util.Constants;


public class ObjectTypeBuilderTest {
	
	
	/**
	 * Basic document test, no properties 
	 */
	@Test
	public void testBuildDocument01() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		checkObjectType(objectType, "cmis:document");
	}

	
	/**
	 * Document test, with properties 
	 */
	@Test
	public void testBuildDocument02() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		checkObjectType(objectType, "cmis:document");
		checkObjectTypeProperties(objectType);
	}
	
	
	/**
	 * Folder test 
	 */
	@Test
	public void testBuildFolder03() {
		FolderTypeDefinitionImpl def = buildFolderTypeDefinition(true);
		
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		checkObjectType(objectType, "cmis:folder");
		checkObjectTypeProperties(objectType);
	}

	/**
	 * Relationship test 
	 */
	@Test
	public void testBuildRelationship04() {
		RelationshipTypeDefinitionImpl def = buildRelationshipTypeDefinition(true);
		
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		checkObjectType(objectType, "cmis:relationship");
		checkObjectTypeProperties(objectType);
	}
	
	/**
	 * type id test 
	 */
	@Test
	public void testBuildDocumentAttribute05() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setId("new-id");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getCmisId(), "new-id");
		
		def.setId("new-id2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getCmisId(), "new-id2");
	}

	/**
	 * type local name test
	 */
	@Test
	public void testBuildDocumentAttribute06() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setLocalName("new-localName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getLocalName(), "new-localName");

		def.setLocalName("new-localName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getLocalName(), "new-localName2");
	}

	/**
	 * type local namespace test
	 */
	@Test
	public void testBuildDocumentAttribute07() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setLocalNamespace("new-localNamespace");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getLocalNamespace(), "new-localNamespace");

		def.setLocalNamespace("new-localNamespace2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getLocalNamespace(), "new-localNamespace2");
	}

	/**
	 * type query name test
	 */
	@Test
	public void testBuildDocumentAttribute08() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setQueryName("new-queryName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getQueryName(), "new-queryName");

		def.setQueryName("new-queryName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getQueryName(), "new-queryName2");
	}

	/**
	 * type display name test
	 */
	@Test
	public void testBuildDocumentAttribute09() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setDisplayName("new-displayName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getDisplayName(), "new-displayName");

		def.setDisplayName("new-displayName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getDisplayName(), "new-displayName2");
	}	
	
	/**
	 * type description test
	 */
	@Test
	public void testBuildDocumentAttribute10() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setDescription("new-description");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getDescription(), "new-description");

		def.setDescription("new-description2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getDescription(), "new-description2");
	}		
	
	/**
	 * base type test
	 */
	@Test
	public void testBuildDocumentAttribute11() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getBase().getCmisId(), BaseTypeId.CMIS_DOCUMENT.value());

		def.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getBase().getCmisId(), BaseTypeId.CMIS_FOLDER.value());

		def.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getBase().getCmisId(), BaseTypeId.CMIS_RELATIONSHIP.value());

		def.setBaseTypeId(BaseTypeId.CMIS_POLICY);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getBase().getCmisId(), BaseTypeId.CMIS_POLICY.value());

		def.setBaseTypeId(BaseTypeId.CMIS_ITEM);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getBase().getCmisId(), BaseTypeId.CMIS_ITEM.value());
		
		def.setBaseTypeId(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(objectType.getBase());
	}	
	
	/**
	 * parent type test
	 */
	@Test
	public void testBuildDocumentAttribute12() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setParentTypeId("new-parent");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getParent().getCmisId(), "new-parent");

		def.setParentTypeId("new-parent2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getParent().getCmisId(), "new-parent2");
		
		//if the id and the parent is the same is considered a root type
		def.setId("cmis:document");
		def.setParentTypeId("cmis:document");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(objectType.getParent());

		//if the id and the parent is the same is considered a root type
		def.setId("trade:doc");
		def.setParentTypeId("trade:doc");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(objectType.getParent());
		
		//if the parent is empty is considered a root type
		def.setParentTypeId(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(objectType.getParent());
	}	
	
	/**
	 * creatable type test
	 */
	@Test
	public void testBuildDocumentAttribute13() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsCreatable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isCreatable(), true);

		def.setIsCreatable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isCreatable(), false);
	}	

	/**
	 * fileable type test
	 */
	@Test
	public void testBuildDocumentAttribute14() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsFileable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isFileable(), true);

		def.setIsFileable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isFileable(), false);
	}	

	/**
	 * queryable type test
	 */
	@Test
	public void testBuildDocumentAttribute15() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsQueryable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isQueryable(), true);

		def.setIsQueryable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isQueryable(), false);
	}

	/**
	 * policy controllable type test
	 */
	@Test
	public void testBuildDocumentAttribute16() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsControllablePolicy(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isControllablePolicy(), true);

		def.setIsControllablePolicy(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isControllablePolicy(), false);
	}	
	

	/**
	 * acl controllable type test
	 */
	@Test
	public void testBuildDocumentAttribute17() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsControllableAcl(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isControllableAcl(), true);

		def.setIsControllableAcl(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isControllableAcl(), false);
	}
	
	/**
	 * full text indexed type test
	 */
	@Test
	public void testBuildDocumentAttribute18() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsFulltextIndexed(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isFulltextIndexed(), true);

		def.setIsFulltextIndexed(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isFulltextIndexed(), false);
	}
	
	/**
	 * include in supertype test
	 */
	@Test
	public void testBuildDocumentAttribute19() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsIncludedInSupertypeQuery(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isIncludedInSupertypeQuery(), true);

		def.setIsIncludedInSupertypeQuery(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isIncludedInSupertypeQuery(), false);
	}
	
	
	/**
	 * is versionable test
	 */
	@Test
	public void testBuildDocumentAttribute20() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setIsVersionable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isVersionable(), true);

		def.setIsVersionable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.isVersionable(), false);
	}
	
	/**
	 * content stream test
	 */
	@Test
	public void testBuildDocumentAttribute21() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(false);
		
		def.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getContentStreamAllowed(), ContentStreamAllowed.ALLOWED);

		def.setContentStreamAllowed(ContentStreamAllowed.NOTALLOWED);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getContentStreamAllowed(), ContentStreamAllowed.NOTALLOWED);

		def.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getContentStreamAllowed(), ContentStreamAllowed.REQUIRED);
	}

	/**
	 * source/target relationship types test
	 */
	@Test
	public void testBuildRelationshipAttribute22() {
		RelationshipTypeDefinitionImpl def = buildRelationshipTypeDefinition(false);
		def.setAllowedSourceTypes(Collections.EMPTY_LIST);
		def.setAllowedTargetTypes(Collections.EMPTY_LIST);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeRelationships().size(), 0);
		
		def.setAllowedSourceTypes(Arrays.asList(new String[] {"s1", "s2"}));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeRelationships().size(), 2);
		
		def.setAllowedTargetTypes(Arrays.asList(new String[] {"t1"}));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeRelationships().size(), 3);
		
		def.setAllowedTargetTypes(Arrays.asList(new String[] {"t2", "t3"}));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		ObjectTypeRelationship[] otr = objectType.getObjectTypeRelationships().toArray(new ObjectTypeRelationship[0]);
		Assert.assertEquals(otr.length, 4);
		Assert.assertEquals(otr[0].getReferencedObjectType().getCmisId(), "s1");
		Assert.assertEquals(otr[1].getReferencedObjectType().getCmisId(), "s2");
		Assert.assertEquals(otr[2].getReferencedObjectType().getCmisId(), "t2");
		Assert.assertEquals(otr[3].getReferencedObjectType().getCmisId(), "t3");
		
		Assert.assertEquals(otr[0].getType(), RelationshipType.SOURCE);
		Assert.assertEquals(otr[1].getType(), RelationshipType.SOURCE);
		Assert.assertEquals(otr[2].getType(), RelationshipType.TARGET);
		Assert.assertEquals(otr[3].getType(), RelationshipType.TARGET);
	}

	/**
	 * check type properties (local name)
	 */
	@Test
	public void testBuildDocumentProperties23() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setLocalName("new-localName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getLocalName(), "new-localName");
		propDef.setLocalName("new-localName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getLocalName(), "new-localName2");
	}
	
	/**
	 * check type properties (local namespace)
	 */
	@Test
	public void testBuildDocumentProperties24() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setLocalNamespace("new-localNamespace");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getLocalNamespace(), "new-localNamespace");
		propDef.setLocalNamespace("new-localNamespace2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getLocalNamespace(), "new-localNamespace2");
	}

	/**
	 * check type properties (query name)
	 */
	@Test
	public void testBuildDocumentProperties25() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setQueryName("new-queryName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getQueryName(), "new-queryName");
		propDef.setQueryName("new-queryName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getQueryName(), "new-queryName2");
	}
	
	/**
	 * check type properties (display name)
	 */
	@Test
	public void testBuildDocumentProperties26() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setDisplayName("new-displayName");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getDisplayName(), "new-displayName");
		propDef.setDisplayName("new-displayName2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getDisplayName(), "new-displayName2");
	}	
	
	
	/**
	 * check type properties (description)
	 */
	@Test
	public void testBuildDocumentProperties27() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setDescription("new-description");
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getDescription(), "new-description");
		propDef.setDescription("new-description2");
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getDescription(), "new-description2");
	}
	
	/**
	 * check type properties (property type)
	 */
	@Test
	public void testBuildDocumentProperties28() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		for(PropertyType type: PropertyType.values()) {
			propDef.setPropertyType(type);
			ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
			Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getPropertyType(), type);
		}
		
	}

	/**
	 * check type properties (cardinality)
	 */
	@Test
	public void testBuildDocumentProperties29() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setCardinality(Cardinality.SINGLE);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getCardinality(), Cardinality.SINGLE);
		
		propDef.setCardinality(Cardinality.MULTI);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getCardinality(), Cardinality.MULTI);
	}

	
	/**
	 * check type properties (updatability)
	 */
	@Test
	public void testBuildDocumentProperties30() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		for(Updatability updatability: Updatability.values()) {
			propDef.setUpdatability(updatability);
			ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
			Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getUpdatability(), updatability);
		}
	}
	
	
	/**
	 * check type properties (required)
	 */
	@Test
	public void testBuildDocumentProperties31() {

		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setIsRequired(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getRequired(), true);
		
		propDef.setIsRequired(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getRequired(), false);
	}	
	
	/**
	 * check type properties (queryable)
	 */
	@Test
	public void testBuildDocumentProperties32() {

		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setIsQueryable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getQueryable(), true);
		
		propDef.setIsQueryable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getQueryable(), false);
	}
	
	/**
	 * check type properties (orderable)
	 */
	@Test
	public void testBuildDocumentProperties33() {

		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyBooleanDefinitionImpl propDef = 
				(PropertyBooleanDefinitionImpl)def.getPropertyDefinitions().get("boolean-id");
		
		propDef.setIsOrderable(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getOrderable(), true);
		
		propDef.setIsOrderable(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "boolean-id").getOrderable(), false);
	}	
	
	/**
	 * check type properties (default value)
	 */
	@Test
	public void testBuildDocumentProperties34() {

		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyStringDefinitionImpl propDef = 
				(PropertyStringDefinitionImpl)def.getPropertyDefinitions().get("string-id");
		
		propDef.setDefaultValue(Collections.singletonList("new-default"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getDefaultValue(), "new-default");

		propDef.setDefaultValue(Collections.singletonList("new-default2"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getDefaultValue(), "new-default2");

		propDef.setDefaultValue(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getDefaultValue());

		propDef.setDefaultValue(Collections.EMPTY_LIST);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getDefaultValue());

		propDef.setDefaultValue(Arrays.asList(new String[] {"new-default3", "new-default4"}));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getDefaultValue(), "new-default3");

	}
	
	
	/**
	 * check type properties (choices)
	 */
	@Test
	public void testBuildDocumentProperties35() {

		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyStringDefinitionImpl propDef = 
				(PropertyStringDefinitionImpl)def.getPropertyDefinitions().get("string-id");
		
		List<Choice<String>> choices = new ArrayList<Choice<String>>();
		ChoiceImpl<String> choice = new ChoiceImpl<String>();
		choice.setValue(Collections.singletonList("option A"));
		choices.add(choice);
		choice = new ChoiceImpl<String>();
		choice.setValue(Collections.singletonList("option B"));
		choices.add(choice);

		propDef.setChoices(choices);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getChoices(),
				"option A"+Constants.CMIS_MULTIVALUE_SEP+"option B");
		
		propDef.setChoices(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getChoices());
		
		
		choice = new ChoiceImpl<String>();
		choice.setValue(Collections.singletonList("option C"));
		choices.add(choice);
		propDef.setChoices(choices);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getChoices(),
				"option A"+Constants.CMIS_MULTIVALUE_SEP+"option B" + Constants.CMIS_MULTIVALUE_SEP + "option C");
		
		propDef.setChoices(Collections.EMPTY_LIST);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getChoices());
	}
	
	/**
	 * check type properties (open choice)
	 */
	@Test
	public void testBuildDocumentProperties36() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyStringDefinitionImpl propDef = 
				(PropertyStringDefinitionImpl)def.getPropertyDefinitions().get("string-id");
		
		List<Choice<String>> choices = new ArrayList<Choice<String>>();
		ChoiceImpl<String> choice = new ChoiceImpl<String>();
		choice.setValue(Collections.singletonList("A"));
		choices.add(choice);
		choice = new ChoiceImpl<String>();
		choice.setValue(Collections.singletonList("B"));
		choices.add(choice);

		
		//with options, open choice: true
		propDef.setChoices(choices);
		propDef.setIsOpenChoice(true);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getOpenChoice(), true);
		
		//with options, open choice: false
		propDef.setChoices(choices);
		propDef.setIsOpenChoice(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getOpenChoice(), false);
		
		//with options, open choice: null
		propDef.setChoices(choices);
		propDef.setIsOpenChoice(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getOpenChoice());
		
		//without options, open choice: true
		propDef.setChoices(null);
		propDef.setIsOpenChoice(true);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getOpenChoice());
		
		//without options, open choice: false
		propDef.setChoices(null);
		propDef.setIsOpenChoice(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getOpenChoice());
		
		//without options, open choice: null
		propDef.setChoices(null);
		propDef.setIsOpenChoice(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getOpenChoice());
		
	}
	
	/**
	 * check type properties (max length)
	 */
	@Test
	public void testBuildDocumentProperties37() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyStringDefinitionImpl propDef = 
				(PropertyStringDefinitionImpl)def.getPropertyDefinitions().get("string-id");
		
		propDef.setMaxLength(new BigInteger("123"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "string-id").getMaxLength(), new Integer(123));
		
		propDef.setMaxLength(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "string-id").getMaxLength());
	}
	
	
	/**
	 * check type properties (max value integer)
	 */
	@Test
	public void testBuildDocumentProperties38() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyIntegerDefinitionImpl propDef = 
				(PropertyIntegerDefinitionImpl)def.getPropertyDefinitions().get("integer-id");
		
		propDef.setMaxValue(new BigInteger("123"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "integer-id").getMaxValue(), new Integer(123));
		
		propDef.setMaxValue(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "integer-id").getMaxValue());
	}
	
	/**
	 * check type properties (min value integer)
	 */
	@Test
	public void testBuildDocumentProperties39() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyIntegerDefinitionImpl propDef = 
				(PropertyIntegerDefinitionImpl)def.getPropertyDefinitions().get("integer-id");
		
		propDef.setMinValue(new BigInteger("123"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "integer-id").getMinValue(), new Integer(123));
		
		propDef.setMinValue(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "integer-id").getMinValue());
		
		propDef.setMinValue(new BigInteger("-123"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "integer-id").getMinValue(), new Integer(-123));
	}
	
	
	/**
	 * check type properties (max value decimal)
	 */
	@Test
	public void testBuildDocumentProperties40() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyDecimalDefinitionImpl propDef = 
				(PropertyDecimalDefinitionImpl)def.getPropertyDefinitions().get("decimal-id");
		
		propDef.setMaxValue(new BigDecimal("123"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMaxValue(), new Integer(123));
		
		propDef.setMaxValue(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "decimal-id").getMaxValue());

		propDef.setMaxValue(new BigDecimal("123.456"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMaxValue(), new Integer(123));
		
		propDef.setMaxValue(new BigDecimal("-123.456"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMaxValue(), new Integer(-123));
		
	}
	
	/**
	 * check type properties (min value decimal)
	 */
	@Test
	public void testBuildDocumentProperties41() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyDecimalDefinitionImpl propDef = 
				(PropertyDecimalDefinitionImpl)def.getPropertyDefinitions().get("decimal-id");
		
		propDef.setMinValue(new BigDecimal("123"));
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMinValue(), new Integer(123));
		
		propDef.setMinValue(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "decimal-id").getMinValue());
		
		propDef.setMinValue(new BigDecimal("-123"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMinValue(), new Integer(-123));

		propDef.setMinValue(new BigDecimal("-123.456"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMinValue(), new Integer(-123));

		propDef.setMinValue(new BigDecimal("123.456"));
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getMinValue(), new Integer(123));
	}
	
	/**
	 * check type properties (precision)
	 */
	@Test
	public void testBuildDocumentProperties42() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyDecimalDefinitionImpl propDef = 
				(PropertyDecimalDefinitionImpl)def.getPropertyDefinitions().get("decimal-id");
		
		propDef.setPrecision(DecimalPrecision.BITS32);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getPrecision(), DecimalPrecision.BITS32);
		
		propDef.setPrecision(DecimalPrecision.BITS64);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "decimal-id").getPrecision(), DecimalPrecision.BITS64);
		
		propDef.setPrecision(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "decimal-id").getPrecision());
	}
	
	
	/**
	 * check type properties (resolution)
	 */
	@Test
	public void testBuildDocumentProperties43() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyDateTimeDefinitionImpl propDef = 
				(PropertyDateTimeDefinitionImpl)def.getPropertyDefinitions().get("dateTime-id");
		
		propDef.setDateTimeResolution(DateTimeResolution.DATE);
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "dateTime-id").getResolution(), DateTimeResolution.DATE);
		
		propDef.setDateTimeResolution(DateTimeResolution.TIME);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "dateTime-id").getResolution(), DateTimeResolution.TIME);
		
		propDef.setDateTimeResolution(DateTimeResolution.YEAR);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(getObjectTypeProperty(objectType, "dateTime-id").getResolution(), DateTimeResolution.YEAR);
		
		propDef.setDateTimeResolution(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertNull(getObjectTypeProperty(objectType, "dateTime-id").getResolution());
		
	}
	
	
	/**
	 * check type properties (inherited)
	 */
	@Test
	public void testBuildDocumentProperties44() {
		DocumentTypeDefinitionImpl def = buildDocumentTypeDefinition(true);
		PropertyDateTimeDefinitionImpl propDef = 
				(PropertyDateTimeDefinitionImpl)def.getPropertyDefinitions().get("dateTime-id");
		
		//all properties are not inherited
		ObjectType objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeProperties().size(), 8);

		//dateTime-id is set to null, so is like not inherited
		propDef.setIsInherited(null);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeProperties().size(), 8);
		
		//inherited true
		propDef.setIsInherited(true);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeProperties().size(), 7);
		
		//default again
		propDef.setIsInherited(false);
		objectType = ObjectTypeBuilder.build(def, "repo01");
		Assert.assertEquals(objectType.getObjectTypeProperties().size(), 8);
		
	}
	
	/**
	 * Get the ObjectTypeProperty
	 * @param objectType
	 * @param id
	 * @return
	 */
	private ObjectTypeProperty getObjectTypeProperty(ObjectType objectType, String id) {
		for(ObjectTypeProperty otp: objectType.getObjectTypeProperties()) {
			if(otp.getCmisId().equals(id)) {
				return otp;
			}
		}
		return null;
	}
	
	private void checkObjectType(ObjectType objectType, String base) {
		
		Assert.assertEquals(objectType.getCmisId(), "id");	
		Assert.assertEquals(objectType.getLocalName(), "localName");
		Assert.assertEquals(objectType.getLocalNamespace(), "localNamespace");
		Assert.assertEquals(objectType.getQueryName(), "queryName");
		Assert.assertEquals(objectType.getDisplayName(), "displayName");
		Assert.assertEquals(objectType.getBase().getCmisId(), base);
		Assert.assertEquals(objectType.getParent().getCmisId(), "parentId");
		Assert.assertEquals(objectType.getDescription(), "description");
		Assert.assertEquals(objectType.isCreatable(), true);
		Assert.assertEquals(objectType.isFileable(), true);
		Assert.assertEquals(objectType.isQueryable(), true);
		Assert.assertEquals(objectType.isControllableAcl(), true);
		Assert.assertEquals(objectType.isControllablePolicy(), true);
		Assert.assertEquals(objectType.isFulltextIndexed(), true);
		Assert.assertEquals(objectType.isIncludedInSupertypeQuery(), true);
		
		if(base.equals("cmis:document")) {
			Assert.assertEquals(objectType.isVersionable(), true);
			Assert.assertEquals(objectType.getContentStreamAllowed(), ContentStreamAllowed.ALLOWED);
		} else if(base.equals("cmis:relationship")) {
			ObjectTypeRelationship[] otr = objectType.getObjectTypeRelationships().toArray(new ObjectTypeRelationship[0]);
			Assert.assertEquals(otr.length, 4);
			
			Assert.assertEquals(otr[0].getReferencedObjectType().getCmisId(), "source1");
			Assert.assertEquals(otr[1].getReferencedObjectType().getCmisId(), "source2");
			Assert.assertEquals(otr[2].getReferencedObjectType().getCmisId(), "target1");
			Assert.assertEquals(otr[3].getReferencedObjectType().getCmisId(), "target2");
			
			Assert.assertEquals(otr[0].getType(), RelationshipType.SOURCE);
			Assert.assertEquals(otr[1].getType(), RelationshipType.SOURCE);
			Assert.assertEquals(otr[2].getType(), RelationshipType.TARGET);
			Assert.assertEquals(otr[3].getType(), RelationshipType.TARGET);
		}
	}
	
	private void checkObjectTypeProperties(ObjectType objectType) {
		Map<String, ObjectTypeProperty> properties = objectType.getObjectTypePropertiesMap();
		
		String[] types = new String[] {
				"boolean", "dateTime", "decimal", "html", "id", "integer", "string", "uri"};
		PropertyType[] propertyTypes = new PropertyType[] {
				PropertyType.BOOLEAN, PropertyType.DATETIME, PropertyType.DECIMAL, PropertyType.HTML, 
				PropertyType.ID, PropertyType.INTEGER, PropertyType.STRING, PropertyType.URI};
		//check common things
		for(int i=0;i<types.length; i++) {
			ObjectTypeProperty otp = properties.get(types[i] + "-id");
			Assert.assertEquals(otp.getLocalName(), types[i] + "-localName");
			Assert.assertEquals(otp.getLocalNamespace(), types[i] +"-localNamespace");
			Assert.assertEquals(otp.getQueryName(), types[i] +"-queryName");
			Assert.assertEquals(otp.getDisplayName(), types[i] +"-displayName");
			Assert.assertEquals(otp.getDescription(), types[i] +"-description");
			Assert.assertEquals(otp.getPropertyType(), propertyTypes[i]);
			Assert.assertEquals(otp.getCardinality(), Cardinality.SINGLE);
			Assert.assertEquals(otp.getUpdatability(), Updatability.ONCREATE);
			Assert.assertEquals(otp.getRequired(), true);
			Assert.assertEquals(otp.getQueryable(), true);
			Assert.assertEquals(otp.getOrderable(), true);
		}
		
		//specific by type
		Assert.assertEquals(properties.get("string-id").getMaxLength(), new Integer(100));
		Assert.assertEquals(properties.get("integer-id").getMaxValue(), new Integer(100));
		Assert.assertEquals(properties.get("integer-id").getMinValue(), new Integer(0));
		Assert.assertEquals(properties.get("decimal-id").getMaxValue(), new Integer(100));
		Assert.assertEquals(properties.get("decimal-id").getMinValue(), new Integer(0));
		Assert.assertEquals(properties.get("decimal-id").getPrecision(), DecimalPrecision.BITS32);
		Assert.assertEquals(properties.get("dateTime-id").getResolution(), DateTimeResolution.DATE);
	}

	private DocumentTypeDefinitionImpl buildDocumentTypeDefinition(boolean withProperties) {
		DocumentTypeDefinitionImpl def = new DocumentTypeDefinitionImpl();
		def.setId("id");
		def.setLocalName("localName");
		def.setLocalNamespace("localNamespace");
		def.setQueryName("queryName");
		def.setDisplayName("displayName");
		def.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
		def.setParentTypeId("parentId");
		def.setDescription("description");
		def.setIsCreatable(true);
		def.setIsFileable(true);
		def.setIsQueryable(true);
		def.setIsControllableAcl(true);
		def.setIsControllablePolicy(true);
		def.setIsFulltextIndexed(true);
		def.setIsIncludedInSupertypeQuery(true);
		
		def.setIsVersionable(true);
		def.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
		
		if(withProperties) {
			buildDocumentTypeDefinitionProperties(def);
		}
		
		return def;
	}
	
	private FolderTypeDefinitionImpl buildFolderTypeDefinition(boolean withProperties) {
		FolderTypeDefinitionImpl def = new FolderTypeDefinitionImpl();
		def.setId("id");
		def.setLocalName("localName");
		def.setLocalNamespace("localNamespace");
		def.setQueryName("queryName");
		def.setDisplayName("displayName");
		def.setBaseTypeId(BaseTypeId.CMIS_FOLDER);
		def.setParentTypeId("parentId");
		def.setDescription("description");
		def.setIsCreatable(true);
		def.setIsFileable(true);
		def.setIsQueryable(true);
		def.setIsControllableAcl(true);
		def.setIsControllablePolicy(true);
		def.setIsFulltextIndexed(true);
		def.setIsIncludedInSupertypeQuery(true);
		
		if(withProperties) {
			buildDocumentTypeDefinitionProperties(def);
		}
		
		return def;
	}

	
	private RelationshipTypeDefinitionImpl buildRelationshipTypeDefinition(boolean withProperties) {
		RelationshipTypeDefinitionImpl def = new RelationshipTypeDefinitionImpl();
		def.setId("id");
		def.setLocalName("localName");
		def.setLocalNamespace("localNamespace");
		def.setQueryName("queryName");
		def.setDisplayName("displayName");
		def.setBaseTypeId(BaseTypeId.CMIS_RELATIONSHIP);
		def.setParentTypeId("parentId");
		def.setDescription("description");
		def.setIsCreatable(true);
		def.setIsFileable(true);
		def.setIsQueryable(true);
		def.setIsControllableAcl(true);
		def.setIsControllablePolicy(true);
		def.setIsFulltextIndexed(true);
		def.setIsIncludedInSupertypeQuery(true);
		
		def.setAllowedSourceTypes(Arrays.asList(new String[] {"source1", "source2"}));
		def.setAllowedTargetTypes(Arrays.asList(new String[] {"target1", "target2"}));
		
		if(withProperties) {
			buildDocumentTypeDefinitionProperties(def);
		}
		
		return def;
	}
	
	private void buildDocumentTypeDefinitionProperties(AbstractTypeDefinition def) {
		
		//boolean
		{
			PropertyBooleanDefinitionImpl booleanDef = new PropertyBooleanDefinitionImpl();
			booleanDef.setIsInherited(false);
			booleanDef.setId("boolean-id");
			booleanDef.setLocalName("boolean-localName");
			booleanDef.setLocalNamespace("boolean-localNamespace");
			booleanDef.setQueryName("boolean-queryName");
			booleanDef.setDisplayName("boolean-displayName");
			booleanDef.setDescription("boolean-description");
			booleanDef.setPropertyType(PropertyType.BOOLEAN);
			booleanDef.setCardinality(Cardinality.SINGLE);
			booleanDef.setUpdatability(Updatability.ONCREATE);
			booleanDef.setIsRequired(true);
			booleanDef.setIsQueryable(true);
			booleanDef.setIsOrderable(true);
			
			//booleanDef.setDefaultValue(Collections.singletonList(true));
			
			def.addPropertyDefinition(booleanDef);
		}
		
		//dateTime 
		{
			PropertyDateTimeDefinitionImpl dateTimeDef = new PropertyDateTimeDefinitionImpl();
			dateTimeDef.setIsInherited(false);
			dateTimeDef.setId("dateTime-id");
			dateTimeDef.setLocalName("dateTime-localName");
			dateTimeDef.setLocalNamespace("dateTime-localNamespace");
			dateTimeDef.setQueryName("dateTime-queryName");
			dateTimeDef.setDisplayName("dateTime-displayName");
			dateTimeDef.setDescription("dateTime-description");
			dateTimeDef.setPropertyType(PropertyType.DATETIME);
			dateTimeDef.setCardinality(Cardinality.SINGLE);
			dateTimeDef.setUpdatability(Updatability.ONCREATE);
			dateTimeDef.setIsRequired(true);
			dateTimeDef.setIsQueryable(true);
			dateTimeDef.setIsOrderable(true);
			
			dateTimeDef.setDateTimeResolution(DateTimeResolution.DATE);
			def.addPropertyDefinition(dateTimeDef); 
		}
		
		
		//decimal
		{
			PropertyDecimalDefinitionImpl decimalDef = new PropertyDecimalDefinitionImpl();
			decimalDef.setIsInherited(false);
			decimalDef.setId("decimal-id");
			decimalDef.setLocalName("decimal-localName");
			decimalDef.setLocalNamespace("decimal-localNamespace");
			decimalDef.setQueryName("decimal-queryName");
			decimalDef.setDisplayName("decimal-displayName");
			decimalDef.setDescription("decimal-description");
			decimalDef.setPropertyType(PropertyType.DECIMAL);
			decimalDef.setCardinality(Cardinality.SINGLE);
			decimalDef.setUpdatability(Updatability.ONCREATE);
			decimalDef.setIsRequired(true);
			decimalDef.setIsQueryable(true);
			decimalDef.setIsOrderable(true);
			
			decimalDef.setMaxValue(new BigDecimal(100));
			decimalDef.setMinValue(new BigDecimal(0));
			decimalDef.setPrecision(DecimalPrecision.BITS32);
			def.addPropertyDefinition(decimalDef);
		}
		
		//html
		{
			PropertyHtmlDefinitionImpl htmlDef = new PropertyHtmlDefinitionImpl();
			htmlDef.setIsInherited(false);
			htmlDef.setId("html-id");
			htmlDef.setLocalName("html-localName");
			htmlDef.setLocalNamespace("html-localNamespace");
			htmlDef.setQueryName("html-queryName");
			htmlDef.setDisplayName("html-displayName");
			htmlDef.setDescription("html-description");
			htmlDef.setPropertyType(PropertyType.HTML);
			htmlDef.setCardinality(Cardinality.SINGLE);
			htmlDef.setUpdatability(Updatability.ONCREATE);
			htmlDef.setIsRequired(true);
			htmlDef.setIsQueryable(true);
			htmlDef.setIsOrderable(true);
			
			def.addPropertyDefinition(htmlDef);
		}
		
		//id
		{
			PropertyIdDefinitionImpl idDef = new PropertyIdDefinitionImpl();
			idDef.setIsInherited(false);
			idDef.setId("id-id");
			idDef.setLocalName("id-localName");
			idDef.setLocalNamespace("id-localNamespace");
			idDef.setQueryName("id-queryName");
			idDef.setDisplayName("id-displayName");
			idDef.setDescription("id-description");
			idDef.setPropertyType(PropertyType.ID);
			idDef.setCardinality(Cardinality.SINGLE);
			idDef.setUpdatability(Updatability.ONCREATE);
			idDef.setIsRequired(true);
			idDef.setIsQueryable(true);
			idDef.setIsOrderable(true);
			
			def.addPropertyDefinition(idDef);
		}
		
		
		//integer
		{
			PropertyIntegerDefinitionImpl integerDef = new PropertyIntegerDefinitionImpl();
			integerDef.setIsInherited(false);
			integerDef.setId("integer-id");
			integerDef.setLocalName("integer-localName");
			integerDef.setLocalNamespace("integer-localNamespace");
			integerDef.setQueryName("integer-queryName");
			integerDef.setDisplayName("integer-displayName");
			integerDef.setDescription("integer-description");
			integerDef.setPropertyType(PropertyType.INTEGER);
			integerDef.setCardinality(Cardinality.SINGLE);
			integerDef.setUpdatability(Updatability.ONCREATE);
			integerDef.setIsRequired(true);
			integerDef.setIsQueryable(true);
			integerDef.setIsOrderable(true);
			
			integerDef.setMaxValue(new BigInteger("100"));
			integerDef.setMinValue(new BigInteger("0"));
			
			def.addPropertyDefinition(integerDef);
		}
		
		//string
		{
			PropertyStringDefinitionImpl stringDef = new PropertyStringDefinitionImpl();
			stringDef.setIsInherited(false);
			stringDef.setId("string-id");
			stringDef.setLocalName("string-localName");
			stringDef.setLocalNamespace("string-localNamespace");
			stringDef.setQueryName("string-queryName");
			stringDef.setDisplayName("string-displayName");
			stringDef.setDescription("string-description");
			stringDef.setPropertyType(PropertyType.STRING);
			stringDef.setCardinality(Cardinality.SINGLE);
			stringDef.setUpdatability(Updatability.ONCREATE);
			stringDef.setIsRequired(true);
			stringDef.setIsQueryable(true);
			stringDef.setIsOrderable(true);
			
			stringDef.setMaxLength(new BigInteger("100"));
			
			def.addPropertyDefinition(stringDef);
		}
		
		
		//uri
		{
			PropertyUriDefinitionImpl uriDef = new PropertyUriDefinitionImpl();
			uriDef.setIsInherited(false);
			uriDef.setId("uri-id");
			uriDef.setLocalName("uri-localName");
			uriDef.setLocalNamespace("uri-localNamespace");
			uriDef.setQueryName("uri-queryName");
			uriDef.setDisplayName("uri-displayName");
			uriDef.setDescription("uri-description");
			uriDef.setPropertyType(PropertyType.URI);
			uriDef.setCardinality(Cardinality.SINGLE);
			uriDef.setUpdatability(Updatability.ONCREATE);
			uriDef.setIsRequired(true);
			uriDef.setIsQueryable(true);
			uriDef.setIsOrderable(true);
			
			def.addPropertyDefinition(uriDef);
		}
	}
}
