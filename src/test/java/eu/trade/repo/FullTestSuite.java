package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.search.codecs.CMISPropertyTypeCodecTest;
import eu.trade.repo.security.CallContextHolderTest;
import eu.trade.repo.security.PermissionCacheTest;
import eu.trade.repo.security.SecurityTest;
import eu.trade.repo.service.cmis.CmisObjectServiceDefaultAclTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
		DaoTestSuite.class,
		ServiceTestSuite.class,
		QueryTestSuite.class,
		CallContextHolderTest.class,
		PermissionCacheTest.class,
		SelectorTestSuite.class,
		SecurityTest.class,
		IndexTestSuite.class,
		CMISPropertyTypeCodecTest.class,
		CmisObjectServiceDefaultAclTest.class
					})

public class FullTestSuite {
	//RUNS EVERY SERVICE TEST
}