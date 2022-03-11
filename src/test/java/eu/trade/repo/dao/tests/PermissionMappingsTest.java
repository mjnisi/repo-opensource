package eu.trade.repo.dao.tests;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.PermissionMapping;

public class PermissionMappingsTest extends BaseTestClass {
	@Test
	public void testCreate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		PermissionMapping pm = new PermissionMapping();
		Permission p = permSelector.getPermission("cmis:read", "test_repo_02");
		pm.setPermission(p);
		pm.setKey("key:test");
		pm.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		
		utilService.merge(pm);
		
		compareTable(
				"PERMISSION_MAPPING",
				"KEY = 'key:test'", 
				"permissionmap-test.xml");
	}

	@Test
	public void testRead() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		List<PermissionMapping> perms = permMappingSelector.getPermissionMappings("asd");
		assertEquals(0, perms.size());
		
		perms = permMappingSelector.getPermissionMappings("cmis:read");
		assertEquals(24, perms.size());
		
		perms = permMappingSelector.getPermissionMappings("cmis:write");
		assertEquals(10, perms.size());

		perms = permMappingSelector.getPermissionMappings("cmis:all");
		assertEquals(0, perms.size());

		perms = permMappingSelector.getREADPermissionMappings();
		assertEquals(24, perms.size());
		
		perms = permMappingSelector.getWRITEPermissionMappings();
		assertEquals(10, perms.size());

		perms = permMappingSelector.getALLPermissionMappings();
		assertEquals(0, perms.size());
	}

	@Test
	public void testUpdate() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		
		List<PermissionMapping> maps = permMappingSelector.loadRepositoryPermissionMappings(TestConstants.TEST_REPO_2);
		for (PermissionMapping pm : maps) {
			if (pm.getId() == 134) {
				assertEquals("canApplyACL.Object", pm.getKey());
				pm.setKey("...");
				utilService.merge(pm);
			}
		}

		maps = permMappingSelector.loadRepositoryPermissionMappings(TestConstants.TEST_REPO_2);
		for (PermissionMapping pm : maps) {
			if (pm.getId() == 134) {
				assertEquals("...", pm.getKey());
			}
		}
	}
	
	@Test
	public void testDelete () throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
		
		List<PermissionMapping> maps = permMappingSelector.loadRepositoryPermissionMappings(TestConstants.TEST_REPO_2);
		assertEquals(35, maps.size());
		for (PermissionMapping pm : maps) {
			if (pm.getId() == 134) {
				utilService.removeDetached(pm);
			}
		}
		maps = permMappingSelector.loadRepositoryPermissionMappings(TestConstants.TEST_REPO_2);
		assertEquals(34, maps.size());
	}
}
