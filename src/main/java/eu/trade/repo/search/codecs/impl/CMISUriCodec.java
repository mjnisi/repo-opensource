package eu.trade.repo.search.codecs.impl;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;

public class CMISUriCodec implements CMISPropertyTypeCodec {

	@SuppressWarnings(UNCHECKED)
	@Override
	public String encode(Object propertyValue) {
		return (String) propertyValue;
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public String decode(Object propertyValue) {
		return (String) propertyValue;
	}
	
	@SuppressWarnings(UNCHECKED)
	@Override
	public String normalize(Object propertyValue) {
		return encode(propertyValue);
	}
}
