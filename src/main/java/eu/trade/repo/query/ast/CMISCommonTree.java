package eu.trade.repo.query.ast;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.lucene.analysis.Analyzer;

import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.PropertySelector;

public class CMISCommonTree extends CommonTree {

	private Map<Integer, Path<?>> propertyJoinMap = null;
	private Analyzer fullTextAnalyzer = null;
	private boolean containsFulltext = false;
	private boolean calculateScore = false;
	private String scoreAlias = null;
	private boolean containsNullFilter = false;
	private boolean negateExpression = false;
	private CMISObjectSelector objSelector;
	private ObjectTypePropertySelector otpSelector;
	private PropertySelector propSelector;
	private List<Integer> subtypeIdsToInclude;
	private Set<String> fullTextTokens;
	private List<Subquery<Integer>> semiJoins;
	private Expression<BigDecimal> score = null;
	private boolean normalizeQuery = false;
	private boolean doSemiJoin = false;
	
	private List<Column> columns;
	private boolean columnsExtracted = false;
	
	private Table table;
	private boolean tableExtracted = false;
	
	private CMISCommonTree filter;
	private boolean filterExtracted = false;
	
	private List<Order> orderBy;
	private boolean orderByExtracted = false;
	
	protected EntityManager em;
	
	
	public CMISCommonTree() {}
	
	public CMISCommonTree(Token tok) {
		super(tok);
	}

	public CMISCommonTree(CMISCommonTree tree) {
		super(tree);
	}

	@Override
	public Tree dupNode() {
		return new CMISCommonTree(this);
	}
	
	public Expression<BigDecimal> getScore() {
		return getRoot().score;
	}
	
	public void setScore(Expression<BigDecimal> score) {
		getRoot().score = score;
	}
	
	public void setCalculateScore(boolean calculateScore) {
		getRoot().calculateScore = calculateScore;
	}
	
	public boolean isCalculateScore() {
		return getRoot().calculateScore;
	}
	
	public void setScoreAlias(String scoreAlias) {
		getRoot().scoreAlias = scoreAlias;
	}
	
	public String getScoreAlias() {
		return getRoot().scoreAlias;
	}
	
	public void addSemiJoin(Subquery<Integer> semiJoinSubquery) {
		getSemiJoins().add(semiJoinSubquery);
	}

	protected void setPropertyJoin(Integer objectTypePropertyId, Path<?> property) {
		getRoot().getPropertyJoinMap().put(objectTypePropertyId, property);
	}
	
	protected void addFullTextToken(String token) {
		Set<String> fullTextTokens = getRoot().fullTextTokens;
		if(fullTextTokens == null) {
			fullTextTokens = new HashSet<>();
			getRoot().fullTextTokens = fullTextTokens;
		}
		fullTextTokens.add(token);
	}
	
	public Set<String> getFullTextTokens() {
		return fullTextTokens;
	}
	
	public Map<Integer, Path<?>> getPropertyJoinMap() {
		if(propertyJoinMap == null) {
			propertyJoinMap = new HashMap<Integer, Path<?>>();
		}
		return propertyJoinMap;
	}
	
	public List<Subquery<Integer>> getSemiJoins() {
		if(getRoot().semiJoins == null) {
			getRoot().semiJoins = new ArrayList<>();
		}
		return getRoot().semiJoins;
	}
	
	public void setFullTextAnalyzer(Analyzer analyzer) {
		getRoot().fullTextAnalyzer = analyzer;
	}
	
	public Analyzer getFullTextAnalyzer() {
		return getRoot().fullTextAnalyzer;
	}
	
	public boolean containsFulltext() {
		return getRoot().containsFulltext;
	}
	
	public void setContainsFulltext(boolean containsFulltext) {
		getRoot().containsFulltext = containsFulltext;
	}
	
	public boolean containsNullFilter() {
		return getRoot().containsNullFilter;
	}
	
	public void setContainsNullFIlter(boolean containsNullFilter) {
		getRoot().containsNullFilter = containsNullFilter;
	}
	
