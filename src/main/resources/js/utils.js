function randomString() {
	var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
	var string_length = 6;
	var randomstring = 'id';
	for (var i=0; i<string_length; i++) {
		var rnum = Math.floor(Math.random() * chars.length);
		randomstring += chars.substring(rnum,rnum+1);
	}
}

function jAlert(message){
	var id = randomString();
	$("#btnLogin").simpledialog({
        'mode' : 'blank',
        'prompt': false,
        'forceInput': false,
        'useModal':true,
        'fullHTML' : 
			"<div style='text-align:center; margin:10px;'>" +
			message +
            "<a data-role='button' href='#' id='"+ id +"'>Close</a>" +
			"</div>"			
    });	
	
	setTimeout(
	$("#" + id).click(function(e) {
        $('#btnLogin').simpledialog('close');
    }), 500);
}