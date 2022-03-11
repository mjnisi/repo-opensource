--------------------------------------------------------------------------------
-- repository_policy
--------------------------------------------------------------------------------
create table repository_policy (
	repository_id number not null,
	object_type_id number not null,
	
	constraint pk_repository_policy primary key (repository_id, object_type_id),
	constraint fk_repository_policy_repo foreign key (repository_id) 
		references repository(id) on delete cascade,
	constraint fk_repository_policy_type foreign key (object_type_id) 
		references object_type(id) on delete cascade
);
create index idx_fk_repository_policy_repo on repository_policy(repository_id);
create index idx_fk_repository_policy_type on repository_policy(object_type_id);