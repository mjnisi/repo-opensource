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
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.util.Constants;

public class PropertyChangeListenerTest  extends BaseTestClass{
	
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
	public void testPropertyUpdatedWithoutAtomicIndexEnabled() throws Throwable {
		setScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioIndexObject_06_metadata.xml");
		
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, false);
		
		CMISObject object = entityManager.find(CMISObject.class, 201);
		Property property = entityManager.find(Property.class, 2010);
		property.setTypedValue("test1modified");
		entityManager.merge(property);
		entityManager.flush();
		
		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(0 == map.getCMISObjectsHavingMetadataChanged().size());
		assertFalse(map.getCMISObjectsHavingMetadataChanged().contains(201));
	}
	
	@Test
	@Transactional
	public void testPropertyChangeListenerIsTriggeredWhenUpdate() throws Throwable {
		setScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioIndexObject_06_metadata.xml");
		
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, true);
		
		CMISObject object = entityManager.find(CMISObject.class, 201);
		object = cmisObjectSelector.getCMISObject(object.getObjectType().getRepository().getCmisId(), object.getCmisObjectId());
		Property property = entityManager.find(Property.class, 2010);
		property.setTypedValue("test1modified");
		entityManager.merge(property);
		entityManager.flush();
		
		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(1 == map.getCMISObjectsHavingMetadataChanged().size());
		assertTrue(map.getCMISObjectsHavingMetadataChanged().contains(new IndexChangeData(201)));
	}
	
	@Test
	@Transactional
	public void testPropertyChangeListenerIsTriggeredWhenPersist() throws Throwable {
		setScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioIndexObject_06_metadata.xml");
		
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, true);
		indexTriggersDelegate.initChangeTrackerMap();
		
		CMISObject object = entityManager.find(CMISObject.class, 201);
		object = cmisObjectSelector.getCMISObject(object.getObjectType().getRepository().getCmisId(), object.getCmisObjectId());
		ObjectTypeProperty objTypeProp = entityManager.find(ObjectTypeProperty.class, 105);
		Property property = new Property(objTypeProp, "testAuthor");
		property.setObject(object);
		entityManager.persist(property);
		entityManager.flush();
		
		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(1 == map.getCMISObjectsHavingMetadataChanged().size());
		assertTrue(map.getCMISObjectsHavingMetadataChanged().contains(new IndexChangeData(201)));
	}
	
	@Test
	@Transactional
	public void testPropertyChangeListenerIsTriggeredWhenRemove() throws Throwable {
		setScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setScenario(DatabaseOperation.INSERT, "scenarioIndexObject_06_metadata.xml");
		
		config.setProperty(Constants.PROPNAME_INDEX_ATOMIC_ENABLED, true);
		indexTriggersDelegate.initChangeTrackerMap();
		
		CMISObject object = entityManager.find(CMISObject.class, 201);
		object = cmisObjectSelector.getCMISObject(object.getObjectType().getRepository().getCmisId(), object.getCmisObjectId());
		ObjectTypeProperty objTypeProp = entityManager.find(ObjectTypeProperty.class, 101);
		Property property = entityManager.find(Property.class, 2010);
		entityManager.remove(property);
		entityManager.flush();
		
		ChangeTrackerMap map = indexTriggersDelegate.getChangeTrackerMap();
		assertFalse( null == map);
		assertTrue(1 == map.getCMISObjectsHavingMetadataChanged().size());
		assertTrue(map.getCMISObjectsHavingMetadataChanged().contains(new IndexChangeData(201)));
	}
}
