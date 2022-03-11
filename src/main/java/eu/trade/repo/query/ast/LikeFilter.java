package eu.trade.repo.query.ast;

import java.util.Map;
import java.util.SortedSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Utilities;

public class LikeFilter extends CMISQueryFilter {

	public  static final String LIKE = "LIKE";

	private final String qualifier;
	private final String name;
	private final boolean not;
	private final String value;

	public LikeFilter(int t, String qualifier, String name, String not, String value) {
		super(new CommonToken(t));

		this.qualifier = qualifier;
		this.name = name;
		this.not = "not".equalsIgnoreCase(not);
		//TODO: unescape special LIKE characters (%, _) 
		this.value = value != null ? Utilities.removePropertyValueSingleQuotes(value) : null;

	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", not="
				+ not + ", value=" + value + "]";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOp() {
		return LIKE;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {

		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = validate(objectTypeProperties);

		setPropertyJoin(objectTypeProperties.get(getName()).first().getId(), property);

		Predicate like = cb.like(obtainAppropriatedStringValue(property, valueMapping), (String) convert( getValue(), getPropertyType(getName(), objectTypeProperties), valueMapping ), '\\');

		if(not ^ negateExpression()) {like = cb.not(like);}

		return like;
	}

	public ObjectTypeProperty.PropertyTypeValueMapping validate(Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties) {

		// validate name is textfield type
		PropertyType propertyType = getPropertyType(getName(), objectTypeProperties);
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);

		if( !valueMapping.isStringSubtype()) {
			throw new CmisInvalidArgumentException(String.format("propertyType %s is of invalid type (%s) for operator:LIKE", getName(), propertyType));
		}
		return valueMapping;
	}
}
