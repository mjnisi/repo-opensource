package eu.europa.ec.digit.cmisrepo.test.performance;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class CreateDocumentStructure extends AbstractCmisTestClient {

    public static void main(String[] args) throws Exception {
        Session cSessionAdmin = getSessionAdmin();
        final Session cSession1 = getSessionTest1();
        final Session cSession2 = getSessionTest2();
        final AtomicInteger cnt = new AtomicInteger();
        
        String cRootFolderId = cSession1.getRepositoryInfo().getRootFolderId();
        for (int i = 10; i < 500; i++) {
            ObjectId cFolderId = createFolder(cSessionAdmin, cRootFolderId, "top_" + i);
            for (int j = 0; j < 5; j++) {
                ObjectId cFolderIdSub = createFolder(cSessionAdmin, cFolderId.getId(), "sub_" + j);
                for (int m = 0; m < 4; m++) {
                    final ObjectId cFolderIdSubSub = createFolder(cSessionAdmin, cFolderIdSub.getId(), "subsub_" + j + "_" + m);
                    final String prefix = "doc_" + i + "_" + j + "_" + m + "_";
                    Thread cThr1 = new Thread() {
                        public void run() {
                            for (int n = 0; n < 5; n++) {
                                try {
                                    createPdfDocument(cSession1, cFolderIdSubSub, prefix + n, 0);
                                    if(cnt.incrementAndGet() % 50 == 0)
                                        System.out.println(cnt.get());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    Thread cThr2 = new Thread() {
                        public void run() {
                            for (int n = 5; n < 10; n++) {
                                try {
                                    createPdfDocument(cSession2, cFolderIdSubSub, prefix + n, 0);
                                    if(cnt.incrementAndGet() % 50 == 0)
                                        System.out.println(cnt.get());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    cThr1.start();
                    cThr2.start();
                    cThr1.join();
                    cThr2.join();
                }
            }
        }
    }
}