--------------------------------------------------------------------------------
-- repository
--------------------------------------------------------------------------------
create table repository (
	id number not null,
	
	cmis_id varchar2(100) not null,
	name varchar2(100) not null,
	description varchar2(100) not null,
	
	c_get_descendants char(1) default 'F' not null,
	c_get_folder_tree char(1) default 'F' not null,
	c_content_stream_updatability varchar2(100) default 'none' not null,
	c_changes varchar2(100) default 'none' not null,
	c_renditions varchar2(100) default 'none' not null,
	c_multifiling char(1) default 'F' not null,
	c_unfiling char(1) default 'F' not null,
	c_version_specific_filing char(1) default 'F' not null,
	c_pwc_updatable char(1) default 'F' not null,
	c_pwc_searchable char(1) default 'F' not null,
	c_all_versions_searchable char(1) default 'F' not null,
	c_query varchar2(100) default 'none' not null,
	c_join varchar2(100) default 'none' not null,
	c_acl varchar2(100) default 'none' not null,
	c_acl_propagation varchar2(100),
	
	security_type varchar2(10) default 'SIMPLE' not null,
	authentication_handler varchar2(100) default 'builtin' not null,
	authorisation_handler varchar2(100) default 'builtin' not null,
	
	constraint pk_repository primary key (id),
	constraint uq_repository unique (cmis_id),
		
	constraint ck_get_descendants check (c_get_descendants in ('F', 'T')),
	constraint ck_get_folder_tree check (c_get_folder_tree in ('F', 'T')),
	constraint ck_content_stream_updatability check (c_content_stream_updatability in ('none', 'anytime', 'pwconly')),
	constraint ck_changes check (c_changes in ('none', 'objectidsonly', 'properties', 'all')),
	constraint ck_renditions check (c_renditions in ('none', 'read')),
	constraint ck_multifiling check (c_multifiling in ('F', 'T')),
	constraint ck_unfiling check (c_unfiling in ('F', 'T')),
	constraint ck_version_specific_filing check (c_version_specific_filing in ('F', 'T')),
	constraint ck_pwc_updatable check (c_pwc_updatable in ('F', 'T')),
	constraint ck_pwc_searchable check (c_pwc_searchable in ('F', 'T')),
	constraint ck_all_versions_searchable check (c_all_versions_searchable in ('F', 'T')),
	constraint ck_query check (c_query in ('none', 'metadataonly', 'fulltextonly', 'bothseparate', 'bothcombined')),
	constraint ck_join check (c_join in ('none', 'inneronly', 'innerandouter')),
	constraint ck_acl check (c_acl in ('none', 'discover', 'manage')),
	constraint ck_acl_propagation check (
		(c_acl in ('none') and c_acl_propagation is null) or
		(c_acl in ('discover', 'manage') and c_acl_propagation in ('objectonly', 'propagate', 'repositorydetermined'))
	),
	constraint ck_security_type check (security_type in ('SIMPLE', 'MULTIPLE'))
	
);
create sequence sq_repository start with 10000;


--------------------------------------------------------------------------------
-- permission
--------------------------------------------------------------------------------
create table permission (
	id number not null,
	repository_id number not null,
	name varchar2(100) not null,
	description varchar2(100),
	parent_id number,
	
	constraint pk_permission primary key (id),
	constraint uq_permission unique (repository_id, name),
	constraint fk_permission_repository foreign key (repository_id) 
		references repository(id) on delete cascade,
	constraint fk_permission_parent foreign key (parent_id) 
		references permission(id) 
);
create sequence sq_permission start with 10000;

create index idx_fk_permission_repository on permission(repository_id);
create index idx_fk_permission_parent on permission(parent_id);


