package eu.trade.repo.index.extractor.tikaext;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TemporaryResources;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.SecureContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Extends Tika AutoDetectParser to throw an IndexNotSupportedTikaException when the mime-type is not set as allowed.
 *
 */
public class RepoAutodectectParser extends AutoDetectParser{

	private static final long serialVersionUID = 1L;


	public RepoAutodectectParser(){
		super(RepoConfig.getDefaultConfig());
	}

	public RepoAutodectectParser(RepoConfig config){
		super(config);
	}


	@Override
	public void parse(
			InputStream stream, ContentHandler handler,
			Metadata metadata, ParseContext context)
					throws IOException, SAXException, TikaException {

		TemporaryResources tmp = new TemporaryResources();
		try {
			TikaInputStream tis = TikaInputStream.get(stream, tmp);

			// Automatically detect the MIME type of the document
			MediaType type = super.getDetector().detect(tis, metadata);
			metadata.set(Metadata.CONTENT_TYPE, type.toString());

			if( !super.getParsers().containsKey(type) ){
				throw new IndexNotSupportedTikaException("Full text indexing is not supported for documents of type " + type );
			}

			// TIKA-216: Zip bomb prevention
			SecureContentHandler sch = new SecureContentHandler(handler, tis);
			try {
				// Parse the document
				super.parse(tis, sch, metadata, context);
			} catch (SAXException e) {
				// Convert zip bomb exceptions to TikaExceptions
				sch.throwIfCauseOf(e);
				throw e;
			}
		} finally {
			tmp.dispose();
		}

	}

}
