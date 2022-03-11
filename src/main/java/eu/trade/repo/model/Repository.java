package eu.trade.repo.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;

import eu.trade.repo.util.Utilities;


@Entity
@NamedQueries({
	@NamedQuery(name="Repository.all", 				query = "select repo from Repository repo"),
	@NamedQuery(name="Repository.repositoryById", 	query = "select repo from Repository repo where repo.cmisId = :repositoryId"),
	@NamedQuery(name="Repository.deleteById-native", query = "delete from Repository where cmisId = :id"),
	@NamedQuery(name="Repository.disaggregatedVersionSeries", query ="select p.value from Property p join p.object.parents pa where p.objectTypeProperty.cmisId = 'cmis:versionSeriesId' and p.object.objectType.repository.cmisId = :repositoryId group by p.value having count(distinct pa.id) > 1 and count(distinct p.object.cmisObjectId) > 1")
})
public class Repository implements DBEntity {
	private static final long serialVersionUID = 1L;
	private static final String REPOSITORY = "repository";
	private static final short LENGTH_10 = 10;
	private static final short LENGTH_100 = 100;
	
	@Id
	@GeneratedValue(generator="sq_repository")
	@SequenceGenerator(sequenceName= "sq_repository", name="sq_repository")
	private Integer id;

	@Column(name="c_acl", length=LENGTH_100)
	private String acl;

	@Column(name="c_acl_propagation", length=LENGTH_100)
	private String aclPropagation;

	@Basic
	@Column(name="c_all_versions_searchable")
	private Character allVersionsSearchable;

	@Column(name="c_changes", length=LENGTH_100)
	private String changes;

	@Column(name="c_content_stream_updatability", length=LENGTH_100)
	private String contentStreamUpdatability;

	@Basic
	@Column(name="c_get_descendants")
	private Character descendants;

	@Basic
	@Column(name="c_get_folder_tree")
	private Character folderTree;

	@Column(name="c_join", length=LENGTH_100)
	private String join;

	@Basic
	@Column(name="c_multifiling")
	private Character multifiling;

	@Basic
	@Column(name="c_pwc_searchable")
	private Character pwcSearchable;

	@Basic
	@Column(name="c_pwc_updatable")
	private Character pwcUpdatable;

	@Column(name="c_query", length=LENGTH_100)
	private String query;

	@Column(name="c_renditions", length=LENGTH_100)
	private String renditions;

	@Basic
	@Column(name="c_unfiling")
	private Character unfiling;

	@Basic
	@Column(name="c_version_specific_filing")
	private Character versionSpecificFiling;

	@Column(length=LENGTH_100)
	private String description;

	@Column(length=LENGTH_100)
	private String name;

	@Column(name="CMIS_ID", length=LENGTH_100)
	private String cmisId;

	@Enumerated(EnumType.STRING)
	@Column(name="security_type", length=LENGTH_10, nullable=false)
	private SecurityType securityType;

	@Column(name="authentication_handler", length=LENGTH_100, nullable=false)
	private String authenticationHandler;

	@Column(name="authorisation_handler", length=LENGTH_100, nullable=false)
	private String authorisationHandler;

	@OneToMany(mappedBy=REPOSITORY, cascade=CascadeType.ALL)
	private Set<Permission> permissions = new LinkedHashSet<>();

	@OneToMany(mappedBy=REPOSITORY, cascade=CascadeType.ALL)
	private Set<ObjectType> objectTypes = new LinkedHashSet<>();

