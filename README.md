**Clean and install external libraries on local maven repository**

mvn clean

**Build**

mvn package -DskipTests=true

**Package**

Add environment properties from /dc-config/<env> (environment.properties, log4j_repo.properties, ecas-proxy-validation, etc) into
a folder included in the server classpath.

decide_cmis.war should be deployable to the target DC environment as decide_cmis.war (copy and rename)

**Install**

_Local Config_

On tomcat add oracle jdbc jar into tomcat_home/lib