--------------------------------------------------------------------------------
-- permission_mapping
--------------------------------------------------------------------------------
create table permission_mapping (
	id number not null,
	repository_id number not null,
	key varchar2(100) not null,
	permission_id number not null,
	
	constraint pk_permission_mapping primary key (id),
	constraint fk_permission_mapping_repo foreign key (repository_id) 
		references repository(id) on delete cascade,
	constraint fk_permission_mapping_perm foreign key (permission_id) 
		references permission(id) on delete cascade
);
create sequence sq_permission_mapping start with 10000;

create index idx_fk_permission_mapping_repo on permission_mapping(repository_id);
create index idx_fk_permission_mapping_perm on permission_mapping(permission_id);


--------------------------------------------------------------------------------
-- object_type
--------------------------------------------------------------------------------
create table object_type (
	id number not null,
	repository_id number not null,
	cmis_id varchar2(100) not null,
	local_name varchar2(100), 
	local_namespace varchar2(100), 
	query_name varchar2(100) not null,
	display_name varchar2(100), 
	base_id number,
	parent_id number,
	description varchar2(100), 
	creatable char(1) default 'F' not null,
	fileable char(1) default 'F' not null,
	queryable char(1) default 'F' not null,
	controllable_policy char(1) default 'F' not null,
	controllable_acl char(1) default 'F' not null,
	fulltext_indexed char(1) default 'F' not null,
	included_in_supertype_query char(1) default 'F' not null,
	 
	versionable char(1),
	content_stream_allowed varchar2(100),
	 
	constraint pk_object_type primary key (id),
	constraint uq_object_type_cmis unique (repository_id, cmis_id),
	constraint uq_object_type_qname unique (repository_id, query_name),

	constraint ck_creatable check (creatable in ('F', 'T')),
	constraint ck_fileable check (fileable in ('F', 'T')),
	constraint ck_queryable check (queryable in ('F', 'T')),
	constraint ck_controllable_policy check (controllable_policy in ('F', 'T')),
	constraint ck_controllable_acl check (controllable_acl in ('F', 'T')),
	constraint ck_fulltext_indexed check (fulltext_indexed in ('F', 'T')),
	constraint ck_included_in_supertype_query check (included_in_supertype_query in ('F', 'T')),
	constraint ck_versionable check (versionable is null or versionable in ('F', 'T')),
	constraint ck_content_stream_allowed check (content_stream_allowed in ('notallowed', 'allowed','required')),
	 
	constraint fk_object_type_repository foreign key (repository_id) 
		references repository(id) on delete cascade,
	constraint fk_object_type_base_id foreign key (base_id) 
		references object_type(id),
	constraint fk_object_type_parent_id foreign key (parent_id) 
		references object_type(id)
);
create sequence sq_object_type start with 10000;

create index idx_fk_object_type_repository on object_type(repository_id);
create index idx_fk_object_type_base_id on object_type(base_id);
create index idx_fk_object_type_parent_id on object_type(parent_id);



--------------------------------------------------------------------------------
-- object_type_relationship
--------------------------------------------------------------------------------
create table object_type_relationship (
	id number not null,
	object_type_id number not null,
	type varchar2(100) not null,
	referenced_object_type_id number not null,
	
	constraint pk_object_type_relationship primary key (id),
	constraint fk_object_type_relationship foreign key (object_type_id) 
		references object_type(id) on delete cascade,
	constraint ck_type check (type in ('SOURCE', 'TARGET')),
	constraint fk_ref_obj_type_relationship foreign key (referenced_object_type_id) 
		references object_type(id) on delete cascade
);
create sequence sq_object_type_relationship start with 10000;

create index idx_fk_object_type_relationshi on object_type_relationship(object_type_id);
create index idx_fk_ref_obj_type_relationsh on object_type_relationship(referenced_object_type_id);

