create or replace view ancestors_view as
select a.object_id object_id, b.parent_id ancestor_id, a.repository_id repository_id
from
object_path_view a join object_path_view b
on
a.object_path like concat(b.object_path, '%') and
a.repository_id=b.repository_id and
b.parent_id is not null;