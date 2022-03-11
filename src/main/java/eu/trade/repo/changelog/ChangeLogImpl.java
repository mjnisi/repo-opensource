package eu.trade.repo.changelog;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.id.IDGenerator;
import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.model.Repository;
import eu.trade.repo.security.Security;
import eu.trade.repo.selectors.RepositorySelector;

/**
 * 
 * @author azaridi
 *
 */

public class ChangeLogImpl implements ChangeLog {

	@Autowired
	private IDGenerator generator;

	@Autowired
	private RepositorySelector repoSelector;

	@Autowired
	private Security security;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public void delete(String repositoryId, String objectId) {
		logChangeEvent(repositoryId, objectId, ChangeType.DELETED);
	}

	@Override
	public void create(String repositoryId, String objectId) {
		logChangeEvent(repositoryId, objectId, ChangeType.CREATED);
	}

	@Override
	public void update(String repositoryId, String objectId) {
		logChangeEvent(repositoryId, objectId, ChangeType.UPDATED);
	}

	@Override
	public void security(String repositoryId, String objectId) {
		logChangeEvent(repositoryId, objectId, ChangeType.SECURITY);
	}

	private void logChangeEvent(String repositoryId, String objectId, ChangeType type) {
		Repository repo = repoSelector.getRepository(repositoryId);
		if (repo.getChanges() == CapabilityChanges.NONE) {
			return; 
		}

		ChangeEvent ev = new ChangeEvent();
		ev.setChangeTime(new Date());
		ev.setChangeType(type);
		ev.setObjectId(objectId);
		ev.setUsername(security.getCallContextHolder().getUsername());
		ev.setChangeLogToken(generator.next());
		ev.setRepository(repo);

		entityManager.persist(ev);
	}
}
