package eu.trade.repo.selectors;

import static eu.trade.repo.util.Constants.*;

import java.util.Arrays;

import eu.trade.repo.model.Acl;

/**
 * Selector for {@link Acl} obejcts
 * 
 * @author porrjai
 */
public class AclSelector extends BaseSelector {

	/**
	 * Returns true when there is non default principal ids for the repository.
	 * 
	 * @param repositoryId {@link String} The repository cmis id.
	 * @return boolean true when there is non default principal ids for the repository.
	 */
	public boolean isThereNonDefaultAcl(String repositoryId) {
		long count = getEntityManager()
				.createNamedQuery("acl.countExcludingCmis", Number.class)
				.setParameter("repositoryId", repositoryId)
				.setParameter("cmisDefaults", Arrays.asList(PRINCIPAL_ID_ANONYMOUS, PRINCIPAL_ID_ANYONE))
				.getSingleResult().longValue();
		return count > 0;
	}
}
