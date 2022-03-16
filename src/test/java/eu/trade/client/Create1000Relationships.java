package eu.trade.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;

public class Create1000Relationships {
	
	
	public static void main(String[] args) throws IOException {
		Session session = getSession();
		
		//c0a16b51cb8e6d9bff72fe358c25d37adc7efffe /shared folder
		//293c77a56cf064f234a240018335b569b6eb8ac5 /home/martjoe/eh
		//7259636a110ba7db2e885dc4c592741e93ea9115 /home/martjoe/madb
		
		//1000 relationships for Allen on madb
		for(int i=0; i<1000; i++) {
			
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:relationship");
			properties.put(PropertyIds.NAME, "rel-" + i);
			properties.put("cmis:sourceId", "7259636a110ba7db2e885dc4c592741e93ea9115");
			properties.put("cmis:targetId", "c0a16b51cb8e6d9bff72fe358c25d37adc7efffe");
			
			List<Ace> ace = new ArrayList<Ace>();
			AccessControlEntryImpl acei = new AccessControlEntryImpl();
			acei.setPrincipal(new AccessControlPrincipalDataImpl("kardaal"));
			acei.setPermissions(Arrays.asList("cmis:all"));
			ace.add(acei);

			acei = new AccessControlEntryImpl();
			acei.setPrincipal(new AccessControlPrincipalDataImpl("martjoe"));
			acei.setPermissions(Arrays.asList("cmis:all"));
			ace.add(acei);

			session.createRelationship(properties, Collections.EMPTY_LIST, ace, Collections.EMPTY_LIST);
			
		}

		//one relationship for Tomasz on eh
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:relationship");
		properties.put(PropertyIds.NAME, "rel-1001");
		properties.put("cmis:sourceId", "293c77a56cf064f234a240018335b569b6eb8ac5");
		properties.put("cmis:targetId", "c0a16b51cb8e6d9bff72fe358c25d37adc7efffe");
		
		List<Ace> ace = new ArrayList<Ace>();
		AccessControlEntryImpl acei = new AccessControlEntryImpl();
		acei.setPrincipal(new AccessControlPrincipalDataImpl("hadamto"));
		acei.setPermissions(Arrays.asList("cmis:all"));
		ace.add(acei);
		
		acei = new AccessControlEntryImpl();
		acei.setPrincipal(new AccessControlPrincipalDataImpl("martjoe"));
		acei.setPermissions(Arrays.asList("cmis:all"));
		ace.add(acei);
		
		session.createRelationship(properties, Collections.EMPTY_LIST, ace, Collections.EMPTY_LIST);
		
		
		
	}
	
	

	private static Session getSession() {

		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "martjoe");
		parameter.put(SessionParameter.PASSWORD, "admin");

		// connection settings
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, "http://localhost:8080/repo/services11/cmis?wsdl");
		
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "shared");

		// create session
		Session session = factory.createSession(parameter);
		return session;
	}

}
