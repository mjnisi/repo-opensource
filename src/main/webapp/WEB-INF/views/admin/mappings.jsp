<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="tag" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tag:navigationBar selectedPanel="mappings" selectedRepo="${selectedRepo}"/>
<div class="row-fluid">
    <div class="span6">
        <form action="${pageContext.request.contextPath}/admin/mappings/update/${selectedRepo}" method="POST">
            <table class="table table-bordered">
                <tr class="info">
                    <td>
                        Action
                    </td>
                    <c:forEach items="${allPermissions}" var="permission">

                        <td class="center-text">
                                ${permission.name}
                        </td>

                    </c:forEach>
                </tr>
                <c:forEach items="${permissionMappings.keys}" var="permissionMappingKey">
                    <tr class="input-holder">
                        <td>
                                ${permissionMappingKey}
                        </td>
                        <c:forEach items="${allPermissions}" var="permission">
                          <div>
                            <td class="center-text" id="${permissionMappingKey}">
                                <input  type="checkbox" id="${permissionMappingKey}[${permission.id}]"
                                       name="permissionMapping[]" value="${permissionMappingKey}[${permission.id}]"
                                       <c:if test="${not empty permissionMappings.permissionsByKey[permissionMappingKey][permission.id]}">checked</c:if> />
                            </td>
                          </div>
                        </c:forEach>

                    </tr>

                </c:forEach>

            </table>
            <input id="save_changes" class="btn btn-primary" type="submit" value="Save changes">
        </form>
    </div>
</div>

<script>
    function getDependencyMap() {
        var map = {};
    <c:forEach items="${permissionsWithChildren}" var="entry">
        <c:if test="${not empty entry.key.name}">
            <c:set var="key">
                '${entry.key.id}'
            </c:set>

            <c:set var="value">
                <c:forEach items="${entry.value}" var="childItem" varStatus="loop">
                    <c:if test="${not empty childItem.id}">
                    '${childItem.id}'<c:if test="${!loop.last}">, </c:if>
                    </c:if>

                </c:forEach>
            </c:set>
            map[${key}] = [${value}];
        </c:if>
    </c:forEach>
        return map;
    }

    function changeCheckboxState() {
        var elementId = $(this).attr('id');
        var elementIdSplit = elementId.split("[");

        var rgx = /\[([^)]+)\]/;
        var permissionId = elementId.match(rgx)[1];

        var map = getDependencyMap();
        var objectsToSelectSuffix = map[permissionId];
        if(objectsToSelectSuffix == undefined){
            return;
        }
        var printValue = 'all ';
        for (var i = 0; i < objectsToSelectSuffix.length; i++) {
            var elementToSelect = elementIdSplit[0].replace(".", "\\.") + "\\[" + objectsToSelectSuffix[i] + "\\]";
            if ($(this).prop('checked')) {

                $('#' + elementToSelect).prop('checked', false);
                $('#' + elementToSelect).prop('disabled', true);
            } else {
                $('#' + elementToSelect).removeProp('disabled');
            }
        }
    }
    $(document).ready(function () {

        $("input").click(function (event) {
            changeCheckboxState.call(this);
        });

        $("form").submit(function () {

            var returnValue = true;
            $(".input-holder").each(function () {
                var selection = false;
                   $(this).find('input').each(function () {
                    if ( $(this).is(':checked')) {
                            selection = true;
                        }
                });
                if(!selection){
                    alert('At least one permission per action must be selected');
                    returnValue = false;
                    return false;
                }
            });

            return returnValue;
        });

        $("input:checked").each(function () {
            changeCheckboxState.call(this);
        } );

    });
</script>