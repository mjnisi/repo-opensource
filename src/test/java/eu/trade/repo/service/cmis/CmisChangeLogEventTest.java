package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;

/**
 * 
 * @author azaridi
 *
 */
public class CmisChangeLogEventTest extends BaseTestClass {
	@Override
	@Before
	public void initUser() {
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_2);
	}
	
	@Test 
	public void testGetContentChanges() throws Exception {
		/*
		<change_event ID="1" CMIS_OBJECT_ID="Test Subfolder 1" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-1" CHANGE_TYPE="created" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:11:04.736"/>
		<change_event ID="2" CMIS_OBJECT_ID="Test Subfolder 2" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-2" CHANGE_TYPE="deleted" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:12:04.736"/>
		<change_event ID="3" CMIS_OBJECT_ID="Test Document 3" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-3" CHANGE_TYPE="updated" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:13:04.736"/>
		<change_event ID="4" CMIS_OBJECT_ID="Test Document 2" USERNAME="admin" CHANGE_LOG_TOKEN="test-change-log-token-4" CHANGE_TYPE="security" REPOSITORY_ID="100" CHANGE_TIME="2013-10-18 16:14:04.736"/>
		 */
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		Holder<String> token = new Holder<String>("test-change-log-token-1");
		ObjectList changes = cmisDiscoveryService.getContentChanges(TEST_REPO_2,
				token, 
				null, 
				"", 
				false, 
				false, 
				BigInteger.ZERO, 
				null);
		
		
		for (ObjectData datum : changes.getObjects()) {
			ChangeEventInfo info = datum.getChangeEventInfo();
			List<PropertyData<?>> data = datum.getProperties().getPropertyList();
			assertEquals(1, data.size()); //fulfilling the apache chemistry client req. .. sungle property ObjectID
			if (datum.getId().equals("Test Subfolder 1")) {
				assertEquals(ChangeType.CREATED, info.getChangeType());
			} else
			if (datum.getId().equals("Test Subfolder 2")) {
				assertEquals(ChangeType.DELETED, info.getChangeType());
			} else
			if (datum.getId().equals("Test Document 3")) {
				assertEquals(ChangeType.UPDATED, info.getChangeType());
			} else
			if (datum.getId().equals("Test Document 2")) {
				assertEquals(ChangeType.SECURITY, info.getChangeType());
			} else {
				assertTrue(false);
			}
			
			for (PropertyData<?> pd : data) {
				if (pd.getId().equals(PropertyIds.OBJECT_ID)) {
					assertEquals(datum.getId(), pd.getFirstValue());
				}
			}
		}
		
		//ONLY CHANGES 3 and 4
		token = new Holder<String>("test-change-log-token-3");
		changes = cmisDiscoveryService.getContentChanges(TEST_REPO_2,
				token, 
				null, 
				"", 
				false, 
				false, 
				BigInteger.TEN, 
				null);

		assertEquals(1, changes.getObjects().size());
		
		//ONLY CHANGE 4
		token = new Holder<String>("test-change-log-token-4");
		changes = cmisDiscoveryService.getContentChanges(TEST_REPO_2,
				token, 
				null, 
				"", 
				false, 
				false, 
				BigInteger.TEN, 
				null);
		assertEquals(0, changes.getObjects().size());
	}
}
