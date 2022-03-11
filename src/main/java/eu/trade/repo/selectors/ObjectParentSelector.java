package eu.trade.repo.selectors;

import static eu.trade.repo.util.Constants.*;

import java.math.BigInteger;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.TypedQuery;

import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.model.view.VersionType;

/**
 * Selector to retrieve objects base in its actual path (not only for folders).
 * 
 * @author porrjai
 */
public class ObjectParentSelector extends BaseSelector {

	private static final String BASE_FOLDER_QUERY =	"ObjectParent.getFolderByPath";
	private static final String BASE_OBJECT_QUERY =	"ObjectParent.getByPath";
	private static final String AND_DEPTH ="AndDepth";
	private static final String AND_ACL = "AndAcl";
	private static final String AND_VERSION = "AndVersion";
	private static final String FETCH_PARENTS_CHILDREN = "AndParentsChildren";

	private static final String BASIC_PATH = "/%";

	/**
	 * Return the set of {@link ObjectParent} that match with the criteria (without considering security restrictions).
	 * 
	 * @param repositoryId {@link String} The repository's cmis id.
	 * @param onlyFolders boolean return only folder type objects.
	 * @param basePath {@link String} The base path for all the objects (folders or not).
	 * @param depth {@link BigInteger} If null, the no depth.
	 * @return {@link Set<ObjectParent>} the set of {@link ObjectParent} that match with the criteria.
	 */
	public Set<ObjectParent> getObjectParents(String repositoryId, boolean onlyFolders, String basePath, BigInteger depth, boolean loadParentsChildren) {
		return getObjectParents(repositoryId, basePath, onlyFolders, depth, null, null, null, loadParentsChildren);
	}

	/**
	 * Return the set of {@link ObjectParent} that match with the criteria.
	 * 
	 * @param repositoryId {@link String} The repository's cmis id.
	 * @param basePath {@link String} The base path for all the objects (folders or not).
	 * @param onlyFolders boolean return only folder type objects.
	 * @param depth {@link BigInteger} If null, the no depth.
	 * @param principalIds {@link Set<String>} Current user's set of principal ids. If null, then no security restrictions will be applied.
	 * @param permissionIds {@link Set<String>} The set of the permissions ids that satisfies certain concrete action.
	 * @param versionTypes TODO
	 * @return {@link Set<ObjectParent>} the set of {@link ObjectParent} that match with the criteria
	 */
	public Set<ObjectParent> getObjectParents(String repositoryId, String basePath, boolean onlyFolders, BigInteger depth, Set<String> principalIds, Set<Integer> permissionIds, Set<VersionType> versionTypes, boolean loadParentsChildren) {
		TypedQuery<ObjectParent> query = getObjectParentQuery(onlyFolders, depth, principalIds, versionTypes, loadParentsChildren);
		String pathSlash;
		if (basePath.equals(CMIS_PATH_SEP)) {
			pathSlash = BASIC_PATH;
		}
		else {
			pathSlash = basePath + BASIC_PATH;
		}
		query = query.setParameter("repositoryId", repositoryId).setParameter("basePath", basePath).setParameter("pathSlash", pathSlash);
		query = addDepth(query, depth, onlyFolders);
		query = addAcl(query, principalIds, permissionIds);
		query = addVersionTypes(query, versionTypes, onlyFolders);
		return new LinkedHashSet<>(query.getResultList());
	}

	private TypedQuery<ObjectParent> getObjectParentQuery(boolean onlyFolders, BigInteger depth, Set<String> principalIds, Set<VersionType> versionTypes, boolean loadParentsChildren) {
		String queryString;
		if (onlyFolders) {
			queryString = BASE_FOLDER_QUERY;
		}
		else {
			queryString = BASE_OBJECT_QUERY;
		}
		if (isDepth(depth)) {
			queryString += AND_DEPTH;
		}
		if (principalIds != null) {
			queryString += AND_ACL;
		}
		if (!onlyFolders && versionTypes != null) {
			queryString += AND_VERSION;
		}
		if (loadParentsChildren) {
			queryString += FETCH_PARENTS_CHILDREN;
		}
		return getEntityManager().createNamedQuery(queryString, ObjectParent.class);
	}

	/**
	 * This method adds the depthElem to the query (ObjectParent.getByPathAndDepth or ObjectParent.getByPathAndDepthAndAcl, {@link eu.trade.repo.model.CMISObject}).
	 * <p>
	 * The criteria for the depth uses the slash count in a negative way, i.e. excluding those elements with more depth than the one requested.
	 * <p>
	 * Fore example, the resulting criteria for a base path "/example" and a depth of 3 would be: {@code "and opw.object_path not like '/example/%/%/%/%'"}. With this criteria all the elements with a depth 4 or more would be excluded.
	 * 
	 * @param query
	 * @param depth
	 * @param onlyFolders If false, looking for objects, the depthElem would have one level less.
	 * @return
	 */
	private TypedQuery<ObjectParent> addDepth(TypedQuery<ObjectParent> query, BigInteger depth, boolean onlyFolders) {
		if (isDepth(depth)) {
			int intDepth = depth.intValue();
			if (!onlyFolders) {
				intDepth--;
			}
			StringBuilder depthElem = new StringBuilder();
			for (int i = 0; i < intDepth; i++) {
				depthElem.append(BASIC_PATH);
			}
			return query.setParameter("depthElem", depthElem.toString());
		}
		return query;
	}

	private TypedQuery<ObjectParent> addAcl(TypedQuery<ObjectParent> query, Set<String> principalIds, Set<Integer> permissionIds) {
		if (principalIds != null) {
			return query.setParameter("principalIds", principalIds).setParameter("permissionIds", permissionIds);
		}
		return query;
	}

	private TypedQuery<ObjectParent> addVersionTypes(TypedQuery<ObjectParent> query, Set<VersionType> versionTypes, boolean onlyFolders) {
		if (!onlyFolders && versionTypes != null) {
			return query.setParameter("versionTypes", versionTypes);
		}
		return query;
	}

	private boolean isDepth(BigInteger depth) {
		return depth != null && depth.compareTo(BigInteger.ZERO) > 0;
	}
}
