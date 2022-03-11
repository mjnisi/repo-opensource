package eu.trade.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

public abstract class BaseJob {

	protected static Session getRESTSession() {

		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "test");
		parameter.put(SessionParameter.PASSWORD, "test");

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, "http://servertst:8080/repo/atom");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "load");

		// create session
		Session session = factory.createSession(parameter);
		return session;
	}


	protected static Session getSOAPSession() {

		// default factory implementation
		SessionFactory factory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();

		// user credentials
		parameter.put(SessionParameter.USER, "admin");
		parameter.put(SessionParameter.PASSWORD, "admin");

		String host = "http://serverdev:8080/repo";
		//String host = "http://martjoe:8080/repo";

		// connection settings
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, 		host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, 	host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE, 		host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE, 	host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE, host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE, 	host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE, 		host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE, 		host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE, host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE, 	host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE, 	host + "/services11/cmis?wsdl");
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "ECDB");


		// create session
		Session session = factory.createSession(parameter);
		//initialize
		session.getRootFolder();

		return session;
	}

	protected static Session getSession(BindingType type) {

		if(BindingType.WEBSERVICES.equals(type)) {
			return getSOAPSession();
		} else if (BindingType.ATOMPUB.equals(type)) {
			return getRESTSession();
		}

		return null;

	}

}
