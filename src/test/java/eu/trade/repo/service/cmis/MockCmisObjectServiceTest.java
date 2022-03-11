package eu.trade.repo.service.cmis;

import static eu.trade.repo.util.Constants.TYPE_CMIS_DOCUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.junit.Before;
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
import eu.trade.repo.model.Rendition;
import eu.trade.repo.security.CallContextHolder;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.out.PropertiesBuilder;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.service.util.Page;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/mock/testApplicationContext-codecs.xml")
public class MockCmisObjectServiceTest {

	@InjectMocks
	private CmisObjectService cmisObjectService;
	
	@Mock
	private IObjectService objectService;	
	
	@Mock
	private IRelationshipService relationshipService;

	@Mock
	private Security security;
	
	private static final String PROPERTY_VALUE_PREFIX = "VALUE";
	private static final String RENDITION_STREAM_PREFIX = "STREAM";
	private static final String MOCK_PERMISSIONNAME_PREFIX = "mockPermissionNamePrefix";
		
	final private String mockUsername = "testUserSilviu";
	final private String mockRepositoryId = "mockRepositoryId";
	final private String mockFolderId = "mockFolderId";
	final private String mockSourceId = "mockSourceId";
	private ContentStream mockContentStream = mock(ContentStream.class);
	private VersioningState mockVersioningState = VersioningState.NONE;	 
	private List<String> mockPolicies = mock(List.class);
	private Acl mockAddAces = mock(Acl.class);
	private Acl mockRemoveAcces = mock(Acl.class);
 	private ExtensionsData mockExtension = mock(ExtensionsData.class);
 	 	
 	@Captor
 	private ArgumentCaptor<CMISObject> actualCaptorCmisObject;
	
 	@Captor
	private ArgumentCaptor<BaseTypeId> actualCaptorBaseTypeId;
 	
 	@Captor
 	private ArgumentCaptor<String> actualCaptorRepositoryId;
 	
