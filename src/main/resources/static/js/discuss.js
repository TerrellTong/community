function like(btn,entityType,entityId) {
	$.post(
		CONTEXT_PATH + "/like",
		{
			"entityType":entityType,
			"entityId":entityId
		},
		function (data) {
			//将json字符串转成JS对象
			data = $.parseJSON(data);
			if(data.code == 0){
				$(btn).children("i").text(data.likeCount);
				$(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
			}else{
				alert(data,msg);
			}
		}
	);
}
