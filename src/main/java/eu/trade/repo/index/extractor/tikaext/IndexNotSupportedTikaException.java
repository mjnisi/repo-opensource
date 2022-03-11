package eu.trade.repo.index.extractor.tikaext;

import org.apache.tika.exception.TikaException;

/**
 * Extends TikaException to avoid logging errors parsing embedded objects 
 * 
 * @see ParsingEmbeddedDocumentExtractor
 *
 */
public class IndexNotSupportedTikaException extends TikaException {

	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_MESSAGE = "Indexing is not supported for the given content-type";

	public IndexNotSupportedTikaException() {
		super(DEFAULT_MESSAGE);
	}

	public IndexNotSupportedTikaException(String message) {
		super(message);
	}

}
