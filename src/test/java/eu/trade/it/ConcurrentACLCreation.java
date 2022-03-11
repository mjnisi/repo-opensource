package eu.trade.it;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ConcurrentACLCreation extends AbstractSessionTest {
	private static final Logger LOG = LoggerFactory.getLogger(ConcurrentDocumentCreation.class);

	@Override
	public void run(Session session) throws Exception {
		String rootFolderId = session.getRepositoryInfo().getRootFolderId();
		LOG.debug("ConcurrentACLCreation");
		Map<String, Object> properties = new HashMap<>();
		properties.put(PropertyIds.NAME, "acl-test-" + System.currentTimeMillis());
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		ObjectId topLevelFolder = session.createFolder(properties, new ObjectIdImpl(rootFolderId));

		properties.put(PropertyIds.NAME, "LEVEL-1");
		ObjectId folderLevel1 = session.createFolder(properties, topLevelFolder);
		properties.put(PropertyIds.NAME, "LEVEL-2");
		ObjectId folderLevel2 = session.createFolder(properties, folderLevel1);

		List<Ace> acesToAdd = new ArrayList<>();
		Ace ace = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl("hadamto"), Arrays.asList("cmis:all"));
		acesToAdd.add(ace);
		session.applyAcl(topLevelFolder, acesToAdd, null, AclPropagation.PROPAGATE);

		List<Exception> exceptionCollector = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch countDownLatch = new CountDownLatch(2);
		executorService.execute(new Job1(session, topLevelFolder, "job1", countDownLatch, exceptionCollector));
		executorService.execute(new Job2(session, folderLevel2, "job2", countDownLatch, exceptionCollector));

		countDownLatch.await();
		LOG.debug("Both jobs are done, verifying results");
		Acl acl = session.getAcl(topLevelFolder, false);
		List<Ace> aces = acl.getAces();
		//top level folder, expected ACLs: cmis:anyone - cmis:all (inherited); hadamto - cmis:write (direct)
		Assert.assertEquals(2, aces.size());

		//bottom folder, expected ACLs: cmis:anyone - cmis:all (inherited); hadamto - cmis:write (inherited); cmis:read (direct)
		Acl level2Acls = session.getAcl(folderLevel2, false);
		Assert.assertEquals(3, level2Acls.getAces().size());

		((Folder)session.getObject(topLevelFolder)).deleteTree(true, UnfileObject.DELETE, true);
		executorService.shutdown();
	}


	private static class Job1 implements Runnable {

		private final ObjectId folderId;
		private final String label;
		private final Session session;
		private final CountDownLatch countDownLatch;
		private final List<Exception> exceptionCollector;

		public Job1(Session session, ObjectId folderId, String label,
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
				List<Ace> acesToRemove = new ArrayList<>();
				Ace ace = new AccessControlEntryImpl(
						new AccessControlPrincipalDataImpl("hadamto"),
						Arrays.asList("cmis:all"));
				acesToRemove.add(ace);
				Ace newAceToAdd = new AccessControlEntryImpl(
						new AccessControlPrincipalDataImpl("hadamto"),
						Arrays.asList("cmis:write"));
				ArrayList<Ace> acesToAdd = new ArrayList<>();
				acesToAdd.add(newAceToAdd);
				session.applyAcl(folderId, acesToAdd, acesToRemove, AclPropagation.PROPAGATE);

			} catch (Exception e) {
				exceptionCollector.add(e);
				LOG.debug(e.getMessage(), e);
			} finally {
				countDownLatch.countDown();
				LOG.debug(label + " finished");
			}
		}
	}

	private static class Job2 implements Runnable {

		private final ObjectId folderId;
		private final String label;
		private final Session session;
		private final CountDownLatch countDownLatch;
		private final List<Exception> exceptionCollector;

		public Job2(Session session, ObjectId folderId, String label,
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
				List<Ace> acesToRemove = new ArrayList<>();
				Ace ace = new AccessControlEntryImpl(
						new AccessControlPrincipalDataImpl("hadamto"),
						Arrays.asList("cmis:all"));
				acesToRemove.add(ace);
				Ace newAceToAdd = new AccessControlEntryImpl(
						new AccessControlPrincipalDataImpl("hadamto"),
						Arrays.asList("cmis:read"));
				ArrayList<Ace> acesToAdd = new ArrayList<>();
				acesToAdd.add(newAceToAdd);
				session.applyAcl(folderId, acesToAdd, acesToRemove, AclPropagation.PROPAGATE);

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
