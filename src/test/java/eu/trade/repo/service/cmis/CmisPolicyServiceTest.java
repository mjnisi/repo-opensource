package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.TEST_PWD;
import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static eu.trade.repo.TestConstants.TEST_USER;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;

/**
 * 
 * @author azaridi, kardaal
 *
 */
public class CmisPolicyServiceTest extends BaseTestClass {
	@Before
	public void setScenario() throws Exception {
		setUser(TEST_USER, TEST_PWD, TEST_REPO_2);
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
	}
	
	@Test
	public void testQuotaPolicy() throws Exception {
		
		//create quota policy and apply to root folder
		CMISObject quotaPolicy = new CMISObject(new ObjectType("cmis:quotaPolicy"));

		quotaPolicy.addProperty(getTestProperty("Quota Policy 1", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.NAME));
		quotaPolicy.addProperty(getTestProperty("cmis:quotaPolicy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.OBJECT_TYPE_ID));
		quotaPolicy.addProperty(new Property(objTypePropSelector.getObjTypeProperty("cmis:quotaPolicy", "cmis:maxQuota", TEST_REPO_2), 10));
		quotaPolicy.addProperty(getTestProperty("Controls folder max quotas.", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.POLICY_TEXT));
		
		//Collections.<Acl>emptySet(), Collections.<Acl>emptySet()
		
		//TODO: apply policy on create object...? DONE? to check
		cmisObjectService.createPolicy(TEST_REPO_2, PropertiesBuilder.build(quotaPolicy), null, null, null, null, null);
		
		CMISObject doc = new CMISObject(new ObjectType("cmis:document"));

		doc.addProperty(getTestProperty("From document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		doc.addProperty(getTestProperty("cmis:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		
		//calling createDocument should trigger policies applied to said object
		String doc1Id = cmisObjectService.createDocument(TEST_REPO_2, PropertiesBuilder.build(doc), "/", null, null, null, null, null, null);
		//createAcl(true, Arrays.asList(new String [] {TestConstants.TEST_USER, TestConstants.TEST2_USER}))
		
	}

	@Test
	public void testApplyPolicy() throws Exception{
		CMISObject policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		CMISObject testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		
		assertEquals(0, testDoc.getPolicies().size());
		assertEquals(0, policy.getObjectsUnderPolicy().size());
		
		//apply the policy
		cmisPolicyService.applyPolicy(TEST_REPO_2 , policy.getCmisObjectId(), testDoc.getCmisObjectId(), null);
		
		//reload
		testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		
		//verify
		assertEquals(1, testDoc.getPolicies().size());
		assertEquals(1, policy.getObjectsUnderPolicy().size());
	}
	
	@Test
	public void testRemovePolicy() throws Exception{
		CMISObject policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		CMISObject testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		
		//add the policy
		cmisPolicyService.applyPolicy(TEST_REPO_2 , policy.getCmisObjectId(), testDoc.getCmisObjectId(), null);

		//verify
		testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		
		assertEquals(1, testDoc.getPolicies().size());
		assertEquals(1, policy.getObjectsUnderPolicy().size());

		//remove the policy
		cmisPolicyService.removePolicy(TEST_REPO_2, policy.getCmisObjectId(), testDoc.getCmisObjectId(), null);
		
		//verify no more policies for testDoc and vice versa
		testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		
		assertEquals(0, testDoc.getPolicies().size());
		assertEquals(0, policy.getObjectsUnderPolicy().size());
	}

	@Test
	public void testGetAppliedPolicies() {
		//test policy
		CMISObject policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");

		//create a second policy
		CMISObject policyTmp= new CMISObject();
		policyTmp.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_2, "cmis:policy"));
		policyTmp.addProperty(getTestProperty("My Policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.NAME));
		policyTmp.addProperty(getTestProperty("cmis:policy", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.OBJECT_TYPE_ID));
		policyTmp.addProperty(getTestProperty("my test policy!", TEST_REPO_2, Constants.TYPE_CMIS_POLICY, PropertyIds.POLICY_TEXT));
		policyTmp = objectService.createObject(TEST_REPO_2, policyTmp, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(),  null, null, BaseTypeId.CMIS_POLICY);
		
		//2 objects
		CMISObject testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		CMISObject testDoc2= cmisObjectSelector.loadCMISObject("Test Document 2");
		
		//policy and policyTmp for doc and policyTmp for doc2 
		cmisPolicyService.applyPolicy(TEST_REPO_2 , policy.getCmisObjectId(), testDoc.getCmisObjectId(), null);
		cmisPolicyService.applyPolicy(TEST_REPO_2 , policyTmp.getCmisObjectId(), testDoc.getCmisObjectId(), null);
		cmisPolicyService.applyPolicy(TEST_REPO_2 , policyTmp.getCmisObjectId(), testDoc2.getCmisObjectId(), null);
		
		//reload objects
		testDoc= cmisObjectSelector.loadCMISObject("Test Document");
		testDoc2= cmisObjectSelector.loadCMISObject("Test Document 2");
		policy = cmisObjectSelector.loadCMISObject("testpolicy-cmisid");
		policyTmp = cmisObjectSelector.loadCMISObject(policyTmp.getCmisObjectId());
		
		//verify 
		assertEquals(1, policy.getObjectsUnderPolicy().size());
		assertEquals(2, policyTmp.getObjectsUnderPolicy().size());
		assertEquals(2, testDoc.getPolicies().size());
		assertEquals(1, testDoc2.getPolicies().size());
		assertEquals(2, cmisPolicyService.getAppliedPolicies(TEST_REPO_2, testDoc.getCmisObjectId(), "", null).size());
		assertEquals(1, cmisPolicyService.getAppliedPolicies(TEST_REPO_2, testDoc2.getCmisObjectId(), "", null).size());
	}
}
