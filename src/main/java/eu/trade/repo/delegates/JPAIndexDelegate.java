package eu.trade.repo.delegates;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.index.model.IndexOperation;
import eu.trade.repo.index.model.IndexOperation.IndexOperationType;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;
import eu.trade.repo.model.Property;


public class JPAIndexDelegate implements IndexLinkedDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(JPAIndexDelegate.class);

	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void updateObjectIndexingState(Integer objectId, IndexingState state, IndexOperationType operationType){
		try{
			CMISObject obj = entityManager.find(CMISObject.class, objectId);
			
			if(null != obj){
				setIndexState ( obj, state, operationType);
				entityManager.merge(obj);
				entityManager.flush();
			}
		
		}catch(IllegalArgumentException | PersistenceException e){
			LOG.error(e.getLocalizedMessage(), e);
			throw e;
		}
	}

	@Transactional
	public IndexingState getObjectIndexingState(Integer objectId, IndexOperationType operationType){
		CMISObject obj = entityManager.find(CMISObject.class, objectId);
		return IndexOperationType.CONTENT.equals(operationType)? IndexingState.get(obj.getIndexStateContent()) : IndexingState.get(obj.getIndexStateMetadata());
	}

	@Transactional
	public CMISObject getCmisObject(Integer objectId){
		return entityManager.find(CMISObject.class, objectId);
	}

	@Override
	public List<Property> obtainPropertyListByObjectId(Integer objectId) {
		return entityManager
				.createNamedQuery("property.by_object_id_and_property_type", Property.class)
				.setParameter("objectId", objectId)
				.setParameter("propertyType", "string")
				.getResultList();
	}

	private void setIndexState(CMISObject obj, IndexingState state, IndexOperationType operationType){
		if( IndexOperationType.CONTENT.equals(operationType) ){
			obj.setIndexStateContent(state.getState());
			obj.setIndexTriesContent( obtainTries(state, obj.getIndexTriesContent()) );
		}else{
			obj.setIndexStateMetadata(state.getState());
			obj.setIndexTriesMetadata(obtainTries(state, obj.getIndexTriesMetadata()));
		}
	}

	private int obtainTries(IndexingState state, int currentTries){
		if( IndexingState.INDEXED.equals(state) || IndexingState.PARTIALLY_INDEXED.equals(state) ){
			return 0;
		}
		int sumTries = IndexingState.ERROR.getState() == state.getState()? 1 : 0;
		return currentTries + sumTries;
	}
}
