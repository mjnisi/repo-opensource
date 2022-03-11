package eu.trade.repo.delegates;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

/**
 * {@link FilterInputStream} that controls the max size of the underlying input stream in the read operation.
 * <p>
 * It also computes the actual size of the stream after had been read.
 * <p>
 * Note: This class is not thread safe.
 * 
 * @author porrjai
 */
public class MaxSizeInputStream extends FilterInputStream {

	private static final String MAX_SIZE_ERROR = "Content too big!. Actual size: %d, Max size allowed: %d";
	private final long maxSize;
	private long actualSize;
	private long lastMark;

	/**
	 * This boolean value (and the method's local copies) controls to not duplicate results from nested calls.
	 * <p>
	 * Depending on the underlying inputStream implementation a read/skip method call could imply nested read/skip mthods call.
	 * @See java.io.FilterInputStream.read(byte[])
	 */
	private boolean reading;


	/**
	 * New instance of an unknown size stream.
	 * <p>
	 * In this case, this class will compute the actual size of the stream after had been read.
	 * 
	 * @param in {@link InputStream} The source input stream.
	 * @param maxSize int The max size allowed for the input stream.
	 */
	public MaxSizeInputStream(InputStream in, long maxSize) {
		super(in);
		this.maxSize = maxSize;
		this.actualSize = 0;
		this.reading = false;
	}

	/**
	 * New instance of a known size stream.
	 * <p>
	 * In this case, this class will compute the actual size of the stream after had been read.
	 * 
	 * @param in {@link InputStream} The source input stream.
	 * @param size int The known size of the stream. When less than 0 then is the same as an unknown size.
	 * @param maxSize int The max size allowed for the input stream.
	 */
	public MaxSizeInputStream(InputStream in, long size, long maxSize) {
		this(in, maxSize);
		checkSize(size);
	}

	private void checkSize(long size) {
		if (size > maxSize) {
			throw new CmisConstraintException(String.format(MAX_SIZE_ERROR, size, maxSize));
		}
	}

	private void inc(boolean isMainReading, int byteRead) {
		if (isMainReading && byteRead != -1) {
			actualSize++;
			checkSize(actualSize);
		}
	}

	private void add(boolean isMainReading, long bytes) {
		if (isMainReading && bytes > 0) {
			actualSize += bytes;
			checkSize(actualSize);
		}
	}

	/**
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {
		boolean isMainReading = startReading();
		try {
			int result = super.read();
			inc(isMainReading, result);
			return result;
		} finally {
			finishReading(isMainReading);
		}
	}

	/**
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	@Override
	public int read(byte[] b) throws IOException {
		boolean isMainReading = startReading();
		try {
			int result = super.read(b);
			add(isMainReading, result);
			return result;
		} finally {
			finishReading(isMainReading);
		}
	}

	/**
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		boolean isMainReading = startReading();
		try {
			int result = super.read(b, off, len);
			add(isMainReading, result);
			return result;
		} finally {
			finishReading(isMainReading);
		}
	}

	/**
	 * @see java.io.FilterInputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {
		boolean isMainReading = startReading();
		try {
			long result = super.skip(n);
			add(isMainReading, result);
			return result;
		} finally {
			finishReading(isMainReading);
		}
	}

	/**
	 * @see java.io.FilterInputStream#mark(int)
	 */
	@Override
	public synchronized void mark(int readlimit) {
		super.mark(readlimit);
		if (markSupported()) {
			lastMark = actualSize;
		}
	}

	/**
	 * @see java.io.FilterInputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		super.reset();
		actualSize = lastMark;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return actualSize;
	}

	/**
	 * 
	 * @return true When this call is not nested.
	 */
	private boolean startReading() {
		if (reading) {
			return false;
		}
		reading = true;
		return true;
	}

	private void finishReading(boolean isMainReading) {
		if (isMainReading) {
			reading = false;
		}
	}
}
