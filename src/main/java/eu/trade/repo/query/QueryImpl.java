package eu.trade.repo.query;

import static eu.trade.repo.util.Constants.CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY;
import static eu.trade.repo.util.Constants.CMIS_QUERY_SCORE;
import static eu.trade.repo.util.Constants.CMIS_SCORE_PROP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryException;
import org.apache.lucene.analysis.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.Score;
import eu.trade.repo.model.ScoreId;
import eu.trade.repo.query.ast.CMISCommonErrorNode;
import eu.trade.repo.query.ast.CMISCommonTree;
import eu.trade.repo.query.ast.CMISQueryFilter;
import eu.trade.repo.query.ast.Column;
import eu.trade.repo.query.ast.ComparisonFilter;
import eu.trade.repo.query.ast.Order;
import eu.trade.repo.query.ast.Table;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.selectors.PropertySelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.selectors.WordSelector;
import eu.trade.repo.util.Constants.ORDER_DIR;
import eu.trade.repo.util.EntityManagerProxyBuilder;


/**
 * 
 * @author kardaal
 *
 */
@Transactional
public abstract class QueryImpl implements Query {

	private static final Logger LOG = LoggerFactory.getLogger(QueryImpl.class);

	private static final String UNCHECKED = "unchecked";
	private static final String RESULTS = "results";
	private static final String OBJECT = "object";
	private static final String AND = "and";
	private static final String OR = "or";
	private static final String NOT = "not";
	private static final String COUNT = "count";
	private static final String OBJECT_ID = "object_id";

	private static final CmisNotSupportedException NOT_YET = new CmisNotSupportedException("Not yet implemented");

	private static final ORDER_DIR DEFAULT_ORDER_DIR = ORDER_DIR.ASC;

	private enum SUBTYPES {ALL, INCLUDE_IN_SUPERTYPE_QUERY, EXCLUDE_FROM_SUPERTYPE_QUERY}

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerProxyBuilder entityManagerProxyBuilder;

	@Autowired
	private ObjectTypeSelector objTypeSelector;

	@Autowired
	private RepositorySelector repSelector;

	@Autowired
	private CMISObjectSelector objSelector;

	@Autowired
	private ObjectTypePropertySelector otpSelector;

	@Autowired
	private PropertySelector propSelector;

	@Autowired
	private WordSelector wordSelector;

	@Autowired
	private Security security;

	@Override
	public QueryResult executeQuery(String statement, String repositoryId, boolean searchAllVersions, int maxItems, int skipCount, boolean isNormalizedQuery) {
		try {
			Repository repository = repSelector.getRepository(repositoryId);
			CapabilityQuery capabilityQuery = repository.getQuery();		

			checkQueryCapability(capabilityQuery);
			checkAllVersionSearchableCapability(repository.getAllVersionsSearchable(), searchAllVersions);

			Map results =  executeQuery(statement, repository, searchAllVersions, false, skipCount, maxItems, isNormalizedQuery);
			String query = (String) results.get("query");

			List<Object> idsAndScores = (List<Object>) results.get(RESULTS);
			if (!idsAndScores.isEmpty()) {

				List<Integer> ids = new ArrayList<>();
				Map<Integer, Double> scoresByObjectId = new HashMap<>();

				if(idsAndScores.get(0) instanceof Tuple) {
					for(Object idAndScore : idsAndScores) {

						boolean hasScore = ((Tuple) idAndScore).getElements().size()>1;

						ids.add((Integer) ((Tuple) idAndScore).get(OBJECT_ID));
						if(hasScore && ((Tuple) idAndScore).get(CMIS_QUERY_SCORE) != null){
							scoresByObjectId.put((Integer)((Tuple) idAndScore).get(OBJECT_ID), ((BigDecimal)((Tuple) idAndScore).get(CMIS_QUERY_SCORE)).doubleValue());
						}
					}
				} else {
					for(Object idAndScore : idsAndScores) {
						ids.add((Integer) idAndScore);
					}
				}

				Set<CMISObject> queryObjects = objSelector.getObjectsWhereIdInList(ids);

				for(CMISObject cmisObject : queryObjects) {
					cmisObject.setScore(scoresByObjectId.get(cmisObject.getId()));
				}

				TreeSet<CMISObject> orderedSet = new TreeSet<CMISObject>(new CMISObject.CmisIdComparator(ids));
				orderedSet.addAll(queryObjects);

				QueryResult qr = new QueryResult();
				qr.setQuery(query);
				qr.setResult(orderedSet);

				return qr;
			} else {

				QueryResult qr = new QueryResult();
				qr.setQuery(query);
				qr.setResult(Collections.<CMISObject>emptySet());

				return qr;
			}

		} catch (RecognitionException e) {
			throw new CmisConstraintException(e.getMessage(), e);
		}
	}

