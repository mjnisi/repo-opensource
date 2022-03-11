<%@ tag body-content="scriptless"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="selectedPanel" type="java.lang.String" required="true" rtexprvalue="true"%>
<%@ attribute name="selectedRepo" type="java.lang.String" required="true" rtexprvalue="true"%>


<ul class="nav nav-tabs">
    <li <c:if test="${selectedPanel eq 'status'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/summary/${selectedRepo}"><i class="icon-home"></i>&nbsp;Status</a></li>
    <li <c:if test="${selectedPanel eq 'capabilities'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/capabilities/${selectedRepo}"><i class="icon-wrench"></i>&nbsp;Capabilities</a></li>
    <li <c:if test="${selectedPanel eq 'renditions'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/renditions/${selectedRepo}"><i class="icon-list-alt"></i>&nbsp;Renditions</a></li>
    <li <c:if test="${selectedPanel eq 'policies'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/policies/${selectedRepo}"><i class="icon-cog"></i>&nbsp;Policies</a></li>
    <li <c:if test="${selectedPanel eq 'security'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/security/${selectedRepo}"><i class="icon-user"></i>&nbsp;Security</a></li>
    <li <c:if test="${selectedPanel eq 'permissions'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/permissions/${selectedRepo}"><i class="icon-chevron-down"></i>&nbsp;Permissions</a></li>
    <li <c:if test="${selectedPanel eq 'mappings'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/mappings/${selectedRepo}"><i class="icon-random"></i>&nbsp;Mappings</a></li>
    <li <c:if test="${selectedPanel eq 'delete'}">class="active"</c:if>><a href="${pageContext.request.contextPath}/admin/delete/${selectedRepo}"><i class="icon-trash"></i>&nbsp;Delete</a></li>
</ul>