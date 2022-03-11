package eu.trade.repo.query;

import java.util.Set;

import eu.trade.repo.model.CMISObject;

public class QueryResult {

	private Set<CMISObject> result;
	private String query;
	
	public void setResult(Set<CMISObject> result) {
		this.result = result;
	}
	public Set<CMISObject> getResult() {
		return result;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	public String getQuery() {
		return query;
	}
}
