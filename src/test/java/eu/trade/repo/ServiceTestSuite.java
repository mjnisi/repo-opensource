package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.security.AclServiceTest;
import eu.trade.repo.service.CMISServiceTest;
import eu.trade.repo.service.ChangeLogTest;
import eu.trade.repo.service.DiscoveryServiceTest;
import eu.trade.repo.service.NavigationServiceTest;
import eu.trade.repo.service.ObjectServiceTest;
import eu.trade.repo.service.RepositoryServiceTest;
import eu.trade.repo.service.VersioningServiceTest;
import eu.trade.repo.service.cmis.CmisChangeLogEventTest;
import eu.trade.repo.service.cmis.CmisDiscoveryServiceTest;
import eu.trade.repo.service.cmis.CmisObjectServiceTest;
import eu.trade.repo.service.cmis.CmisPolicyServiceTest;
import eu.trade.repo.service.cmis.CmisRepositoryServiceTest;
import eu.trade.repo.service.cmis.CmisVersioningServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
					CMISServiceTest.class,
					ObjectServiceTest.class,
					RepositoryServiceTest.class,
					NavigationServiceTest.class,
					CmisObjectServiceTest.class,
					CmisRepositoryServiceTest.class,
					VersioningServiceTest.class,
					CmisDiscoveryServiceTest.class,
					DiscoveryServiceTest.class,
					CmisPolicyServiceTest.class,
					ChangeLogTest.class,
					AclServiceTest.class,
					CmisChangeLogEventTest.class,
					CmisVersioningServiceTest.class
					})

public class ServiceTestSuite {
	//RUNS EVERY SERVICE TEST
}