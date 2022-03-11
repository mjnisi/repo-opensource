package eu.trade.repo.index;

import java.io.IOException;

import org.apache.tika.extractor.ParsingEmbeddedDocumentExtractor;

/**
 * Extends TikaException to avoid logging errors parsing embedded objects 
 * 
 * @see ParsingEmbeddedDocumentExtractor
 *
 */
public class IndexNotSupportedException extends IOException {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Indexing is not supported for the given content-type";

	public IndexNotSupportedException() {
		super(DEFAULT_MESSAGE);
	}

	public IndexNotSupportedException(String message) {
		super(message);
	}

	public IndexNotSupportedException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
