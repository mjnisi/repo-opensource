delete from user_roles where role_id in (select role_id from roles where app_id in (select app_id from applications where abrev = 'REPO') );
delete from role_operations where role_id in (select role_id from roles where app_id in (select app_id from applications where abrev = 'REPO') );
delete from operations where app_id in (select app_id from applications where abrev = 'REPO');
delete from roles where app_id in (select app_id from applications where abrev = 'REPO');
delete from applications where  abrev = 'REPO';