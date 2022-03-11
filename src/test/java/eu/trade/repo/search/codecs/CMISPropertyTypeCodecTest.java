package eu.trade.repo.search.codecs;

import static org.apache.chemistry.opencmis.commons.enums.PropertyType.BOOLEAN;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.DATETIME;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.DECIMAL;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.HTML;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.ID;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.INTEGER;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.STRING;
import static org.apache.chemistry.opencmis.commons.enums.PropertyType.URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.search.codecs.CMISPropertyTypeCodec;
import eu.trade.repo.search.codecs.CMISPropertyTypeCodecUtil;
import eu.trade.repo.search.codecs.impl.CMISBooleanCodec;
import eu.trade.repo.search.codecs.impl.CMISDateTimeCodec;

public class CMISPropertyTypeCodecTest extends BaseTestClass {
		
	@Test
	public void testCMISBooleanCodec() {
		CMISPropertyTypeCodec codec = CMISPropertyTypeCodecUtil.codecFor(BOOLEAN);

		assertTrue(CMISBooleanCodec.class.isAssignableFrom(codec.getClass()));

		assertEquals(BigDecimal.ONE, codec.<BigDecimal, String>encode("true"));
		assertEquals(BigDecimal.ZERO, codec.<BigDecimal, String>encode("false"));
		
		assertEquals(BigDecimal.ONE, codec.<BigDecimal, Boolean>encode(true));
		assertEquals(BigDecimal.ZERO, codec.<BigDecimal, Boolean>encode(false));
		
		assertEquals(BigDecimal.ZERO, codec.<BigDecimal, String>encode("random_string"));
		
		assertTrue(codec.<Boolean, BigDecimal>decode(BigDecimal.ONE));
		assertFalse(codec.<Boolean, BigDecimal>decode(BigDecimal.ZERO));
	}

	@Test
	public void testCMISDateTimeCodec() {

		String dateTimeCMISQueryFormat = "1970-01-01T01:00:00.000+01:00";
		DateTime testDateTime = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parseDateTime(dateTimeCMISQueryFormat);

		CMISPropertyTypeCodec codec = CMISPropertyTypeCodecUtil.codecFor(DATETIME);

		
		assertTrue(CMISDateTimeCodec.class.isAssignableFrom(codec.getClass()));

		String encodedDate = codec.<String, Date>encode(testDateTime.toDate());
		assertEquals("1970-01-01T00:00:00.000Z", encodedDate);
		
		encodedDate = codec.<String, GregorianCalendar>encode(testDateTime.toGregorianCalendar());
		assertEquals("1970-01-01T00:00:00.000Z", encodedDate);
		
		encodedDate = codec.<String, String>encode(dateTimeCMISQueryFormat);
		assertEquals("1970-01-01T00:00:00.000Z", encodedDate);
		
		Calendar calendar = codec.decode(encodedDate);
		assertEquals(1970, calendar.get(Calendar.YEAR));

	}
	
	@Test
	public void testCMISIDCodec() {
		assertEquals("a_sample_id", CMISPropertyTypeCodecUtil.codecFor(ID).<String, String>encode("a_sample_id"));
		assertEquals("a_sample_id", CMISPropertyTypeCodecUtil.codecFor(ID).<String, String>decode("a_sample_id"));
	}
	
