package eu.europa.ec.digit.cmisrepo.test.performance;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class CreateFolderAndUploadOneDocument extends AbstractCmisTestClient{

    public static void main(String[] args) throws Exception {
        Session cSession = getSessionAdmin();
        
        String cRootFolderId = cSession.getRepositoryInfo().getRootFolderId();
        ObjectId cFolderId = createFolder(cSession, cRootFolderId, "some_folder_" + System.currentTimeMillis());

        String objectTypeId = getDocumentTestTypeId();
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, "documentName");
        properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

        ObjectId cDocumentId = cSession.createDocument(properties, cFolderId, getStreamForPdf(cSession, "test.pdf", new File("./resources/test_docs/test_doc_medium.pdf")), VersioningState.NONE);
        System.out.println(cDocumentId);
    }
}
