package eu.trade.repo.service;

import static eu.trade.repo.TestConstants.TEST_PWD;
import static eu.trade.repo.TestConstants.TEST_REPO_2;
import static eu.trade.repo.TestConstants.TEST_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.exception.PropertyNotFoundException;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.util.Constants;

/**
 * 
 * @author azaridi
 *
 */
@Transactional
public class VersioningServiceTest extends BaseTestClass {
	@Autowired
	private IDGenerator mockGenerator;

	@Before
	public void setScenario() throws Exception {
		setUser(TEST_USER, TEST_PWD, TEST_REPO_2);
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
	}

	@Test (expected=CmisInvalidArgumentException.class)
	public void cannotCheckInNonPWC() {
		CMISObject obj = objectService.getObject(TEST_REPO_2, "Test Document");
		Holder<String> objectId = new Holder<String>(obj.getCmisObjectId());
		
		versioningService.checkOut(TEST_REPO_2, objectId, new Holder<Boolean>(false));
		versioningService.checkIn(TEST_REPO_2, new Holder<String>(obj.getCmisObjectId()), true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
	}
	@Test
	public void testOnlyPWCUpdatable() throws Exception {
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		//check out the series - create a PWC
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);

		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//the PWC and the majorDoc
		assertEquals(2, versions.size());

		//try and update the non-pwc object, exception expected
		try {
			objectService.updateProperties(TEST_REPO_2, majorDoc.getCmisObjectId(), null);
		} catch (CmisInvalidArgumentException e) {
			assertTrue(e.getMessage().endsWith("is not the PWC of its series, cannot update properties "));
		}

		CMISObject pwc = objectService.getObject(TEST_REPO_2, objectId.getValue());
		//CMISObject pwc = versioningService.getObjectOfLatestVersion(TEST_REPO_2, objectId.getValue(), (String)majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue(), true);
		Properties ps = PropertiesBuilder.build(pwc, "cmis:name");

		//now update the PWC,  nothing is really updated, just need to check the call to update passes
		objectService.updateProperties(TEST_REPO_2, pwc.getCmisObjectId(), ps);
	}

	@Test
	public void testOnlyLatestUpdatable() throws Exception {
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		//check out the series - create a PWC
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		//check in another major version - 2.0
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//major versions 1.0 and 2.0
		assertEquals(2, versions.size());

		//try and update the non-latest object, exception expected
		try {
			objectService.updateProperties(TEST_REPO_2, majorDoc.getCmisObjectId(), null);
		} catch (CmisInvalidArgumentException e) {
			assertTrue(e.getMessage().endsWith("is not the latest in its series, cannot update properties"));
		}

		//now update the latest in the series,  nothing is really updated, just need to check the call to update passes
		CMISObject latest = versioningService.getObjectOfLatestVersion(TEST_REPO_2, objectId.getValue(), (String)majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue(), true);
		Properties ps = PropertiesBuilder.build(latest, "cmis:name");
		objectService.updateProperties(TEST_REPO_2, objectId.getValue(),ps );
	}