	public void setCMISObjectSelector(CMISObjectSelector objSelector) {
		getRoot().objSelector = objSelector;
	}
	
	public CMISObjectSelector getCMISObjectSelector() {
		return getRoot().objSelector;
	}
	
	public void setNegateExpression(boolean negateExpression) {
		getRoot().negateExpression = negateExpression;
	}
	
	public boolean negateExpression() {
		return getRoot().negateExpression;
	}
	
	public void setEntityManager(EntityManager em) {
		getRoot().em = em;
	}
	
	public EntityManager getEntityManager() {
		return getRoot().em;
	}
	
	public void setObjectTypePropertySelector(ObjectTypePropertySelector otpSelector) {
		getRoot().otpSelector = otpSelector;
	}
	
	public ObjectTypePropertySelector getObjectTypePropertySelector() {
		return getRoot().otpSelector;
	}
	
	public void setPropertySelector(PropertySelector propSelector) {
		getRoot().propSelector = propSelector;
	}
	
	public PropertySelector getPropertySelector() {
		return getRoot().propSelector;
	}
	
	public void setSubtypeIdsToInclude(List<Integer> subtypeIdsToInclude) {
		getRoot().subtypeIdsToInclude = subtypeIdsToInclude;
	}
	
	public List<Integer> getSubtypeIdsToInclude() {
		return getRoot().subtypeIdsToInclude;
	}
	
	public boolean isNormalizeQuery() {
		return getRoot().normalizeQuery;
	}

	public void setNormalizeQuery(boolean normalizeQuery) {
		getRoot().normalizeQuery = normalizeQuery;
	}
	
	public boolean doSemiJoin() {
		return getRoot().doSemiJoin;
	}
	
	public void setDoSemiJoin(boolean doSemiJoin) {
		getRoot().doSemiJoin = doSemiJoin;
	}
	
	public List<Column> getColumns() {
		if(!getRoot().columnsExtracted) {
			getRoot().columns = (List<Column>) extractQueryParts().get("columns");
			getRoot().columnsExtracted = true;
		}
		return getRoot().columns;
	}
	
	public Table getTable() {
		if(!getRoot().tableExtracted) {
			getRoot().table = (Table) extractQueryParts().get("table");
			getRoot().tableExtracted = true;
		}
		return getRoot().table;
	}
	
	public CMISCommonTree getFilter() {
		if(!getRoot().filterExtracted) {
			getRoot().filter = (CMISCommonTree) extractQueryParts().get("filter");
			getRoot().filterExtracted = true;
		}
		return getRoot().filter;
	}
	public void setFilter(CMISCommonTree filter) {
		getRoot().filter = filter;
	}
	
	public List<Order> getOrderBy() {
		if(!getRoot().orderByExtracted) {
			getRoot().orderBy = (List<Order>) extractQueryParts().get("orderBy");
			getRoot().orderByExtracted = true;
		}
		return getRoot().orderBy;
	}

	private CMISCommonTree getRoot() {
		
		Tree node = this;
		Tree parent = this;
		
		do {
			node = parent;
		} while((parent = node.getParent()) != null);
		
		return (CMISCommonTree) node;
	}
	
	public Map<String, ?> extractQueryParts() {

		List<?> children = getRoot().getChildren();

		List<Column> columns = new ArrayList<Column>();
		Table table = null;
		CMISCommonTree filter = null;

		List<Order> orderBy = new ArrayList<>();

		for (Object child : children) {
			if (child instanceof Column) {
				columns.add((Column) child);
			} else if (child instanceof Table) {
				table = (Table) child;
			} else if (child instanceof CMISCommonTree && !(child instanceof Order)) {
				filter = (CMISCommonTree) child;
			} else if(child instanceof Order) {
				orderBy.add((Order) child);
			}
		}

		Map queryParts = new HashMap();

		queryParts.put("columns", columns);
		queryParts.put("table", table);
		queryParts.put("filter", filter);
		queryParts.put("orderBy", orderBy);

		return queryParts;
	}
}
