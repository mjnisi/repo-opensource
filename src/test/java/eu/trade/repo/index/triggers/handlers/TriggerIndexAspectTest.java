package eu.trade.repo.index.triggers.handlers;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.index.txsync.IndexTransactionSynchronization;
import eu.trade.repo.index.txsync.IndexTransactionSynchronizationFactory;
import eu.trade.repo.model.Repository;
import eu.trade.repo.selectors.RepositorySelector;


public class TriggerIndexAspectTest extends BaseTestClass{
	@Mock
	private JoinPoint joinPoint;
	@Mock
	private MethodSignature signature;
	
	
	@Mock private IndexConfigHolder indexConfigHolder;
	@Mock private IndexTransactionSynchronizationFactory indexSynchFactory;
	@Mock private RepositorySelector repositorySelector;
	@Mock private IndexTransactionSynchronization indexSynch; 

	@Autowired private IndexTriggersDelegate indexTriggersDelegate;

	@InjectMocks @Autowired private TriggerIndexAspect aspect;

	@Before
	public void initMocks() throws Exception{ 
		MockitoAnnotations.initMocks(this); 
		
		if( TransactionSynchronizationManager.hasResource("indexTrackerMap")){
			TransactionSynchronizationManager.unbindResource("indexTrackerMap");
		}
		//indexConfigHolder
		when(indexConfigHolder.getAtomicIndexEnabled()).thenReturn(true);
		
		//repositorySelector
		Integer repositoryId = 10;
		Repository repository = new Repository();
		repository.setId(repositoryId);
		when(repositorySelector.getRepository(any(String.class))).thenReturn(repository);
				
		//indexSynchFactory
		when(indexSynchFactory.getIndexTransactionSynchronization()).thenReturn(indexSynch);
				
		//joinPoint
		when(signature.getMethod()).thenReturn(TargetClass.class.getMethod("methodToTriggerIndex", String.class));
		when(joinPoint.getSignature()).thenReturn(signature);
		when(joinPoint.getTarget()).thenReturn(new TargetClass());
		when(joinPoint.getArgs()).thenReturn(new Object[]{"test_repo_01"});
	}

	@Test
	@Transactional
	public void testChangeTrackerMap() throws Throwable {

		aspect.triggerIndex(joinPoint, null);

		assertFalse(null == indexTriggersDelegate.getChangeTrackerMap());
		assertTrue( null != TransactionSynchronizationManager.getResource("indexTrackerMap") );
		assertTrue( TransactionSynchronizationManager.getSynchronizations().contains(indexSynch) );
		
	}


}
