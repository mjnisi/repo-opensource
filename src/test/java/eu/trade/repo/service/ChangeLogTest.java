package eu.trade.repo.service;

import static eu.trade.repo.TestConstants.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.util.Constants;

public class ChangeLogTest extends BaseTestClass {
	@Test
	public void testGetContentChanges() throws Exception{
		/*
		<change_event ID="1" CMIS_OBJECT_ID="106" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-1" CHANGE_TYPE="created" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:11:04.736"/>
		<change_event ID="2" CMIS_OBJECT_ID="107" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-2" CHANGE_TYPE="deleted" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:12:04.736"/>
		<change_event ID="3" CMIS_OBJECT_ID="108" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-3" CHANGE_TYPE="updated" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:13:04.736"/>
		<change_event ID="4" CMIS_OBJECT_ID="109" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-4" CHANGE_TYPE="security" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:14:04.736"/>
		 */

		setScenario("scenarioChangeEventLogCaps.xml", DatabaseOperation.CLEAN_INSERT);
		Holder<String> changeLog = new Holder<String>("test-change-log-token-1");
		List<ChangeEvent> changes = discoveryService.getContentChanges(TEST_REPO_5, changeLog, BigInteger.TEN);
		assertEquals(0, changes.size());

		changeLog = new Holder<String>("test-change-log-token-2");
		changes = discoveryService.getContentChanges(TEST_REPO_5, changeLog, BigInteger.TEN);
		assertEquals(0, changes.size());

		changeLog = new Holder<String>("test-change-log-token-3");
		changes = discoveryService.getContentChanges(TEST_REPO_5, changeLog, BigInteger.TEN);
		assertEquals(0, changes.size());

		changeLog = new Holder<String>("test-change-log-token-4");
		changes = discoveryService.getContentChanges(TEST_REPO_5, changeLog, BigInteger.TEN);
		assertEquals(0, changes.size());
	}

	@Test
	public void testWriteEventLog() throws Exception {
		String[] checkpointTokens = new String[4];

		setUser(TEST_USER, TEST_PWD, TEST_REPO_5);
		setScenario("scenarioChangeEventLog.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();

		//CHECKPOINT0 at THIS POINT
		checkpointTokens[0] = changeEventSelector.getLatestChangeLogToken(TEST_REPO_5_ID);

		//CREATE
		CMISObject rootFolder = cmisObjectSelector.loadCMISObject("/");
		assertNotNull(rootFolder);
		CMISObject folder = new CMISObject();
		folder.setObjectType(objTypeSelector.getObjectTypeByCmisId(TEST_REPO_5, "test:folder"));
		folder.addProperty(getTestProperty("MyFolder", TEST_REPO_5, Constants.TYPE_CMIS_FOLDER, PropertyIds.NAME));
		folder.addProperty(getTestProperty("test:folder", TEST_REPO_5, Constants.TYPE_CMIS_FOLDER, PropertyIds.OBJECT_TYPE_ID));
		folder.addParent(rootFolder);
		objectService.createObject(TEST_REPO_5, folder, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_FOLDER);

		//CHECKPOINT1 at THIS POINT
		checkpointTokens[1] = changeEventSelector.getLatestChangeLogToken(TEST_REPO_5_ID);
		List<ChangeEvent> evLog = changeEventSelector.getChangeEvents(checkpointTokens[0], TEST_REPO_5_ID, BigInteger.TEN);
		assertEquals(2, evLog.size());

		//DELETE
		CMISObject object = cmisObjectSelector.getCMISObject(TEST_REPO_5, "Test Document 3");
		assertNotNull(object);
		objectService.deleteObject(TEST_REPO_5, object.getCmisObjectId(), true);

		//ASSERT
		evLog = changeEventSelector.getChangeEvents(checkpointTokens[1], TEST_REPO_5_ID, BigInteger.TEN);
		assertEquals(2, evLog.size());

		//CHECKPOINT2
		checkpointTokens[2] = changeEventSelector.getLatestChangeLogToken(TEST_REPO_5_ID);

		//UPDATE
		object = cmisObjectSelector.getCMISObject(TEST_REPO_5, "Test Document");
		assertNotNull(object);
		Properties ps = new PropertiesImpl();
		// Note: Currently the update properties method updated (last modified by, last updated date, etc.) with an empty property set.
		objectService.updateProperties(TEST_REPO_5, object.getCmisObjectId(), ps);

		//ASSERT
		evLog = changeEventSelector.getChangeEvents(checkpointTokens[2], TEST_REPO_5_ID, BigInteger.TEN);
		assertEquals(1, evLog.size());

		//CHECKPOINT3
		checkpointTokens[3] = changeEventSelector.getLatestChangeLogToken(TEST_REPO_5_ID);

		//VERIFY CHECKPOINTS
		for (int i = 0; i < checkpointTokens.length; i++) {
			List<ChangeEvent> evts = changeEventSelector.getChangeEvents(checkpointTokens[i], TEST_REPO_5_ID, BigInteger.TEN);
			if (i == 0) {//CHKPNT0 - all changes
				assertEquals(5, evts.size());
			}

			if (i == 1) {//CHKPNT1 - after create
				assertEquals(3, evts.size());
			}

			if (i == 2) {//CHKPNT2 - after delete
				assertEquals(1, evts.size());
			}

			if (i == 3) {//CHKPNT2 - after update
				assertEquals(0, evts.size());
			}

		}
	}

	@Test
	public void testWriteMoveEventLog() throws Exception {


		setUser(TEST_USER, TEST_PWD, TEST_REPO_5);
		setScenario("scenarioChangeEventLog.xml", DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();

		//CHECKPOINT0 at THIS POINT
		String checkpointToken = changeEventSelector.getLatestChangeLogToken(TEST_REPO_5_ID);

		//MOVE
		CMISObject object = cmisObjectSelector.getCMISObject(TEST_REPO_5, "Test Document");
		CMISObject target = cmisObjectSelector.getCMISObject(TEST_REPO_5, "Test Subfolder");
		CMISObject source= cmisObjectSelector.loadCMISObject("Test Folder");
		assertNotNull(object);
		assertNotNull(target);
		assertNotNull(source);
		objectService.moveObject(TEST_REPO_5, object.getCmisObjectId(), source.getCmisObjectId(), target.getCmisObjectId());

		//ASSERT
		//CHECKPOINT1 at THIS POINT
		List<ChangeEvent> evLog = changeEventSelector.getChangeEvents(checkpointToken, TEST_REPO_5_ID, BigInteger.TEN);
		assertEquals(2, evLog.size());

		for (ChangeEvent changeEvent : evLog) {
			assertEquals(ChangeType.UPDATED, changeEvent.getChangeType());
			assertEquals("test", changeEvent.getUsername());
			assertEquals(TEST_REPO_5, changeEvent.getRepository().getCmisId());
			assertTrue(changeEvent.getObjectId().equals(source.getCmisObjectId())
					|| changeEvent.getObjectId().equals(target.getCmisObjectId()));
		}
	}
}
