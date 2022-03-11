package eu.trade.repo.service.cmis.data.out;

import static eu.trade.repo.service.cmis.data.util.TypeUtil.BIGDECIMAL_PARSER;
import static eu.trade.repo.service.cmis.data.util.TypeUtil.BIGINTEGER_PARSER;
import static eu.trade.repo.service.cmis.data.util.TypeUtil.BOOLEAN_PARSER;
import static eu.trade.repo.service.cmis.data.util.TypeUtil.DATETIME_PARSER;
import static eu.trade.repo.service.cmis.data.util.TypeUtil.STRING_PARSER;
import static eu.trade.repo.service.cmis.data.util.TypeUtil.split;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.service.cmis.data.util.TypeParser;

/**
 * Helper class to build the property definitions map of TypeDefinition#getPropertyDefinitions().
 * 
 * @author porrjai
 */
public final class PropertyDefinitionsBuilder {


	private PropertyDefinitionsBuilder() {
	}


	public static Map<String, PropertyDefinition<?>> buildPropertyDefinitions(ObjectType objectType) {
		Map<String, PropertyDefinition<?>> propertyDefinitionsMap = new LinkedHashMap<>();
		Map<String, SortedSet<ObjectTypeProperty>> allPropertiesByCmisId = objectType.getObjectTypePropertiesIncludeParents(true, true);
		for (Map.Entry<String, SortedSet<ObjectTypeProperty>> entry : allPropertiesByCmisId.entrySet()) {
			ObjectTypeProperty objectTypeProperty = entry.getValue().first();
			PropertyDefinition<?> propertyDefinition = createPropDef(objectTypeProperty);
			propertyDefinitionsMap.put(entry.getKey(), propertyDefinition);
		}
		return propertyDefinitionsMap;
	}

	private static PropertyDefinition<?> createPropDef(ObjectTypeProperty objectTypeProperty) {
		AbstractPropertyDefinition<?> result = initPropertyDefinition(objectTypeProperty);
		result.setId(objectTypeProperty.getCmisId());
		result.setLocalName(objectTypeProperty.getLocalName());
		result.setLocalNamespace(objectTypeProperty.getLocalNamespace());
		result.setDisplayName(objectTypeProperty.getDisplayName());
		result.setQueryName(objectTypeProperty.getQueryName());
		result.setDescription(objectTypeProperty.getDescription());
		result.setPropertyType(objectTypeProperty.getPropertyType());
		result.setCardinality(objectTypeProperty.getCardinality());
		result.setUpdatability(objectTypeProperty.getUpdatability());
		result.setIsInherited(objectTypeProperty.isInherited());
		result.setIsRequired(objectTypeProperty.getRequired());
		result.setIsQueryable(objectTypeProperty.getQueryable());
		result.setIsOrderable(objectTypeProperty.getOrderable());
		result.setIsOpenChoice(objectTypeProperty.getOpenChoice());

		return result;
	}

	private static AbstractPropertyDefinition<?> initPropertyDefinition(ObjectTypeProperty objectTypeProperty) {
		switch (objectTypeProperty.getPropertyType()) {
			case BOOLEAN:
				return getBasicPropertyDefinition(new PropertyBooleanDefinitionImpl(), objectTypeProperty, BOOLEAN_PARSER);

			case DATETIME:
				PropertyDateTimeDefinitionImpl propertyDateTimeDefinitionImpl = getBasicPropertyDefinition(new PropertyDateTimeDefinitionImpl(), objectTypeProperty, DATETIME_PARSER);
				propertyDateTimeDefinitionImpl.setDateTimeResolution(objectTypeProperty.getResolution());
				return propertyDateTimeDefinitionImpl;

			case DECIMAL:
				PropertyDecimalDefinitionImpl propertyDecimalDefinitionImpl = getBasicPropertyDefinition(new PropertyDecimalDefinitionImpl(), objectTypeProperty, BIGDECIMAL_PARSER);
				if(objectTypeProperty.getMinValue() != null) {
					propertyDecimalDefinitionImpl.setMinValue(new BigDecimal(objectTypeProperty.getMinValue()));
				}
				if(objectTypeProperty.getMaxValue() != null) {
					propertyDecimalDefinitionImpl.setMaxValue(new BigDecimal(objectTypeProperty.getMaxValue()));
				}
				propertyDecimalDefinitionImpl.setPrecision(objectTypeProperty.getPrecision());
				return propertyDecimalDefinitionImpl;

			case HTML:
				return getBasicPropertyDefinition(new PropertyHtmlDefinitionImpl(), objectTypeProperty, STRING_PARSER);

			case ID:
				return getBasicPropertyDefinition(new PropertyIdDefinitionImpl(), objectTypeProperty, STRING_PARSER);

			case INTEGER:
				return getBasicPropertyDefinition(new PropertyIntegerDefinitionImpl(), objectTypeProperty, BIGINTEGER_PARSER);

			case STRING:
				PropertyStringDefinitionImpl propertyStringDefinitionImpl = getBasicPropertyDefinition(new PropertyStringDefinitionImpl(), objectTypeProperty, STRING_PARSER);
				Integer maxLength = objectTypeProperty.getMaxLength();
				if (maxLength != null) {
					propertyStringDefinitionImpl.setMaxLength(BigInteger.valueOf(maxLength));
				}
				return propertyStringDefinitionImpl;

			case URI:
				return getBasicPropertyDefinition(new PropertyUriDefinitionImpl(), objectTypeProperty, STRING_PARSER);
		}

		throw new IllegalArgumentException("Unknown datatype! Spec change?");
	}

	private static <T, E extends AbstractPropertyDefinition<T>> E getBasicPropertyDefinition(E propertyDefinition, ObjectTypeProperty objectTypeProperty, TypeParser<T> parser) {
		setValues(propertyDefinition, objectTypeProperty, parser);
		return propertyDefinition;
	}

	private static <T> void setValues(AbstractPropertyDefinition<T> result, ObjectTypeProperty objectTypeProperty, TypeParser<T> parser) {
		// TODO Review the Default and Choices definition. And remove the split using a real collection. Expected at Beta 2...
		// Default value, if not null, should be a single value. Now is split to cover multivalued default values.
		result.setDefaultValue(split(objectTypeProperty.getDefaultValue(), parser));
		// Choice list currently is a flat list.
		ChoiceImpl<T> choice = new ChoiceImpl<>();
		choice.setDisplayName(objectTypeProperty.getDisplayName());
		choice.setValue(split(objectTypeProperty.getChoices(), parser));
		List<Choice<T>> choices = new ArrayList<>();
		choices.add(choice);
		result.setChoices(choices);
	}

}
