package eu.trade.repo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import eu.trade.repo.dao.tests.AclTest;
import eu.trade.repo.dao.tests.CMISObjectTest;
import eu.trade.repo.dao.tests.ChangeEventTest;
import eu.trade.repo.dao.tests.ObjectTypePropertiesTest;
import eu.trade.repo.dao.tests.ObjectTypeTest;
import eu.trade.repo.dao.tests.PermissionMappingsTest;
import eu.trade.repo.dao.tests.PermissionsTest;
import eu.trade.repo.dao.tests.RenditionTest;
import eu.trade.repo.dao.tests.RepositoryTest;
import eu.trade.repo.dao.tests.SecurityHandlerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
					AclTest.class,
					CMISObjectTest.class, 
					ObjectTypeTest.class,
					ObjectTypePropertiesTest.class,
					PermissionsTest.class,
					PermissionMappingsTest.class,
					RenditionTest.class, 
					RepositoryTest.class,
					ChangeEventTest.class,
					SecurityHandlerTest.class
					})

public class DaoTestSuite {
	//RUNS EVERY DAO TEST
}