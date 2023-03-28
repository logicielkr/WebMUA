function check_submit(form, msg) {
	if(typeof(_check) == "function" && !_check(form)) {
		return false;
	}
	if(confirm(_getMessage(msg))) {
		if(!$("table#graha_mail_address th.type").is(':visible')) {
			$("table#graha_mail_address td.personal_name input")
			.add("table#graha_mail_address td.email_address input")
			.add("table#graha_mail td.subject input")
			.each(function() {
				if($(this).hasClass("label")) {
					$(this).val("");
				}
			});
		}
		return true;
	} else {
		return false;
	}
}
