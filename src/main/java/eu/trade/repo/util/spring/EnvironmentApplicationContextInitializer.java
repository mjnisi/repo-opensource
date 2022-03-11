package eu.trade.repo.util.spring;

import java.util.Properties;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import eu.trade.repo.util.Utilities;

/**
 * {@link ApplicationContextInitializer} that add the environment.properties file to the list of the Spring Environment Property Sources.
 * <p>
 * This enables the application context xml file to use placeholders for the properties defined in environemnt.properties file.
 * <p>
 * Note: Currently it also defines the Spring active profile: "web". This is needed because the current 3.1 version is not able to call this class in
 * the test environment causing the placeholders from environment.properties to be undefined when loading the context.
 * In the Spring version 3.2 this class can be called from the @ContextConfiguration in the test classes, avoiding the need to use the web profile.
 * 
 * @see ConfigurableEnvironment#getPropertySources()
 * @see MutablePropertySources
 * @author porrjai
 */
public class EnvironmentApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	private static final String ENVIRONMENT_PROPERTIES_FILE = "environment.properties";
	private static final String ENVIRONMENT_PROPERTIES_NAME = "application.environment.properties";
	private static final String WEB_PROFILE = "web";
	
	private static final String PROPNAME_H2_SERVER_RESULT_SET_FETCH_SIZE = "REPO_INDEX_H2_SERVER_RESULT_SET_FETCH_SIZE";
	private static final String PROPNAME_H2_SERVER_CACHED_OBJECTS = "REPO_INDEX_H2_SERVER_CACHED_OBJECTS";
	
	private static final String DEFAULT_H2_SERVER_RESULT_SET_FETCH_SIZE = "512";
	private static final String DEFAULT_H2_SERVER_CACHED_OBJECTS = "512";

	/**
	 * Add the ENVIRONMENT_PROPERTIES_FILE as the last property source of the application context environment.
	 * 
	 * @see org.springframework.context.ApplicationContextInitializer#initialize(org.springframework.context.ConfigurableApplicationContext)
	 */
	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		Properties properties = Utilities.loadClasspathProperties(ENVIRONMENT_PROPERTIES_FILE);
		
		System.setProperty("h2.serverCachedObjects", getPropertyValue(properties, PROPNAME_H2_SERVER_CACHED_OBJECTS, DEFAULT_H2_SERVER_CACHED_OBJECTS));
		System.setProperty("h2.serverResultSetFetchSize", getPropertyValue(properties, PROPNAME_H2_SERVER_RESULT_SET_FETCH_SIZE, DEFAULT_H2_SERVER_RESULT_SET_FETCH_SIZE));
		
		PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(ENVIRONMENT_PROPERTIES_NAME, properties);
		applicationContext.getEnvironment().getPropertySources().addLast(propertiesPropertySource);
		applicationContext.getEnvironment().addActiveProfile(WEB_PROFILE);
	}
	
	private String getPropertyValue(Properties properties, String propKey, String defaultValue){
		String result = properties.getProperty(propKey);
		return ( null == result )? defaultValue : result;
	}
}
