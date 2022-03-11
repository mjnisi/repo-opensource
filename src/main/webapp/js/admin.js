function setAclPropagationGroupVisibility(){

	if ($("#acl").val() == 'none') {
		$("#aclPropagationGroup").hide();
	} else {
		$("#aclPropagationGroup").show(100);
	}
}

jQuery(document).ready(function ($) {
	setAclPropagationGroupVisibility();
	$("#acl").click(function() {setAclPropagationGroupVisibility()});
});

function getColorByIndexState(indexStateId){
	var color = "#F9F9F9";
	switch(indexStateId)
	{
	case 0: //NONE
		color = "#339BB9";//blue
		break;
	case 1://INDEXED
		color = "#57A957";//strong green
		break;
	case 2://ERROR
		color = "#C43C35";//red
		break;
	case 3://NOT_INDEXABLE
		color = "#F89406";//yellow
		break;
	case 4://PARTIALLY_INDEXED
		color = "#9acb9a";//light green
		break;
	}
	return color;
}
