package eu.trade.repo.index.extractor.tikaext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.Executor;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import eu.trade.repo.index.IndexNotSupportedException;


/**
 * Reader for the text content from a given binary stream. This class
 * uses a background parsing task with a {@link Parser}
 * to parse the text content from
 * a given input stream. The {@link BodyContentHandler} class and a pipe
 * is used to convert the push-based SAX event stream to the pull-based
 * character stream defined by the {@link Reader} interface.
 *
 * @since Apache Tika 0.2
 */
public class RepoParsingReader extends Reader {

	/**
	 * Parser instance used for parsing the given binary stream.
	 */
	private final Parser parser;

	/**
	 * Buffered read end of the pipe.
	 */
	private final Reader reader;

	/**
	 * Write end of the pipe.
	 */
	private final Writer writer;

	/**
	 * The binary stream being parsed.
	 */
	private final InputStream stream;

	/**
	 * Metadata associated with the document being parsed.
	 */
	private final Metadata metadata;

	/**
	 * The parse context.
	 */
	private final ParseContext context;

	/**
	 * An exception (if any) thrown by the parsing thread.
	 */
	private transient Exception exception;

	/**
	 * Utility method that returns a {@link Metadata} instance
	 * for a document with the given name.
	 *
	 * @param name resource name (or <code>null</code>)
	 * @return metadata instance
	 */
	private static Metadata getMetadata(String name) {
		Metadata metadata = new Metadata();
		if (name != null && name.length() > 0) {
			metadata.set(Metadata.RESOURCE_NAME_KEY, name);
		}
		return metadata;
	}

	/**
	 * Creates a reader for the text content of the given binary stream.
	 *
	 * @param stream binary stream
	 * @throws IOException if the document can not be parsed
	 */
	public RepoParsingReader(InputStream stream) throws IOException {
		this(new RepoAutodectectParser(), stream, new Metadata(), new ParseContext());
		context.set(Parser.class, parser);
	}

	/**
	 * Creates a reader for the text content of the given binary stream
	 * with the given name.
	 *
	 * @param stream binary stream
	 * @param name document name
	 * @throws IOException if the document can not be parsed
	 */
	public RepoParsingReader(InputStream stream, String name) throws IOException {
		this(new RepoAutodectectParser(), stream, getMetadata(name), new ParseContext());
		context.set(Parser.class, parser);
	}

	/**
	 * Creates a reader for the text content of the given file.
	 *
	 * @param file file
	 * @throws IOException if the document can not be parsed
	 */
	public RepoParsingReader(File file) throws IOException {
		this(new FileInputStream(file), file.getName());
	}

	/**
	 * Creates a reader for the text content of the given binary stream
	 * with the given document metadata. The given parser is used for
	 * parsing. A new background thread is started for the parsing task.
	 * <p>
	 * The created reader will be responsible for closing the given stream.
	 * The stream and any associated resources will be closed at or before
	 * the time when the {@link #close()} method is called on this reader.
	 *
	 * @param parser parser instance
	 * @param stream binary stream
	 * @param metadata document metadata
	 * @throws IOException if the document can not be parsed
	 */
	public RepoParsingReader(
			Parser parser, InputStream stream, final Metadata metadata,
			ParseContext context) throws IOException {
		this(parser, stream, metadata, context, new Executor() {
			public void execute(Runnable command) {
				String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
				if (name != null) {
					name = "Apache Tika: " + name;
				} else {
					name = "Apache Tika";
				}
				Thread thread = new Thread(command, name);
				thread.setDaemon(true);
				thread.start();
			}
		});
	}

	/**
	 * Creates a reader for the text content of the given binary stream
	 * with the given document metadata. The given parser is used for the
	 * parsing task that is run with the given executor. The given executor
	 * <em>must</em> run the parsing task asynchronously in a separate thread,
	 * since the current thread must return to the caller that can then
	 * consume the parsed text through the {@link Reader} interface.
	 * <p>
	 * The created reader will be responsible for closing the given stream.
	 * The stream and any associated resources will be closed at or before
	 * the time when the {@link #close()} method is called on this reader.
	 *
	 * @param parser parser instance
	 * @param stream binary stream
	 * @param metadata document metadata
	 * @param context parsing context
	 * @param executor executor for the parsing task
	 * @throws IOException if the document can not be parsed
	 * @since Apache Tika 0.4
	 */
	public RepoParsingReader(
			Parser parser, InputStream stream, Metadata metadata,
			ParseContext context, Executor executor) throws IOException {
		this.parser = parser;
		PipedReader pipedReader = new PipedReader();
		this.reader = new BufferedReader(pipedReader);
		try {
			this.writer = new PipedWriter(pipedReader);
		} catch (IOException e) {
			// Should never happen
			throw new IllegalStateException(e); 
		}
		this.stream = stream;
		this.metadata = metadata;
		this.context = context;

		executor.execute(new RepoParsingTask());

		// TIKA-203: Buffer first character to force metadata extraction
		reader.mark(1);
		reader.read();
		reader.reset();
	}

	/**
	 * The background parsing task.
	 */
	private class RepoParsingTask implements Runnable {

		/**
		 * Parses the given binary stream and writes the text content
		 * to the write end of the pipe. Potential exceptions (including
		 * the one caused if the read end is closed unexpectedly) are
		 * stored before the input stream is closed and processing is stopped.
		 */
		public void run() {
			try {
				ContentHandler handler = new BodyContentHandler(writer);
				parser.parse(stream, handler, metadata, context);

			} catch (Exception e) {
				exception = e;
			}

			try {
				stream.close();
			} catch (IOException e) {
				if (exception == null) {
					exception = e;
				}
			}

			try {
				writer.close();
			} catch (IOException e) {
				if (exception == null) {
					exception = e;
				}
			}
		}

	}

	/**
	 * Reads parsed text from the pipe connected to the parsing thread.
	 * Fails if the parsing thread has thrown an exception.
	 *
	 * @param cbuf character buffer
	 * @param off start offset within the buffer
	 * @param len maximum number of characters to read
	 * @throws IOException if the parsing thread has failed or
	 *                     if for some reason the pipe does not work properly
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if ( exception instanceof IndexNotSupportedTikaException) {
			throw new IndexNotSupportedException(exception.getLocalizedMessage(), exception);
		} else if (exception instanceof IOException){
			throw (IOException) exception;
		} else if (exception != null) {
			IOException e = new IOException("", exception);
            throw e;
		}
		return reader.read(cbuf, off, len);
	}

	/**
	 * Closes the read end of the pipe. If the parsing thread is still
	 * running, next write to the pipe will fail and cause the thread
	 * to stop. Thus there is no need to explicitly terminate the thread.
	 *
	 * @throws IOException if the pipe can not be closed
	 */
	@Override
	public void close() throws IOException {
		reader.close();
	}

}
