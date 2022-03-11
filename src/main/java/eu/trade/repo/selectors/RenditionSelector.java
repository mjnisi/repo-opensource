package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.Rendition;

public class RenditionSelector extends BaseSelector {
    public List<Rendition> getAllRenditions() {
    	return getEntityManager()
    			.createNamedQuery("Rendition.all", Rendition.class)
    			.getResultList();
    }

    public List<Rendition> getObjectRenditions (Integer cmisObjId) {
    	return getEntityManager()
    			.createNamedQuery("Rendition.by_cmisid", Rendition.class)
    			.setParameter("cmisObjId", cmisObjId)
    			.getResultList();
    }

    public Rendition getRenditionByStreamId (String streamId) {
    	return getEntityManager()
    			.createNamedQuery("Rendition.by_stream", Rendition.class)
    			.setParameter("streamId", streamId)
    			.getSingleResult();
    }

}
