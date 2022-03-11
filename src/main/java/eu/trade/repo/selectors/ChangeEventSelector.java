package eu.trade.repo.selectors;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

import eu.trade.repo.model.ChangeEvent;
/**
 * 
 * @author azaridi
 *
 */
public class ChangeEventSelector extends BaseSelector {

	/**
	 * Gets all changeEvents after the one identified by the supplied changeLogToken.
	 * @param changeLogToken All changeEvents after this token
	 * @param repositoryId The repository whose changeEvents to get
	 * @param maxItems The maximum changeEvent to get
	 * @return Every changeEvent after the one identified by the supplied changeLogToken. Will throw an exception if no ChangeEvent can be found for changeLogToken 
	 */
	public List<ChangeEvent> getChangeEvents(String changeLogToken, int repositoryId, BigInteger maxItems) {
		
		if(changeLogToken == null) {
			changeLogToken = getFirstChangeLogToken(repositoryId);
		}
		
		ChangeEvent ev = null;
		try {
			ev = getChangeLogEvent(changeLogToken);
		} catch (NoResultException x) {
			throw new CmisInvalidArgumentException("Unknown change Token: "+changeLogToken);
		}

		return getEntityManager().createNamedQuery("ChangeEvent.get_all", ChangeEvent.class)
				.setParameter("id", ev.getId())
				.setParameter("repositoryId", repositoryId)
				.setMaxResults(maxItems.intValue())
				.getResultList();
	}

	/**
	 * In case of an invalid repoId or no change token this method will return a fake token "&gt;noChangeLogToken&lt;".
	 * 
	 * @param repositoryId The repository whose latestChangeLog is requested
	 * @return the latestChangeToken of the supplied Repository
	 */
	public String getLatestChangeLogToken(int repositoryId) {
		return getChangeLogToken(repositoryId, false);
	}
	
	/**
	 * In case of an invalid repoId or no change token this method will return a fake token "&gt;noChangeLogToken&lt;".
	 * 
	 * @param repositoryId The repository whose first Change token is requested
	 * @return the first Change Token of the supplied Repository
	 */
	public String getFirstChangeLogToken(int repositoryId) {
		return getChangeLogToken(repositoryId, true);
	}
	


	/**
	 * Returns the change token 
	 * 
	 * @param repositoryId
	 * @param first true for get the first token, false to the latest one
	 * @return
	 */
	private String getChangeLogToken(int repositoryId, boolean first) {
		String queryName = first ? "ChangeEvent.get_firstChangeLogToken": "ChangeEvent.get_latestChangeLogToken";
		
		List<String> tokens = getEntityManager()
				.createNamedQuery(queryName, String.class)
				.setParameter("repositoryId", repositoryId)
				.getResultList();
		//TODO should never be the case .. should either fix the DB or dbunit scenario if this appears
		if (!tokens.isEmpty()) {
			return tokens.get(0);
		} else {
			return "<noChangeLogToken>";
		}
	}

	/**
	 * Get the ChangeEvent uniquely identified by the supplied changeLogToken
	 * @param changeLogToken The ChangeEvent identifier 
	 * @return The ChangeEvent identified by the supplied changeLogToken
	 */
	public ChangeEvent getChangeLogEvent(String changeLogToken) {
		return getEntityManager().createNamedQuery("ChangeEvent.get_event", ChangeEvent.class)
				.setParameter("changeLogToken", changeLogToken)
				.getSingleResult();
	}
}
