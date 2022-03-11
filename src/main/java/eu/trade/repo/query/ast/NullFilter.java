package eu.trade.repo.query.ast;

import java.util.Map;
import java.util.SortedSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;

public class NullFilter extends CMISQueryFilter {
	
	private final String qualifier;
	private final String name;
	private final boolean not;
	
	public NullFilter(int t, String qualifier, String name, String not) {
		super(new CommonToken(t));
		
		this.qualifier = qualifier;
		this.name = name;
		this.not = "not".equalsIgnoreCase(not);
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[name=" + name + ", qualifier=" + qualifier + ", not="
				+ not + "]";
	}
	
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getOp() {
		return "ISNULL";
	}
	
	@Override
	public String getValue() {
		return null;
	}
	
	//TODO: check acls?
	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {

		setContainsNullFIlter(true);
		
		Subquery<Integer> notNull = c.subquery(Integer.class);
		Root<Property> notNullQueryRoot = notNull.from(Property.class);
		notNull.select(notNullQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));
		notNull.where(cb.equal(notNullQueryRoot.<ObjectTypeProperty>get("objectTypeProperty").<Integer>get("id"), objectTypeProperties.get(getName()).first().getId()));
		
		Predicate predicateNotNull = property.<CMISObject>get(OBJECT).<Integer>get("id").in(notNull);
		
		if(not ^ negateExpression()) {
			return predicateNotNull;
		} else {
			return cb.not(predicateNotNull);
		}
	}
}
