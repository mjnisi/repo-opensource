package eu.trade.repo.search.codecs;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;

public final class CMISPropertyTypeCodecUtil{

	private static CMISPropertyTypeCodecFactory codecFactory;

	private CMISPropertyTypeCodecUtil(){}
	
	
	public static CMISPropertyTypeCodec codecFor(PropertyType propertyType){
		return codecFactory.codecFor(propertyType);
	}
	
	
	public static void setCodecFactory(CMISPropertyTypeCodecFactory codecFactory){		
		CMISPropertyTypeCodecUtil.codecFactory = codecFactory;
	}
}

