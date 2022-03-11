<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <style type="text/css">
        table {
            font-size: 12px;
            color: #333333;
            border-width: 1px;
            border-color: #729ea5;
            border-collapse: collapse;
            margin-bottom: 10px
        }

        table th {
            font-size: 12px;
            background-color: #acc8cc;
            border-width: 1px;
            padding: 8px;
            border-style: solid;
            border-color: #729ea5;
            text-align: left;
        }

        table tr {
            background-color: #d4e3e5;
        }

        table td {
            font-size: 12px;
            border-width: 1px;
            padding: 8px;
            border-style: solid;
            border-color: #729ea5;
        }

    </style>


</head>
<body>
<h2>CMIS Repo server status ${date}</h2>

<h3>System information</h3>
<table border="1">
    <tr>
        <th>Maximum memory</th>
        <td>${maxMemory}</td>
    </tr>
    <tr>
        <th>Allocated memory</th>
        <td>${allocatedMemory}</td>
    </tr>
    <tr>
        <th>Free memory</th>
        <td>${freeMemory}</td>
    </tr>
    <tr>
        <th>Process CPU load</th>
        <td>${processCpuLoad}</td>
    </tr>
    <tr>
        <th>System CPU load</th>
        <td>${systemCPULoad}</td>
    </tr>
</table>
<pre>
Maximum memory: the maximum amount of memory that the Java Virtual Machine will attempt to use.
Allocated memory: the total amount of memory in the Java Virtual Machine.
Free memory: the amount of free memory in the Java Virtual Machine.
</pre>

<h3>Database information</h3>
<table border="1">
    <tr>
        <th>DB Url</th>
        <td>${dbURL}</td>
    </tr>
    <tr>
        <th>DB User Name</th>
        <td>${dbUserName}</td>
    </tr>
    <tr>
        <th>Is DB connection valid</th>
        <td>${dbConnectionValid}</td>
    </tr>
    <tr>
        <th>DB Driver Name</th>
        <td>${dbDriverName}</td>
    </tr>
    <tr>
        <th>DB Driver Version</th>
        <td>${dbDriverVersion}</td>
    </tr>
    <tr>
        <th>DB Transaction Isolation Level</th>
        <td>${dbTransactionIsolationLevel}</td>
    </tr>
</table>

<c:if test="${not empty dbExceptionStackTrace}">
<h3>Database exception:</h3>
    <pre><strong>${dbExceptionMessage}</strong></pre>

    <code>
        <c:forEach items="${dbExceptionStackTrace}" var="lineToPrint">
            ${lineToPrint}
        </c:forEach>
    </code>
</c:if>

<h3>Repository information</h3>
    <table>
        <tr>
            <th>Repository</th>
            <th>Object type</th>
            <th>Number of objects</th>
        </tr>
        <c:forEach items="${repoObjects}" var="repoSummary">
            <tr>
                <td>${repoSummary[0]}</td>
                <td>${repoSummary[1]}</td>
                <td>${repoSummary[2]}</td>
            </tr>
        </c:forEach>
    </table>

</body>
</html>