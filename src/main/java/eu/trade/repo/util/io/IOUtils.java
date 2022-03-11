package eu.trade.repo.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * Copy from org.apache.commons.io.IOUtils, to close streams when pipedInputStream is closed.
 *
 */
public final class IOUtils {

	private static final int EOF = -1;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private IOUtils(){}
	
	public static long copyLarge(InputStream input, ExtPipedOutputStream output) throws IOException {
		return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
	}
	private static long copyLarge(InputStream input, ExtPipedOutputStream output, byte[] buffer)     throws IOException {
		long count = 0;
		int n = 0;
		try{
			while (EOF != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
				count += n;
			}
		}catch(PipedInputStreamClosedException e){
			output.close();
		}
		return count;
	}

}
