package eu.trade.repo.changelog;

public interface ChangeLog {
	void delete(String repositoryId, String objectId) ;
	void create(String repositoryId, String objectId) ;
	void update(String repositoryId, String objectId) ;
	void security(String repositoryId, String objectId);
}
