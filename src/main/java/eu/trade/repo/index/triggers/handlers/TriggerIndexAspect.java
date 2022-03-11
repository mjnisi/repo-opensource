package eu.trade.repo.index.triggers.handlers;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;
import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.index.txsync.IndexTransactionSynchronization;
import eu.trade.repo.index.txsync.IndexTransactionSynchronizationFactory;


@Aspect
public class TriggerIndexAspect extends BaseAspect{
	private static final Logger LOG = LoggerFactory.getLogger(TriggerIndexAspect.class);

	@Autowired
	private IndexConfigHolder indexConfigHolder;

	@Autowired
	private IndexTriggersDelegate indexTriggersDelegate;

	@Autowired
	private IndexTransactionSynchronizationFactory indexSynchFactory;


	@Before("@annotation(triggerIndex)")
	public void triggerIndex(JoinPoint joinPoint, TriggerIndex triggerIndex) {
		if (indexConfigHolder.getAtomicIndexEnabled()) {
			//create transaction synchronization
			IndexTransactionSynchronization synch = indexSynchFactory.getIndexTransactionSynchronization();
			boolean isNewSynch = registerSynchronization(synch);
			LOG.debug("Index synchronization ({}) is registered", (isNewSynch? "new" : "pre-existant"));
		}
	}


	private boolean registerSynchronization(TransactionSynchronization synchronization){
		List<TransactionSynchronization> synchList = TransactionSynchronizationManager.getSynchronizations();		
		boolean found = false;
		for (TransactionSynchronization sych : synchList) {
			if( sych instanceof IndexTransactionSynchronization){				
				found = true;
				break;
			}
		}
		if( !found ){
			TransactionSynchronizationManager.registerSynchronization(synchronization);			
			indexTriggersDelegate.initChangeTrackerMap();
		}
		return !found;
	}
}