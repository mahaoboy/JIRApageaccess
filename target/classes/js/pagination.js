AJS.$(document).ready(function() {
    AJS.$('#fromdatepicker').datePicker({'overrideBrowserDefault': true});
    AJS.$('#todatepicker').datePicker({'overrideBrowserDefault': true});
    
});

AJS.$(document).ready(function($) {
	
	var confluenceContextPath = AJS.params.baseURL;
	var spaceAttachmentsError = AJS.I18n.getText("PageAccess.pagination.error");
	var userNotExist = AJS.I18n.getText("PageAccess.pagination.userNotExist");
	$(".pageList").each(function() {
    	var $spaceAttachments = $(this);
    	var spaceAttachmentsData = {};

    	$("fieldset input[class='param']", $spaceAttachments).each(function() {
    		spaceAttachmentsData[this.name] = this.value;
		});
    	$spaceAttachments.data("spaceAttachmentsCacheData", spaceAttachmentsData);


    	$(".pageAccess", $spaceAttachments).live('click', function() {
    		var $spaceAttachmentsPage = $(this);
    		var $clickedPage = $spaceAttachmentsPage.attr("clickedPage");

    		spaceAttachmentsData["pageNumber"] = $clickedPage;

    		updateSpaceAttachments();
    		return false;
    	});

    	$(".spaceAttachmentsSortBy", $spaceAttachments).live('click', function() {
    		var $spaceAttachmentsSortBy = $(this);
    		var $sortBy = $spaceAttachmentsSortBy.attr("sortBy");

    		spaceAttachmentsData["sortBy"] = $sortBy;
    		spaceAttachmentsData["pageNumber"] = 1;

    		updateSpaceAttachments();
    		return false;
    	});

    	$("#removeFilterLink", $spaceAttachments).live('click', function() {

    		$("input[name='fromdatepicker']", $spaceAttachments).val("");
    		$("input[name='todatepicker']", $spaceAttachments).val("");
    		$("select[name='groupName']", $spaceAttachments).val("");
    		$("select[name='spaceName']", $spaceAttachments).val("");
    		$("input[name='userNameField']", $spaceAttachments).val("");
    		$("input[name='pageTitleField']", $spaceAttachments).val("");
    		
    		spaceAttachmentsData["fromDate"] = "";
    		spaceAttachmentsData["toDate"] = "";
    		spaceAttachmentsData["groupNameField"] = "";
    		spaceAttachmentsData["spaceNameField"] = "";
    		spaceAttachmentsData["userNameField"] = "";
    		spaceAttachmentsData["pageTitleField"] = "";
    		spaceAttachmentsData["pageNumber"] = 1;
    		spaceAttachmentsData["totalItem"] = 0;

    		updateSpaceAttachments();
    		return false;
    	});

    	$("#filter-save-button", $spaceAttachments).live('click', function() {
    		var $fromdatepicker = $("input[name='fromdatepicker']", $spaceAttachments).val();
    		var $todatepicker = $("input[name='todatepicker']", $spaceAttachments).val();
    		var $groupName = $("select[name='groupName']", $spaceAttachments).val();
    		var $spaceName = $("select[name='spaceName']", $spaceAttachments).val();
    		var $userNameField = $("input[name='userNameField']", $spaceAttachments).val();
    		var $pageTitleField = $("input[name='pageTitleField']", $spaceAttachments).val();

    		filterSpaceAttachments($fromdatepicker, $todatepicker, $groupName, $spaceName, $userNameField, $pageTitleField);
    	});

    	function getPosition(element){
            var e = document.getElementById(element);
            var left = 0;
            var top = 0;

            do{
                left += e.offsetLeft;
                top += e.offsetTop;
            }while(e = e.offsetParent);

            return [left, top];
        }

    	var filterSpaceAttachments = function(fromdatepicker, todatepicker, groupName, spaceName, userNameField, pageTitleField) {
    		if(checkElement(fromdatepicker)){
        		spaceAttachmentsData["fromDate"] = fromdatepicker;
    		}
    		if(checkElement(todatepicker)){
        		spaceAttachmentsData["toDate"] = todatepicker;
    		}
    		if(checkElement(groupName)){
        		spaceAttachmentsData["groupNameField"] = groupName;
    		}
    		if(checkElement(spaceName)){
        		spaceAttachmentsData["spaceNameField"] = encodeURIComponent(spaceName);
    		}
    		if(checkElement(userNameField)){
        		spaceAttachmentsData["userNameField"] = userNameField;
    		}
    		if(checkElement(pageTitleField)){
        		spaceAttachmentsData["pageTitleField"] = encodeURIComponent(pageTitleField);
    		}
    		spaceAttachmentsData["pageNumber"] = 1;
    		spaceAttachmentsData["totalItem"] = 0;

    		if(userNameField.length > 0){
    			checkUserExist(userNameField);
    		}else{
    			updateSpaceAttachments();
    		}
    		
    		return false;
    	};

    	var updateSpaceAttachments = function() {
    		
    		$.ajax({
    			cache: false,
    			data : $spaceAttachments.data("spaceAttachmentsCacheData"),
    			dataType : "html",
    			success : function(serverGeneratedHtml) {
	    			var $finalOutput = $(serverGeneratedHtml);
	    			if($finalOutput.find(".attachments-container").length > 0){
	    				$(".attachments-container", $spaceAttachments).replaceWith($finalOutput.find(".attachments-container"));
	    			    AJS.$('#fromdatepicker').datePicker();
	    			    AJS.$('#todatepicker').datePicker();

	    			    $(document).scrollTop( $("#SearchResult").offset().top );
	    			}else{
	    				location.reload();
	    			}
				},
				error : function(XMLHttpRequest, textStatus, errorThrown) {

					AJS.log("Error retrieving data: " + errorThrown);
					$(".attachments-container", $spaceAttachments).remove();
					$spaceAttachments.append('<div class="space-attachments-error error">'+ spaceAttachmentsError +'</div>');
				},

	            type : "GET",
	            url : confluenceContextPath + "/plugins/servlet/winagile/pageaccessstatistics"
	    	});
    	};

    	var checkUserExist = function(username){
        	var userquery = {};
        	userquery["username"] = username;
        	$.ajax({
    			cache: false,
    			data : userquery,
    			dataType : "json",
    			success : function(serverGeneratedHtml) {
        			if(!checkElement(serverGeneratedHtml.errorMessages)){
        				updateSpaceAttachments();
        				return false;
        			}else{
        				alert(serverGeneratedHtml.errorMessages);
        				return false;
        			}
    			},
    			error : function(jqXHR, textStatus, errorThrown) {
    				AJS.log("Error retrieving data: " +  $.parseJSON(jqXHR.responseText).errorMessages);
    				alert($.parseJSON(jqXHR.responseText).errorMessages);
    				return false;
    			},

                type : "GET",
                url : confluenceContextPath + "/rest/api/2/user"
        	});
        };

	});
	function checkElement(elem) {
		if (typeof (elem) != undefined && typeof (elem) != null
				&& typeof (elem) != 'undefined') {
			return true;
		} else {
			return false;
		}
	}
	
	
});

