package eu.trade.repo.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.model.Repository;
import eu.trade.repo.query.Query;
import eu.trade.repo.query.QueryResult;
import eu.trade.repo.selectors.ChangeEventSelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.IDiscoveryService;

public class DiscoveryService implements IDiscoveryService {
	
	private static final Logger LOG = LoggerFactory.getLogger(DiscoveryService.class);
	
	@Autowired
	private Query query;

	@Autowired
	private ChangeEventSelector changeEventSelector;
	
	@Autowired
	private RepositorySelector repositorySelector;
	
	@Override
	public QueryResult query(String statement, String repositoryId, boolean searchAllVersions, int maxItems, int skipCount, boolean isNormalizedQuery) {
		LOG.debug("discovery: {}", statement);
		return query.executeQuery(statement, repositoryId, searchAllVersions, maxItems, skipCount, isNormalizedQuery);
	}

	@Override
	public int queryCount(String statement, String repositoryId, boolean searchAllVersions, boolean isNormalizedQuery) {

		try {
			return query.queryCount(statement, repositoryId, searchAllVersions, isNormalizedQuery);
		} catch (RecognitionException e) {
			throw new CmisConstraintException(e.getMessage(), e);
		}

	}

	/**
	 * @see eu.trade.repo.service.interfaces.IDiscoveryService#getColumns(java.lang.String)
	 */
	@Override
	public Map<String, String> getColumns(String statement) {
		try {
			return query.getQueryColumns(statement);
		} catch (RecognitionException e) {
			throw new CmisConstraintException(e.getMessage(), e);
		}
	}

	@Override
	public List<ChangeEvent> getContentChanges(String repositoryId,
			Holder<String> changeLogToken, BigInteger maxItems) {
		Repository repo = repositorySelector.getRepository(repositoryId);
		if (repo.getChanges() == CapabilityChanges.NONE) {
			return new ArrayList<ChangeEvent>(); 
		}
		
		return changeEventSelector.getChangeEvents(changeLogToken.getValue(), 
				repo.getId(), maxItems);
	}
}