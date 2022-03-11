package eu.europa.ec.digit.cmisrepo.test.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class CreateFolderAndUploadDocuments extends AbstractCmisTestClient {

	public static void main(String[] args) throws Exception {
		Session cSession = getSessionAdmin();

		String cRootFolderId = cSession.getRepositoryInfo().getRootFolderId();
		ObjectId cFolderId = createFolder(cSession, cRootFolderId, "some_folder_" + System.currentTimeMillis());

		long nTime0 = System.currentTimeMillis();
		int nCntDocs = 1;
		for (int i = 0; i < nCntDocs; i++) {
			uploadOneDoc(cSession, cFolderId);
			if (i % 10 == 0)
				System.out.println(i);
		}
		
		System.out.println("Wall time spent: " + nCntDocs + " docs - " + (System.currentTimeMillis() - nTime0) + " ms");
		// Wall time spent: 100 docs - 14545
		// Wall time spent: 1000 docs - 265171 ms
	}

	static void uploadOneDoc(Session cSession, ObjectId cFolderId) throws Exception {
		String objectTypeId = getDocumentTestTypeId();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, "doc_name_" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
		InputStream cIn = null;

		try {
			cIn = new FileInputStream(new File("./resources/test_docs/test_doc_medium.pdf"));
			ContentStream cContent = getContentStreamForPdf(cSession, "test.pdf", cIn, 0);
			cSession.createDocument(properties, cFolderId, cContent, VersioningState.NONE);
		} finally {
			if (cIn != null)
				cIn.close();
		}
	}
}