	@Override
	public Map<String, String> getQueryColumns(String cmisQuery) throws RecognitionException {

		List<Column> columns = (List<Column>)parseQuery(cmisQuery).extractQueryParts().get("columns");

		Map<String, String> queryColumns = new HashMap<>();

		for(Column column : columns) {
			String columnKey = column.getName();
			if(columnKey == null) {
				columnKey = column.getFunction();
			}
			queryColumns.put(columnKey, column.getAlias());
		}

		return queryColumns;
	}

	/**
	 * Tranforms the CMIS-QL query to JPA->SQL and executes.
	 * 
	 * The AST root node for the query is used to store global variables related to the query to ensure thread safety.
	 * 
	 * @param cmisQuery
	 * @param repositoryCMISId
	 * @param searchAllVersions
	 * @param count
	 * @param offset
	 * @param pageSize
	 * @return
	 * @throws RecognitionException
	 */
	@SuppressWarnings(UNCHECKED)
	protected Map<String, ? extends Object> executeQuery(String cmisQuery, Repository repository, boolean searchAllVersions, boolean count, int offset, int pageSize, boolean isNormalizedQuery) throws RecognitionException {

		LOG.debug(cmisQuery);
		
		//--> validate some input parameters (offset, pagesize)
		validatePaging(offset, pageSize);
		
		CMISCommonTree ast = parseQuery(cmisQuery);
		
		Map queryParts = ast.extractQueryParts();

		//TODO: order by virtual alias works?
		String scoreAlias = extractScoreAlias(queryParts);
		updateCalculateScoreFlag(ast, scoreAlias);

		Table table = (Table) queryParts.get("table");
		List<Order> orderBy = (List<Order>) queryParts.get("orderBy");	

		//--> retrieve rootType & validate it is queryable
		ObjectType rootType = getObjectType(repository, table);
		validateObjectTypeQueryable(rootType);

		final Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties = rootType.getObjectTypePropertiesIncludeParents(false, true);
		
		validateOrderBy(scoreAlias, table, orderBy, objectTypeProperties);

		modifyAstIsLatestVersion(searchAllVersions, ast, table, repository);

		List<Integer> subtypeIdsToInclude = getSubtypes(rootType, SUBTYPES.INCLUDE_IN_SUPERTYPE_QUERY);
		subtypeIdsToInclude.add(rootType.getId());				

		ast.setSubtypeIdsToInclude(subtypeIdsToInclude);
		
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createTupleQuery();
		
		//---> BUILD SUBQUERY & ASSOCIATE ROOTS/SEMI-JOINS
		
		Subquery<Integer> selectionQuery = c.subquery(Integer.class);

		List<Predicate> criteria = new ArrayList<Predicate>();

		criteria.addAll(buildQuery(repository, rootType, objectTypeProperties, cb, selectionQuery, ast, isNormalizedQuery));
		
		Root<?> [] roots = selectionQuery.getRoots().toArray(new Root<?>[selectionQuery.getRoots().size()]);

		for(int i=1; i<roots.length; i++) {
			criteria.add(cb.equal(roots[0].<CMISObject>get(OBJECT).<Integer>get("id"), roots[i].<CMISObject>get(OBJECT).<Integer>get("id")));
		}
		
		setSemiJoinsRootIds(ast, cb, roots);
		
		//---> RESTRICT SUBQUERY OBJECT-IDs BY ACL
		addPropertyAclRestriction(repository.getCmisId(), roots[0], selectionQuery, cb, criteria);
		
		selectionQuery.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		
		List<Predicate> mainQueryCriteria = new ArrayList<Predicate>();
		
		Root<CMISObject> queryRoot = c.from(CMISObject.class);
		
		boolean groupByObjectId = false;
		
		//--> JOIN SCORE_VIEW TO RETRIEVE SCORES FOR FULLTEXT
		groupByObjectId = joinScoreView(count, ast, repository, cb, queryRoot, groupByObjectId);
		
		//--> ORDER_BY
		groupByObjectId = applyOrderBy(count, ast, orderBy, objectTypeProperties, cb, c, mainQueryCriteria, queryRoot, groupByObjectId);
		
		if(groupByObjectId) {
			c.groupBy(queryRoot.<Integer>get("id"));
		}
		
		return doQuery(count, offset, pageSize, ast, cb, c, selectionQuery, mainQueryCriteria, queryRoot);
	}

