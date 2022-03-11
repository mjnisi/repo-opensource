<%@ tag body-content="scriptless"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<%@ attribute name="node" type="java.util.Map" required="true" rtexprvalue="true" %>
<%@ attribute name="isRoot" type="java.lang.Boolean" required="true" rtexprvalue="true" %>

<c:if test="${isRoot}">
  <ul>
    <li id="${node.name}" <c:if test="${(node.name eq 'cmis:all') ||
	                                    (node.name eq 'cmis:read') ||
										(node.name eq 'cmis:write')}">class="basic_permission"</c:if>>
		<a href="">${node.name}</a>
</c:if>

<c:if test="${not empty node.children}">
	<ul>
	<c:forEach var="child" items="${node.children}">
	  <li id="${child.name}" <c:if test="${(child.name eq 'cmis:all') ||
	                                       (child.name eq 'cmis:read') ||
										   (child.name eq 'cmis:write')}">class="basic_permission"</c:if>>
	    <a href="">${child.name}</a>
	  	<tag:permissionsTree node="${child}" isRoot="false"/>
	  </li>
	</c:forEach>
	</ul>
</c:if>

<c:if test="${isRoot}">
    </li>
  </ul>
</c:if>