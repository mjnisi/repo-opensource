<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<form id="newRepoForm" class="form-horizontal" action="${pageContext.request.contextPath}/admin/createRepository">
    <div class="control-group">
        <label class="control-label" for="repositoryId">Repository ID:</label>

        <div class="controls">
            <input type="text" id="repositoryId" name="repositoryId" placeholder="Repository ID" required >
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="repositoryName">Repository name:</label>

        <div class="controls">
            <input type="text" id="repositoryName" name="repositoryName" placeholder="Repository name" required >
        </div>
    </div>
    <div class="control-group">
        <label class="control-label" for="repositoryDesc">Repository description:</label>

        <div class="controls">
            <input type="text" id="repositoryDesc" name="repositoryDesc" placeholder="Repository description" >
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <button type="submit" class="btn btn-primary">Create repository</button>
        </div>
    </div>
</form>

<script type="text/javascript">

    $(document).ready(function () {
        $("#newRepoForm").validate();
    })
</script>