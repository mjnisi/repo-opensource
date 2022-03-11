package eu.trade.repo.query;

import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.lucene.analysis.Analyzer;

import eu.trade.repo.model.Repository;
import eu.trade.repo.selectors.WordSelector;

public abstract class ConcreteQueryImpl extends QueryImpl implements ConcreteQuery {
	
	@Override
	public Map<String, ? extends Object> executeLowLevelQuery(String cmisQuery, Repository repository, boolean searchAllVersions, boolean count, int offset, int pageSize, boolean isNormalizedQuery) throws RecognitionException { 
		return super.executeQuery(cmisQuery, repository, searchAllVersions, count, offset, pageSize, isNormalizedQuery);
	}	

}
