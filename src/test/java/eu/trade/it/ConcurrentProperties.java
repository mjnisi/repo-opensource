package eu.trade.it;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConcurrentProperties extends AbstractSessionTest {

	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentProperties.class);
	
	@Override
	public void run(Session session) throws Exception {

		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "concurrent-test-properties-" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		Folder folder = session.getRootFolder().createFolder(properties);
		
		properties.put(PropertyIds.NAME, "uniqueName" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		ObjectId docId = folder.createDocument(properties, null, VersioningState.MAJOR);

		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch countDownLatch = new CountDownLatch(2);
		
		executorService.execute(new Job(session, docId, "job1", countDownLatch, exceptionCollector));
		executorService.execute(new Job(session, docId, "job2", countDownLatch,	exceptionCollector));
		
		LOG.debug("TEST is awaits");
		countDownLatch.await();
		LOG.debug("TEST is done - verifying exceptions");
		Assert.assertEquals(1, exceptionCollector.size());
		Exception exception = exceptionCollector.get(0);
		LOG.debug(exception.getClass().getName());
		Assert.assertTrue(exception instanceof CmisUpdateConflictException);
		LOG.debug("Exception message: " + exception.getMessage());
		
		//removes the folder in the end
		folder.deleteTree(true, UnfileObject.DELETE, false);
		executorService.shutdown();
	}

	
	private static class Job implements Runnable {

		private final ObjectId id;
		private final String label;
		private final Session session;
		private final CountDownLatch countDownLatch;
		private final List<Exception> exceptionCollector;

		public Job(Session session, ObjectId id, String label, 
				CountDownLatch countDownLatch, List<Exception> exceptionCollector) {
			this.session = session;
			this.id = id;
			this.label = label;
			this.countDownLatch = countDownLatch;
			this.exceptionCollector = exceptionCollector;
		}

		@Override
		public void run() {
			try {
				LOG.debug(label + " start");
				Document document = (Document) session.getObject(id);
				
				Map<String, Object> properties = new HashMap<>();
				properties.put(PropertyIds.DESCRIPTION, label);
				
				document.updateProperties(properties);

				LOG.debug(label + " updated property");

			} catch (Exception e) {
				exceptionCollector.add(e);
			} finally {
				countDownLatch.countDown();
				LOG.debug(label + " finished");
			}

		}

	}
	
}
