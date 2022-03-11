package eu.trade.client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public class CreateHANFolders {
	
	
	public static void main(String[] args) throws IOException {
		Session session = getRESTSession();
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/java/eu/trade/client/data.txt"));
		String line;
		
		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("#");
			String code = parts[0];
			String title = parts[1];
			String status = parts[2];
			String[] serviceOwner = parts[3].split(";");
		
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "trade:folder");
			properties.put(PropertyIds.NAME, title.replaceAll("/", "-") + ("INACTIVE".equals(status)?" - (inactive)":""));
			properties.put("trade:headingCode", code);
			properties.put("trade:title", title);
			properties.put("trade:status", status);
			properties.put("trade:serviceOwner", Arrays.asList(serviceOwner));
			
			String parentCode = code.substring(0, code.lastIndexOf('.') );
			
			ItemIterable<QueryResult> result = session.query(
				"select cmis:objectId from trade:folder where trade:headingCode = '"+parentCode+"'", false);
			String folderId = session.getRootFolder().getId();
			
			if(result.getTotalNumItems() == 1) {
				folderId = (String)result.iterator().next().getPropertyById(PropertyIds.OBJECT_ID).getFirstValue();
			}
			
			Folder folder = (Folder)session.getObject(folderId);
			folder.createFolder(properties);
			System.out.println("creating ... " + title);
			
		}
		
		reader.close();
		
		
		
	}
	
	

	private static Session getRESTSession() {

		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://serverdev:8080/repo/atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "martjoe");

		// create session
		Session session = factory.createSession(parameter);
		return session;
	}

}
