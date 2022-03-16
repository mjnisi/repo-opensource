package eu.trade.repo.query;

import java.util.Map;

import org.antlr.runtime.RecognitionException;

import eu.trade.repo.model.Repository;

public interface ConcreteQuery {
	Map<String, ? extends Object> executeLowLevelQuery(String cmisQuery, Repository repository, boolean searchAllVersions, boolean count, int offset, int pageSize, boolean isNormalizedQuery) throws RecognitionException;

}
