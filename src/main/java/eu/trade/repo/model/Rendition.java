package eu.trade.repo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;


@Entity
@NamedQueries({
	@NamedQuery(name="Rendition.all", 		query = "select rnd from Rendition rnd"),
	@NamedQuery(name="Rendition.by_cmisid", query = "select rnd from Rendition rnd where rnd.object.id = :cmisObjId"),
	@NamedQuery(name="Rendition.by_stream",	query = "select rnd from Rendition rnd where rnd.streamId = :streamId"),
})

public class Rendition implements DBEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator="sq_rendition")
	@SequenceGenerator(sequenceName= "sq_rendition", name="sq_rendition")
	private Integer id;

	@Column(name="mime_type", nullable=false, length=100)
	private String mimeType;

	@Column(name="rendition_document_id", length=100)
	private String renditionDocumentId;

	@Column(name="stream_id", nullable=false, length=100)
	private String streamId;

	@Column(nullable=false, length=100)
	private String kind;

	@ManyToOne (fetch=FetchType.LAZY)
	@JoinColumn(name="object_id")
	private CMISObject object;

	//optional columns
	@Column(nullable=true)
	private Integer length;

	@Column(nullable=true, length=100)
	private String title;

	@Column(nullable=true)
	private Integer width;

	@Column(nullable=true)
	private Integer height;

	@Override
	public Integer getId() 		{	return this.id;	}
	public Integer getHeight() 	{	return this.height;	}
	public String  getKind() 	{	return this.kind;	}
	public Integer getLength() 	{	return this.length;	}
	public String  getMimeType(){	return this.mimeType;	}
	public String  getStreamId(){	return this.streamId;	}
	public String  getTitle() 	{	return this.title;	}
	public Integer getWidth() 	{	return this.width;	}
	public CMISObject  getObject() 	{	return this.object;	}
	public String  getRenditionDocumentId() {	return this.renditionDocumentId;	}

	public void setObject(CMISObject object) 	{	this.object = object;	}
	public void setWidth(Integer width) 	{	this.width = width;	}
	public void setTitle(String title) 		{	this.title = title;	}
	public void setStreamId(String streamId){	this.streamId = streamId;	}
	public void setMimeType(String mimeType){	this.mimeType = mimeType;	}
	public void setLength(Integer length) 	{	this.length = length;	}
	public void setKind(String kind) 		{	this.kind = kind;	}
	public void setHeight(Integer height) 	{	this.height = height;	}
	@Override
	public void setId(Integer id) 			{	this.id = id;	}
	public void setRenditionDocumentId(String renditionDocumentId) {	this.renditionDocumentId = renditionDocumentId;	}
}