package eu.trade.repo.selectors;

import java.util.List;

import eu.trade.repo.model.Repository;

public class RepositorySelector extends BaseSelector {

	public List<Repository> getAllRepositories() {
		return getEntityManager()
				.createNamedQuery("Repository.all", Repository.class)
				.getResultList();
	}

	public Repository getRepository(String repositoryId) {
		return getRepository(repositoryId, "Repository.repositoryById");
	}

	public Repository getRepositoryWithSecurityHandlers(String repositoryId) {
		Repository repository = getRepository(repositoryId);
		repository.getSecurityHandlers().size();
		return repository;
	}

	private Repository getRepository(String repositoryId, String namedQuery) {
		return getEntityManager()
				.createNamedQuery(namedQuery, Repository.class)
				.setParameter("repositoryId", repositoryId)
				.getSingleResult();
	}
	
	public List<String> getDisaggregatedVersionSeries(String repositoryId) {
		List<String>  results = getEntityManager().createNamedQuery("Repository.disaggregatedVersionSeries", String.class)
			.setParameter("repositoryId", repositoryId).getResultList();
		return results;
	}

}
