<%! int count =0;%>

<div id="right" >
<h1><%=count++%></h1>
<table border="1" width="100%">
<tbody><tr>
 <th width="5%">ip</th>
 <th>path</th>
 <th width="8%">content</th>
 <th width="13%">更新时间</th>
 <th width="13%">创建时间</th>
</tr>
<tr> 
<td align="center">10.232.36.130/10.232.36.130:2181 </td>
<td>/terminator/dump-controller/search4realjhsItem </td>
<td>10.235.145.94  <br>
 
 <a href="http://daily.terminator.admin.taobao.org/runtime/zklock_edit.htm?path=%2Fterminator%2Fdump-controller%2Fsearch4realjhsItem" onclick="opendialog('编辑',this.href,600,400,function(){ window.location.reload();});return false;">编辑</a>
  
 <button onclick="testClick();">testClick</button> 
 </td>
<td align="right">2013/06/03 18:41:26</td>
<td>
  2013/06/03 18:41:26 
</td>
</tr>
</tbody></table>
</div>
<script>
 function testClick(){
   alert("fuck me");
 }
</script>