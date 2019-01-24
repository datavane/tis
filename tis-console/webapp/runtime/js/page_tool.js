
   function open_dialog(titlee,content,width){
     var $dialog = $('<div></div>')
		.html(content)
		.dialog({
			autoOpen: false,
			title: titlee,
			modal:true
		});
    
    $dialog.dialog( "option" , "width" , width);
    $dialog.dialog('open');
   }
   
String.prototype.isEmpty = function() {
  return this.length === 0 || this == " " || /^\s*$/.test(this);
}
   
function opendialog(title,src,width,height,callbackfunc){
   try
{ 
     var dialog = $("<div style='height:"+(height)+"'> <iframe id='wdialog' frameborder='0' height='"+(height-60)+"' width='100%' src='"+src+"' ></iframe></div>")
		.dialog({
			autoOpen: false,
			width:width,
			height:height,
			title: title,
			modal: true,
			autoResize:false,
			//close: function(event, ui) { 
			//  if(!notRefeshWhenDialogClose){ 
			//     window.location.reload();
			//  }  
			//}
			close: callbackfunc
		});
	 
    dialog.dialog('open');
    return dialog;
}catch(err)
{
   alert(err);
}
   }
   
   function appendMessage(data,container){
	   if(data.errormsg.length>0){
		  var errorul = $("<ul class='errorMessage'></ul>");				  	
		  for(var i =0;i<data.errormsg.length;i++){
			errorul.append($("<li>"+data.errormsg[i]+"</li>"));
		  }
		  container.append(errorul);
	    }
				  	
		if(data.msg.length >0){
		  var msgul = $("<ul class='actionMessage'></ul>");
		  for(var i =0;i<data.msg.length;i++){
			 msgul.append($("<li>"+data.msg[i]+"</li>")); 
		  }
		  container.append(msgul);
		}
	}
	
	function showMessageDialog(data){
	   var content = $("<div></div>");
	   appendMessage(data,content);
	   open_dialog("服务器执行结果",content,600);
	}
   
