<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags"%>

<tag:navigationBar selectedPanel="security" selectedRepo="${selectedRepo}"/>

<c:if test="${not empty status}">
	<c:choose>
		<c:when test="${status eq 'ok'}">
		    <div class="alert alert-success">
		        <button class="close" data-dismiss="alert" type="button">&times;</button>
		        Repository security has been updated.
    		</div>
		</c:when>
		<c:when test="${status eq 'error'}">
		    <div class="alert alert-error">
		        <button class="close" data-dismiss="alert" type="button">&times;</button>
		        <strong>Security Update Fail!</strong>
		        Check the log file for more details.
    		</div>
		</c:when>
		<c:when test="${status eq 'migrate'}">
		    <div class="alert alert-error">
		        <button class="close" data-dismiss="alert" type="button">&times;</button>
		        <strong>Security Type Toggle Fail!</strong>
		        The repository current ACLs must be previously migrated.
    		</div>
		</c:when>
	</c:choose>
</c:if>

<form class="form-horizontal" action="${pageContext.request.contextPath}/admin/toggleSecurityType" method="post">
	<input type="hidden" value="${repository.cmisId}" name="repoId">
	
	<div class="control-group condensed">
		<label class="control-label" for="securityType"><strong>Security type: </strong><c:out value="${securityType}"/></label>
		<c:choose>
			<c:when test="${isSimple}">
				<c:set var="toggleAction" value="Enable"/>
			</c:when>
			<c:otherwise>
				<c:set var="toggleAction" value="Disable"/>
			</c:otherwise>
		</c:choose>
			
		<div class="controls">
			<button type="submit" class="btn btn-primary"><c:out value="${toggleAction}"/> multiple security handlers</button>
		</div>
	</div>
</form>

<form class="form-horizontal" action="${pageContext.request.contextPath}/admin/updateSecurity" method="post">
    <input type="hidden" value="${repository.cmisId}" name="repoId">

	<c:choose>
		<c:when test="${isSimple}">

			<div class="control-group condensed">
				<label class="control-label" for="defaultAuthenticationHandler">Authentication:</label>

				<div class="controls">
					<select id="defaultAuthenticationHandler" name="defaultAuthenticationHandler">
						<c:forEach items="${availableAuthenticationHandlers}" var="authenticationHandlerOption">
							<option value="${authenticationHandlerOption.name}" title="${authenticationHandlerOption.description}"
								<c:if test="${defaultAuthenticationHandler == authenticationHandlerOption.name}">selected="true"</c:if>>${authenticationHandlerOption.name} (${authenticationHandlerOption.domain})</option>
						</c:forEach>
					</select>
				</div>
			</div>
			
			<div class="control-group condensed">
				<label class="control-label" for="defaultAuthorisationHandler">Authorisation:</label>

				<div class="controls">
					<select id="defaultAuthorisationHandler" name="defaultAuthorisationHandler">
						<c:forEach items="${availableAuthorisationHandlers}" var="authorisationHandlerOption">
							<option value="${authorisationHandlerOption.name}" title="${authorisationHandlerOption.description}"
								<c:if test="${defaultAuthorisationHandler == authorisationHandlerOption.name}">selected="true"</c:if>>${authorisationHandlerOption.name} (${authorisationHandlerOption.domain})</option>
						</c:forEach>
					</select>
				</div>
			</div>
		</c:when>
		<c:otherwise>

			<div class="row-fluid">
				<div class="control-group condensed">
					<label class="control-label" for="defaultAuthenticationHandler">Default Authentication:</label>

					<div class="controls">
						<select id="defaultAuthenticationHandler" name="defaultAuthenticationHandler">
							<c:forEach items="${availableAuthenticationHandlers}" var="authenticationHandlerOption">
								<option value="${authenticationHandlerOption.name}" <c:if test="${defaultAuthenticationHandler == authenticationHandlerOption.name}">selected="true"</c:if>>${authenticationHandlerOption.name}</option>
							</c:forEach>
						</select>
					</div>
				</div>

				<div class="control-group condensed">
					<label class="control-label" for="defaultAuthorisationHandler">Default Authorisation:</label>

					<div class="controls">
						<select id="defaultAuthorisationHandler" name="defaultAuthorisationHandler">
							<c:forEach items="${availableAuthorisationHandlers}" var="authorisationHandlerOption">
								<option value="${authorisationHandlerOption.name}" <c:if test="${defaultAuthorisationHandler == authorisationHandlerOption.name}">selected="true"</c:if>>${authorisationHandlerOption.name}</option>
							</c:forEach>
						</select>
					</div>
				</div>
			</div>

			<div class="row-fluid">
				<div class="span4">
					<table class="table table-bordered table-condensed">

						<tr class="info">
							<td>Authentication</td>
							<td>Domain</td>
							<td>Enabled</td>
						</tr>

						<c:forEach items="${availableAuthenticationHandlers}" var="availableAuthenticationHandler">
							<tr title="${availableAuthenticationHandler.description}">
								<td>${availableAuthenticationHandler.name}</td>
								<td>${availableAuthenticationHandler.domain}</td>
								<td>
									<div class="switch">
										<input type="checkbox" id="enableAuthenticationHandler${availableAuthenticationHandler.name}" name="enableAuthenticationHandler${availableAuthenticationHandler.name}"
											<c:if test="${not empty enabledAuthenticationHandlers[availableAuthenticationHandler.name]}">checked="true"</c:if>>
									</div>
								</td>
							</tr>
						</c:forEach>
					</table>
				</div>
				<div class="span4">
					<p>
						<span class="label label-info">Info</span>
						The authentication domain is used for qualify <u>users</u> in the principal ID of the ACL. Eg. ecas/user01
					</p>
				</div>				
			</div>

			<div class="row-fluid">
				<div class="span4">
					<table class="table table-bordered table-condensed">

						<tr class="info">
							<td>Authorisation</td>
							<td>Domain</td>
							<td>Enabled</td>
						</tr>

						<c:forEach items="${availableAuthorisationHandlers}" var="availableAuthorisationHandler">
							<tr title="${availableAuthorisationHandler.description}">
								<td>${availableAuthorisationHandler.name}</td>
								<td>${availableAuthorisationHandler.domain}</td>
								<td>
									<div class="switch">
										<input type="checkbox" id="enableAuthorisationHandler${availableAuthorisationHandler.name}" name="enableAuthorisationHandler${availableAuthorisationHandler.name}"
											<c:if test="${not empty enabledAuthorisationHandlers[availableAuthorisationHandler.name]}">checked="true"</c:if>>
									</div>
								</td>
							</tr>
						</c:forEach>
					</table>
				</div>
				<div class="span4">
					<p>
						<span class="label label-info">Info</span>
						The authorisation domain is used for qualify the <u>roles and operations</u> in the principal ID of the ACL. Eg. gaca/MADB.CaseHandler
					</p>
				</div>				
			</div>

		</c:otherwise>
	</c:choose>

	<div class="control-group">
        <div class="controls">
            <button type="submit" class="btn btn-primary">Save changes</button>
        </div>
    </div>

</form>