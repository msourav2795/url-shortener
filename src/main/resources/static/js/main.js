$(document).ready(function() {
	var ip=location.host;
	$("#button").click(function() {
		$.ajax({
			type : 'POST',
			url : 'http://'+ ip +'/shortenurl',
			data : JSON.stringify({
				"fullUrl" : $("#urlinput").val()
			}),
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				$("#shorturltext").val(data.shortUrl);
			}
		});
	});
	$("#customButton").click(function() {
		$("#info").hide();
		$.ajax({
			type : 'POST',
			url : 'http://'+ ip +'/customUrl',
			data : JSON.stringify({
				"shortUrl" : $("#cshorturltext").val(),
				"fullUrl" : $("#curlinput").val()
			}),
			contentType : "application/json; charset=utf-8",
			success : function(data) {
				$("#cshorturltext").val(data.shortUrl);
				if(data.shortUrl==""){
					$("#info").text("Url already exists");
				}
				else{
					$("#info").text("Custom url generated");
				}
				$("#info").show();
			}
		});
	});
});