	private Map<String, Object> doQuery(boolean count, int offset, int pageSize, CMISCommonTree ast, CriteriaBuilder cb, CriteriaQuery<Tuple> c, Subquery<Integer> selectionQuery, List<Predicate> mainQueryCriteria, Root<CMISObject> queryRoot) {
		Expression<Integer> objectId = queryRoot.get("id");
		mainQueryCriteria.add(queryRoot.<Integer>get("id").in(selectionQuery));
		c.where(cb.and(mainQueryCriteria.toArray(new Predicate[mainQueryCriteria.size()])));
		
		if(ast.getScore() != null) {
			c.multiselect(objectId.alias(OBJECT_ID), ast.getScore().alias(CMIS_QUERY_SCORE));
		} else {
			if(count) {
				c.select(cb.tuple(cb.count(objectId).alias(COUNT)));
			} else {
				c.multiselect(objectId.alias(OBJECT_ID));
			}
		}
		
		TypedQuery<Tuple> query = getEntityManager().createQuery(c);

		Long rsCount = -1L;

		List<Tuple> resultSet = null;

		if(count) {
			rsCount = (Long) query.getResultList().get(0).get(COUNT);
		} else {
			resultSet = doQuery(offset, pageSize, query);
		}

		Map<String, Object> results = new HashMap<String, Object>();

		results.put(COUNT, rsCount);
		results.put(RESULTS, resultSet);
		results.put("query", query.unwrap(org.hibernate.Query.class).getQueryString());
		return results;
	}

	/**
	 * If query asks for score e.g. <b>select SCORE() from cmis:document where contains(...)</b>
	 * then sets the appropriate flag in the AST root node. This flag is then checked during the
	 * process of building the JPA Criteria query, in order to add an appropriate join with the
	 * score_view view. The score alias e.g. <b>select SCORE() myScore</b> is also stored in the
	 * root node to be passed to the rest of the process, as this may be used to order the results
	 * by score.
	 * 
	 * NOTE: The query can only <b>select SCORE()</b> if it also uses CONTAINS(...), as scores are
	 * calculated only for fulltext or combination (fulltext + metadata) queries.
	 * 
	 * @param ast
	 * @param queryParts
	 * @return The alias for the SCORE() function.
	 */
	private void updateCalculateScoreFlag(CMISCommonTree ast, String scoreAlias) {
		
		if(scoreAlias != null) {
			ast.setCalculateScore(true);
			ast.setScoreAlias(scoreAlias);
		}
		
	}

	private boolean applyOrderBy(boolean count, CMISCommonTree ast, List<Order> orderBy, final Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, CriteriaQuery<Tuple> c, List<Predicate> mainQueryCriteria, Root<CMISObject> queryRoot, boolean groupByObjectId) {
		
		if(!orderBy.isEmpty() && !count) {
			
			List<javax.persistence.criteria.Order> orders = new ArrayList<>();
			
			for(int i=0; i<orderBy.size(); i++) {
			
				if(!isOrderByScore(orderBy.get(i), ast)) {
				
					Join<CMISObject, Property> propertyJoin = queryRoot.join("properties", JoinType.LEFT);
					
					mainQueryCriteria.add(cb.equal(propertyJoin.<ObjectTypeProperty>get("objectTypeProperty").<Integer>get("id"), objectTypeProperties.get(orderBy.get(i).getName()).first().getId()));
					
					boolean isStringType = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(objectTypeProperties.get(orderBy.get(i).getName()).first().getPropertyType()).isStringSubtype();		
					
					orders.add(isOrderAsc(orderBy.get(i)) ? cb.asc(cb.least(isStringType?propertyJoin.<String>get(CMISQueryFilter.VALUE):propertyJoin.<String>get(CMISQueryFilter.NUMERICVALUE)))
									                      : cb.desc(cb.least(isStringType?propertyJoin.<String>get(CMISQueryFilter.VALUE):propertyJoin.<String>get(CMISQueryFilter.NUMERICVALUE))));
				} else {
					//TODO: score column, order by, fulltext combination checks...
					orders.add(isOrderAsc(orderBy.get(i)) ? cb.asc(ast.getScore())
					                                      : cb.desc(ast.getScore()));
				}
			}
			
			c.orderBy(orders);
			
			return true;
		}
		return groupByObjectId;
	}

