package eu.trade.repo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.trade.repo.TestConstants;

public class RepositoryTest {

	@Test(expected=IllegalStateException.class)
	public void testAddSecurityHandler_fail() {
		Repository repository = new Repository();
		repository.setSecurityType(SecurityType.SIMPLE);
		repository.addSecurityHandler(new SecurityHandler());
	}

	@Test
	public void testAddRemoveSecurityHandler_ok() {
		Repository repository = new Repository();
		repository.setCmisId(TestConstants.TEST_REPO_1);
		repository.setSecurityType(SecurityType.MULTIPLE);
		SecurityHandler securityHandler = new SecurityHandler();
		securityHandler = repository.addSecurityHandler(securityHandler);
		assertEquals(1, repository.getSecurityHandlers().size());
		assertEquals(repository, securityHandler.getRepository());
		repository.removeSecurityHandler(securityHandler);
		assertEquals(0, repository.getSecurityHandlers().size());
	}
	
	@Test
	public void testAddEnabledPolicy() {
		
		Repository repository = new Repository();
		repository.setCmisId("myRepo");
		
		ObjectType policyType = new ObjectType();
		policyType.setBase(new ObjectType(BaseTypeId.CMIS_POLICY.value()));
		policyType.setRepository(repository);
		policyType.setCmisId("policy:policyA");
		
		assertEquals(0, repository.getEnabledPolicies().size());
		repository.addEnabledPolicy(policyType);
		assertEquals(1, repository.getEnabledPolicies().size());
		
		ObjectType documentType = new ObjectType();
		documentType.setBase(new ObjectType(BaseTypeId.CMIS_DOCUMENT.value()));
		documentType.setRepository(repository);
		documentType.setCmisId("wrong:document");
		try {
			repository.addEnabledPolicy(documentType);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(e.getMessage(), "Only policy types allowed.");
		}
		assertEquals(1, repository.getEnabledPolicies().size());
		
		//remove a type that is not there
		repository.removeEnabledPolicy(documentType);
		assertEquals(1, repository.getEnabledPolicies().size());
		
		//remove the correct policy 
		repository.removeEnabledPolicy(policyType);
		assertEquals(0, repository.getEnabledPolicies().size());
		
	}
}
