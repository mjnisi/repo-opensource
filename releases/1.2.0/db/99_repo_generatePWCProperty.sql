--
-- VERY IMPORTANT! this script should be executed only once!
-- if the script is executed several times will produce corrupted data.
--
-- inserts in property table the values for new CMIS 1.1 property cmis:isPrivateWorkingCopy
--
-- executed before the migration tool, should insert 0 rows
--
-- executed after the migration tool, should insert this number of rows:
-- select count(*) from property where object_type_property_id in (select id from object_type_property where cmis_id = 'cmis:versionLabel');
--
insert into property(id, object_id, object_type_property_id, value, numeric_value, normalized_value)
select 
sq_property.nextval id, 
pVL.object_id object_id,
otpPWC.id object_type_property_id,
decode(pVL.value,'PWC','true','false') value,
decode(pVL.value,'PWC',1,0) numeric_value,
null normalized_value
from object_type_property otpPWC, 
object_type_property otpVL, 
property pVL
where otpPWC.cmis_id = 'cmis:isPrivateWorkingCopy'
and otpVL.cmis_id = 'cmis:versionLabel'
and otpVL.object_type_id = otpPWC.object_type_id
and pVl.object_type_property_id = otpVL.id;