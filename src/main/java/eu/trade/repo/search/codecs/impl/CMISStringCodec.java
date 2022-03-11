package eu.trade.repo.search.codecs.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;

public class CMISStringCodec implements CMISPropertyTypeCodec {
	@Autowired
	private CMISStringPropertyTypeNormalizer stringNormalizer;


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
		return stringNormalizer.normalizeString(encode(propertyValue));
	}

}
