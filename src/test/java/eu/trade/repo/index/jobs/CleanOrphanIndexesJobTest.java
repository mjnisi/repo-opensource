package eu.trade.repo.index.jobs;

import static org.junit.Assert.assertTrue;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.IndexTask;
import eu.trade.repo.index.impl.IndexTaskImpl;
import eu.trade.repo.index.model.IndexOperation;

public class CleanOrphanIndexesJobTest extends BaseLobTestClass {

	@Autowired
	private CleanOrphanIndexesJob indexBackgroundJob1;
	@Autowired
	private CleanOrphanIndexesJob indexBackgroundJob2;
	@Autowired
	private IndexSynchronizer indexSynchronizer;

	//*********************** cleanOrphanIndexes
	@Test 
	public void testCleanOrphanIndexes_noTasksOverlapping() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexObject_01_orphans.xml");

		indexBackgroundJob1.execute();

		compareLobTable("index_word", null, "index_delegate_1_wo.xml");
	}

	@Test 
	public void testCleanOrphanIndexes_tasksOverlapping() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexObject_05_jobOrphans.xml");

		IndexTask task = new IndexTaskImpl(TestConstants.TEST_REPO_1_INT, 502, IndexOperation.CONTENT_INDEX, indexSynchronizer);		
		boolean isInQueue = indexSynchronizer.putInQueue(task, false);
		assertTrue(isInQueue);

		indexBackgroundJob1.execute();

		compareLobTable("index_word", null, "index_delegate_5_wo.xml");
		compareLobTable("index_word_object", null, "index_delegate_5_wo.xml");

		indexSynchronizer.doOnTaskFinished(task);
	}

	@Test 
	public void testCleanOrphanIndexes_jobsInSequence() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml", "rootObjects_01.xml", "scenarioIndexObject_01_orphans.xml");

		indexBackgroundJob1.execute();
		indexBackgroundJob2.execute();

		compareLobTable("index_word", null, "index_delegate_1_wo.xml");
	}

}