	private boolean joinScoreView(boolean count, CMISCommonTree ast, Repository rep, CriteriaBuilder cb, Root<CMISObject> queryRoot, boolean groupByObjectId) {
		
		if(ast.containsFulltext() && ast.isCalculateScore() && !count) {

			Set<String> tokens = ast.getFullTextTokens();
			
			List<Integer> wordIds = wordSelector.getWordIds(tokens, rep);
			
			Join<CMISObject, Score> scoreJoin = queryRoot.join("scores", JoinType.LEFT);
			scoreJoin.on(scoreJoin.<ScoreId>get("scoreId").<Integer>get("wordId").in(wordIds));
			
			ast.setScore(cb.sum(scoreJoin.<BigDecimal>get(CMIS_QUERY_SCORE)));
			
			return true;
		}
		
		return groupByObjectId;
	}

	private void setSemiJoinsRootIds(CMISCommonTree ast, CriteriaBuilder cb, Root<?>[] roots) {
		List<Subquery<Integer>> semiJoins = ast.getSemiJoins();
		
		for(Subquery<Integer> semiJoinSubquery : semiJoins) {
			Predicate whereCondition = semiJoinSubquery.getRestriction();
			semiJoinSubquery.where(cb.and(
					                     whereCondition,
					                     cb.equal(roots[0].<CMISObject>get(OBJECT).<Integer>get("id"),
					                    		 semiJoinSubquery.getRoots().iterator().next().<CMISObject>get(OBJECT).<Integer>get("id")
					                    		 )
					                   ));
		}
	}

	private void validateOrderBy(String scoreAlias, Table table, List<Order> orderBy, final Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties) {
		
		//validate order column(s) exist(s) and is/are orderable
		for(Order order : orderBy) {
			if(!order.getName().equalsIgnoreCase("SCORE()") && !order.getName().equalsIgnoreCase(scoreAlias)) {
				SortedSet<ObjectTypeProperty> orderOtps = objectTypeProperties.get(order.getName());
				if(orderOtps == null) {
					throw new CmisInvalidArgumentException("Column: " + order.getName() + " does not exist for ObjectType: " + table.getName() + "!");
				}

				if(!orderOtps.first().getOrderable()) {
					throw new CmisInvalidArgumentException("Column: " + order.getName() + " for ObjectType: " + table.getName() + " is not Orderable!");
				}
			}
		}
	}

	private String extractScoreAlias(Map queryParts) {
		
		String scoreAlias = null;
		
		List<Column> columns = (List<Column>) queryParts.get("columns");
		for(Column column : columns) {
			if(CMIS_QUERY_SCORE.equalsIgnoreCase(column.getFunction())) {
				scoreAlias = column.getAlias();
				if(scoreAlias == null || "".equals(scoreAlias.trim())) {
					scoreAlias = CMIS_SCORE_PROP;
				}
			}
		}
		return scoreAlias;
	}

	private void modifyAstIsLatestVersion(boolean searchAllVersions, CMISCommonTree ast, Table table, Repository repository) {

		if(!searchAllVersions || !repository.getAllVersionsSearchable()) {
			Boolean versionable = objTypeSelector.getObjectTypeByQueryName(repository.getCmisId(), table.getName(), false).isVersionable();
			if(versionable != null && versionable) {
				
				ComparisonFilter isLatestVersionFilter = new ComparisonFilter(CMISLexer.FILTER, null, "cmis:isLatestVersion", "=", "true");
				CommonTree filter = ast.getFilter();

				if(filter == null) {
					ast.addChild(isLatestVersionFilter);
					ast.setFilter(isLatestVersionFilter);
				} else {
					CMISCommonTree andNode = new CMISCommonTree(new CommonToken(CMISLexer.AND, "and"));

					filter.getParent().addChild(andNode);
					filter.getParent().deleteChild(filter.getChildIndex());

					andNode.addChild(isLatestVersionFilter);
					andNode.addChild(filter);
					ast.setFilter(andNode);
				}
			}
		}
		
	}

