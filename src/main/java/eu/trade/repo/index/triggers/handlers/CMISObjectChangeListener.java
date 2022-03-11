package eu.trade.repo.index.triggers.handlers;

import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trade.repo.model.CMISObject;

public class CMISObjectChangeListener extends BaseChangeListener {

	private static final Logger LOG = LoggerFactory.getLogger(CMISObjectChangeListener.class);

	@PostRemove
	public void cmisObjectRemoved(CMISObject object){
		if( getIndexConfigHolder().getAtomicIndexEnabled() ){
			getIndexTriggersDelegate().registerCMISObjectRemoval(object.getId());
			LOG.debug("Cmis object (ID = {}) removal registered ", object.getId());
		}
	}

	@PostUpdate
	public void cmisObjectUpdated(CMISObject object){
		LOG.debug("Cmis object (ID = {}) updated: content_state: {}; metadata_state: {}", object.getId(), object.getIndexStateContent(), object.getIndexStateMetadata());
	}

}
