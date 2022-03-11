package eu.trade.repo.index.triggers.handlers;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.triggers.ChangeTrackerMap;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.index.triggers.IndexChangeData;
import eu.trade.repo.index.triggers.StreamChangeType;
import eu.trade.repo.index.triggers.annotation.RegisterStreamChange;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;

public class StreamChangeListenerTest extends BaseLobTestClass{
	
	@Mock private JoinPoint joinPoint;
	@Mock private MethodSignature signature;
	@Mock private RegisterStreamChange registerStreamChange;
	@Mock private IndexConfigHolder indexConfigHolder;


	@Autowired private IndexTriggersDelegate indexTriggersDelegate;
	@PersistenceContext
	private EntityManager entityManager;

	@InjectMocks @Autowired private StreamChangeListener aspect;
	
	@Before
	public void initMocks() throws Exception{ 
		MockitoAnnotations.initMocks(this); 
		
		if( TransactionSynchronizationManager.hasResource("indexTrackerMap")){
			TransactionSynchronizationManager.unbindResource("indexTrackerMap");
		}
		
		indexTriggersDelegate.initChangeTrackerMap();
		
		//indexConfigHolder
		when(indexConfigHolder.getAtomicIndexEnabled()).thenReturn(true);
		
		//registerStreamChange
		when(registerStreamChange.value()).thenReturn(StreamChangeType.INSERT);
		
		//joinPoint
		when(signature.getMethod()).thenReturn(TargetClass.class.getMethod("methodToListenStreamChanges", Integer.class, int.class));
		when(joinPoint.getSignature()).thenReturn(signature);
		when(joinPoint.getTarget()).thenReturn(new TargetClass());
		when(joinPoint.getArgs()).thenReturn(new Object[]{Integer.valueOf(203), Integer.valueOf(1024)});
	}

	@Test
	@Transactional
	public void testUpdateTriggerIndexContentInfo() throws Throwable {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "scenarioIndexHelper_03_errorState.xml");
		
		aspect.updateTriggerIndexContentInfo(joinPoint, registerStreamChange);

		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse(null == map);
		assertTrue(1 == map.getCMISObjectsHavingStreamChangedOrRemoved().size());
		assertTrue(map.getCMISObjectsHavingStreamChangedOrRemoved().contains(new IndexChangeData(203)));
		IndexChangeData data = map.getCMISObjectsHavingStreamChangedOrRemoved().iterator().next();
		assertTrue(StreamChangeType.INSERT.equals(data.getChangeType()));
		assertTrue(data.getFileSize().intValue() == 1024);
		
		CMISObject object = entityManager.find(CMISObject.class, 203);
		assertTrue(IndexingState.NONE == IndexingState.get(object.getIndexStateContent()));
		assertTrue(0 == object.getIndexTriesContent());
		
	}


}
