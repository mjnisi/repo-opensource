package eu.trade.repo.service.cmis;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.doThrow;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import eu.trade.repo.service.interfaces.IMultifilingService;

public class MockCmisMultiFilingServiceTest {
	
	private final String mockObjectId = "mockObjectId";
	private final String mockRepositoryId = "mockRepositoryId";
	private final String mockFolderId = "mockFolderId";
	private final ExtensionsData mockExtension = mock(ExtensionsData.class);
	
	@InjectMocks
	private CmisMultiFilingService cmisMultiFilingService;

	@Mock	
	private IMultifilingService multifilingService;
	
	@Before
	public void prepareTestEnvironment() {
		MockitoAnnotations.initMocks(this);
	}	
	
	@Test
	public void testAddObjectToFolder_allVerssionsFalse() {
		
		doNothing().when(multifilingService).addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, false);
		
		cmisMultiFilingService.addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, false, mockExtension);
		
		verify(multifilingService, times(1)).addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, false);
		verifyNoMoreInteractions(multifilingService);
	}
	
	@Test
	public void testAddObjectToFolder_allVerssionsTrue() {
		
		doNothing().when(multifilingService).addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, true);
		
		cmisMultiFilingService.addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, true, mockExtension);
		
		verify(multifilingService, times(1)).addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, true);
		verifyNoMoreInteractions(multifilingService);
	}
	
	@Test(expected=Exception.class)
	public void testAddObjectToFolder_serviceThrowException() {
		
		doThrow(new Exception()).when(multifilingService).addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, true);
		
		cmisMultiFilingService.addObjectToFolder(mockRepositoryId, mockObjectId, mockFolderId, true, mockExtension);				
	}
	
	@Test
	public void testRemoveObjectFromFolder() {
		
		doNothing().when(multifilingService).removeObjectFromFolder(mockRepositoryId, mockObjectId, mockFolderId);
		
		cmisMultiFilingService.removeObjectFromFolder(mockRepositoryId, mockObjectId, mockFolderId, mockExtension);
		
		verify(multifilingService, times(1)).removeObjectFromFolder(mockRepositoryId, mockObjectId, mockFolderId);
		verifyNoMoreInteractions(multifilingService);
	}
			
	@Test(expected=Exception.class)
	public void testRemoveObjectFromFolder_serviceThrowException() {
		
		doThrow(new Exception()).when(multifilingService).removeObjectFromFolder(mockRepositoryId, mockObjectId, mockFolderId);
		
		cmisMultiFilingService.removeObjectFromFolder(mockRepositoryId, mockObjectId, mockFolderId, mockExtension);				
	}
		
}
