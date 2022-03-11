package eu.trade.repo.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class QueryStats {

	static String WORD_QUERY = "select * from (" +
	                              "select distinct word_id, (" +
			                         "select count(min(object_id)) " +
	                                 "from index_word_object " +
			                         "where " +
	                                 "word_id=iwo.word_id " +
			                         "group by object_id" +
	                              ") as wcount " +
                        	      "from index_word_object iwo" +
                               ") join index_word iw on iw.id=word_id " +
                               "where wcount = ";
	
	static String dbUrl = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=158.166.188.37)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=plug1.ec.europa.eu)))";
	
	static String CMIS_QUERY_FULLTEXT = "select cmis:name from cmis:document where contains('#TO REPLACE#')";
	static String CMIS_QUERY_FULLTEXT_SCORE = "select cmis:name, SCORE() from cmis:document where contains('#TO REPLACE#') order by SEARCH_SCORE";
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("oracle.jdbc.OracleDriver");
		
		Connection jdbcConnection = DriverManager.getConnection(dbUrl, "repo_test12", "repo_test12");
		
		Statement statement = jdbcConnection.createStatement();
		ResultSet rsIndexedCount = null;
		
		long statsStartTime = new SimpleDateFormat("yyyy.MM.dd.HH:mm").parse("2014.03.26.01:00").getTime();
		//long statsStartTime = new SimpleDateFormat("yyyy.MM.dd.HH:mm").parse("2014.03.25.17:15").getTime();
		
		do {
		
			rsIndexedCount = statement.executeQuery("select distinct count(*) " +
			                                        "from object where " +
					                                "(index_state_content = 1 or index_state_content = 4) and " +
			                                        "(index_state_metadata = 1 or index_state_metadata = 4) and " +
					                                "object_type_id = 10000");
			rsIndexedCount.next();
		
		} while(rsIndexedCount.getInt(1) < 15000 && System.currentTimeMillis() < statsStartTime);
		
		System.out.println(rsIndexedCount.getInt(1));
		System.out.println(new Date());
		
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "kardaal");
		parameter.put(SessionParameter.PASSWORD, "pass");

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://serverdev.trade.cec.eu.int:8080/repo/atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "madb");
		
		/*parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
		parameter.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.toString(true));
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/ACLService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/DiscoveryService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/MultiFilingService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/NavigationService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/ObjectService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, "hhttp://serverdev.trade.cec.eu.int:8080/repo/services/PolicyService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/RelationshipService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/RepositoryService?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, "http://serverdev.trade.cec.eu.int:8080/repo/services/VersioningService?wsdl");
		parameter.put(SessionParameter.REPOSITORY_ID, "madb");*/

		// create session
		Session session = factory.createSession(parameter);

		System.out.println("Docs,Average response time(milliseconds)");
		
		for(int i=1; i<=10000; i++) {
			
			System.out.print(i + ",");
			
			ResultSet rs = statement.executeQuery(WORD_QUERY + i);
			
			double averageTime = 0.0;
			double previousAvgTime = 0.0;
			
			int wordCount = 0;
			while (rs.next() && wordCount < 30) {

				String word = rs.getString("WORD");
				
				//System.out.println(word);
				
				//System.out.println("Query: " + CMIS_QUERY_FULLTEXT.replaceAll("\\Q#TO REPLACE#\\E", word));
				
				long startTime = System.currentTimeMillis();
				ItemIterable<QueryResult> results = null;
				try {
					//results = session.query(CMIS_QUERY_FULLTEXT.replaceAll("\\Q#TO REPLACE#\\E", word), true);
					OperationContext op = new OperationContextImpl();
					op.setMaxItemsPerPage(10);
					
					results = session.query(CMIS_QUERY_FULLTEXT.replaceAll("\\Q#TO REPLACE#\\E", word), false, op);
					
					//int itemCount=0;
					for(QueryResult hit: results.getPage(10)) {  
						//itemCount++;
						for(PropertyData<?> property: hit.getProperties()) {
					        String queryName = property.getQueryName();
					        Object value = property.getFirstValue();

					        //System.out.println(queryName + ": " + value);
					    }
					    //System.out.println("--------------------------------------");
					}
					//System.out.println("-->" + itemCount);
					
					
				} catch(Exception e) {
					//System.out.println(e);
					continue;
				}
				//System.out.println(results);
				
				long endTime = System.currentTimeMillis();
				//double timeDiff = (endTime-startTime)/1000d;
				double timeDiff = endTime-startTime;
				averageTime += timeDiff;
				wordCount++;
			}
			
			averageTime /= wordCount;
			
			System.out.print(averageTime);
			System.out.print("\n");
			
			//System.out.println(i + " - " + averageTime + " seconds");
		}
		
		
		if(statement != null) {
			statement.close();
		}
		if(jdbcConnection != null) {
			jdbcConnection.close();
		}
		
	}
}
