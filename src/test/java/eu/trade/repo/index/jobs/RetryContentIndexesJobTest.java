package eu.trade.repo.index.jobs;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;

public class RetryContentIndexesJobTest extends BaseLobTestClass {

	@Autowired
	private RetryContentIndexesJob retryContentIndexesJob;
	

	@Test
	public void testSearchAndRegisterDocumentsToIndex() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "scenarioIndexObject_04_jobs.xml");
		
		retryContentIndexesJob.execute();

		compareLobTable("index_word", null, "indexJobs_01.xml");
		compareLobTable("index_word_object", null, "indexJobs_01.xml");
		
		compareTable(
				"index_word",
				"REPOSITORY_ID = '101' order by word",
				"indexJobs_01.xml");

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY term.id, dic.WORD",
				"indexJobs_01.xml", "index_word_object term, index_word dic");
		
	}



}
