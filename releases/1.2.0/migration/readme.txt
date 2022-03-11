How to build the package:
- mvn clean compile test-compile jar:jar jar:test-jar dependency:copy-dependencies -DskipTests=true
- (optional) clean unnecesary resources from repo-tests.jar (dbunit, indexlogs, jmeter)
- extract files and remove from repo-tests.jar 
	* environment.properties
	* confidential.properties
	* ecas-proxy-validation.properties
	* jmx_access.properties
	* jmx_password.properties
	* log4j.properties (add the line: log4j.logger.eu.trade.repo.migration.CMIS11Migrate=INFO)
- move database keys from environment.properties to confidential.properties 

How to run:
- edit confidential.properties to configure the target database
- run migrate.bat


