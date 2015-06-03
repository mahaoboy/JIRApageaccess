AJS.$(document).ready(
		function($) {
			JIRA.bind(JIRA.Events.ISSUE_REFRESHED, function(e, context) {
				getPageID();
			});
			getPageID();

			function getPageID() {
				if (checkElement(AJS.$("a[id='key-val']")
						.attr("data-issue-key"))) {
					var timeSpend = $.now() - getCookie("Request-Time");
					accessRecorde(AJS.$("a[id='key-val']").attr(
							"data-issue-key")
							+ "_" + timeSpend);
				}
			}
			function accessRecorde(pageid) {
				var status;
				AJS.$.ajax({
					url : "/rest/addpageaccess/1.0/message/" + pageid,
					type : 'get',
					dataType : 'json',
					async : false,
					success : function(data) {
						status = data.value;
					}
				});
				return status;
			}

			function checkElement(elem) {
				if (typeof (elem) != undefined && typeof (elem) != null
						&& typeof (elem) != 'undefined') {
					return true;
				} else {
					return false;
				}
			}

			function getCookie(cname) {
				var name = cname + "=";
				var ca = document.cookie.split(';');
				for (var i = 0; i < ca.length; i++) {
					var c = ca[i];
					while (c.charAt(0) == ' ')
						c = c.substring(1);
					if (c.indexOf(name) == 0)
						return c.substring(name.length, c.length);
				}
				return "";
			}
		});