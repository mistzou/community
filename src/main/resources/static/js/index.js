$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	//获取标题和内容
	const title = $("#recipient-name").val();
	const content = $("#message-text").val();

	if(title == ''){
		msg("标题不能为空！",1);
		return false;
	}else if(content == ''){
		msg("正文不能为空！",1);
		return false;
	}
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{"title":title,"content":content},
		function(data){
			data = $.parseJSON(data);
			msg(data.msg,data.code);
		})
}

function msg(msg,code){
	//在提示框内显示返回信息
	$("#hintBody").html(msg);
	//显示提示框
	$("#hintModal").modal("show");
	//自动隐藏提示框
	setTimeout(function(){
		$("#hintModal").modal("hide");
		//刷新页面
		if(code == 0){
			$("#publishModal").modal("hide");
			window.location.reload();
		}
	}, 2000);
}