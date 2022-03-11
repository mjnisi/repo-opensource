package eu.trade.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public class TestPolicies {
	
	
	public static void main(String[] args) throws IOException {
		Session session = getSession();
		
		Folder folder = (Folder)session.getObjectByPath("/var");
			
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "trade:quotaPolicy");
		properties.put(PropertyIds.NAME, "quota.pol");
		/*properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, Arrays.asList(new String[] {"tag"}));
		properties.put("trade:quota", new BigInteger("12345"));
		properties.put("name", Arrays.asList(new String[] { "house", "tree"}));*/
		
		Policy pol = (Policy)folder.createPolicy(properties);
	}
	
	

	private static Session getSession() {

		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");

		String serverPrefix = "http://localhost:8080/repo";
		
		// connection settings
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, serverPrefix + "/services11/cmis?wsdl");
		
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "policies");

		// create session
		Session session = factory.createSession(parameter);
		return session;
	}

}
