package eu.trade.repo.query.ast;

import java.util.ArrayList;
import java.util.List;
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
import eu.trade.repo.search.codecs.CMISPropertyTypeCodecUtil;
import eu.trade.repo.security.Security;

public abstract class CMISQueryFilter extends CMISCommonTree {

	public static final String VALUE = ObjectTypeProperty.PropertyTypeValueMapping.VALUE.getValueField();
	public static final String NORMALIZEDVALUE = ObjectTypeProperty.PropertyTypeValueMapping.NORMALIZED_VALUE.getValueField();
	public static final String NUMERICVALUE = ObjectTypeProperty.PropertyTypeValueMapping.NUMERIC_VALUE.getValueField();
	protected final String OBJECT = "object";

	public CMISQueryFilter(CommonToken commonToken) {
		super(commonToken);
	}

	public abstract String getName();
	public abstract String getOp();
	public abstract String getText();
	public abstract String getValue();

	public abstract Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep);

	protected <T> T convert(String propertyValue, PropertyType propertyType) {
		return CMISPropertyTypeCodecUtil.codecFor(propertyType).encode(propertyValue);
	}

	protected <T> T convert(String propertyValue, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		return normalizeApplies(valueMapping)?
				CMISPropertyTypeCodecUtil.codecFor(propertyType).<T, String> normalize(propertyValue)
				:CMISPropertyTypeCodecUtil.codecFor(propertyType).<T, String> encode(propertyValue);
	}
	
	protected <T> List<T> convert(List<String> values, PropertyType propertyType) {
		List<T> convertedValues = new ArrayList<T>();
		for(String value : values) {
			convertedValues.add(this.<T>convert(value, propertyType));
		}
		return convertedValues;
	}
	
	protected <T> List<T> convert(List<String> values, PropertyType propertyType, ObjectTypeProperty.PropertyTypeValueMapping valueMapping) {
		List<T> convertedValues = new ArrayList<T>();
		for(String value : values) {
			convertedValues.add(this.<T>convert(value, propertyType, valueMapping));
		}
		return convertedValues;
	}


	protected PropertyType getPropertyType(String propertyName, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties) {
		return objectTypeProperties.get(propertyName).first().getPropertyType();
	}

	protected Path<String> obtainAppropriatedStringValue(Path<?> property, ObjectTypeProperty.PropertyTypeValueMapping valueMapping){
		return normalizeApplies(valueMapping)? property.<String> get(NORMALIZEDVALUE) : property.<String> get(VALUE);
	}

	protected boolean normalizeApplies(ObjectTypeProperty.PropertyTypeValueMapping valueMapping){
		return ( isNormalizeQuery() && valueMapping == ObjectTypeProperty.PropertyTypeValueMapping.NORMALIZED_VALUE );
	}
}
