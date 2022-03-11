<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="row-fluid">
    <div class="span4">
        <table class="table table-bordered table-condensed">
            <tr class="info">
                <td>
                    Repository
                </td>
                <td>
                    User name
                </td>
                <td>
                    Authentication
                </td>
                <td>
                    Remote IP
                </td>
            </tr>
            <c:forEach items="${sessions}" var="userKey">
                <tr>
                    <td>${userKey.repositoryId}</td>
                    <td>${userKey.username}</td>
                    <td>${userKey.authenticationName}</td>
                    <td>${userKey.remoteIP}</td>
                </tr>
            </c:forEach>
        </table>
    </div>
</div>