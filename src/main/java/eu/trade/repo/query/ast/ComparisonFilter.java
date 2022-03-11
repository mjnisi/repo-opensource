package eu.trade.repo.query.ast;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Utilities;

public class ComparisonFilter extends CMISQueryFilter {

	public static final String EQUAL = "=";

	public static final String NOT_EQUAL = "<>";

	public static final String GT = ">";
	public static final String GTE = ">=";

	public static final String LT = "<";
	public static final String LTE = "<=";
	
	private final String qualifier;
	private final String name;
	private final String op;
	private final String value;
	
	public ComparisonFilter(int t, String qualifier, String name, String op, String value) {
		super(new CommonToken(t));
		
		this.qualifier = qualifier;
		this.name = name;
		this.op = op;
		this.value = value != null ? Utilities.convertCMIStoSQLEscapes(Utilities.removePropertyValueSingleQuotes(value)) : null;
		
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", op="
				+ op + ", value=" + value + "]";
	}

    public String getQualifier() {
        return qualifier;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getOp() {
        return op;
    }

    public String getValue() {
        return value;
    }

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {
		
		Predicate propertyFilter = null;
		
		PropertyType propertyType = getPropertyType(getName(), objectTypeProperties);
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);
		
		
		setPropertyJoin(objectTypeProperties.get(getName()).first().getId(), property);
		
		//TODO: move all this to subclasses, and
		//TODO: modify grammar to generate appropriate filter types for comparison operators
		if (EQUAL.equals(op)) {
			propertyFilter = handleEqual(cb, property, propertyType, valueMapping);
		} else if (NOT_EQUAL.equals(op)) {
			propertyFilter = handleNotEqual(cb, property, propertyType, valueMapping);
		} else if (GT.equals(op)) {
			propertyFilter = handleGreaterThan(cb, property, propertyType,valueMapping);
		} else if (GTE.equals(op)) {
			propertyFilter  = handleGreaterThanOrEquals(cb, property, propertyType, valueMapping);
		} else if (LT.equals(op)) {
			propertyFilter  = handleLessThan(cb, property, propertyType, valueMapping);
		} else if (LTE.equals(op)) {
			propertyFilter  = handleLessThanOrEquals(cb, property, propertyType, valueMapping);
		}
		
		return !negateExpression()?propertyFilter:cb.not(propertyFilter);
	}
	
	private Predicate handleEqual(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		return cb.equal(valueMapping.isStringSubtype() ? obtainAppropriatedStringValue(property, valueMapping) : property.<BigDecimal> get(NUMERICVALUE), convert(getValue(), propertyType, valueMapping));
	}
	
	private Predicate handleNotEqual(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		return cb.notEqual(valueMapping.isStringSubtype() ? obtainAppropriatedStringValue(property, valueMapping) : property.<BigDecimal> get(NUMERICVALUE), convert(getValue(), propertyType, valueMapping));
	}
	
	private Predicate handleGreaterThan(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		
		Predicate greaterThanFilter = null;
		
		if (valueMapping.isStringSubtype()) {
			greaterThanFilter = cb.greaterThan(obtainAppropriatedStringValue(property, valueMapping), (String) convert(getValue(), propertyType, valueMapping));
		} else {
			greaterThanFilter = cb.greaterThan(property.<BigDecimal> get(NUMERICVALUE), (BigDecimal) convert(getValue(), propertyType));
		}
		
		return greaterThanFilter;
	}
	
	private Predicate handleGreaterThanOrEquals(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		
		Predicate greaterThanOrEqualsFilter = null;
		
		if (valueMapping.isStringSubtype()) {
			greaterThanOrEqualsFilter = cb.greaterThanOrEqualTo(obtainAppropriatedStringValue(property, valueMapping), (String) convert(getValue(), propertyType, valueMapping));
		} else {
			greaterThanOrEqualsFilter = cb.greaterThanOrEqualTo(property.<BigDecimal> get(NUMERICVALUE), (BigDecimal) convert(getValue(), propertyType));
		}
		
		return greaterThanOrEqualsFilter;
	}
	
	private Predicate handleLessThan(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		
		Predicate lessThanFilter = null;
		
		if (valueMapping.isStringSubtype()) {
			lessThanFilter = cb.lessThan(obtainAppropriatedStringValue(property, valueMapping), (String) convert(getValue(), propertyType, valueMapping));
		} else {
			lessThanFilter = cb.lessThan(property.<BigDecimal> get(NUMERICVALUE), (BigDecimal) convert(getValue(), propertyType));
		}
		
		return lessThanFilter;
	}
	
	private Predicate handleLessThanOrEquals(CriteriaBuilder cb, Path<?> property, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		
		Predicate lessThanOrEqualsFilter = null;
		
		if (valueMapping.isStringSubtype()) {
			lessThanOrEqualsFilter = cb.lessThanOrEqualTo(obtainAppropriatedStringValue(property, valueMapping), (String) convert(getValue(), propertyType, valueMapping));
		} else {
			lessThanOrEqualsFilter = cb.lessThanOrEqualTo(property.<BigDecimal> get(NUMERICVALUE), (BigDecimal) convert(getValue(), propertyType));
		}
		
		return lessThanOrEqualsFilter;
	}
	
	
}
