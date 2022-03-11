/**
 * 
 */
package eu.trade.repo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Repository's additional handler for security implementation (authentication or authorization). Only used when {@link SecurityType#MULTIPLE}.
 * <p>
 * This class refers to a security handler by its name, taken from the configuration. Be aware of the conflicts that may arise between the database and the configuration.
 * 
 * @see config/common/repo_securityConfig.xml
 * @see eu.trade.repo.security.AuthenticationHandler
 * @see eu.trade.repo.security.AuthorizationHandler
 * @author porrjai
 */
@Entity
@Table(name="security_handler")
public class SecurityHandler implements DBEntity {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="sq_security_handler")
	@SequenceGenerator(sequenceName= "sq_security_handler", name="sq_security_handler")
	private Integer id;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="repository_id", nullable=false)
	private Repository repository;

	@Enumerated(EnumType.STRING)
	@Column(name="handler_type", length=20, nullable=false)
	private HandlerType handlerType;

	@Column(name="handler_name", length=100, nullable=false)
	private String handlerName;

	/**
	 * @see eu.trade.repo.model.DBEntity#getId()
	 */
	@Override
	public Integer getId() {
		return id;
	}

	/**
	 * @see eu.trade.repo.model.DBEntity#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @return the handlerType
	 */
	public HandlerType getHandlerType() {
		return handlerType;
	}

	/**
	 * @param handlerType the handlerType to set
	 */
	public void setHandlerType(HandlerType handlerType) {
		this.handlerType = handlerType;
	}

	/**
	 * @return the handlerName
	 */
	public String getHandlerName() {
		return handlerName;
	}

	/**
	 * @param handlerName the handlerName to set
	 */
	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (handlerName == null ? 0 : handlerName.hashCode());
		result = prime * result + (handlerType == null ? 0 : handlerType.hashCode());
		result = prime * result + (repository == null ? 0 : repository.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ( !(obj instanceof SecurityHandler) )  {
			return false;
		}
		SecurityHandler other = (SecurityHandler) obj;
		if (handlerName == null) {
			return false;
		}
		if (!handlerName.equals(other.handlerName)) {
			return false;
		}
		if (handlerType != other.handlerType) {
			return false;
		}
		if (repository == null) {
			return false;
		}
		return repository.equals(other.repository);
	}


}