--------------------------------------------------------------------------------
-- object_type_property
--------------------------------------------------------------------------------
create table object_type_property (
	id number not null,
	object_type_id number not null,
	cmis_id varchar2(100) not null,
	local_name varchar2(100), 
	local_namespace varchar2(100), 
	query_name varchar2(100) not null,
	display_name varchar2(100), 
	description varchar2(100), 
	property_type varchar2(100) not null, 
	cardinality varchar2(100) not null, 
	updatability varchar2(100) not null, 
	required char(1) default 'F' not null,
	queryable char(1) default 'F' not null,
	orderable char(1) default 'F' not null,
	choices varchar2(100), 
	open_choice char(1),
	default_value varchar2(100),
	
	min_value number,
	max_value number,
	resolution varchar2(100), 
	precision integer,
	max_length integer,

	constraint pk_object_type_property primary key (id),
	constraint uq_object_type_property_cmis unique (object_type_id, cmis_id),
	constraint uq_object_type_property_qname unique (object_type_id, query_name),
	
	constraint fk_obj_type_property_obj_type foreign key (object_type_id) 
		references object_type(id) on delete cascade,
	
	constraint ck_property_type check (property_type in ('string', 'boolean', 'decimal', 'integer', 'datetime', 'uri', 'id', 'html')),
	constraint ck_cardinality check (cardinality in ('single', 'multi')),
	constraint ck_updatability check (updatability in ('readonly', 'readwrite','whencheckedout','oncreate')),
	constraint ck_required check (required in ('F', 'T')),
	constraint ck_queryable_prop check (queryable in ('F', 'T')),
	constraint ck_orderable check (orderable in ('F', 'T')),
	constraint ck_open_choice check (
		(choices is not null and open_choice in ('F', 'T')) or
		(choices is null and open_choice is null)
	),
	
	constraint ck_min_value check (
		(property_type not in ('decimal', 'integer') and min_value is null) or
		(property_type in ('decimal', 'integer'))
	),
	constraint ck_max_value check (
		(property_type not in ('decimal', 'integer') and max_value is null) or
		(property_type in ('decimal', 'integer'))
	),
	constraint ck_resolution check (
		(property_type not in ('datetime') and resolution is null) or
		(property_type in ('datetime') and resolution is null) or
		(property_type in ('datetime') and resolution in ('year','date','time')) 
	),
	constraint ck_precision check (
		(property_type not in ('decimal') and precision is null) or
		(property_type in ('decimal') and precision is null) or 
		(property_type in ('decimal') and precision in (32, 64))
	),
	constraint ck_max_length check (
		(property_type not in ('string') and max_length is null) or
		(property_type in ('string'))
	)
	
	
);
create sequence sq_object_type_property start with 10000;

create index idx_fk_obj_type_property_obj_t on object_type_property(object_type_id);

--------------------------------------------------------------------------------
-- object
--------------------------------------------------------------------------------
create table object (
	id number not null,
	object_type_id number not null,
	cmis_object_id varchar2(100) not null,
	index_state_content number default 0 not null,
	index_tries_content number default 0 not null,
	index_state_metadata number default 0 not null,
	index_tries_metadata number default 0 not null,
	
	constraint pk_object primary key (id),
	constraint uq_object unique (cmis_object_id),
	constraint fk_object_object_type foreign key (object_type_id) 
		references object_type(id) on delete cascade
);
create sequence sq_object start with 10000;

create index idx_fk_object_object_type on object(object_type_id);
create index idx_object_index_state_content on object(index_state_content);
create index idx_object_index_state_metadat on object(index_state_metadata);

--------------------------------------------------------------------------------
-- object_child
--------------------------------------------------------------------------------
create table object_child (
	object_id number not null,
	child_object_id number not null,
	
	constraint pk_object_child primary key (object_id, child_object_id),
	constraint fk_object_child_object foreign key (object_id) 
		references object(id) on delete cascade,
	constraint fk_object_child_child foreign key (child_object_id) 
		references object(id) on delete cascade
);
create index idx_fk_object_child_object on object_child(object_id);
create index idx_fk_object_child_child on object_child(child_object_id);


