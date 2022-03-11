package eu.trade.repo.service.interfaces;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.spi.Holder;

import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.query.QueryResult;

public interface IDiscoveryService {

	QueryResult query(String statement, String repositoryId,
			boolean searchAllVersions,
			int maxItems,
			int skipCount,
			boolean isNormalizedQuery);

	int queryCount(String statement, String repositoryId, boolean searchAllVersions, boolean isNormalizedQuery);

	Map<String, String> getColumns(String statement);

	/**
	 * Will return all content changes after the one identifed by the supplied changeLogToken.
	 * @param repositoryId The repository 
	 * @param changeLogToken All content changes after the one identifed by this token. 
	 * @param maxItems The maximum items 
	 * @return The list of content changes, empty if the repository does not support the change log capability
	 */
	List<ChangeEvent> getContentChanges(String repositoryId, Holder<String> changeLogToken, BigInteger maxItems);
}