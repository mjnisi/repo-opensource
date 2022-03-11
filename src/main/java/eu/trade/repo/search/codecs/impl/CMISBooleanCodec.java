package eu.trade.repo.search.codecs.impl;

import java.math.BigDecimal;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;

public class CMISBooleanCodec implements CMISPropertyTypeCodec {

	/**
	 * Expects Boolean or String("true", "false") as input
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal encode(Object propertyValue) {
		Boolean value = Boolean.FALSE;
		if ( propertyValue instanceof Boolean ) {
			value = (Boolean) propertyValue;
		} else if (propertyValue instanceof String) {
			value = Boolean.parseBoolean((String) propertyValue);
		}
		return value ? BigDecimal.ONE : BigDecimal.ZERO;
	}

	/**
	 * Expects BigDecimal(1|0) as input
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public Boolean decode(Object propertyvalue) {
		BigDecimal bigDecimal = (BigDecimal) propertyvalue;
		// In order to prevent scale problems reading from database use always BigDecimal.compareTo(). Remember BigDecimal("1.0").equals(BigDecimal("1.000")) is false.
		return BigDecimal.ONE.compareTo(bigDecimal) == 0;
	}


	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal normalize(Object propertyValue) {
		return encode(propertyValue);
	}

}