--------------------------------------------------------------------------------
-- object_policy
--------------------------------------------------------------------------------
create table object_policy (
	object_id number not null,
	policy_object_id number not null,
	
	constraint pk_object_policy primary key (object_id, policy_object_id),
	constraint fk_object_policy_object foreign key (object_id) 
		references object(id) on delete cascade,
	constraint fk_object_policy_policy foreign key (policy_object_id) 
		references object(id) on delete cascade
);
create index idx_fk_object_policy_object on object_policy(object_id);
create index idx_fk_object_policy_policy on object_policy(policy_object_id);

--------------------------------------------------------------------------------
-- property
--------------------------------------------------------------------------------
create table property (
	id number not null,
	object_id number not null,
	object_type_property_id number not null,
	value varchar2(4000) not null,
	numeric_value number default 0 not null,
	normalized_value varchar2(4000),
	
	constraint pk_property primary key (id),
	constraint fk_property_object foreign key (object_id) 
		references object(id) on delete cascade,
	constraint fk_property_obj_type_property foreign key (object_type_property_id) 
		references object_type_property(id) on delete cascade
);
create sequence sq_property start with 10000;

create index idx_property_value on property(object_type_property_id, value);
create index idx_property_numeric_value on property(object_type_property_id, numeric_value);
create index idx_fk_property_object on property(object_id);
create index idx_fk_property_obj_type_prope on property(object_type_property_id);

--------------------------------------------------------------------------------
-- stream
--------------------------------------------------------------------------------
create table stream (
	id number not null,
	data blob,
	
	constraint pk_stream primary key (id),
	constraint fk_stream_object foreign key (id) 
		references object(id) on delete cascade
);



--------------------------------------------------------------------------------
-- rendition
--------------------------------------------------------------------------------
create table rendition (
	id number not null,
	object_id number not null,	
	stream_id varchar2(100) not null,
	mime_type varchar2(100) not null,
	length integer,
	title varchar2(100),
	kind varchar2(100) not null, 
	height integer, 
	width integer, 
	rendition_document_id varchar2(100), 
	 
	constraint pk_rendition primary key (id),
	constraint uq_rendition_stream_id unique (stream_id),
	constraint fk_rendition_object foreign key (object_id) 
		references object(id) on delete cascade
);
create sequence sq_rendition start with 10000;

create index idx_fk_rendition_object on rendition(object_id);

--------------------------------------------------------------------------------
-- acl
--------------------------------------------------------------------------------
create table acl (
	id number not null,
	object_id number not null,
	principal_id varchar2(100) not null, 
	permission_id number not null,
	is_direct char(1) default 'F' not null,
	
	constraint pk_acl primary key (id),
	constraint fk_acl_object foreign key (object_id) 
		references object(id) on delete cascade,
	constraint fk_acl_permmission foreign key (permission_id) 
		references permission(id) on delete cascade
);
create sequence sq_acl start with 10000;

create index idx_fk_acl_object on acl(object_id);
create index idx_fk_acl_permmission on acl(permission_id);
create index idx_acl on acl(principal_id, permission_id);




--------------------------------------------------------------------------------
-- index_word
--------------------------------------------------------------------------------
create table index_word (
	id number not null,
	repository_id number not null,
	word varchar2(200),
	constraint pk_index_word primary key (id),
	constraint fk_index_word_repository foreign key (repository_id) 
		references repository(id) on delete cascade
);
create sequence sq_index_word start with 10000;

create index idx_fk_index_word_repository on index_word(repository_id);
create index idx_index_word on index_word(word, repository_id);

--------------------------------------------------------------------------------
-- index_word_object
--------------------------------------------------------------------------------
create table index_word_object (
	id number not null,
	word_id number not null,
	object_id number not null,
	property_id number default -1 not null,
	object_type_property_id number default -1 not null,
	frequency number default 0,
	sqrt_frequency number default 0,
	
	constraint pk_index_word_object primary key (id),
	constraint fk_word_object_dictionary foreign key (word_id) 
		references index_word(id) on delete cascade
);
create sequence sq_index_word_object start with 10000;

