<%@ tag body-content="scriptless"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ attribute name="indexValue" type="java.lang.Long" required="true" rtexprvalue="true"%>
<%@ attribute name="totalNumberOfDocuments" type="java.lang.Long" required="true" rtexprvalue="true"%>
<%@ attribute name="indexStateId" type="java.lang.Integer" required="true" rtexprvalue="true"%>

<c:set var="chartClass">
    <c:choose>
        <c:when test="${indexStateId == 1 || indexStateId == 4}">bar-success</c:when>
        <c:when test="${indexStateId == 2}">bar-danger</c:when>
        <c:when test="${indexStateId == 0}">bar-info</c:when>
        <c:when test="${indexStateId == 3}">bar-warning</c:when>
    </c:choose>
</c:set>

<div class="progress">
    <div class="bar ${chartClass}" style="width: <fmt:formatNumber value="${indexValue/totalNumberOfDocuments}" type="PERCENT" maxFractionDigits="0"/>"></div>
</div>