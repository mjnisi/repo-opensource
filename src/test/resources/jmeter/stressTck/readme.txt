JMeter conf for TCK Tests

1) Copy the following files to $JMETER_HOME/bin/
		tck-parameters.properties		This file sets the connection parameters for the TCK tests.
		jmeter.stats.properties			This files sets the properties for the JMX statistics collector.

2) Run maven with the profile "jmeter":
		mvn clean package -P jmeter

	This build generates two jars:
		repo-jar-with-dependencies.jar
		jmeter-repo-tests.jar
		
3) Copy repo-jar-with-dependencies.jar to $JMETER_HOME/lib/

4) Copy jmeter-repo-tests.jar to $JMETER_HOME/lib/junit/
