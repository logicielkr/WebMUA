function fetch_next_message(obj) {
	var nextobj = null;
	var graha_mail_folder_id = null;
	if(obj) {
		nextobj = $(obj).parent().parent().next().find("td.fetch_message input")[0];
		graha_mail_folder_id = $(obj).parent().parent().next().find("td.graha_mail_folder_id").text();
	} else {
		nextobj = $("table#mail_folder td.fetch_message input").first()[0];
		graha_mail_folder_id = $("table#mail_folder td.graha_mail_folder_id").first().text();
	}
	if(nextobj && graha_mail_folder_id != null && graha_mail_folder_id != "") {
		fetch_message(graha_mail_folder_id, nextobj, fetch_next_message);
	}
}
function fetch_message(graha_mail_folder_id, obj, callback) {
	var formData = null;
	formData = new FormData();
	formData.append("graha_mail_folder_id", graha_mail_folder_id);
	formData.append("graha_mail_account_id", graha_mail_account_id);
	var url = "../mail/fetch.xml";
	$(obj).val("진행중");
	$.ajax({
		url: url,
		processData: false,
		contentType: false,
		type: 'POST',
		enctype: 'multipart/form-data',
		data: formData,
		success: function(result) {
			if(callback) {
				callback(obj);
			}
			var res = parse_graha_xml_document(result);
			console.log(new XMLSerializer().serializeToString(result));
			console.log(JSON.stringify(res));
			if(res.errors && res.errors.error) {
				alert(res.errors.error);
				$(obj).val("실패");
			} else {
				if(res.results && res.results.savedMailCount) {
					$(obj).val("성공(" + res.results.savedMailCount + ")");
				} else {
					$(obj).val("성공");
				}
			}
		},
		error: function(result) {
			$(obj).val("실패");
			if(callback) {
				callback(obj);
			}
		}
	});
}
$(document).ready(function() {
	var exists = false;
	$("table#mail_folder td.fetch_message").each(function() {
		if(
			$(this).parent().find("td.is_not_fetch").text() != "t" &&
			(
				(
					graha_mail_account_type == "pop3" && $(this).parent().find("td.type").text() == "Inbox"
				) ||
				(
					graha_mail_account_type == "imap"
				)
			)
		) {
			$(this).append("<input type='button' value='가져오기' onclick='fetch_message(" + $(this).parent().find("td.graha_mail_folder_id").text() + ", this)' />");
			exists = true;
		}
	});
	if(exists) {
		$("table#mail_folder td.fetch_message").add("table#mail_folder th.fetch_message").show();
		$("div.nav.top div.right").append("<input type='button' value='모두 가져오기' onclick='fetch_next_message()' />");
	}
});