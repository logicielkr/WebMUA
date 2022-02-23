var currentGrahaMailAccountId = null;
$(document).ready(function() {
	$("table#graha_mail td.sender_name").add("table#graha_mail td.rcpt_name").add("table#graha_mail td.subject").add("table#graha_mail td.sent_date").add("table#graha_mail td.received_date").bind("mouseenter", function(){
		var $this = $(this);
		if(this.offsetWidth < this.scrollWidth && !$this.attr("title")){
			$this.attr("title", $this.text());
		}
	});
	$("form.account input[type='submit']").hide();
	var params = new URLSearchParams(document.location.search.substring(1));
	if(params.get("graha_mail_account_id") == null) {
		currentGrahaMailAccountId = $("form.search input.graha_mail_account_id").val();
	} else {
		currentGrahaMailAccountId = params.get("graha_mail_account_id");
	}
	$("form.account select.graha_mail_account_id").change(function() {
		if(currentGrahaMailAccountId == $(this).val()) {
			$("table#graha_mail tbody").add("ul.pages").show();
		} else {
			$("table#graha_mail tbody").add("ul.pages").hide();
		}
		$("form.fetch input.graha_mail_account_id").val($(this).val());
		$("form.folder_list input.graha_mail_account_id").val($(this).val());
		$("form.search input.graha_mail_account_id").val($(this).val());
		$("form.write input.graha_mail_account_id").val($(this).val());
		var url = "folder.xml?graha_mail_account_id=" + $(this).val();
		var lastFolderID = $("form.search select.graha_mail_folder_id").val();
		$.ajax({
			url: url,
			processData: false,
			contentType: false,
			type: 'GET',
			success: function(result) {
				var obj = parse_graha_xml_document(result);
				if(
					obj && obj.props && 
					(obj.props["mail_account.type"] == "imap" || obj.props["mail_account.type"] == "pop3")
				) {
					$("form.fetch input").show();
					$("form.fetch button").show();
					$("form.write input").show();
					$("form.write button").show();
				} else {
					$("form.fetch input").hide();
					$("form.fetch button").hide();
					$("form.write input").hide();
					$("form.write button").hide();
				}
				if(obj && obj.rows && obj.rows["graha_mail_folder"] && obj.rows["graha_mail_folder"].length > 0) {
					var existsLastFolderID = false;
					$("form.search select.graha_mail_folder_id").empty();
					for(var i = 0; i < obj.rows["graha_mail_folder"].length; i++) {
						$("form.search select.graha_mail_folder_id").append("<option value='" + obj.rows["graha_mail_folder"][i].graha_mail_folder_id + "'>" + obj.rows["graha_mail_folder"][i].name + "</option>");
						if(obj.rows["graha_mail_folder"][i].graha_mail_folder_id == lastFolderID) {
							existsLastFolderID = true;
						}
					}
					if(existsLastFolderID) {
						$("form.search select.graha_mail_folder_id").val(lastFolderID);
					}
					$("form.fetch input.graha_mail_folder_id").val($("form.search select.graha_mail_folder_id").val());
					$("form.search select").show();
					$("form.search input").show();
					$("form.search button").show();
				} else {
					$("form.fetch input").hide();
					$("form.fetch button").hide();
					$("form.search select").hide();
					$("form.search input").hide();
					$("form.search button").hide();
				}
			},
			error: function(result) {
				$("form.search select").hide();
				$("form.search input").hide();
				$("form.search button").hide();
			}
		});
	});
});