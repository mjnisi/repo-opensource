package eu.trade.repo.service.interfaces;

import javax.persistence.EntityManager;

import eu.trade.repo.model.DBEntity;

public interface ICMISBaseService {
	EntityManager getEntityManager();
	void setEntityManager(EntityManager entityManager);

	//INTERFACE
    <T extends DBEntity> T find(Class<T> clazz, Integer id) ;
	<T extends DBEntity> T merge(T entity) ;
	<T extends DBEntity> void persist(T entity) ;
	void flush() ;
	<T extends DBEntity> void removeDetached(T dbe);
	<T extends DBEntity> void remove(T dbe);
}
