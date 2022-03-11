package eu.trade.repo.search.codecs.impl;

import java.math.BigDecimal;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;

public class CMISDecimalCodec implements CMISPropertyTypeCodec {

	/**
	 * Expects Double|BigDecimal|String
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal encode(Object propertyValue) {

		BigDecimal encodedDecimal = null;

		if (propertyValue instanceof BigDecimal) {
			encodedDecimal = (BigDecimal) propertyValue;
		} else if (propertyValue instanceof Double) {
			encodedDecimal = new BigDecimal((Double) propertyValue);
		} else if (propertyValue instanceof Integer) {
			encodedDecimal = new BigDecimal((Integer) propertyValue);
		} else if (propertyValue instanceof String) {
			try {
				encodedDecimal = new BigDecimal((String) propertyValue);
			} catch (NumberFormatException e) {
				throw new CmisInvalidArgumentException(propertyValue + " is not a valid Decimal value", e);
			}
		}

		return encodedDecimal;

	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal decode(Object propertyValue) {
		return (BigDecimal) propertyValue;
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal normalize(Object propertyValue) {
		return encode(propertyValue);
	}
}
