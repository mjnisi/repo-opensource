<%@ page import="eu.trade.repo.web.admin.AdminController" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--Sidebar content-->
<div class="well sidebar-nav">
    <ul class="nav nav-list">
        <li ${selectedAction eq 'home' ? 'class="active"' : ''} ><a href="${pageContext.request.contextPath}/admin">Home</a></li>
        <li ${selectedAction eq 'newRepo' ? 'class="active"' : ''} ><a href="${pageContext.request.contextPath}/admin/newRepository">Create new repository</a></li>
        <li ${selectedAction eq 'session' ? 'class="active"' : ''} ><a href="${pageContext.request.contextPath}/admin/sessions">Sessions</a></li>
        <li ${selectedAction eq 'configuration' ? 'class="active"' : ''} ><a href="#">Configuration</a></li>

        <li class="nav-header">Repositories</li>

        <c:forEach items="${repositories}" var="repository" varStatus="status">
            <li ${repository.cmisId eq selectedRepo ? 'class="active"' : ''}>
                <a href="${pageContext.request.contextPath}/admin/summary/${repository.cmisId}">${repository.name}</a>
            </li>
        </c:forEach>
    </ul>
</div>