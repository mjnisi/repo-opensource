package eu.trade.repo.index.triggers.handlers;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.IndexingState;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;

public class PropertyChangeListener extends BaseChangeListener{

	private static final Logger LOG = LoggerFactory.getLogger(PropertyChangeListener.class);

	@PostPersist
	@PostUpdate
	public void propertyChanged(Property prop){
		if( getIndexConfigHolder().getAtomicIndexEnabled() ){
			ObjectTypeProperty propertyType = getEntityManager().find(ObjectTypeProperty.class, prop.getObjectTypeProperty().getId());
			if( PropertyType.STRING.equals(propertyType.getPropertyType())){ 
				LOG.debug("Cmis object (ID = {}) string property persisted or updated.", prop.getObject().getId());
				CMISObject object = getEntityManager().find(CMISObject.class, prop.getObject().getId());
				object.setIndexStateMetadata(IndexingState.NONE.getState());
				object.setIndexTriesMetadata(Integer.valueOf(0));

				getIndexTriggersDelegate().registerPropertyChange(prop.getObject().getId());
				LOG.debug("Cmis object (ID = {}) string property change registered.", prop.getObject().getId());
			}
		}
	}

	@PostRemove
	public void propertyRemoved(Property prop){
		if( getIndexConfigHolder().getAtomicIndexEnabled() ){

			CMISObject object = getEntityManager().find(CMISObject.class, prop.getObject().getId());
			if( null != object ){
				//cmis:object not removed

				ObjectTypeProperty propertyType = getEntityManager().find(ObjectTypeProperty.class, prop.getObjectTypeProperty().getId());
				if( PropertyType.STRING.equals(propertyType.getPropertyType())){ 

					LOG.debug("Cmis object (ID = {}) string property removed.", prop.getObject().getId());
					object.setIndexStateMetadata(IndexingState.NONE.getState());
					object.setIndexTriesMetadata(Integer.valueOf(0));
					getIndexTriggersDelegate().registerPropertyChange(prop.getObject().getId());
					LOG.debug("Cmis object (ID = {}) string property removal registered.", prop.getObject().getId());
				}
			}
		}
	}

}
