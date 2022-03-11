package eu.trade.repo.util.io;

/**
 * {@link RuntimeException} when the input stream side of the pipe is closed.
 * 
 * TODO: Should be this a {@link java.io.IOException}?
 * 
 * @author abascis
 */
public class PipedInputStreamClosedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * @see java.lang.RuntimeException.RuntimeException()
	 */
	public PipedInputStreamClosedException() {
	}

	/**
	 * @see java.lang.RuntimeException.RuntimeException(Throwable)
	 */
	public PipedInputStreamClosedException(Throwable cause) {
		super(cause);
	}
}
