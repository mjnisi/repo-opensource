package eu.trade.repo.index.triggers.handlers;


import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.configuration.Configuration;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.index.triggers.ChangeTrackerMap;
import eu.trade.repo.index.triggers.IndexChangeData;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.util.Constants;

public class CMISObjectChangeListenerTest extends BaseTestClass{

	@Autowired @Qualifier("indexConfig")
	private Configuration config;

	@Autowired private IndexTriggersDelegate indexTriggersDelegate;
	@PersistenceContext EntityManager entityManager;

	@Before
	public void init(){
		if( TransactionSynchronizationManager.hasResource("indexTrackerMap")){
			TransactionSynchronizationManager.unbindResource("indexTrackerMap");
		}
		indexTriggersDelegate.initChangeTrackerMap();
	}

	@Test
	@Transactional
	public void testCmisObjectRemovedWithoutAtomicIndexEnabled() throws Throwable {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, false);

		CMISObject object = entityManager.find(CMISObject.class, 103);
		ObjectType objectType = entityManager.find(ObjectType.class, object.getObjectType().getId());
		entityManager.remove(object);
		entityManager.flush();

		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(0 == map.getCMISObjectsRemoved().size());
		assertFalse(map.getCMISObjectsRemoved().contains(103));
	}

	@Test
	@Transactional
	public void testCmisObjectChangeListenerIsTriggered() throws Throwable {
		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);

		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, true);

		CMISObject object = entityManager.find(CMISObject.class, 103);
		object = cmisObjectSelector.getCMISObject(object.getObjectType().getRepository().getCmisId(), object.getCmisObjectId());
		entityManager.remove(object);
		entityManager.flush();

		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(1 == map.getCMISObjectsRemoved().size());
		assertTrue(map.getCMISObjectsRemoved().contains(new IndexChangeData(103)));

	}
	

}
