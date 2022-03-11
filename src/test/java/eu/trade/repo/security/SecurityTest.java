package eu.trade.repo.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;

public class SecurityTest extends BaseTestClass {

	@Autowired
	private Security security;

	@Before
	public void setScenario() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	}
	
	@Test
	public void testGetPermissionMappings() {
		// TODO: validate cache in some way
		List<PermissionMapping> permissionMappings = security.getPermissionMappings(TestConstants.TEST_REPO_2);
		Map<String, String> dbKeys = ActionMap.getDbKeys();
		assertNotNull(permissionMappings);
		assertNotNull(dbKeys);
		assertEquals(dbKeys.size(), permissionMappings.size());
		for (PermissionMapping permissionMapping : permissionMappings) {
			String key = permissionMapping.getKey();
			if (!dbKeys.containsKey(key)) {
				log();
			}
			assertTrue("This key is not present: " + key, dbKeys.containsKey(key));
		}
	}
	
	private void log() {
		List<PermissionMapping> permissionMappings = security.getPermissionMappings(TestConstants.TEST_REPO_2);
		for (PermissionMapping permissionMapping : permissionMappings) {
			System.out.println("PermissionMapping key: " + permissionMapping.getKey());
		}
		System.out.println("Total: " + permissionMappings.size());
		for(eu.trade.repo.model.PermissionMapping permissionMapping : permMappingSelector.getRepositoryPermissionMappingsWithPermission(TestConstants.TEST_REPO_2)) {
			System.out.println(toString(permissionMapping));
		}
	}
	
	private String toString(eu.trade.repo.model.PermissionMapping permissionMapping) {
		return new StringBuffer("[id: ")
				.append(permissionMapping.getId())
				.append(", key: ")
				.append(permissionMapping.getKey())
				.append(", repo id: ")
				.append(permissionMapping.getRepository().getCmisId())
				.append(", perm id: ")
				.append(permissionMapping.getPermission().getId())
				.append(", perm name: ")
				.append(permissionMapping.getPermission().getName())
				.append("]")
				.toString(); 
	}
}
