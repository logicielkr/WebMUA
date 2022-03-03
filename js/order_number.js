/**
Usage : 
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
*/
function GrahaCommonOrderNumber() {
}

GrahaCommonOrderNumber.appendButton = function(url, tableId, table, label) {
	if("ontouchstart" in document.documentElement) {
	} else if($("table#" + tableId + " tbody tr").length > 1) {
		$("div.nav.bottom div.right").append("<input type='button' value='" + label.default + "' onclick='GrahaCommonOrderNumber.applyOrder(this, \"" + url + "\", \"" + tableId + "\", \"" + table.pk + "\", \"" + table.order_column + "\", \"" + label.default + "\", \"" + label.apply + "\", \"" + label.fail + "\", \"" + label.processing + "\")' />");
	}
};

GrahaCommonOrderNumber.applyOrder = function(obj, url, tableId, tablePk, orderColumn, defaultLabel, applyLabel, failLabel, processingLabel) {
	if($(obj).val() == defaultLabel) {
		$("table#" + tableId + " tbody td").each(function(){
			$(this).css("width", $(this).width() +"px");
		});
		$("table#" + tableId + " tbody").sortable({
			cursor: 'pointer',
			axis: 'y'
		});
		$("table#" + tableId + " tbody").disableSelection();
		$(obj).val(applyLabel);
	} else if($(obj).val() == applyLabel) {
		var index = 1;
		var formData = new FormData();
		$("table#" + tableId + " tbody td." + tablePk + "").each(function() {
			formData.append("" + tablePk + "." + index, $(this).text());
			formData.append("" + orderColumn + "." + index, index);
			index++;
		});
		$(obj).val(processingLabel);
		$.ajax({
			url: url,
			processData: false,
			contentType: false,
			type: 'POST',
			enctype: 'multipart/form-data',
			data: formData,
			success: function(result) {
				$("table#" + tableId + " tbody").sortable("destroy");
				$("table#" + tableId + " tbody").enableSelection();
				$(obj).val(defaultLabel);
			},
			error: function(result) {
				$(obj).val(failLabel);
			}
		});
	}
};