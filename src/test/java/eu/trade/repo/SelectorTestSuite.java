package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.selectors.AclTestSelector;
import eu.trade.repo.selectors.ChangeLogSelectorTest;
import eu.trade.repo.selectors.ObjectParentSelector;
import eu.trade.repo.selectors.ObjectParentSelectorTest;
import eu.trade.repo.selectors.RepositoryTestSelector;

@RunWith(Suite.class)
@Suite.SuiteClasses({
					ChangeLogSelectorTest.class, 
					ObjectParentSelectorTest.class
					})

public class SelectorTestSuite {
	//RUNS EVERY DAO TEST
}