create index idx_index_word_object_bywoject on index_word_object(word_id, object_id);
create index idx_fk_word_object_dictionary on index_word_object(word_id);
create index idx_index_word_object on index_word_object(object_id, property_id);
create index idx_index_word_object_byword on index_word_object(word_id, object_type_property_id);
create index idx_index_word_object_prop on index_word_object(property_id);


--------------------------------------------------------------------------------
-- index_word_position
--------------------------------------------------------------------------------
create table index_word_position (
	word_object_id number not null,
	position number not null,
	step number default 1,
	constraint pk_index_word_position primary key (word_object_id, position),
	constraint fk_index_word_position foreign key (word_object_id) 
		references index_word_object(id) on delete cascade
);
create index idx_fk_index_word_position on index_word_position(word_object_id);

--------------------------------------------------------------------------------
-- security_handler
--------------------------------------------------------------------------------
create table security_handler (
	id number not null,
	repository_id number not null,
	handler_type varchar2(20) not null,
	handler_name varchar2(100) not null,
	constraint pk_security_handler primary key (id),
	constraint uq_security_handler unique (repository_id, handler_type, handler_name),
	constraint ck_security_handler check (handler_type in ('AUTHENTICATION','AUTHORISATION')),
	constraint fk_security_handler_repository foreign key (repository_id) 
		references repository(id) on delete cascade
);
create sequence sq_security_handler start with 10000;
create index idx_fk_security_handler_repo on security_handler(repository_id);

--------------------------------------------------------------------------------
-- change_event
--------------------------------------------------------------------------------
create table change_event (
	id number not null,
	repository_id number not null,
	cmis_object_id varchar2(100) not null,
	change_type varchar2(100) not null,
	change_log_token varchar2(100) not null,
	change_time timestamp not null,
	username varchar2(100) not null,
	
	constraint pk_change_event primary key (id),
	constraint uq_change_event unique (change_log_token),
	constraint ck_change_event_type check (change_type in ('created','updated','deleted', 'security')),
	constraint fk_change_event_repository foreign key (repository_id) 
		references repository(id) on delete cascade
);
create sequence sq_change_event start with 10000;
create index idx_fk_change_event_repository on change_event(repository_id);
create index idx_change_event_repository on change_event(repository_id, id);

--------------------------------------------------------------------------------
-- object_path_view
--------------------------------------------------------------------------------
create view object_path_view as
select object_id, object_path, parent_id, repository_id, type_id
from
(
	select 
		p.object_id object_id, p.value object_path, parent_oc.object_id parent_id, r.cmis_id repository_id, 'cmis:folder' type_id
	from property p 
		inner join object_type_property otp on p.object_type_property_id = otp.id
		inner join object_type ot on otp.object_type_id = ot.id
		inner join repository r on ot.repository_id = r.id
		left join object_child parent_oc on p.object_id = parent_oc.child_object_id
	where 
		otp.cmis_id = 'cmis:path' and
		ot.cmis_id = 'cmis:folder'

	union all

	select 
		child_oc.child_object_id object_id, 
		(
			CASE parent_p.value
			WHEN '/' THEN '/' || child_p.value
			ELSE parent_p.value || '/' || child_p.value
			END
		) object_path, 
		child_oc.object_id parent_id, parent_r.cmis_id repository_id, child_ot.cmis_id type_id
	from 
		object_child child_oc, property child_p, object_type_property child_otp, object_type child_ot, 
		property parent_p, object_type_property parent_otp, object_type parent_ot, repository parent_r
	where 
		child_oc.child_object_id = child_p.object_id and
		child_p.object_type_property_id = child_otp.id and
		child_otp.cmis_id = 'cmis:name' and
		child_otp.object_type_id = child_ot.id and
		child_ot.cmis_id <> 'cmis:folder' and
		child_oc.object_id = parent_p.object_id and
		parent_p.object_type_property_id = parent_otp.id and
		parent_otp.object_type_id = parent_ot.id and
		parent_ot.repository_id = parent_r.id and
		parent_otp.cmis_id = 'cmis:path' and
		parent_ot.cmis_id = 'cmis:folder'
);

