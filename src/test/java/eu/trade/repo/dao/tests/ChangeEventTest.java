package eu.trade.repo.dao.tests;

import java.util.Date;

import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.ChangeEvent;

public class ChangeEventTest extends BaseTestClass {
	@Before
	public void setupTestEnvr() throws Exception {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	}

	@Test
	public void testCreate() throws Exception {
		ChangeEvent event = new ChangeEvent();
		event.setChangeLogToken("test-change-log-token");
		event.setChangeTime(new Date());
		event.setChangeType(ChangeType.CREATED);
		event.setObjectId("123");
		event.setRepository(repoSelector.getRepository(TestConstants.TEST_REPO_2));
		event.setUsername(TestConstants.ADMIN_USER);

		utilService.persist(event);

		compareTable(
				"change_event",
				"1 = 1",
				"changeEvent-test.xml");
	}
}
