<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>REPO</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css"/>
<script type="text/javascript" src="js/jquery-1.10.2.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>
</head>
<body>


<div class="container-fluid">
    <div class="row-fluid">
        <div class="span2">
        </div>

        <div class="span10">
            <div class="page-header">
                <h1>REPO</h1>
				<h2>Trade Document Repository</h2>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span2">
            <tiles:insertAttribute name="layout-sidebar"/>
        </div>
        <div class="span10">


<ul class="nav nav-tabs" id="myTab">
<li class="active"><a href="#cmis10" data-toggle="tab">CMIS 1.0 bindings</a></li>
<li><a href="#cmis11" data-toggle="tab">CMIS 1.1 bindings</a></li>
<li><a href="#client10" data-toggle="tab">CMIS 1.0 client configurations</a></li>
<li><a href="#client11" data-toggle="tab">CMIS 1.1 client configurations</a></li>
<li><a href="#other" data-toggle="tab">Other</a></li>
</ul>

<div class="tab-content">
<div class="tab-pane active" id="cmis10">

		<h4>Web Services binding (combined)</h4>
			<div><a href="services/cmis?wsdl">${pageContext.request.requestURL}services/cmis?wsdl</a></div>

		<h4>Web Services binding (divided)</h4>

			<div><a href="services/RepositoryService?wsdl">${pageContext.request.requestURL}services/RepositoryService?wsdl</a></div>
			<div><a href="services/NavigationService?wsdl">${pageContext.request.requestURL}services/NavigationService?wsdl</a></div>
			<div><a href="services/ObjectService?wsdl">${pageContext.request.requestURL}services/ObjectService?wsdl</a></div>
			<div><a href="services/VersioningService?wsdl">${pageContext.request.requestURL}services/VersioningService?wsdl</a></div>
			<div><a href="services/RelationshipService?wsdl">${pageContext.request.requestURL}services/RelationshipService?wsdl</a></div>
			<div><a href="services/DiscoveryService?wsdl">${pageContext.request.requestURL}services/DiscoveryService?wsdl</a></div>
			<div><a href="services/MultiFilingService?wsdl">${pageContext.request.requestURL}services/MultiFilingService?wsdl</a></div>
			<div><a href="services/ACLService?wsdl">${pageContext.request.requestURL}services/ACLService?wsdl</a></div>
			<div><a href="services/PolicyService?wsdl">${pageContext.request.requestURL}services/PolicyService?wsdl</a></div>

		<h4>AtomPub binding</h4>
			<div><a href="atom">${pageContext.request.requestURL}atom</a></div>




</div>
<div class="tab-pane" id="cmis11">


		<h4>Web Services binding (combined)</h4>
			<div><a href="services/cmis?wsdl">${pageContext.request.requestURL}services11/cmis?wsdl</a></div>

		<h4>Web Services binding (divided)</h4>

			<div><a href="services11/RepositoryService?wsdl">${pageContext.request.requestURL}services11/RepositoryService?wsdl</a></div>
			<div><a href="services11/NavigationService?wsdl">${pageContext.request.requestURL}services11/NavigationService?wsdl</a></div>
			<div><a href="services11/ObjectService?wsdl">${pageContext.request.requestURL}services11/ObjectService?wsdl</a></div>
			<div><a href="services11/VersioningService?wsdl">${pageContext.request.requestURL}services11/VersioningService?wsdl</a></div>
			<div><a href="services11/RelationshipService?wsdl">${pageContext.request.requestURL}services11/RelationshipService?wsdl</a></div>
			<div><a href="services11/DiscoveryService?wsdl">${pageContext.request.requestURL}services11/DiscoveryService?wsdl</a></div>
			<div><a href="services11/MultiFilingService?wsdl">${pageContext.request.requestURL}services11/MultiFilingService?wsdl</a></div>
			<div><a href="services11/ACLService?wsdl">${pageContext.request.requestURL}services11/ACLService?wsdl</a></div>
			<div><a href="services11/PolicyService?wsdl">${pageContext.request.requestURL}services11/PolicyService?wsdl</a></div>

		<h4>AtomPub binding</h4>
			<div><a href="atom11">${pageContext.request.requestURL}atom11</a></div>

		<h4>Browser binding</h4>
			<div><a href="browser">${pageContext.request.requestURL}browser</a></div>


</div>
<div class="tab-pane" id="client10">

		<h4>Apache Workbench configuration</h4>

		<div><pre>org.apache.chemistry.opencmis.binding.spi.type=webservices
