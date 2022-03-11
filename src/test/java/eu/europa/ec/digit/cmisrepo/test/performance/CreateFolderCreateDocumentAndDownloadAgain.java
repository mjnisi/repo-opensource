package eu.europa.ec.digit.cmisrepo.test.performance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class CreateFolderCreateDocumentAndDownloadAgain extends AbstractCmisTestClient {

	public static void main(String[] args) throws Exception {
		Session cSession = getSessionAdmin();
		String cRootFolderId = cSession.getRepositoryInfo().getRootFolderId();

		// Create folder
		ObjectId cFolderId = createFolder(cSession, cRootFolderId, "folder_" + System.currentTimeMillis());
		System.out.println(cFolderId);

		// Create document
		ObjectId cObjectId = createPdfDocument(cSession, cFolderId,
				"doc_example_" + System.currentTimeMillis() + ".pdf", 5);
		System.out.println(cObjectId);

		// Get document
		Document cDoc = (Document) cSession.getObject(cObjectId);

		InputStream cIn = cDoc.getContentStream().getStream();
		OutputStream cOut = new FileOutputStream(
				new File("./resources/test_DELETE_" + System.currentTimeMillis() + ".pdf"));
		byte[] anBytes = new byte[1024];
		int nBytesRead = 0;
		while ((nBytesRead = cIn.read(anBytes)) != -1) {
			cOut.write(anBytes, 0, nBytesRead);
		}

		cIn.close();
		cOut.flush();
		cOut.close();
	}
}
