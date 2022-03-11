package eu.trade.repo.query.ast;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.model.Word;
import eu.trade.repo.model.WordObject;
import eu.trade.repo.model.WordPosition;
import eu.trade.repo.model.WordPositionId;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.Utilities;

public class TextSearchFilter extends CMISQueryFilter {
	
	private static final String WORD = "word";
	
	
	private final String qualifier;
	private final String value;
	
	public TextSearchFilter(int t, String qualifier, String value) {
		super(new CommonToken(t));
		
		this.qualifier = qualifier;
		this.value = value != null ? Utilities.removePropertyValueSingleQuotes(value) : null;
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[qualifier=" + qualifier + ", value=" + value + "]";
	}

	@Override
	public String getName() {
		return "fulltext";
	}

	@Override
	public String getOp() {
		return "CONTAINS";
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {
		
		setContainsFulltext(true);
		List<String> phraseWords = new ArrayList<String>();
		
		try {
			TokenStream stream = getFullTextAnalyzer().tokenStream("content", new StringReader(value));
			stream.reset();
			
			String token = null;
			while(stream.incrementToken()) {
				token = stream.getAttribute(CharTermAttribute.class).toString();
				phraseWords.add(token);
			}
		} catch (IOException e) {
			//LOG THIS
			throw new CmisRuntimeException("Error occurred while reading tokens in contains(...) for input: " + value, e);
		}
		
		if(phraseWords.isEmpty()) {
			throw new CmisInvalidArgumentException("CONTAINS: text was empty or discarded (max length: 50 chars)");
		}
		
		Subquery<Integer> subquery = c.subquery(Integer.class);
		Root<WordObject> containsQueryRoot = subquery.from(WordObject.class);
		subquery.select(containsQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));
		Join<WordObject, Word> joinOnDictionary = containsQueryRoot.join(WORD);
		
		List<Predicate> criteria = new ArrayList<Predicate>();
		criteria.add(cb.equal(joinOnDictionary.<String>get(WORD), phraseWords.get(0)));
		addFullTextToken(phraseWords.get(0));
		
		for(int i=1; i<phraseWords.size(); i++) {
			
			Subquery<Integer> subquery2 = c.subquery(Integer.class);
			Root<WordObject> positionQueryRoot = subquery2.from(WordObject.class);
			Join<WordObject, WordPosition> positionJoin = positionQueryRoot.join("positions");
			subquery2.select(cb.sum(positionJoin.<WordPositionId>get("id").<Integer>get("position"), -i));
			subquery2.where(cb.and(cb.equal(positionQueryRoot.<WordObject, CMISObject>join(OBJECT).<Integer>get("id"), containsQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id")),
					cb.equal(positionQueryRoot.<WordObject, Word>join(WORD).<String>get(WORD), phraseWords.get(i))));
			
			criteria.add(containsQueryRoot.<WordObject, WordPosition>join("positions").<WordPositionId>get("id").<Integer>get("position").in(subquery2));
			addFullTextToken(phraseWords.get(i));
		}
		
		//TODO: investigate whether this can be conditional depending on position in tree
		criteria.add(cb.equal(joinOnDictionary.<Repository>get("repository").<Integer>get("id"), rep.getId()));
		
		subquery.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
		
		return applyFulltextAcls(subquery, containsQueryRoot, property, security, rep, c, cb);
	}
	
	protected Predicate applyFulltextAcls(Subquery<Integer> subquery, Path<?> subqueryRoot, Path<?> property, Security security, Repository rep, Subquery<?> c, CriteriaBuilder cb) {
		
		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(rep.getAcl())) {
			
			Set<String> pricipalIds = security.getCallContextHolder().getPrincipalIds();
			
			Set<Integer> permissionIdsGetContentStream = security.getPermissionIds(rep.getCmisId(), Action.CAN_GET_CONTENT_STREAM);
			
			Subquery<Integer> subquery2 = c.subquery(Integer.class);
			Root<Acl> aclQueryRoot = subquery2.from(Acl.class);
			subquery2.select(aclQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));
			
			subquery2.where(cb.and(aclQueryRoot.<String>get("principalId").in(pricipalIds),
			                       aclQueryRoot.<Permission>get("permission").<Integer>get("id").in(permissionIdsGetContentStream)));
			
			subquery.where(cb.and(subquery.getRestriction(), subqueryRoot.<CMISObject>get(OBJECT).<Integer>get("id").in(subquery2)));
		}
		
		Path<Integer> objectId = property.<CMISObject>get(OBJECT).get("id");
		return !negateExpression()?objectId.in(subquery):cb.not(objectId.in(subquery));
	}
}
