package eu.trade.repo;

import static org.junit.Assert.*;

import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class RepoServiceFactoryTest extends BaseTestClass {

	@Autowired
	private ObjectFactory<CmisService> cmisServiceBeanFactory;

	@Test
	public void testName() throws Exception {
		RepoService repoService1 = getRepoService();
		RepoService repoService2 = getRepoService();
		// Test null or different instances.
		assertTrue(repoService1 == null || repoService1 != repoService2);
	}

	private RepoService getRepoService() {
		CmisService cmisService = cmisServiceBeanFactory.getObject();
		if (cmisService instanceof RepoService) {
			return (RepoService) cmisService;
		}
		return null;
	}
}
