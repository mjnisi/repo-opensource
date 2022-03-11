<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag"%>

<tag:navigationBar selectedPanel="capabilities" selectedRepo="${selectedRepo}"/>

<c:if test="${not empty status && status eq 'error'}">
    <div class="alert alert-error">
        <button class="close" data-dismiss="alert" type="button">&times;</button>
        <strong>Update fail!</strong>
        Check the log file for more details.
    </div>
</c:if>

<c:if test="${not empty status && status eq 'ok'}">
    <div class="alert alert-success">
        <button class="close" data-dismiss="alert" type="button">&times;</button>
        Repository has been updated.
    </div>
</c:if>

<form class="form-horizontal" action="${pageContext.request.contextPath}/admin/updateCapabilities" method="post">
    <input type="hidden" value="${repository.cmisId}" name="repoId">

    <div class="control-group condensed">
        <label class="control-label" for="getDescendants">Get descendants:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="getDescendants" name="getDescendants"
                   <c:if test="${repository.getDescendants}">checked="true"</c:if>>
            </div>
        </div>
    </div>
    <div class="control-group condensed">
        <label class="control-label" for="getFolderTree">Get folder tree:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="getFolderTree" name="getFolderTree"
                   <c:if test="${repository.getFolderTree}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="contentStreamUpdatability">Content Stream updatability:</label>

        <div class="controls">
            <select id="contentStreamUpdatability" name="contentStreamUpdatability">
                <c:forEach items="${contentStreamUpdatesCapabilities}" var="contentStreamUpdateOption">
                    <option
                            <c:if test="${repository.contentStreamUpdatability == contentStreamUpdateOption}">selected="true"</c:if>>${fn:toLowerCase(contentStreamUpdateOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="changes">Changes:</label>

        <div class="controls">
            <select id="changes" name="changes">
                <c:forEach items="${changesCapabilities}" var="changeOption">
                    <option
                            <c:if test="${repository.changes == changeOption}">selected="true"</c:if>>${fn:toLowerCase(changeOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="renditions">Renditions:</label>

        <div class="controls">
            <select id="renditions" name="renditions">
                <c:forEach items="${renditionsCapabilities}" var="renditionOption">
                    <option
                            <c:if test="${repository.renditions == renditionOption}">selected="true"</c:if>>${fn:toLowerCase(renditionOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="multifiling">Multifiling:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="multifiling" name="multifiling"
                   <c:if test="${repository.multifiling}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="unfiling">Unfiling:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="unfiling" name="unfiling"
                   <c:if test="${repository.unfiling}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="versionSpecificFiling">Version specific filing:</label>

        <div class="controls">
            <div id="switchVersionSpecificFiling" class="switch">
            <input type="checkbox" id="versionSpecificFiling" name="versionSpecificFiling"
                   <c:if test="${repository.versionSpecificFiling}">checked="true"</c:if>>
                   
            </div>


	           	<c:if test="${not repository.versionSpecificFiling and not empty versionSeriesDisaggregated}">
                   <span class="help-inline">
	           		<span class="label label-warning">Warning</span> There are several version series with different folder parents, <a href="#modalAlertSpecificFilingDisaggregated" data-toggle="modal">see the complete list</a>.
                   </span>
	           	</c:if>

                
        </div>
        <c:if test="${not repository.versionSpecificFiling and not empty versionSeriesDisaggregated}">
			<div id="modalAlertSpecificFilingDisaggregated" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    			<div class="modal-header">
        			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        			<h3 id="myModalLabel">Disaggregated version series</h3>
    			</div>
    			<div class="modal-body">
    			<table class="table table-condensed">
    				<tr><th>Version Series ID</th></tr>
    			<c:forEach items="${versionSeriesDisaggregated}" var="versionSeriesId">
	    			<tr>
	    				<td>${versionSeriesId}</td>
	    			</tr>
    			</c:forEach>
    			</table>
    			</div>
    			<div class="modal-footer"><button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">OK</button></div>
			</div>
		</c:if>
        
        <c:if test="${repository.versionSpecificFiling}">
			<div id="modalAlertSpecificFiling" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    			<div class="modal-header">
        			<button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        			<h3 id="myModalLabel">Disabling Version Specific Filing</h3>
    			</div>
    			<div class="modal-body">
				<p>
					<span class="label label-warning">Warning</span>
					Please note that changing this capability, in the case of documents whose versions are filed in different folders, will let the previous versions unaltered regarding to their parents. 
					Therefore, after deleting the latest version of a document, the previous one may appear in a different folder.
				</p>
    			</div>
    			<div class="modal-footer"><button type="button" class="btn btn-primary" data-dismiss="modal" aria-hidden="true">OK</button></div>
			</div>
		</c:if>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="pwcUpdatable">PWC updatable:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="pwcUpdatable" name="pwcUpdatable"
                   <c:if test="${repository.pwcUpdatable}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="pwcSearchable">PWC searchable:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="pwcSearchable" name="pwcSearchable"
                   <c:if test="${repository.pwcSearchable}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="allVersionsSearchable">All versions searchable:</label>

        <div class="controls">
            <div class="switch">
            <input type="checkbox" id="allVersionsSearchable" name="allVersionsSearchable"
                   <c:if test="${repository.allVersionsSearchable}">checked="true"</c:if>>
                </div>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="query">Query:</label>

        <div class="controls">
            <select id="query" name="query">
                <c:forEach items="${queryCapabilities}" var="queryOption">
                    <option
                            <c:if test="${repository.query == queryOption}">selected="true"</c:if>>${fn:toLowerCase(queryOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="join">Join:</label>

        <div class="controls">
            <select id="join" name="join">
                <c:forEach items="${joinCapabilities}" var="joinOption">
                    <option
                            <c:if test="${repository.join == joinOption}">selected="true"</c:if>>${fn:toLowerCase(joinOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group condensed">
        <label class="control-label" for="acl">ACL:</label>

        <div class="controls">
            <select id="acl" name="acl">
                <c:forEach items="${aclCapabilities}" var="aclOption">
                    <option
                            <c:if test="${repository.acl == aclOption}">selected="true"</c:if>>${fn:toLowerCase(aclOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div id="aclPropagationGroup" class="control-group condensed">
        <label class="control-label" for="aclPropagation">ACL Propagation:</label>

        <div class="controls">
            <select id="aclPropagation" name="aclPropagation">
                <c:forEach items="${aclPropagationCapabilities}" var="aclPropagationOption">
                    <option
                            <c:if test="${repository.aclPropagation == aclPropagationOption}">selected="true"</c:if>>${fn:toLowerCase(aclPropagationOption)}</option>
                </c:forEach>
            </select>
        </div>
    </div>

    <div class="control-group">
        <div class="controls">
            <button type="submit" class="btn btn-primary">Save changes</button>
        </div>
    </div>

</form>

<script type="text/javascript">
	$('#switchVersionSpecificFiling').on('switch-change', function (e, data) {
		if ( data && (data.value == false) && $('#modalAlertSpecificFiling') ) {
			$('#modalAlertSpecificFiling').modal('show');
		}
	});

</script>