	@OneToMany(mappedBy=REPOSITORY, cascade= {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval=true)
	private final Set<PermissionMapping> permissionMappings = new LinkedHashSet<>();

	@OneToMany(mappedBy=REPOSITORY, fetch=FetchType.LAZY)
	private List<Word> words = new ArrayList<Word>();

	@OneToMany(mappedBy=REPOSITORY, cascade=CascadeType.ALL, orphanRemoval = true)
	private Set<SecurityHandler> securityHandlers = new LinkedHashSet<>();

	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(
			name="repository_policy",
			joinColumns={@JoinColumn(name="repository_id", referencedColumnName="id")},
			inverseJoinColumns={@JoinColumn(name="object_type_id", referencedColumnName="id")})
	private Set<ObjectType> enabledPolicies = new LinkedHashSet<>();
	
	@Override
	public Integer getId() 						{	return this.id;	}
	public CapabilityJoin getJoin() 			{	return CapabilityJoin.fromValue(this.join);	}
	public CapabilityAcl getAcl() 				{	return CapabilityAcl.fromValue(this.acl);	}
	public CapabilityChanges getChanges() 		{	return CapabilityChanges.fromValue(this.changes);	}
	public String getName() 					{	return this.name;	}
	public String getCmisId() 					{	return this.cmisId;	}
	public CapabilityQuery getQuery()			{	return CapabilityQuery.fromValue(this.query);	}
	public CapabilityRenditions getRenditions() {	return CapabilityRenditions.fromValue(this.renditions);	}
	public String getDescription() 				{	return this.description;	}
	public AclPropagation getAclPropagation() 	{	return aclPropagation == null ? null : AclPropagation.fromValue(this.aclPropagation);	}
	public Boolean getUnfiling() 				{	return Utilities.getBooleanValue(unfiling);	}
	public Boolean getGetDescendants() 			{	return Utilities.getBooleanValue(descendants);	}
	public Boolean getGetFolderTree() 			{	return Utilities.getBooleanValue(folderTree);		}
	public Boolean getMultifiling() 			{	return Utilities.getBooleanValue(multifiling);	}
	public Boolean getPwcSearchable() 			{	return Utilities.getBooleanValue(pwcSearchable);	}
	public Boolean getPwcUpdatable() 			{	return Utilities.getBooleanValue(pwcUpdatable);	}
	public Boolean getAllVersionsSearchable() 	{	return Utilities.getBooleanValue(allVersionsSearchable);	}
	public Boolean getVersionSpecificFiling() 	{	return Utilities.getBooleanValue(versionSpecificFiling);	}
	public Set<ObjectType> getObjectTypes() 	{	return this.objectTypes;	}
	public Set<Permission> getPermissions() 	{	return this.permissions;	}
	public CapabilityContentStreamUpdates getContentStreamUpdatability()	{ return CapabilityContentStreamUpdates.fromValue(this.contentStreamUpdatability);	}
	public Set<PermissionMapping> getPermissionMappings() 					{	return this.permissionMappings;	}
	public List<Word> getWords()				{	return this.words;	}
	public SecurityType getSecurityType() 		{ 	return securityType;	}
	public String getAuthenticationHandler() 	{ 	return authenticationHandler;	}
	public String getAuthorisationHandler() 	{ 	return authorisationHandler;	}
	public Set<SecurityHandler> getSecurityHandlers() { return securityHandlers; }
	public Set<ObjectType> getEnabledPolicies() {	return enabledPolicies; }

	@Override
	public void setId(Integer id) 								{	this.id = id;	}
	public void setAcl(CapabilityAcl acl)						{	this.acl = acl == null ? null : acl.value();	}
	public void setJoin(CapabilityJoin join) 					{	this.join = join.value(); }
	public void setQuery(CapabilityQuery query)					{	this.query = query.value();	}
	public void setName(String name) 							{	this.name = name;	}
	public void setCmisId(String cmisId) 						{	this.cmisId = cmisId;	}
	public void setChanges(CapabilityChanges changes)			{	this.changes = changes.value();	}
	public void setUnfiling(Boolean unfiling) 					{	this.unfiling = Utilities.setBooleanValue(unfiling);	}
	public void setRenditions(CapabilityRenditions renditions)	{	this.renditions = renditions.value();	}
	public void setDescription(String description) 				{	this.description = description;	}
	public void setGetDescendants(Boolean cGetDescendants) 		{	this.descendants = Utilities.setBooleanValue(cGetDescendants);	}
	public void setGetFolderTree(Boolean cGetFolderTree) 		{	this.folderTree = Utilities.setBooleanValue(cGetFolderTree);	}
	public void setMultifiling(Boolean cMultifiling)	 		{	this.multifiling = Utilities.setBooleanValue(cMultifiling);	}
	public void setPwcSearchable(Boolean cPwcSearchable) 		{	this.pwcSearchable = Utilities.setBooleanValue(cPwcSearchable);	}
	public void setPwcUpdatable(Boolean cPwcUpdatable) 	 		{	this.pwcUpdatable = Utilities.setBooleanValue(cPwcUpdatable);	}
	public void setAclPropagation(AclPropagation aclPropagation) { this.aclPropagation = aclPropagation == null ? null : aclPropagation.value(); }
	public void setPermissions(Set<Permission> permissions)		{	this.permissions = permissions;	}
	public void setObjectTypes(Set<ObjectType> objectTypes)		{	this.objectTypes = objectTypes;	}
	public void setAllVersionsSearchable(Boolean cAllVersionsSearchable) {	allVersionsSearchable = Utilities.setBooleanValue(cAllVersionsSearchable);	}
	public void setVersionSpecificFiling(Boolean cVersionSpecificFiling) {	versionSpecificFiling = Utilities.setBooleanValue(cVersionSpecificFiling);	}
	public void setContentStreamUpdatability(CapabilityContentStreamUpdates contentStreamUpdatability)  	{	this.contentStreamUpdatability = contentStreamUpdatability.value();	}
	public void setPermissionMappings(Set<PermissionMapping> permissionMappings) {
		this.permissionMappings.clear();
		this.permissionMappings.addAll(permissionMappings);
	}
	public void setWords(List<Word> words) 						{	this.words = words;	}
	public void setSecurityType(SecurityType securityType) 		{ 	this.securityType = securityType;	}
	public void setAuthenticationHandler(String authenticationHandler)	{	this.authenticationHandler = authenticationHandler; }
	public void setAuthorisationHandler(String authorisationHandler) 	{	this.authorisationHandler = authorisationHandler; }
	public void setSecurityHandlers(Set<SecurityHandler> securityHandlers) { this.securityHandlers = securityHandlers; }
	public void setEnabledPolicies(Set<ObjectType> enabledPolicies)		{	this.enabledPolicies = enabledPolicies; }
	
	//ADDS/REMOVES
	public PermissionMapping addPermissionMapping(PermissionMapping permissionMapping) {
		getPermissionMappings().add(permissionMapping);
		permissionMapping.setRepository(this);

		return permissionMapping;
	}

	public PermissionMapping removePermissionMapping(PermissionMapping permissionMapping) {
		getPermissionMappings().remove(permissionMapping);
		permissionMapping.setRepository(null);

		return permissionMapping;
	}

	public Permission addPermission(Permission permission) {
		permission.setRepository(this);
		getPermissions().add(permission);

		return permission;
	}

	public Permission removePermission(Permission permission) {
		getPermissions().remove(permission);
		permission.setRepository(null);

		return permission;
	}

	public ObjectType addObjectType(ObjectType objecttp) {
		objecttp.setRepository(this);
		getObjectTypes().add(objecttp);

		return objecttp;
	}

	public ObjectType removeObjectType(ObjectType objecttp) {
		getObjectTypes().remove(objecttp);
		objecttp.setRepository(null);

		return objecttp;
	}

	public SecurityHandler addSecurityHandler(SecurityHandler securityHandler) {
		if (securityType.equals(SecurityType.SIMPLE)) {
			throw new IllegalStateException("No additional security handlers can be added to a repository with SIMPLE security.");
		}
		securityHandler.setRepository(this);
		getSecurityHandlers().add(securityHandler);

		return securityHandler;
	}

	public SecurityHandler removeSecurityHandler(SecurityHandler securityHandler) {
		getSecurityHandlers().remove(securityHandler);
		securityHandler.setRepository(null);

		return securityHandler;
	}

	public ObjectType addEnabledPolicy(ObjectType policyType) {
		if(!BaseTypeId.CMIS_POLICY.value().equals(policyType.getBase().getCmisId())) {
			throw new IllegalStateException("Only policy types allowed.");
		}
		getEnabledPolicies().add(policyType);
		return policyType;
	}
	
	public ObjectType removeEnabledPolicy(ObjectType policyType) {
		getEnabledPolicies().remove(policyType);
		
		return policyType;
	}
	
	@Override
	public String toString() {
		StringBuffer ans = new StringBuffer();
		
		String part = String.format(
				"\tName:%s \n\n\tAcl:%s \n\tAcl Propagation:%s \n\tAll Versions Searchable:%s \n\tAuthentication Handler:%s \n\tAuthorisation Handler:%s \n\tChanges:%s " +
				"\n\tConstent Stream Updatability:%s \n\tGet Descendants:%s \n\tGet Folder Tree:%s \n\tJoin:%s \n\tMulitfilling:%s \n\tPWC Searchable:%s \n\tPWC Updatable:%s" +
				"\n\tQuery:%s \n\tRenditions:%s \n\tSecurity Type:%s \n\tUnfiling:%s \n\tVersion Specific Filing:%s Description:%s", 
				getName(), getAcl(), getAclPropagation(), getAllVersionsSearchable(), getAuthenticationHandler(), getAuthorisationHandler(), getChanges(),
				getContentStreamUpdatability(), getGetDescendants(), getGetFolderTree(), getJoin(), getMultifiling(), getPwcSearchable(), getPwcUpdatable(),
				getQuery(), getRenditions(), getSecurityType(), getUnfiling(), getVersionSpecificFiling(), getDescription());
		ans.append(part);
		return ans.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if ( !(o instanceof Repository) ) {
			return false;
		}

		Repository that = (Repository) o;

		return getCmisId().equals(that.getCmisId());
	}

	@Override
	public int hashCode() {
		return cmisId.hashCode();
	}
}
