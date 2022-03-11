package eu.trade.repo.query.ast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;

import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.query.CMISLexer;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Utilities;

public class QuantifiedInFilter extends CMISQueryFilter {
	
	public  static final String ANY_IN = "ANY_IN";
	
	private final String qualifier;
	private final String name;
	private final boolean not;
	private final List<String> values;
	
	public QuantifiedInFilter(int t, String qualifier, String name, String not, Object values) {
		super(new CommonToken(t));
		
		this.qualifier = qualifier;
		this.name = name;
		this.not = "not".equalsIgnoreCase(not);
		
		this.values = new ArrayList<String>();
		CommonTree tree = (CommonTree)values;
		
		if(tree.getChildCount() == 0 && tree.getToken() != null && tree.getToken().getType() == CMISLexer.CONST) {
			//If only 1 item in IN_EXPR
			this.values.add(Utilities.convertCMIStoSQLEscapes(Utilities.removePropertyValueSingleQuotes(tree.getToken().getText())));
		} else {
			for(int i=0;i<tree.getChildCount();i++) {
				this.values.add(Utilities.convertCMIStoSQLEscapes(Utilities.removePropertyValueSingleQuotes(tree.getChild(i).getText())));
			}
		}
		
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", not=" + not + ", values=" + values + "]";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOp() {
		return ANY_IN;
	}

	@Override
	public String getValue() {
		return values.toString(); 
	}

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {
		
		PropertyType propertyType = getPropertyType(getName(), objectTypeProperties);
		ObjectTypeProperty.PropertyTypeValueMapping valueMapping = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(propertyType);
		
		setPropertyJoin(objectTypeProperties.get(getName()).first().getId(), property);
		
		Expression<?> exp = null;
		Predicate in = null;
		
		if(valueMapping.isStringSubtype()) {
			exp = obtainAppropriatedStringValue(property, valueMapping);
			in = exp.in(this.<List<String>>convert(values, propertyType, valueMapping));
		} else {
			exp = property.<BigDecimal>get(ObjectTypeProperty.PropertyTypeValueMapping.NUMERIC_VALUE.getValueField());
			in = exp.in(this.<List<BigDecimal>>convert(values, propertyType));
		}
		
		if(not ^ negateExpression()) {in = cb.not(in);}
		
		return in;
	}
	
}
