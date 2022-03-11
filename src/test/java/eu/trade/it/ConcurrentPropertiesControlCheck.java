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
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConcurrentPropertiesControlCheck extends AbstractSessionTest {

	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentPropertiesControlCheck.class);
	
	@Override
	public void run(Session session) throws Exception {

		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "concurrent-test-properties-" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		Folder folder = session.getRootFolder().createFolder(properties);
		
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");

		final int NUM_DOCS = 10;
		ObjectId[] docIds = new ObjectId[NUM_DOCS];
		for(int i=0;i<NUM_DOCS; i++) {
			properties.put(PropertyIds.NAME, "uniqueName_" + i + "_" + System.currentTimeMillis());
			docIds[i] = folder.createDocument(properties, null, VersioningState.MAJOR);
		}

		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_DOCS);
		CountDownLatch countDownLatch = new CountDownLatch(NUM_DOCS);
		
		
		for(int i=0;i<NUM_DOCS; i++) {
			executorService.execute(new Job(session, docIds[i], "job" + i, countDownLatch, exceptionCollector));
		}
		
		LOG.debug("TEST is awaits");
		countDownLatch.await();
		LOG.debug("TEST is done - verifying exceptions");
		Assert.assertEquals(0, exceptionCollector.size());

		for(int i=0;i<NUM_DOCS; i++) {
			String description = session.getObject(docIds[i]).getPropertyValue(PropertyIds.DESCRIPTION);
			Assert.assertEquals("job" + i, description);
		}
		
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
				e.printStackTrace();
			} finally {
				countDownLatch.countDown();
				LOG.debug(label + " finished");
			}

		}

	}
	
}
