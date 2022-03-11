package eu.trade.repo.service.cmis;

import static eu.trade.repo.TestConstants.ADMIN_PWD;
import static eu.trade.repo.TestConstants.ADMIN_USER;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.testng.Assert;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.Repository;

public class CmisVersioningServiceTest extends BaseTestClass {

	@Test
	public void testVersionSpecificFiling01MoveEmptyFolder() throws Exception {
		
		setScenario(true);
		
		/*move empty folder*/
		cmisObjectService.moveObject("vsf_enabled", 
			new Holder<String>("fb8e554fac9173048394ba538bde677e6b743481"), /* /empty_folder */ 
			"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /disaggregated */ 
			"e27a657a91f017fdf0476a956924a5b74bd4eca7", /* rootFolder */ 
			null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test01.xml");
	}
	

	
	@Test
	public void testVersionSpecificFiling02MoveFirstVersion() throws Exception {
		
		setScenario(true);
		
		/*move first version*/
		cmisObjectService.moveObject("vsf_enabled", 
				new Holder<String>("b360517db27aeaf94117681548229009db979647"), /* /repo-workflow.txt (n/a) */ 
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /disaggregated */ 
				"e27a657a91f017fdf0476a956924a5b74bd4eca7", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test02.xml");
	}

	
	@Test
	public void testVersionSpecificFiling03MoveLastVersion() throws Exception {
		
		setScenario(true);
		
		/*move last version*/
		cmisObjectService.moveObject("vsf_enabled", 
				new Holder<String>("be3a624ca393d7699effbfcbc2bd7761c556ed1c"), /* /repo-workflow.txt (1.1) */ 
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /disaggregated */ 
				"e27a657a91f017fdf0476a956924a5b74bd4eca7", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test03.xml");
	}


	@Test
	public void testVersionSpecificFiling04MovePWC() throws Exception {
		
		setScenario(true);
		
		/*move PWC*/
		cmisObjectService.moveObject("vsf_enabled", 
				new Holder<String>("d78eb554c17f12d99aae4915e3f112247f7d7af3"), /* /repo-workflow.txt (PWC) */ 
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /disaggregated */ 
				"e27a657a91f017fdf0476a956924a5b74bd4eca7", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test04.xml");
	}

	@Test
	public void testVersionSpecificFiling05MoveEmptyFolder() throws Exception {
		
		setScenario(false);
		
		/*move empty folder*/
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("8db8b17c1dfc55620592cac6ff84c98746f31350"), /* /empty_folder */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test05.xml");
	}


	
	@Test
	public void testVersionSpecificFiling06MoveNotVersioned() throws Exception {
		
		setScenario(false);
		
		/*move not versioned object*/
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("1649d0aa60ce607ef9b80705a32fd048235c2d88"), /* /no_versions.txt */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test06.xml");
	}


	@Test
	public void testVersionSpecificFiling07MoveLastVersionWithPWC() throws Exception {
		
		setScenario(false);
		
		/*move last version, with PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("d1d7106f2061563ed60180f0e4a87893a1e741af"), /* /repo-workflow.txt (2.0)*/ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test07.xml");
		
		//check the listing of children is filtered
		testFolderListing("vsf_disabled", 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /*/grouped */
				new String[] {
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af" /* repo-workflow.txt (2.0) */
			});
	}



	@Test
	public void testVersionSpecificFiling08MoveLastVersionWithoutPWC() throws Exception {
		
		setScenario(false);
		
		/* move last version, without PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd"), /* /grouped/cmis-client.xml (3.0) */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test08.xml");
		
		testFolderListing("vsf_disabled",
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /*/grouped */
				new String[] {
				"8db8b17c1dfc55620592cac6ff84c98746f31350", /* /empty_folder */
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af", /* repo-workflow.txt (2.0) */
				"1649d0aa60ce607ef9b80705a32fd048235c2d88", /* no_versions.txt */
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b" /*/grouped */
			});
	}
	
	
	@Test
	public void testVersionSpecificFiling09MovePWC() throws Exception {
		
		setScenario(false);
		
		/* move PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e"), /* /repo-workflow.txt (PWC) */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		//same result as test 07
		compareTable("object_child", "scenarioVersionSpecificFiling-test07.xml");
		
		//check the listing of children is filtered
		testFolderListing("vsf_disabled",
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /*/grouped */
				new String[] {
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af" /* repo-workflow.txt (2.0) */
			});
	}
	
	
	@Test
	public void testVersionSpecificFiling10MoveFirstVersionWithPWC() throws Exception {
		
		setScenario(false);
		
		/* move first version with PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("dc4fc1f7e10b2684adb422ca2e371d4830e0bb16"), /* /repo-workflow.txt (n/a) */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		//same result as test 07
		compareTable("object_child", "scenarioVersionSpecificFiling-test07.xml");
		
		//check the listing of children is filtered
		testFolderListing("vsf_disabled", 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /*/grouped */
				new String[] {
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af" /* repo-workflow.txt (2.0) */
			});
	}
	

	
	@Test
	public void testVersionSpecificFiling11MoveFirstVersionWithoutPWC() throws Exception {
		
		setScenario(false);
		
		/* move first version with PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("96e9149d908a5a4d4303cda48e0da11bde035708"), /* /grouped/cmis-client.xml (1.0) */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				null);
		
		//same result as test 08
		compareTable("object_child", "scenarioVersionSpecificFiling-test08.xml");
		
		testFolderListing("vsf_disabled", 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /*/grouped */
				new String[] {
				"8db8b17c1dfc55620592cac6ff84c98746f31350", /* /empty_folder */
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af", /* repo-workflow.txt (2.0) */
				"1649d0aa60ce607ef9b80705a32fd048235c2d88", /* no_versions.txt */
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b" /*/grouped */
			});
	}
	
	@Test
	public void testVersionSpecificFiling12MoveIntermediateVersionWithoutPWC() throws Exception {
		
		setScenario(false);
		
		/* move first version with PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("2e9631505e77871c3bbcde31974f4b7c3e0b843e"), /* /grouped/cmis-client.xml (2.1) */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				null);
		
		//same result as test 08
		compareTable("object_child", "scenarioVersionSpecificFiling-test08.xml");
		
		testFolderListing("vsf_disabled", 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /*/grouped */
				new String[] {
				"8db8b17c1dfc55620592cac6ff84c98746f31350", /* /empty_folder */
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af", /* repo-workflow.txt (2.0) */
				"1649d0aa60ce607ef9b80705a32fd048235c2d88", /* no_versions.txt */
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b" /*/grouped */
			});
	}	
	
	
	@Test
	public void testVersionSpecificFiling13MoveIntermediateVersionWithPWC() throws Exception {
		
		setScenario(false);
		
		/* move first version with PWC */
		cmisObjectService.moveObject("vsf_disabled", 
				new Holder<String>("62cb0e0002026d9f252f6e862bd1462ec3a6d753"), /* /repo-workflow.txt (1.1) */ 
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /* /grouped */ 
				"248f09f9cda18f26a2609b49f66d2163ca1716b6", /* rootFolder */ 
				null);
		
		//same result as test 07
		compareTable("object_child", "scenarioVersionSpecificFiling-test07.xml");
		
		//check the listing of children is filtered
		testFolderListing("vsf_disabled",
				"7825fb0b42d45f500cfc84ec0dd094cfd4fc8d3b", /*/grouped */
				new String[] {
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", /* cmis-client.xml (3.0) */
				"7eb6b1d9775ed3f127c278ca10c0ea0e50a9598e", /* repo-workflow.txt (PWC) */
				"d1d7106f2061563ed60180f0e4a87893a1e741af" /* repo-workflow.txt (2.0) */
			});
	}
	
	@Test
	public void testVersionSpecificFiling14DeleteFolderDisaggregatedSingleVersion() throws Exception {
		
		setScenario(true);
		
		// 14 delete folder with one document disaggregated (vsf enabled) single version
		cmisObjectService.deleteTree("vsf_enabled", 
				"5fed19b4c0ce39d095100ac24963f4a957c4ed6f", /* /disaggregated/draft */ 
				false, 
				UnfileObject.DELETE, true, null);
		
		compareTable("object", "scenarioVersionSpecificFiling-test14.xml");
	}

	@Test
	public void testVersionSpecificFiling15DeleteFolderDisaggregatedAllVersions() throws Exception {
		
		setScenario(true);
		
		// 15 delete folder with one document disaggregated (vsf enabled) all versions
		cmisObjectService.deleteTree("vsf_enabled", 
				"5fed19b4c0ce39d095100ac24963f4a957c4ed6f", /* /disaggregated/draft */ 
				true, 
				UnfileObject.DELETE, true, null);
		
		compareTable("object", "scenarioVersionSpecificFiling-test15.xml");
	}

	@Test
	public void testVersionSpecificFiling16DeleteFolderDisaggregated() throws Exception {
		
		setScenario(true);
		//the repository was created with vsf=true and then changed to vsf=false
		Repository repo = repositoryService.getRepositoryById("vsf_enabled");
		repo.setVersionSpecificFiling(false);
		repositoryService.update(repo);
		
		// delete folder with one document disaggregated (vsf disabled) ***
		cmisObjectService.deleteTree("vsf_enabled", 
				"5fed19b4c0ce39d095100ac24963f4a957c4ed6f", /* /disaggregated/draft */ 
				false, 
				UnfileObject.DELETE, true, null);
		
		//same result as 14
		compareTable("object", "scenarioVersionSpecificFiling-test14.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test16.xml");
		
		//check the children listing
		testFolderListing("vsf_enabled",
				"3821bbc395e7565ba5c2f29f9716846d77661608", /*/disaggregated/published */
				new String[] {
				"d069a690d8770c44939b4027a392c5874219f5bb" /* published.txt (2.0) */
			});
		testFolderListing("vsf_enabled",
				"c1311499ba029a763f982ae048ad3bc6ad2739a0", /*/disaggregated/approved */
				new String[0]);
		
	}
	
	@Test
	public void testVersionSpecificFiling17MoveDisaggregated() throws Exception {
		
		setScenario(true);
		//the repository was created with vsf=true and then changed to vsf=false
		Repository repo = repositoryService.getRepositoryById("vsf_enabled");
		repo.setVersionSpecificFiling(false);
		repositoryService.update(repo);
		
		// move a document disaggregated (vsf disabled) ***
		/*
		 * With a repository created with vsf enabled and the versions in
		 * several places, then the vsf has changed to disabled.
		 * The moving should "regroup" the disaggregated documents in the version series 
		 */
		cmisObjectService.moveObject("vsf_enabled", 
				new Holder<String>("d069a690d8770c44939b4027a392c5874219f5bb") /* published.txt (2.0) */, 
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /dissaggregated */ 
				"3821bbc395e7565ba5c2f29f9716846d77661608", /* /disaggregated/published */ 
				null);
		
		compareTable("object_child", "scenarioVersionSpecificFiling-test17.xml");
		
		//check the children listing
		testFolderListing("vsf_enabled",
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /*/disaggregated */
				new String[] {
				"d069a690d8770c44939b4027a392c5874219f5bb" /* published.txt (2.0) */
			});
	}
	
	
	@Test
	public void testVersionSpecificFiling18MoveDisaggregatedError() throws Exception {
		
		setScenario(true);
		//the repository was created with vsf=true and then changed to vsf=false
		Repository repo = repositoryService.getRepositoryById("vsf_enabled");
		repo.setVersionSpecificFiling(false);
		repositoryService.update(repo);
		
		// move a document disaggregated (vsf disabled) ***
		/*
		 * With a repository created with vsf enabled and the versions in
		 * several places, then the vsf has changed to disabled.
		 * The moving should "regroup" the disaggregated documents in the version series 
		 * 
		 * The system will fail to move the non-latest version.
		 */
		try {
			cmisObjectService.moveObject("vsf_enabled", 
				new Holder<String>("335f18fed276ce39398012577e658f934bcfd3c2") /* draft.txt (n/a) */, 
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /dissaggregated */ 
				"5fed19b4c0ce39d095100ac24963f4a957c4ed6f", /* /disaggregated/draft */ 
				null);
			Assert.fail("ERROR: The only document in the version series that is moveable is the latest one " +
					"(with version specific filing disabled).");
		} catch (IllegalStateException e) {
			
		}
		
	}
	
	
	@Test
	public void testVersionSpecificFiling19DeleteDocDisaggregatedSingleVersion() throws Exception {
		
		setScenario(true);
		//the repository was created with vsf=true and then changed to vsf=false
		Repository repo = repositoryService.getRepositoryById("vsf_enabled");
		repo.setVersionSpecificFiling(false);
		repositoryService.update(repo);
		
		/*
		 * With a repository created with vsf enabled and the versions in
		 * several places, then the vsf has changed to disabled.
		 */
		// delete one document in the version series disaggregated (vsf disabled) ***
		cmisObjectService.deleteObject("vsf_enabled", 
				"335f18fed276ce39398012577e658f934bcfd3c2", false, null);
		
		compareTable("object", "scenarioVersionSpecificFiling-test19.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test19.xml");
		
		//check the children listing
		testFolderListing("vsf_enabled",
				"de2d6a780c2082624b55e61dc98be7a6fd582860", /* /disaggregated/approved */
				new String[0]);
		testFolderListing("vsf_enabled",
				"3821bbc395e7565ba5c2f29f9716846d77661608", /* /disaggregated/published */
				new String[] {
				"d069a690d8770c44939b4027a392c5874219f5bb" /* published.txt (2.0) */
		});
	}
	
	
	@Test
	public void testVersionSpecificFiling20DeleteDocDisaggregatedAllVersions() throws Exception {
		
		setScenario(true);
		//the repository was created with vsf=true and then changed to vsf=false
		Repository repo = repositoryService.getRepositoryById("vsf_enabled");
		repo.setVersionSpecificFiling(false);
		repositoryService.update(repo);
		
		/*
		 * With a repository created with vsf enabled and the versions in
		 * several places, then the vsf has changed to disabled.
		 */
		// delete all documents in the version series disaggregated (vsf disabled) ***
		cmisObjectService.deleteObject("vsf_enabled", 
				"335f18fed276ce39398012577e658f934bcfd3c2", true, null);
		
		compareTable("object", "scenarioVersionSpecificFiling-test20.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test20.xml");
		
	}

	
	@Test
	public void testVersionSpecificFiling21DeleteDocDisaggregatedAllVersions() throws Exception {
		
		setScenario(true);

		// delete all documents in the version series disaggregated (vsf enabled)
		cmisObjectService.deleteObject("vsf_enabled", 
				"335f18fed276ce39398012577e658f934bcfd3c2", true, null);
		
		//same result as 20
		compareTable("object", "scenarioVersionSpecificFiling-test20.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test20.xml");
		
	}

	
	@Test
	public void testVersionSpecificFiling22DeleteDocGroupedAllVersions() throws Exception {
		
		setScenario(true);

		// delete all documents in the version series grouped (vsf enabled)
		cmisObjectService.deleteObject("vsf_enabled", 
				"d78eb554c17f12d99aae4915e3f112247f7d7af3", true, null); /* /repo-workflow.txt (PWC) */

		compareTable("object", "scenarioVersionSpecificFiling-test22.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test22.xml");
		
	}
	
	@Test
	public void testVersionSpecificFiling23DeleteDocGroupedAllVersions() throws Exception {
		
		setScenario(false);

		// delete all documents in the version series grouped (vsf disabled)
		cmisObjectService.deleteObject("vsf_disabled", 
				"9d96c868fcc77a831a3ca3fa6acfab5b3edf65fd", true, null);  /* /grouped/cmis-client.xml (3.0) */

		compareTable("object", "scenarioVersionSpecificFiling-test23.xml");
		compareTable("object_child", "scenarioVersionSpecificFiling-test23.xml");
		
	}
	

	
	/**
	 * 
	 * @param versionSpecificFiling
	 * @throws Exception
	 */
	private void setScenario(boolean versionSpecificFiling) throws Exception {
		setUser(ADMIN_USER, ADMIN_PWD, versionSpecificFiling ? "vsf_enabled": "vsf_disabled");
		setScenario(
				(versionSpecificFiling ? 
					"scenarioVersionSpecificFiling.xml" : 
					"scenarioVersionSpecificFilingDisabled.xml"), 
				DatabaseOperation.CLEAN_INSERT);
		callContextHolder.login();
	}
	
	/**
	 * Check if the content of the folder contains the children given.
	 * 
	 * @param repositoryId
	 * @param folderId
	 * @param childrenIds
	 */
	private void testFolderListing(String repositoryId, String folderId, String[] childrenIds) {
		ObjectInFolderList results = cmisNavigationService.getChildren(repositoryId, 
				folderId,
				null, null, false, IncludeRelationships.NONE, 
				null, false, BigInteger.valueOf(-1), BigInteger.ZERO, null);
		Set<String> ids = new HashSet<String>();
		for(ObjectInFolderData data: results.getObjects()) {
			ids.add(data.getObject().getId());
		}
		
		Assert.assertTrue(ids.containsAll(Arrays.asList(childrenIds)));
	}
}
