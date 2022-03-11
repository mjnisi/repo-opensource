alter table property add normalized_value varchar2(4000);

alter table index_word_object add property_id number;
alter table index_word_object add obj_prop_type_id number;

create index idx_index_word_object_prop on index_word_object(property_id);
create index idx_index_word_obj_objproptype on index_word_object(obj_prop_type_id);

alter table object add index_state_content number default 0 not null;
alter table object add index_tries_content number default 0 not null;
alter table object add index_state_metadata number default 0 not null;
alter table object add index_tries_metadata number default 0 not null;

alter table object drop column index_state;
alter table object drop column index_tries;