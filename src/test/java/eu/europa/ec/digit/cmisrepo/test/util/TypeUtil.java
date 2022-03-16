package eu.europa.ec.digit.cmisrepo.test.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIdDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public class TypeUtil {

	// Modify these values to conform to your local repository requirements
	//
	// property id's and names here
//	private static final String property_stringPropertyId = "StringPropertyDefinition";
//	private static final String property_intPropertyId = "IntPropertyDefinition";
	private static final String property_boolPropertyId = "BoolPropertyDefinition";
	private static final String property_idPropertyId = "IdPropertyDefinition";

	//
	// type related
	// the id of the new type's parent
//	private static final String type_idOfParentType = "cmis:document";
	// Id of the new subclass we will be creating
//	private static final String type_idOfNewClass = "cmis:newDocSubclass1";
	// other information about type
//	private static final String type_description = "Test document type definition";
//	private static final String type_displayName = "TestTypeDefinition";
//	private static final String type_localName = "some test local name";
//	private static final String type_localNamespace = "some test local name space";

	// globals
	// private static Session session = null;

	public static void main(String[] args) {
		/*-
		 * Everything stolen from:
		 * https://chemistry.apache.org/java/examples/example-create-type.html
		 */
		System.out.println("An example of type creation with CMIS TypeMutability from OpenCMIS.");

		// Assume /cmis.properties is the name of the input file containing all
		// of the session parameters (test only)
		Map<String, String> parameters = loadConnectionProperties("/temp/cmis.properties");

		Session session = getSession(parameters);

		// Look at repository info - demonstrates a valid connection
		RepositoryInfo repositoryInfo = session.getRepositoryInfo();
		System.out
				.println("Connected to repository.  Supports CMIS version:" + repositoryInfo.getCmisVersionSupported());

		// Check here to verify the types we will be creating are permissible
		// for this repository.
		Boolean canCreateStringProperty = false;
		Boolean canCreateIdProperty = false;
		Boolean canCreateBoolProperty = false;
		Boolean canCreateIntProperty = false;
		for (PropertyType propCreatable : repositoryInfo.getCapabilities().getCreatablePropertyTypes().canCreate()) {

			if (propCreatable.equals(PropertyType.STRING)) {
				canCreateStringProperty = true;
			} else if (propCreatable.equals(PropertyType.INTEGER)) {
				canCreateIntProperty = true;
			} else if (propCreatable.equals(PropertyType.ID)) {
				canCreateIdProperty = true;
			} else if (propCreatable.equals(PropertyType.BOOLEAN)) {
				canCreateBoolProperty = true;
			}

			System.out.println("Repository can create property of : " + propCreatable);
		}

		assert canCreateStringProperty : "String is not one of the createable properties.";
		assert canCreateIdProperty : "Id is not one of the createable properties.";
		assert canCreateBoolProperty : "Boolean is not one of the createable properties.";
		assert canCreateIntProperty : "Integer is not one of the createable properties.";

		// Create new type with string property
		// and verify it exists in type collection
		TypeDefinition newType = createNewType(session);

		// clean up the type if permitted
		// TODO - check to see if delete is permitted first...
		session.deleteType(newType.getId());

		// TODO - verify delete here for completeness.
		System.out.println("Cleanup completed.");
	}

	/**
	 * Create the new type with with our 4 test property types.
	 * 
	 * @return
	 */
	public static TypeDefinition createNewType(Session session) {
		Map<String, PropertyDefinition<?>> cMapPropertyDefinitions = new LinkedHashMap<String, PropertyDefinition<?>>();
		StringPropertyDefinition spd = createStringPropertyDefinition();
		TestIntegerPropertyDefinition ipd = createIntPropertyDefinition();
		TestBooleanPropertyDefinition bpd = createBooleanPropertyDefinition();
		TestIdPropertyDefinition idpd = createIDPropertyDefinition();

		cMapPropertyDefinitions.put(spd.getId(), spd);
		cMapPropertyDefinitions.put(ipd.getId(), ipd);
		cMapPropertyDefinitions.put(bpd.getId(), bpd);
		cMapPropertyDefinitions.put(idpd.getId(), idpd);

		TestDocumentTypeDefinition typeToCreate = new TestDocumentTypeDefinition(
				"cmis:newDocSubclass1", 
				"Test document type definition",
				"TestTypeDefinition", 
				"some test local name", 
				"some test local name space", 
				"cmis:document", 
				true,
				true, 
				true, 
				ContentStreamAllowed.ALLOWED, 
				false, 
				cMapPropertyDefinitions
		);

		TypeDefinition createdType = session.createType(typeToCreate);

		TypeDefinition retrievedType = session.getTypeDefinition(createdType.getId());

		return retrievedType;
	}

	public static Session getSession(Map<String, String> parameters) {
		Session session = null;

		SessionFactory factory = SessionFactoryImpl.newInstance();
		if (parameters.containsKey(SessionParameter.REPOSITORY_ID)) {
			session = factory.createSession(parameters);
		} else {
			// Create session for the first repository.
			List<Repository> repositories = factory.getRepositories(parameters);
			session = repositories.get(0).createSession();
		}

		// reset op context to default
		session.setDefaultContext(session.createOperationContext());

		return session;
	}

	/*
	 * Load our connection properties from a local file
	 * 
	 * Note file should use the same format as the expert settings for Workbench
	 */
	public static final Map<String, String> loadConnectionProperties(String configResource) {
		Properties testConfig = new Properties();
		if (configResource == null) {
			throw new CmisRuntimeException("Filename with connection parameters was not supplied.");
		}

		FileInputStream inStream;
		try {
			inStream = new FileInputStream(configResource);
			testConfig.load(inStream);
			inStream.close();
		} catch (FileNotFoundException e1) {
			throw new CmisRuntimeException(
					"Test properties file '" + configResource + "' was not found at:" + configResource);
		} catch (IOException e) {
			throw new CmisRuntimeException("Exception loading test properties file " + configResource, e);
		}

		Map<String, String> map = new HashMap<String, String>();
		for (Entry<?, ?> entry : testConfig.entrySet()) {
			System.out.println("Found key: " + entry.getKey() + " Value:" + entry.getValue());
			map.put((String) entry.getKey(), ((String) entry.getValue()).trim());
		}
		return map;
	}

	/**
	 * Create a single string property definition with a choice list
	 */
	private static StringPropertyDefinition createStringPropertyDefinition() {
		TestStringChoice strChoice1 = new TestStringChoice("choice1", Arrays.asList("val1"), null);
		TestStringChoice strChoice2 = new TestStringChoice("choice2", Arrays.asList("val1"), null);
		List<Choice<String>> choiceList = new LinkedList<Choice<String>>();
		choiceList.add(strChoice1);
		choiceList.add(strChoice2);

		StringPropertyDefinition spd = new StringPropertyDefinition(
				"StringPropertyDefinitionIdAndQueryName", 
				Cardinality.SINGLE,
				"StringPropertyDefinitionDescription", 
				"StringPropertyDefinitionDisplayName", 
				"StringPropertyDefinitionLocalName", 
				"StringPropertyDefinitionLocalNameSpace", 
				Updatability.READWRITE, 
				false, 
				false, 
				Arrays.asList("test"),
				choiceList, 
				null);

		return spd;
	}

	private static TestIntegerPropertyDefinition createIntPropertyDefinition() {
//		Cardinality cardinality = Cardinality.MULTI;
		String description = "Int property definition";
		String displayName = "IntPropertyDefinition";
		String localName = "IntPropertyDefinition";
		String localNameSpace = "IntPropertyDefinition";
		Updatability updatability = Updatability.READWRITE;
		Boolean orderable = false;
		Boolean queryable = false;
		ArrayList<BigInteger> defaults = new ArrayList<BigInteger>();
		// defaults.add(new BigInteger("101"));
		BigInteger minVal = new BigInteger("100");
		BigInteger maxVal = new BigInteger("1000");

		TestIntegerPropertyDefinition ipd = new TestIntegerPropertyDefinition(
				"IntPropertyDefinition", 
				Cardinality.MULTI,
				description, 
				displayName, 
				localName, 
				localNameSpace, 
				updatability, 
				orderable, 
				queryable, 
				defaults,
				minVal, 
				maxVal, 
				null);
		return ipd;
	}

	private static TestBooleanPropertyDefinition createBooleanPropertyDefinition() {

		Cardinality cardinality = Cardinality.SINGLE;
		String description = "Boolean property definition";
		String displayName = "BooleanPropertyDefinition";
		String localName = "BooleanPropertyDefinition";
		String localNameSpace = "BooleanPropertyDefinition";
		Updatability updatability = Updatability.ONCREATE;
		Boolean orderable = false;
		Boolean queryable = false;
		ArrayList<Boolean> defaults = new ArrayList<Boolean>();
		defaults.add(false);

		TestBooleanPropertyDefinition spd = new TestBooleanPropertyDefinition(property_boolPropertyId, cardinality,
				description, displayName, localName, localNameSpace, updatability, orderable, queryable, defaults);
		return spd;
	}

	private static TestIdPropertyDefinition createIDPropertyDefinition() {
		Cardinality cardinality = Cardinality.SINGLE;
		String description = "ID property definition";
		String displayName = "IDPropertyDefinition";
		String localName = "IDPropertyDefinition";
		String localNameSpace = "IDPropertyDefinition";
		Updatability updatability = Updatability.READWRITE;
		Boolean orderable = false;
		Boolean queryable = false;

		TestIdPropertyDefinition idpd = new TestIdPropertyDefinition(property_idPropertyId, cardinality, description,
				displayName, localName, localNameSpace, updatability, orderable, queryable, null);
		return idpd;
	}

	/**
	 * **************************************************************************
	 * Inner classes follow
	 * **************************************************************************
	 * 
	 * All of the abstract base classes (for properties and types) that are used in
	 * this example are defined here along with their subclasses for each type that
	 * we support in this example.
	 * 
	 * These classes can be further extended and reused for additional type
	 * mutability operations.
	 * 
	 * These were made inner classes so the entire example would be contained in a
	 * single Java file. (no design reason)
	 * 
	 */

	private static class StringPropertyDefinition extends TestPropertyDefinitionImpl<String>
			implements PropertyStringDefinition {

		BigInteger maxLength = null;

		public StringPropertyDefinition(String idAndQueryName, Cardinality cardinality, String description,
				String displayName, String localName, String localNameSpace, Updatability updatability,
				Boolean orderable, Boolean queryable, List<String> defaultValue, List<Choice<String>> choiceList,
				BigInteger maxLength) {
			super(idAndQueryName, cardinality, description, displayName, localName, localNameSpace, updatability,
					orderable, queryable, defaultValue, choiceList);

			this.maxLength = maxLength;
		}

		@Override
		public PropertyType getPropertyType() {
			return PropertyType.STRING;
		}

		@Override
		public BigInteger getMaxLength() {
			return maxLength;
		}

	}

	private static class TestIntegerPropertyDefinition extends TestPropertyDefinitionImpl<BigInteger>
			implements PropertyIntegerDefinition {

		private BigInteger minVal = null;
		private BigInteger maxVal = null;

		public TestIntegerPropertyDefinition(String idAndQueryName, Cardinality cardinality, String description,
				String displayName, String localName, String localNameSpace, Updatability updatability,
				Boolean orderable, Boolean queryable, List<BigInteger> defaultValue, BigInteger minVal,
				BigInteger maxVal, List<Choice<BigInteger>> choiceList) {
			super(idAndQueryName, cardinality, description, displayName, localName, localNameSpace, updatability,
					orderable, queryable, defaultValue, choiceList);

			this.minVal = minVal;
			this.maxVal = maxVal;
		}

		@Override
		public PropertyType getPropertyType() {
			return PropertyType.INTEGER;
		}

		@Override
		public BigInteger getMaxValue() {
			return this.maxVal;
		}

		@Override
		public BigInteger getMinValue() {
			return this.minVal;
		}
	}

	private static class TestBooleanPropertyDefinition extends TestPropertyDefinitionImpl<Boolean>
			implements PropertyBooleanDefinition {

		public TestBooleanPropertyDefinition(String idAndQueryName, Cardinality cardinality, String description,
				String displayName, String localName, String localNameSpace, Updatability updatability,
				Boolean orderable, Boolean queryable, List<Boolean> defaultValue) {

			super(idAndQueryName, cardinality, description, displayName, localName, localNameSpace, updatability,
					orderable, queryable, defaultValue, null);
		}

		@Override
		public PropertyType getPropertyType() {
			return PropertyType.BOOLEAN;
		}
	}

	private static class TestIdPropertyDefinition extends TestPropertyDefinitionImpl<String>
			implements PropertyIdDefinition {

		public TestIdPropertyDefinition(String idAndQueryName, Cardinality cardinality, String description,
				String displayName, String localName, String localNameSpace, Updatability updatability,
				Boolean orderable, Boolean queryable, List<String> defaultValue) {
			super(idAndQueryName, cardinality, description, displayName, localName, localNameSpace, updatability,
					orderable, queryable, defaultValue, null);
		}

		@Override
		public PropertyType getPropertyType() {
			return PropertyType.ID;
		}

	}

	/**
	 * Base class for all property definition types
	 * 
	 * See TestStringPropertyDefinition for example of how to subclass this.
	 *
	 * @param <T>
	 */
	abstract private static class TestPropertyDefinitionImpl<T> implements PropertyDefinition<T> {

		private String idAndQueryName = null;
		private Cardinality cardinality = null;
		private String description = null;
		private String displayName = null;
		private String localName = null;
		private String localNameSpace = null;
		private Updatability updatability = null;
		private Boolean orderable = null;
		private Boolean queryable = null;

		private List<T> defaultValue = null;
		private List<Choice<T>> choiceList = null;

		public TestPropertyDefinitionImpl(String idAndQueryName, Cardinality cardinality, String description,
				String displayName, String localName, String localNameSpace, Updatability updatability,
				Boolean orderable, Boolean queryable, List<T> defaultValue, List<Choice<T>> choiceList) {
			super();
			this.idAndQueryName = idAndQueryName;
			this.cardinality = cardinality;
			this.description = description;
			this.displayName = displayName;
			this.localName = localName;
			this.localNameSpace = localNameSpace;
			this.updatability = updatability;
			this.orderable = orderable;
			this.queryable = queryable;
			this.defaultValue = defaultValue;
			this.choiceList = choiceList;
		}

		@Override
		public String getId() {
			return idAndQueryName;
		}

		@Override
		public Cardinality getCardinality() {
			return cardinality;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		@Override
		public String getLocalNamespace() {
			return localNameSpace;
		}

		@Override
		abstract public PropertyType getPropertyType();

		@Override
		public String getQueryName() {
			return idAndQueryName;
		}

		@Override
		public Updatability getUpdatability() {
			return updatability;
		}

		@Override
		public Boolean isOrderable() {
			return orderable;
		}

		@Override
		public Boolean isQueryable() {
			return queryable;
		}

		@Override
		public List<Choice<T>> getChoices() {
			return this.choiceList;
		}

		@Override
		public List<T> getDefaultValue() {
			return this.defaultValue;
		}

		/**
		 * TODO For these remaining attributes you will want to set them accordingly.
		 * They are all set to static values only because this is sample code.
		 */
		@Override
		public Boolean isInherited() {
			return false;
		}

		@Override
		public Boolean isOpenChoice() {
			return false;
		}

		@Override
		public Boolean isRequired() {
			return false;
		}

		@Override
		public List<CmisExtensionElement> getExtensions() {
			return null;
		}

		@Override
		public void setExtensions(List<CmisExtensionElement> arg0) {
		}

	}

	/**
	 * Base class for all typeDefinitions. See TestDocumentTypeDefinition for an
	 * example of how to subclass this for document.
	 *
	 */
	private static abstract class TestTypeDefinition implements TypeDefinition {

		private String description = null;
		private String displayName = null;
		private String idAndQueryName = null;
		private String localName = null;
		private String localNamespace = null;
		private String parentTypeId = null;
		private Boolean isCreatable = null;
		private Boolean includedInSupertypeQuery = null;
		private Boolean queryable = null;
		private Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

		public TestTypeDefinition(String idAndQueryName, String description, String displayName, String localName,
				String localNamespace, String parentTypeId, Boolean isCreatable, Boolean includedInSupertypeQuery,
				Boolean queryable, Map<String, PropertyDefinition<?>> propertyDefinitions) {

			this.description = description;
			this.displayName = displayName;
			this.idAndQueryName = idAndQueryName;
			this.localName = localName;
			this.localNamespace = localNamespace;
			this.parentTypeId = parentTypeId;
			this.isCreatable = isCreatable;
			this.includedInSupertypeQuery = includedInSupertypeQuery;
			this.queryable = queryable;

			if (propertyDefinitions != null) {
				this.propertyDefinitions = propertyDefinitions;
			}
		}

		@Override
		abstract public BaseTypeId getBaseTypeId();

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public String getId() {
			return idAndQueryName;
		}

		@Override
		public String getLocalName() {
			return localName;
		}

		@Override
		public String getLocalNamespace() {
			return localNamespace;
		}

		@Override
		public String getParentTypeId() {
			return parentTypeId;
		}

		@Override
		public Map<String, PropertyDefinition<?>> getPropertyDefinitions() {
			return propertyDefinitions;
		}

		@Override
		public String getQueryName() {
			return idAndQueryName;
		}

		@Override
		public Boolean isCreatable() {
			return isCreatable;
		}

		@Override
		public Boolean isIncludedInSupertypeQuery() {
			return includedInSupertypeQuery;
		}

		@Override
		public Boolean isQueryable() {
			return queryable;
		}

		/**
		 * TODO For these remaining attributes you will want to set them accordingly.
		 * They are all set to static values only because this is sample code.
		 */

		@Override
		public TypeMutability getTypeMutability() {
			return new TestTypeMutability();
		}

		@Override
		public Boolean isControllableAcl() {
			return true;
		}

		@Override
		public Boolean isControllablePolicy() {
			return false;
		}

		@Override
		public Boolean isFileable() {
			return true;
		}

		@Override
		public Boolean isFulltextIndexed() {
			return false;
		}

		@Override
		public List<CmisExtensionElement> getExtensions() {
			return null;
		}

		@Override
		public void setExtensions(List<CmisExtensionElement> extension) {
		}

	}

	private static class TestDocumentTypeDefinition extends TestTypeDefinition implements DocumentTypeDefinition {

		private ContentStreamAllowed contentStreamAllowed = null;
		private Boolean versionable = null;

		public TestDocumentTypeDefinition(String idAndQueryName, String description, String displayName,
				String localName, String localNamespace, String parentTypeId, Boolean isCreatable,
				Boolean includedInSupertypeQuery, Boolean queryable, ContentStreamAllowed contentStreamAllowed,
				Boolean versionable, Map<String, PropertyDefinition<?>> propertyDefinitions) {

			super(idAndQueryName, description, displayName, localName, localNamespace, parentTypeId, isCreatable,
					includedInSupertypeQuery, queryable, propertyDefinitions);

			this.contentStreamAllowed = contentStreamAllowed;
			this.versionable = versionable;
		}

		@Override
		public BaseTypeId getBaseTypeId() {
			return BaseTypeId.CMIS_DOCUMENT;
		}

		@Override
		public ContentStreamAllowed getContentStreamAllowed() {
			return contentStreamAllowed;
		}

		@Override
		public Boolean isVersionable() {
			return versionable;
		}

	}

	public static class TestTypeMutability implements TypeMutability {

		/**
		 * TODO: Change these values based on your repository and requirements
		 */

		@Override
		public List<CmisExtensionElement> getExtensions() {
			return null;
		}

		@Override
		public void setExtensions(List<CmisExtensionElement> arg0) {
		}

		@Override
		public Boolean canCreate() {
			return true;
		}

		@Override
		public Boolean canDelete() {
			return true;
		}

		@Override
		public Boolean canUpdate() {
			return true;
		}
	}

	public static class TestStringChoice implements Choice<String> {

		private final String displayName;
		private final List<String> value;
		private final List<Choice<String>> choices;

		public TestStringChoice(String displayName, List<String> value, List<Choice<String>> choices) {
			this.displayName = displayName;
			this.value = value;
			this.choices = choices;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}

		@Override
		public List<String> getValue() {
			return value;
		}

		@Override
		public List<Choice<String>> getChoice() {
			return choices;
		}

	}
}