	@Test
	public void testCMISIntegerCodec() {
		//Expects Integer|BigInteger|String
		//encode
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(INTEGER).<BigDecimal, Integer>encode(10));
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(INTEGER).<BigDecimal, BigInteger>encode(BigInteger.valueOf(10)));
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(INTEGER).<BigDecimal, String>encode("10"));
		//decode
		assertEquals(BigInteger.valueOf(10), CMISPropertyTypeCodecUtil.codecFor(INTEGER).<Integer, BigDecimal>decode(new BigDecimal(10)));
	}
	
	@Test
	public void testCMISDecimalCodec() {
		//Expects Double|BigDecimal|String
		//encode
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, Double>encode(10d));
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, BigDecimal>encode(new BigDecimal(10)));
		assertEquals(new BigDecimal(10), CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, String>encode("10"));
		assertEquals(new BigDecimal("10.0"), CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, String>encode("10.0"));
		//decode
		assertEquals(new BigDecimal(10.99999999), CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, BigDecimal>decode(new BigDecimal(10.99999999)));
	}
	
	@Test
	public void testCMISHTMLCodec() {
		assertEquals("<html lang='en'><!-- a comment -->&nbsp;</html>", CMISPropertyTypeCodecUtil.codecFor(HTML).<String, String>encode("<html lang='en'><!-- a comment -->&nbsp;</html>"));
		assertEquals("<html lang='en'><!-- a comment -->&nbsp;</html>", CMISPropertyTypeCodecUtil.codecFor(HTML).<String, String>decode("<html lang='en'><!-- a comment -->&nbsp;</html>"));
	}
	
	@Test
	public void testCMISStringCodec() {
		assertEquals("a string", CMISPropertyTypeCodecUtil.codecFor(STRING).<String, String>encode("a string"));
		assertEquals("a string", CMISPropertyTypeCodecUtil.codecFor(STRING).<String, String>decode("a string"));
	}
	
	@Test
	public void testCMISUriCodec() {
		assertEquals("http://www.google.com/technology/pigeonrank.html", CMISPropertyTypeCodecUtil.codecFor(URI).<String, String>encode("http://www.google.com/technology/pigeonrank.html"));
		assertEquals("http://www.google.com/technology/pigeonrank.html", CMISPropertyTypeCodecUtil.codecFor(URI).<String, String>decode("http://www.google.com/technology/pigeonrank.html"));
	}
	
	@Test
	public void testCMISIntegerCodecMalformedIntegerString() {
		try {
			CMISPropertyTypeCodecUtil.codecFor(INTEGER).<BigDecimal, String>encode("10.0");
			fail("Exception should be thrown for CMISPropertyTypeCodec.codecFor(INTEGER).<BigDecimal, String>encode(\"10.0\")");
		} catch(Exception e) {
			assertTrue(e instanceof CmisInvalidArgumentException);
		}
	}
	
	@Test
	public void testCMISDecimalCodecMalformedDecimalString() {
		try {
			CMISPropertyTypeCodecUtil.codecFor(DECIMAL).<BigDecimal, String>encode("10,0");
			fail("Exception should be thrown for CMISPropertyTypeCodec.codecFor(DECIMAL).<BigDecimal, String>encode(\"10,0\")");
		} catch(Exception e) {
			assertTrue(e instanceof CmisInvalidArgumentException);
		}
	}
	
	@Test(expected=CmisInvalidArgumentException.class)
	public void testDateTimeCodecMalformedDateTimeString() {
		//Has extra single quotes around T ('T'), should be 1970-01-01T01:00:00.000+01:00
		CMISPropertyTypeCodecUtil.codecFor(DATETIME).<String, String>encode("1970-01-01'T'01:00:00.000+01:00");
	}
	
	@Test(expected=CmisInvalidArgumentException.class)
	public void testDateTimeCodecMalformedISO_UTCString() {
		//Has extra single quotes around T ('T'), should be 1970-01-01T01:00:00.000+01:00
		CMISPropertyTypeCodecUtil.codecFor(DATETIME).<String, String>decode("1970-01-01'T'00:00:00.000Z");
	}

	private Class getCodecClass(String codecClassName) {

		Class[] declaredClasses = CMISPropertyTypeCodec.class.getDeclaredClasses();

		for (Class declaredClass : declaredClasses) {

			if (declaredClass.getName().indexOf("CMISPropertyTypeCodec$" + codecClassName) >= 0) {
				return declaredClass;
			}
		}

		return null;
	}
}
