package eu.trade.repo.migration;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseTestClass;

/**
 * This class checks and migrates the repository from 
 * repo 1.1.0 (CMIS 1.0) to repo 1.2.0 (CMIS 1.1)
 * 
 * @author martjoe
 *
 */
public class MigrateRepositoryTest extends BaseTestClass {

	@Autowired
	private CMIS11Migrate cmis11Migrate;
	
	@Test
	public void migrationChecks() throws Exception {
		setScenario("repo_tst.xml", DatabaseOperation.CLEAN_INSERT);
		resetSequence("sq_object_type", 100000);
		resetSequence("sq_object_type_property", 100000);
		
		//we are looking for 0 errors
		Assert.assertTrue(cmis11Migrate.migration() == 0);
		
	}
}
