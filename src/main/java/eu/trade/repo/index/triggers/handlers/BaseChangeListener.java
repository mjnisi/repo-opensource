package eu.trade.repo.index.triggers.handlers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import eu.trade.repo.index.impl.IndexConfigHolder;
import eu.trade.repo.index.triggers.IndexTriggersDelegate;


public class BaseChangeListener{

	private EntityManagerFactory entityManagerFactory;
	private IndexConfigHolder configHolder;
	private IndexTriggersDelegate indexTriggersDelegate;


	protected EntityManager getEntityManager(){
		EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.getResource(getEntityManagerFactory());
		return emHolder.getEntityManager();
	}

	protected IndexConfigHolder getIndexConfigHolder(){
		if( null == configHolder ){
			configHolder = ApplicationContextProvider.getApplicationContext().getBean(IndexConfigHolder.class);
		}
		return configHolder;
	}

	protected IndexTriggersDelegate getIndexTriggersDelegate(){
		if( null == indexTriggersDelegate ){
			indexTriggersDelegate = ApplicationContextProvider.getApplicationContext().getBean(IndexTriggersDelegate.class);
		}
		return indexTriggersDelegate;
	}

	private EntityManagerFactory getEntityManagerFactory(){
		if( null == entityManagerFactory ){
			entityManagerFactory = (EntityManagerFactory)ApplicationContextProvider.getApplicationContext().getBean("entityManagerFactory");
		}
		return entityManagerFactory;
	}
}
