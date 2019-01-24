<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.List"%>
<%@ page import="com.taobao.terminator.manage.biz.dal.pojo.ServerJoinGroup"%>
<%@ page import="java.lang.Short"%>
<%@ page import="java.util.Collection"%>
<%@ page import="com.taobao.terminator.manage.common.ManageUtils"%>
<html>
<head>
<title>indexquery</title>
<link href="/runtime/css/jquery-ui-1.9.1.custom.min.css" rel="stylesheet" type="text/css"/>
<script src="/runtime/js/jquery-1.8.2.js"></script>
<script src="/runtime/js/jquery-ui-1.9.1.custom.min.js"></script>

<style type="text/css"><!--
 #serverSelectDialog, #selectColumn{
 
  width: 600px;
  background-color:#EAEAEA;
  border: 3px solid #0000FF;
  padding:7px;
  display:none;
 }
 
 .explain-block{
 
  background-color:#EAEAEA;
  border: 1px solid #0000FF;
  padding:7px;
 }
 
  .help{
     background: url(<%=request.getContextPath()%>/runtime/imgs/help.png) no-repeat scroll left center transparent ;
     padding-left:17px;
     background-color: white;
   } 
 
 -->
</style>
 <script>
  function open_message_box(){
      var  $dialog = $('<div id="messagebox" style="height:600px;overflow:hidden;overflow-y:auto;background-color:black;color:white;"></div>');  
		$dialog.dialog({
			autoOpen: false,
			title: "",
			maxHeight:600,
			height:600
		});
		 
    $dialog.dialog("option" , "width" , 1000);
    $dialog.dialog('open');
   }
   
   function appendMessage(json){
   
   if(! $("#messagebox").is(":visible")){
   $("#messagebox").show('slow',function(){});
   }
  
   for(var i=0;i<json.list.length;i++){
     var row = json.list[i];
     var tr = $('<tr></tr>');
     tr.append($("<td width='5%'>"+ row.server+'</td>'));

     var content = 
      $("<td style='position:relative;word-break:break-all;'><a href='#' explainid='"+row.pk+"' style='background-color:pink;<%=ManageUtils.isTrue("debugQuery")?"":"display:none;"%>' onclick='return openExplain(this)'>explain</a>"+ row.rowContent + "</td>");
     
     <%if(ManageUtils.isTrue("debugQuery")){%>
       content.append($("<div id='e"+row.pk+"' class='explain-block' style='display:none;'>"
                      +"<button onclick='explain_close();' >关闭</button><br/>"+row.explain+"</div>"));
     <%}%>
     
      tr.append(content);	
     $("#messagebox").append(tr);
   }
   }
   
   function explain_close(){
     $(".explain-block").hide('slow', function(){});
   }
   
   
   function openExplain(link){
   var targetid = $(link).attr('explainid');
       $("#e"+targetid).show('slow', function(){});
        return false;
   }
   
   var resultCount =0
   
   function setresultcount(count){
       if(count<resultCount){
         return;
       }
       resultCount = count;
       $("#resultcount").html("结果条数:"+resultCount);
   }
   
   function dailysearch(ipaddress,value){
   
    if(! $("#messagebox").is(":visible")){
      $("#messagebox").show('slow',function(){});
    }
      
    for( var i = 0 ;i< value.response.docs.length ;i++){
      
      var row = value.response.docs[i];
       
      var tr = $('<tr></tr>');

      tr.append($("<td width='5%'>"+ ipaddress+'</td>'));

      var content = $('<td> </td>');
      for(var key in row){
       content.append( '<strong>'+ key+"</strong>:"+row[key] +" &nbsp;");
      }
      tr.append(content);
      
      $("#messagebox").append(tr);
    
    }
   }
   
   
   function btnSfieldClick(){
        $("#selectColumn").show('slow', function(){});
        return false;
   }
  
   
   function btnHideFieldSelectClick(){
        $("#selectColumn").hide('slow', function(){});
        return false;
   }
   
   function btnserverSelectClick(){
        $("#serverSelectDialog").show('slow', function(){});
        return false;
   }
   
    function btnHideServerSelectClick(){
        $("#serverSelectDialog").hide('slow', function(){});
        return false;
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
   
</script>
</head>
<body>
<form method="post" id="queryForm" action="<%=request.getContextPath()%>/query-index">
<%--
<a target="_top" href="<%=request.getContextPath()%>/runtime/app.htm?appid=<%=ManageUtils.getAppDomain(request).getAppid()%>">应用服务器一览</a>
--%>
<%
    Map<Short, List<ServerJoinGroup>> canidateServer 
      = (Map<Short, List<ServerJoinGroup>>)request.getAttribute("querySelectServerCandiate");
    Collection<String> selectedCanidateServers 
      = ( Collection<String>)request.getAttribute("selectedCanidateServers");
if(canidateServer != null){
  %>
 
  <a id="btnserverSelect" href="#" onclick="return btnserverSelectClick();">设置目标服务器</a>
  <a target='_blank' href='/runtime/view_pojo.htm'  onclick="opendialog(null,this.href,$(window).width()-15,($(document).height()>$(window).height()?$(document).height():$(window).height())-15,function(){}); return false;">POJO</a>
  
  
<div>

<div id="serverSelectDialog">

  <table width="100%">
  <tr><td width="50%"><button onclick="return btnHideServerSelectClick()">关闭</button></td>
      <td align="right"><button id="selectall">全选</button><button  id="unselectall">全不选</button></td></tr>
  </table>
  <table width="100%" border="1">
   <%for(Map.Entry<Short, List<ServerJoinGroup>> groupindex : canidateServer.entrySet()){%>
    <tr>
      <td width="40px">第<%=groupindex.getKey()%>组</td>
      <td>
         <%for( ServerJoinGroup server :  groupindex.getValue()){%>
            
            <input id="lab<%=server.hashCode()%>" <%if(selectedCanidateServers.contains(server.getIpAddress()+"_"+ groupindex.getKey())){%>checked<%}%> type="checkbox" name="servergroup<%=groupindex.getKey()%>" value="<%=server.getIpAddress()%>" />
            <lable for="lab<%=server.hashCode()%>" style="font-weight:<%=server.isLeader()?"bold":""%>"><%=server.getIp()%></lable>
                  
         <%}%>
      </td>
    </tr>
   <%}%>
  </table>
</div>
</div>
<%
}
%>



<fieldset>
 <legend>设置查询参数</legend>
 <input type="hidden" name="appName" value="<%=request.getParameter("appName")%>" />
 <div>
 <span>query:</span><span class="help"><a target="_blank"
  href="http://wiki.apache.org/solr/SolrQuerySyntax">Solr查询语法</a></span> <br/>
 
 <textarea name="q" cols="60" rows="2"><%=org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("q"),"*:*")%></textarea> 
 <span style="color:#cccccc;font-size:16px"> example:  *:*,id:478222</span>
 </div>
 <div>
  <span style="display:inline-block;width:5em;">sort:</span>
  <input type="text" name="sort" value="<%=org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("sort"),"")%>" size="40"/> 
   <span style="color:#cccccc;font-size:16px"> example:  "create_time desc"</span>
  </div>
 <div><span style="display:inline-block;width:5em;">show rows:</span>
  <input type="text" name="shownum" value="<%=org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("shownum"),"3")%>" />
 </div>
 <!--**************************添加代码--begin******************************** -->
 <div><span style="display:inline-block;width:5em;">fq:</span>
  <input type="text" name="fq" placeholder="id:[5 TO 12]" value="<%=org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("fq"),"")%>" />
  <span style="color:#cccccc;font-size:16px"> example:  "id:[1 TO 10]"</span>
 </div>
 <!--**************************添加代码--end********************************** -->
  <div style="position:relative"><span style="display:inline-block;width:3em;">column:</span>
  <a id="select_column" href="#" onclick="return btnSfieldClick();">选择</a>
  <%--▼▼▼selectColumn start--%>
  <div id="selectColumn" style="display:none;">
    <p>
      <button onclick="return btnHideFieldSelectClick()">关闭</button>
      <button id="fieldselectall">全选</button><button  id="fieldunselectall">全不选</button>
    </p>
     <div>
  <%
   List<com.taobao.terminator.solrdao.pojo.PSchemaField> slist 
     =  (List<com.taobao.terminator.solrdao.pojo.PSchemaField> ) request.getAttribute("sfields");
   
   List<String> selectedFields = ( List<String>)request.getAttribute("selectedFields");  
     if(selectedFields == null){
       throw new IllegalStateException(" the attr of the key 'selectedFields' can not be null in httprequest");
     }
  for(com.taobao.terminator.solrdao.pojo.PSchemaField field: slist){
   if(field.isStored()){
   
  %>
  <input type="checkbox" name="sfields" <%=selectedFields.contains(field.getName())?"checked":""%> 
    id="sfields<%=field.hashCode()%>"  value="<%=field.getName()%>" >
  <label for="sfields<%=field.hashCode()%>"><%=field.getName()%></label>
  <%}
  }
  %>
     </div>

