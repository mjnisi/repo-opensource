package eu.trade.repo.selectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.Repository;

@Transactional
public class RepositoryTestSelector {

	@Autowired
	private RepositorySelector repositorySelector;

	public Repository loadRepositoryWithSecurityHandlers(String repositoryId) {
		Repository repository = repositorySelector.getRepository(repositoryId);
		repository.getSecurityHandlers().size();
		return repository;
	}

}
