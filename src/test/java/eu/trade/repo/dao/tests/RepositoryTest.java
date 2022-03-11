package eu.trade.repo.dao.tests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.SecurityType;

public class RepositoryTest extends BaseTestClass {
	private final String REPO_CMISID = "cmis:repo01";

	@Autowired
	private AbstractPlatformTransactionManager transactionManager;

	
	@Before
	public void setScenario() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	}

	@Test
	public void testCreate() throws Exception {
		assertEquals(1, repoSelector.getAllRepositories().size());
		Repository repo = new Repository();
		repo.setName("demo repo");
		repo.setDescription("demo description");
		repo.setGetDescendants(false);
		repo.setGetFolderTree(false);
		repo.setAcl(CapabilityAcl.DISCOVER);
		repo.setAllVersionsSearchable(false);
		repo.setChanges(CapabilityChanges.NONE);
		repo.setJoin(CapabilityJoin.NONE);
		repo.setVersionSpecificFiling(false);
		repo.setUnfiling(false);
		repo.setRenditions(CapabilityRenditions.READ);
		repo.setQuery(CapabilityQuery.NONE);
		repo.setContentStreamUpdatability(CapabilityContentStreamUpdates.NONE);
		repo.setGetDescendants(false);
		repo.setPwcUpdatable(false);
		repo.setPwcSearchable(false);
		repo.setMultifiling(false);
		repo.setAclPropagation(AclPropagation.PROPAGATE);
		repo.setCmisId(REPO_CMISID);
		// Not default security model
		repo.setSecurityType(SecurityType.MULTIPLE);
		repo.setAuthenticationHandler(REPO_CMISID);
		repo.setAuthorisationHandler(REPO_CMISID);

		utilService.persist(repo);
		assertEquals(2, repoSelector.getAllRepositories().size());
		compareTable(
				"repository",
				"cmis_id = '"+REPO_CMISID+"'",
				"repository-test.xml");
	}

	@Test
	public void testRead() throws Exception {
		Repository repo = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		assertEquals(100, repo.getId().intValue());
		assertEquals(CapabilityAcl.NONE, repo.getAcl());
		assertNull(repo.getAclPropagation()); // acl is none
		assertEquals(CapabilityChanges.OBJECTIDSONLY, repo.getChanges());
		assertEquals("test_repo_02", repo.getCmisId());
		assertEquals(CapabilityContentStreamUpdates.NONE, repo.getContentStreamUpdatability());
		assertEquals("Test repository 01", repo.getDescription());
		assertEquals(CapabilityJoin.NONE, repo.getJoin());
		assertEquals("test_repo_02", repo.getName());
		assertEquals(CapabilityRenditions.NONE, repo.getRenditions());
		assertEquals(CapabilityQuery.NONE, repo.getQuery());
		assertEquals(false, repo.getAllVersionsSearchable().booleanValue());
		assertEquals(false, repo.getGetDescendants().booleanValue());
		assertEquals(false, repo.getGetFolderTree().booleanValue());
		assertEquals(false, repo.getMultifiling().booleanValue());
		assertEquals(false, repo.getPwcSearchable().booleanValue());
		assertEquals(false, repo.getPwcUpdatable().booleanValue());
		assertEquals(false, repo.getUnfiling().booleanValue());
		assertEquals(false, repo.getVersionSpecificFiling().booleanValue());

	}

	@Test
	public void testUpdate() throws Exception {
		Repository repo = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repo.setName("demo repo");
		repo.setDescription("demo description");
		repo.setGetDescendants(true);
		repo.setGetFolderTree(true);
		repo.setAcl(CapabilityAcl.DISCOVER);
		repo.setAllVersionsSearchable(true);
		repo.setChanges(CapabilityChanges.NONE);
		repo.setJoin(CapabilityJoin.NONE);
		repo.setVersionSpecificFiling(true);
		repo.setUnfiling(true);
		repo.setRenditions(CapabilityRenditions.READ);
		repo.setQuery(CapabilityQuery.NONE);
		repo.setContentStreamUpdatability(CapabilityContentStreamUpdates.NONE);
		repo.setGetDescendants(true);
		repo.setPwcUpdatable(true);
		repo.setPwcSearchable(true);
		repo.setMultifiling(true);
		repo.setAclPropagation(AclPropagation.PROPAGATE);

		utilService.merge(repo);
		repo = repoSelector.getRepository(TestConstants.TEST_REPO_2);

		assertEquals(100, repo.getId().intValue());
		assertEquals(CapabilityAcl.DISCOVER, repo.getAcl());
		assertEquals(AclPropagation.PROPAGATE, repo.getAclPropagation()); // acl is none
		assertEquals(CapabilityChanges.NONE, repo.getChanges());
		assertEquals(TestConstants.TEST_REPO_2, repo.getCmisId());
		assertEquals(CapabilityContentStreamUpdates.NONE, repo.getContentStreamUpdatability());
		assertEquals("demo description", repo.getDescription());
		assertEquals(CapabilityJoin.NONE, repo.getJoin());
		assertEquals("demo repo", repo.getName());
		assertEquals(CapabilityRenditions.READ, repo.getRenditions());
		assertEquals(CapabilityQuery.NONE, repo.getQuery());
		assertEquals(true, repo.getAllVersionsSearchable().booleanValue());
		assertEquals(true, repo.getGetDescendants().booleanValue());
		assertEquals(true, repo.getGetFolderTree().booleanValue());
		assertEquals(true, repo.getMultifiling().booleanValue());
		assertEquals(true, repo.getPwcSearchable().booleanValue());
		assertEquals(true, repo.getPwcUpdatable().booleanValue());
		assertEquals(true, repo.getUnfiling().booleanValue());
		assertEquals(true, repo.getVersionSpecificFiling().booleanValue());
	}

	@Test
	public void testDelete () throws Exception {
		assertEquals(1, repoSelector.getAllRepositories().size());
		utilService.removeDetached(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		assertEquals(0, repoSelector.getAllRepositories().size());
	}
	
	
	@Test
	public void testEnabledPoliciesAddRemove() throws Exception {
		
		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

		Repository repo = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		assertEquals(0, repo.getEnabledPolicies().size());
		
		ObjectType policyType = objTypeSelector.getObjectTypeByCmisId(
				TestConstants.TEST_REPO_2, BaseTypeId.CMIS_POLICY.value());
		
		repo.addEnabledPolicy(policyType);
		utilService.merge(repo);
		
		transactionManager.commit(txStatus);
		
		compareTable("repository_policy", "repository-test.xml");
		
		//removal
		repo.removeEnabledPolicy(policyType);
		utilService.merge(repo);
		
		compareTable("repository_policy", "repository-test-noPoliciesEnabled.xml");
		
	}
	
	

}
