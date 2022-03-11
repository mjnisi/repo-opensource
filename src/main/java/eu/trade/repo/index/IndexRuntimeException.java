package eu.trade.repo.index;

import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public class IndexRuntimeException extends CmisRuntimeException {

	private static final long serialVersionUID = 5355118752053255090L;

	
	public IndexRuntimeException() {
		super();
	}
	
	public IndexRuntimeException(String message) {
		super(message);
	}

	public IndexRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}


}
