package eu.trade.repo.index.triggers;

import java.math.BigInteger;
import java.util.Set;

import static junit.framework.Assert.*;

import org.junit.Test;

import eu.trade.repo.BaseTestClass;

public class ChangeTrackerMapTest  extends BaseTestClass {

	@Test
	public void testGetCMISObjectsRemoved() throws Exception {
		ChangeTrackerMap tracker = new ChangeTrackerMap();
		
		tracker.registerCMISObjectRemoval(101);
		tracker.registerCMISObjectRemoval(102);
		
		Set<IndexChangeData> changeSet = tracker.getCMISObjectsRemoved();
		assertEquals(2, changeSet.size());
		assertTrue(changeSet.contains(new IndexChangeData(101)));
		assertTrue(changeSet.contains(new IndexChangeData(102)));
		
		tracker.registerCMISObjectRemoval(103);
		changeSet = tracker.getCMISObjectsRemoved();
		assertEquals(3, changeSet.size());
		
		tracker.registerCMISObjectRemoval(103);
		changeSet = tracker.getCMISObjectsRemoved();
		assertEquals(3, changeSet.size());
		
	}
	
	@Test
	public void testGetCMISObjectsHavingMetadataChanged() throws Exception {
		ChangeTrackerMap tracker = new ChangeTrackerMap();
		
		tracker.registerPropertyChange(101);
		tracker.registerPropertyChange(102);
		
		Set<IndexChangeData> changeSet = tracker.getCMISObjectsHavingMetadataChanged();
		assertEquals(2, changeSet.size());
		assertTrue(changeSet.contains(new IndexChangeData(101)));
		assertTrue(changeSet.contains(new IndexChangeData(102)));
		
		tracker.registerCMISObjectRemoval( 101);
		changeSet = tracker.getCMISObjectsHavingMetadataChanged();
		assertEquals(1, changeSet.size());
		assertFalse(changeSet.contains(new IndexChangeData(101)));
		assertTrue(changeSet.contains(new IndexChangeData(102)));
	}
	
	@Test
	public void testGetCMISObjectsHavingStreamChangedOrRemoved() throws Exception {
		ChangeTrackerMap tracker = new ChangeTrackerMap();
		
		tracker.registerStreamChange( 101, StreamChangeType.INSERT, BigInteger.valueOf(1024));
		
		Set<IndexChangeData> changeSet = tracker.getCMISObjectsHavingStreamChangedOrRemoved();
		assertEquals(1, changeSet.size());
		assertTrue(changeSet.iterator().next().getObjectId() == 101);
		IndexChangeData data = changeSet.iterator().next();
		assertEquals(StreamChangeType.INSERT, data.getChangeType());
		
		tracker.registerStreamChange( 101, StreamChangeType.DELETE, BigInteger.valueOf(1024));
		
		changeSet = tracker.getCMISObjectsHavingStreamChangedOrRemoved();
		assertEquals(1, changeSet.size());
		assertTrue(changeSet.iterator().next().getObjectId() == 101);
		data = changeSet.iterator().next();
		assertEquals(StreamChangeType.DELETE, data.getChangeType());
		
		tracker.registerStreamChange( 102, StreamChangeType.INSERT, BigInteger.valueOf(2048));
		changeSet = tracker.getCMISObjectsHavingStreamChangedOrRemoved();
		assertEquals(2, changeSet.size());
		
		tracker.registerCMISObjectRemoval( 101);
		changeSet = tracker.getCMISObjectsHavingStreamChangedOrRemoved();
		assertEquals(1, changeSet.size());
		data = changeSet.iterator().next();
		assertFalse(data.getObjectId() == 101);
		assertTrue(data.getObjectId() == 102);
	}
	
	
	
	@Test
	public void testChangeResultIdSet() throws Exception {
		ChangeTrackerMap tracker = new ChangeTrackerMap();
		
		tracker.registerPropertyChange( 101);
		tracker.registerPropertyChange( 102);
		
		Set<IndexChangeData> changeSet = tracker.getCMISObjectsHavingMetadataChanged();
		assertEquals(2, changeSet.size());
		assertTrue(changeSet.contains(new IndexChangeData(101)));
		assertTrue(changeSet.contains(new IndexChangeData(102)));
		
		changeSet.add(new IndexChangeData(103));
		
		changeSet = tracker.getCMISObjectsHavingMetadataChanged();
		assertEquals(2, changeSet.size());
		assertFalse(changeSet.contains(new IndexChangeData(103)));
		
	}
}
