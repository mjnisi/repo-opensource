package eu.trade.repo.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General utility methods.
 */
public final class Utilities {

	private static final Logger LOG = LoggerFactory.getLogger(Utilities.class);

	private static final Pattern PROPERTY_VALUE_IN_SINGLE_QUOTES = Pattern.compile("^'(.*)'$");

	private Utilities() {
	}

	public static Boolean getBooleanValue(Character b) {
		if (b == null) {
			return null;
		}
		return b.equals('T');

	}

	public static Character setBooleanValue(Boolean val) {
		if (val == null) {
			return null;
		}
		return val ? 'T' : 'F';
	}

	public static String removePropertyValueSingleQuotes(String propValueInSingleQuotes) {

		String result = PROPERTY_VALUE_IN_SINGLE_QUOTES.matcher(propValueInSingleQuotes).replaceAll("$1");

		return result;
	}
	
	public static String convertCMIStoSQLEscapes(String cmisPropertyValue) {
		cmisPropertyValue = cmisPropertyValue.replaceAll("\\Q\\'\\E", "'");
		return cmisPropertyValue.replaceAll("\\Q\\\\\\E", Matcher.quoteReplacement("\\"));
	}

	/**
	 * Returns true if at least one of the collections is null or empty.
	 * 
	 * @param collections {@link Collection[]} The collections to check. Mandatory not null.
	 * @return true if at least one of the collections is null or empty.
	 */
	public static boolean isEmpty(Collection<?>... collections) {
		for (Collection<?> collection : collections) {
			if (collection == null || collection.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if all the collections has the same size.
	 * 
	 * @param collections {@link Collection[]} The collections to check. Mandatory not null.
	 * @return true if all the collections has the same size.
	 */
	public static boolean sameSize(Collection<?>... collections) {
		int size = 0;
		boolean first = true;

		for (Collection<?> collection : collections) {
			if (first) {
				size = collection.size();
			}
			else if (collection.size() != size) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Build a set from the specified array.
	 * <p>
	 * if a null parameter is provided then it will return null.
	 * 
	 * @param array T[] array of T.
	 * @return {@link Set<T>} the set from the specified array.
	 */
	public static <T> Set<T> toSet(T[] array) {
		if (array == null) {
			return null;
		}
		Set<T> result = new HashSet<>(array.length);
		for (T t : array) {
			result.add(t);
		}
		return result;
	}

	/**
	 * Build a set from the specified array.
	 * <p>
	 * if a null parameter is provided then it will return null.
	 * 
	 * @param array T[] array of T.
	 * @return {@link Set<T>} the set from the specified array.
	 */
	public static <T> Set<T> asSet(T... array) {
		return toSet(array);
	}

	/**
	 * Close an array of {@link AutoCloseable} ignoring any closing exception (but logging it).
	 * 
	 * @param closeables {@link AutoCloseable} an array of elemtn to be closed.
	 */
	public static void close(AutoCloseable... closeables) {
		if (closeables != null) {
			for (AutoCloseable closeable : closeables) {
				if (closeable != null) {
					try {
						closeable.close();
					} catch (Exception e) {
						LOG.warn("Error closing: " + closeable, e);
					}
				}
			}
		}
	}

	/**
	 * Load the properties file.
	 * 
	 * @param propertiesFile {@link String} The properties file path;
	 * @return {@link Properties} The properties from the file.
	 * @throws IllegalArgumentException when there is any io problem loading the file.
	 */
	public static Properties loadProperties(String propertiesFile) throws IllegalArgumentException {
		try (InputStream is = new FileInputStream(propertiesFile)) {
			Properties properties = new Properties();
			properties.load(is);
			return properties;
		} catch (IOException e) {
			throw new IllegalArgumentException("Error loading properties file: " + propertiesFile, e);
		}
	}

	public static Properties loadClasspathProperties(String configFile)  throws IllegalArgumentException {
		Properties properties = null;
		try {
			properties = loadProperties(configFile);
		} catch (IllegalArgumentException e) {
			// Second try: locate the path from the context class loader or one of tits parents.
			properties = findAndLoadProperties(configFile, Thread.currentThread().getContextClassLoader());
		}
		return properties;
	}

	private static Properties findAndLoadProperties(String configFile, ClassLoader classLoader)  throws IllegalArgumentException {
		try {
			return loadProperties(configFile, classLoader);
		} catch (IllegalArgumentException e) {
			ClassLoader parent = getParent(classLoader);
			if (parent == null) {
				throw e;
			}
			return findAndLoadProperties(configFile, parent);
		}
	}

	private static Properties loadProperties(String configFile, ClassLoader classLoader)  throws IllegalArgumentException {
		// TODO: review if we can get the classpath resource in other way (some problem with spaces in the path and the %20 encode)
		URL url = classLoader.getResource(configFile);
		if (url == null) {
			throw new IllegalArgumentException("Error loading properties file: " + configFile);
		}
		return loadProperties(url.getPath());
	}

	private static ClassLoader getParent(ClassLoader classLoader) {
		ClassLoader parent = null;
		try {
			parent = classLoader.getParent();
		} catch (SecurityException e) {
			LOG.warn("Access to the parent clazss loader not allowed.", e);
		}
		return parent;
	}

}
