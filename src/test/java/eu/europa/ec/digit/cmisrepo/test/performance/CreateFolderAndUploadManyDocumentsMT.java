package eu.europa.ec.digit.cmisrepo.test.performance;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class CreateFolderAndUploadManyDocumentsMT extends AbstractCmisTestClient {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final Session cSession = getSessionAdmin();

		final String cRootFolderId = cSession.getRepositoryInfo().getRootFolderId();
		final ObjectId cFolderId = createFolder(cSession, cRootFolderId, "some_folder_" + System.currentTimeMillis());

		long nTime0 = System.currentTimeMillis();
		List<Thread> cLstThr = new LinkedList<>();
		final int nCntDocs = 10;
		final int nCntThreads = 10;
		final AtomicInteger cCnt = new AtomicInteger();
		final AtomicLong cLastCurrentTime = new AtomicLong(System.currentTimeMillis());

		final Queue<Long> cLstTime = new ConcurrentLinkedQueue<>();
		for (int j = 0; j < nCntThreads; j++) {
			final int nId = j;
			Thread cThr = new Thread() {
				public void run() {
					for (int i = 0; i < nCntDocs / nCntThreads; i++) {
						try {
							uploadOneDoc(cSession, cFolderId, "doc_name_" + nId + "_" + System.currentTimeMillis());
							int nCnt = cCnt.incrementAndGet();
							if (nCnt % 500 == 0) {
								cLstTime.add(System.currentTimeMillis() - cLastCurrentTime.get());
								cLastCurrentTime.set(System.currentTimeMillis());
								System.out.println(nCnt);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			};

			cThr.start();
			cLstThr.add(cThr);
		}

		for (Thread cThr : cLstThr) {
			cThr.join();
		}

		System.out.println("Wall time spent: " + nCntDocs + " docs - " + (System.currentTimeMillis() - nTime0) + " ms, " + nCntThreads + " threads");
		
		int nLoop = 0;
		for (Long cTime : cLstTime) {
			System.out.println(++nLoop + " " + cTime);
		}
	}

	static void uploadOneDoc(Session cSession, ObjectId cFolderId, String a_cName) throws Exception {
		String objectTypeId = getDocumentTestTypeId();
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.NAME, a_cName);
		properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
		InputStream cIn = null;

		try {
			createPdfDocument(cSession, cFolderId, a_cName, 20);
		} finally {
			if (cIn != null)
				cIn.close();
		}
	}
}
