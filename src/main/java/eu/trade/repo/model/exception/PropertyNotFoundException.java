package eu.trade.repo.model.exception;

public class PropertyNotFoundException extends Exception {
	private static final long serialVersionUID = 7009353779589913772L;

	public PropertyNotFoundException() {
		super();
	}
	
	public PropertyNotFoundException(Throwable e) {
		super(e);
	}

	public PropertyNotFoundException(String msg) {
		super(msg);
	}

	public PropertyNotFoundException(String msg, Throwable e) {
		super(msg, e);
	}
}
