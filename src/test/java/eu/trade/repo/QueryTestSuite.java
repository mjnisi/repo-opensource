package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.query.QueryCaseInsensitiveTest;
import eu.trade.repo.query.QueryTest;
import eu.trade.repo.query.QueryParserTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
					QueryTest.class,
					QueryParserTest.class,
					QueryCaseInsensitiveTest.class
					})

public class QueryTestSuite {
	//RUNS EVERY SERVICE TEST
}