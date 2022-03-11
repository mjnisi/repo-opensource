<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form id="newTicketForm" class="form-horizontal" action="${pageContext.request.contextPath}/ticket/createTicket">
    <div class="control-group">
        <label class="control-label" for="targetService">Target Service:</label>

        <div class="controls">
            <input class="input-xxlarge" type="text" id="targetService" name="targetService" placeholder="Target Service" />
        </div>
    </div>
    <div class="control-group">
        <div class="controls">
            <button type="submit" class="btn btn-primary">Create ticket</button>
        </div>
    </div>
</form>