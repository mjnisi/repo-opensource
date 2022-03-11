<%@ tag %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<%@ attribute name="divName" type="java.lang.String" required="true" rtexprvalue="true"%>
<%@ attribute name="indexStatusMap" type="java.util.LinkedHashMap" required="true" rtexprvalue="true" %>


var r = Raphael("${divName}", 442, 230),
          pie = r.piechart(240, 110, 100, 
          		[
                <c:forEach items="${indexStatusMap}" var="index" varStatus="loop">
                	${index.value.numObjects}
                	<c:if test="${!loop.last}">, </c:if>
                </c:forEach>
            ], 
            { legend:
            	[<c:forEach items="${indexStatusMap}" var="index" varStatus="loop">
            		"${index.key}"
            		<c:if test="${!loop.last}">, </c:if>
            	</c:forEach>], 
            	colors:[<c:forEach items="${indexStatusMap}" var="index" varStatus="loop">
            		getColorByIndexState(${index.value.stateId})
            		<c:if test="${!loop.last}">, </c:if>
            	</c:forEach>], 
            	legendpos:"west",
            	matchColors : true
       			}
          );
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


  