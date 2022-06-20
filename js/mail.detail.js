var completed = false;
$(document).ready(function() {
	var index = 0;
	var htmlloaded = false;
	var htmlContentType = false;
	$("table#graha_mail_part td.content_type").each(function() {
		if($(this).text() == "text/plain") {
			if($(this).parent().find("td.parent_content_type").text() == "multipart/alternative") {
				var path = $(this).parent().find("td.parent_path").text();
				var isHide = false;
				$("table#graha_mail_part td.parent_path").each(function() {
					if($(this).text() == path) {
						if($(this).parent().find("td.content_type").text() == "text/html") {
							isHide = true;
						}
					}
				});
				if(isHide) {
					$(this).parent().next().hide();
				}
			}
		} else if(
			$(this).text() == "text/html" ||
			$(this).text().indexOf("text/x-amp-html") == 0
		) {
			var iframeInnerHTML = $(this).parent().next().text();
			$(this).parent().next().hide();
			if(!htmlloaded) {
				htmlContentType = true;
				$("<tr class='htmltypemail'><td><iframe scrolling='no' style='width:100%;height:500px;border:none;' class='htmltypemail_" + index + "'></iframe></td></tr>").insertAfter($(this).parent());
				if($("iframe.htmltypemail_" + index + "").length > 0) {
					var iframe = $("iframe.htmltypemail_" + index + "")[0];
					var win = iframe.contentWindow;
					var doc = iframe.contentDocument;
					if(!doc) {
						doc = win.document;
					}
					doc.open();
					doc.write(iframeInnerHTML);
					doc.close();
					var idx = index;
					$(win).on("load", function() {
						$("iframe.htmltypemail_" + idx + "").height($(doc).outerHeight(true));
						completed = true;
						$("form.re input").add("form.re button").add("form.fwd input").add("form.fwd button").show();
					});
				}
			}
			htmlloaded = true;
			index++;
		}
	});
	$("form.re").add("form.fwd").submit(function() {
		try {
			if(window.sessionStorage) {
				var graha_mail_rid = $(this).find("input.graha_mail_rid").val();
				var graha_mail_fid = $(this).find("input.graha_mail_fid").val();
				var graha_mail_id = -1;
				if(graha_mail_rid != null && graha_mail_rid != "") {
					graha_mail_id = graha_mail_rid;
				} else {
					graha_mail_id = graha_mail_fid;
				}
				var exists = false;
				$("iframe").each(function() {
					if($(this).attr("class").indexOf("htmltypemail_") == 0) {
						var win = $(this)[0].contentWindow;
						var doc = $(this)[0].contentDocument;
						if(!doc) {
							doc = win.document;
						}
						if(doc.documentElement.innerText) {
							window.sessionStorage.setItem("message_" + graha_mail_id, doc.documentElement.innerText);
						} else {
							window.sessionStorage.setItem("message_" + graha_mail_id, $(doc.documentElement).text());
						}
						exists = true;
						return false;
					}
				});
				if(exists) {
					return true;
				} else {
					$("table#graha_mail_part td.graha.contents").each(function() {
						if($(this).is(":visible")) {
							window.sessionStorage.setItem("message_" + graha_mail_id, $(this).text());
							exists = true;
							return false;
						}
					});
					return true;
				}
			} else {
				if(confirm("오래된 웹브라우저에서는 일부 기능에 제약이 있을 수 있습니다.  계속 진행하시겠습니까?")) {
					return true;
				}
			}
			return false;
		} catch(error) {
			return false;
		}
	});
	if(!htmlContentType) {
		$("form.re input").add("form.re button").add("form.fwd input").add("form.fwd button").show();
	}
});

function _check(obj) {
	if(obj) {
		if($(obj).attr("name") != null && $(obj).attr("name") == "send") {
			if($("table#graha_mail_address tbody tr").length == 0) {
				alert(_getMessage("message.40012"));
				return false;
			}
			var existsTo = false;
			$("table#graha_mail_address tbody tr").each(function() {
				var type = $(this).find("td.type").text();
				if(type != null && type == "받는사람") {
					existsTo = true;
				}
				var email_address = $(this).find("td.email_address").text();
				if(email_address == null || email_address == "") {
					alert(_getMessage("message.40014"));
					return false;
				} else if(
					email_address.indexOf(" ") < 0 &&
					email_address.indexOf("@") > 0 && 
					email_address.lastIndexOf(".") > 0 && 
					email_address.lastIndexOf(".") > email_address.indexOf("@")
				) {
				} else {
					alert(_getMessage("message.40015") + "(" + email_address + ")");
					return false;
				}
			});
			if(!existsTo) {
				alert(_getMessage("message.40013"));
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
}
