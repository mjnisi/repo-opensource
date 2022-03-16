package eu.trade.repo;

import java.io.IOException;
import java.util.Properties;



public class TestConnectionConfig {

	private final Properties properties;
	
	public static final String DBUNIT_DATABASE_SCHEMA = "DBUNIT_DATABASE_SCHEMA";
	public static final String DBUNIT_DATATYPE_FACTORY = "DBUNIT_DATATYPE_FACTORY";
	
	public TestConnectionConfig(String propFile) throws IOException{
		properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream(propFile));
	}
	
	public Properties getProperties(){
		return properties;
	}
}