	private void addPropertyAclRestriction(String repositoryId, Root<?> root, Subquery<?> c, CriteriaBuilder cb, List<Predicate> criteria) {

		Predicate propertyAclRestriction = null;

		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(repSelector.getRepository(repositoryId).getAcl())) {

			Set<String> pricipalIds = security.getCallContextHolder().getPrincipalIds();

			Set<Integer> permissionIdsGetObjectProperties = security.getPermissionIds(repositoryId, Action.CAN_GET_PROPERTIES);

			Subquery<Integer> subquery = c.subquery(Integer.class);
			Root<Acl> aclQueryRoot = subquery.from(Acl.class);
			subquery.select(aclQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));

			subquery.where(cb.and(aclQueryRoot.<String>get("principalId").in(pricipalIds),
					aclQueryRoot.<Permission>get("permission").<Integer>get("id").in(permissionIdsGetObjectProperties)));

			if(root.getJavaType().isAssignableFrom(CMISObject.class)) {
				propertyAclRestriction = root.<Integer>get("id").in(subquery);
			} else {
				propertyAclRestriction = root.<CMISObject>get(OBJECT).<Integer>get("id").in(subquery);
			}

			criteria.add(propertyAclRestriction);
		}
	}

	private void validatePaging(int offset, int pageSize) {

		if(pageSize == 0 || offset == -1 || pageSize == -1 && offset != 0) {
			throw new CmisQueryException("Invalid paging parameters: PAGESIZE=" + pageSize + " OFFSET=" + offset);
		}
	}

	/**
	 * Parse query with ANTLR, return Abstract Syntax Tree (AST). The AST represents the structure of the parsed tree, where each
	 * significant token is represented by a subclass of (?)CommonTree
	 * 
	 * The following subclasses are currently available:
	 * 
	 * * 
	 * 
	 * @param cmisQuery
	 * @return
	 * @throws RecognitionException
	 */
	private CMISCommonTree parseQuery(String cmisQuery) throws RecognitionException {

		CMISLexer lex = new CMISLexer(new ANTLRStringStream(cmisQuery));
		CommonTokenStream tokens = new CommonTokenStream(lex);
		CMISParser parser = new CMISParser(tokens);
		parser.setTreeAdaptor(ADAPTOR);

		CMISParser.cmis_query_return r = parser.cmis_query();
		CMISCommonTree ast = r.getTree();
		ast.setFullTextAnalyzer(createFullTextAnalyzer());
		ast.setCMISObjectSelector(objSelector);
		ast.setEntityManager(getEntityManager());
		ast.setObjectTypePropertySelector(otpSelector);
		ast.setPropertySelector(propSelector);

		validateLexErrors(lex);
		validateParserErrors(parser);
		return ast;
	}


	/**
	 * 
	 * @param offset
	 * @param pageSize
	 * @param query {@link TypedQuery<Number>} The query that finds the object's ids.
	 * @return {@link List<Number>} List of non repeated ids.
	 */
	private List<Tuple> doQuery(int offset, int pageSize, TypedQuery<Tuple> query) {

		if(pageSize > 0) {
			query.setMaxResults(pageSize);
			if(offset > 0) {
				query.setFirstResult(offset);
			}
		}

		return query.getResultList();
	}

	private List<Predicate> buildQuery(Repository rep, ObjectType rootType, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, Subquery<Integer> c, CMISCommonTree ast, boolean isNormalizedQuery) {

		List<Predicate> queryPredicates = null;

		if (ast.getFilter() != null) {
			queryPredicates = buildFilterQuery(rootType, objectTypeProperties, cb, c, rep, ast, isNormalizedQuery);
		} else {
			queryPredicates = buildTableQuery(rootType, c, cb, ast);
		}

		return queryPredicates;
	}

	private List<Predicate> buildTableQuery(ObjectType rootType, Subquery<Integer> c, CriteriaBuilder cb, CMISCommonTree ast) {

		List<Predicate> predicates = new ArrayList<Predicate>();

		Root<?> queryRoot = c.from(CMISObject.class);
		Expression<Integer> objectId = queryRoot.get("id");
		c.select(objectId);			

		if(BaseTypeId.CMIS_SECONDARY.value().equals(rootType.getBase().getCmisId())) {
			Join<CMISObject, ObjectType> secondaryTypes = queryRoot.join("secondaryTypes");
			predicates.add(secondaryTypes.<Integer>get("id").in(ast.getSubtypeIdsToInclude()));
		} else {
			predicates.add(queryRoot.<ObjectType>get("objectType").<Integer>get("id").in(ast.getSubtypeIdsToInclude()));
		}
		
		return predicates;
	}

	private List<Predicate> buildFilterQuery(ObjectType rootType, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, Subquery<Integer> c, Repository rep, CMISCommonTree ast, boolean isNormalizedQuery) {

		List<Predicate> filterPredicates = new ArrayList<Predicate>();

		Root<?> queryRoot= c.from(Property.class);
		Expression<Integer> objectId = queryRoot.<CMISObject>get(OBJECT).get("id");

		List<Integer> subtypeIdsToExclude = getSubtypes(rootType, SUBTYPES.EXCLUDE_FROM_SUPERTYPE_QUERY);

		if(!subtypeIdsToExclude.isEmpty()) {
			filterPredicates.add(cb.not(queryRoot.<CMISObject>get(OBJECT).<ObjectType>get("objectType").<Integer>get("id").in(subtypeIdsToExclude)));
		}

		filterPredicates.add(buildTree(ast.getFilter(), ast, objectTypeProperties, cb, c, queryRoot, rep, isNormalizedQuery));

		//if(((CMISCommonTree)ast).containsNullFilter() || ((CMISCommonTree)ast).containsFulltext()) {			
			
			if(BaseTypeId.CMIS_SECONDARY.value().equals(rootType.getBase().getCmisId())) {
				Join<CMISObject, ObjectType> secondaryTypes = queryRoot.<Property, CMISObject>join(OBJECT).join("secondaryTypes");
				filterPredicates.add(secondaryTypes.<Integer>get("id").in(ast.getSubtypeIdsToInclude()));
			} else {
				filterPredicates.add(queryRoot.<CMISObject>get(OBJECT).<ObjectType>get("objectType").<Integer>get("id").in(ast.getSubtypeIdsToInclude()));
			}

			//filterPredicates.add(queryRoot.<CMISObject>get(OBJECT).<ObjectType>get("objectType").<Integer>get("id").in(subtypeIdsToInclude));
		/*} else {
			filterPredicates.add(cb.equal(queryRoot.<CMISObject>get(OBJECT).<ObjectType>get("objectType").<Integer>get("id"), rootType.getId()));
		}*/
		
		c.select(objectId);
		
		return filterPredicates;
	}

	private List<javax.persistence.criteria.Order> orderBy(List<Order> orderBy, CMISCommonTree ast, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb) {

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();

		for(int i=0; i<orderBy.size(); i++) {

			Path<?> propertyJoin = null;

			if(!isOrderByScore(orderBy.get(i), ast)) {

				propertyJoin = ast.getPropertyJoinMap().get(objectTypeProperties.get(orderBy.get(i).getName()).first().getId());

				if(propertyJoin == null) {
					//JPA 2.0 limitation: no conditions on ON clause, so order_by not working correctly when ordering on column which
					//is not included in filter.
					throw NOT_YET;
				}

				boolean isStringType = ObjectTypeProperty.getPropertyTypeValueTypeMappings().get(objectTypeProperties.get(orderBy.get(i).getName()).first().getPropertyType()).isStringSubtype();		
				
				orders.add(isOrderAsc(orderBy.get(i)) ? cb.asc(cb.least(isStringType?propertyJoin.<String>get(CMISQueryFilter.VALUE):propertyJoin.<String>get(CMISQueryFilter.NUMERICVALUE)))
								                      : cb.desc(cb.least(isStringType?propertyJoin.<String>get(CMISQueryFilter.VALUE):propertyJoin.<String>get(CMISQueryFilter.NUMERICVALUE))));
			} else {
				//TODO: score column, order by, fulltext combination checks...
				orders.add(isOrderAsc(orderBy.get(i)) ? cb.asc(ast.getScore())
						                              : cb.desc(ast.getScore()));
			}
		}

		return orders;
	}

	private boolean isOrderAsc(Order order) {
		return order.getOrder() == null && ORDER_DIR.ASC.equals(DEFAULT_ORDER_DIR) || "asc".equals(order.getOrder());
	}
	
	private boolean isOrderByScore(Order order, CMISCommonTree ast) {
		return order.getName().equalsIgnoreCase("SCORE()") || order.getName().equalsIgnoreCase(ast.getScoreAlias());
	}

	@Override
	public int queryCount(String statement, String repositoryId, boolean searchAllVersions, boolean isNormalizedQuery) throws RecognitionException {
		Repository repository = repSelector.getRepository(repositoryId);
		Object count = executeQuery(statement, repository, searchAllVersions, true, 0, -1, isNormalizedQuery).get(COUNT);
		return count instanceof Long ? ((Long) count).intValue(): (Integer) count;
	}

	protected abstract Analyzer createPropertiesAnalyzer();
	protected abstract Analyzer createFullTextAnalyzer();

	private List<Integer> getSubtypes(ObjectType rootType, SUBTYPES subtypes) {

		List<Integer> subTypes = new ArrayList<Integer>();

		List<ObjectType> children = rootType.getChildren();

		for(ObjectType child : children) {

			boolean add = false;

			switch(subtypes) {
				case EXCLUDE_FROM_SUPERTYPE_QUERY:
					if(!child.isIncludedInSupertypeQuery()) {
						add = true;
					}
					break;
				case INCLUDE_IN_SUPERTYPE_QUERY:
					if(child.isIncludedInSupertypeQuery()) {
						add = true;
					}
					break;
				case ALL:
					add = true;
			}

			if(add) {
				subTypes.add(child.getId());
			}

			subTypes.addAll(getSubtypes(child, subtypes));
		}

		return subTypes;
	}

	private ObjectType getObjectType(Repository repository, Table table) {

		ObjectType rootType = null;

		try {
			rootType = objTypeSelector.getObjectTypeByQueryName(repository.getCmisId(), table.getName(), true);
			// If table <Object-Type> undefined, JPA throws NoResultException ->
			// invalid query...
		} catch (NoResultException e) {
			// TODO: i18n strings...
			throw new CmisInvalidArgumentException("Object Type: " + table.getName() + " undefined!", e);
		}
		
		return rootType;
		
	}
	
	private void validateObjectTypeQueryable(ObjectType objectType) {

		// VALIDATION: verify root ObjectType is queryable
		if (!objectType.isQueryable().booleanValue()) {
			throw new CmisRuntimeException("Type: " + objectType.getQueryName() + " is not queryable!");
		}
		
	}

	/**
	 * 
	 * @param commonTree
	 * @param objectTypeProperties
	 * @param cb
	 * @param cmisObjectRoot
	 * @param columns
	 * @param table
	 * @return Predicate [AND|OR]
	 */
	private Predicate buildTree(CMISCommonTree filter, CMISCommonTree ast, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, Subquery<?> c, Root<?> queryRoot, Repository rep, boolean isNormalizedQuery) {

		Predicate predicate = null;

		String text = filter.getText();
		boolean negateExpression = filter.negateExpression();

		if(isAndNodeEquivalent(text, negateExpression)) {

			predicate = buildAndNode(filter, ast, objectTypeProperties, cb, c, queryRoot, rep, isNormalizedQuery);

		} else if(isOrNodeEquivalent(text, negateExpression)) {

			List<Predicate> childPredicates = new ArrayList<Predicate>();

			for(int i=0; i<filter.getChildren().size(); i++) {
				childPredicates.add(buildTree((CMISCommonTree)filter.getChildren().get(i), ast, objectTypeProperties, cb, c, queryRoot, rep, isNormalizedQuery));
			}

			predicate = cb.or(childPredicates.toArray(new Predicate[childPredicates.size()]));

		} else if(NOT.equalsIgnoreCase(text)) {

			filter.setNegateExpression(!negateExpression);

			predicate = buildTree((CMISCommonTree) filter.getChildren().get(0), ast, objectTypeProperties, cb, c, queryRoot, rep, isNormalizedQuery);

		} else if(filter instanceof CMISQueryFilter) {

			filter.setNormalizeQuery(isNormalizedQuery);
			predicate = buildFilterCriteria((CMISQueryFilter) filter, objectTypeProperties, cb, c, queryRoot, ast.getTable(), rep);
		}

		filter.setNegateExpression(negateExpression);

		return predicate;
	}

	private boolean isOrNodeEquivalent(String text, boolean negateExpression) {
		return OR.equalsIgnoreCase(text) && !negateExpression || AND.equalsIgnoreCase(text) && negateExpression;
	}

	private boolean isAndNodeEquivalent(String text, boolean negateExpression) {
		return AND.equalsIgnoreCase(text) && !negateExpression || OR.equalsIgnoreCase(text) && negateExpression;
	}

	private Predicate buildAndNode(CMISCommonTree filter, CMISCommonTree ast, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, Subquery<?> c, Root<?> queryRoot, Repository rep, boolean isNormalizedQuery) {
				
		List<Predicate> childPredicates = new ArrayList<Predicate>();
			
		for(int i=0; i<filter.getChildren().size(); i++) {

			if(i>0) {

				Subquery<Integer> subquery = c.subquery(Integer.class);
				Root<Property> propertyRoot = subquery.from(Property.class);
				subquery.select(cb.literal(1));
				subquery.where(buildTree((CMISCommonTree)filter.getChildren().get(i), ast, objectTypeProperties, cb, c, propertyRoot, rep, isNormalizedQuery));

				filter.addSemiJoin(subquery);

				childPredicates.add(cb.exists(subquery));

			} else {
				childPredicates.add(buildTree((CMISCommonTree) filter.getChildren().get(i), ast, objectTypeProperties, cb, c, queryRoot, rep, isNormalizedQuery));
			}
		}
		
		return cb.and(childPredicates.toArray(new Predicate[childPredicates.size()]));
	}

	/**
	 * 
	 * @param filter
	 * @param objectTypeProperties
	 * @param cb
	 * @param cmisObjectRoot
	 * @param columns
	 * @param table
	 * @return
	 */
	private Predicate buildFilterCriteria(CMISQueryFilter filter, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, CriteriaBuilder cb, Subquery<?> c, Path<?> queryRoot, Table table, Repository rep) {

		validateQueryProperty(filter, objectTypeProperties, table);

		Predicate propertyFilter = filter.toPredicate(cb, c, queryRoot, objectTypeProperties, security, rep);

		if(!"fulltext".equals(filter.getName()) && !"in_folder".equals(filter.getName()) && !"in_tree".equals(filter.getName()) && !"ISNULL".equals(filter.getOp())) {
			propertyFilter = cb.and(cb.equal(queryRoot.get("objectTypeProperty").get("id"), objectTypeProperties.get(filter.getName()).first().getId()), propertyFilter);
		}

		return propertyFilter;
	}

	protected EntityManager getEntityManager() {
		return entityManagerProxyBuilder.getInstance(entityManager, /*no cache*/false);
	}

	/**
	 * 
	 * @param filter
	 * @param objectTypeProperties
	 * @param columns
	 * @param table
	 */
	private void validateQueryProperty(CMISQueryFilter filter, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Table table) {

		String queryProperty = filter.getName();

		if("fulltext".equals(queryProperty) || "in_folder".equals(queryProperty) || "in_tree".equals(queryProperty)) {
			return;
		}

		// verify cmisObjectRoot properties exist and are queryable
		SortedSet<ObjectTypeProperty> properties = objectTypeProperties.get(queryProperty);

		if (properties == null || properties.isEmpty()) {
			throw new CmisInvalidArgumentException("Property " + queryProperty + " not found for Object_Type:" + table.getName());
		}

		if (!properties.first().getQueryable()) {
			throw new CmisInvalidArgumentException("Property: " + queryProperty + " is not queryable.");
		}
	}

	private void validateLexErrors(CMISLexer lex) {
		if (lex.getErrors() != null && !lex.getErrors().isEmpty()) {
			throw new CmisRuntimeException("Invalid query. " + lex.getErrors().get(0));
		}
	}

	private void validateParserErrors(CMISParser parser) {
		if (parser.getErrors() != null && !parser.getErrors().isEmpty()) {
			throw new CmisRuntimeException("Invalid query. " + parser.getErrors().get(0));
		}
	}

	private void checkQueryCapability(CapabilityQuery capabilityQuery) {
		if(capabilityQuery.equals(CapabilityQuery.NONE)){
			throw new CmisNotSupportedException(CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY);
		}
	}

	private void checkAllVersionSearchableCapability(boolean allVersionsSearchable, boolean searchAllVersions) {
		if(!allVersionsSearchable && searchAllVersions){
			throw new CmisNotSupportedException(CAPABILITY_NOT_SUPPORTED_BY_THE_REPOSITORY);
		}
	}

	private static final TreeAdaptor ADAPTOR = new CommonTreeAdaptor() {

		@Override
		public Object create(Token payload) {
			return new CMISCommonTree(payload);
		}

		@Override
		public Object dupNode(Object old) {
			return old == null ? null : ((CMISCommonTree) old).dupNode();
		}

		@Override
		public Object errorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
			return new CMISCommonErrorNode(input, start, stop, e);
		}
	};
}