	@Test
	public void testVersioningFlags2() throws PropertyNotFoundException {
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = null;
		CMISObject minorDoc = createVersionSeries("minor Doc", VersioningState.MINOR);

		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, minorDoc.getCmisObjectId(),minorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//the majorDoc
		assertEquals(1, versions.size());
		assertTrue((boolean)minorDoc.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
		assertFalse((boolean)minorDoc.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
		assertFalse((boolean)minorDoc.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));

		//CREATE A PWC
		objectId = new Holder<String>(minorDoc.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);

		//CREATE ANOTHER MINOR VERSION
		versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		//the PWC and the minorDoc
		versions = versioningService.getAllVersions(TEST_REPO_2, minorDoc.getCmisObjectId(),minorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());

		for (CMISObject version : versions) {
			//0.1
			assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));

			if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("0.1"))  {
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			} //0.2
			else {
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			}
		}
	}

	@Test
	public void testVersioningFlags() throws PropertyNotFoundException {
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = null;
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);

		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//the majorDoc
		assertEquals(1, versions.size());
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));

		//CREATE A PWC
		objectId = new Holder<String>(majorDoc.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);

		//CREATING A PWC DOESNT EFFECT THE TRIPLET OF FLAGS
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
		assertTrue((boolean)majorDoc.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));

		//CREATE ANOTHER MAJOR VERSION
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		//the PWC and the majorDoc
		versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());

		for (CMISObject version : versions) {
			//1.0
			assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));

			if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("1.0"))  {
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			} //2.0
			else {
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			}
		}
	}

	@Test
	public void testCheckinCommentPropertyEmpty_0() throws PropertyNotFoundException {
		doTestCheckinCommentProperty(null, false);
	}

	@Test
	public void testCheckinCommentPropertyEmpty_1() throws PropertyNotFoundException {
		doTestCheckinCommentProperty("", false);
	}

	@Test
	public void testCheckinCommentPropertyNotEmpty() throws PropertyNotFoundException {
		doTestCheckinCommentProperty("Not empty comment", true);
	}

	private void doTestCheckinCommentProperty(String comment, boolean mustExists) throws PropertyNotFoundException {
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = null;
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);

		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//the majorDoc
		assertEquals(1, versions.size());

		//CREATE A PWC
		objectId = new Holder<String>(majorDoc.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);

		//CREATE ANOTHER MAJOR VERSION
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, comment, Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		//the PWC and the majorDoc
		versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(), majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());

		for (CMISObject version : versions) {
			//1.0
			assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));

			if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("1.0"))  {
				assertEquals("Ceckin comment property was found in version 1.0", false, hasCheckinCommnet(version));
			} //2.0
			else {
				assertEquals("Ceckin comment property inconsistent in version 2.0", mustExists, hasCheckinCommnet(version));
			}
		}
	}

	private boolean hasCheckinCommnet(CMISObject version) {
		boolean found = true;
		try {
			version.getProperty(PropertyIds.CHECKIN_COMMENT);
		} catch (PropertyNotFoundException e) {
			found = false;
		}
		return found;
	}

	@Test
	public void testDeletePreservesFlags() throws PropertyNotFoundException {
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = null;
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR); //A MAJOR VERSION
		objectId = new Holder<String>(majorDoc.getCmisObjectId());

		//A MINOR VERSION
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		//A MAJOR VERSION
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		//3 versions 1.0, 1.1 and 2.0
		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(3, versions.size());

		CMISObject toDelete = null;
		for (CMISObject version : versions) {
			if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("1.0"))  {
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			} else
				if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("1.1"))  {
					assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
					assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
					assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
				} else {//2.0
					toDelete = version;
					assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
					assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
					assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
				}
		}

		objectService.deleteObject(TEST_REPO_2, toDelete.getCmisObjectId(), false);
		versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());
		for (CMISObject version : versions) {
			if (version.getPropertyValue(PropertyIds.VERSION_LABEL).equals("1.0"))  {
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			} else {
				toDelete = version;
				assertTrue((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION));
				assertFalse((boolean)version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION));
			}
		}
	}

	@Test
	public void testVersionFlagCreateMajor() throws PropertyNotFoundException {
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = null;
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR); //A MAJOR VERSION - 1.0
		objectId = new Holder<String>(majorDoc.getCmisObjectId());

		//A MAJOR VERSION - 2.0
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		//A MINOR VERSION - 2.1
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		//A MINOR VERSION - 2.2
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		//A MAJOR VERSION - 3.0
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		versioningService.checkIn(TEST_REPO_2, objectId, true, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		//5 versions
		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(5, versions.size());
		assertEquals(Constants.DEFAULT_CREATE_MAJOR_VERSIONLABEL, majorDoc.getPropertyValue(PropertyIds.VERSION_LABEL));

		for (CMISObject version : versions) {
			String label = version.getPropertyValue(PropertyIds.VERSION_LABEL);
			boolean isLatest = version.getPropertyValue(PropertyIds.IS_LATEST_VERSION);
			boolean isMajor = version.getPropertyValue(PropertyIds.IS_MAJOR_VERSION);
			boolean isLatestMajor = version.getPropertyValue(PropertyIds.IS_LATEST_MAJOR_VERSION);

			if (label.equals("1.0"))  {
				assertTrue(isMajor);
				assertFalse(isLatestMajor);
				assertFalse(isLatest);
			} else
				if (label.equals("2.0"))  {
					assertTrue(isMajor);
					assertFalse(isLatestMajor);
					assertFalse(isLatest);
				} else
					if (label.equals("2.1"))  {
						assertFalse(isMajor);
						assertFalse(isLatestMajor);
						assertFalse(isLatest);
					} else
						if (label.equals("2.2"))  {
							assertFalse(isMajor);
							assertFalse(isLatestMajor);
							assertFalse(isLatest);
						} else{//3.0
							assertEquals("3.0", version.getPropertyValue(PropertyIds.VERSION_LABEL));
							assertTrue(isMajor);
							assertTrue(isLatestMajor);
							assertTrue(isLatest);
						}
		}
	}

	@Test
	public void testVersionFlagCreateMinor() throws PropertyNotFoundException {
		CMISObject testDoc = createVersionSeries("test Doc", VersioningState.MINOR); //A MAJOR VERSION - 1.0
		assertEquals(Constants.DEFAULT_CREATE_MINOR_VERSIONLABEL, testDoc.getPropertyValue(PropertyIds.VERSION_LABEL));
	}

	@Test
	public void testVersionFlagCreateNone() throws PropertyNotFoundException {
		CMISObject testDoc = createVersionSeries("test Doc", VersioningState.NONE); //NO VERSION - n/a
		assertEquals(Constants.DEFAULT_CREATE_NONE_VERSIONLABEL, testDoc.getPropertyValue(PropertyIds.VERSION_LABEL));
	}

	@Test
	public void testVersionFlagCreatePWC() throws PropertyNotFoundException {
		CMISObject testDoc = createVersionSeries("test Doc", VersioningState.CHECKEDOUT); //A CO VERSION - PWC
		assertEquals(Constants.DEFAULT_CREATE_COUT_VERSIONLABEL, testDoc.getPropertyValue(PropertyIds.VERSION_LABEL));
	}

	@Test
	public void testCheckout() throws Exception {
		//minor doc
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);

		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());
		CMISObject pwc = cmisObjectSelector.getCMISObjectWithChildrenAndProperties(objectId.getValue());

		//test Doc and PWC
		assertEquals(Constants.DEFAULT_CREATE_COUT_VERSIONLABEL, pwc.getPropertyValue(PropertyIds.VERSION_LABEL));
		assertEquals("test Doc", pwc.getPropertyValue(PropertyIds.NAME));
		assertEquals("test:document", pwc.getPropertyValue(PropertyIds.OBJECT_TYPE_ID));
		assertTrue((boolean) pwc.getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));
		assertEquals(majorDoc.getPropertyValue(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID), pwc.getCmisObjectId());
		assertEquals(pwc.getPropertyValue(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID), pwc.getCmisObjectId());
		assertEquals(pwc.getPropertyValue(PropertyIds.VERSION_SERIES_ID), majorDoc.getPropertyValue(PropertyIds.VERSION_SERIES_ID));

	}

	@Test
	public void testCheckin() throws Exception {
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.MAJOR);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);

		objectId = new Holder<String>(majorDoc.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);

		//check in a minor
		versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());

		CMISObject minor = cmisObjectSelector.getCMISObjectWithChildrenAndProperties(objectId.getValue());

		assertEquals("1.1", minor.getPropertyValue(PropertyIds.VERSION_LABEL));
		assertEquals("test Doc", minor.getPropertyValue(PropertyIds.NAME));
		assertEquals("test:document", minor.getPropertyValue(PropertyIds.OBJECT_TYPE_ID));
		assertFalse((boolean) minor.getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT));
		assertEquals(minor.getPropertyValue(PropertyIds.VERSION_SERIES_ID), majorDoc.getPropertyValue(PropertyIds.VERSION_SERIES_ID));
	}

	
	@Test
	public void testIsPrivateWorkingCopy() throws Exception {
		CMISObject doc1 = createVersionSeries("myDoc1.txt", VersioningState.NONE);
		assertEquals("Created document with versioning state NONE shouldn't be PWC", false, 
				doc1.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		
		CMISObject doc2 = createVersionSeries("myDoc2.txt", VersioningState.MINOR);
		assertEquals("Created document with versioning state MINOR shouldn't be PWC", false, 
				doc2.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		
		CMISObject doc3 = createVersionSeries("myDoc3.txt", VersioningState.MAJOR);
		assertEquals("Created document with versioning state MAJOR shouldn't be PWC", false, 
				doc3.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		

		CMISObject doc4 = createVersionSeries("myDoc4.txt", VersioningState.CHECKEDOUT);
		assertEquals("Created document with versioning state CHECKEDOUT should be PWC", true, 
				doc4.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		
		//check out
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);
		Holder<String> objectId = new Holder<String>(doc1.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		CMISObject doc5 = objectService.getObject(TEST_REPO_2, objectId.getValue());
		assertEquals("Check-out document should be PWC", true, 
				doc5.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		
		//check in
		versioningService.checkIn(TEST_REPO_2, objectId, false, new PropertiesImpl(), null, "Comment", 
				Collections.EMPTY_LIST, Collections.EMPTY_SET, Collections.EMPTY_SET);
		CMISObject doc6 = objectService.getObject(TEST_REPO_2, objectId.getValue());
		assertEquals("Check-in document shouldn't be PWC", false, 
				doc6.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));
		
		//check out again
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		CMISObject doc7 = objectService.getObject(TEST_REPO_2, objectId.getValue());
		assertEquals("Check-out document should be PWC", true, 
				doc7.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY));

		
		//check the other documents in the version series
		List<CMISObject> series = versioningService.getAllVersions(TEST_REPO_2, objectId.getValue(), null);
		for(CMISObject doc: series) {
			Boolean isPWC = doc.getPropertyValue(PropertyIds.IS_PRIVATE_WORKING_COPY);
			String versionLabel = doc.getPropertyValue(PropertyIds.VERSION_LABEL);
			
			if(Constants.DEFAULT_CREATE_COUT_VERSIONLABEL.equals(versionLabel)) {
				assertEquals("Document with version Label PWC should have property cmis:isPrivateWorkingCopy true", true, isPWC);
			} else {
				assertEquals("Document with version Label other than PWC should have property cmis:isPrivateWorkingCopy false", false, isPWC);
			}
			
			//double check the method is working property
			assertEquals("CMISObject.isPwc() should me consistent with the property cmis:isPrivateWorkingCopy", doc.isPwc(), isPWC);
		}
		
	}
	
	@Test
	public void testCancelCheckout() throws Exception {
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.NONE);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());
		Holder<Boolean> contentCopied = new Holder<Boolean>(false);

		objectId = new Holder<String>(majorDoc.getCmisObjectId());
		versioningService.checkOut(TEST_REPO_2, objectId, contentCopied);
		//ORIGINAL AND PWC
		List<CMISObject> versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		assertEquals(2, versions.size());

		versioningService.cancelCheckOut(TEST_REPO_2, objectId.getValue());
		versions = versioningService.getAllVersions(TEST_REPO_2, majorDoc.getCmisObjectId(),majorDoc.getProperty(PropertyIds.VERSION_SERIES_ID).getTypedValue().toString());
		//JUST THE ORIGINAL FILE
		assertEquals(1, versions.size());
		assertEquals(Constants.DEFAULT_CREATE_NONE_VERSIONLABEL, versions.get(0).getPropertyValue(PropertyIds.VERSION_LABEL));

	}

	@Test
	public void negativeScenario_checkIn_notCheckedOut() throws Exception {
		CMISObject majorDoc = createVersionSeries("test Doc", VersioningState.NONE);
		Holder<String> objectId = new Holder<String>(majorDoc.getCmisObjectId());

		objectId = new Holder<String>(majorDoc.getCmisObjectId());
		try {
			versioningService.checkIn(TEST_REPO_2, objectId, false, null, null, "", Collections.<String>emptyList(), Collections.<Acl>emptySet(),Collections.<Acl>emptySet());
		} catch (Exception e) {
			assertTrue(e.getMessage().endsWith("is not checked-out, cannot get PWC"));
		}
	}

	//PRIVATE
	private CMISObject createVersionSeries(String name, VersioningState state) {
		CMISObject majorDoc;

		//every test following needs a starting document
		majorDoc = new CMISObject();
		ObjectType tp = new ObjectType();
		tp.setCmisId("test:document");
		majorDoc.setObjectType(tp);
		majorDoc.addProperty(getTestProperty(name, TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.NAME));
		majorDoc.addProperty(getTestProperty("test:document", TEST_REPO_2, Constants.TYPE_CMIS_DOCUMENT, PropertyIds.OBJECT_TYPE_ID));
		majorDoc = objectService.createObject(TEST_REPO_2, majorDoc, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, state, BaseTypeId.CMIS_DOCUMENT);

		return majorDoc;
	}

}
