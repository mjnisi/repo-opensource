package eu.trade.repo.index.extractor;

import java.io.InputStream;

/**
 * Resposible for extracting the stream's content of a cmis object.
 * @author abascis
 *
 */
public interface ContentExtractor {

	/**
	 * Obtains and parses the given document. Returns the extracted text content and its metadata in a wrapper object.
	 * 
	 * @param objectId
	 * @return
	 */
	ContentExtractorStreamResult extractContent(Integer objectId, String fileName);

	/**
	 * Parses the given document and returns the extracted text content and its metadata in a wrapper object.
	 * 
	 * @param objectId
	 * @return
	 */
	ContentExtractorStreamResult extractContentAsStream(InputStream docInputStream, String fileName);


}
