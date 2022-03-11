--------------------------------------------------------------------------------
-- index_transient
--------------------------------------------------------------------------------
drop table index_transient;
drop table index_transient_metadata;

create table index_transient (
	position number not null,
	object_id number not null,
	word varchar2(200),
	step number default 1,
	constraint pk_index_transient primary key (position, object_id, word)	
); 
create index idx_index_transient_object on index_transient(object_id);
create index idx_index_transient_word on index_transient(word);
create index idx_index_transient_objectAndWord on index_transient(object_id, word);


create table index_transient_metadata (
	position number not null,
	object_id number not null,
	word varchar2(200),
	step number default 1,
	property_id number not null,
	object_type_property_id number not null,
	constraint pk_index_transient_metadata primary key (position, property_id, object_id, word)	
); 
create index idx_index_metadata_object on index_transient_metadata(object_id);
create index idx_index_metadata_word on index_transient_metadata(word);
create index idx_index_metadata_objectAndWord on index_transient_metadata(object_id, word);