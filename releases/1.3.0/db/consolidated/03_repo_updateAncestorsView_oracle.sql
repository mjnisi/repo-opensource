--create or replace view ancestors_view as
--select child_object_id object_id, 
--connect_by_root object_id ancestor_id
--from object_child
--connect by prior child_object_id = object_id;

create or replace view ancestors_view as
  WITH ancestors (object_id, ancestor_id) AS (
       SELECT child_object_id object_id, object_id ancestor_id
       FROM object_child
       UNION ALL
          SELECT a.object_id, oc.object_id
          FROM ancestors a, object_child oc
          WHERE a.ancestor_id = oc.child_object_id
  )
  SELECT object_id, ancestor_id
  FROM ancestors;

create or replace view descendants_view as
select 
    object_id,
    connect_by_root child_object_id descendant_id
  from object_child 
  connect by prior object_id = child_object_id;