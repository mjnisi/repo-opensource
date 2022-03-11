package eu.trade.it;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentMove extends AbstractSessionTest {
	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentMove.class);

	/**
	 * total folders = NUM_FOLDERS^MAX_DEPTH + NUM_FOLDERS^(MAX_DEPTH-1) + NUM_FOLDERS^(MAX_DEPTH-2)
	 * total folders = 5^3 + 5^2 + 5^1 = 155
	 * total documents = total folders * NUM_DOCS = 155 * 5 = 775
	 */
	private final int NUM_FOLDERS = 5;
	private final int NUM_DOCS = 5;
	private final int MAX_DEPTH = 3;
	
	/**
	 * This tests creates a structure with several folders and documents.
	 * Then:
	 *  - moves a light folder (2 concurrent jobs, one fails)
	 *  - moves a heavy folder (2 concurrent jobs, one fails)
	 *  - revert the heavy folder movement (2 concurrent jobs, one fails)
	 *  - revert the light folder movement (2 concurrent jobs, one fails)
	 * 
	 */
	@Override
	public void run(Session session) throws Exception {

		String folderName = "concurrent-test-move-" + System.currentTimeMillis();
		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, folderName);
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		Folder folder = session.getRootFolder().createFolder(properties);
		
		buildInitialStructure(folder);
		
		checkObjectCount(session, folder);
		
		LOG.debug("move light folder ...");
		testMove(session, 
				"/" + folderName + "/folder-0/folder-0/folder-0",
				"/" + folderName + "/folder-1",
				"folder-moved-1");

		checkObjectCount(session, folder);

		LOG.debug("move heavy folder ...");
		testMove(session, 
				"/" + folderName + "/folder-0",
				"/" + folderName + "/folder-1",
				"folder-moved-2");
		
		checkObjectCount(session, folder);
		
		LOG.debug("reverting changes 1#2 ...");
		//revert the changes
		testMove(session, 
				"/" + folderName + "/folder-1/folder-moved-2",
				"/" + folderName,
				null);
		LOG.debug("reverting changes 2#2 ...");
		testMove(session, 
				"/" + folderName + "/folder-1/folder-moved-1",
				"/" + folderName + "/folder-moved-2/folder-0",
				null);
		
		checkObjectCount(session, folder);

		//check the structure
		checkStructure(folder);
		
		//removes the folder in the end
		LOG.debug("deleting test folder ...");
		folder.deleteTree(true, UnfileObject.DELETE, false);
	}

	/**
	 * Build the test structure with several threads.
	 * 
	 * @param folder target folder
	 * @throws InterruptedException
	 */
	private void buildInitialStructure(Folder folder) throws InterruptedException {
		LOG.debug("creating structure ...");
		ExecutorService executorService = Executors.newFixedThreadPool(20);
		CountDownLatch countDownLatch = new CountDownLatch(
			calculateNumberObjects(MAX_DEPTH, NUM_FOLDERS));
		buildInitialStructure(folder, MAX_DEPTH, executorService, countDownLatch);
		countDownLatch.await();
		executorService.shutdown();
		LOG.debug("structure finished");
	}
	
	/**
	 * Buils the test structure (internal recursive method) 
	 * 
	 * @param folder target folder
	 * @param depth remaining depth, will run until depth = 0
	 * @param executorService
	 * @param countDownLatch
	 */
	private void buildInitialStructure(final Folder folder, final int depth, final ExecutorService executorService,
			final CountDownLatch countDownLatch) {
		
		if(depth > 0) {
			for(int i=0;i<NUM_FOLDERS;i++) {
				
				final String FOLDER_NAME = "folder-" + i;
				
				executorService.execute(new Runnable() {
					
					@Override
					public void run() {
						
						try {
							Map<String, String> properties = new HashMap<String,String>();
							properties.put(PropertyIds.NAME, FOLDER_NAME);
							properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
							Folder newFolder = folder.createFolder(properties);

							for(int j=0;j<NUM_DOCS;j++) {
								properties = new HashMap<String,String>();
								properties.put(PropertyIds.NAME, "document-" +j);
								properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
								newFolder.createDocument(properties, null, VersioningState.MAJOR);
							}
							
							buildInitialStructure(newFolder, depth -1, executorService, countDownLatch);
						} finally {
							countDownLatch.countDown();
						}
						
						
					}//run
					
				});//runnable
			}
			
			
		}
		
	}
	
	/**
	 * Calculates the number of objects
	 * 
	 * for depth 3 and num_folders 5:
	 * 
	 * (5 * 5 * 5) + (5 * 5) + (5) 
	 * 
	 * @return
	 */
	private int calculateNumberObjects(int depth, int num) {
		int total = 0;
		for(int i=depth;i>0;i--) {
			total += (int)Math.pow(num, i); 
		}
		return total;
	}
	

	
	/**
	 * Checks the structure of the test objects.
	 * 
	 * @param folder root folder
	 */
	private void checkStructure(Folder folder) {
		checkStructure(folder, 0);
	}
	
	/**
	 * Check recusrively the structure:
	 * 
	 *  - number of folders per level (NUM_FOLDERS)
	 *  - number of documents per level (NUM_DOCS)
	 * 
	 * @param folder target folder
	 * @param depth depths is growing recursively until reach MAX_DEPTH
	 */
	private void checkStructure(Folder folder, int depth) {
		
		if(depth < MAX_DEPTH) {
			//check folder structure
			ItemIterable<CmisObject> children = folder.getChildren();
			int folderCount = 0;
			int documentCount = 0;
			for(CmisObject object: folder.getChildren()) {
				
				if(object instanceof Folder) {
					folderCount++;
					checkStructure((Folder)object, depth + 1);
				} else if(object instanceof Document) {
					documentCount++;
				}
			}

			//no documents in first level
			if(depth > 0) {
				Assert.assertEquals("wrong doc count in folder " + folder.getPath(), NUM_DOCS, documentCount);
			}
			
			//no folders in last level
			if(depth < (MAX_DEPTH -1)) {
				Assert.assertEquals("wrong folder count in folder " + folder.getPath(), NUM_FOLDERS, folderCount);
			}
		}
	}

	/**
	 * Checks the global object count in the test folder.
	 * 
	 * @param session
	 * @param folder target folder
	 */
	private void checkObjectCount(Session session, Folder folder) {
		long t = System.currentTimeMillis();
		LOG.debug("start counting objects check ...");
		long folders = session.query("select * from cmis:folder where in_tree('"+folder.getId()+"')", true).getTotalNumItems();
		long documents = session.query("select * from cmis:document where in_tree('"+folder.getId()+"')", true).getTotalNumItems();
		Assert.assertEquals(calculateNumberObjects(MAX_DEPTH, NUM_FOLDERS), folders);
		Assert.assertEquals(calculateNumberObjects(MAX_DEPTH, NUM_FOLDERS) * NUM_DOCS, documents);
		LOG.debug("end counting objects check ..." + (System.currentTimeMillis() - t) + " ms");
	}

	/**
	 * Test a move operation.
	 * 
	 * The operation is executed several times for testing the concurency.
	 * 
	 * @param session
	 * @param sourcePath folder path to move
	 * @param targetPath target folder
	 * @param newName the same to be used to avoid name overlaping (ignored if null)
	 * @throws InterruptedException
	 */
	private void testMove(Session session, String sourcePath, String targetPath, String newName) throws InterruptedException {
		
		LOG.debug("testMove === ");
		LOG.debug(" sourcePath: " + sourcePath);
		LOG.debug(" targetPath: " + targetPath);
		
		Folder folderToMove = (Folder)session.getObjectByPath(sourcePath);
		//first update the name, the target folder already has a folder with this name
		if(newName != null) {
			folderToMove = (Folder)folderToMove.rename(newName);
		}
		
		Folder targetFolder = (Folder)session.getObjectByPath(targetPath);
		
		//folderToMove.move(new ObjectIdImpl(folderToMove.getParentId()), targetFolder);
		
		final int NUM_JOBS = 2;
		
		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(NUM_JOBS);
		CountDownLatch countDownLatch = new CountDownLatch(NUM_JOBS);
		
		for(int i=1;i<=NUM_JOBS;i++) {
			executorService.execute(new Job(session, folderToMove.getId(), targetFolder.getId(), "job" + i, countDownLatch, exceptionCollector));
		}

		LOG.debug("TEST is awaits");
		countDownLatch.await();
		LOG.debug("TEST is done - verifying exceptions");
		executorService.shutdown();
		
		Assert.assertEquals(NUM_JOBS - 1, exceptionCollector.size());
		Exception exception = exceptionCollector.get(0);
		LOG.debug(exception.getClass().getName());
		Assert.assertTrue(exception instanceof CmisNameConstraintViolationException);
		LOG.debug("Exception message: " + exception.getMessage());
	}

	
	/**
	 * Class used to implement concurrent movements.
	 *
	 */
	private static class Job implements Runnable {

		private final String sourceId;
		private final String targetId;
		private final String label;
		private final Session session;
		private final CountDownLatch countDownLatch;
		private final List<Exception> exceptionCollector;

		public Job(Session session, String sourceId, String targetId, String label,
				   CountDownLatch countDownLatch,
				   List<Exception> exceptionCollector) {
			this.session = session;
			this.sourceId = sourceId;
			this.targetId = targetId;
			this.label = label;
			this.countDownLatch = countDownLatch;
			this.exceptionCollector = exceptionCollector;
		}

		@Override
		public void run() {
			try {
				LOG.debug(label + " start, time: " + System.currentTimeMillis());

				Folder source = (Folder)session.getObject(sourceId);

				source.move(new ObjectIdImpl(source.getParentId()), new ObjectIdImpl(targetId));
				
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