	@Before
	public void autowireDependencies() {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * input 	: nullProperties 
	 * scenario : run cmisObjectService.createDocument
	 * expected : CMISObjectBuilder.build method is throwing : CmisInvalidArgumentException
	 */	
	@Test
	public void testCreateDocument_nullProperties() {
		try {
			//runnig the scenario
			cmisObjectService.createDocument(mockRepositoryId, getNullProperties(), mockFolderId, mockContentStream, mockVersioningState, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);			
			fail("cmisObjectService.createDocument should have thrown a CmisInvalidArgumentException exception");
		} catch (CmisInvalidArgumentException cmisInvalidArgumentException) {
			//assertion & verify interactions
			String expectedMessageException = "Properties must be set!";
			String actualMessageException = cmisInvalidArgumentException.getMessage();
			assertEquals(expectedMessageException, actualMessageException);
			verifyNoMoreInteractions(security, objectService, relationshipService);
		}
	}
	
	/**
	 *  input 	: mocking services in a positive scenario, then overriding the interested one
	 * 	 	    : emptyProperties 
	 * scenario : objectService.createObject return an CMISObject as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response is the same as objectService is returning
	 */	
	@Test
	public void testCreateDocument_emptyProperties() {
					
		final String expectedDocumentId = "expectedDocumentId";		
		preparePositiveScenario_createObject(expectedDocumentId);
		
		//runnig the scenario
		String actualDocumentId = cmisObjectService.createDocument(mockRepositoryId, getEmptyProperties(), mockFolderId, null, null, null, null, null, null);
		
		//assertion & verify interactions
		verifyPositiveScenario_createDocument(expectedDocumentId, actualDocumentId, new Integer[]{});		
	}		

	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one
	 * 	 	    : properties = {1 token}
	 * scenario : objectService.createObject return an CMISObject as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response is the same as objectService is returning
	 */	
	@Test
	public void testCreateDocument_properties_1token() {
		final Integer[] expectedProperties = new Integer[] {new Integer(1)}; 
		final String expectedDocumentId = "expectedDocumentId";		
		preparePositiveScenario_createObject(expectedDocumentId);			
				
		//runnig the scenario
		String actualDocumentId = cmisObjectService.createDocument(mockRepositoryId, getProperties(Cardinality.SINGLE, expectedProperties), mockFolderId, mockContentStream, VersioningState.NONE, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assertion & verify interactions
		verifyPositiveScenario_createDocument(expectedDocumentId, actualDocumentId, expectedProperties);						
	}	
	
	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one
	 * 	 	    : properties = {4 tokens} 
	 * scenario : objectService.createObject return an CMISObject as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response is the same as objectService is returning
	 */	
	@Test
	public void testCreateDocument_properties_4tokens() {
		final Integer[] expectedProperties = new Integer[] {new Integer(1), new Integer(19), new Integer(6), new Integer(13)}; 
		final String expectedDocumentId = "expectedDocumentId";		
		preparePositiveScenario_createObject(expectedDocumentId);			
				
		//runnig the scenario
		String actualDocumentId = cmisObjectService.createDocument(mockRepositoryId, getProperties(Cardinality.MULTI, expectedProperties), mockFolderId, mockContentStream, VersioningState.NONE, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assertion & verify interactions
		verifyPositiveScenario_createDocument(expectedDocumentId, actualDocumentId, expectedProperties);						
	}

	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one 
	 * scenario : objectService.createObject return a NULL as response (this is not possible.. so it's ok)
	 * 			  run cmisObjectService.createDocument
	 * expected : the code will throw a NullPointerException 
	 */	
	@Test
	public void testCreateDocument_objectServiceCreateObject_ReturnObjectWithoutCmisId() {
		final Integer[] expectedProperties = new Integer[] {new Integer(1)}; 	
		preparePositiveScenario_createObject("expectedDocumentId");			
				
		CMISObject expectedNullObject = null;
		//scenario: now we override the positive scenario, with a negative one for objectService.createObject
		when(objectService.createObject(anyString(), any(CMISObject.class), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), any(BaseTypeId.class))).thenReturn(expectedNullObject);
		
		try {
			//runnig the scenario
			cmisObjectService.createDocument(mockRepositoryId, getProperties(Cardinality.SINGLE, expectedProperties), mockFolderId, null, null, null, null, null, null);
			fail("the code is throwing a NullPointerException NOW");
		} catch(NullPointerException nullPointerException) {
			//assertion & verify interactions
			verifyAllServicesScenario_createDocument(expectedProperties);
		}							
	}
	
	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one 
	 * scenario : security.getPermissionNames throw an RuntimeException exception as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response should have been propagated the same RuntimeException exception type from security.getPermissionNames
	 */	
	@Test
	public void testCreateDocument_securityGetPermissionNames_ThrowRuntimeException() {
				
		preparePositiveScenario_createObject("expectedDocumentId");
						
		//scenario: now we override the positive scenario, with a negative one for security.getPermissionNames
		Exception expectedSecurityException = new RuntimeException("message");
		when(security.getPermissionNames(anyString())).thenThrow(expectedSecurityException);
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocument(expectedSecurityException, getEmptyProperties());
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verifyNoMoreInteractions(security, objectService, relationshipService);
		
	}	
	
	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one 
	 * scenario : security.getPermissionNames throw a SecurityException exception as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response should have been propagated the same SecurityException exception type from security.getPermissionNames
	 */	
	@Test
	public void testCreateDocument_securityGetPermissionNames_ThrowSecurityException() {
				
		preparePositiveScenario_createObject("expectedDocumentId");
						
		//scenario: now we override the positive scenario, with a negative one for security.getPermissionNames
		Exception expectedSecurityException = new SecurityException("message");
		when(security.getPermissionNames(anyString())).thenThrow(expectedSecurityException);				
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocument(expectedSecurityException, getEmptyProperties());
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one 
	 * scenario : security.getCallContextHolder throw a SecurityException exception as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response should have been propagated the same SecurityException exception type from security.getCallContextHolder
	 */	
	@Test
	public void testCreateDocument_securityGetCallContextHolder_ThrowSecurityException() {
				
		preparePositiveScenario_createObject("expectedDocumentId");
						
		//scenario: now we override the positive scenario, with a negative one for security.getCallContextHolder
		Exception expectedSecurityException = new SecurityException("message");
		when(security.getCallContextHolder()).thenThrow(expectedSecurityException);				
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocument(expectedSecurityException, getEmptyProperties());
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario, then overriding the interested one
	 * scenario : objectService.createObject throw a CmisConstraintException exception as response
	 * 			  run cmisObjectService.createDocument
	 * expected : response should have been propagated the same CmisConstraintException exception type from objectService.createObject
	 */	
	@Test
	public void testCreateDocument_objectServiceGetCallContextHolder_ThrowSomeException() {
	
		preparePositiveScenario_createObject("expectedDocumentId");
						
		//scenario: now we override the positive scenario, with a negative one for objectService.createObject
		Exception expectedObjectServiceException = new CmisConstraintException("message");
		when(objectService.createObject(anyString(), any(CMISObject.class), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), any(BaseTypeId.class))).thenThrow(expectedObjectServiceException);				
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocument(expectedObjectServiceException, getEmptyProperties());
		//verify interactions
		verifyAllServicesScenario_createDocument(new Integer[] {} );
	}
	
	/**
	 * input 	: mocking objectService.getObject method
	 * scenario : objectService.getObject throw a CmisUnauthorizedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisConstraintException exception type from objectService.getObject
	 */	
	@Test
	public void testCreateDocumentFromSource_objectServiceGetObject_throwSomeException() {			
		
		Exception expectedObjectServiceException = new CmisUnauthorizedException("mesage");
		when(objectService.getObject(anyString(), anyString())).thenThrow(expectedObjectServiceException);
		
		//runnig7asserting the scenario
		runAndAssertExceptionScenario_createDocumentFromSource(expectedObjectServiceException);
		//verify interactions
		verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking objectService.getContentStream method
	 * scenario : objectService.getContentStream throw a CmisUnauthorizedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisConstraintException exception type from objectService.getContentStream
	 */	
	@Test
	public void testCreateDocumentFromSource_objectServiceGetContentStream_throwSomeException() {
		
		Exception expectedObjectServiceException = new CmisUnauthorizedException("mesage");
		when(objectService.getContentStream(anyString(), anyString())).thenThrow(expectedObjectServiceException);
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocumentFromSource(expectedObjectServiceException);
		//verify interactions
		verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
		verify(objectService, times(1)).getContentStream(mockRepositoryId, mockSourceId);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : security.getPermissionNames throw a CmisUnauthorizedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisConstraintException exception type from security.getPermissionNames
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateDocumentFromSource_securityGetPermissionNames_throwSomeException() throws CloneNotSupportedException {
		
		preparePositiveScenario_createDocumentFromSource("expectedDocumentId");
		
		Exception expectedSecurityException = new CmisUnauthorizedException("mesage");
		//scenario: now we override the positive scenario, with a negative one for security.getPermissionNames
		when(security.getPermissionNames(anyString())).thenThrow(expectedSecurityException);
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocumentFromSource(expectedSecurityException);
		//verify interactions
		verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
		verify(objectService, times(1)).getContentStream(mockRepositoryId, mockSourceId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : security.getCallContextHolder() throw a CmisUnauthorizedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisConstraintException exception type from security.getCallContextHolder()
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateDocumentFromSource_securityGetCallContextHolder_throwSomeException() throws CloneNotSupportedException {
		
		preparePositiveScenario_createDocumentFromSource("expectedDocumentId");
		
		Exception expectedSecurityException = new CmisUnauthorizedException("mesage");
		//scenario: now we override the positive scenario, with a negative one for security.getPermissionNames
		when(security.getCallContextHolder()).thenThrow(expectedSecurityException);
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocumentFromSource(expectedSecurityException);
		//verify interactions
		verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
		verify(objectService, times(1)).getContentStream(mockRepositoryId, mockSourceId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : mockSource.clone() throw a CloneNotSupportedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisInvalidArgumentException exception type 
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateDocumentFromSource_cmisClone_throwCloneNotSupportedException() throws CloneNotSupportedException {
		
		preparePositiveScenario_createDocumentFromSource("expectedDocumentId");
		
		CMISObject mockSource = mock(CMISObject.class);
		//scenario: now we override the positive scenario, with a negative one for security.getObject & cloning part
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockSource);
		when(mockSource.clone()).thenThrow(new CloneNotSupportedException());
				
		try {
			//running the scenario
			cmisObjectService.createDocumentFromSource(mockRepositoryId, mockSourceId, getEmptyProperties(), mockFolderId, mockVersioningState, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
			fail("Create document should have thrown an Exception");
		} catch(CmisInvalidArgumentException cmisInvalidArgumentException) {
			//so the first CloneNotSupportedException is reverted to CmisInvalidArgumentException exception
			verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
			verify(objectService, times(1)).getContentStream(mockRepositoryId, mockSourceId);		
			verifyNoMoreInteractions(security, objectService, relationshipService);
		}
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : objectService.createObject throw a CmisUnauthorizedException exception as response
	 * 			  run cmisObjectService.createDocumentFromSource
	 * expected : response should have been propagated the same CmisConstraintException exception type from security.getCallContextHolder()
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateDocumentFromSource_objectService_throwSomeException() throws CloneNotSupportedException {
		
		preparePositiveScenario_createDocumentFromSource("expectedDocumentId");
		
		Exception expectedSecurityException = new CmisUnauthorizedException("mesage");
		//scenario: now we override the positive scenario, with a negative one for objectService.createObject
		when(objectService.createObject(anyString(), any(CMISObject.class), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), any(BaseTypeId.class))).thenThrow(expectedSecurityException);
		
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createDocumentFromSource(expectedSecurityException);
		//verify interactions	
		verifyAllServicesScenario_createDocumentFromSource(new Integer[] {});
		
	}			

	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : run cmisObjectService.createDocumentFromSource
	 * expected : response should contain the documentId from the objectService
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateDocumentFromSource() throws CloneNotSupportedException {
		final String expectedDocumentId = "expectedDocumentId";
		preparePositiveScenario_createDocumentFromSource(expectedDocumentId);		
				
		//runnig the scenario
		String actualDocumentId = cmisObjectService.createDocumentFromSource(mockRepositoryId, mockSourceId, getEmptyProperties(), mockFolderId, mockVersioningState, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		//assert&verify interactions
		assertEquals(expectedDocumentId, actualDocumentId);		
		verifyAllServicesScenario_createDocumentFromSource(new Integer[] {});
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : run cmisObjectService.createFolder
	 * expected : response should contain the folderId from the objectService
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateFolder() throws CloneNotSupportedException {
		final String expectedFolderId = "expectedFolderId";
		preparePositiveScenario_createObject(expectedFolderId);		
				
		//runnig the scenario
		String actualFolderId = cmisObjectService.createFolder(mockRepositoryId, getEmptyProperties(), mockFolderId, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedFolderId, actualFolderId);			
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createObject(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), actualCaptorBaseTypeId.capture());
		verifyCapture_createObject(new Integer[] {}, BaseTypeId.CMIS_FOLDER);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : run cmisObjectService.createPolicy
	 * expected : response should contain the policyId from the objectService
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreatePolicy() throws CloneNotSupportedException {
		final String expectedPolicyId = "expectedPolicyId";
		preparePositiveScenario_createObject(expectedPolicyId);		
				
		//runnig the scenario
		String actualPolicyId = cmisObjectService.createPolicy(mockRepositoryId, getEmptyProperties(), mockFolderId, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedPolicyId, actualPolicyId);			
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createObject(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), actualCaptorBaseTypeId.capture());
		verifyCapture_createObject(new Integer[] {}, BaseTypeId.CMIS_POLICY);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : run cmisObjectService.createItem
	 * expected : response should contain the itemId from the objectService
	 * @throws CloneNotSupportedException 
	 */	
	@Test
	public void testCreateItem() throws CloneNotSupportedException {
		final String expectedItemId = "expectedItemId";
		preparePositiveScenario_createObject(expectedItemId);		
				
		//runnig the scenario
		String actualItemId = cmisObjectService.createItem(mockRepositoryId, getEmptyProperties(), mockFolderId, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedItemId, actualItemId);			
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createObject(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), actualCaptorBaseTypeId.capture());
		verifyCapture_createObject(new Integer[] {}, BaseTypeId.CMIS_ITEM);
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should contain the documentId from the objectService 
	 */
	@Test
	public void testCreateRelationship_emptyProperties() {
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		//runnig the scenario
		String actualRelationshipId = cmisObjectService.createRelationship(mockRepositoryId, getEmptyProperties(), mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedMockCmisObjectId, actualRelationshipId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createRelationship(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), anyList());
		verifyCapture_createRelationship(new Integer[] {});
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * 			: properties {1token}
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should contain the documentId from the objectService 
	 */
	@Test
	public void testCreateRelationship_properties_1token() {
		final Integer[] expectedProperties = new Integer[] {new Integer(1)}; 
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		//runnig the scenario
		String actualRelationshipId = cmisObjectService.createRelationship(mockRepositoryId, getProperties(Cardinality.SINGLE, expectedProperties), mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedMockCmisObjectId, actualRelationshipId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createRelationship(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), anyList());
		verifyCapture_createRelationship(expectedProperties);
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * 			: properties {3tokens}
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should contain the relationshipId from the objectService 
	 */
	@Test
	public void testCreateRelationship_properties_3tokens() {
		final Integer[] expectedProperties = new Integer[] {new Integer(1), new Integer(2), new Integer(9)}; 
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		//runnig the scenario
		String actualRelationshipId = cmisObjectService.createRelationship(mockRepositoryId, getProperties(Cardinality.MULTI, expectedProperties), mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
		
		//assert&verify interactions
		assertEquals(expectedMockCmisObjectId, actualRelationshipId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createRelationship(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), anyList());
		verifyCapture_createRelationship(expectedProperties);
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * 			: overriding security service to throw an Exception
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should receive the same Exception (type, message) from security service layer 
	 */
	@Test
	public void testCreateRelationship_securityGetPermissionNames_Exception() {
		
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		Exception securityException = new RuntimeException("this message shouldn't be modified");
		when(security.getPermissionNames(anyString())).thenThrow(securityException);
				
		//runnig&asserting the scenario
		runAndAssertExceptionScenario_createRelationship(securityException, getEmptyProperties());
		
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);		
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * 			: overriding security service to throw an Exception
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should receive the same Exception (type, message) from security service layer 
	 */
	@Test
	public void testCreateRelationship_securityGetCallContextHolder_Exception() {
		
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		Exception securityException = new RuntimeException("this message shouldn't be modified");
		when(security.getCallContextHolder()).thenThrow(securityException);
				
		//runnig the scenario
		runAndAssertExceptionScenario_createRelationship(securityException, getEmptyProperties());
		
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);		
		verify(security, times(1)).getCallContextHolder();
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}	
	
	/**
	 * input 	: mocking services in a positive scenario 
	 * 			: overriding security service to throw an Exception
	 * scenario : run cmisObjectService.createRelationship
	 * expected : response should receive the same Exception (type, message) from security service layer 
	 */
	@Test
	public void testCreateRelationship_objectService_Exception() {
		
		final String expectedMockCmisObjectId = "mockExpectedCmisObjectId";
		preparePositiveScenario_createRelationship(expectedMockCmisObjectId);
		
		Exception objectException = new RuntimeException("this message shouldn't be modified");
		when(objectService.createRelationship(anyString(), any(CMISObject.class), anySet(), anySet(), anyList())).thenThrow(objectException);
				
		//runnig the scenario
		runAndAssertExceptionScenario_createRelationship(objectException, getEmptyProperties());
		
		//verify interactions
		verify(security, times(1)).getPermissionNames(mockRepositoryId);		
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createRelationship(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), anyList());
		verifyCapture_createRelationship(new Integer[]{});
		verifyNoMoreInteractions(security, relationshipService, objectService);		
	}
	
	/**
	 * input 	: 
	 * 			: overriding security service to throw an Exception
	 * scenario : run cmisObjectService.getAllowableActions
	 * expected : response should receive the same Exception (type, message) from security service layer 
	 */
	@Test
	public void testGetAllowableActions_securityThrowException() {
		final String mockObjectId = "mockObjectId";
		Exception securityException = new RuntimeException("message"); 
		when(security.getAllowableActions(anyString())).thenThrow(securityException);
		
		try {
			cmisObjectService.getAllowableActions(mockRepositoryId, mockObjectId, mockExtension);
			fail("actually cmisObjectService should have thrown an Exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(securityException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(securityException.getClass(), cmisObjectServiceException.getClass());
			verify(security, times(1)).getAllowableActions(mockObjectId);
			verifyNoMoreInteractions(security, relationshipService, objectService);
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking security in a positive manner
	 * scenario : run cmisObjectService.getAllowableActions
	 * expected : response should receive allowableActions from the services
	 */
	@Test
	public void testGetAllowableActions() {
		final String mockObjectId = "mockObjectId"; 
		AllowableActions expectedMockAllowableActions = mock(AllowableActions.class);
		when(security.getAllowableActions(anyString())).thenReturn(expectedMockAllowableActions);
			
		AllowableActions actualAllowableActions = cmisObjectService.getAllowableActions(mockRepositoryId, mockObjectId, mockExtension);
		assertSame(expectedMockAllowableActions, actualAllowableActions);		
		verify(security, times(1)).getAllowableActions(mockObjectId);
		verifyNoMoreInteractions(security, relationshipService, objectService);	
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService throw Exception
	 * scenario : run cmisObjectService.getProperties
	 * expected : response should get the same execption
	 */
	@Test
	public void testGetProperties_objectGetPropertiesException() {
		final String mockObjectId = "mockObjectId";
		Exception objectException = new RuntimeException("just a message");
		when(objectService.getObject(anyString(), anyString())).thenThrow(objectException);
		
		try {
			cmisObjectService.getProperties(mockRepositoryId, mockObjectId, null, mockExtension);
			fail("cmisObjectService.getProperties should have been raised an exception");
		} catch (Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);
			verifyNoMoreInteractions(objectService, relationshipService, security);
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking services
	 * scenario : run cmisObjectService.getProperties
	 * expected : response should get an empty Properties..
	 */
	@Test
	public void testGetProperties_objectServiceReturnEmpty() {
		final String mockObjectId = "mockObjectId";
		CMISObject mockObject = mock(CMISObject.class);
		ObjectType mockObjectType = mock(ObjectType.class);
		when(mockObject.getObjectType()).thenReturn(mockObjectType);
		Map<String, SortedSet<ObjectTypeProperty>> mockObjectTypeProperties = Collections.emptyMap();
		when(mockObjectType.getObjectTypePropertiesIncludeParents(anyBoolean(), anyBoolean())).thenReturn(mockObjectTypeProperties);
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObject);
		
		Properties actualProperties = cmisObjectService.getProperties(mockRepositoryId, mockObjectId, null, mockExtension);
		
		assertNotNull(actualProperties);
		assertTrue(actualProperties.getProperties().isEmpty());
		verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);
		verifyNoMoreInteractions(objectService, relationshipService, security);		
	}	
	
	@Test
	public void testGetProperties_objectService_return1token() {
		doTestGetProperties( new Integer[] {new Integer(1)});
	}
	
	@Test
	public void testGetProperties_objectService_return3tokens() {
		doTestGetProperties( new Integer[] {new Integer(189), new Integer(2240), new Integer(1006)});
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService throw Exception
	 * scenario : run cmisObjectService.getRenditions
	 * expected : response should get the same execption
	 */
	@Test
	public void testGetRenditions_objectServiceThrowException() {
		final String mockObjectId = "mockObjectId";
		Exception objectException = new RuntimeException("error message");
		when(objectService.getRenditions(anyString(), anyString(), anyInt(), anyInt())).thenThrow(objectException);
				
		try {
			cmisObjectService.getRenditions(mockRepositoryId, mockObjectId, null, new BigInteger("-1"), new BigInteger("0"), mockExtension);
			fail("cmisObjectService.getRenditions should have been raised an exception");
		} catch (Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getRenditions(mockRepositoryId, mockObjectId, -1, 0);
			verifyNoMoreInteractions(objectService, relationshipService, security);
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService get empty RenditionList
	 * scenario : run cmisObjectService.getRenditions
	 * expected : response should get an empty List
	 */
	@Test
	public void testGetRenditions_objectService_emptyRenditions() {
		final String mockObjectId = "mockObjectId";
		
		when(objectService.getRenditions(anyString(), anyString(), anyInt(), anyInt())).thenReturn(getEmptyRenditionList());
						
		List<RenditionData> actualRenditions = cmisObjectService.getRenditions(mockRepositoryId, mockObjectId, null, new BigInteger("-1"), new BigInteger("0"), mockExtension);
		
		//assert&verify
		assertNotNull(actualRenditions);
		assertTrue(actualRenditions.isEmpty());
		verify(objectService, times(1)).getRenditions(anyString(), anyString(), anyInt(), anyInt() );
		
	}
	
	@Test
	public void testGetRenditions_objectService_rendition1token() {
		doTestGetRendition( new Integer[] {new Integer(12)});
				
	}
	
	@Test
	public void testGetRenditions_objectService_rendition6token() {
		doTestGetRendition( new Integer[] {new Integer(80), new Integer(75), new Integer(12), new Integer(15), new Integer(2), new Integer(123)});
				
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService throw Exception
	 * scenario : run cmisObjectService.deleteObject
	 * expected : response should get the same execption
	 */
	@Test
	public void testDeleteObject_objectServiceThrowException() {
		final String mockObjectId = "mockObjectId";
		Exception objectException = new RuntimeException("some message");
		doThrow(objectException).when(objectService).deleteObject(anyString(), anyString(), anyBoolean());
		
		try {
			cmisObjectService.deleteObject(mockRepositoryId, mockObjectId, true, mockExtension);
			fail("cmisObjectService.deleteObject should have been thrown an exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).deleteObject(mockRepositoryId, mockObjectId, true);
			verifyNoMoreInteractions(objectService, relationshipService, security);
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to run with success
	 * scenario : run cmisObjectService.deleteObject
	 * expected : response should run without any error
	 */
	@Test
	public void testDeleteObject_objectServiceOk() {
		final String mockObjectId = "mockObjectId";		
		doNothing().when(objectService).deleteObject(anyString(), anyString(), anyBoolean());
		
		cmisObjectService.deleteObject(mockRepositoryId, mockObjectId, true, mockExtension);
	
		verify(objectService, times(1)).deleteObject(mockRepositoryId, mockObjectId, true);
		verifyNoMoreInteractions(objectService, relationshipService, security);
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to throw an Exception
	 * scenario : run cmisObjectService.moveObject
	 * expected : response should get the same exception
	 */
	@Test
	public void testMoveObject_objectServiceThrowException() {
		Holder<String> mockHolderObject = mock(Holder.class);
		when(mockHolderObject.getValue()).thenReturn("mockObjectValue");
		Exception objectException = new RuntimeException("some err message"); 
		doThrow(objectException).when(objectService).moveObject(anyString(), anyString(), anyString(), anyString());
		
		try {
			cmisObjectService.moveObject(mockRepositoryId, mockHolderObject, "mockTargetFolderId", "mockSourceFolderId", mockExtension);
			fail("cmisObjectService.move should have been thrown an exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).moveObject(mockRepositoryId, "mockObjectValue", "mockSourceFolderId", "mockTargetFolderId");
			verifyNoMoreInteractions(objectService, relationshipService, security);
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to run with success
	 * scenario : run cmisObjectService.moveObject
	 * expected : response should run without any error
	 */
	@Test
	public void testMoveObject_objectServiceOk() {
		Holder<String> mockHolderObject = mock(Holder.class);
		when(mockHolderObject.getValue()).thenReturn("mockObjectValue");
		doNothing().when(objectService).moveObject(anyString(), anyString(), anyString(), anyString());
		
		cmisObjectService.moveObject(mockRepositoryId, mockHolderObject, "mockTargetFolderId", "mockSourceFolderId", mockExtension);
	
		verify(objectService, times(1)).moveObject(mockRepositoryId, "mockObjectValue", "mockSourceFolderId", "mockTargetFolderId");
		verifyNoMoreInteractions(objectService, relationshipService, security);		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to throw an Exception
	 * scenario : run cmisObjectService.getContentStream
	 * expected : response should get the same exception
	 */
	@Test
	public void testGetContentStream_objectServiceThrowException() {
		Exception objectException = new RuntimeException("some error message");
		when(objectService.getContentStream(anyString(), anyString())).thenThrow(objectException);
		
		try {
			// run the scenario
			cmisObjectService.getContentStream(mockRepositoryId, "mockObjectId", "streamId", new BigInteger("-1"), new BigInteger("0"), mockExtension);
			fail("cmisObjectService.getContentStream should have been thrown an exception");
		} catch(Exception cmisObjectServiceException) {
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getContentStream(mockRepositoryId, "mockObjectId");
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to run with success
	 * scenario : run cmisObjectService.getContentStream
	 * expected : response should run without any error
	 */
	@Test
	public void testGetContentStream_objectServiceOk() {
				
		ContentStream expectedMockContentStream = mock(ContentStream.class);
		when(objectService.getContentStream(anyString(), anyString())).thenReturn(expectedMockContentStream);
		
		ContentStream actualContentStream = cmisObjectService.getContentStream(mockRepositoryId, "mockObjectId", "streamId", new BigInteger("-1"), new BigInteger("0"), mockExtension);
	
		//asserting&verifying
		assertSame(expectedMockContentStream, actualContentStream);
		verify(objectService, times(1)).getContentStream(mockRepositoryId, "mockObjectId");
		verifyNoMoreInteractions(objectService, relationshipService, security);		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to throw an Exception
	 * scenario : run cmisObjectService.deleteTree
	 * expected : response should get the same exception
	 */
	@Test
	public void testDeleteTree_objectServiceThrowException() {
		Exception objectException = new RuntimeException("some err message");
		when(objectService.deleteTree(anyString(), anyString(), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenThrow(objectException);
		
		try {
			cmisObjectService.deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true, mockExtension);
			fail("cmisObjectService.deleteTree should have been thrown an exception");
		} catch(Exception cmisObjectServiceException) {
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true);
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to run with success
	 * scenario : run cmisObjectService.deleteTree
	 * expected : response should run without any error
	 */
	@Test
	public void testDeleteTree_objectService_nonEmptyResponse() {
		final String[] expectedMockIds = new String[] {"cmis_id:1", "cmis_id:12", "cmis_id:2" , "cmis_id:3", "cmis_id:31", "cmis_id:316"};
		List<String> expectedMockIds_asList = getMockDeletedListOfIds(expectedMockIds);
		when(objectService.deleteTree(anyString(), anyString(), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenReturn(expectedMockIds_asList);
				
		FailedToDeleteData actualFailedDelatedData = cmisObjectService.deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true, mockExtension);
		
		assertSame(expectedMockIds_asList, actualFailedDelatedData.getIds());		
		verify(objectService, times(1)).deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true);
		verifyNoMoreInteractions(objectService, relationshipService, security);
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to run with success
	 * scenario : run cmisObjectService.deleteTree
	 * expected : response should run without any error
	 */
	@Test
	public void testDeleteTree_objectService_emptyResponse() {
		final String[] expectedMockIds = new String[] {};
		List<String> expectedMockIds_asList = getMockDeletedListOfIds(expectedMockIds);
		when(objectService.deleteTree(anyString(), anyString(), anyBoolean(), any(UnfileObject.class), anyBoolean())).thenReturn(expectedMockIds_asList);
				
		FailedToDeleteData actualFailedDelatedData = cmisObjectService.deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true, mockExtension);
		assertNotNull(actualFailedDelatedData.getIds());
		assertTrue(actualFailedDelatedData.getIds().isEmpty());
		
		verify(objectService, times(1)).deleteTree(mockRepositoryId, mockFolderId, true, UnfileObject.DELETE, true);
		verifyNoMoreInteractions(objectService, relationshipService, security);
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectHolder to return NULL
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should get the NullPointerException
	 */
	@Test
	public void testSetContentStream_NullObjectId() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn(null);
		Holder<String> expectedMockChangeTokenHolder = mock(Holder.class);
				
		try {
			cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedMockChangeTokenHolder, mockContentStream, mockExtension);
			fail("cmisObjectService.setContentStream should have raised an exception");
		} catch(NullPointerException cmisObjectServiceException) {
			
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}
		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectHolder to return empty string
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should get the CmisUpdateConflictException
	 */
	@Test
	public void testSetContentStream_emptyObjectId() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("");
		Holder<String> expectedMockChangeTokenHolder = mock(Holder.class);
				
		try {
			cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedMockChangeTokenHolder, mockContentStream, mockExtension);
			fail("cmisObjectService.setContentStream should have raised an exception");
		} catch(CmisUpdateConflictException cmisObjectServiceException) {
			assertEquals("Cannot verify change token, null or empty object ID", cmisObjectServiceException.getMessage());
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}
		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService to throw an Exception
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should get the same Exception
	 */
	@Test
	public void testSetContentStream_objectServiceGetObjectThrowException() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedMockChangeTokenHolder = mock(Holder.class);
		when(expectedMockChangeTokenHolder.getValue()).thenReturn("mockHolder");
		Exception objectException = new RuntimeException("some err message"); 
		doThrow(objectException).when(objectService).getObject(anyString(), anyString());
		
		try {
			cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedMockChangeTokenHolder, mockContentStream, mockExtension);
			fail("cmisObjectService.setContentStream should have raised an exception");
		} catch(Exception cmisObjectServiceException) {
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}		
	}
	
	/**
	 * input 	:  
	 * 			: mocking the clientObjectHolder is not the same as the objectHolder from storage
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should get CmisUpdateConflictException
	 */
	@Test
	public void testSetContentStream_objectServiceReturnWrongToken() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedMockChangeTokenHolder = mock(Holder.class);
		when(expectedMockChangeTokenHolder.getValue()).thenReturn("expectedMockHolderValue");
		CMISObject mockObjectHavingToken = mock(CMISObject.class);
		when(mockObjectHavingToken.getPropertyValue(PropertyIds.CHANGE_TOKEN)).thenReturn("actualMockHolderValue");
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObjectHavingToken);
		
		try {
			cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedMockChangeTokenHolder, mockContentStream, mockExtension);
			fail("cmisObjectService.setContentStream should have raised an exception");
		} catch(CmisUpdateConflictException cmisObjectServiceException) {
			assertTrue(cmisObjectServiceException.getMessage().startsWith("Change Tokens do not match. Cannot modify CmisObject with id:"));
			verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}		
	}
	
	/**
	 * input 	:  
	 * 			: mocking the objectService.setContentStream throw an Exception
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should get the same Exception
	 */
	@Test
	public void testSetContentStream_objectServiceSetContentStreamThrowException() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedMockChangeTokenHolder = mock(Holder.class);
		when(expectedMockChangeTokenHolder.getValue()).thenReturn("expectedMockHolderValue");
		CMISObject mockObjectHavingToken = mock(CMISObject.class);
		when(mockObjectHavingToken.getPropertyValue(PropertyIds.CHANGE_TOKEN)).thenReturn("expectedMockHolderValue");
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObjectHavingToken);
		Exception objectException = new RuntimeException("some err message");
		when(objectService.setContentStream(anyString(), anyString(), anyBoolean(), any(ContentStream.class))).thenThrow(objectException);
		try {
			cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedMockChangeTokenHolder, mockContentStream, mockExtension);
			fail("cmisObjectService.setContentStream should have raised an exception");
		} catch(Exception cmisObjectServiceException) {
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
			verify(objectService, times(1)).setContentStream(mockRepositoryId, "mockObjectValue", true, mockContentStream);
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}		
	}
	
	/**
	 * input 	:  
	 * 			: mocking all the services to run properly
	 * scenario : run cmisObjectService.setContentStream
	 * expected : response should verify the holderValue :is changed 
	 */
	@Test
	public void testSetContentStream_objectServicesOk() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedStubChangeTokenHolder = getStubTokenHolder("expectedMockHolderValue");		
		CMISObject mockObjectHavingToken = mock(CMISObject.class);
		when(mockObjectHavingToken.getPropertyValue(PropertyIds.CHANGE_TOKEN)).thenReturn("expectedMockHolderValue");
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObjectHavingToken);
		when(objectService.setContentStream(anyString(), anyString(), anyBoolean(), any(ContentStream.class))).thenReturn("newMockHolderValue");
		
		cmisObjectService.setContentStream(mockRepositoryId, expectedMockObjectHolder, true, expectedStubChangeTokenHolder, mockContentStream, mockExtension);
		
		assertEquals("newMockHolderValue", expectedStubChangeTokenHolder.getValue());
		verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
		verify(objectService, times(1)).setContentStream(mockRepositoryId, "mockObjectValue", true, mockContentStream);
		verifyNoMoreInteractions(objectService, relationshipService, security);				
	}
	
	/**
	 * input 	:  
	 * 			: mocking all the services to run properly
	 * scenario : run cmisObjectService.deleteContentStream
	 * expected : response should verify the holderValue :is changed 
	 */
	@Test
	public void testDeleteContentStream_objectServicesOk() {
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedStubChangeTokenHolder = getStubTokenHolder("expectedMockHolderValue");		
		CMISObject mockObjectHavingToken = mock(CMISObject.class);
		when(mockObjectHavingToken.getPropertyValue(PropertyIds.CHANGE_TOKEN)).thenReturn("expectedMockHolderValue");
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObjectHavingToken);
		when(objectService.deleteContentStream(anyString(), anyString())).thenReturn("newMockHolderValue");
		
		cmisObjectService.deleteContentStream(mockRepositoryId, expectedMockObjectHolder, expectedStubChangeTokenHolder, mockExtension);
		
		assertEquals("newMockHolderValue", expectedStubChangeTokenHolder.getValue());
		verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
		verify(objectService, times(1)).deleteContentStream(mockRepositoryId, "mockObjectValue");
		verifyNoMoreInteractions(objectService, relationshipService, security);	
	}
	
	/**
	 * input 	:  
	 * 			: mocking all the services to run properly
	 * scenario : run cmisObjectService.updateProperties
	 * expected : response should verify the holderValue :is changed 
	 */
	@Test
	public void testUpdateProperties_objectServicesOk() {
		Properties emptyProperties = getEmptyProperties();
		Holder<String> expectedMockObjectHolder = mock(Holder.class);
		when(expectedMockObjectHolder.getValue()).thenReturn("mockObjectValue");
		Holder<String> expectedStubChangeTokenHolder = getStubTokenHolder("expectedMockHolderValue");		
		CMISObject mockObjectHavingToken = mock(CMISObject.class);
		when(mockObjectHavingToken.getPropertyValue(PropertyIds.CHANGE_TOKEN)).thenReturn("expectedMockHolderValue");
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockObjectHavingToken);
		when(objectService.updateProperties(anyString(), anyString(), any(Properties.class))).thenReturn("newMockHolderValue");
		
		cmisObjectService.updateProperties(mockRepositoryId, expectedMockObjectHolder, expectedStubChangeTokenHolder, emptyProperties, mockExtension);
		
		assertEquals("newMockHolderValue", expectedStubChangeTokenHolder.getValue());
		verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectValue");
		verify(objectService, times(1)).updateProperties(mockRepositoryId, "mockObjectValue", emptyProperties);
		verifyNoMoreInteractions(objectService, relationshipService, security);
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService.getObject throw an Exception
	 * scenario : run cmisObjectService.getObject
	 * expected : response should get the same Exception 
	 */
	@Test
	public void testGetObject_objectServiceGetObjectThrowException() {
		String mockObjectId = "mockObjectId";
		Exception objectException = new RuntimeException("some error message");
		when(objectService.getObject(anyString(), anyString())).thenThrow(objectException);
		
		try {
			cmisObjectService.getObject(mockRepositoryId, mockObjectId, null, false, IncludeRelationships.NONE, null, false, false, mockExtension);
			fail("cmisObjectService.getObject should have been thrown an exception");
		} catch(Exception cmisObjectServiceException) {
			assertEquals(objectException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(objectException.getClass(), cmisObjectServiceException.getClass());
			verify(objectService, times(1)).getObject(mockRepositoryId, "mockObjectId");			
			verifyNoMoreInteractions(objectService, relationshipService, security);	
		}
		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService.getObject 
	 * scenario : run cmisObjectService.getObject
	 * expected : response should get expected 
	 */
	@Test
	public void testGetObject_allowableActionsFalse_relationshipNone() {
		String mockObjectId = "mockObjectId";
		CMISObject expectedMockCmisObject =  mock(CMISObject.class);
		when(expectedMockCmisObject.getCmisObjectId()).thenReturn(mockObjectId);
		ObjectType expectedMockObjectType = mock(ObjectType.class);
		when(objectService.getObject(anyString(), anyString())).thenReturn(expectedMockCmisObject);
		when(expectedMockCmisObject.getObjectType()).thenReturn(expectedMockObjectType);
		when(expectedMockObjectType.getObjectTypePropertiesIncludeParents(anyBoolean(), anyBoolean())).thenReturn(Collections.EMPTY_MAP);
		Page<CMISObject> mockPage = mock(Page.class);
		when(mockPage.getPageElements()).thenReturn(Collections.<CMISObject>emptySet());
		when(relationshipService.getObjectRelationships(anyString(), anyString(), any(IncludeRelationships.class))).thenReturn(mockPage);
		
		ObjectData actualObjectData = cmisObjectService.getObject(mockRepositoryId, mockObjectId, null, false, IncludeRelationships.NONE, null, false, false, mockExtension);
		
		assertEquals(expectedMockCmisObject.getCmisObjectId(), actualObjectData.getId());
		assertTrue(actualObjectData.getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions().isEmpty());
		assertTrue(actualObjectData.getRelationships() == null || actualObjectData.getRelationships().isEmpty());
		verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);
		verify(relationshipService, times(1)).getObjectRelationships(mockRepositoryId, mockObjectId, IncludeRelationships.NONE);
		verifyNoMoreInteractions(objectService, relationshipService, security);		
	}
	
	/**
	 * input 	:  
	 * 			: mocking objectService.getObject 
	 * scenario : run cmisObjectService.getObject
	 * expected : response should get expected 
	 */
	@Test	
	public void testGetObject_allowableActionsTrue_relationshipNone() {
		String mockObjectId = "mockObjectId";
		CMISObject expectedMockCmisObject =  mock(CMISObject.class);
		when(expectedMockCmisObject.getCmisObjectId()).thenReturn(mockObjectId);
		ObjectType expectedMockObjectType = mock(ObjectType.class);
		when(objectService.getObject(anyString(), anyString())).thenReturn(expectedMockCmisObject);
		when(expectedMockCmisObject.getObjectType()).thenReturn(expectedMockObjectType);
		when(expectedMockObjectType.getObjectTypePropertiesIncludeParents(anyBoolean(), anyBoolean())).thenReturn(Collections.EMPTY_MAP);
		AllowableActions mockAllowableActions = mock(AllowableActions.class);
		when(security.getAllowableActions(any(CMISObject.class))).thenReturn(mockAllowableActions);
		Page<CMISObject> mockPage = mock(Page.class);
		when(mockPage.getPageElements()).thenReturn(Collections.<CMISObject>emptySet());
		when(relationshipService.getObjectRelationships(anyString(), anyString(), any(IncludeRelationships.class))).thenReturn(mockPage);
				
		ObjectData actualObjectData = cmisObjectService.getObject(mockRepositoryId, mockObjectId, null, true, IncludeRelationships.NONE, null, false, false, mockExtension);
				
		assertEquals(expectedMockCmisObject.getCmisObjectId(), actualObjectData.getId());
		assertSame(mockAllowableActions, actualObjectData.getAllowableActions());
		assertTrue(actualObjectData.getRelationships() == null || actualObjectData.getRelationships().isEmpty());
		verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);
		verify(security, times(1)).getAllowableActions(expectedMockCmisObject);
		verify(relationshipService, times(1)).getObjectRelationships(mockRepositoryId, mockObjectId, IncludeRelationships.NONE);
		verifyNoMoreInteractions(objectService, relationshipService, security);		
	}
	
	@Test
	public void testGetObject_allowableActionsFalse_relationshipSource() {
		doTestGetObject_allowableActionsFalse_relationship(false, IncludeRelationships.SOURCE);
		
	}
	
	@Test
	public void testGetObject_allowableActionsFalse_relationshipTarget() {
		doTestGetObject_allowableActionsFalse_relationship(false, IncludeRelationships.TARGET);
		
	}
	
	@Test
	public void testGetObject_allowableActionsFalse_relationshipBoth() {
		doTestGetObject_allowableActionsFalse_relationship(false, IncludeRelationships.BOTH);
		
	}
	
	@Test
	public void testGetObject_allowableActionsTrue_relationshipSource() {
		doTestGetObject_allowableActionsFalse_relationship(true, IncludeRelationships.SOURCE);
		
	}
	
	@Test
	public void testGetObject_allowableActionsTrue_relationshipTarget() {
		doTestGetObject_allowableActionsFalse_relationship(true, IncludeRelationships.TARGET);
		
	}
	
	@Test
	public void testGetObject_allowableActionsTrue_relationshipBoth() {
		doTestGetObject_allowableActionsFalse_relationship(true, IncludeRelationships.BOTH);
		
	}
	
	private void doTestGetObject_allowableActionsFalse_relationship(boolean includeAllowableActions,IncludeRelationships expectedRelationship) {
		Integer[] expectedRelationshipIds = new Integer[] {10, 3, 4, 17};
		String mockObjectId = "mockObjectId";
		CMISObject expectedMockCmisObject =  mock(CMISObject.class);
		when(expectedMockCmisObject.getCmisObjectId()).thenReturn(mockObjectId);
		ObjectType expectedMockObjectType = mock(ObjectType.class);
		when(objectService.getObject(anyString(), anyString())).thenReturn(expectedMockCmisObject);
		when(expectedMockCmisObject.getObjectType()).thenReturn(expectedMockObjectType);
		when(expectedMockObjectType.getObjectTypePropertiesIncludeParents(anyBoolean(), anyBoolean())).thenReturn(Collections.EMPTY_MAP);
		AllowableActions mockAllowableActions = mock(AllowableActions.class);
		when(security.getAllowableActions(any(CMISObject.class))).thenReturn(mockAllowableActions);
		Page<CMISObject> expectedMockPageWithObjects = mock(Page.class); 
		Set<CMISObject> expectedStubRelationshipObjects = getStubObjectsAsSet(expectedRelationshipIds);
		when(relationshipService.getObjectRelationships(anyString(), anyString(), any(IncludeRelationships.class))).thenReturn(expectedMockPageWithObjects);		
		when(expectedMockPageWithObjects.getPageElements()).thenReturn(expectedStubRelationshipObjects);
		
		ObjectData actualObjectData = cmisObjectService.getObject(mockRepositoryId, mockObjectId, null, includeAllowableActions, expectedRelationship, null, false, false, mockExtension);
				
		assertEquals(expectedMockCmisObject.getCmisObjectId(), actualObjectData.getId());
		assertTrue(actualObjectData.getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions() == null || actualObjectData.getAllowableActions().getAllowableActions().isEmpty());
		assertNotNull(actualObjectData.getRelationships());
		List<String> actualRelationshipIds = new ArrayList();
		String[] actualRelationshipIds_asArr = new String[expectedRelationshipIds.length];
		for(ObjectData relationshipObjectData : actualObjectData.getRelationships()) {
			actualRelationshipIds.add(relationshipObjectData.getId());
		}
		compareExpectedWithActualProperties(expectedRelationshipIds, actualRelationshipIds.toArray(actualRelationshipIds_asArr), "");
		verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);	
		if(includeAllowableActions) {
			verify(security, times(1)).getAllowableActions(expectedMockCmisObject);
		}
		verify(relationshipService, times(1)).getObjectRelationships(mockRepositoryId, mockObjectId, expectedRelationship);
		verifyNoMoreInteractions(objectService, relationshipService, security);	
	}
	
	private Set<CMISObject> getStubObjectsAsSet(Integer[] ids) {
		Set<CMISObject> cmisObjects = new HashSet();
		for (Integer id : ids) {
			cmisObjects.add(getCMISObject(id));
		}
		return cmisObjects;
	}

	private Holder<String> getStubTokenHolder(String value) {
		Holder<String> tokenHolder = new Holder();
		tokenHolder.setValue(value);
		return tokenHolder;
	}

	private List<String> getMockDeletedListOfIds(String[] expectedMockIds) {
		return Arrays.asList(expectedMockIds);
	}

	private void doTestGetRendition(Integer[] expectedRenditionIds) {
		final String mockObjectId = "mockObjectId";
		
		when(objectService.getRenditions(mockRepositoryId, mockObjectId,  -1, 0)).thenReturn(getRenditionList(expectedRenditionIds));
						
		List<RenditionData> actualRenditions = cmisObjectService.getRenditions(mockRepositoryId, mockObjectId, null, new BigInteger("-1"), new BigInteger("0"), mockExtension);
		
		assertNotNull(actualRenditions);
		assertFalse(actualRenditions.isEmpty());
		String[] actualRenditionStreamIds_asArr = new String[expectedRenditionIds.length];
		List<String> actualRenditionStreamIds = new ArrayList();
		int i = -1;
		for(RenditionData actualRendition : actualRenditions) {
			actualRenditionStreamIds.add(actualRendition.getStreamId());
		}
		
		compareExpectedWithActualProperties(expectedRenditionIds, actualRenditionStreamIds.toArray(actualRenditionStreamIds_asArr), RENDITION_STREAM_PREFIX);
	}
	
	private List<Rendition> getEmptyRenditionList() {
		return getRenditionList();
	}
	
	private List<Rendition> getRenditionList(Integer ... ids) {
		List<Rendition> renditionList = new ArrayList();
		for (Integer id : ids) {
			Rendition rendition = new Rendition();
			rendition.setId(id);
			rendition.setStreamId(RENDITION_STREAM_PREFIX + id);
			renditionList.add(rendition);
		}
		return renditionList;
	}

	private void doTestGetProperties(Integer[] expectedPropertyModel) {
		final String mockObjectId = "mockObjectId";
		CMISObject mockObject = mock(CMISObject.class);
		ObjectType mockObjectType = mock(ObjectType.class);		
		when(mockObject.getObjectType()).thenReturn(mockObjectType);
		when(mockObject.getProperties()).thenReturn(getPropertyModelAsSet(Cardinality.SINGLE, expectedPropertyModel));
		// this is the result of calling up to inherited properties: mockObject.getObjectType().getObjectTypePropertiesIncludeParents(true, true) 
		Map<String, SortedSet<ObjectTypeProperty>> mockObjectTypeProperties = Collections.emptyMap();
		when(mockObjectType.getObjectTypePropertiesIncludeParents(anyBoolean(), anyBoolean())).thenReturn(mockObjectTypeProperties);
		when(objectService.getObject(mockRepositoryId, mockObjectId)).thenReturn(mockObject);
		
		Properties actualProperties = cmisObjectService.getProperties(mockRepositoryId, mockObjectId, null, mockExtension);
		
		assertNotNull(actualProperties);
		assertFalse(actualProperties.getProperties().isEmpty());
		Map<String, PropertyData<?>> actualPropertuesAsMap =  actualProperties.getProperties();
		String[] actualPropertiesAsStrings = new String[expectedPropertyModel.length];
		int i=-1;
		for (PropertyData actualPropertyData : actualPropertuesAsMap.values()) {			
			actualPropertiesAsStrings[++i] = actualPropertyData.getFirstValue().toString(); 
		}
		compareExpectedWithActualProperties(expectedPropertyModel, actualPropertiesAsStrings, PROPERTY_VALUE_PREFIX);
		verify(objectService, times(1)).getObject(mockRepositoryId, mockObjectId);
		verifyNoMoreInteractions(objectService, relationshipService, security);
	}	
		
	private void preparePositiveScenario_createObject(String expectedObjectId) {
		
		when(security.getPermissionNames(anyString())).thenReturn(getPermissionNamesAllowedByRepository(33, 11, 22));
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);
		CMISObject mockObject = mock(CMISObject.class);
		when (mockObject.getCmisObjectId()).thenReturn(expectedObjectId);
		when(objectService.createObject(anyString(), any(CMISObject.class), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), any(BaseTypeId.class))).thenReturn(mockObject);		
	}
	
	private void verifyPositiveScenario_createDocument(String expectedDocumentId, String actualDocumentId, Integer[] expectedProperties) {
		assertEquals(expectedDocumentId, actualDocumentId);
		verifyAllServicesScenario_createDocument(expectedProperties);
		
	}
	
	private void verifyAllServicesScenario_createDocument(Integer[] expectedProperties) {
				
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();		
		verify(objectService, times(1)).createObject(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), actualCaptorBaseTypeId.capture());
		verifyCapture_createObject(expectedProperties, BaseTypeId.CMIS_DOCUMENT);
		verifyNoMoreInteractions(security, objectService, relationshipService);		
	}	
	
	private void compareExpectedWithActualProperties(Integer[] expectedProperties, String[] actualProperties, String expectedPrefix) {

		String [] expectedPropertiesAsString = new String[expectedProperties.length] ;
		for(int i=0; i<expectedProperties.length; i++) {
			expectedPropertiesAsString[i] = expectedPrefix + expectedProperties[i];
		}
		Arrays.sort(expectedPropertiesAsString);
		Arrays.sort(actualProperties);
		for (int i=0; i<expectedPropertiesAsString.length; i++) {
			assertEquals(expectedPropertiesAsString[i] , actualProperties[i]);
		}		
	}

	private void preparePositiveScenario_createDocumentFromSource(String expectedDocumentId) throws CloneNotSupportedException {
		
		CMISObject mockSource = mock(CMISObject.class);
		CMISObject mockSourceCloned = mock(CMISObject.class);
		ContentStream sourceStream = mock(ContentStream.class);
		when(objectService.getObject(anyString(), anyString())).thenReturn(mockSource);
		when(objectService.getContentStream(anyString(), anyString())).thenReturn(mockContentStream);
		when(mockSource.clone()).thenReturn(mockSourceCloned);
		when(security.getPermissionNames(anyString())).thenReturn(getPermissionNamesAllowedByRepository(33, 11, 22));
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);
		CMISObject mockObject = mock(CMISObject.class);
		when (mockObject.getCmisObjectId()).thenReturn(expectedDocumentId);
		when(objectService.createObject(anyString(), any(CMISObject.class), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), any(BaseTypeId.class))).thenReturn(mockObject);		
	}
	
	private void verifyAllServicesScenario_createDocumentFromSource(Integer[] expectedProperties) {
		verify(objectService, times(1)).getObject(mockRepositoryId, mockSourceId);
		verify(objectService, times(1)).getContentStream(mockRepositoryId, mockSourceId);
		verify(security, times(1)).getPermissionNames(mockRepositoryId);
		verify(security, times(1)).getCallContextHolder();
		verify(objectService, times(1)).createObject(actualCaptorRepositoryId.capture(), actualCaptorCmisObject.capture(), anySet(), anySet(), any(ContentStream.class), any(VersioningState.class), actualCaptorBaseTypeId.capture());
		verifyCapture_createObject(expectedProperties, BaseTypeId.CMIS_DOCUMENT);		
		verifyNoMoreInteractions(security, objectService, relationshipService);
	}		
	
	private void verifyCapture_createObject(Integer[] expectedProperties, BaseTypeId expectedBaseTypeId) {
		assertEquals(mockRepositoryId, actualCaptorRepositoryId.getValue());
		Set<Property> actualObjectProperties = actualCaptorCmisObject.getValue().getProperties();		
		assertNotNull(actualObjectProperties);
		assertEquals(expectedProperties.length, actualObjectProperties.size());
		String[] actualProperties_asArr = new String[expectedProperties.length];
		List<String> actualProperties = new ArrayList();		
		for(Property actualProperty : actualObjectProperties) { 
			actualProperties.add(actualProperty.getTypedValue().toString());			
		}
		
		compareExpectedWithActualProperties(expectedProperties, actualProperties.toArray(actualProperties_asArr), PROPERTY_VALUE_PREFIX);
		assertEquals(expectedBaseTypeId, actualCaptorBaseTypeId.getValue());		
	}
	
	private void verifyCapture_createRelationship(Integer[] expectedProperties) {
		assertEquals(mockRepositoryId, actualCaptorRepositoryId.getValue());
		Set<Property> actualObjectProperties = actualCaptorCmisObject.getValue().getProperties();		
		assertNotNull(actualObjectProperties);
		assertEquals(expectedProperties.length, actualObjectProperties.size());
		String[] actualProperties_asArr = new String[expectedProperties.length];
		List<String> actualProperties = new ArrayList();
		
		for(Property actualProperty : actualObjectProperties) { 
			actualProperties.add(actualProperty.getTypedValue().toString());
		
		}
		compareExpectedWithActualProperties(expectedProperties, actualProperties.toArray(actualProperties_asArr), PROPERTY_VALUE_PREFIX);				
	}

	private void preparePositiveScenario_createRelationship(String expectedMockCmisObjectId) {
		
		CMISObject mockExpectedMISObject = mock(CMISObject.class);
		when(mockExpectedMISObject.getCmisObjectId()).thenReturn(expectedMockCmisObjectId);		
		when(security.getPermissionNames(anyString())).thenReturn(getPermissionNamesAllowedByRepository(33, 11, 22));
		CallContextHolder mockCallContextHolder = mock(CallContextHolder.class);
		when(security.getCallContextHolder()).thenReturn(mockCallContextHolder);		
		when(mockCallContextHolder.getUsername()).thenReturn(mockUsername);		
		when(objectService.createRelationship(anyString(), any(CMISObject.class), anySet(), anySet(), anyList())).thenReturn(mockExpectedMISObject);
	}
	
	private void runAndAssertExceptionScenario_createDocument(Exception expectedException, Properties properties) {
		
		try {
			cmisObjectService.createDocument(mockRepositoryId, properties, mockFolderId, null, null, null, null, null, null);
			fail("actually cmisObjectService should have thrown an Exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(expectedException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(expectedException.getClass(), cmisObjectServiceException.getClass());
		}	
	}	
	
	private void runAndAssertExceptionScenario_createDocumentFromSource(Exception expectedException) {		
		
		try {
			cmisObjectService.createDocumentFromSource(mockRepositoryId, mockSourceId, getEmptyProperties(), mockFolderId, mockVersioningState, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
			fail("Create document should have thrown an Exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(expectedException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(expectedException.getClass(), cmisObjectServiceException.getClass());
		}	
	}	
	
	private void runAndAssertExceptionScenario_createRelationship(Exception expectedException, Properties properties) {
		
		try {
			cmisObjectService.createRelationship(mockRepositoryId, properties, mockPolicies, mockAddAces, mockRemoveAcces, mockExtension);
			fail("actually cmisObjectService should have thrown an Exception");
		} catch(Exception cmisObjectServiceException) {
			//test cmisObjectServiceException is the same as the one raised by first layer
			assertEquals(expectedException.getMessage(), cmisObjectServiceException.getMessage());
			assertEquals(expectedException.getClass(), cmisObjectServiceException.getClass());
		}	
	}

	private Properties getNullProperties() {
		return null;
	}
	
	private Properties getEmptyProperties() {
		
		return getProperties(Cardinality.SINGLE, new Integer[] {});
	}
		
	private Properties getProperties(Cardinality cardinality, Integer[] ids) {
		CMISObject cmisObject = new CMISObject();
		for (Integer id : ids) {
			cmisObject.addProperty(getPropertyModel(cardinality, id));
			cmisObject.setObjectType(getObjectType());
		}
		if(cmisObject.getObjectType() == null ) {
			cmisObject.setObjectType(getObjectType());
		}
		
		return PropertiesBuilder.build(cmisObject, "");		
	}

	private ObjectType getObjectType() {
		
		ObjectType objectType = new ObjectType();
		objectType.setObjectTypeProperties(new TreeSet());
		objectType.setCmisId(TYPE_CMIS_DOCUMENT);
		objectType.setBase(objectType);
		return objectType;
	}
	
	private Set<Property> getPropertyModelAsSet(Cardinality cardinality, Integer[] ids) {
		Set<Property> properties = new HashSet();
		for(Integer id : ids) {
			properties.add(getPropertyModel(cardinality, id));
		}
		return properties;
	}

	private Property getPropertyModel(Cardinality cardinality, Integer id) {
		
		return new Property(getObjectTypePropertyModel(cardinality, PropertyType.STRING, id), PROPERTY_VALUE_PREFIX + id);		
	}

	private ObjectTypeProperty getObjectTypePropertyModel(Cardinality cardinality, PropertyType propertyType, Integer id) {
		ObjectTypeProperty objectTypeProperty = new ObjectTypeProperty();	
		objectTypeProperty.setCardinality(cardinality);
		objectTypeProperty.setPropertyType(propertyType);
		objectTypeProperty.setId(id);
		objectTypeProperty.setCmisId(id.toString());
		return objectTypeProperty;
	}	
	
	private CMISObject getCMISObject(Integer id) {
		
		CMISObject object = new CMISObject();
		object.setId(id);
		object.setCmisObjectId(id.toString());
		object.setObjectType(getObjectType());
		
		return object;
	}
	
	private Set<String> getPermissionNamesAllowedByRepository(Integer ... permissionIds) {
		Set<String> permissions = new HashSet();
		for(Integer permissionId : permissionIds) {
			permissions.add(MOCK_PERMISSIONNAME_PREFIX + permissionId);
		}
		
		return permissions;
	}

}
