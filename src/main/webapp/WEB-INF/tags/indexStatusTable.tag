<%@ tag body-content="scriptless"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<%@ attribute name="indexType" type="java.lang.String" required="false" rtexprvalue="true"%>
<%@ attribute name="totalNumberOfIndexableObjects" type="java.lang.Long" required="true" rtexprvalue="true" %>
<%@ attribute name="indexStatusMap" type="java.util.LinkedHashMap" required="true" rtexprvalue="true" %>


<table class="table table-bordered table-condensed">

    <tr class="info">
        <td colspan="4">
            ${indexType} Index Status, total: ${totalNumberOfIndexableObjects}
        </td>
    </tr>
    <c:forEach items="${indexStatusMap}" var="index" varStatus="loop">
        <tr>
            <td>${index.key}. ${index.value.stateDescription}</td>
            <td>${index.value.numObjects}</td>
            <td><fmt:formatNumber value="${index.value.numObjects / totalNumberOfIndexableObjects}" type="PERCENT" maxFractionDigits="3"/></td>
            <td style="width: 200px">
            	<tag:drawBarChart indexValue="${index.value.numObjects}" totalNumberOfDocuments="${totalNumberOfIndexableObjects}" indexStateId="${index.value.stateId}"/>
            </td>
        </tr>
    </c:forEach>
</table>
    