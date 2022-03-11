package eu.europa.ec.digit.cmisrepo.test.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class TestAccess extends AbstractCmisTestClient {


	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.WARN);
		
		// Create a document as one user
		Session cSessionTest1 = getSessionTest1();
		String cRootFolderId = cSessionTest1.getRepositoryInfo().getRootFolderId();
		
//		Folder cRootfolder = (Folder) cSessionTest1.getObject(cRootFolderId);
//		Map<String, Object> properties = new HashMap<>();
//		properties.put(PropertyIds.NAME, "some_folder_" + System.currentTimeMillis());
//		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
//		OperationContext op = new OperationContextImpl();
//		op.setIncludeAcls(false);
//		Ace cAceRoot = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST1), Arrays.asList(PermissionsUtil.CMIS_READ));
//		cRootfolder.createFolder(properties, null, Arrays.asList(cAceRoot), null, op);
		
		Ace cAceFolder = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST1), Arrays.asList("cmis:all"));
		ObjectId cFolderId = createFolder(cSessionTest1, cRootFolderId, "some_folder_" + System.currentTimeMillis(), 
				null, Arrays.asList(cAceFolder), null);

		System.out.println("Folder permissions:");
		Acl cAclFolder = cSessionTest1.getAcl(cFolderId, false);
		for (Ace cAce : cAclFolder.getAces()) {
			System.out.println(cAce);
		}
		
		String objectTypeId = getDocumentTestTypeId();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, "doc_name_" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
		InputStream cIn = null;

		ObjectId cDocumentId = null;
		try {
			cIn = new FileInputStream(new File("./resources/test_docs/test_doc_medium.pdf"));
			ContentStream cContent = getContentStreamForPdf(cSessionTest1, "test" + System.currentTimeMillis() + ".pdf", cIn, 0);
			cDocumentId = cSessionTest1.createDocument(properties, cFolderId, cContent, VersioningState.NONE);
		} finally {
			if (cIn != null)
				cIn.close();
		}
		
		System.out.println("Document permissions:");		
		Acl cAclDocument = cSessionTest1.getAcl(cDocumentId, false);
		for (Ace cAce : cAclDocument.getAces()) {
			System.out.println(cAce);
		}

		// Access it as second user - expected to fail
		Session cSessionTest2 = getSessionTest2();
		try {
			cSessionTest2.getObject(cDocumentId);
			throw new RuntimeException("Should not get here");
		} catch (CmisPermissionDeniedException e1) {
			// expected
		}
		
		// First user grants access to second user
		CmisObject cDocument = cSessionTest1.getObject(cDocumentId);
		Ace cAceTest2 = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST2), Arrays.asList("cmis:all"));		
		cDocument.addAcl(Arrays.asList(cAceTest2), AclPropagation.OBJECTONLY);

		System.out.println("Document permissions 2:");		
		cAclDocument = cSessionTest1.getAcl(cDocumentId, false);
		for (Ace cAce : cAclDocument.getAces()) {
			System.out.println(cAce);
		}


		// Access as second user will not fail
		try {
			cSessionTest2.getObject(cDocumentId);
		} catch (Exception e) {
			// NOT expected
			throw new RuntimeException("Should not fail here", e);
		}
	}
}