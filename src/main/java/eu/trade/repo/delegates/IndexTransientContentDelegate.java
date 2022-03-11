package eu.trade.repo.delegates;

import java.io.IOException;
import java.io.Reader;

/**
 * Resposible for processing cmis object to obtain the content transient index.
 *
 */
public interface IndexTransientContentDelegate extends IndexTransientDelegate{
	
	/**
	 * Process the stream of a cmis object and creates its content transient index.
	 * @param repositoryId 
	 * @param objectId
	 * @param docReader cmis object content reader
	 * @param segmentSize number of tokens to be saved in a batch
	 * @param indexSizeLimit max number of tokens per cmis object content index
	 * @return true if content has been fully processed; false if the indexSizeLimit has been reached.
	 * @throws IOException
	 */
	boolean processContentToTransientIndex( Integer repositoryId, Integer objectId, Reader docReader, int segmentSize, int indexSizeLimit ) throws IOException;
}
