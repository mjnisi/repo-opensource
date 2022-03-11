package eu.trade.repo.search.codecs.impl;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;
import eu.trade.repo.search.codecs.CMISPropertyTypeCodecFactory;


/**
 * @author kardaal
 * 
 */
public abstract class CMISPropertyTypeCodecFactoryImpl implements CMISPropertyTypeCodecFactory {

	public CMISPropertyTypeCodec codecFor(PropertyType propertyType) {

		CMISPropertyTypeCodec codec = null;

		switch (propertyType) {
			case BOOLEAN:
				codec = createBooleanCodec();
				break;
			case DATETIME:
				codec = createDateTimeCodec();
				break;
			case ID:
				codec = createIdCodec();
				break;
			case INTEGER:
				codec = createIntegerCodec();
				break;
			case DECIMAL:
				codec = createDecimalCodec();
				break;
			case HTML:
				codec = createHtmlCodec();
				break;
			case STRING:
				codec = createStringCodec();
				break;
			case URI:
				codec = createUriCodec();
				break;
			default:
				throw new CmisRuntimeException("PropertyType: " + propertyType + " not implemented!");
		}

		return codec;
	}

	protected abstract CMISPropertyTypeCodec createBooleanCodec();
	protected abstract CMISPropertyTypeCodec createDateTimeCodec();
	protected abstract CMISPropertyTypeCodec createDecimalCodec();
	protected abstract CMISPropertyTypeCodec createHtmlCodec();
	protected abstract CMISPropertyTypeCodec createIdCodec();
	protected abstract CMISPropertyTypeCodec createIntegerCodec();
	protected abstract CMISPropertyTypeCodec createStringCodec();
	protected abstract CMISPropertyTypeCodec createUriCodec();

}