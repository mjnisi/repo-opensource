--------------------------------------------------------------------------------
-- delete index data
--------------------------------------------------------------------------------

truncate table index_word_position;
truncate table index_word_object;
truncate table index_word;

update object set INDEX_STATE_CONTENT=0, INDEX_TRIES_CONTENT=0, INDEX_STATE_METADATA=0, INDEX_TRIES_METADATA=0;
--------------------------------------------------------------------------------
-- index_word_object: modify and fill
--------------------------------------------------------------------------------

create table index_word_object_new (
	id number not null,
	word_id number not null,
	object_id number not null,
	property_id number default -1 not null,
	object_type_property_id number default -1 not null,
	frequency number default 0,
	sqrt_frequency number default 0
);

insert into index_word_object_new (id, word_id, object_id, property_id, object_type_property_id, frequency, sqrt_frequency)
select id, word_id, object_id, nvl2(property_id, property_id, -1), nvl2(obj_prop_type_id, obj_prop_type_id, -1), frequency, sqrt(frequency) 
from index_word_object;

-- DROP AFFECTED CONSTRAINTS
alter table index_word_object drop constraint fk_word_object_dictionary;
alter table index_word_position drop constraint fk_index_word_position;
alter table index_word_object drop constraint pk_index_word_object;

-- DROP AFFECTED INDEXES
drop index idx_fk_word_object_dictionary;
drop index idx_fk_index_word_position;
drop index idx_index_word_object;
drop index idx_index_word_object_prop;
drop index idx_index_word_obj_objproptype;

-- drop view
drop view score_view;

-- DROP OLD TABLE
drop table index_word_object;

-- RENAME NEW TABLE TO OLD NAME
ALTER TABLE index_word_object_new RENAME TO index_word_object;

-- RECREATE CONSTRAINTS
CREATE UNIQUE INDEX pk_index_word_object ON index_word_object (id) PARALLEL 32 NOLOGGING;
ALTER INDEX pk_index_word_object noparallel;
ALTER TABLE index_word_object ADD CONSTRAINT pk_index_word_object PRIMARY KEY (id);

CREATE INDEX idx_fk_word_object_dictionary ON index_word_object (word_id) PARALLEL 32 NOLOGGING;
ALTER INDEX idx_fk_word_object_dictionary noparallel;
ALTER TABLE index_word_object ADD CONSTRAINT fk_word_object_dictionary FOREIGN KEY (word_id) REFERENCES index_word(id) on delete cascade;

CREATE INDEX idx_fk_index_word_position ON index_word_position (word_object_id) PARALLEL 32 NOLOGGING;
ALTER INDEX idx_fk_index_word_position noparallel;
ALTER TABLE index_word_position ADD CONSTRAINT fk_index_word_position foreign key (word_object_id) references index_word_object(id) on delete cascade;

-- RECREATE INDEXES

create index idx_index_word_object on index_word_object(object_id, property_id) PARALLEL 32 NOLOGGING;
ALTER INDEX idx_index_word_object noparallel;

-- create index idx_index_word_object_byword on index_word_object(word_id, object_type_property_id) PARALLEL 32 NOLOGGING;
-- ALTER INDEX idx_index_word_object_byword noparallel;

create index idx_index_word_object_byword on index_word_object(word_id, object_type_property_id) PARALLEL 32 NOLOGGING;
ALTER INDEX idx_index_word_object_byword noparallel;
		
-- modified index on table index_word		
drop index idx_index_word;
create index idx_index_word on index_word(word, repository_id) PARALLEL 32 NOLOGGING;
ALTER INDEX idx_index_word noparallel;

-- new indexes
CREATE INDEX IDX_OBJECT_INDEX_STATE_METADAT ON OBJECT(INDEX_STATE_METADATA);
CREATE INDEX IDX_INDEX_WORD_OBJECT_BYWOJECT ON INDEX_WORD_OBJECT(WORD_ID, OBJECT_ID);
CREATE INDEX IDX_OBJECT_INDEX_STATE_CONTENT ON OBJECT(INDEX_STATE_CONTENT);
CREATE INDEX IDX_INDEX_WORD_OBJECT_PROP ON INDEX_WORD_OBJECT(PROPERTY_ID);

