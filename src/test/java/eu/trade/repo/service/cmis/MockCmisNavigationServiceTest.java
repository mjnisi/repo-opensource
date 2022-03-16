package eu.trade.repo.service.cmis;

import static eu.trade.repo.util.Constants.TYPE_CMIS_DOCUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.model.view.ObjectParentId;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.interfaces.INavigationService;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.util.Node;
import eu.trade.repo.service.util.Page;
import eu.trade.repo.service.util.Tree;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/mock/testApplicationContext-codecs.xml")
public class MockCmisNavigationServiceTest {
	
	@InjectMocks
	private CmisNavigationService cmisnavigationService;
	
	@Mock
	private INavigationService navigationService;
	
	@Mock
	private IRelationshipService relationshipService;

	@Mock
	private Security security;		
	
	static String CMIS_ID_PREFIX = "cmisId:";
	
	private final String mockObjectId = "mockObjectId";
	private final String mockRepositoryId = "mockRepositoryId";
	private final String mockFolderId = "mockFolderId";
	private final String mockFilter = "mockFilter";
	private final String mockOrderBy = "mockOrderBy";
	private final String mockRenditionFilter = "mockRenditionFilter";
	private final BigInteger mockMaxItems =  new BigInteger("100");
	private final BigInteger mockSkipCount = new BigInteger("0");
	private final ExtensionsData mockExtension = mock(ExtensionsData.class);
	private final Map<String, Set<CMISObject>> mockRelationshipMappings = mock(Map.class);
	private final AllowableActions mockAllowableActions = mock(AllowableActions.class);
	private final Page<CMISObject> expectedChildrenPage = getPage(10, getCMISObject(240), getCMISObject(157));
	private final Set<CMISObject> expectedCheckedOutObjects = getObjectResults(getCMISObject(40), getCMISObject(12), getCMISObject(670));
	private final CMISObject expectedObjectParents = getCMISObjectWithParents(100, 10, 20);
	private final BigInteger depth = new BigInteger("2");
	private final Tree expectedTree = getTree();
		
	@Captor
	private ArgumentCaptor<String> captorRequestRepository;
	
	@Captor
 	private ArgumentCaptor<Set<CMISObject>> captorRequestCmisObjects;
	
	@Captor
 	private ArgumentCaptor<Collection<Node>> captorRequestNodeObjects;
	
	@Captor
 	private ArgumentCaptor<RelationshipDirection> captorRequestRelationshipDirection;
	
	@Captor
	private ArgumentCaptor<IncludeRelationships> captorRequestIncludeRelationship;
	
	@Captor
	private ArgumentCaptor<Integer> captorRequestMaxItems;
	
	@Captor
	private ArgumentCaptor<Integer> captorRequestSkipCount;
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		
		when(navigationService.getChildren(mockRepositoryId, mockFolderId, mockOrderBy, 
				mockMaxItems.intValue(), mockSkipCount.intValue())).thenReturn(expectedChildrenPage);
		when(navigationService.getCheckedOutDocs(mockRepositoryId, mockFolderId, mockOrderBy, 
				mockMaxItems.intValue(), mockSkipCount.intValue())).thenReturn(expectedCheckedOutObjects);																
		when(navigationService.getObjectWithParents(mockRepositoryId, mockObjectId))
				.thenReturn(expectedObjectParents);
				
		when(security.getAllowableActions(any(CMISObject.class)))
				.thenReturn(mockAllowableActions);
		
