function fetch_next_message(obj) {
	var nextobj = null;
	var graha_mail_account_id = null;
	if(obj) {
		nextobj = $(obj).parent().parent().next().find("td.fetch_message input")[0];
		graha_mail_account_id = $(obj).parent().parent().next().find("td.graha_mail_account_id").text();
	} else {
		nextobj = $("table#graha_mail_account td.fetch_message input").first()[0];
		graha_mail_account_id = $("table#graha_mail_account td.graha_mail_account_id").first().text();
	}
	if(nextobj && graha_mail_account_id != null && graha_mail_account_id != "") {
		fetch_message(graha_mail_account_id, nextobj, fetch_next_message);
	}
}
function fetch_message(graha_mail_account_id, obj, callback) {
	var formData = null;
	formData = new FormData();
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
			var res = parse_graha_xml_document(result);
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
			if(callback) {
				callback(obj);
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
	$("form.insert input").add("form.insert button").add("form.insert").show();
	var exists = false;
	$("table#graha_mail_account td.fetch_message").each(function() {
		if(
			$(this).parent().find("td.type").text() != "store" && 
			(
				$(this).parent().find("td.type").text() != "imap" ||
				(
					$(this).parent().find("td.imap_fetch_type").text() != "F" &&
					$(this).parent().find("td.imap_fetch_type").text() != "M"
				)
			)
		) {
			$(this).append("<input type='button' value='가져오기' onclick='fetch_message(" + $(this).parent().find("td.graha_mail_account_id").text() + ", this)' />");
			exists = true;
		}
	});
	if(exists) {
		$("table#graha_mail_account td.fetch_message").add("table#graha_mail_account th.fetch_message").show();
		$("div.nav.top div.right").append("<input type='button' value='모두 가져오기' onclick='fetch_next_message()' />");
	}
	var label = {
		default:"순서변경",
		apply:"순서적용",
		fail:"실패",
		processing:"진행중"
	};
	var table = {
		pk:"graha_mail_account_id",
		order_column:"order_number"
	};
	var url = "order_number.xml";
	GrahaCommonOrderNumber.appendButton(url, "graha_mail_account", table, label);
});