org.apache.chemistry.opencmis.binding.webservices.RepositoryService=${pageContext.request.requestURL}services/RepositoryService?wsdl
org.apache.chemistry.opencmis.binding.webservices.NavigationService=${pageContext.request.requestURL}services/NavigationService?wsdl
org.apache.chemistry.opencmis.binding.webservices.ObjectService=${pageContext.request.requestURL}services/ObjectService?wsdl
org.apache.chemistry.opencmis.binding.webservices.VersioningService=${pageContext.request.requestURL}services/VersioningService?wsdl
org.apache.chemistry.opencmis.binding.webservices.DiscoveryService=${pageContext.request.requestURL}services/DiscoveryService?wsdl
org.apache.chemistry.opencmis.binding.webservices.MultiFilingService=${pageContext.request.requestURL}services/MultiFilingService?wsdl
org.apache.chemistry.opencmis.binding.webservices.RelationshipService=${pageContext.request.requestURL}services/RelationshipService?wsdl
org.apache.chemistry.opencmis.binding.webservices.ACLService=${pageContext.request.requestURL}services/ACLService?wsdl
org.apache.chemistry.opencmis.binding.webservices.PolicyService=${pageContext.request.requestURL}services/PolicyService?wsdl
org.apache.chemistry.opencmis.binding.compression=true
org.apache.chemistry.opencmis.user=admin
org.apache.chemistry.opencmis.password=admin</pre></div>


	<h4>Repo client configuration</h4>
		<div><pre>&lt;configuration&gt;
 &lt;session&gt;
  &lt;name&gt;demo2&lt;/name&gt;
  &lt;binding&gt;soap&lt;/binding&gt;
  &lt;url&gt;
   &lt;acl&gt;${pageContext.request.requestURL}services/ACLService?wsdl&lt;/acl&gt;
   &lt;discovery&gt;${pageContext.request.requestURL}services/DiscoveryService?wsdl&lt;/discovery&gt;
   &lt;multifiling&gt;${pageContext.request.requestURL}services/MultiFilingService?wsdl&lt;/multifiling&gt;
   &lt;navigation&gt;${pageContext.request.requestURL}services/NavigationService?wsdl&lt;/navigation&gt;
   &lt;object&gt;${pageContext.request.requestURL}services/ObjectService?wsdl&lt;/object&gt;
   &lt;policy&gt;${pageContext.request.requestURL}services/PolicyService?wsdl&lt;/policy&gt;
   &lt;relationship&gt;${pageContext.request.requestURL}services/RelationshipService?wsdl&lt;/relationship&gt;
   &lt;repository&gt;${pageContext.request.requestURL}services/RepositoryService?wsdl&lt;/repository&gt;
   &lt;versioning&gt;${pageContext.request.requestURL}services/VersioningService?wsdl&lt;/versioning&gt;
  &lt;/url&gt;
  &lt;repositoryId&gt;demo&lt;/repositoryId&gt;
  &lt;credentialsResolver&gt;eu.trade.cmis.service.EcasCredentialsResolver&lt;/credentialsResolver&gt;
  &lt;desktopIntegration&gt;
   &lt;programSource&gt;http://tomcat7dev.trade.cec.eu.int:8080/cmisdir/&lt;/programSource&gt;
   &lt;repoURL&gt;http://serverdev.trade.cec.eu.int:8080/repo&lt;/repoURL&gt;
  &lt;/desktopIntegration&gt;
  &lt;service&gt;eu.trade.cmis.service.ServiceProvider&lt;/service&gt;
  &lt;errorHandler&gt;eu.trade.cmis.service.TradeErrorHandler&lt;/errorHandler&gt;
  &lt;configurationClass&gt;eu.trade.cmis.config.TradeConfig&lt;/configurationClass&gt;

  &lt;!--
  &lt;principalResolver&gt;eu.trade.cmis.service.principal.EcasUserPrincipalHandler&lt;/principalResolver&gt;
  &lt;principalResolver&gt;eu.trade.service.principal.LdapUsersPrincipalResolver&lt;/principalResolver&gt;
  --&gt;

  &lt;sendHandler&gt;eu.trade.cmis.service.send.MailLinksSender&lt;/sendHandler&gt;
  &lt;sendHandler&gt;eu.trade.cmis.service.send.MailAttachmentsSender&lt;/sendHandler&gt;
  &lt;externalSystemConfiguration&gt;han_cmisproxytst&lt;/externalSystemConfiguration&gt;
  &lt;externalSystemDocumentType&gt;han:item&lt;/externalSystemDocumentType&gt;
 &lt;/session&gt;
&lt;/configuration&gt;</pre></div>

</div>
<div class="tab-pane" id="client11">

<h4>Apache Workbench configuration</h4>

		<div><pre>org.apache.chemistry.opencmis.binding.spi.type=webservices
