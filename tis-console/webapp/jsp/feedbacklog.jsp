<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
	<head>	
<style type="text/css"><!--
 #msgdialog {
   width:100%;
   height:90%;
   background-color:black;
   color:white;margin:0 auto;
   overflow-y:scroll;
   padding:4px;
   #color:#00FF00;
   border-radius: 5px;
 }
#msgdialog li{
 word-wrap: break-word; 
 word-break: normal; 
}
.msg {
  color:#00FF00;
}
.warn {
  color:yellow;
}
.error{
  color:red;
}
pre{
 margin:0px;
 word-wrap:break-word;
}
-->
</style>	
    <link href="/runtime/css/jquery-ui-1.9.1.custom.min.css" rel="stylesheet" type="text/css"/>
    <link href="/runtime/css/customerCenter.css" rel="stylesheet" type="text/css"/>
    <script src="/runtime/js/jquery-1.8.2.js"></script>
    <script src="/runtime/js/jquery-ui-1.9.1.custom.min.js"></script>
	</head>
	<body>
	<%--
		<fieldset style="float:left;width:300px;height:40px">
		  <legend>显示字段</legend>
		  <input type="checkbox" value="time" id="chktime" checked /><label for="chktime">时间</label>
		  <input type="checkbox" value="level" id="chklevel" checked /><label for="chklevel">level</label>
		  <input type="checkbox" value="component" id="chkcomponent" checked /><label for="chkcomponent">来源</label>
		  <input type="checkbox" value="address" id="chkaddress" checked /><label for="chkaddress">ip</label>
		</fieldset>
		<fieldset style="float:left;width:100px;height:40px">
		  <legend>控制</legend>
		 <button onclick="Chat.stop();">停止</button>
		 <button onclick="Chat.resume();">开启</button>
		</fieldset>
--%>
<div style="background-color:#999999;padding:15px;clear:both;">
<%--
		<table width="100%" height="40" style="background-color:#6E6E6E;border-radius: 5px;border: thin inset blue;margin-bottom: 6px;" >
			<tr>
				 <td  style="background-color:#00FF00;width:30%;height;100%;border-radius: 5px;" align="center">30%</td>
				 <td></td>
			</tr>
		</table>
--%>		
		<ul id="msgdialog" >
			
		</ul>
	</div>
		
	</body>
</html>

<script>
        var Chat = {};

        Chat.socket = null;

         Chat.isStop = false;

        Chat.stop = function(){
           Chat.isStop = true;
        }
        
        Chat.resume = function(){
           Chat.isStop = false;
        }

        Chat.connect = (function(host) {
            if ('WebSocket' in window) {
                Chat.socket = new WebSocket(host);
            } else if ('MozWebSocket' in window) {
                Chat.socket = new MozWebSocket(host);
            } else {
                Console.log('Error: WebSocket is not supported by this browser.');
                return;
            }

            Chat.socket.onopen = function () {
                //Console.log('Info: WebSocket connection opened.');
                //document.getElementById('chat').onkeydown = function(event) {
                //    if (event.keyCode == 13) {
                //        Chat.sendMessage();
                //    }
                //};
                
                console.debug("hello");    
            };

            Chat.socket.onclose = function () {
               // document.getElementById('chat').onkeydown = null;
               // Console.log('Info: WebSocket closed.');
            };

            Chat.socket.onmessage = function (message) {
                if(Chat.isStop){
                  return ;
                }
                //var msg = jQuery.parseJSON(message.data);
                addmessage(message.data);
            };
        });

<%
  String appname = request.getParameter("appname");
  String runtime = request.getParameter("runtime");
  String logtype = request.getParameter("logtype");
  
  if( appname != null && !appname.startsWith("search4")){
    throw new IllegalArgumentException("parameter appname "+ appname+" can not be null");
  }
  Integer taskid = null;
  if(appname == null){
    taskid = Integer.valueOf( request.getParameter("taskid"));
  }
  
  
  if( runtime == null){
     throw new IllegalArgumentException("parameter runtime "+ runtime+" can not be null");
  }
%>

        Chat.initialize = function() {
Chat.connect('ws://' + window.location.host 
   + '/download/logfeedback?collection=<%=appname%>&logtype=<%=logtype%>');
         };

        Chat.sendMessage = (function() {
            var message = document.getElementById('chat').value;
            if (message != '') {
                Chat.socket.send(message);
                document.getElementById('chat').value = '';
            }
        });

        var Console = {};

        Console.log = (function(message) {
          console.debug(message);
        });

        Chat.initialize();


        document.addEventListener("DOMContentLoaded", function() {
            // Remove elements with "noscript" class - <noscript> is not allowed in XHTML
            var noscripts = document.getElementsByClassName("noscript");
            for (var i = 0; i < noscripts.length; i++) {
                noscripts[i].parentNode.removeChild(noscripts[i]);
            }
        }, false);


	var ii = 0;
	function addmessage(msg){
	   removeHead();
	  
	  // var log ='';
	   
	  //  if($("#chktime").attr("checked")){
      //       log += '['+msg +"]" ;    
     //   }
        
      //  if($("#chkcomponent").attr("checked")){
     //        log += '['+msg.component +"]" ;    
      //  }
        
      //  if($("#chklevel").attr("checked")){
      //       log += '['+msg.levelliteral +"]" ;    
     //   }
        
      //  if($("#chkaddress").attr("checked")){
      //       log += '['+msg.address +"]" ;    
      //  }
        
        var msgClass = "msg";
     //   if(msg.level>=3){
    //      msgClass = "error";
     //   }else if(msg.level > 1 ){
    //      msgClass = "warn";
     //   }
        
	   $("#msgdialog").append("<li class='"+msgClass+"'>"+msg +"</li>");
	}
	
	function removeHead(){
	  if($("#msgdialog")[0].scrollHeight > ($("#msgdialog").height()+50)){
	    $("#msgdialog li:first-child").remove();
	    return removeHead();
      }else{
    	return true;
      }
	}
</script>