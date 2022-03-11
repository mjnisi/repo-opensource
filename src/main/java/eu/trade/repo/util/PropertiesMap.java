package eu.trade.repo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertiesMap<T>  {
	private final Map<String, List<T>> map;

	public PropertiesMap(){
		map = new HashMap<String, List<T>>();
	}

	/**
	 * Append value to the list of values of propTypeCmisId
	 * 
	 * @param propTypeCmisId
	 * @param value
	 */
	public void put(String propTypeCmisId, T value){
		if ( null == map.get(propTypeCmisId) ){
			map.put(propTypeCmisId, new ArrayList<T>());
		}
		map.get(propTypeCmisId).add(value);
	}

	/**
	 * Sets the value of propTypeCmisId overriding the previous values
	 * @param propTypeCmisId
	 * @param values
	 */
	public void putAll(String propTypeCmisId, List<T> values){
		map.put(propTypeCmisId, values);
	}

	/**
	 * Returns the list of values for propTypeCmisId or null
	 * @param propTypeCmisId
	 * @return
	 */
	public List<T> get(String propTypeCmisId){
		return map.get(propTypeCmisId);
	}

	/**
	 * Returns the first value of the list or null
	 * 
	 * @param propTypeCmisId
	 * @return
	 */
	public T first(String propTypeCmisId) {
		return null != map.get(propTypeCmisId)? map.get(propTypeCmisId).get(0) : null;
	}

	/**
	 * @return The map size
	 */
	public int size() {
		return map.size();
	}

	/**
	 * @return the key set
	 */
	public Set<String> keySet() {
		return map.keySet();
	}

	/**
	 * @return whether the map is empty
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}
}
