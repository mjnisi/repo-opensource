package eu.trade.repo.policy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.policyimpl.PolicyMonitor;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;

public class PolicyAspectTest extends BaseTestClass {

	@Autowired
	private PolicyMonitor policyMonitor;
	
	/**
	 * Test for a policy that stops the execution.
	 * 
	 * there is a repository with a policy enforcing some quota.
	 * When a user try to create a document the size of the folder is
	 * calculated and the bypassing the creation of the object.  
	 *	
	 * /bin
	 * /home/hadamto
	 * /home/azaridi
 	 * /home/martjoe (applied policy.pol)
	 * /home/martjoe/projects/dualuse
	 * /home/martjoe/projects/eh (e8fa835ab126b643f87916b2139838c890ea1b92)
	 * /home/martjoe/projects/madb/example.txt (10240 bytes)
	 * /var/policy.pol (10000 bytes quota)
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicyOnDocumentCreate() throws Exception {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies01.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioPolicies01-enableQuotaPolicy.xml", DatabaseOperation.INSERT);
		
		Properties props = buildDocumentProperties();
		ContentStream contentStream = buildDocumentContentStream();

		try {
			cmisObjectService.createDocument(
				"policies", props, "e8fa835ab126b643f87916b2139838c890ea1b92", contentStream, 
					VersioningState.NONE, null, null, null, null);
			Assert.fail();
		} catch (CmisConstraintException e) {
			Assert.assertEquals("Unable to proceed with the action due to policy restrictions: Quota exceeded.", e.getMessage());
			Assert.assertEquals(InvocationTargetException.class, e.getCause().getClass());
			Assert.assertEquals("Quota exceeded.", ((InvocationTargetException)e.getCause()).getTargetException().getMessage());
		}
		
	}




	/**
	 * Test to validate several policies.
	 *
	 * A folder is going to be created under the folder /home/hadamto and
	 * two policies will be triggered
	 * 
	 * /bin
	 * /home (applied a.pol)
	 * /home/hadamto (applied b.pol) 8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf
	 * /home/azaridi
	 * /home/martjoe (applied policy.pol)
	 * /home/martjoe/projects/dualuse
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb/example.txt (10240 bytes)
	 * /var/policy.pol (10000 bytes quota)
	 * /var/a.pol 
	 * /var/b.pol
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicySuccessfullPREAndPOST() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies01.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioPolicies02.xml", DatabaseOperation.INSERT);
		setScenario("scenarioPolicies02-enableAPolicy.xml", DatabaseOperation.INSERT);
		setScenario("scenarioPolicies02-enableBPolicy.xml", DatabaseOperation.INSERT);
		
		policyMonitor.reset();
		Assert.assertEquals(0, policyMonitor.getCounter());
		
		Properties props = buildFolderProperties();
		
		//create the folder /home/hadamto/tmp
		cmisObjectService.createFolder("policies", props, 
				"8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf", null, null, null, null);
		
		//this should be triggered both policies
		Assert.assertEquals(4, policyMonitor.getCounter());
		
	}


	/**
	 * Test to validate disabled policies.
	 *
	 * A folder is going to be created under the folder /home/hadamto and
	 * no policies will be triggered because both types are disabled
	 * 
	 * /bin
	 * /home (applied a.pol)
	 * /home/hadamto (applied b.pol) 8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf
	 * /home/azaridi
	 * /home/martjoe (applied policy.pol)
	 * /home/martjoe/projects/dualuse
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb/example.txt (10240 bytes)
	 * /var/policy.pol (10000 bytes quota)
	 * /var/a.pol 
	 * /var/b.pol
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicyNoEnabledPolicies() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies01.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioPolicies02.xml", DatabaseOperation.INSERT);
		
		policyMonitor.reset();
		Assert.assertEquals(0, policyMonitor.getCounter());
		
		Properties props = buildFolderProperties();
		//create the folder /home/hadamto/tmp
		cmisObjectService.createFolder("policies", props, 
				"8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf", null, null, null, null);
		
		//this should be triggered both policies
		Assert.assertEquals(0, policyMonitor.getCounter());
	}
	
	
	/**
	 * Test to validate disabled policies and enabled policies at the same time.
	 *
	 * A folder is going to be created under the folder /home/hadamto and
	 * a policy will be triggered because one disabled and one is enabled
	 * 
	 * /bin
	 * /home (applied a.pol)
	 * /home/hadamto (applied b.pol) 8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf
	 * /home/azaridi
	 * /home/martjoe (applied policy.pol)
	 * /home/martjoe/projects/dualuse
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb/example.txt (10240 bytes)
	 * /var/policy.pol (10000 bytes quota)
	 * /var/a.pol 
	 * /var/b.pol
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicyOneEnabledPolicy() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies01.xml", DatabaseOperation.CLEAN_INSERT);
		setScenario("scenarioPolicies02.xml", DatabaseOperation.INSERT);
		setScenario("scenarioPolicies02-enableAPolicy.xml", DatabaseOperation.INSERT);
		
		policyMonitor.reset();
		Assert.assertEquals(0, policyMonitor.getCounter());
		
		Properties props = buildFolderProperties();
		//create the folder /home/hadamto/tmp
		cmisObjectService.createFolder("policies", props, 
				"8694e116fa6b2dc4dac6f82ba17cb9d029eeffaf", null, null, null, null);
		
		//this should be triggered both policies
		Assert.assertEquals(2, policyMonitor.getCounter());
		Assert.assertEquals(2, policyMonitor.getMessages().size());
		Assert.assertTrue(policyMonitor.getMessages().containsAll(
				Arrays.asList("A BEFORE (createFolder)", "A AFTER (createFolder)")));
		
	}
	
	/**
	 * Test to validate policies in a multi-filed example.
	 * 
	 * The document motd.txt is multi-filed in four folders, some of them
	 * have applied the policy A and some of them the policy B.
	 * 
	 * From a folder that apparently only have the policy A, we 
	 * are going to test that the policy B is triggered as well because of the
	 * multi-filing nature of the document.
	 * 
	 * The method to trigger is updateProperties.
	 * 
	 *  /bin
	 * 	
	 * 	/home
	 * 	/home/motd.txt (multifiled under: /home, /home/martjoe, /home/hadamto and /home/azaridi)
	 * 
	 * 	/home/martjoe (a.pol applied)
	 * 	/home/martjoe/motd.txt
	 * 	/home/martjoe/projects
	 * 	/home/martjoe/projects/madb
	 * 	/home/martjoe/projects/madb/example.txt
	 * 	/home/martjoe/projects/eh
	 * 	/home/martjoe/projects/dues
	 *
	 * 	/home/hadamto (a.pol applied)
	 * 	/home/hadamto/motd.txt
	 *
	 * 	/home/azaridi (b.pol applied)
	 * 	/home/azaridi/motd.txt
	 *
	 * 	/var/quota.pol
	 * 	/var/a.pol
	 * 	/var/b.pol
	 * 
	 * @throws Exception
	 */	
	@Test
	public void triggerPolicyMultifiling() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies03.xml", DatabaseOperation.CLEAN_INSERT);
		
