package eu.trade.repo.index.bug;

import java.util.Map;
import java.util.Set;

import eu.trade.repo.index.triggers.IndexTriggersDelegate;

public class StubIndexTriggersDelegate extends IndexTriggersDelegate {
	
	protected final static String MESSAGE_EXCEPTION = "Just throw exception when trying to syncronize the index part after commiting";
	
	private boolean raiseException;

	public boolean isRaiseException() {
		return raiseException;
	}

	public void setRaiseException(boolean raiseException) {
		this.raiseException = raiseException;
	}

	@Override
	public Map<Integer, Integer> getRepositoryByObjectMap(Set<Integer> changedObjectIds) {
		if(raiseException) {
			// just throw an Exception
			throw new RuntimeException(MESSAGE_EXCEPTION);
		} else {
			// execute the actual method
			return super.getRepositoryByObjectMap(changedObjectIds);
		}
	}
	
	
}
