<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
    <tiles:insertAttribute name="layout-head"/>
</head>
<body style="padding-left: 40px">

<div class="container-fluid">
    <div class="row-fluid">
        <div class="span2">
        </div>

        <div class="span10">
            <div class="page-header">
                <h3>${pageTitle}</h3>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span2">
            <tiles:insertAttribute name="layout-sidebar"/>
        </div>
        <div class="span10">
            <tiles:insertAttribute name="layout-content"/>
        </div>
    </div>
</div>
</body>
</html>