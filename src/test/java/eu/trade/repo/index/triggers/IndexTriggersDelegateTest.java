package eu.trade.repo.index.triggers;

import java.math.BigInteger;
import java.util.Set;

import org.jodah.concurrentunit.Waiter;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;


public class IndexTriggersDelegateTest extends BaseTestClass{

	@Test
	public void testRegisterPropertyChangeWithoutMap() throws Throwable {

		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {

				IndexTriggersDelegate delegate = new IndexTriggersDelegate();
				delegate.registerPropertyChange( 101);
				waiter.assertTrue( null == delegate.getChangeTrackerMap());

				waiter.resume();
			}
		}).start();
		waiter.await();

	}

	@Test
	public void testRegisterPropertyChangeHavingMap() throws Throwable {

		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {

				IndexTriggersDelegate delegate = new IndexTriggersDelegate();

				delegate.initChangeTrackerMap();
				delegate.registerPropertyChange( 101);
				waiter.assertFalse( null == delegate.getChangeTrackerMap());

				ChangeTrackerMap map = delegate.getChangeTrackerMap();
				Set<IndexChangeData> changeSet = map.getCMISObjectsHavingMetadataChanged();
				waiter.assertEquals(1, changeSet.size());
				waiter.assertTrue(changeSet.contains(new IndexChangeData( 101)));

				waiter.resume();
			}
		}).start();
		waiter.await();
	}


	@Test
	public void testRegisterStreamChangeWithoutMap() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();
				delegate.registerStreamChange( 101, StreamChangeType.INSERT, BigInteger.valueOf(1024));
				waiter.assertTrue( null == delegate.getChangeTrackerMap());
				waiter.resume();
			}
		}).start();
		waiter.await();
	}

	@Test
	public void testRegisterStreamChangeHavingMap() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();

				delegate.initChangeTrackerMap();
				delegate.registerStreamChange( 101, StreamChangeType.INSERT, BigInteger.valueOf(1024));
				waiter.assertFalse( null == delegate.getChangeTrackerMap());
				ChangeTrackerMap map = delegate.getChangeTrackerMap();
				Set<IndexChangeData> changeSet = map.getCMISObjectsHavingStreamChangedOrRemoved();
				waiter.assertEquals(1, changeSet.size());
				waiter.assertTrue(changeSet.contains(new IndexChangeData( 101)));

				waiter.resume();
			}
		}).start();
		waiter.await();
	}


	@Test
	public void testRegisterCMISObjectRemovalWithoutMap() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();
				delegate.registerCMISObjectRemoval(101);
				waiter.assertTrue( null == delegate.getChangeTrackerMap());
				waiter.resume();
			}
		}).start();
		waiter.await();
	}

	@Test
	public void testRegisterCMISObjectRemovalHavingMap() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();

				delegate.initChangeTrackerMap();
				delegate.registerCMISObjectRemoval(101);
				waiter.assertFalse( null == delegate.getChangeTrackerMap());
				ChangeTrackerMap map = delegate.getChangeTrackerMap();
				Set<IndexChangeData> changeSet = map.getCMISObjectsRemoved();
				waiter.assertEquals(1, changeSet.size());
				waiter.assertTrue(changeSet.contains(new IndexChangeData(101)));
				waiter.resume();
			}
		}).start();
		waiter.await();
	}

	@Test
	public void testChangeTrackerMap() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();
				delegate.initChangeTrackerMap();
				ChangeTrackerMap tracker = delegate.getChangeTrackerMap();

				tracker.registerPropertyChange(101);
				tracker.registerPropertyChange(102);

				ChangeTrackerMap tracker2 = delegate.getChangeTrackerMap();
				Set<IndexChangeData> changeSet = tracker2.getCMISObjectsHavingMetadataChanged();
				waiter.assertEquals(2, changeSet.size());
				waiter.assertTrue(changeSet.contains(new IndexChangeData(101)));
				waiter.assertTrue(changeSet.contains(new IndexChangeData(102)));

				waiter.resume();
			}
		}).start();
		waiter.await();
	}
	
	@Test
	public void testMultipleInitTrackerWithinSameThread() throws Throwable {
		final Waiter waiter = new Waiter();
		new Thread(new Runnable() {
			public void run() {
				IndexTriggersDelegate delegate = new IndexTriggersDelegate();
				delegate.initChangeTrackerMap();
				ChangeTrackerMap tracker = delegate.getChangeTrackerMap();

				tracker.registerPropertyChange(101);
				tracker.registerPropertyChange(102);
				
				delegate.initChangeTrackerMap();

				ChangeTrackerMap tracker2 = delegate.getChangeTrackerMap();
				Set<IndexChangeData> changeSet = tracker2.getCMISObjectsHavingMetadataChanged();
				waiter.assertEquals(2, changeSet.size());
				waiter.assertTrue(changeSet.contains(new IndexChangeData(101)));
				waiter.assertTrue(changeSet.contains(new IndexChangeData(102)));

				waiter.resume();
			}
		}).start();
		waiter.await();
	}
	
	
	
//	@Test
//	public void testWords() throws Throwable {
//		final Waiter waiter = new Waiter();
//		new Thread(new Runnable() {
//			public void run() {
//				index.executeOperation(repositoryId, objectId, null, change.getFileSize(), true, IndexOperation.CONTENT_INDEX_DELETE);
//				delegate.registerStreamChange(101, StreamChangeType.INSERT, BigInteger.valueOf(1024));
//				waiter.assertTrue( null == delegate.getChangeTrackerMap());
//				waiter.resume();
//			}
//		}).start();
//		waiter.await();
//	}
}
