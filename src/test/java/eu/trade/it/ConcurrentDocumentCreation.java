package eu.trade.it;


import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentDocumentCreation extends AbstractSessionTest {
	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentDocumentCreation.class);

	@Override
	public void run(Session session) throws Exception {
		String rootFolderId = session.getRepositoryInfo().getRootFolderId();
		LOG.debug("ConcurrentDocumentCreation");
		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "concurrent-test-" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		ObjectId folderId = session.createFolder(properties, new ObjectIdImpl(rootFolderId));

		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		CountDownLatch countDownLatch = new CountDownLatch(4);
		executorService.execute(new Job(session, folderId, "job1", countDownLatch, exceptionCollector));
		executorService.execute(new Job(session, folderId, "job2", countDownLatch, exceptionCollector));
		executorService.execute(new Job(session, folderId, "job3", countDownLatch, exceptionCollector));
		executorService.execute(new Job(session, folderId, "job4", countDownLatch, exceptionCollector));

		countDownLatch.await();
		Assert.assertEquals(3, exceptionCollector.size());

		//removes the folder in the end
		((Folder)session.getObject(folderId)).deleteTree(true, UnfileObject.DELETE, true);
		executorService.shutdown();
	}

	private static class Job implements Runnable {

		private final ObjectId folderId;
		private final String label;
		private final Session session;
		private final CountDownLatch countDownLatch;
		private final List<Exception> exceptionCollector;

		public Job(Session session, ObjectId folderId, String label,
				   CountDownLatch countDownLatch,
				   List<Exception> exceptionCollector) {
			this.session = session;
			this.folderId = folderId;
			this.label = label;
			this.countDownLatch = countDownLatch;
			this.exceptionCollector = exceptionCollector;
		}

		@Override
		public void run() {
			try {
				LOG.debug(label + " start, time: " + System.currentTimeMillis());
				Map<String, Object> properties = new HashMap<>();
				properties.put(PropertyIds.NAME, "TheSameNameTest");

				if(label.equals("job2") || label.equals("job1")){
					properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
					session.createFolder(properties, folderId);
				} else {
					properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
					session.createDocument(properties, folderId, null, VersioningState.MAJOR, null, null, null);
				}

			} catch (Exception e) {
				exceptionCollector.add(e);
				LOG.debug(e.getMessage(), e);
			} finally {
				countDownLatch.countDown();
				LOG.debug(label + " finished");
			}
		}

	}
}
