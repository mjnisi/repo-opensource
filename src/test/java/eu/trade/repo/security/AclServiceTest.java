package eu.trade.repo.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.TestConstants;
import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.util.Constants;

public class AclServiceTest  extends BaseTestClass {
	@Autowired
	private Security security;

	@Before
	public void setScenario() throws Exception {
		setScenario("scenarioAclService.xml", DatabaseOperation.CLEAN_INSERT);
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD , TestConstants.TEST_REPO_2);
	}
	
	@Test
	public void testGetAcls() {
		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);
		CMISObject child= objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER1_CMISID);
		
		
		Set<Acl> pacls = aclService.getAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId()); 
		Set<Acl> cacls = aclService.getAcl(TestConstants.TEST_REPO_2, child.getCmisObjectId());
		
		assertEquals(pacls.size(), 1);
		assertEquals(cacls.size(), 0);
		

	}

	@Test
	@Transactional
	public void testApplyAcls() {
		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);
		Set<Acl> pacls = aclService.getAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId());
		assertEquals(pacls.size(), 1);
		
		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId("asd");
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));
		
		Set<Acl> aces = new HashSet<Acl>();
		aces.add(acl);
		aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.OBJECTONLY);
		
		pacls = aclService.getAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId());
		assertEquals(2, pacls.size());
	}
	
	@Test
	@Transactional
	public void testEscalatePrivilege() {
		
		setUser(TestConstants.ADMIN_USER, TestConstants.ADMIN_PWD, TestConstants.TEST_REPO_2);
		
		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);
		
		//Add READ permission to TEST2_USER
		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.TEST2_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_READ, TestConstants.TEST_REPO_2));
		
		Set<Acl> aces = new HashSet<Acl>();
		aces.add(acl);
		aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.OBJECTONLY);
		
		//TEST2_USER attempts to escalate privilege
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);
		
		acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.TEST2_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));
		
		aces = new HashSet<Acl>();
		aces.add(acl);
		
		boolean permissionDeniedEscalateOwnPrivilege = false;
		try {
			aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.OBJECTONLY);
		} catch(CmisPermissionDeniedException cpdex) {
			permissionDeniedEscalateOwnPrivilege = true;
		}
		assertTrue(permissionDeniedEscalateOwnPrivilege);
		
		acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.ADMIN_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));
		
		aces = new HashSet<Acl>();
		aces.add(acl);
		
		boolean permissionDeniedEscalateOthersPrivilege = false;
		try {
			aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.OBJECTONLY);
		} catch(CmisPermissionDeniedException cpdex) {
			permissionDeniedEscalateOthersPrivilege = true;
		}
		assertTrue(permissionDeniedEscalateOthersPrivilege);
		
	}

	@Test
	@Transactional
	public void testRemoveAcls() {
		//ACL OF A DOC
		CMISObject doc = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTDOC_CMISID);
		Set<Acl> dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());
		assertEquals(2, dacls.size());
		
		//remove an acl
		Set<Acl> tbrem = new HashSet<>();
		tbrem.add(dacls.iterator().next());
		aclService.applyAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId(), Collections.<Acl>emptySet(), tbrem, AclPropagation.OBJECTONLY);
		
		//ACL OF A FLDR
		dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());
		assertEquals(1, dacls.size());
		
		
		CMISObject subf = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTSUBFOLDER_CMISID);
		Set<Acl> facls = aclService.getAcl(TestConstants.TEST_REPO_2, subf.getCmisObjectId());
		assertEquals(2, facls.size());
		
		tbrem.clear();
		tbrem.add(facls.iterator().next());
		aclService.applyAcl(TestConstants.TEST_REPO_2, subf.getCmisObjectId(), Collections.<Acl>emptySet(), tbrem, AclPropagation.OBJECTONLY);

		facls = aclService.getAcl(TestConstants.TEST_REPO_2, subf.getCmisObjectId());
		assertEquals(1, facls.size());

	}
	

	/**
	 * 	//see Jira TDR-97 - Remove ACL objectonly (should "propagate" to inheriting children)
	 *  When removing from ParentNode with objectonly option, the child should not keep the inherited ace (since is not valid an inherited permission)
	 *	The child inherited ACEs that has been removed from the parent are changed to be direct ACEs
	 */
	@Test
	@Transactional
	public void testSpecialCases() {

		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);
		CMISObject doc = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTDOC_CMISID);
		
		Set<Acl> dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());
		assertEquals(2, dacls.size());

		Set<Acl> pacls = aclService.getAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId());
		assertEquals(1, pacls.size());
		
		//add an ACL to the parent and propagate it to the child doc
		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.ADMIN_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));
		
		Set<Acl> aces = new HashSet<Acl>();
		aces.add(acl);
		pacls = aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.PROPAGATE);
		dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());

		//Check new ACL was added to both parent folder and child doc, direct for parent, derived for child
		assertEquals(2, pacls.size());
		assertEquals(TestConstants.ADMIN_USER, ((Acl)pacls.toArray()[1]).getPrincipalId());
		assertEquals(Constants.CMIS_WRITE, ((Acl)pacls.toArray()[1]).getPermission().getName());
		assertEquals(true, ((Acl)pacls.toArray()[1]).getIsDirect());
		assertEquals(3, dacls.size());
		assertEquals(TestConstants.ADMIN_USER, ((Acl)dacls.toArray()[2]).getPrincipalId());
		assertEquals(Constants.CMIS_WRITE, ((Acl)dacls.toArray()[2]).getPermission().getName());
		assertEquals(false, ((Acl)dacls.toArray()[2]).getIsDirect());
		
		
		//Remove ACL from parent (objectonly)
		pacls = aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), Collections.<Acl>emptySet(), aces, AclPropagation.OBJECTONLY);
		dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());
		
		//Check that ACL was removed from parent but not child
		assertEquals(1, pacls.size());
		assertEquals(3, dacls.size());
		//check that child ACL is now direct
		assertEquals(TestConstants.ADMIN_USER, ((Acl)dacls.toArray()[2]).getPrincipalId());
		assertEquals(Constants.CMIS_WRITE, ((Acl)dacls.toArray()[2]).getPermission().getName());
		assertEquals(true, ((Acl)dacls.toArray()[2]).getIsDirect());
		
	}
	
	/**
	 * Consider an ACL applied to a parent and propagated also consider the same acl applied directly to some child of said parent.
	 * Now, when the ACL is again removed and propagated from the parent, the child should losr the inherited ACL while retaining the 
	 * direct one. 
	 */
	@Test
	@Transactional
	public void testSpecialCases2() {
		String test_user = "Aza";
		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);
		CMISObject doc = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTDOC_CMISID);

		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.ADMIN_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));

		Acl acl2 = new Acl();
		acl2.setIsDirect(true);
		acl2.setPrincipalId(test_user);
		acl2.setPermission(permSelector.getPermission(Constants.CMIS_ALL, TestConstants.TEST_REPO_2));

		Set<Acl> aces = new HashSet<Acl>();
		aces.add(acl);
		aces.add(acl2);
		//ACL and ACL2 assigned to parent and child (child indirectly)
		Set<Acl> pacls = aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.PROPAGATE);
		
		//ACL and ACL2 assigned to child (directly)
		aclService.applyAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.OBJECTONLY);
		
		//Child has 4 new ACL , 2 DIRECT 2 INDIRECT
		Set<Acl> dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());
		assertEquals(6, dacls.size());

		//Remove ACL2 from parent and propagate. Child should have lost ONLY its INDIRECT {admin:write} ACL
		aces.clear();
		aces.add(acl2);
		pacls = aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), Collections.<Acl>emptySet(), aces, AclPropagation.PROPAGATE);
		dacls = aclService.getAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId());

		//LEFT ARE THE 2ORIGNAL , THE 2DIRECTS AND THE INDIRECT {admin:write}
		assertEquals(5, dacls.size());

		assertEquals(test_user, ((Acl)dacls.toArray()[2]).getPrincipalId());
		assertEquals(Constants.CMIS_ALL, ((Acl)dacls.toArray()[2]).getPermission().getName());
		assertEquals(true, ((Acl)dacls.toArray()[2]).getIsDirect());

		assertEquals(TestConstants.ADMIN_USER, ((Acl)dacls.toArray()[3]).getPrincipalId());
		assertEquals(Constants.CMIS_WRITE, ((Acl)dacls.toArray()[3]).getPermission().getName());
		assertEquals(true, ((Acl)dacls.toArray()[3]).getIsDirect());

		assertEquals(TestConstants.ADMIN_USER, ((Acl)dacls.toArray()[4]).getPrincipalId());
		assertEquals(Constants.CMIS_WRITE, ((Acl)dacls.toArray()[4]).getPermission().getName());
		assertEquals(false, ((Acl)dacls.toArray()[4]).getIsDirect());
		
		//REMOVE THE INDIRECT ACL FROM CHILD - PRPAGATE, PARENT UNAFFECTED
		aces.clear();
		acl.setIsDirect(false);
		aces.add(acl);
		dacls = aclService.applyAcl(TestConstants.TEST_REPO_2, doc.getCmisObjectId(), Collections.<Acl>emptySet(), aces, AclPropagation.PROPAGATE);
		assertEquals(4, dacls.size());
		
		pacls = aclService.getAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId());
		assertEquals(2, pacls.size());
	}
	
	@Test (expected=CmisPermissionDeniedException.class)
	@Transactional
	public void testAccessControl() {
		setUser(TestConstants.TEST2_USER, TestConstants.TEST2_PWD, TestConstants.TEST_REPO_2);

		CMISObject parent = objectService.getObject(TestConstants.TEST_REPO_2, TestConstants.TESTFOLDER_CMISID);

		Acl acl = new Acl();
		acl.setIsDirect(true);
		acl.setPrincipalId(TestConstants.ADMIN_USER);
		acl.setPermission(permSelector.getPermission(Constants.CMIS_WRITE, TestConstants.TEST_REPO_2));

		Set<Acl> aces = new HashSet<Acl>();
		aces.add(acl);
		//NOT PERMITTED FOR TEST USER
		aclService.applyAcl(TestConstants.TEST_REPO_2, parent.getCmisObjectId(), aces, Collections.<Acl>emptySet(), AclPropagation.PROPAGATE);
	}
}