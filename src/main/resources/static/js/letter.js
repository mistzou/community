$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
	$(".delMessage").click(delete_msg2);
});

function send_letter() {
	$("#sendModal").modal("hide");

	const toName = $("#recipient-name").val();

	const content = $("#message-text").val();


	$.post(
		CONTEXT_PATH + "/letter/sendMessage",
		{"toName":toName,"content":content},
		function (data){
			data = $.parseJSON(data);
			$("#hintBody").html(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}

function delete_msg2() {
	const id = $(this).attr("value");
	const obj = $(this).parents(".media");
	if(id==null || id=="" || id==undefined){
		return false;
	}
	$.post(
		CONTEXT_PATH + "/letter/delMessage",
		{"id":id},
		function (data){
			data = $.parseJSON(data);
			if(data.code == 0){
				// TODO 删除数据
				obj.remove();
			}
		}
	)
}