org.apache.chemistry.opencmis.binding.webservices.RepositoryService=${pageContext.request.requestURL}services11/RepositoryService?wsdl
org.apache.chemistry.opencmis.binding.webservices.NavigationService=${pageContext.request.requestURL}services11/NavigationService?wsdl
org.apache.chemistry.opencmis.binding.webservices.ObjectService=${pageContext.request.requestURL}services11/ObjectService?wsdl
org.apache.chemistry.opencmis.binding.webservices.VersioningService=${pageContext.request.requestURL}services11/VersioningService?wsdl
org.apache.chemistry.opencmis.binding.webservices.DiscoveryService=${pageContext.request.requestURL}services11/DiscoveryService?wsdl
org.apache.chemistry.opencmis.binding.webservices.MultiFilingService=${pageContext.request.requestURL}services11/MultiFilingService?wsdl
org.apache.chemistry.opencmis.binding.webservices.RelationshipService=${pageContext.request.requestURL}services11/RelationshipService?wsdl
org.apache.chemistry.opencmis.binding.webservices.ACLService=${pageContext.request.requestURL}services11/ACLService?wsdl
org.apache.chemistry.opencmis.binding.webservices.PolicyService=${pageContext.request.requestURL}services11/PolicyService?wsdl
org.apache.chemistry.opencmis.binding.compression=true
org.apache.chemistry.opencmis.user=admin
org.apache.chemistry.opencmis.password=******</pre></div>


	<h4>Repo client configuration</h4>
		<div><pre>&lt;configuration&gt;
 &lt;session&gt;
  &lt;name&gt;demo2&lt;/name&gt;
  &lt;binding&gt;soap&lt;/binding&gt;
  &lt;url&gt;
   &lt;acl&gt;${pageContext.request.requestURL}services11/ACLService?wsdl&lt;/acl&gt;
   &lt;discovery&gt;${pageContext.request.requestURL}services11/DiscoveryService?wsdl&lt;/discovery&gt;
   &lt;multifiling&gt;${pageContext.request.requestURL}services11/MultiFilingService?wsdl&lt;/multifiling&gt;
   &lt;navigation&gt;${pageContext.request.requestURL}services11/NavigationService?wsdl&lt;/navigation&gt;
   &lt;object&gt;${pageContext.request.requestURL}services11/ObjectService?wsdl&lt;/object&gt;
   &lt;policy&gt;${pageContext.request.requestURL}services11/PolicyService?wsdl&lt;/policy&gt;
   &lt;relationship&gt;${pageContext.request.requestURL}services11/RelationshipService?wsdl&lt;/relationship&gt;
   &lt;repository&gt;${pageContext.request.requestURL}services11/RepositoryService?wsdl&lt;/repository&gt;
   &lt;versioning&gt;${pageContext.request.requestURL}services11/VersioningService?wsdl&lt;/versioning&gt;
  &lt;/url&gt;
  &lt;repositoryId&gt;demo&lt;/repositoryId&gt;
  &lt;credentialsResolver&gt;eu.trade.cmis.service.EcasCredentialsResolver&lt;/credentialsResolver&gt;
  &lt;desktopIntegration&gt;
   &lt;programSource&gt;http://tomcat7dev.trade.cec.eu.int:8080/cmisdir/&lt;/programSource&gt;
   &lt;repoURL&gt;http://serverdev.trade.cec.eu.int:8080/repo&lt;/repoURL&gt;
  &lt;/desktopIntegration&gt;
  &lt;service&gt;eu.trade.cmis.service.ServiceProvider&lt;/service&gt;
  &lt;errorHandler&gt;eu.trade.cmis.service.TradeErrorHandler&lt;/errorHandler&gt;
  &lt;configurationClass&gt;eu.trade.cmis.config.TradeConfig&lt;/configurationClass&gt;

  &lt;!--
  &lt;principalResolver&gt;eu.trade.cmis.service.principal.EcasUserPrincipalHandler&lt;/principalResolver&gt;
  &lt;principalResolver&gt;eu.trade.service.principal.LdapUsersPrincipalResolver&lt;/principalResolver&gt;
  --&gt;

  &lt;sendHandler&gt;eu.trade.cmis.service.send.MailLinksSender&lt;/sendHandler&gt;
  &lt;sendHandler&gt;eu.trade.cmis.service.send.MailAttachmentsSender&lt;/sendHandler&gt;
  &lt;externalSystemConfiguration&gt;han_cmisproxytst&lt;/externalSystemConfiguration&gt;
  &lt;externalSystemDocumentType&gt;han:item&lt;/externalSystemDocumentType&gt;
 &lt;/session&gt;
&lt;/configuration&gt;</pre></div>

</div>
<div class="tab-pane" id="other">

		<h4>Administration panel</h4>
		<div><a href="admin">${pageContext.request.requestURL}admin</a></div>

		<h4>Web interface</h4>
		<div><a href="web">${pageContext.request.requestURL}web</a></div>
</div>
</div>




        </div>
    </div>
</div>




</body>
</html>