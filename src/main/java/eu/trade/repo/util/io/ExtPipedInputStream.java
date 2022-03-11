package eu.trade.repo.util.io;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ExtPipedInputStream extends PipedInputStream {

	private static final int DEFAULT_PIPE_SIZE = 1024;
	private boolean closed = false;
	
    public ExtPipedInputStream(PipedOutputStream src) throws IOException {
        super(src, DEFAULT_PIPE_SIZE);
    }

    public ExtPipedInputStream(PipedOutputStream src, int pipeSize) throws IOException {
         super(src, pipeSize);
    }

    public ExtPipedInputStream() {
        super();
    }
   
    public ExtPipedInputStream(int pipeSize) {
        super(pipeSize);
    }
    
	public boolean isClosed(){
		return closed;
	}
	
	@Override
	public void close()  throws IOException {
		super.close();
		closed = true;
	}
}
