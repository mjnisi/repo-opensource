package eu.trade.repo.service;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.service.util.Page;
import eu.trade.repo.service.util.Tree;

public class NavigationServiceTest extends BaseTestClass {
	@Autowired
	private AbstractPlatformTransactionManager transactionManager;

	
	@Before
	public void setScenario() throws Exception {
		setScenario("scenario02.xml", DatabaseOperation.CLEAN_INSERT);
	}

	@Test(expected = CmisNotSupportedException.class)
	public void testFolderTreeCapabilityNotSupported() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		navigationService.getTree(TestConstants.TEST_REPO_2, "internalFolder1", null, false, false, true);
	}

	@Test(expected = CmisNotSupportedException.class)
	public void testGetDescendantsCapabilityNotSupported() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		navigationService.getTree(TestConstants.TEST_REPO_2, "internalFolder1", null, false, true, true);
	}

	@Test
	public void testGetChildrenCount() throws Exception {
		assertEquals(1, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolder1", null, -1, 0).getCount());
		assertEquals(3, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolderA", null, -1, 0).getCount());
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(3, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolder1", null, -1, 0).getCount());
		assertEquals(3, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolderA", null, -1, 0).getCount());
		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		// Now the pwcSearchable has no impact in the results
		assertEquals(3, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolder1", null, -1, 0).getCount());
		assertEquals(3, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolderA", null, -1, 0).getCount());
	}

	private String createNamesString(Set<CMISObject> objs, String propName) throws PropertyNotFoundException {
		StringBuffer sb = new StringBuffer();
		for (CMISObject obj : objs) {
			sb.append(obj.getProperty(PropertyIds.NAME).getTypedValue()+",");
		}
		if (sb.length() > 0) {
			return sb.substring(0, sb.length()-1);
		}
		return sb.toString();
	}
	
	@Test
	@Transactional
	public void testGetChildrenOrderedWithSecurity() throws Exception {
		setScenario("scenario03OrderChildren.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		
		Page<CMISObject> page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name DESC", -1, 1);
		assertEquals("b,a,D,C,B,A", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(6, page.getPageElements().size());

		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name DESC", 10, 0);
		assertEquals("b,a,D,C,B,A", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(6, page.getPageElements().size());
		
		//ASCENDING
		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name ASC", -1, 0);
		assertEquals("A,B,C,D,a,b", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(6, page.getPageElements().size());
	}
	
	@Test
	@Transactional
	public void testGetChildrenOrderedByName() throws Exception {
		setScenario("scenario03OrderChildren.xml", DatabaseOperation.CLEAN_INSERT);
		//TEST WITH NO PRINCIPALIDS
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		//DESCENDING
		Page<CMISObject> page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name DESC", 10, 0);
		assertEquals("b,a,D,C,B,A", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(6, page.getPageElements().size());
		
		//DESCENDING PAGE
		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name desc", 3, 0);
		assertEquals("b,a,D", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(3, page.getPageElements().size());
		
		//ASCENDING
		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name asc", -1, 0);
		assertEquals("A,B,C,D,a,b", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(6, page.getPageElements().size());
		
		//ASCENDING PAGE
		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:name ASC", 3, 0);
		assertEquals("A,B,C", createNamesString(page.getPageElements(), PropertyIds.NAME));
		assertEquals(3, page.getPageElements().size());
	}
	
	
	@Test
	@Transactional
	public void testGetChildrenOrderedByCreationDate() throws Exception {
		setScenario("scenario03OrderChildren.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		//FETCH SOME, NO PRINCIPAL
		Page<CMISObject> page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:creationDate ASC", -1, 0);
		Iterator<CMISObject> it = page.getPageElements().iterator();
		GregorianCalendar first = it.next().getProperty(PropertyIds.CREATION_DATE).getTypedValue();
		while (it.hasNext()) {
			GregorianCalendar next = it.next().getProperty(PropertyIds.CREATION_DATE).getTypedValue();
			assertTrue(next.getTimeInMillis() > first.getTimeInMillis());
			first = next;
		}
		
		page = navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", "cmis:creationDate DESC", -1, 0);
		it = page.getPageElements().iterator();
		first = it.next().getProperty(PropertyIds.CREATION_DATE).getTypedValue();
		while (it.hasNext()) {
			GregorianCalendar next = it.next().getProperty(PropertyIds.CREATION_DATE).getTypedValue();
			assertTrue(next.getTimeInMillis() < first.getTimeInMillis());
			first = next;
		}
	}
	
	@Test
	public void testGetChildrenCount_2() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		assertEquals(7, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());
		assertEquals(1, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Subfolder 5", null, -1, 0).getCount());
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());
		assertEquals(1, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Subfolder 5", null, -1, 0).getCount());
		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());
		assertEquals(1, navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, -1, 0).getCount());
		assertEquals(0, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Subfolder 5", null, -1, 0).getCount());
	}

	@Test
	public void testGetChildrenCount_3() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		assertEquals(8, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());

		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(9, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());
		// Now the pwcSearchable has no impact in the results
		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		assertEquals(9, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.FALSE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getChildren(TestConstants.TEST_REPO_2, "Test Folder", null, -1, 0).getCount());
	}

	@Test
	public void testGetDescendants() throws Exception {
		setScenario("scenario03ACL.xml", DatabaseOperation.CLEAN_INSERT);
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setGetDescendants(Boolean.TRUE);
		repositoryService.update(repository);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		
		assertEquals(13, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(14, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		assertEquals(14, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());
	}

	@Test
	public void testGetDescendants_2() throws Exception {
		setScenario("scenario03ACL.xml", DatabaseOperation.CLEAN_INSERT);
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setGetDescendants(Boolean.TRUE);
		repositoryService.update(repository);

		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		assertEquals(14, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(15, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());
		// Now the pwcSearchable has no impact in the results
		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		assertEquals(15, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.FALSE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(14, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true).getDescendants().size());
	}

	@Test
	public void testGetTree() throws Exception {
		setScenario("scenario03ACL.xml", DatabaseOperation.CLEAN_INSERT);
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setGetFolderTree(Boolean.TRUE);
		repositoryService.update(repository);

		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		Holder<String> obejctId =  new Holder<String>("Test Document");
		Holder<Boolean> contentCopied =  new Holder<Boolean>();
		versioningService.checkOut(TestConstants.TEST_REPO_2, obejctId, contentCopied);

		assertEquals(8, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, true, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, true, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.TRUE);
		repository.setPwcSearchable(Boolean.FALSE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, true, true).getDescendants().size());

		repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setVersionSpecificFiling(Boolean.FALSE);
		repository.setPwcSearchable(Boolean.TRUE);
		repositoryService.update(repository);
		assertEquals(8, navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, true, true).getDescendants().size());
	}

	@Test
	public void testGetWithParents() throws Exception {
		setScenario("scenario03ACL.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		
		String docid = "Test major Version Document";
		
		CMISObject cmisObject = navigationService.getObjectWithParents(TestConstants.TEST_REPO_2, docid);
		assertEquals(3, cmisObject.getParents().size());

		TransactionStatus txStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		cmisObject = objectService.getObject(TestConstants.TEST_REPO_2, "Test Subfolder 12");
		Acl acl = new Acl();
		acl.setPrincipalId(TestConstants.TEST_USER);
		acl.setPermission(permSelector.getPermission("cmis:all", TestConstants.TEST_REPO_2));
		cmisObject.addAcl(acl);

		transactionManager.commit(txStatus);
		
		cmisObject = navigationService.getObjectWithParents(TestConstants.TEST_REPO_2, docid);
		assertEquals(4, cmisObject.getParents().size());
	}

	@Test
	public void testGetParents() throws Exception {
		setScenario("scenarioGetParents.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
		
		Repository repository = repoSelector.getRepository(TestConstants.TEST_REPO_2);
		repository.setGetFolderTree(Boolean.TRUE);
		repositoryService.update(repository);

		Tree t = navigationService.getTree(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, false, false, true);
		
		assertTrue(t.getDescendants().size() == 2);
	}

	@Test
	public void testGetChildrenOfRoot() throws Exception {
		Set<CMISObject> children = navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, Integer.MAX_VALUE, 0).getPageElements();
		assertEquals(3, children.size());
	}

	@Test
	public void testGetChildrenOfRoot_withMaxItems() throws Exception {
		Set<CMISObject> children = navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, 2, 0).getPageElements();
		assertEquals(2, children.size());
	}

	@Test
	public void testGetChildrenOfRoot_withMaxItemsAndOffset() throws Exception {
		Set<CMISObject> children = navigationService.getChildren(TestConstants.TEST_REPO_2, getRootFolderId(TestConstants.TEST_REPO_2), null, 2, 2).getPageElements();
		assertEquals(1, children.size());
	}

	@Test(expected = CmisInvalidArgumentException.class)
	public void testFolderParents_invalidArgument() throws Exception {
		navigationService.getFolderParent(TestConstants.TEST_REPO_2, "internalDocA");
	}

	@Test
	public void testFolderParents() throws Exception {
		CMISObject folderParent = navigationService.getFolderParent(TestConstants.TEST_REPO_2, "internalFolderA");
		assertEquals("internalFolder1", folderParent.getCmisObjectId());
	}

	@Test
	public void testEmptyFolderGetChilren() throws Exception {
		Page<CMISObject> children = navigationService.getChildren(TestConstants.TEST_REPO_2, "internalFolderA", null, 2, 2);
		assertTrue(children.getPageElements().isEmpty());
		assertEquals(0, children.getCount());
	}
}
