CREATE OR REPLACE VIEW ANCESTORS_VIEW AS
  SELECT a.object_id object_id,
    b.parent_id ancestor_id
  FROM object_path_view a
  JOIN object_path_view b
  ON a.object_path LIKE concat(b.object_path, '%')
  AND b.parent_id IS NOT NULL;
  
 create or replace view descendants_view as
  SELECT b.object_id object_id,
    a.object_id descendant_id
  FROM object_path_view a
  JOIN object_path_view b
  ON a.object_path LIKE concat(b.object_path, '/%');