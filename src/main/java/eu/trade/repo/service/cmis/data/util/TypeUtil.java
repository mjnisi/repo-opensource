package eu.trade.repo.service.cmis.data.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;

import eu.trade.repo.util.Constants;

/**
 * Helper class to deal with properties typed data.
 * 
 * @author porrjai
 */
public final class TypeUtil {


	private TypeUtil() {
	}

	public static final TypeParser<String> STRING_PARSER =
			new TypeParser<String>() {
		@Override
		public String parse(String value) {
			return value;
		}
	};

	public static final TypeParser<Boolean> BOOLEAN_PARSER =
			new TypeParser<Boolean>() {
		@Override
		public Boolean parse(String value) {
			return Boolean.valueOf(value);
		}
	};

	public static final TypeParser<GregorianCalendar> DATETIME_PARSER =
			new TypeParser<GregorianCalendar>() {
		@Override
		public GregorianCalendar parse(String value) {
			return DateTime.parse(value).toGregorianCalendar();
		}
	};

	public static final TypeParser<BigDecimal> BIGDECIMAL_PARSER =
			new TypeParser<BigDecimal>() {
		@Override
		public BigDecimal parse(String value) {
			return new BigDecimal(value);
		}
	};

	public static final TypeParser<BigInteger> BIGINTEGER_PARSER =
			new TypeParser<BigInteger>() {
		@Override
		public BigInteger parse(String value) {
			return new BigInteger(value);
		}
	};

	public static <T> List<T> split(String values, TypeParser<T> parser) {
		List<T> list = new ArrayList<>();
		if (values != null && !values.isEmpty()) {
			String[] split = values.split(Constants.CMIS_MULTIVALUE_SEP);
			for (String value : split) {
				list.add(parser.parse(value));
			}
		}
		return list;
	}
}