--------------------------------------------------------------------------------
-- score_view (Oracle)
--------------------------------------------------------------------------------
create or replace view score_view as
select
  iwo.object_id as object_id,
  iwo.word_id as word_id,
  decode(sign(iwo.property_id+1),1,
    -- metadata
    (
      select (
        sqrt(frequency)* --tf
        (
          select power(1+log(10, sub_score/(
          
            select (count(*)+1.0) from index_word_object where word_id=iwo.word_id and property_id in
            (
              select prop.id
              from
              property prop join object obj on
              prop.object_id=obj.id and obj.index_state_metadata=1 and
              prop.object_type_property_id=object_type_property_id
            )
          
          )), 2)
          from
          (
            select count(*) as sub_score
            from property p where p.object_type_property_id=object_type_property_id
          )
        )* --idf
        (select (1/sqrt(sum(frequency))) from index_word_object iwo0 join property p on iwo0.object_id=p.object_id where iwo0.object_id=iwo.object_id and p.id=iwo.property_id)* --lengthNorm
        (10) --boost
      ) from index_word_object iwo1 join property p1 on p1.id=iwo1.property_id where word_id=iwo.word_id and iwo1.object_id=iwo.object_id and iwo1.property_id=iwo.property_id
    ),
    -- content
    (
      select (
        sqrt(frequency)* --tf
        (
          select power(1+log(10, sub_score/(select (count(*)+1.0) from index_word_object where word_id=iwo.word_id and property_id=-1)), 2)
          from (select count(*) as sub_score from object where index_state_content=1)
        )* --idf
        (select (1/sqrt(sum(frequency))) from index_word_object iwo0 where iwo0.object_id=object_id and iwo0.property_id=-1)* --lengthNorm
        (1) --boost
      ) from index_word_object where word_id=iwo.word_id and object_id=iwo.object_id and property_id=-1
    )
  ) as score
	
from index_word_object iwo;

--------------------------------------------------------------------------------
-- score_view (H2)
--------------------------------------------------------------------------------
/*create or replace view score_view as
select
  iwo.object_id as object_id,
  iwo.word_id as word_id,
  decode(sign(iwo.property_id+1),1,
    -- metadata
    (
      select (
        sqrt(frequency)* --tf
        (
          select power(1+log(sub_score/(
          
            select (count(*)+1.0) from index_word_object where word_id=iwo.word_id and property_id in
            (
              select prop.id
              from
              property prop join object obj on
              prop.object_id=obj.id and obj.index_state_metadata=1 and
              prop.object_type_property_id=object_type_property_id
            )
          
          )), 2)
          from
          (
            select count(*) as sub_score
            from property p where p.object_type_property_id=object_type_property_id
          )
        )* --idf
        (select (1/sqrt(sum(frequency))) from index_word_object iwo0 join property p on iwo0.object_id=p.object_id where iwo0.object_id=iwo.object_id and p.id=iwo.property_id)* --lengthNorm
        (10) --boost
      ) from index_word_object iwo1 join property p1 on p1.id=iwo1.property_id where word_id=iwo.word_id and iwo1.object_id=iwo.object_id and iwo1.property_id=iwo.property_id
    ),
    -- content
    (
      select (
        sqrt(frequency)* --tf
        (
          select power(1+log(sub_score/(select (count(*)+1.0) from index_word_object where word_id=iwo.word_id and property_id=-1)), 2)
          from (select count(*) as sub_score from object where index_state_content=1)
        )* --idf
        (select (1/sqrt(sum(frequency))) from index_word_object iwo0 where iwo0.object_id=object_id and iwo0.property_id=-1)* --lengthNorm
        (1) --boost
      ) from index_word_object where word_id=iwo.word_id and object_id=iwo.object_id and property_id=-1
    )
  ) as score
	
from index_word_object iwo;*/