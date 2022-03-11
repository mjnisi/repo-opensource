package eu.trade.repo.selectors;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.security.Security;

public class ChangeLogSelectorTest extends BaseTestClass {
	@Autowired
	private Security security;

	@Test
	public void testReadLatest() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenario03.xml");

		//latest CHANGE_LOG_TOKEN="test-change-log-token-4"
		assertEquals(changeEventSelector.getLatestChangeLogToken(TEST_REPO_2_ID), "test-change-log-token-4");
	}

	@Test
	public void testReadLatest_NonExistantRepo() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenario03.xml");
		//non-existing repository
		changeEventSelector.getLatestChangeLogToken(999999999);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void testReadCompleteLog() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenario03.xml");

		//latest CHANGE_LOG_TOKEN="test-change-log-token-4"
		List<ChangeEvent> changeLog = changeEventSelector.getChangeEvents("test-change-log-token-4", TEST_REPO_2_ID, BigInteger.TEN);
		assertEquals(1, changeLog.size(), 1);

		changeLog = changeEventSelector.getChangeEvents("test-change-log-token-3", TEST_REPO_2_ID, BigInteger.TEN);
		assertEquals(2, changeLog.size(), 2);

		changeLog = changeEventSelector.getChangeEvents("test-change-log-token-2", TEST_REPO_2_ID, BigInteger.TEN);
		assertEquals(3, changeLog.size(), 3);

		changeLog = changeEventSelector.getChangeEvents("test-change-log-token-1", TEST_REPO_2_ID, BigInteger.TEN);
		assertEquals(4, changeLog.size(), 4);

		//will throw exception
		changeLog = changeEventSelector.getChangeEvents("asd", TEST_REPO_2_ID, BigInteger.TEN);
	}

	@Test
	public void testReadChangeEvent() throws Exception {
		setScenario(DatabaseOperation.CLEAN_INSERT, "scenario03.xml");
		assertNotNull(changeEventSelector.getChangeLogEvent("test-change-log-token-1"));
		assertNotNull(changeEventSelector.getChangeLogEvent("test-change-log-token-2"));
		assertNotNull(changeEventSelector.getChangeLogEvent("test-change-log-token-3"));
		assertNotNull(changeEventSelector.getChangeLogEvent("test-change-log-token-4"));
	}
}
