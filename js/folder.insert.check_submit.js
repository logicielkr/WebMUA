function check_submit(form, msg) {
	if(typeof(_check) == "function" && !_check(form)) {
		return false;
	}
	var url = "exists.xml?name=" + $("form#insert input[name='name']").val();
	url += "&graha_mail_account_id=" + $("form#insert input[name='graha_mail_account_id']").val();
	if($("form#insert input[name='graha_mail_folder_id']").val() != "") {
		url += "&graha_mail_folder_id=" + $("form#insert input[name='graha_mail_folder_id']").val();
	}
	$.ajax({
		url: url,
		type: "GET",
		async: true,
		success: function(result) {
			var obj = parse_graha_xml_document(result);
			if(obj["rows"] && obj["rows"]["graha_mail_folder"] && obj["rows"]["graha_mail_folder"].length > 0) {
				alert(_getMessage("message.80002"));
				form.name.focus();
			} else {
				if(confirm(_getMessage(msg))) {
					form.submit();
				}
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			if(jqXHR.status = 404) {
				if(confirm(_getMessage(msg))) {
					form.submit();
				}
			}
		}
	});
	return false;
}