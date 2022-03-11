
The extra-step to be executed: fill the Property.NORMALIZED_VALUE field in database for all properties of type 'string'

To execute:
1. Modify database connection properties in the file: config/repo-normalizer.properties
2. Execute from this directory:
> java -jar repo-normalizer.jar
3. Wait until the application finish (message: END normalizer will be printed.) 