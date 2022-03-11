package eu.trade.repo.index.model;

import java.util.HashMap;

public class PropertyWordObjectMap extends HashMap<Integer, WordObjectDTO> implements IWordObjectExtractor{

	private static final long serialVersionUID = 7633643994579438681L;

	@Override
	public Integer getIdFromTransientItem(TransientDTO item) {
		return ( null != item && null != get(item.getPropertyId()))? get(item.getPropertyId()).getId() : null;
	}

}
