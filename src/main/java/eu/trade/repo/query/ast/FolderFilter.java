package eu.trade.repo.query.ast;

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

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Utilities;

public class FolderFilter extends CMISQueryFilter {
	
	private final String function;
	private final String qualifier;
	private final String value;
	
	public FolderFilter(int t, String function, String qualifier, String value) {
		super(new CommonToken(t));
		
		this.function = function;
		this.qualifier = qualifier;
		this.value = value != null ? Utilities.convertCMIStoSQLEscapes(Utilities.removePropertyValueSingleQuotes(value)) : null;
		
	}

	@Override
	public String getText() {
		return toString();
	}

	@Override
	public String toString() {
		return "[function=" + function + ", qualifier=" + qualifier + ", value=" + value + "]";
	}

	@Override
	public String getName() {
		return function.toLowerCase();
	}

	@Override
	public String getOp() {
		return function.toUpperCase();
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public Predicate toPredicate(CriteriaBuilder cb, Subquery<?> c, Path<?> property, Map<String, SortedSet<ObjectTypeProperty>> objectTypeProperties, Security security, Repository rep) {
		
		Set<String> principalIds = security.getCallContextHolder().getPrincipalIds();
		Set<Integer> permissionIdsGetContentStream = security.getPermissionIds(rep.getCmisId(), Action.CAN_GET_CHILDREN);
		
		Subquery<Integer> subquery2 = c.subquery(Integer.class);
		Root<CMISObject> childrenQueryRoot = subquery2.from(CMISObject.class);
		Join<CMISObject, CMISObject> children = childrenQueryRoot.join("children");
		subquery2.select(children.<Integer>get("id"));
		
		Predicate restrictParent = cb.equal(childrenQueryRoot.<String>get("cmisObjectId"), value);
		Predicate restrictChildType = children.<ObjectType>get("objectType").<Integer>get("id").in(getSubtypeIdsToInclude());
		
		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(rep.getAcl())) {
			
			Subquery<Integer> subquery3 = c.subquery(Integer.class);
			Root<Acl> aclQueryRoot = subquery3.from(Acl.class);
			subquery3.select(aclQueryRoot.<CMISObject>get("object").<Integer>get("id"));
			
			subquery3.where(cb.and(aclQueryRoot.<String>get("principalId").in(principalIds),
			                       aclQueryRoot.<Permission>get("permission").<Integer>get("id").in(permissionIdsGetContentStream)));
			
			//TODO:also for in_tree()
			Predicate restrictParentWithAcl = cb.and(restrictParent, childrenQueryRoot.<Integer>get("id").in(subquery3));
			subquery2.where(cb.and(restrictParentWithAcl, restrictChildType));
		} else {
			subquery2.where(cb.and(restrictParent, restrictChildType));
		}
		
		Path<Integer> objectId = property.<CMISObject>get(OBJECT).get("id");
		return !negateExpression()?objectId.in(subquery2):cb.not(objectId.in(subquery2));
	}
}
