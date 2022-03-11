<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<tag:navigationBar selectedPanel="delete" selectedRepo="${selectedRepo}"/>


<a role="button" data-toggle="modal" class="btn btn-large btn-danger"  href="#deleteRepository">Delete Repository</a>

<div id="deleteRepository" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        <h3 id="myModalLabel">Delete Repository</h3>
    </div>
    <div class="modal-body">
        <p>Delete repository ${repository.name} (${repository.cmisId})?</p>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
        <a href="${pageContext.request.contextPath}/admin/deleteConfirmed/${repository.cmisId}" class="btn btn-danger" >Delete</a>
    </div>
</div>

