package eu.trade.repo.util;

import org.apache.commons.configuration.Configuration;

public final class ConfigUtils {

	private ConfigUtils(){}

	/**
	 * Method to get the 'propName' property value if it is a positive value, or the default value instead.
	 * @param config
	 * @param propName
	 * @param defaultValue
	 * @return
	 */
	public static int getIntPositive(Configuration config, String propName, int defaultValue){
		int value = config.getInt(propName, defaultValue);
		value = (0 >= value)? defaultValue : value;
		return value;
	}
	
	/**
	 * Method to get the 'propName' property value if it is between 0 and 1, or the default value if it is between 0 and 1 or 0 instead.
	 * @param config
	 * @param propName
	 * @param defaultValue
	 * @return
	 */
	public static double getPercentage(Configuration config, String propName, double defaultValue){
		double value = config.getDouble(propName, defaultValue);
		value = (0 <= value && 1 >= value)? value : (0 <= defaultValue && 1 >= defaultValue)? defaultValue : 0;
		return value;
	}
	
}
