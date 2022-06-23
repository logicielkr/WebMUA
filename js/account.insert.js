$(document).ready(function() {
	$("input#insert_submit").show();
	var graha_mail_account_template_id = $("table#graha_mail_account select.graha_mail_account_template_id").val();
	if(graha_mail_account_template_id != "0") {
		hideor(true, "type", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type");
		loadFromGrahaMailAccountTemplate(graha_mail_account_template_id);
	}
	$("table#graha_mail_account select.mail_check_column").change(function() {
		if($(this).val() == "MessageID") {
			alert(_getMessage("message.70003"));
		}
	});
	$("table#graha_mail_account select.graha_mail_account_template_id").change(function() {
		if($(this).val() == "0") {
			hideor(false, "type", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type", "leave_on_server", "imap_fetch_type", "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
			callback(null);
			if($("table#graha_mail_account select.type").val() == "imap") {
				hideor(true, "leave_on_server");
				hideor(false, "imap_fetch_type");
			} else if($("table#graha_mail_account select.type").val() == "pop3") {
				hideor(false, "leave_on_server");
				hideor(true, "imap_fetch_type");
			} else if($("table#graha_mail_account select.type").val() == "store") {
				hideor(true, "type", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type", "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
				hideor(true, "leave_on_server", "imap_fetch_type");
			}
		} else {
			hideor(true, "type", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type");
			hideor(false, "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
			loadFromGrahaMailAccountTemplate($("table#graha_mail_account select.graha_mail_account_template_id").val());
		}
	});
	if($("table#graha_mail_account select.type").val() == "store") {
		hideor(true, "graha_mail_account_template_id", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type", "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
		hideor(true, "leave_on_server", "imap_fetch_type");
	}
	$("table#graha_mail_account select.type").change(function() {
		if(
			$(this).val() == "imap" ||
			$(this).val() == "pop3"
		) {
			hideor(false, "graha_mail_account_template_id", "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type", "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
		}
		if($(this).val() == "imap") {
			hideor(true, "leave_on_server");
			hideor(false, "imap_fetch_type");
		} else if($(this).val() == "pop3") {
			hideor(false, "leave_on_server");
			hideor(true, "imap_fetch_type");
		} else if($(this).val() == "store") {
			hideor(true, "host", "port", "encryption_type", "smtp_host", "smtp_port", "smtp_encryption_type", "user_name", "password", "smtp_user_name", "smtp_password", "mail_check_column", "signature");
			hideor(true, "leave_on_server", "imap_fetch_type");
		}
	});
});
function hideor() {
	if(arguments.length > 0) {
		var target = null;
		for(var i = 1; i < arguments.length; i++) {
			if(i == 1) {
				target = $("table#graha_mail_account td." + arguments[i] + " input").add("table#graha_mail_account td." + arguments[i] + " select").add("table#graha_mail_account td." + arguments[i] + " textarea");
			} else {
				target = target.add("table#graha_mail_account td." + arguments[i] + " input").add("table#graha_mail_account td." + arguments[i] + " select").add("table#graha_mail_account td." + arguments[i] + " textarea");
			}
		}
		if(target != null) {
			target.attr('disabled', arguments[0]);
		}
	}
}
function callback(graha_mail_account_template) {
	if(graha_mail_account_template != null && graha_mail_account_template.encryption_type) {
		$("table#graha_mail_account select.encryption_type").val(graha_mail_account_template.encryption_type);
	} else {
		$("table#graha_mail_account select.encryption_type").val("plain");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.host) {
		$("table#graha_mail_account input.host").val(graha_mail_account_template.host);
	} else {
		$("table#graha_mail_account input.host").val("");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.port) {
		$("table#graha_mail_account input.port").val(graha_mail_account_template.port);
	} else {
		$("table#graha_mail_account input.port").val("");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.smtp_encryption_type) {
		$("table#graha_mail_account select.smtp_encryption_type").val(graha_mail_account_template.smtp_encryption_type);
	} else {
		$("table#graha_mail_account select.smtp_encryption_type").val("plain");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.smtp_host) {
		$("table#graha_mail_account input.smtp_host").val(graha_mail_account_template.smtp_host);
	} else {
		$("table#graha_mail_account input.smtp_host").val("");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.smtp_port) {
		$("table#graha_mail_account input.smtp_port").val(graha_mail_account_template.smtp_port);
	} else {
		$("table#graha_mail_account input.smtp_port").val("");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.type) {
		$("table#graha_mail_account select.type").val(graha_mail_account_template.type);
		if(graha_mail_account_template.type == "imap") {
			hideor(true, "leave_on_server");
			hideor(false, "imap_fetch_type");
		} else if(graha_mail_account_template.type == "pop3") {
			hideor(false, "leave_on_server");
			hideor(true, "imap_fetch_type");
		} else if(graha_mail_account_template.type == "store") {
			hideor(true, "leave_on_server", "imap_fetch_type");
		}
	} else {
		$("table#graha_mail_account select.type").val("pop3");
	}
	if(graha_mail_account_template != null && graha_mail_account_template.default_charset) {
		$("table#graha_mail_account select.default_charset").val(graha_mail_account_template.default_charset);
	} else {
	}
	if(
		graha_mail_account_template != null &&
		graha_mail_account_template.imap_fetch_type &&
		$("table#graha_mail_account select.imap_fetch_type").val() == ""
	) {
		$("table#graha_mail_account select.imap_fetch_type").val(graha_mail_account_template.imap_fetch_type);
	} else {
	}
}
function loadFromGrahaMailAccountTemplate(graha_mail_account_template_id) {
	if(graha_mail_account_template_id == null) {
		return;
	}
	var url = "graha_mail_account_template.xml";
	url += "?graha_mail_account_template_id=" + graha_mail_account_template_id;
	$.ajax({
		url: url,
		processData: false,
		contentType: false,
		type: 'GET',
		success: function(result){
			var obj = parse_graha_xml_document(result);
			if(
				obj && 
				obj.rows && 
				obj.rows["graha_mail_account_template"] && 
				obj.rows["graha_mail_account_template"].length > 0 && 
				obj.rows["graha_mail_account_template"][0].type
			) {
				callback(obj.rows["graha_mail_account_template"][0]);
			}
		},
		error: function(result) {
			
		}
	});
}