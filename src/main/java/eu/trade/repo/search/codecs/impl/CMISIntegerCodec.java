package eu.trade.repo.search.codecs.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;

public class CMISIntegerCodec implements CMISPropertyTypeCodec {

	/**
	 * Expects Integer|BigInteger|String
	 */
	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal encode(Object propertyValue) {

		BigDecimal encodedInteger = null;

		if(propertyValue instanceof BigInteger) {
			encodedInteger = new BigDecimal((BigInteger) propertyValue);
		} else if(propertyValue instanceof Integer) {
			encodedInteger = new BigDecimal((Integer) propertyValue);
		} else if (propertyValue instanceof String) {
			try {
				//First, verify that the String really is an Integer (not e.g. a Double)
				//While the field (BigDecimal) can store any number type, it has no meaning
				//semantically for a CMIS_INTEGER field to provide e.g. a float, double etc
				BigInteger bigInt = new BigInteger((String) propertyValue);
				//then, convert to a BigDecimal
				encodedInteger = new BigDecimal(bigInt);
			} catch (NumberFormatException e) {
				throw new CmisInvalidArgumentException(propertyValue + " is not a valid Integer value", e);
			}
		}

		return encodedInteger;
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public BigInteger decode(Object propertyValue) {
		return BigInteger.valueOf( ((BigDecimal) propertyValue).longValue() );
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public BigDecimal normalize(Object propertyValue) {
		return encode(propertyValue);
	}
}