</div>
&nbsp;&nbsp;&nbsp;
<input type="checkbox" value="true" id="debugQuery" name="debugQuery" <%=ManageUtils.isTrue("debugQuery")?"checked":""%> />
<label for="debugQuery">debugQuery:</label>
 <%--▲▲▲selectColumn end--%>
 </div>
 <p>  
   <button onclick="queryCollection();">query</button>
   <span id="resultcount"></span>
 </p>
</fieldset>


</form>

<table style="display:none;" id="messagebox" width="100%" border="1" style="table-layout:fixed;">
</table>
 

</body>
</html>

<script>

 function appendMessage(json){
   
   if(! $("#messagebox").is(":visible")){
   $("#messagebox").show('slow',function(){});
   }
  
   for(var i=0;i<json.result.length;i++){
     var row = json.result[i];
     var tr = $('<tr></tr>');
     tr.append($("<td width='5%'>"+ row.server+'</td>'));

     var content = 
      $("<td style='position:relative;word-break:break-all;'><a href='#' explainid='"+row.pk+"' style='background-color:pink;<%=ManageUtils.isTrue("debugQuery")?"":"display:none;"%>' onclick='return openExplain(this)'>explain</a>"+ row.rowContent + "</td>");
     
     <%if(ManageUtils.isTrue("debugQuery")){%>
       content.append($("<div id='e"+row.pk+"' class='explain-block' style='display:none;'>"
                      +"<button onclick='explain_close();' >关闭</button><br/>"+row.explain+"</div>"));
     <%}%>
     
      tr.append(content);   
     $("#messagebox").append(tr);
   }
   }


  function queryCollection(){
    jQuery.ajax({
        dataType:"jsonp",
        url:"<%=request.getContextPath()%>/query-index",
        data:$("#queryForm").serialize(),
        success:function(data,textStatus)
        {
        
                
        });
  }

  $(document).ready(function(){
    
    $("#selectall").click(function(){
      $("#serverSelectDialog").find("input[type='checkbox']").attr("checked",true);
        return false;
      }
    );
    
   $("#fieldselectall").click(function(){ 
       $("#selectColumn").find("input[type='checkbox']").attr("checked",true);
        return false;
   });
   
    $("#fieldunselectall").click(function(){     
       $("#selectColumn").find("input[type='checkbox']").attr("checked",false);
        return false;
    });
    
   
    
    
    $("#unselectall").click(function(){
    $("#serverSelectDialog").find("input[type='checkbox']").attr("checked",false);
      return false;
    });
  })
</script>


 
