$(window).on("load", function() {
	if(!$("table#graha_mail_address th.type").is(':visible')) {
		$("table#graha_mail_address td.personal_name input")
		.add("table#graha_mail_address td.email_address input")
		.add("table#graha_mail td.subject input")
		.each(function() {
			if($(this).val() == "") {
				if($(this).parent().hasClass("personal_name")) {
					$(this).val("이름을 입력하세요.");
				} else if($(this).parent().hasClass("email_address")) {
					$(this).val("이메일을 입력하세요.");
				} else if($(this).parent().hasClass("subject")) {
					$(this).val("제목을 입력하세요.");
				}
				$(this).addClass("label");
			}
			$(this).focus(function() {
				if($(this).hasClass("label")) {
					$(this).val("");
					$(this).removeClass("label");
				}
			});
			$(this).blur(function() {
				if($(this).val() == "") {
					if($(this).parent().hasClass("personal_name")) {
						$(this).val("이름을 입력하세요.");
					} else if($(this).parent().hasClass("email_address")) {
						$(this).val("이메일을 입력하세요.");
					} else if($(this).parent().hasClass("subject")) {
						$(this).val("제목을 입력하세요.");
					}
					$(this).addClass("label");
				}
			});
		});
	}
});

$(document).ready(function() {
	var params = new URLSearchParams(document.location.search.substring(1));
	var graha_mail_rid = params.get("graha_mail_rid");
	var graha_mail_fid = params.get("graha_mail_fid");
	if(
		(graha_mail_rid != null && graha_mail_rid != "") ||
		(graha_mail_fid != null && graha_mail_fid != "")
	) {
		if(
			$("table#graha_mail input.subject").val().indexOf("Re: ") == 0 ||
			$("table#graha_mail input.subject").val().indexOf("Fwd: ") == 0
		) {
			return;
		}
		var url = "detail.xml";
		if(graha_mail_rid != null && graha_mail_rid != "") {
			url += "?graha_mail_id=" + graha_mail_rid
		} else {
			url += "?graha_mail_id=" + graha_mail_fid
		}
		if(params.get("graha_mail_folder_id") != null && params.get("graha_mail_folder_id") != "") {
			url += "&graha_mail_folder_id=" + params.get("graha_mail_folder_id");
		}
		if(params.get("graha_mail_account_id") != null && params.get("graha_mail_account_id") != "") {
			url += "&graha_mail_account_id=" + params.get("graha_mail_account_id");
		}
		$.ajax({
			url: url,
			processData: false,
			contentType: false,
			type: 'GET',
			success: function(result) {
				var obj = parse_graha_xml_document(result);
				if(
					obj && obj.rows && 
					obj.rows["graha_mail"] && obj.rows["graha_mail"].length > 0 &&
					obj.rows["graha_mail_address"] && obj.rows["graha_mail_address"].length > 0 &&
					obj.rows["graha_mail_part"] && obj.rows["graha_mail_part"].length > 0
				) {
					var replyTo = null;
					var from = null;
					var to = null;
					var cc = new Array();
					for(var i = 0; i < obj.rows["graha_mail_address"].length; i++) {
						if(obj.rows["graha_mail_address"][i].type == "회신메일") {
							replyTo = {
								name:obj.rows["graha_mail_address"][i].personal_name,
								address:obj.rows["graha_mail_address"][i].email_address
							};
						}
						if(obj.rows["graha_mail_address"][i].type == "받는사람") {
							to = {
								name:obj.rows["graha_mail_address"][i].personal_name,
								address:obj.rows["graha_mail_address"][i].email_address
							};
						}
						if(obj.rows["graha_mail_address"][i].type == "보낸사람") {
							from = {
								name:obj.rows["graha_mail_address"][i].personal_name,
								address:obj.rows["graha_mail_address"][i].email_address
							};
						}
						if(obj.rows["graha_mail_address"][i].type == "참조") {
							cc.push({
								name:obj.rows["graha_mail_address"][i].personal_name,
								address:obj.rows["graha_mail_address"][i].email_address
							});
						}
					}
					if(cc.length + 1 > $("table#graha_mail_address input.personal_name").length) {
						var index = $("table#graha_mail_address input.personal_name").length;
						while(cc.length + 1 > $("table#graha_mail_address input.personal_name").length) {
							var last = $("table#graha_mail_address tbody tr:last-child").clone();
							last.find("input").add(last.find("select")).each(function() {
								var name = $(this).attr("name");
								$(this).attr("name", name.substring(0, name.lastIndexOf(".")) + "." + (index + 1));
							});
							last.appendTo($("table#graha_mail_address tbody"));
							index++;
						}
					}
					var name = null;
					var address = null;
					if(replyTo != null && replyTo.name) {
						name = replyTo.name;
					}
					if((replyTo == null || !(replyTo.name)) && from != null && from.name) {
						name = from.name;
					}
					if(replyTo != null && replyTo.address) {
						address = replyTo.address;
					}
					if((replyTo == null || !(replyTo.address)) && from != null && from.address) {
						address = from.address;
					}
					if(graha_mail_rid != null && graha_mail_rid != "") {
						$("table#graha_mail_address input[name='personal_name.1']").val(name);
						$("table#graha_mail_address input[name='email_address.1']").val(address);
						for(var i = 0; i < cc.length; i++) {
							$("table#graha_mail_address select[name='type." + (i + 2) + "']").val("CC");
							if(cc[i].name) {
								$("table#graha_mail_address input[name='personal_name." + (i + 2) + "']").val(cc[i].name);
							}
							if(cc[i].address) {
								$("table#graha_mail_address input[name='email_address." + (i + 2) + "']").val(cc[i].address);
							}
						}
					}
					if(graha_mail_rid != null && graha_mail_rid != "") {
						$("table#graha_mail input.subject").val("Re: " + obj.rows["graha_mail"][0].subject);
					} else {
						$("table#graha_mail input.subject").val("Fwd: " + obj.rows["graha_mail"][0].subject);
					}
					if(window.sessionStorage) {
						var contents = null;
						if(graha_mail_rid != null && graha_mail_rid != "") {
							contents = window.sessionStorage.getItem("message_" + graha_mail_rid);
						} else {
							contents = window.sessionStorage.getItem("message_" + graha_mail_fid);
						}
						if(contents != null && contents != "") {
							var tmp = contents.split(/\n\r|\r\n|\n|\r/);
							contents = "\n";
							if(graha_mail_fid != null && graha_mail_fid != "") {
								contents += "-------- Forwarded Message --------\n";
								contents += "Subject: 	" + obj.rows["graha_mail"][0].subject + "\n";
							}
							if(
								obj.rows["graha_mail"][0].sent_date != null && 
								obj.rows["graha_mail"][0].sent_date != "" &&
								obj.rows["graha_mail"][0].sent_date.lastIndexOf(":") > 0
							) {
								if(graha_mail_fid != null && graha_mail_fid != "") {
									contents += "Date: 	" + obj.rows["graha_mail"][0].sent_date_fwd  + "\n";
								} else {
									contents += "On " + obj.rows["graha_mail"][0].sent_date_re  + ", ";
								}
							}
							if(graha_mail_fid != null && graha_mail_fid != "") {
								contents += "From: 	" + getEmailAddress(from) + "\n";
								contents += "To: 	" + getEmailAddress(to) + "\n";
								contents += "\n";
							} else {
								contents += getEmailAddress(name, address);
								contents += " wrote:\n\n";
							}
							for(var i = 0; i < tmp.length; i++) {
								if(graha_mail_fid != null && graha_mail_fid != "") {
									contents += tmp[i] + "\n";
								} else {
									contents += "> " + tmp[i] + "\n";
								}
							}
							contents += "\n";
							contents += $("table#graha_mail_part textarea.contents").val();
							$("table#graha_mail_part textarea.contents").val(contents);
						}
					}
				} else {
					alert("원본 메일을 불러오는 과정에서 에러가 발생했습니다.");
				}
			},
			error: function(result) {
				alert("원본 메일을 불러오는 과정에서 에러가 발생했습니다.");
			}
		});
	}
});
function getEmailAddress() {
	var email = null;
	if(arguments.length == 1) {
		if(arguments[0] != null) {
			if(arguments[0].name && arguments[0].name != null && arguments[0].name != "") {
				email = arguments[0].name;
			}
			if(email == null && arguments[0].address && arguments[0].address != null && arguments[0].address != "") {
				email = arguments[0].address;
			} else if(email != null && arguments[0].address && arguments[0].address != null && arguments[0].address != "") {
				email += " <" + arguments[0].address + ">";
			}
		}
		return email;
	} else if(arguments.length == 2) {
		if(arguments[0] && arguments[0] != null && arguments[0] != "") {
			email = arguments[0];
		}
		if(email == null && arguments[1] && arguments[1] != null && arguments[1] != "") {
			email = arguments[1];
		} else if(email != null && arguments[1] && arguments[1] != null && arguments[1] != "") {
			email += " <" + arguments[1] + ">";
		}
		return email;
	} else {
		throw new Error("argument length " + arguments.length);
	}
}
