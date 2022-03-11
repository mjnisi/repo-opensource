package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.delegates.JDBCIndexDelegateTest;
import eu.trade.repo.delegates.JDBCIndexTransientContentDelegateTest;
import eu.trade.repo.delegates.JDBCLobDelegateTest;
import eu.trade.repo.index.impl.IndexExecutorSelectorTest;
import eu.trade.repo.index.impl.IndexSynchronizerImplTest;
import eu.trade.repo.index.impl.ReverseIndexCreatorTest;
import eu.trade.repo.index.impl.ops.ContentIndexOperatorTest;
import eu.trade.repo.index.impl.ops.IndexOperatorFactoryTest;
import eu.trade.repo.index.impl.ops.MetadataIndexOperatorTest;
import eu.trade.repo.index.jobs.CleanOrphanIndexesJobTest;
import eu.trade.repo.index.jobs.RetryContentIndexesJobTest;
import eu.trade.repo.index.jobs.RetryMetadataIndexesJobTest;
import eu.trade.repo.index.triggers.ChangeTrackerMapTest;
import eu.trade.repo.index.triggers.IndexTriggersDelegateTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	JDBCIndexDelegateTest.class,
	JDBCIndexTransientContentDelegateTest.class,
	JDBCLobDelegateTest.class, 
	ContentIndexOperatorTest.class,
	IndexOperatorFactoryTest.class,
	MetadataIndexOperatorTest.class,
	IndexExecutorSelectorTest.class,
	IndexSynchronizerImplTest.class,
	ReverseIndexCreatorTest.class,
//	IndexImplTest.class,
	CleanOrphanIndexesJobTest.class,
	RetryContentIndexesJobTest.class,
	RetryMetadataIndexesJobTest.class,
	ChangeTrackerMapTest.class,
	IndexTriggersDelegateTest.class
})

public class IndexTestSuite {
	//RUNS EVERY SERVICE TEST
}