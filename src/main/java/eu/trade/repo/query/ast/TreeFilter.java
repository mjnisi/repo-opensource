package eu.trade.repo.query.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.antlr.runtime.CommonToken;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;

import eu.trade.repo.model.Acl;
import eu.trade.repo.model.Ancestor;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.ObjectAncestorId;
import eu.trade.repo.model.Permission;
import eu.trade.repo.model.Property;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.util.Utilities;

/**
 * TODO: maybe extend FolderFilter
 * 
 * @author kardaal
 *
 */
public class TreeFilter extends CMISQueryFilter {

	private final String function;
	private final String qualifier;
	private final String value;
	
	public TreeFilter(int t, String function, String qualifier, String value) {
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
		
		String queryFolderPath = getPropertySelector().getObjectPropertyByQueryName(value, PropertyIds.PATH).getTypedValue();
		String queryFolderSubPaths = queryFolderPath.endsWith("/")?queryFolderPath+"%":queryFolderPath + "/%";
		
		CriteriaBuilder cb1 = getEntityManager().getCriteriaBuilder();
		
		Subquery<Integer> c2 = c.subquery(Integer.class);
		Root<Property> property2 =  c2.from(Property.class);
		c2.select(property2.<CMISObject>get(OBJECT).<Integer>get("id"));
		
		List<Predicate> criteria = new ArrayList<Predicate>();
		criteria.add(cb1.or(cb1.like(property2.<String>get(VALUE), queryFolderSubPaths), cb1.equal(property2.<String>get(VALUE), queryFolderPath)));
		
		Subquery<Integer> disallowedParentsAndDescendantsSubQuery = null;
		
		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(rep.getAcl())) {
			
			//(1) get forbidden paths
			//select property.value where property.otpid = <path_id> and objectId not in (select objects where access_descendants is allowed)
			
			Set<Integer> permissionIdsGetDescendants = security.getPermissionIds(rep.getCmisId(), Action.CAN_GET_DESCENDANTS);
			
			Subquery<Integer> subquery3 = c.subquery(Integer.class);
			Root<Acl> aclQueryRoot = subquery3.from(Acl.class);
			subquery3.select(aclQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));
			
			subquery3.where(cb.and(aclQueryRoot.<String>get("principalId").in(principalIds),
			                       aclQueryRoot.<Permission>get("permission").<Integer>get("id").in(permissionIdsGetDescendants)));
			
			Subquery<Integer> disallowedParentsSubQuery = c.subquery(Integer.class);
			Root<Property> disallowedParentsSubQueryRoot = disallowedParentsSubQuery.from(Property.class);
			
			disallowedParentsSubQuery.select(disallowedParentsSubQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id"));
			
			disallowedParentsSubQuery.where(cb.and(cb.and(cb.equal(disallowedParentsSubQueryRoot.<ObjectTypeProperty>get("objectTypeProperty").<Integer>get("id"),
	                                                               getObjectTypePropertySelector().getFolderProperty(PropertyIds.PATH, rep.getCmisId()).getId()),
	                                                      cb.not(disallowedParentsSubQueryRoot.<CMISObject>get(OBJECT).<Integer>get("id").in(subquery3))),
	                                               cb.or(cb.like(disallowedParentsSubQueryRoot.<String>get(VALUE), queryFolderSubPaths), cb.equal(disallowedParentsSubQueryRoot.<String>get(VALUE), queryFolderPath))));
			
			disallowedParentsAndDescendantsSubQuery = c.subquery(Integer.class);
			Root<CMISObject> disallowedParentsAndDescendantsSubQueryRoot = disallowedParentsAndDescendantsSubQuery.from(CMISObject.class);
			disallowedParentsAndDescendantsSubQuery.select(disallowedParentsAndDescendantsSubQueryRoot.<Integer>get("id"));
			
			disallowedParentsAndDescendantsSubQuery.where(cb.or(disallowedParentsAndDescendantsSubQueryRoot.<CMISObject, Ancestor>join("ancestors").<ObjectAncestorId>get("objectAncestorId").<CMISObject>get("ancestor").<Integer>get("id").in(disallowedParentsSubQuery),
					                                            disallowedParentsAndDescendantsSubQueryRoot.<Integer>get("id").in(disallowedParentsSubQuery)));
			
			/*CriteriaQuery<Integer> c1 = cb1.createQuery(Integer.class);
			Root<CMISObject> c1Root = c1.from(CMISObject.class);
			c1.select(c1Root.<Integer>get("id")).distinct(true);
			
			c1.where(cb.or(c1Root.<CMISObject, Parent>join("parentsToRoot").<ParentId>get("objectParentId").<CMISObject>get("parent").<Integer>get("id").in(disallowedParentsSubQuery),
					       c1Root.<Integer>get("id").in(disallowedParentsSubQuery)));

			TypedQuery<Integer> query = getEntityManager().createQuery(c1);
			List<Integer> res = query.getResultList();
			System.out.println(res);*/

			
		}
		
		c2.where(cb1.and(criteria.toArray(new Predicate[criteria.size()])));
		
		//(3) select appropriate children from non-forbidden folders
		Subquery<Integer> c3 = c.subquery(Integer.class);
		Root<CMISObject> childrenQueryRoot = c3.from(CMISObject.class);
		Join<CMISObject, CMISObject> children = childrenQueryRoot.join("children");
		c3.select(children.<Integer>get("id"));
		
		Predicate restrictParent = childrenQueryRoot.<Integer>get("id").in(c2);
		Predicate restrictChildType = children.<ObjectType>get("objectType").<Integer>get("id").in(getSubtypeIdsToInclude());
		
		if(!security.getCallContextHolder().isAdmin() && !CapabilityAcl.NONE.equals(rep.getAcl())) {
			
			Set<Integer> permissionIdsGetProperties = security.getPermissionIds(rep.getCmisId(), Action.CAN_GET_PROPERTIES);
			
			Subquery<Integer> subquery4 = c.subquery(Integer.class);
			Root<Acl> aclQueryRoot1 = subquery4.from(Acl.class);
			subquery4.select(aclQueryRoot1.<CMISObject>get(OBJECT).<Integer>get("id"));
			
			subquery4.where(cb.and(aclQueryRoot1.<String>get("principalId").in(principalIds),
			                       aclQueryRoot1.<Permission>get("permission").<Integer>get("id").in(permissionIdsGetProperties)));
			
			Predicate restrictChildTypeWithAcl = cb.and(restrictChildType, children.<Integer>get("id").in(subquery4));
			
			c3.where(cb.and(cb.and(restrictParent, restrictChildTypeWithAcl),
					        cb.not(childrenQueryRoot.<Integer>get("id").in(disallowedParentsAndDescendantsSubQuery))));
		} else {
			c3.where(cb.and(restrictParent, restrictChildType));
		}
		
		Path<Integer> objectId = property.<CMISObject>get(OBJECT).get("id");
		return !negateExpression()?objectId.in(c3):cb.not(objectId.in(c3));
	}
	
}


















