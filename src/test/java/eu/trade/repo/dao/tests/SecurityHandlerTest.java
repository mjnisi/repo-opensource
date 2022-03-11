package eu.trade.repo.dao.tests;

import static org.junit.Assert.*;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.HandlerType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.SecurityHandler;
import eu.trade.repo.model.SecurityType;
import eu.trade.repo.selectors.RepositoryTestSelector;

public class SecurityHandlerTest extends BaseTestClass {

	private static final String NAME = "name";
	private static final String NAME2 = "name2";

	@Autowired
	private RepositoryTestSelector repositoryTestSelector;

	@Before
	public void setUp() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	}

	@Test
	public void testCreate() throws Exception {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		assertEquals(0, repo.getSecurityHandlers().size());
		assertEquals(SecurityType.SIMPLE, repo.getSecurityType());

		addHandler(repo);
		validate(HandlerType.AUTHENTICATION, NAME);
	}

	@Test
	public void testUpdate() throws Exception {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		addHandler(repo);
		repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);

		for (SecurityHandler handler : repo.getSecurityHandlers()) {
			handler.setHandlerType(HandlerType.AUTHORISATION);
			handler.setHandlerName(NAME2);
		}

		utilService.merge(repo);
		validate(HandlerType.AUTHORISATION, NAME2);
	}

	@Test(expected=org.springframework.dao.DataIntegrityViolationException.class)
	public void testAddTwice_fail() throws Exception {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		addHandler(repo);
		repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		// addHandler(repo) doesn't fails because the new one replace the other in the set, ending with only one handler in the set before merge.
		SecurityHandler handler = getHandler();
		handler.setRepository(repo);
		utilService.persist(handler);
	}

	@Test
	public void testDeleteRepository() throws Exception {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		addHandler(repo);
		repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		utilService.removeDetached(repo);
		compareTable("repository", "securityHandler-test-delete.xml");
		compareTable("security_handler", "securityHandler-test-delete.xml");
	}

	@Test
	public void testDeleteRemoved() throws Exception {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		SecurityHandler securityHandler = addHandler(repo);
		repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		repo.removeSecurityHandler(securityHandler);
		assertEquals(0, repo.getSecurityHandlers().size());
		utilService.merge(repo);
		compareTable("repository", "securityHandler-test-remove.xml");
		compareTable("security_handler", "securityHandler-test-remove.xml");
	}

	private SecurityHandler addHandler(Repository repo) {
		repo.setSecurityType(SecurityType.MULTIPLE);
		SecurityHandler securityHandler = getHandler();
		repo.addSecurityHandler(securityHandler);

		utilService.merge(repo);
		return securityHandler;
	}

	private void validate(HandlerType handlerType, String name) {
		Repository repo = repositoryTestSelector.loadRepositoryWithSecurityHandlers(TestConstants.TEST_REPO_2);
		assertEquals(SecurityType.MULTIPLE, repo.getSecurityType());
		assertEquals(1, repo.getSecurityHandlers().size());

		for (SecurityHandler handler : repo.getSecurityHandlers()) {
			assertEquals(handlerType, handler.getHandlerType());
			assertEquals(name, handler.getHandlerName());
		}
	}

	private SecurityHandler getHandler() {
		SecurityHandler securityHandler = new SecurityHandler();
		securityHandler.setHandlerType(HandlerType.AUTHENTICATION);
		securityHandler.setHandlerName(NAME);
		return securityHandler;
	}
}
