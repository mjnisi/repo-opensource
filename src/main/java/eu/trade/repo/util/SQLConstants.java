package eu.trade.repo.util;

public class SQLConstants {
	private SQLConstants() {}
	
	public static final String BASEQUERY_PREFIX = "from CMISObject obj left join obj.properties pr where pr.objectTypeProperty.cmisId = :objectTypeProperty and obj.id in (";
	public static final String BASEQUERY_IDS_PREFIX 	= "select obj.id "+BASEQUERY_PREFIX;
	public static final String BASEQUERY_OBJS_PREFIX 	= "select obj    "+BASEQUERY_PREFIX;
	public static final String BASEQUERY_POSTFIX_ASC 	= " order by pr.numericValue ASC, pr.value ASC";
	public static final String BASEQUERY_POSTFIX_DESC 	= " order by pr.numericValue DESC, pr.value DESC";
	
	public static final String BASEQUERY_CHILDRENIDS_ASC  = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId )"+BASEQUERY_POSTFIX_ASC;
	public static final String BASEQUERY_CHILDRENIDS_DESC = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId )"+BASEQUERY_POSTFIX_DESC;
	
	public static final String BASEQUERY_CHILDRENIDS_ACLASC  = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) )"+BASEQUERY_POSTFIX_ASC;
	public static final String BASEQUERY_CHILDRENIDS_ACLDESC = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) )"+BASEQUERY_POSTFIX_DESC;
	
	public static final String BASEQUERY_CHILDRENIDS_ACLVERSION_ASC	 = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes)))"+ BASEQUERY_POSTFIX_ASC;
	public static final String BASEQUERY_CHILDRENIDS_ACLVERSION_DESC = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and o.id in (select a.object.id from Acl a where a.principalId in (:principalIds) and a.permission.id in (:permissionIds)) and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes)))"+ BASEQUERY_POSTFIX_DESC;
	
	public static final String BASEQUERY_CHILDRENIDS_VERSIONASC  = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes)))"+ BASEQUERY_POSTFIX_ASC;
	public static final String BASEQUERY_CHILDRENIDS_VERSIONDESC = BASEQUERY_IDS_PREFIX+"select distinct o.id from CMISObject o join o.parents p where p.cmisObjectId = :objectId and exists (select 1 from ObjectVersion ov where ov.id = o.id and ov.versionType in (:versionTypes)))"+ BASEQUERY_POSTFIX_DESC;	

}
