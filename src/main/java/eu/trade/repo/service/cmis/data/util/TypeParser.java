package eu.trade.repo.service.cmis.data.util;

/**
 * Interface for all the simple type parsers.
 * 
 * @author porrjai
 *
 * @param <T> The result type.
 */
public interface TypeParser<T> {

	/**
	 * Returns the typed object parses the string value. 
	 * 
	 * @param value {@link String} The value to be parsed.
	 * @return <T> The typed object parses the string value.
	 */
	T parse(String value);
}
