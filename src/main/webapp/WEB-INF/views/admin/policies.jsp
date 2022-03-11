<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tag:navigationBar selectedPanel="policies" selectedRepo="${selectedRepo}"/>
<div class="row-fluid">
    <div class="span6">
        <form action="${pageContext.request.contextPath}/admin/policies/update/${selectedRepo}" method="POST">
            <table class="table table-bordered">
                <tr class="info">
                    <td>Policy</td>
                    <td>Description</td>
                    <td>Enabled</td>
                </tr>
                <c:forEach items="${allPolicies}" var="entry">
                    <tr class="input-holder">
                        <td>${entry.key.cmisId}</td>
                        <td>${entry.key.description}</td>
                        <td><input type="checkbox" name="enabledPolicies[]" value="${entry.key.cmisId}"
                        	<c:if test="${entry.value}"> checked="checked"</c:if> 
                        	/>
                        </td>
                    </tr>
                </c:forEach>

            </table>
            <input id="save_changes" class="btn btn-primary" type="submit" value="Save changes">
        </form>
    </div>
    <div class="span4">
    	<p><span class="label label-info">Info</span> 
    		If you miss policies in the list, add the cmis:policy sub-types 
    		in this repository to start using them.</p>

    	 
    	<p><span class="label label-warning">Warning</span> 
    		Enabling policies will have an impact for all the services 
    		intercepted by the policies.</p>
    </div>
</div>
