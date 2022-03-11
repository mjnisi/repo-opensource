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

public class QuantifiedComparisonFilter extends CMISQueryFilter {
	
	public  static final String EQUALS_ANY = "= ANY";
	
	private final String qualifier;
	private final String name;
	private final String value;
	
	public QuantifiedComparisonFilter(int t, String qualifier, String name, String value) {
		super(new CommonToken(t));
		
		this.qualifier = qualifier;
		this.name = name;
		this.value = value != null ? Utilities.convertCMIStoSQLEscapes(Utilities.removePropertyValueSingleQuotes(value)) : null;
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", value=" + value + "]";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOp() {
		return EQUALS_ANY;
	}

	@Override
	public String getValue() {
		return value; 
	}

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {
		
		//TODO: validate cmis propertyType cardinality == "multi" ?
		
		PropertyType propertyType = getPropertyType(getName(), objectTypeProperties);
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);
		
		setPropertyJoin(objectTypeProperties.get(getName()).first().getId(), property);
		
		Predicate equals = cb.equal(valueMapping.isStringSubtype() ? obtainAppropriatedStringValue(property, valueMapping) : property.<BigDecimal> get(NUMERICVALUE), convert(getValue(), propertyType, valueMapping));
		
		return !negateExpression()?equals:cb.not(equals);
	}
}
