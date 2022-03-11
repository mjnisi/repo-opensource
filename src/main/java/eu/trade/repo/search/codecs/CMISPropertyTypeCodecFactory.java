package eu.trade.repo.search.codecs;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;


public interface CMISPropertyTypeCodecFactory {

	CMISPropertyTypeCodec codecFor(PropertyType propertyType);
	
}