--------------------------------------------------------------------------------
-- object_version_view
--------------------------------------------------------------------------------
create view object_version_view as
select object_id, version_type
from
(
	select 
		o.id object_id, 'NON_VERSION' as version_type
	from 
		object o
	where 
		EXISTS (
			select 1
			from object_type ot, object_type base_ot
			where o.object_type_id = ot.id
			and base_ot.id = ot.base_id
			and base_ot.cmis_id <> 'cmis:document'
		)

	union all
  
	select 
		o.id object_id, 'LATEST' as version_type
	from 
		object o
	where 
		EXISTS (
			select 1
			from object_type ot, object_type base_ot
			where o.object_type_id = ot.id
			and base_ot.id = ot.base_id
			and base_ot.cmis_id = 'cmis:document'
		)
		and EXISTS (
			select 1 
			from property is_latest_version_p
			where 
				o.id = is_latest_version_p.object_id and
				is_latest_version_p.numeric_value = 1 and
				EXISTS (
					select 1 
					from object_type_property is_latest_version_otp
					where 
					is_latest_version_p.object_type_property_id = is_latest_version_otp.id and
					is_latest_version_otp.cmis_id = 'cmis:isLatestVersion' 
				)
		)
		
	union all

	select 
		o.id object_id, 'NOT_LATEST' as version_type
	from 
		object o
	where 
		EXISTS (
			select 1
			from object_type ot, object_type base_ot
			where o.object_type_id = ot.id
			and base_ot.id = ot.base_id
			and base_ot.cmis_id = 'cmis:document'
		)
		and EXISTS (
			select 1 
			from property is_latest_version_p
			where 
				o.id = is_latest_version_p.object_id and
				is_latest_version_p.numeric_value = 0 and
				EXISTS (
					select 1 
					from object_type_property is_latest_version_otp
					where 
						is_latest_version_p.object_type_property_id = is_latest_version_otp.id and
						is_latest_version_otp.cmis_id = 'cmis:isLatestVersion' 
				)
		) 
		and EXISTS (
			select 1 
			from property version_label_p
			where 
				o.id = version_label_p.object_id and
				version_label_p.value <> 'PWC' and
				EXISTS (
					select 1 
					from object_type_property version_label_otp
					where 
						version_label_p.object_type_property_id = version_label_otp.id and
						version_label_otp.cmis_id = 'cmis:versionLabel' 
				)
		)

	union all

	select 
		o.id object_id, 'PWC' as version_type
	from 
		object o
	where 
		EXISTS (
			select 1
			from object_type ot, object_type base_ot
			where o.object_type_id = ot.id
			and base_ot.id = ot.base_id
			and base_ot.cmis_id = 'cmis:document'
		)
		and EXISTS (
			select 1 
			from property version_label_p
			where 
				o.id = version_label_p.object_id and
				version_label_p.value = 'PWC' and
				EXISTS (
					select 1 
					from object_type_property version_label_otp
					where 
						version_label_p.object_type_property_id = version_label_otp.id and
						version_label_otp.cmis_id = 'cmis:versionLabel' 
				)
		)
);



--------------------------------------------------------------------------------
-- children_count_view
--------------------------------------------------------------------------------

create view children_count_view as
select o.id, count(oc.object_id) as num
from object o left join object_child oc on o.id = oc.object_id
group by o.id;

--------------------------------------------------------------------------------
-- parents_count_view
--------------------------------------------------------------------------------

create view parents_count_view as
select o.id, count(oc.child_object_id) as num
from object o left join object_child oc on o.id = oc.child_object_id
group by o.id;
