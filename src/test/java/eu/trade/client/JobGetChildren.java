package eu.trade.client;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobGetChildren extends BaseJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(JobGetChildren.class);
	
	private List<String> ids;
	private String label;
	private Session session;
	private CountDownLatch countDownLatch;
	private List<Exception> exceptionCollector;

	public JobGetChildren(List<String> ids, String label,
			   CountDownLatch countDownLatch,
			   List<Exception> exceptionCollector, BindingType type) {
		this.session = getSession(type);
		this.ids = ids;
		this.label = label;
		this.countDownLatch = countDownLatch;
		this.exceptionCollector = exceptionCollector;
	}

	@Override
	public void run() {
		
		Random random = new Random(System.currentTimeMillis()); 
		try {

			long sum = 0; 
			for(int i=0;i<ids.size();i++) {
				
				long pre = System.currentTimeMillis();
				ObjectInFolderList r = session.getBinding().getNavigationService().getChildren(
						session.getRepositoryInfo().getId(),
						ids.get(i),
						null,
						null,
						false,
						IncludeRelationships.NONE,
						null,
						false,
						new BigInteger("10"),
						BigInteger.ZERO,
						null);
				for(ObjectInFolderData o:r.getObjects()) {
					String name = o.getObject().getProperties().getProperties().get(PropertyIds.NAME).toString();
				}
				long delta = (System.currentTimeMillis() - pre);
				sum+=delta;
				//LOG.info(label + " time: " + delta);
				
				int delay =  100 + random.nextInt(100);
				Thread.sleep(delay);
				
			}
			//LOG.info(label + " AVG time: " + (sum/ids.size()));
			LOG.info(label + ";" + (sum/ids.size()));
			
		} catch (Exception e) {
			exceptionCollector.add(e);
			LOG.error(e.getMessage(), e);
		} finally {
			countDownLatch.countDown();
			LOG.debug(label + " finished");
		}
	}

}