		when(relationshipService.getRelationshipMappings(anyString(),(Set<CMISObject>) anySet(), any(IncludeRelationships.class))).thenReturn(mockRelationshipMappings);
		when(relationshipService.getRelationshipMappings(anyString(),(Collection<Node>) anySet(), any(IncludeRelationships.class))).thenReturn(mockRelationshipMappings);
		
	}
	
	/**
	 * scenario 
	 * navigationService.getFolderParent return CMISObject(id=23, cmisObjectId="cmisId:23")
	 * services : navigationService
	 * expected : navigationService(1), relationshipService(0), security(0)
	 */
	@Test
	public void testGetFolderParent() {
		when(navigationService.getFolderParent(mockRepositoryId, mockFolderId)).thenReturn(getCMISObject(23));
	 	
		ObjectData actualObject = cmisnavigationService.getFolderParent(mockRepositoryId, mockFolderId, mockFilter, mockExtension);
		
		assertNotNull("actual object shouldn't be NULL", actualObject);
		assertTrue("id of returning object should be cmisId:23", actualObject.getId().equals(CMIS_ID_PREFIX + 23));
		assertNull("allowable actions should be NULL", actualObject.getAllowableActions());
		assertNull("relationships should be NULL", actualObject.getRelationships());
		
		verify(navigationService, times(1)).getFolderParent(mockRepositoryId, mockFolderId);
		verifyNoMoreInteractions(navigationService, relationshipService, security);
	}	
	
	/**
	 * scenario 
	 * services : navigationService throw CmisInvalidArgumentException
	 * expected : Exception
	 */
	@Test(expected=CmisInvalidArgumentException.class)
	public void testGetFolderParent_CmisInvalidArgumentException() {
		when(navigationService.getFolderParent(mockRepositoryId, mockFolderId)).thenThrow(CmisInvalidArgumentException.class);
	 	
		cmisnavigationService.getFolderParent(mockRepositoryId, mockFolderId, mockFilter, mockExtension);			
	}
	
	/**
	 * scenario 
	 * services : navigationService throw CmisInvalidArgumentException
	 * expected : Exception
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testGetFolderParent_IllegalArgumentException() {
		when(navigationService.getFolderParent(mockRepositoryId, mockFolderId)).thenThrow(IllegalArgumentException.class);
	 	
		cmisnavigationService.getFolderParent(mockRepositoryId, mockFolderId, mockFilter, mockExtension);				
	}
	
	/**
	 * scenario 
	 * services : navigationService throw CmisInvalidArgumentException
	 * expected : Exception
	 */
	@Test(expected=CmisInvalidArgumentException.class)
	public void testGetFolderParent_CmisObjectNotFoundException() {
		when(navigationService.getFolderParent(mockRepositoryId, mockFolderId)).thenThrow(CmisInvalidArgumentException.class);
	 	
		cmisnavigationService.getFolderParent(mockRepositoryId, mockFolderId, mockFilter, mockExtension);				
	}
	
	/**
	 * scenario -- this shouldn't happen in real app
	 * services : navigationService throw NullPointerException
	 * expected : Exception
	 */
	@Test(expected=NullPointerException.class)
	public void testGetFolderParent_NullPointerException() {
		when(navigationService.getFolderParent(mockRepositoryId, mockFolderId)).thenThrow(NullPointerException.class);
	 	
		cmisnavigationService.getFolderParent(mockRepositoryId, mockFolderId, mockFilter, mockExtension);				
	}
	
	/**
	 * 
	 * scenario - this wouldn't ever be possible .. 
	 * boolean includeAllowableActions=true , (could have any other value)
	 * boolean includePathSegment=false , (could have any other value)
	 * IncludeRelationships includeRelationships = IncludeRelationships.BOTH , (could have any other value)
	 * navigationService.getChildren return NULL
	 * services : navigationService, security, relationshipService
	 * expected : Exception
	 */
	@Test(expected = NullPointerException.class)
	public void testGetChildren_relationshipServiceNULL() {	
		// override with NULL the returning value in case of  navigationService
		when(navigationService.getChildren(mockRepositoryId, mockFolderId, mockOrderBy, 
				mockMaxItems.intValue(), mockSkipCount.intValue())).thenReturn(null);
		
		getChildren(true, false, IncludeRelationships.BOTH);				
	}
		
	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getChildren return Page(size=10, pageElements=empty)
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetChildren_hasEmptyResults() {
		// override navigationService to return no child, instead of returning some mock children
		when(navigationService.getChildren(mockRepositoryId, mockFolderId, mockOrderBy, 
				mockMaxItems.intValue(), mockSkipCount.intValue())).thenReturn(getPage(10));
					
		assertGetChildren_emptyResults(getChildren(false, false, IncludeRelationships.NONE));		
		verifyNavigationService_getChildren();	
		verifyRelationshipService_withCmisObject(Collections.<String>emptySet(), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}

	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject((id=157,cmisObjectId="cmisId:157")))
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetChildren_allowableActionsFalse() {	
										
		assertGetChildren_NonEmptyResults(getChildren(false, false, IncludeRelationships.NONE), 
													  false, false, IncludeRelationships.NONE);		
		verifyNavigationService_getChildren();
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject(id=157,cmisObjectId="cmisId:157"))
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test	
	public void testGetChildren_allowableActionsTrue() {	
		
		assertGetChildren_NonEmptyResults(getChildren(true, false, IncludeRelationships.NONE), 
													  true, false, IncludeRelationships.NONE);				
		verifyNavigationService_getChildren();
		verifySecurity_getAllowableActions(expectedChildrenPage.getPageElements());
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.BOTH
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject(id=157,cmisObjectId="cmisId:157"))
	 * relationshipObjects (...)
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test	
	public void testGetChildren_relationshipBOTH() {
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{240, 157}, new Integer[]{111, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(240, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(157, PropertyIds.TARGET_ID)});
		
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "240")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "157")).thenReturn(Collections.singleton(expectedRelationshipObject));
		
		ObjectInFolderList actualObjectInFolderList = getChildren(true, false, IncludeRelationships.BOTH);
		
		assertGetChildren_NonEmptyResults(actualObjectInFolderList, true, false, IncludeRelationships.BOTH);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getChildren();
		verifySecurity_getAllowableActions(expectedChildrenPage.getPageElements());		
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.BOTH);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}
	
	private Map<String, List<String>> getObjectRelationshipMapping(Integer[] objectIds, Integer[] relationshipObjectIds) {
		Map<String, List<String>> objectRelationshipMappings = new HashMap();
		for(int i=0; i<objectIds.length; i++) {
			String key = CMIS_ID_PREFIX + objectIds[i];
			List<String> value = relationshipObjectIds[i] != null ? Arrays.asList(CMIS_ID_PREFIX + relationshipObjectIds[i]) : Collections.<String>emptyList();
			Map<String, List<String>> objectRelationshipMapping = Collections.singletonMap(key, value);
			objectRelationshipMappings.putAll(objectRelationshipMapping);
		}
		
		return objectRelationshipMappings;
	}

	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.SOURCE
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject(id=157,cmisObjectId="cmisId:157"))
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetChildren_relationshipSOURCE() {
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{240, 157}, new Integer[]{111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(240, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(157, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "240")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "157")).thenReturn(null);
		
		ObjectInFolderList actualObjectInFolderList = getChildren(true, false, IncludeRelationships.SOURCE);
		
		assertGetChildren_NonEmptyResults(actualObjectInFolderList, true, false, IncludeRelationships.SOURCE);	
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getChildren();
		verifySecurity_getAllowableActions(expectedChildrenPage.getPageElements());
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.SOURCE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * boolean includePathSegment=false
	 * IncludeRelationships includeRelationships = IncludeRelationships.TARGET
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject(id=157,cmisObjectId="cmisId:157"))
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetChildren_relationshipTARGET() {
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{240, 157}, new Integer[]{null, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(240, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(157, PropertyIds.TARGET_ID)});
		
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "240")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "157")).thenReturn(Collections.singleton(expectedRelationshipObject));
		
		ObjectInFolderList actualObjectInFolderList = getChildren(true, false, IncludeRelationships.TARGET);
		
		assertGetChildren_NonEmptyResults(actualObjectInFolderList, true, false, IncludeRelationships.TARGET);	
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getChildren();
		verifySecurity_getAllowableActions(expectedChildrenPage.getPageElements());
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.TARGET);
		verifyNoMoreInteractions(navigationService, security, relationshipService);	
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * boolean includePathSegment=true
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getChildren return Page(size=10, pageElements=CMISObject(id=240,cmisObjectId="cmisId:240"),
	 * 																   CMISObject(id=157,cmisObjectId="cmisId:157"))
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetChildren_pathSegmentTrue() {		
		
		assertGetChildren_NonEmptyResults(getChildren(true, true, IncludeRelationships.NONE),
													  true, true, IncludeRelationships.NONE);		
		verifyNavigationService_getChildren();
		verifySecurity_getAllowableActions(expectedChildrenPage.getPageElements());
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "240", CMIS_ID_PREFIX + "157")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getCheckedOutDocs return empty Set collection
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_hasEmptyResults() {
		// override navigationService to return no child, instead of returning some mock CheckedOutDocs
		when(navigationService.getCheckedOutDocs(mockRepositoryId, mockFolderId, mockOrderBy, 
				mockMaxItems.intValue(), mockSkipCount.intValue())).thenReturn(Collections.<CMISObject>emptySet());
		
		assertGetCheckedOutDocs_emptyResults(getCheckedOutDocs(false, IncludeRelationships.NONE));		
		verifyNavigationService_getCheckedOutDocs();
		verifyRelationshipService_withCmisObject(Collections.<String>emptySet(), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}
		
	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getCheckedOutDocs return Set(CMISObject(id=40,cmisObjectId="cmisId:40"), 
	 * 												  CMISObject(id=12,cmisObjectId="cmisId:12"), 
	 * 												  CMISObject(id=670,cmisObjectId="cmisId:670"));
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_allowableActionsFalse() {
		
		assertGetCheckedOutDocs_nonEmptyResults(getCheckedOutDocs(false, IncludeRelationships.NONE),
				  												  false, IncludeRelationships.NONE);		
		verifyNavigationService_getCheckedOutDocs();	
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "40", CMIS_ID_PREFIX + "12", CMIS_ID_PREFIX + "670")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}

	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getCheckedOutDocs return Set(CMISObject(id=40,cmisObjectId="cmisId:40"), 
	 * 												  CMISObject(id=12,cmisObjectId="cmisId:12"), 
	 * 												  CMISObject(id=670,cmisObjectId="cmisId:670"));
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(3), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_allowableActionsTrue() {
		
		assertGetCheckedOutDocs_nonEmptyResults(getCheckedOutDocs(true, IncludeRelationships.NONE),
				  												  true, IncludeRelationships.NONE);		
		verifyNavigationService_getCheckedOutDocs();
		verifySecurity_getAllowableActions(expectedCheckedOutObjects);	
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "40", CMIS_ID_PREFIX + "12", CMIS_ID_PREFIX + "670")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}	

	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * IncludeRelationships includeRelationships = IncludeRelationships.BOTH
	 * navigationService.getCheckedOutDocs return Set(CMISObject(id=40,cmisObjectId="cmisId:40"), 
	 * 												  CMISObject(id=12,cmisObjectId="cmisId:12"), 
	 * 												  CMISObject(id=670,cmisObjectId="cmisId:670"));
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(3), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_relationshipBOTH() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "40", CMIS_ID_PREFIX + "12", CMIS_ID_PREFIX + "670"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{40, 12, 670}, new Integer[]{111, 111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(40, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(12, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "40")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "12")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "670")).thenReturn(null);
		
		ObjectList actualObjectList = getCheckedOutDocs(true, IncludeRelationships.BOTH);
		assertGetCheckedOutDocs_nonEmptyResults(actualObjectList, true, IncludeRelationships.BOTH);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getCheckedOutDocs();
		verifySecurity_getAllowableActions(expectedCheckedOutObjects);	
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.BOTH);	
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * IncludeRelationships includeRelationships = IncludeRelationships.SOURCE
	 * navigationService.getCheckedOutDocs return Set(CMISObject(id=40,cmisObjectId="cmisId:40"), 
	 * 												  CMISObject(id=12,cmisObjectId="cmisId:12"), 
	 * 												  CMISObject(id=670,cmisObjectId="cmisId:670"));
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(3), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_relationshipSOURCE() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "40", CMIS_ID_PREFIX + "12", CMIS_ID_PREFIX + "670"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{40, 12, 670}, new Integer[]{111, null, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(40, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(12, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "40")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "12")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "670")).thenReturn(null);
				
		ObjectList actualObjectList = getCheckedOutDocs(true, IncludeRelationships.SOURCE);
		assertGetCheckedOutDocs_nonEmptyResults(actualObjectList, true, IncludeRelationships.SOURCE);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getCheckedOutDocs();
		verifySecurity_getAllowableActions(expectedCheckedOutObjects);	
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.SOURCE);	
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true 
	 * IncludeRelationships includeRelationships = IncludeRelationships.TARGET
	 * navigationService.getCheckedOutDocs return Set(CMISObject(id=40,cmisObjectId="cmisId:40"), 
	 * 												  CMISObject(id=12,cmisObjectId="cmisId:12"), 
	 * 												  CMISObject(id=670,cmisObjectId="cmisId:670"));
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(3), relationshipService(1)
	 */
	@Test
	public void testGetCheckedOutDocs_relationshipTARGET() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "40", CMIS_ID_PREFIX + "12", CMIS_ID_PREFIX + "670"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{40, 12, 670}, new Integer[]{null, 111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(40, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(12, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "40")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "12")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "670")).thenReturn(null);
		
		ObjectList actualObjectList = getCheckedOutDocs(true, IncludeRelationships.TARGET);
		assertGetCheckedOutDocs_nonEmptyResults(actualObjectList, true, IncludeRelationships.TARGET);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectList);
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getCheckedOutDocs();
		verifySecurity_getAllowableActions(expectedCheckedOutObjects);	
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.TARGET);	
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getObjectWithParents return CMISObject(id=100,cmisObjectId="cmisId:100"
	 * 														,parents = empty);
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetObjectParents_hasNoObjectParents() {
		// override navigationService to return no parent, instead of returning some mock CMISObject list of parents
		when(navigationService.getObjectWithParents(mockRepositoryId, mockObjectId)).thenReturn(getCMISObjectWithParents(100));
		
		assertGetObjectParents_emptyResults(getObjectParents(false, IncludeRelationships.NONE));				
		verifyNavigationService_getObjectWithParents();
		verifyRelationshipService_withCmisObject(Collections.<String>emptySet(), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}		
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=false 
	 * IncludeRelationships includeRelationships = IncludeRelationships.NONE
	 * navigationService.getObjectWithParents return CMISObject(id=100,cmisObjectId="cmisId:100"
	 * 														,parents = [CMISObject(id=10,cmisObjectId="cmisId:10"),
	 * 																	CMISObject(id=20,cmisObjectId="cmisId:20")]);
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetObjectParents_allowableActionsFalse() {
		
		assertGetObjectParents_nonEmptyResults(getObjectParents(false, IncludeRelationships.NONE), 
				 											    false, IncludeRelationships.NONE);
		verifyNavigationService_getObjectWithParents();
		verifyRelationshipService_withCmisObject(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "10", CMIS_ID_PREFIX + "20")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);					
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships includeRelationships = IncludeRelationships.BOTH
	 * navigationService.getObjectWithParents return CMISObject(id=100,cmisObjectId="cmisId:100"
	 * 														,parents = [CMISObject(id=10,cmisObjectId="cmisId:10"),
	 * 																	CMISObject(id=20,cmisObjectId="cmisId:20")]);
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetObjectParents_relationshipBOTH() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "10", CMIS_ID_PREFIX + "20"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{10, 20}, new Integer[]{111, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(10, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(20, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "10")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		
		List<ObjectParentData> actualObjectParentData = getObjectParents(true, IncludeRelationships.BOTH);
		
		assertGetObjectParents_nonEmptyResults(actualObjectParentData, true, IncludeRelationships.BOTH);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectParentData.toArray(new ObjectParentData[actualObjectParentData.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getObjectWithParents();
		verifySecurity_getAllowableActions(expectedObjectParents.getParents());			
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.BOTH);					
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships includeRelationships = IncludeRelationships.SOURCE
	 * navigationService.getObjectWithParents return CMISObject(id=100,cmisObjectId="cmisId:100"
	 * 														,parents = [CMISObject(id=10,cmisObjectId="cmisId:10"),
	 * 																	CMISObject(id=20,cmisObjectId="cmisId:20")]);
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetObjectParents_relationshipSOURCE() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "10", CMIS_ID_PREFIX + "20"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{10, 20}, new Integer[]{111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(10, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(20, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "10")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(null);
		
		List<ObjectParentData> actualObjectParentData = getObjectParents(true, IncludeRelationships.SOURCE);
		
		assertGetObjectParents_nonEmptyResults(actualObjectParentData, true, IncludeRelationships.SOURCE);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectParentData.toArray(new ObjectParentData[actualObjectParentData.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getObjectWithParents();
		verifySecurity_getAllowableActions(expectedObjectParents.getParents());			
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.SOURCE);					
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}
		
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships includeRelationships = IncludeRelationships.TARGET
	 * navigationService.getObjectWithParents return CMISObject(id=100,cmisObjectId="cmisId:100"
	 * 														,parents = [CMISObject(id=10,cmisObjectId="cmisId:10"),
	 * 																	CMISObject(id=20,cmisObjectId="cmisId:20")]);
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetObjectParents_relationshipTARGET() {
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "10", CMIS_ID_PREFIX + "20"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{10, 20}, new Integer[]{null, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(10, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(20, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "10")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		
		List<ObjectParentData> actualObjectParentData = getObjectParents(true, IncludeRelationships.TARGET);
		
		assertGetObjectParents_nonEmptyResults(actualObjectParentData, true, IncludeRelationships.TARGET);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectParentData.toArray(new ObjectParentData[actualObjectParentData.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		verifyNavigationService_getObjectWithParents();
		verifySecurity_getAllowableActions(expectedObjectParents.getParents());			
		verifyRelationshipService_withCmisObject(expectedRequestObjectIds, IncludeRelationships.TARGET);					
		verifyNoMoreInteractions(navigationService, security, relationshipService);
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=false
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(mockNode [children = emptyCollection]
	 * 										 ,getDescendands = emptyCollection );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_allowableActionsFalse_emptyTree() {
		
		Tree mockEmptyTree = getMockEmptyTree();		
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, false, false, false)).thenReturn(mockEmptyTree);
		
		assertGetTree_emptyResults(getDescendants(false, IncludeRelationships.NONE));
				
		verifyNavigationService_getTree(false, false);	
		verifyRelationshipService_withNode(Collections.<String>emptySet(), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}		

	/**
	 * scenario 
	 * boolean includeAllowableActions=false
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_allowableActionsFalse() {
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, false, false, false)).thenReturn(expectedTree);
		
		assertGetTree_nonEmptyResults(getDescendants(false, IncludeRelationships.NONE)
														   ,false, IncludeRelationships.NONE);
								
		verifyNavigationService_getTree(false, false);	
		verifyRelationshipService_withNode(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_allowableActionsTrue() {
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, false, false)).thenReturn(expectedTree);
		
		assertGetTree_nonEmptyResults(getDescendants(true, IncludeRelationships.NONE)
														   ,true, IncludeRelationships.NONE);
								
		verifyNavigationService_getTree(true, false);		
		verifySecurity_getAllowableActions(extractCmisObject(expectedTree));
		verifyRelationshipService_withNode(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = BOTH
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_includeRelationshipsBoth() {
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{111, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(Collections.singleton(expectedRelationshipObject));
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, false, false)).thenReturn(expectedTree);
		
		List<ObjectInFolderContainer> actualObjectInFolderContainer = getDescendants(true, IncludeRelationships.BOTH);
		
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.BOTH);
				
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, false);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.BOTH);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = SOURCE
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_includeRelationshipsSource() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(null);
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, false, false)).thenReturn(expectedTree);
		
		
		List<ObjectInFolderContainer> actualObjectInFolderContainer = getDescendants(true, IncludeRelationships.SOURCE);
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.SOURCE);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, false);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.SOURCE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = TARGET
	 * boolean onlyFolders=false -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetDescendants_includeRelationshipsTarget() {
				
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{null, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(Collections.singleton(expectedRelationshipObject));
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, false, false)).thenReturn(expectedTree);
		
		
		List<ObjectInFolderContainer> actualObjectInFolderContainer = getDescendants(true, IncludeRelationships.TARGET);
		
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.TARGET);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, false);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.TARGET);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}

	/**
	 * scenario 
	 * boolean includeAllowableActions=false
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(mockNode [children = emptyCollection]
	 * 										 ,getDescendands = emptyCollection );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_allowableActionsFalse_emptyTree() {
		
		Tree mockEmptyTree = getMockEmptyTree();		
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, false, true, false)).thenReturn(mockEmptyTree);
		
		assertGetTree_emptyResults(getFolderTree(false, IncludeRelationships.NONE));
				
		verifyNavigationService_getTree(false, true);	
		verifyRelationshipService_withNode(Collections.<String>emptySet(), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);		
	}		

	/**
	 * scenario 
	 * boolean includeAllowableActions=false
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(0), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_allowableActionsFalse() {
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, false, true, false)).thenReturn(expectedTree);
		
		assertGetTree_nonEmptyResults(getFolderTree(false, IncludeRelationships.NONE)
														   ,false, IncludeRelationships.NONE);
								
		verifyNavigationService_getTree(false, true);	
		verifyRelationshipService_withNode(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}	
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = NONE
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_allowableActionsTrue() {
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, true, false)).thenReturn(expectedTree);
		
		assertGetTree_nonEmptyResults(getFolderTree(true, IncludeRelationships.NONE)
														   ,true, IncludeRelationships.NONE);
								
		verifyNavigationService_getTree(true, true);		
		verifySecurity_getAllowableActions(extractCmisObject(expectedTree));
		verifyRelationshipService_withNode(new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30")), IncludeRelationships.NONE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = BOTH
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_includeRelationshipsBoth() {
		
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{111, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(Collections.singleton(expectedRelationshipObject));
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, true, false)).thenReturn(expectedTree);
		
	 	List<ObjectInFolderContainer> actualObjectInFolderContainer = getFolderTree(true, IncludeRelationships.BOTH);
		
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.BOTH);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, true);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.BOTH);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = SOURCE
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_includeRelationshipsSource() {
				
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{111, null});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(Collections.singleton(expectedRelationshipObject));
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(null);
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, true, false)).thenReturn(expectedTree);
		
	 	List<ObjectInFolderContainer> actualObjectInFolderContainer = getFolderTree(true, IncludeRelationships.SOURCE);
		
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.SOURCE);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, true);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.SOURCE);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
	
	/**
	 * scenario 
	 * boolean includeAllowableActions=true
	 * IncludeRelationships = TARGET
	 * boolean onlyFolders=true -- hardcoded value--
	 * boolean loadParentsChildren=false 
	 * navigationService.getTree return Tree(node=Node(CMISObject(id=10)) [children = Collection {Node(CMISObject(id=20)), Node(CMISObject(id=30))}]
	 * 										 ,getDescendands = {Node(CMISObject(id=20)), Node(CMISObject(id=30))} );
	 * services : navigationService, security, relationshipService
	 * expected : navigationService(1), security(2), relationshipService(1)
	 */
	@Test
	public void testGetFolderTree_includeRelationshipsTarget() {
				
		Set<String> expectedRequestObjectIds = new HashSet(Arrays.asList(CMIS_ID_PREFIX + "20", CMIS_ID_PREFIX + "30"));
		Map<String, List<String>> expectedObjectRelationshipMapping = getObjectRelationshipMapping(new Integer[]{20, 30}, new Integer[]{null, 111});
		CMISObject expectedRelationshipObject = getExpectedRelationshipObject(111, new RelationshipInfo[] {new RelationshipInfo(20, PropertyIds.SOURCE_ID)
																										, new RelationshipInfo(30, PropertyIds.TARGET_ID)});
						
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "20")).thenReturn(null);
		when(mockRelationshipMappings.get(CMIS_ID_PREFIX + "30")).thenReturn(Collections.singleton(expectedRelationshipObject));
				
		when(navigationService.getTree(mockRepositoryId, mockFolderId, depth, true, true, false)).thenReturn(expectedTree);
		
	 	List<ObjectInFolderContainer> actualObjectInFolderContainer = getFolderTree(true, IncludeRelationships.TARGET);
		
		assertGetTree_nonEmptyResults(actualObjectInFolderContainer, true, IncludeRelationships.TARGET);
		
		Map<String, List<String>> actualObjectRelationshipMapping = getActualObjectRelationshipMapping(actualObjectInFolderContainer.toArray(new ObjectInFolderContainer[actualObjectInFolderContainer.size()]));
		compareAreMeaningfullTheSame(expectedObjectRelationshipMapping, actualObjectRelationshipMapping);
		
		Set<CMISObject> cmisExpectedObjects = extractCmisObject(expectedTree);						
		verifyNavigationService_getTree(true, true);		
		verifySecurity_getAllowableActions(cmisExpectedObjects);
		verifyRelationshipService_withNode(expectedRequestObjectIds, IncludeRelationships.TARGET);
		verifyNoMoreInteractions(navigationService, security, relationshipService);				
	}
					
	private Set<CMISObject> extractCmisObject(Tree expectedTree) {
		Set<CMISObject> cmisObjects = new HashSet();
		for (Node node :expectedTree.getDescendants() ) {
			cmisObjects.add(node.getCmisObject());
		}
		return cmisObjects;
	}

	private ObjectInFolderList getChildren(boolean includeAllowableActions, boolean includePathSegment, IncludeRelationships includeRelationships) {				
		
		return cmisnavigationService.getChildren(mockRepositoryId, mockFolderId, mockFilter
						, mockOrderBy, includeAllowableActions, includeRelationships
						, mockRenditionFilter, includePathSegment, mockMaxItems, mockSkipCount, mockExtension);				
	}

	private void assertGetChildren_emptyResults(ObjectInFolderList actualResult) {
		
		assertNotNull("result shouldnt be NULL", actualResult);
		assertTrue("total result count should be 10", actualResult.getNumItems().intValue() == 10);			
		assertTrue("objects list should be empty", actualResult.getObjects().isEmpty());
	}
	
	private void assertGetChildren_NonEmptyResults(ObjectInFolderList actualResult, boolean includeAllowableActions, boolean includePathSegment, IncludeRelationships includeRelationships) {
		
		assertNotNull("result shouldnt be NULL", actualResult);
		assertTrue("total result count should be 10", actualResult.getNumItems().intValue() == 10);
		List<ObjectInFolderData> actualObjects = actualResult.getObjects();
		assertNotNull("objects list shouldnt be NULL", actualObjects);
		assertTrue("result page should be paginated with value of 2", actualResult.getObjects().size() == 2);
		String[] actualCmisIds = new String[actualResult.getObjects().size()];
		for(int i=0; i<actualResult.getObjects().size(); i++) {
			actualCmisIds[i]  = actualObjects.get(i).getObject().getId();
		}			
		Arrays.sort(actualCmisIds);
		assertTrue("One value should be 157", actualCmisIds[0].equals(CMIS_ID_PREFIX + 157));
		assertTrue("Next value should be 240", actualCmisIds[1].equals(CMIS_ID_PREFIX + 240));
		
		for(int i=0; i<actualCmisIds.length; i++) {	
					
			assertObjectData(actualResult.getObjects().get(i).getObject(), includeAllowableActions, includeRelationships);			
		}		
	}
	
	private ObjectList getCheckedOutDocs(boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		 return cmisnavigationService.getCheckedOutDocs(mockRepositoryId, mockFolderId
						, mockFilter, mockOrderBy, includeAllowableActions, includeRelationships
						, mockRenditionFilter, mockMaxItems, mockSkipCount, mockExtension);
	}
	
	private void assertGetCheckedOutDocs_nonEmptyResults(ObjectList actualObjectList, boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		assertNotNull("result shouldnt be NULL", actualObjectList);
		assertNotNull("total objects shouldnt be NULL", actualObjectList.getObjects());
		assertTrue("result page should be paginated with value of 3", actualObjectList.getObjects().size() == 3);
		String[] actualCmisIds = new String[actualObjectList.getObjects().size()];
		for(int i=0; i<actualObjectList.getObjects().size()  ;i++) {
			actualCmisIds[i]  = actualObjectList.getObjects().get(i).getId();
		}				
		Arrays.sort(actualCmisIds);
		assertTrue("One value should be 12", actualCmisIds[0].equals(CMIS_ID_PREFIX + 12));
		assertTrue("Next value should be 40", actualCmisIds[1].equals(CMIS_ID_PREFIX + 40));
		assertTrue("Next value should be 670", actualCmisIds[2].equals(CMIS_ID_PREFIX + 670));
		
		for(int i =0; i<actualCmisIds.length; i++) {
			assertObjectData(actualObjectList.getObjects().get(i), includeAllowableActions, includeRelationships);
		}	
	}
	
	private void assertObjectData(ObjectData actualObjectData, boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		if(IncludeRelationships.NONE.equals(includeRelationships)) {
			assertTrue(actualObjectData.getRelationships() == null || actualObjectData.getRelationships().isEmpty());								
		} 
		if(!includeAllowableActions) {			
			assertTrue(actualObjectData.getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions().isEmpty());
		}
	}
	
	private void assertGetCheckedOutDocs_emptyResults(ObjectList actualObjectList) {
		
		assertNotNull("result shouldnt be NULL", actualObjectList);
		assertNotNull("total objects shouldnt be NULL", actualObjectList.getObjects());
		assertTrue("total objects should be eempty", actualObjectList.getObjects().isEmpty());
	}
	
	private List<ObjectParentData> getObjectParents(boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		return cmisnavigationService.getObjectParents(mockRepositoryId, "mockObjectId", mockFilter, 
				includeAllowableActions, includeRelationships, mockRenditionFilter, false, mockExtension);
	}
	
	private void assertGetObjectParents_nonEmptyResults(List<ObjectParentData> actualObjectParents, boolean includeAllowableActions, IncludeRelationships includeRelationships) {	
		
		assertNotNull("result shouldnt be NULL", actualObjectParents);
		assertTrue("total result count should be 2", actualObjectParents.size() == 2);					
		String[] actualCmisIds = new String[actualObjectParents.size()];
		for(int i=0; i<actualObjectParents.size(); i++) {
			actualCmisIds[i]  = actualObjectParents.get(i).getObject().getId();
		}			
		Arrays.sort(actualCmisIds);
		assertTrue("One value should be 10", actualCmisIds[0].equals(CMIS_ID_PREFIX + 10));
		assertTrue("Next value should be 20", actualCmisIds[1].equals(CMIS_ID_PREFIX + 20));
		
		for(int i =0; i<actualCmisIds.length; i++) {	
			
			assertObjectData(actualObjectParents.get(i).getObject(), includeAllowableActions, includeRelationships);
		}
	}
	
	private List<ObjectInFolderContainer> getDescendants(boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		return cmisnavigationService.getDescendants(mockRepositoryId, mockFolderId, depth, mockFilter
						, includeAllowableActions, includeRelationships, mockRenditionFilter, false, mockExtension);					
	}
	
	private List<ObjectInFolderContainer> getFolderTree(boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		return cmisnavigationService.getFolderTree(mockRepositoryId, mockFolderId, depth, mockFilter
						, includeAllowableActions, includeRelationships, mockRenditionFilter, true, mockExtension);					
	}
	
	private void assertGetTree_nonEmptyResults(List<ObjectInFolderContainer> actualResult, boolean includeAllowableActions, IncludeRelationships includeRelationships) {
		
		assertNotNull("result shouldnt be NULL", actualResult);
		assertTrue("total result count should be 10", actualResult.size() == 2);
		ObjectInFolderContainer objectInFolderContainer = actualResult.get(0);
		ObjectInFolderData objectInFolderData =  objectInFolderContainer.getObject();
		String id = objectInFolderData.getObject().getId();
		
		String[] actualCmisIds = new String[actualResult.size()];
		for(int i=0; i<actualResult.size() ;i++) {
			actualCmisIds[i]  = actualResult.get(i).getObject().getObject().getId();
		}			
		Arrays.sort(actualCmisIds);
		assertTrue("One value should be 20", actualCmisIds[0].equals(CMIS_ID_PREFIX + 20));
		assertTrue("Next value should be 30", actualCmisIds[1].equals(CMIS_ID_PREFIX + 30));
		
		for(int i =0; i<actualCmisIds.length; i++) {
			assertObjectData(actualResult.get(i).getObject().getObject(), includeAllowableActions, includeRelationships);
		}
	}
	
	private void assertGetTree_emptyResults(List<ObjectInFolderContainer> actualResults) {
		
		assertNotNull("result shouldn't be NULL", actualResults);
		assertTrue("result should be empty", actualResults.isEmpty());
	}
	
	private void assertGetObjectParents_emptyResults(List<ObjectParentData> actualObjectParents) {	
		
		assertNotNull("result shouldnt be NULL", actualObjectParents);
		assertTrue("total result count should be empty", actualObjectParents.isEmpty());	
	}
	
	private void verifyNavigationService_getChildren() {
		
		verify(navigationService, times(1)).getChildren(mockRepositoryId, mockFolderId, mockOrderBy, mockMaxItems.intValue(), mockSkipCount.intValue());		
	}
	
	private void verifyNavigationService_getCheckedOutDocs() {
		
		verify(navigationService, times(1)).getCheckedOutDocs(mockRepositoryId, mockFolderId, mockOrderBy, mockMaxItems.intValue(), mockSkipCount.intValue());		
	}
	
	private void verifyNavigationService_getObjectWithParents() {
		
		verify(navigationService, times(1)).getObjectWithParents(mockRepositoryId, mockObjectId);		
	}
	
	private void verifyNavigationService_getTree(boolean includeAllowableActions, boolean onlyFolders) {
		
		verify(navigationService, times(1)).getTree(mockRepositoryId, mockFolderId, depth, includeAllowableActions, onlyFolders, false);		
	}
	
	private void verifySecurity_getAllowableActions(Set<CMISObject> expectedCmisObjects) {
		
		for(CMISObject expectedCmisObject : expectedCmisObjects) {
			verify(security, times(1)).getAllowableActions(expectedCmisObject);
		}		
	}
	
	private void verifyRelationshipService_withNode(Set<String> expectedRequestObjectIds, IncludeRelationships expectedIncludeRelationship) {
		verify(relationshipService, times(1)).getRelationshipMappings(captorRequestRepository.capture(), captorRequestNodeObjects.capture(), captorRequestIncludeRelationship.capture());
		
		assertEquals(mockRepositoryId, captorRequestRepository.getValue());
		Collection<Node> actualRequestObjects = captorRequestNodeObjects.getValue();
		compareAreMeaningfullTheSame(expectedRequestObjectIds, getObjectIds(actualRequestObjects.toArray(new Node[actualRequestObjects.size()])));
		assertEquals(expectedIncludeRelationship, captorRequestIncludeRelationship.getValue());		
	}
	
	private void verifyRelationshipService_withCmisObject(Set<String> expectedRequestObjectIds, IncludeRelationships expectedIncludeRelationship) {
		verify(relationshipService, times(1)).getRelationshipMappings(captorRequestRepository.capture(), captorRequestCmisObjects.capture(), captorRequestIncludeRelationship.capture());
		
		assertEquals(mockRepositoryId, captorRequestRepository.getValue());
		Set<CMISObject> actualRequestObjects = captorRequestCmisObjects.getValue();
		compareAreMeaningfullTheSame(expectedRequestObjectIds, getObjectIds(actualRequestObjects.toArray(new CMISObject[actualRequestObjects.size()])));
		assertEquals(expectedIncludeRelationship, captorRequestIncludeRelationship.getValue());		
	}
		
	private Set<String> getObjectIds(CMISObject[] cmisObjects) {
		Set<String> objectIds = new HashSet();
		for(CMISObject cmisObject : cmisObjects) {
			objectIds.add(cmisObject.getCmisObjectId());
		}
		return objectIds;
	}

	private Set<String> getObjectIds(Node[] nodes) {
		Set<String> objectIds = new HashSet();
		for(Node node : nodes) {
			objectIds.add(node.getCmisObject().getCmisObjectId());
		}
		return objectIds;
	}
	
	private Page<CMISObject> getPage(int pageSize, CMISObject ... cmisObjects) {
		
		return new Page(getObjectResults(cmisObjects), pageSize); 
	}
	
	private Set<CMISObject> getObjectResults(CMISObject ... cmisObjects) {
		
		Set<CMISObject> results = new LinkedHashSet();
		for (CMISObject cmisObject : cmisObjects) {
			results.add(cmisObject);
		}
		return results;
		
	}
	
	private CMISObject getCMISObject(int id) {
		
		CMISObject object = new CMISObject();
		object.setId(id);
		object.setCmisObjectId(CMIS_ID_PREFIX +id);
		object.setObjectType(getObjectType());				
		return object;
	}
	
	/**
	 * @param objectId
	 * @param relationshipObjectIds
	 * @return CMISObject relationship object
	 */
	private CMISObject getExpectedRelationshipObject(Integer objectId, RelationshipInfo[] relationshipInfos) {
		int objectTypePropertyId = 0;
		CMISObject relationshipObject = getCMISObject(objectId);
		for (RelationshipInfo relationshipInfo : relationshipInfos) {			
			//not important objectTypePropertyId value, so just generating some unique values			
			relationshipObject.addProperty(getPropertyModel(++objectTypePropertyId, relationshipInfo.getPropertyId(), relationshipInfo.getRelationshipObjectId()));								
		}
		return relationshipObject;
	}
	
	class RelationshipInfo {
	 	private Integer relationshipObjectId;
	 	private  String propertyId;
	 	
	 	RelationshipInfo(Integer relationshipObjectId, String propertyId) {
	 		setRelationshipObjectId(relationshipObjectId);
	 		setPropertyId(propertyId);
	 	}
	 	
		public Integer getRelationshipObjectId() {
			return relationshipObjectId;
		}
		public void setRelationshipObjectId(Integer relationshipObjectId) {
			this.relationshipObjectId = relationshipObjectId;
		}

		public String getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(String propertyId) {
			this.propertyId = propertyId;
		}				
		 	
	}	
	
	private Property getPropertyModel(Integer objectTypePropertyId, String targetOrSourceCmisId, Integer relationshipObjectId) {
		
		return new Property(getObjectTypePropertyModel(objectTypePropertyId, targetOrSourceCmisId), CMIS_ID_PREFIX + relationshipObjectId);		
	}

	/**
	 * 
	 * @param objectTypePropertyId
	 * @param targetOrSourceCmisId can take one from this enum values {PropertyIds.SOURCE_ID(cmis:sourceId), PropertyIds.TARGET_ID(cmis:targetId)}
	 * @return
	 */
	private ObjectTypeProperty getObjectTypePropertyModel(Integer objectTypePropertyId, String targetOrSourceCmisId) {
		ObjectTypeProperty objectTypeProperty = new ObjectTypeProperty();	
		objectTypeProperty.setCardinality(Cardinality.SINGLE);
		objectTypeProperty.setPropertyType(PropertyType.STRING);
		objectTypeProperty.setId(objectTypePropertyId);
		objectTypeProperty.setCmisId(targetOrSourceCmisId);
		return objectTypeProperty;
	}	
	
	private CMISObject getCMISObjectWithParents(int objectId, int ... parentIds) {
		
		CMISObject cmisObject = getCMISObject(objectId);
		for(Integer parentId : parentIds) {					
			cmisObject.addParent(getCMISObject(parentId));
		}		
		return cmisObject;
	}
	
	private ObjectType getObjectType() {
		
		ObjectType objectType = new ObjectType();
		objectType.setObjectTypeProperties(new TreeSet());
		objectType.setCmisId(TYPE_CMIS_DOCUMENT);
		objectType.setBase(objectType);
		return objectType;
	}
	
	private Tree getMockEmptyTree() {
		
		Tree mockTree = mock(Tree.class);
	 	Node mockNode = getMockEmptyNode();
		when(mockTree.getDescendants()).thenReturn(Collections.<Node>emptySet());
		when(mockTree.getRoot()).thenReturn(mockNode);
		return mockTree;		
	}
	
	private Node getMockEmptyNode() {
		
		Node mockNode = mock(Node.class);
		when(mockNode.getChildren()).thenReturn(Collections.<Node>emptyList());
		return mockNode;
	}
	
	private Tree getTree() {
		Tree tree = new Tree(getCMISObject(10), getObjectParents(10, 20, 30));
		return tree;
	}

	private Set<ObjectParent> getObjectParents(int parentId,int ... childIds) {
		
		Set<ObjectParent> objectParents = new HashSet();
		for(int childId : childIds) {
			objectParents.add(getObjectParent(parentId, childId));
		}
		return objectParents;		
	}

	private ObjectParent getObjectParent(int parentId, int childId) {
		ObjectParent mockObjectParent = mock(ObjectParent.class);
		when(mockObjectParent.getCmisObject()).thenReturn(getCMISObject(childId));	
		when(mockObjectParent.getObjectParentId()).thenReturn(getObjectParentId(parentId, childId));
		return mockObjectParent;
	}

	private ObjectParentId getObjectParentId(int parentId, int childId) {
		ObjectParentId objectParentId = new ObjectParentId();
		objectParentId.setParentId(parentId);
		objectParentId.setObjectId(childId);
		return objectParentId;
	}
	
	private void compareAreMeaningfullTheSame(Map<String, List<String>> expectedObjectRelationshipMapping, Map<String, List<String>> actualObjectRelationshipMapping) {
		assertTrue(expectedObjectRelationshipMapping.size() == actualObjectRelationshipMapping.size());
		for(String expectedkey : expectedObjectRelationshipMapping.keySet()) {
			assertTrue(actualObjectRelationshipMapping.containsKey(expectedkey));
			List<String> expectedRelationshipObjects = expectedObjectRelationshipMapping.get(expectedkey);
			List<String> actualRelationshipObjects = actualObjectRelationshipMapping.get(expectedkey);
			assertTrue(expectedRelationshipObjects.size() == actualRelationshipObjects.size());
			for (String expectedRelationshipObject : expectedRelationshipObjects) {
				assertTrue(actualRelationshipObjects.contains(expectedRelationshipObject));
			}
		}
		
	}
	
	private void compareAreMeaningfullTheSame(Set<String> expectedRequestObjectIds, Set<String> actualRequestObjectIds) {
		assertEquals(expectedRequestObjectIds.size(), actualRequestObjectIds.size());
		for(String expectedRequestObjectId : expectedRequestObjectIds) {
			assertTrue(actualRequestObjectIds.contains(expectedRequestObjectId));
		}
	}

	private Map<String, List<String>> getActualObjectRelationshipMapping(ObjectInFolderList actualObjectInFolderList) {
		Map<String, List<String>> actualObjectRelationshipMapping = new HashMap();
		for (ObjectInFolderData actualObjectInFolderData : actualObjectInFolderList.getObjects()) {
			ObjectData actualObjectData = actualObjectInFolderData.getObject();			
			actualObjectRelationshipMapping.put(actualObjectData.getId(), new ArrayList());
			for(ObjectData actualRelationshipObject : actualObjectData.getRelationships()) {
				actualObjectRelationshipMapping.get(actualObjectData.getId()).add(actualRelationshipObject.getId());
			}
		}
		return actualObjectRelationshipMapping;
	}
	
	private <T>  Map<String, List<String>> getActualObjectRelationshipMapping(ObjectInFolderContainer[] actualObjectInFolderContainer) {
		Map<String, List<String>> actualObjectRelationshipMapping = new HashMap();
		for(ObjectInFolderContainer objectInFolderContainer : actualObjectInFolderContainer) {
			ObjectData actualObjectData = objectInFolderContainer.getObject().getObject();
			actualObjectRelationshipMapping.put(actualObjectData.getId(), new ArrayList());
			for(ObjectData actualRelationshipObject : actualObjectData.getRelationships()) {
				actualObjectRelationshipMapping.get(actualObjectData.getId()).add(actualRelationshipObject.getId());
			}
		}
		return actualObjectRelationshipMapping;		
	}
	
	private Map<String, List<String>> getActualObjectRelationshipMapping(ObjectList actualObjectList) {
		Map<String, List<String>> actualObjectRelationshipMapping = new HashMap();
		for (ObjectData actualObjectData : actualObjectList.getObjects()) {					
			actualObjectRelationshipMapping.put(actualObjectData.getId(), new ArrayList());
			for(ObjectData actualRelationshipObject : actualObjectData.getRelationships()) {
				actualObjectRelationshipMapping.get(actualObjectData.getId()).add(actualRelationshipObject.getId());
			}
		}
		return actualObjectRelationshipMapping;
	}
	
	private Map<String, List<String>> getActualObjectRelationshipMapping(ObjectParentData[] actualObjectParentData) {
		Map<String, List<String>> actualObjectRelationshipMapping = new HashMap();
		for (ObjectParentData objectParentData : actualObjectParentData) {
			ObjectData actualObjectData = objectParentData.getObject();			
			actualObjectRelationshipMapping.put(actualObjectData.getId(), new ArrayList());
			for(ObjectData actualRelationshipObject : actualObjectData.getRelationships()) {
				actualObjectRelationshipMapping.get(actualObjectData.getId()).add(actualRelationshipObject.getId());
			}
		}
		return actualObjectRelationshipMapping;
	}
	

}
