package eu.trade.repo.util.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ExtPipedOutputStream extends PipedOutputStream {

	private ExtPipedInputStream pipedInputStream;

	/**
	 * Creates a piped output stream connected to the specified piped
	 * input stream. Data bytes written to this stream will then be
	 * available as input from <code>snk</code>.
	 *
	 * @param      snk   The piped input stream to connect to.
	 * @exception  IOException  if an I/O error occurs.
	 */
	public ExtPipedOutputStream(PipedInputStream snk)  throws IOException {
		super(snk);
	}

	/**
	 * Creates a piped output stream that is not yet connected to a
	 * piped input stream. It must be connected to a piped input stream,
	 * either by the receiver or the sender, before being used.
	 *
	 * @see     java.io.PipedInputStream#connect(java.io.PipedOutputStream)
	 * @see     java.io.PipedOutputStream#connect(java.io.PipedInputStream)
	 */
	public ExtPipedOutputStream() {
		super();
	}

	@Override
	public synchronized void connect(PipedInputStream snk) throws IOException {
		super.connect(snk);
		if( snk instanceof ExtPipedInputStream ){
			pipedInputStream = (ExtPipedInputStream) snk;
		}
	}

	@Override
	public void write(int b)  throws IOException {
		if( isPipedInputStreamClosed() ){
			throw new PipedInputStreamClosedException();
		}
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		try {
			super.write(b, off, len);
		} catch(IOException e) {
			if( isPipedInputStreamClosed() ) {
				throw new PipedInputStreamClosedException(e);
			} else {
				throw e;
			}
		}
	}

	public boolean isPipedInputStreamClosed(){
		return null != pipedInputStream && pipedInputStream.isClosed();
	}
}
