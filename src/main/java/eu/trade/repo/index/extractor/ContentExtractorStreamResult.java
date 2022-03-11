package eu.trade.repo.index.extractor;

import java.io.Reader;

import org.apache.tika.metadata.Metadata;

/**
 * Utility Bean to store the the content reader and the metadata obtained by the parsing process.
 * 
 */
public class ContentExtractorStreamResult {
	
	private Metadata metadata;
	private Reader contentReader;
	
	public Metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
	public Reader getContentReader() {
		return contentReader;
	}
	public void setContentReader(Reader contentReader) {
		this.contentReader = contentReader;
	}
}
