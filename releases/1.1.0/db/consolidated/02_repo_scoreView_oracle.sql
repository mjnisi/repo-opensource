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