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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="object_type_relationship")
@NamedQueries({
	@NamedQuery(name="ObjectTypeRelationship.by_cmisid",query = "select otr from ObjectTypeRelationship otr where otr.objectType.cmisId = :cmisId"),
	@NamedQuery(name="ObjectTypeRelationship.all", 	query = "select otr from ObjectTypeRelationship otr")
})

public class ObjectTypeRelationship implements DBEntity {
	private static final long serialVersionUID = 1L;
	//MODEL

	@Id
	@GeneratedValue(generator="sq_object_type_relationship")
	@SequenceGenerator(sequenceName= "sq_object_type_relationship", name="sq_object_type_relationship")
	private Integer id;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="OBJECT_TYPE_ID")
	private ObjectType objectType;

	@Enumerated(EnumType.STRING)
	@Column (length=100, nullable = false)
	private RelationshipType type;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="referenced_object_type_id")
	private ObjectType referencedObjectType;


	//GETTERS/SETTERS
	@Override
	public Integer getId() 	{	return this.id;	}
	public RelationshipType  getType() {	return this.type;	}
	public ObjectType  getReferencedObjectType(){	return this.referencedObjectType;	}
	public ObjectType getObjectType() {	return this.objectType;	}

	@Override
	public void setId(Integer id) 		{	this.id = id;	}
	public void setReferencedObjectType(ObjectType value) 	{	this.referencedObjectType = value;	}
	public void setType(RelationshipType type) 	{	this.type = type;	}
	public void setObjectType(ObjectType objectType) {		this.objectType = objectType;	}

	public enum RelationshipType {
		SOURCE,
		TARGET
    }
}