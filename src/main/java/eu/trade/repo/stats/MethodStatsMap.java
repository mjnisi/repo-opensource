package eu.trade.repo.stats;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ConcurrentHashMap;

public class MethodStatsMap<K, V> extends ConcurrentHashMap<K, V>{

	private static final long serialVersionUID = 1L;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	// Also make a removePropertyChangeListener method the same way.  
	public void addPropertyChangeListener(PropertyChangeListener l) {  
		pcs.addPropertyChangeListener(l);  
	}  
	// Override whatever Hashtable method you want; here I'm just doing put   
	public synchronized V put(K key, V value) {  
		V result = super.put(key, value);  
		pcs.firePropertyChange("contents", null, null);  
		return result;  
	}  
}
