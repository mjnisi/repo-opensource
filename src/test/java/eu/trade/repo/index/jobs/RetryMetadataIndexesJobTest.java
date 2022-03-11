package eu.trade.repo.index.jobs;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;

public class RetryMetadataIndexesJobTest extends BaseLobTestClass {

	@Autowired
	private RetryMetadataIndexesJob retryMetadataIndexesJob;
	

	@Test
	//TODO
	public void testSearchAndRegisterMetadataToIndex() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "baseRepo1.xml");
		setLobScenario(DatabaseOperation.INSERT, "scenarioIndexObject_06_metadata.xml");
		
		retryMetadataIndexesJob.execute();

//		compareLobTable("index_word", null, "indexJobs_02.xml");
//		compareLobTable("index_word_object", null, "indexJobs_02.xml");
		
		compareTable(
				"index_word",
				"REPOSITORY_ID = '101' order by word",
				"indexJobs_02.xml");

		compareTable(
				"index_word_object",
				"term.word_id = dic.ID ORDER BY term.id, dic.WORD",
				"indexJobs_02.xml", "index_word_object term, index_word dic");
		
	}



}
