package eu.trade.repo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.chemistry.opencmis.commons.enums.ChangeType;

@Entity
@Table(name="change_event")
@NamedQueries({
	@NamedQuery(name="ChangeEvent.get_firstChangeLogToken", query = "select ce.changeLogToken from ChangeEvent ce where ce.id = (select min(id) from ChangeEvent where repository.id = :repositoryId)"),
	@NamedQuery(name="ChangeEvent.get_latestChangeLogToken", query = "select ce.changeLogToken from ChangeEvent ce where ce.id = (select max(id) from ChangeEvent where repository.id = :repositoryId)"),
	@NamedQuery(name="ChangeEvent.get_event", query = "select ce from ChangeEvent ce where ce.changeLogToken = :changeLogToken"),
	@NamedQuery(name="ChangeEvent.get_all", query = "select ce from ChangeEvent ce where ce.id > :id and ce.repository.id= :repositoryId order by ce.id asc")
})
/**
 * 
 * @author azaridi
 *
 */
public class ChangeEvent implements DBEntity {

	private static final long serialVersionUID = -6039917701974943935L;

	//MODEL
	@Id
	@GeneratedValue(generator="sq_change_event")
	@SequenceGenerator(sequenceName= "sq_change_event", name="sq_change_event")
	private Integer id;

	@ManyToOne
	@JoinColumn(name="repository_id")
	private Repository repository;

	@Column(name="cmis_object_id", length=100)
	private String cmisIbjectId;
	
	@Column(name="change_type", length=100)
	private String changeType;

	@Column(name="change_log_token", length=100)
	private String changeLogToken;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="change_time")
	private Date changeTime;
	
	@Column(length=100)
	private String username;
	
	//GETTERS/SETTERS
	public Integer 		getId() 		{	return id;	}
	public String 		getObjectId() 	{	return cmisIbjectId;	}
	public Repository 	getRepository()	{	return repository; }
	public ChangeType 	getChangeType() {	return ChangeType.fromValue(changeType);	}
	public Date getChangeTime() 		{ 	return changeTime; }
	public String getChangeLogToken() 	{	return changeLogToken;	}
	public String getUsername() 		{	return username;	}

	public void setUsername(String username) 			{	this.username = username;	}
	public void setId(Integer id) 						{	this.id = id;	}
	public void setObjectId(String objectId) 			{ this.cmisIbjectId = objectId; }
	public void setRepository(Repository repository)	{	this.repository = repository;	}
	public void setChangeLogToken(String changeLogToken) {	this.changeLogToken = changeLogToken;	}
	public void setChangeTime(Date changeTime) 			{ this.changeTime = changeTime;	}
	public void setChangeType(ChangeType type) 			{ changeType = type.value(); }
	
}