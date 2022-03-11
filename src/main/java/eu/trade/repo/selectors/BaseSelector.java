package eu.trade.repo.selectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.util.EntityManagerProxyBuilder;

@Transactional
public class BaseSelector {
	//MODEL
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerProxyBuilder entityManagerProxyBuilder;

	protected BaseSelector() {}
	//GETTERS/SETTERS
	public EntityManager getEntityManager() {	return entityManagerProxyBuilder.getInstance(entityManager);	}
	public void setEntityManager(EntityManager entityManager) {	this.entityManager = entityManager;	}

}
