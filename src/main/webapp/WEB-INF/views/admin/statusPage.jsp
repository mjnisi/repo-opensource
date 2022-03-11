<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>



<script type="text/javascript">

    window.onload = function () {
    	<tag:indexStatusChart divName="indexStatusMetadataChart" indexStatusMap="${indexStatusMetadata}" />
    	<tag:indexStatusChart divName="indexStatusChart" indexStatusMap="${indexStatus}" />
    };

</script>

			
<!--Body content-->
<tag:navigationBar selectedPanel="status" selectedRepo="${selectedRepo}"/>


<div class="row-fluid">
    <div class="span4">
        <table class="table table-bordered table-condensed">

            <tr class="info">
                <td colspan="2">
                    General Status
                </td>
            </tr>

            <c:forEach items="${objectCount}" var="repoSummary">
                <tr>
                    <td>${repoSummary[0]}</td>
                    <td>${repoSummary[1]}</td>
                </tr>
            </c:forEach>
        </table>


    </div>

</div>


<div class="row-fluid">
    <div class="span4">
    	<tag:indexStatusTable 
			indexType="Metadata" 
			indexStatusMap="${indexStatusMetadata}" 
			totalNumberOfIndexableObjects="${totalNumberOfObjects}" />
	</div>
    <div class="span4">
        <div class="index-status">
        	<div id="indexStatusMetadataChart"></div>
        </div>
    </div>
</div>
	
<div class="row-fluid">
    <div class="span4">
    	<tag:indexStatusTable 
			indexType="Content" 
			indexStatusMap="${indexStatus}" 
			totalNumberOfIndexableObjects="${totalNumberOfDocuments}" />
	</div>
    <div class="span4">
        <div class="index-status">
        	<div id="indexStatusChart"></div>
        </div>
    </div>
</div>




