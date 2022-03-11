<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script type="text/javascript">

    window.onload = function () {
        var r = Raphael("repositorySizeChart", 442, 230),
                pie = r.piechart(240, 110, 100, [
                    <c:forEach items="${objectCountPerRepository}" var="objCount" varStatus="loop">${objCount[1]}<c:if test="${!loop.last}">, </c:if></c:forEach>
                ], { legend:[<c:forEach items="${objectCountPerRepository}" var="objCount" varStatus="loop">"${objCount[0]}"<c:if test="${!loop.last}">, </c:if></c:forEach>], colors:[], legendpos:"west", href:[""]});
        pie.hover(function () {
            this.sector.stop();
            this.sector.scale(1.1, 1.1, this.cx, this.cy);
            if (this.label) {
                this.label[0].stop();
                this.label[0].attr({ r:7.5 });
                this.label[1].attr({ "font-weight":800 });
            }
        }, function () {
            this.sector.animate({ transform:'s1 1 ' + this.cx + ' ' + this.cy }, 500, "bounce");
            if (this.label) {
                this.label[0].animate({ r:5 }, 500, "bounce");
                this.label[1].attr({ "font-weight":400 });
            }
        });
    };
</script>

<div class="row-fluid">
    <div class="span4">
        <table class="table table-bordered">

            <tr class="info">
                <td>
                    Repository Id
                </td>
                <td>
                    Repository Name
                </td>
                <td>
                    Repository Description
                </td>
            </tr>

            <c:forEach items="${repositories}" var="repository">
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/summary/${repository.cmisId}">${repository.cmisId}</a>
                    </td>
                    <td>${repository.name}</td>
                    <td>${repository.description}</td>
                </tr>
            </c:forEach>
        </table>

    </div>
    <div class="span4">
        <div class="general-status">
            <div id="repositorySizeChart"></div>
        </div>
    </div>
</div>