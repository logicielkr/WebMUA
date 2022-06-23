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
	order_column:"order_number",
	param_names:["parent_id"],
	param_values:["1"]
};
var url = "order_number.xml";
GrahaCommonOrderNumber.appendButton(url, "graha_mail_account", table, label);
*/
function GrahaCommonOrderNumber() {
	
}

GrahaCommonOrderNumber.appendButton = function(url, tableId, table, label) {
	if("ontouchstart" in document.documentElement) {
	} else if($("table#" + tableId + " tbody tr").length > 1) {
		$("div.nav.bottom div.center").append("<input type='button' value='" + label.default + "' onclick='GrahaCommonOrderNumber.applyOrder(this, \"" + url + "\", \"" + tableId + "\")' />");
		GrahaCommonOrderNumber.table = table;
		GrahaCommonOrderNumber.label = label;
	}
};

GrahaCommonOrderNumber.applyOrder = function(obj, url, tableId) {
	if($(obj).val() == GrahaCommonOrderNumber.label.default) {
		$("table#" + tableId + " tbody td").each(function(){
			$(this).css("width", $(this).width() +"px");
		});
		$("table#" + tableId + " tbody").sortable({
			cursor: 'pointer',
			axis: 'y'
		});
		$("table#" + tableId + " tbody").disableSelection();
		$(obj).val(GrahaCommonOrderNumber.label.apply);
	} else if($(obj).val() == GrahaCommonOrderNumber.label.apply) {
		var index = 1;
		var formData = new FormData();
		$("table#" + tableId + " tbody td." + GrahaCommonOrderNumber.table.pk + "").each(function() {
			formData.append("" + GrahaCommonOrderNumber.table.pk + "." + index, $(this).text());
			formData.append("" + GrahaCommonOrderNumber.table.order_column + "." + index, index);
			/*
			console.log("" + GrahaCommonOrderNumber.table.pk + "." + index, $(this).text());
			console.log("" + GrahaCommonOrderNumber.table.order_column + "." + index, index);
			*/
			index++;
		});
		if(GrahaCommonOrderNumber.table.param_names) {
			for(var i = 0; i < GrahaCommonOrderNumber.table.param_names.length; i++) {
				formData.append(GrahaCommonOrderNumber.table.param_names[i], GrahaCommonOrderNumber.table.param_values[i]);
			}
		}
		$(obj).val(GrahaCommonOrderNumber.label.processing);
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
				$(obj).val(GrahaCommonOrderNumber.label.default);
			},
			error: function(result) {
				$(obj).val(GrahaCommonOrderNumber.label.fail);
			}
		});
	}
};