<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tag:navigationBar selectedPanel="permissions" selectedRepo="${selectedRepo}"/>

<div class="row-fluid">
    <div class="span6">

        <p><i class="icon-info-sign"></i>&nbsp;Select a permission and do a right click. Only non basic permissions are
            editable.<br/></p>

        <div id="treeViewDiv">
            <tag:permissionsTree node="${permissionsTree}" isRoot="true"/>
        </div>
    </div>
</div>

<div id="newPermission" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">
    <form id="newPermissionForm" class="form-horizontal" method="post"
          action="${pageContext.request.contextPath}/admin/permissions/create/${selectedRepo}">
        <input type="hidden" name="parentId" id="parentId" value=""/>

        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
            <h3 id="myModalLabel"> Create new permission</h3>
        </div>
        <div class="modal-body">
            <div class="control-group">
                <label class="control-label required" for="permission-name">Name:</label>

                <div class="controls">
                    <input id="permission-name" name="permission-name" type="text" placeholder="Permission name"
                           class="input-xlarge required">
                </div>
            </div>
            <div class="control-group">
                <label class="control-label required" for="permission-description">Description:</label>

                <div class="controls">
                    <input id="permission-description" name="permission-description" type="text"
                           placeholder="Permission name" class="input-xlarge required">
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="submit" class="btn btn-primary form-submit">Create</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
        </div>
    </form>
</div>


<div id="renamePermission" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">

    <form id="renamePermissionForm" class="form-horizontal" method="post"
          action="${pageContext.request.contextPath}/admin/permissions/rename/${selectedRepo}">
        <input type="hidden" name="permissionName" id="permissionName" value=""/>

        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
            <h3>Rename permission</h3>
        </div>
        <div class="modal-body">
            <p><i class="icon-info-sign"></i>&nbsp;Be careful, when you rename a permission all objects with this permission will be affected.<br/></p>
            <div class="control-group">
                <label class="control-label required" for="permission-name">Name:</label>

                <div class="controls">
                    <input id="new-permission-name" name="new-permission-name" type="text" placeholder="Permission name"
                           class="input-xlarge" required>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label required" for="new-permission-description">Description:</label>

                <div class="controls">
                    <input id="new-permission-description" name="new-permission-description" type="text"
                           placeholder="Permission description" class="input-xlarge" required>
                </div>
            </div>
        </div>
        <div class="modal-footer">
            <button type="submit" class="btn btn-primary form-submit">Create</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
        </div>
    </form>
</div>

<div id="deletePermission" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
     aria-hidden="true">

    <form id="deletePermissionForm" class="form-horizontal" method="post"
          action="${pageContext.request.contextPath}/admin/permissions/delete/${selectedRepo}">
        <input type="hidden" name="permissionToDelete" id="permissionToDelete" value=""/>

        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
            <h3>Delete permission</h3>
        </div>
        <div class="modal-body">
            <p><i class="icon-info-sign"></i>&nbsp;Be careful, when you delete a permission all objects with this permission will be affected. Do you want to continue?<br/></p>

        </div>
        <div class="modal-footer">
            <button type="submit" class="btn btn-primary form-submit">Yes</button>
            <button class="btn" data-dismiss="modal" aria-hidden="true">No</button>
        </div>
    </form>
</div>

<script type="text/javascript">
    $(document).ready(function () {
        $("#newPermissionForm").validate();
        $("#treeViewDiv").jstree({
            "themes":{
                "theme":"apple",
                "dots":true,
                "icons":true
            },
            "plugins":[ "themes", "html_data", "contextmenu", "ui"],
            "ui":{
                "initially_select":[ "root" ],
                "select_limit":1
            },
            "contextmenu":{
                "items":customMenu    }
        }).bind("loaded.jstree", function (event, data) {
                    $(this).jstree("open_all");
                });

        function customMenu(node) {
            // The default set of all items
            var items = {
                newItem:{
                    label:"Create new permission",
                    action:function () {
                        $('#parentId').val($(node).attr('id'));
                        $('#newPermission').modal();
                    }
                },
                renameItem:{
                    label:"Rename permission",
                    action:function () {
                        $('#permissionName').val($(node).attr('id'));
                        $('#renamePermission').modal();
                    }
                },
                deleteItem:{
                    label:"Delete permission",
                    action:function () {
                        $('#permissionToDelete').val($(node).attr('id'));
                        $('#deletePermission').modal();
                    }
                }
            };

            //for basic permissions delete and rename is not possible
            if ($(node).hasClass("basic_permission")) {
                delete items.renameItem;
                delete items.deleteItem;
            }

            return items;
        }
    });
</script>

