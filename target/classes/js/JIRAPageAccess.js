AJS.$(document).ready(function($) {
	$('#userNameField').autocomplete({
		serviceUrl : '/rest/addpageaccess/1.0/infomessage/user',
		onHint : function(hint) {
			$('#autocomplete-ajax-x').val(hint);
		}
	});
	
	$('#pageTitleField').autocomplete({
		serviceUrl : '/rest/addpageaccess/1.0/infomessage/page',
		onHint : function(hint) {
			$('#autocomplete-ajax-y').val(hint);
		}
	});

});