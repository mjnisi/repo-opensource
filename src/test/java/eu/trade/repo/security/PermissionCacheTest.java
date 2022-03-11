package eu.trade.repo.security;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.util.Constants;

public class PermissionCacheTest extends BaseTestClass {

	@Autowired
	private PermissionCache permissionCache;

	@Autowired
	private PermissionMappingCache permissionMappingCache;

	private static final String[] EXPANDED_READ = {Constants.CMIS_READ, Constants.CMIS_WRITE, Constants.CMIS_ALL};
	private static final String[] EXPANDED_WRITE = {Constants.CMIS_WRITE, Constants.CMIS_ALL};
	private static final String[] EXPANDED_ALL = {Constants.CMIS_ALL};

	@Test
	public void testGetPermissionDefinitions() {
		List<PermissionDefinition> permissionMappings = permissionCache.getPermissionDefinitions(TestConstants.TEST_REPO_1);
		for (PermissionDefinition permissionDefinition : permissionMappings) {
			String permissionName = permissionDefinition.getId();
			switch (permissionName) {
				case Constants.CMIS_READ :
					checkExpandedPermissions(permissionName, EXPANDED_READ);
					break;
				case Constants.CMIS_WRITE :
					checkExpandedPermissions(permissionName, EXPANDED_WRITE);
					break;
				case Constants.CMIS_ALL :
					checkExpandedPermissions(permissionName, EXPANDED_ALL);
					break;
				default :
					fail("Not recongised permission: " + permissionName);
					break;
			}
		}
	}

	private void checkExpandedPermissions(String permissionName, String[] expectedPermissions) {
		List<PermissionMapping> permissionMappings = permissionMappingCache.getPermissionMappings(TestConstants.TEST_REPO_1);
		for (PermissionMapping permissionMapping : permissionMappings) {
			List<String> permissions = permissionMapping.getPermissions();
			if (permissions.contains(permissionName)) {
				for (String expected : expectedPermissions) {
					assertTrue("The expanded permissions doesn't contains the expected permission: " + expected, permissions.contains(expected));
				}
			}
		}
	}
}
