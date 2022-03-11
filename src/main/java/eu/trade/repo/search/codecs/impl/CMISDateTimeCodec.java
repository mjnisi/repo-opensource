package eu.trade.repo.search.codecs.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.joda.time.format.ISODateTimeFormat;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.Utilities;


public class CMISDateTimeCodec implements CMISPropertyTypeCodec {

	private static final DateTimeFormatter formatter;
	private static final String TIMESTAMP_PREFIX = "TIMESTAMP";
	
	static{
		List<DateTimeParser> parsers = new ArrayList<DateTimeParser>();
		for (String dateTimeFormat : Constants.CMIS_QUERY_DATETIME_FORMATS) {
			parsers.add(DateTimeFormat.forPattern(dateTimeFormat).getParser());
		}
		formatter = new DateTimeFormatterBuilder().append(null, parsers.toArray(new DateTimeParser[parsers.size()])).toFormatter();
	}

	public CMISDateTimeCodec(){
		super();
	}

	@SuppressWarnings(UNCHECKED)
	@Override
	public String encode(Object propertyValue) {

		DateTime jodaDateTime = null;

		if (propertyValue instanceof java.util.Date) {
			jodaDateTime = new DateTime(propertyValue);
		} else if (propertyValue instanceof Calendar) {
			jodaDateTime = new DateTime(((Calendar) propertyValue).getTime());
		} else {
			try {
				String propertyValueStr = Utilities.removePropertyValueSingleQuotes((String) propertyValue);
				if(propertyValueStr.toUpperCase().startsWith(TIMESTAMP_PREFIX)) {
					propertyValueStr = Utilities.removePropertyValueSingleQuotes(propertyValueStr.substring(TIMESTAMP_PREFIX.length() + 1).trim());
				}
				jodaDateTime = formatter.parseDateTime(propertyValueStr);
			} catch (RuntimeException e) {
				throw new CmisInvalidArgumentException("An error was encountered when attempting to parse datetime: " + propertyValue, e);
			}
		}

		return jodaDateTime.toDateTimeISO().withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTime());
	}
	
	@SuppressWarnings(UNCHECKED)
	@Override
	public GregorianCalendar decode(Object propertyValue) {

		try {
			return ISODateTimeFormat.dateTimeParser().parseDateTime((String) propertyValue).toGregorianCalendar();
		} catch (RuntimeException e) {
			throw new CmisInvalidArgumentException("An error was encountered when attempting to parse datetime: " + propertyValue, e);
		}

	}
	
	@SuppressWarnings(UNCHECKED)
	@Override
	public String normalize(Object propertyValue) {
		return encode(propertyValue);
	}
}