package eu.trade.repo.index.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.delegates.JDBCLobDelegate;
import eu.trade.repo.index.extractor.tikaext.Repo;
import eu.trade.repo.util.Utilities;


/**
 * Resposible for extracting the stream's content of a cmis object.
 * 
 * It uses Tika to parse the stream content and JDBCLobDelegate to create a series of connected pipes from the cmis object stream in database 
 * and avoid loading the whole content in memory. This way, chuncks of the content are loaded into memory each time the parsing needs to move 
 * forward in reading the content.
 *
 */
public class ContentExtractorImpl implements ContentExtractor{

	private static final Logger LOG = LoggerFactory.getLogger(ContentExtractorImpl.class);


	@Autowired
	private JDBCLobDelegate lobDelegate;
	@Autowired
	private Repo tika;


	/**
	 * Gets from the database and parses the given document. Returns the extracted text content and its metadata in a wrapper object.
	 * Uses Tika as the extractor library. Tika auto-detects the document's format based in its mime-type.
	 * 
	 * (Note that this method does not close the stream. The method that call this one is responsible for this)
	 * @param objectId
	 * @return
	 */
	@Override
	public ContentExtractorStreamResult extractContent(Integer objectId, String fileName) {
		InputStream in = lobDelegate.getStream(objectId);
		return extractContentAsStream(in, fileName);
	}

	/**
	 * Parses the given document and returns the extracted text content and its metadata in a wrapper object.
	 * Uses Tika as the extractor library. Tika auto-detects the document's format based in its mime-type.
	 * 
	 * @param objectId
	 * @return
	 */
	@Override
	public ContentExtractorStreamResult extractContentAsStream(InputStream docInputStream, String fileName) {
		Metadata metadata = new Metadata();
		metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
		Reader fulltext = null;

		try{
			fulltext = tika.parse(docInputStream, metadata);

		} catch(IOException ioe) {
			Utilities.close(docInputStream);
			LOG.warn(ioe.getMessage(), ioe);
			throw new CmisStreamNotSupportedException("The document can not be read or parsed", ioe);
		} catch(Exception e){
			LOG.error(e.getMessage(), e);
			throw new CmisStreamNotSupportedException("Exception parsing", e);
		}

		ContentExtractorStreamResult result = new ContentExtractorStreamResult();
		result.setMetadata(metadata);
		result.setContentReader(fulltext);
		return result;
	}

}
