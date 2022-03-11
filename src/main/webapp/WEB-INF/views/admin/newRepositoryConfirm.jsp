<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!--Body content-->
<c:if test="${not empty repository}">

    <div class="alert alert-success">
        <i class="icon-ok-circle"></i>&nbsp;Repository created.
    </div>
 <pre>
    Repository id: ${repository.cmisId}
    Repository name: ${repository.name}
    Repository description: ${repository.description}
 </pre>

</c:if>

<c:if test="${empty repository && not empty error}">
    <div class="alert alert-error">
        <i class="icon-warning-sign"></i>&nbsp;Error creating repository.
    </div>
<pre>
Error message:
${error}
Check the log file for further details.
</pre>
</c:if>