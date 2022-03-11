
package eu.trade.repo.service;


import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Date;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.junit.Ignore;
import org.junit.Test;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.Repository;
import eu.trade.repo.util.Constants;

public class CMISServiceTest extends BaseTestClass {

	@Test
	@Ignore
	public void createObject(){
		CMISObject cmisObject = new CMISObject();

		ObjectType objectType = new ObjectType();
		objectType.setCmisId(Constants.TYPE_CMIS_DOCUMENT);
		objectType.setQueryName("cmis:document");
		cmisObject.setObjectType(objectType);

		Repository repository = new Repository();
		repository.setName("test_repo_01");

		cmisObject.setCmisObjectId(cmisObject.toString()+new Date().hashCode());
		assertNull(cmisObject.getId());
		cmisObject = objectService.createObject("test_repo_01", cmisObject, Collections.<Acl>emptySet(), Collections.<Acl>emptySet(), null, VersioningState.MAJOR, BaseTypeId.CMIS_DOCUMENT);
		assertNotNull(cmisObject.getId());
	}
}

