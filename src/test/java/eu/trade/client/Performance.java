package eu.trade.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Performance extends BaseJob {
	
	
	/**
	  To run this app:
	  
	  Add this in the pom:
	  
	  <dependency>
		  <groupId>eu.europa.ec.digit.iam.ecas.client</groupId>
		  <artifactId>ecas-desktop-windows</artifactId>
		  <version>3.11.2</version>
		</dependency>
		<dependency>
		  <groupId>eu.europa.ec.digit.iam.ecas.client</groupId>
		  <artifactId>ecas-desktop</artifactId>
		  <version>3.11.2</version>
		</dependency>

	  
	  Comment:
	  
	  <dependency>
            <groupId>ec.eu.europa</groupId>
              <artifactId>ecas-tomcat7.0</artifactId>
              <version>1.20.1</version>
        </dependency>
        
        
        If you are hacing issues to get the dependencies try with:
        
        
        <repositories>
		<repository>
			<id>lib</id>
			<name>lib</name>
			<url>http://lib.trade.cec.eu.int:8080/artifactory/repo</url>
		</repository>
		<repository>
			<id>citnet</id>
			<name>citnet</name>
			<url>https://webgate.ec.europa.eu/CITnet/nexus/content/repositories/ecas</url>
		</repository>
	</repositories>
	
	
	Create the file src/test/resources/ecas-config.xml with:
	
	
	<?xml version="1.0" encoding="utf-8"?>
<client-config xmlns="https://ecas.ec.europa.eu/cas/schemas/client-config/ecas/"
               xmlns:cas="https://ecas.ec.europa.eu/cas/schemas/client-config/cas/">
    <ecasBaseUrl>https://ecas.cc.cec.eu.int:7002</ecasBaseUrl>
    <groups>
        <group>CMISDIR_GROUP</group>
    </groups>
    <acceptStrengths>
        <strength>STRONG</strength>
    </acceptStrengths>
    <connectionTimeout>120000</connectionTimeout>
    <strictSSLHostnameVerification>true</strictSSLHostnameVerification>
    <requestingUserDetails>true</requestingUserDetails>
    <acceptedTicketTypes>
        <ticketType>SERVICE</ticketType>
        <ticketType>PROXY</ticketType>
        <ticketType>DESKTOP</ticketType>
    </acceptedTicketTypes>
    <assuranceLevel>LOW</assuranceLevel>
    <proxyGrantingProtocol>DESKTOP</proxyGrantingProtocol>
</client-config>


	 Settings on where to connect are in BaseJob
	
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Performance.class);
	
	
	public static void main(String[] args) throws Exception {
		
		for(int i=2;i<=100;i+=2) {
			Performance p = new Performance();
			p.run(i,1000);
		}
	}
	
	public void run(int concurrency, int samples) throws Exception {

		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
		CountDownLatch countDownLatch = new CountDownLatch(concurrency);

		//initialise the connections
		
		for(int i=0;i<concurrency;i++) {
			List<String> tmpIds = new ArrayList<String>();
			for(int j=0;j<samples;j++) {
				//tmpIds.add(getRandomObjects(BaseTypeId.CMIS_DOCUMENT.value(), false));
				tmpIds.add(getRandomObjects(BaseTypeId.CMIS_FOLDER.value(), false));
			}
			executorService.execute(new JobGetChildren(tmpIds, Integer.toString(concurrency), countDownLatch, exceptionCollector, BindingType.WEBSERVICES));
			//executorService.execute(new JobGetObject(tmpIds, Integer.toString(concurrency), countDownLatch, exceptionCollector, BindingType.WEBSERVICES));
			//executorService.execute(new JobGetObjectByPath(tmpIds, Integer.toString(concurrency), countDownLatch, exceptionCollector, BindingType.WEBSERVICES));
		}

		countDownLatch.await();
		executorService.shutdown();
//		
//		Session session = getSOAPSession();
//		List<RepositoryInfo> infos = session.getBinding().getRepositoryService().getRepositoryInfos(null);
//		for(RepositoryInfo i: infos) {
//			System.out.println(i.getId());
//		}
//		
		
	}
	
	private static List<String> folderIds;
	private static int lastFolderIdIndex = 0;
	
	private synchronized static String getRandomObjects(String objectType, boolean paths) {

		if(folderIds == null) {
			LOG.debug("getRandomObjectId ... start collecting");
			folderIds = new ArrayList<String>();	

			Session session = getSOAPSession();
			
			ItemIterable<QueryResult> items = session.query("select cmis:objectId from "+ objectType, false);
			Iterator<QueryResult> iterator = items.getPage(10000).iterator();
			while(iterator.hasNext()) {
				String id = iterator.next().getPropertyById("cmis:objectId").getFirstValue().toString();

				if(paths) {
					Document doc = (Document)session.getObject(id);
					LOG.debug("id: " + id + ", paths: " + doc.getPaths());
					folderIds.addAll(doc.getPaths());
				} else {
					folderIds.add(id);
				}
				
			}
			
			Collections.shuffle(folderIds);
			LOG.debug("getRandomObjectId ... end collecting");
		}
		
		String folderId = folderIds.get(lastFolderIdIndex);
		lastFolderIdIndex++;
		if(lastFolderIdIndex>=folderIds.size()) {
			lastFolderIdIndex = 0;
		}

		
		return folderId;
	}
	
	

}
