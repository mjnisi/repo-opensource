--drop table object_secondary_type;
create table object_secondary_type (
	object_id number not null,
	object_type_id number not null,
	constraint pk_object_sec_type primary key (object_id, object_type_id),
	constraint fk_object_sec_type_object foreign key (object_id) 
		references object(id) on delete cascade,
	constraint fk_object_sec_type_obj_type foreign key (object_type_id) 
		references object_type(id) on delete cascade
);