		policyMonitor.reset();
		
		String motdId = "d02ad8fc18159ba79ed191734cff3abda6e5eaf9";
		//update motd.txt
		ObjectData object = cmisObjectService.getObject("policies", motdId, null, false, IncludeRelationships.NONE, 
				null, false, false, null);
		Holder<String> id = new Holder<String>(object.getId());
		Holder<String> changeToken = new Holder<String>((String)object.getProperties().getProperties().get("cmis:changeToken").getFirstValue());
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("cmis:description", "new description"));
		cmisObjectService.updateProperties("policies", id, changeToken, properties, null);
		
		Assert.assertTrue(policyMonitor.getMessages().contains("B BEFORE (updateProperties)"));
		Assert.assertTrue(policyMonitor.getMessages().contains("B AFTER (updateProperties)"));
		Assert.assertEquals(2, policyMonitor.getCounter());

	}
	 

	/**
	 * Test to validate that a single policy object applied to several objects
	 * is triggering the policy code only with the ancestor objects
	 * 
	 * 	only policy A enabled
	 * 
	 * /bin
	 * /home
	 * /home/azaridi (applied policy a.pol)
	 * /home/hadamto (applied policy a.pol)
	 * /home/martjoe (applied policy a.pol)
	 * /home/martjoe/projects
	 * /home/martjoe/projects/dues
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb
	 * /home/martjoe/projects/madb/example.txt
	 * /var
	 * /var/a.pol
	 * /var/b.pol
	 * /var/quota.pol
	 * 
	 */
	@Test
	public void triggerPolicySeveralAncestors() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies04.xml", DatabaseOperation.CLEAN_INSERT);
		
		policyMonitor.reset();
		
		String folderId = "7bc70b69272f4c1d24ccfe5d1ff22199cdc40f59";// /home/martjoe
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("cmis:name", "myNewItem"));
		cmisObjectService.createItem("policies", properties, folderId, null, null, null, null);

		Assert.assertEquals(2, policyMonitor.getCounter());
		Assert.assertTrue(policyMonitor.getMessages().contains(
			"A AFTER (createItem) " + Collections.singletonList(folderId)));
		Assert.assertTrue(policyMonitor.getMessages().contains(
			"A BEFORE (createItem) " + Collections.singletonList(folderId)));
	}
	

	/**
	 * Test the pre state in the policy code.
	 * 
	 * The A policy is removing any space in the cmis:name on createDocument 
	 * invocations.
	 * 
	 * 	only policy A enabled
	 * 
	 * /bin
	 * /home
	 * /home/azaridi (applied policy a.pol)
	 * /home/hadamto (applied policy a.pol)
	 * /home/martjoe (applied policy a.pol)
	 * /home/martjoe/projects
	 * /home/martjoe/projects/dues
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb
	 * /home/martjoe/projects/madb/example.txt
	 * /var
	 * /var/a.pol
	 * /var/b.pol
	 * /var/quota.pol
	 * 
	 */
	@Test
	public void triggerPolicyPre() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies04.xml", DatabaseOperation.CLEAN_INSERT);
		
		policyMonitor.reset();
		
		String folderId = "7bc70b69272f4c1d24ccfe5d1ff22199cdc40f59";// /home/martjoe
		PropertiesImpl properties = new PropertiesImpl();
		properties.addProperty(new PropertyStringImpl("cmis:name", "my New Doc"));
		String id = cmisObjectService.createDocument("policies", properties, folderId, 
				null, VersioningState.NONE, null, null, null, null);

		Assert.assertEquals(2, policyMonitor.getCounter());
		Assert.assertTrue(policyMonitor.getMessages().contains("A BEFORE (createDocument)"));
		Assert.assertEquals(1, policyMonitor.getMessages().size());
		
		ObjectData objectData = cmisObjectService.getObject(
			"policies", id, null, false, IncludeRelationships.NONE, null, false, false, null);
		String name = objectData.getProperties().getProperties().get("cmis:name").getFirstValue().toString();
		Assert.assertEquals("myNewDoc", name);
		
	}

	/**
	 * Test the post state in the policy code.
	 * 
	 * The A policy is tracking the number of elements in a getChildren execution
	 * 
	 * 	only policy A enabled
	 * 
	 * /bin
	 * /home
	 * /home/azaridi (applied policy a.pol)
	 * /home/hadamto (applied policy a.pol)
	 * /home/martjoe (applied policy a.pol)
	 * /home/martjoe/projects
	 * /home/martjoe/projects/dues
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb
	 * /home/martjoe/projects/madb/example.txt
	 * /var
	 * /var/a.pol
	 * /var/b.pol
	 * /var/quota.pol
	 * 
	 */
	@Test
	public void triggerPolicyPost() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies04.xml", DatabaseOperation.CLEAN_INSERT);
		
		policyMonitor.reset();
		
		String folderId = "1e11ce609f46443d9fe91197f3363ec992e2ee7a";// /home/martjoe/projects
		ObjectInFolderList list = 
				cmisNavigationService.getChildren("policies", folderId, null, null, false, 
				IncludeRelationships.NONE, null, true, 
				new BigInteger("10"), BigInteger.ZERO, 
				null);
		
		Assert.assertTrue(policyMonitor.getMessages().contains("A AFTER (getChildren) 3"));
		Assert.assertEquals(3, list.getNumItems().intValue());
		
	}

	/**
	 * Test to validate executions when the service is returning an exception (failing)
	 * 
	 * 	only policy A enabled
	 * 
	 * /bin
	 * /home
	 * /home/azaridi (applied policy a.pol)
	 * /home/hadamto (applied policy a.pol)
	 * /home/martjoe (applied policy a.pol)
	 * /home/martjoe/projects
	 * /home/martjoe/projects/dues
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb
	 * /home/martjoe/projects/madb/example.txt
	 * /var
	 * /var/a.pol
	 * /var/b.pol
	 * /var/quota.pol	 
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicyServiceError() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies04.xml", DatabaseOperation.CLEAN_INSERT);
		
		
		String folderId = "1e11ce609f46443d9fe91197f3363ec992e2ee7a";// /home/martjoe/projects
		Properties props = buildFolderProperties();
		
		//first time should be fine
		cmisObjectService.createFolder("policies", props, folderId, null, null, null, null);
		policyMonitor.reset();

		try {
			//second time a name constrain should be triggered
			cmisObjectService.createFolder("policies", props, folderId, null, null, null, null);
			Assert.fail();
		} catch (CmisNameConstraintViolationException e) {
			//no-op
		}
		
		Assert.assertTrue(policyMonitor.getMessages().contains("A BEFORE (createFolder)"));
		Assert.assertTrue(!policyMonitor.getMessages().contains("A AFTER (createFolder)"));
		
	}
	
	
	/**
	 * Test to validate executions when the object ID does not exist.
	 * 
	 * 	only policy A enabled
	 * 
	 * /bin
	 * /home
	 * /home/azaridi (applied policy a.pol)
	 * /home/hadamto (applied policy a.pol)
	 * /home/martjoe (applied policy a.pol)
	 * /home/martjoe/projects
	 * /home/martjoe/projects/dues
	 * /home/martjoe/projects/eh
	 * /home/martjoe/projects/madb
	 * /home/martjoe/projects/madb/example.txt
	 * /var
	 * /var/a.pol
	 * /var/b.pol
	 * /var/quota.pol	 
	 * 
	 * @throws Exception
	 */
	@Test
	public void triggerPolicyServiceWrongId() throws Exception {
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, "policies");

		setScenario("scenarioPolicies04.xml", DatabaseOperation.CLEAN_INSERT);
		
		policyMonitor.reset();
		String folderId = "-wrong-id";
		try {
			ObjectInFolderList list = 
					cmisNavigationService.getChildren("policies", folderId, null, null, false, 
					IncludeRelationships.NONE, null, true, 
					new BigInteger("10"), BigInteger.ZERO, 
					null);
			Assert.fail();
		} catch (CmisObjectNotFoundException e) {
			//no-op
		}

		//no policy should be executed 
		Assert.assertTrue(policyMonitor.getMessages().isEmpty());
		Assert.assertEquals(0, policyMonitor.getCounter());
		
	}	
	
	
	/**
	 * Builds the needed properties to create a folder.
	 * 
	 * name: tmp
	 * type: cmis:folder
	 * 
	 * @return
	 */
	private Properties buildFolderProperties() {
		CMISObject folder1 = new CMISObject();

		ObjectType docType = new ObjectType("cmis:folder");
		folder1.setObjectType(docType);

		folder1.addProperty(getTestProperty("tmp", "policies", Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder1.addProperty(getTestProperty("cmis:folder", "policies", Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(folder1, "");
		return props;
	}
	
	/**
	 * Builds the needed properties to create a document.
	 * 
	 * name: MyDocument.txt
	 * type: cmis:document
	 * 
	 * @return
	 */
	private Properties buildDocumentProperties() {
		CMISObject doc1 = new CMISObject();

		ObjectType docType = new ObjectType("cmis:document");
		doc1.setObjectType(docType);

		doc1.addProperty(getTestProperty("MyDocument.txt", "policies", Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc1.addProperty(getTestProperty("cmis:document", "policies", Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));

		Properties props = PropertiesBuilder.build(doc1, "");
		return props;
	}
	
	
	/**
	 * Builds a ContentStream
	 * 
	 * @return
	 * @throws IOException
	 */
	private ContentStream buildDocumentContentStream() throws IOException {
		byte[] text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam ut purus purus.".getBytes();
		InputStream is = new ByteArrayInputStream(text);
		ContentStream contentStream = new ContentStreamImpl("MyDocument.txt", BigInteger.valueOf(is.available()), "text/plain", is);
		return contentStream;
	}
	
}
