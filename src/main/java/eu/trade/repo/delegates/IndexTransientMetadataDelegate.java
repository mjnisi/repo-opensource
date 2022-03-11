package eu.trade.repo.delegates;

import java.io.IOException;
import java.util.List;

import eu.trade.repo.model.Property;

/**
 * Resposible for saving index data in the transient index for METADATA operations
 *
 */
public interface IndexTransientMetadataDelegate extends IndexTransientDelegate{

	/**
	 * Process the metadata of a cmis object and creates its metadata transient index part.
	 * @param repositoryId
	 * @param objectId
	 * @param propertyList list of 'string' properties the cmis object metadata consists of
	 * @return true if metadata has been fully indexed; false otherwise
	 * @throws IOException
	 */
	boolean processMetadataToTransientIndex( Integer repositoryId, Integer objectId, List<Property> propertyList ) throws IOException;
}
