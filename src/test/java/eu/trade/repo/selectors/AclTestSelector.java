package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.Acl;

public class AclTestSelector extends BaseSelector {
	public List<Acl> getAcls() {
		return getEntityManager()
				.createNamedQuery("acl.test.all", Acl.class)
				.getResultList();
	}

	public List<Acl> getAclsByPID(String principalId) {
		return getEntityManager()
				.createNamedQuery("acl.test.by_pid", Acl.class)
				.setParameter("principalId", principalId)
				.getResultList();
	}

}
