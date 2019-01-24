/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerJoinGroup;
import com.qlangtech.tis.manage.common.ManageUtils;

/*
 * 生成的jsp：C:\Users\baisui\AppData\Local\Temp\jetty-0.0.0.0-8080-webapp-_-any-8060336629567986024.dir\jsp\org\apache\jsp
 * <br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Query_005findex_jsp extends BasicServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter _jspx_out = null;
        PrintWriter out = null;
        try {
            out = resp.getWriter();
            _jspx_out = out;
            out.write("<html>\r\n");
            out.write("<head>\r\n");
            out.write("<title>indexquery</title>\r\n");
            out.write("<link href=\"/runtime/css/jquery-ui-1.9.1.custom.min.css\" rel=\"stylesheet\" type=\"text/css\"/>\r\n");
            out.write("<script src=\"/runtime/js/jquery-1.8.2.js\"></script>\r\n");
            out.write("<script src=\"/runtime/js/jquery-ui-1.9.1.custom.min.js\"></script>\r\n");
            out.write("\r\n");
            out.write("<style type=\"text/css\"><!--\r\n");
            out.write(" #serverSelectDialog, #selectColumn{\r\n");
            out.write(" \r\n");
            out.write("  width: 600px;\r\n");
            out.write("  background-color:#EAEAEA;\r\n");
            out.write("  border: 3px solid #0000FF;\r\n");
            out.write("  padding:7px;\r\n");
            out.write("  display:none;\r\n");
            out.write(" }\r\n");
            out.write(" \r\n");
            out.write(" .explain-block{\r\n");
            out.write(" \r\n");
            out.write("  background-color:#EAEAEA;\r\n");
            out.write("  border: 1px solid #0000FF;\r\n");
            out.write("  padding:7px;\r\n");
            out.write(" }\r\n");
            out.write(" \r\n");
            out.write("  .help{\r\n");
            out.write("     background: url(");
            out.print(request.getContextPath());
            out.write("/runtime/imgs/help.png) no-repeat scroll left center transparent ;\r\n");
            out.write("     padding-left:17px;\r\n");
            out.write("     background-color: white;\r\n");
            out.write("   } \r\n");
            out.write(" \r\n");
            out.write(" -->\r\n");
            out.write("</style>\r\n");
            out.write(" <script>\r\n");
            out.write("  function open_message_box(){\r\n");
            out.write("      var  $dialog = $('<div id=\"messagebox\" style=\"height:600px;overflow:hidden;overflow-y:auto;background-color:black;color:white;\"></div>');  \r\n");
            out.write("\t\t$dialog.dialog({\r\n");
            out.write("\t\t\tautoOpen: false,\r\n");
            out.write("\t\t\ttitle: \"\",\r\n");
            out.write("\t\t\tmaxHeight:600,\r\n");
            out.write("\t\t\theight:600\r\n");
            out.write("\t\t});\r\n");
            out.write("\t\t \r\n");
            out.write("    $dialog.dialog(\"option\" , \"width\" , 1000);\r\n");
            out.write("    $dialog.dialog('open');\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   function appendMessage(json){\r\n");
            out.write("   \r\n");
            out.write("   if(! $(\"#messagebox\").is(\":visible\")){\r\n");
            out.write("   $(\"#messagebox\").show('slow',function(){});\r\n");
            out.write("   }\r\n");
            out.write("  \r\n");
            out.write("   for(var i=0;i<json.list.length;i++){\r\n");
            out.write("     var row = json.list[i];\r\n");
            out.write("     var tr = $('<tr></tr>');\r\n");
            out.write("     tr.append($(\"<td width='5%'>\"+ row.server+'</td>'));\r\n");
            out.write("\r\n");
            out.write("     var content = \r\n");
            out.write("      $(\"<td style='position:relative;word-break:break-all;'><a href='#' explainid='\"+row.pk+\"' style='background-color:pink;");
            out.print(ManageUtils.isTrue("debugQuery") ? "" : "display:none;");
            out.write("' onclick='return openExplain(this)'>explain</a>\"+ row.rowContent + \"</td>\");\r\n");
            out.write("     \r\n");
            out.write("     ");
            if (ManageUtils.isTrue("debugQuery")) {
                out.write("\r\n");
                out.write("       content.append($(\"<div id='e\"+row.pk+\"' class='explain-block' style='display:none;'>\"\r\n");
                out.write("                      +\"<button onclick='explain_close();' >关闭</button><br/>\"+row.explain+\"</div>\"));\r\n");
                out.write("     ");
            }
            out.write("\r\n");
            out.write("     \r\n");
            out.write("      tr.append(content);\t\r\n");
            out.write("     $(\"#messagebox\").append(tr);\r\n");
            out.write("   }\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   function explain_close(){\r\n");
            out.write("     $(\".explain-block\").hide('slow', function(){});\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   \r\n");
            out.write("   function openExplain(link){\r\n");
            out.write("   var targetid = $(link).attr('explainid');\r\n");
            out.write("       $(\"#e\"+targetid).show('slow', function(){});\r\n");
            out.write("        return false;\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   var resultCount =0\r\n");
            out.write("   \r\n");
            out.write("   function setresultcount(count){\r\n");
            out.write("       if(count<resultCount){\r\n");
            out.write("         return;\r\n");
            out.write("       }\r\n");
            out.write("       resultCount = count;\r\n");
            out.write("       $(\"#resultcount\").html(\"结果条数:\"+resultCount);\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   function dailysearch(ipaddress,value){\r\n");
            out.write("   \r\n");
            out.write("    if(! $(\"#messagebox\").is(\":visible\")){\r\n");
            out.write("      $(\"#messagebox\").show('slow',function(){});\r\n");
            out.write("    }\r\n");
            out.write("      \r\n");
            out.write("    for( var i = 0 ;i< value.response.docs.length ;i++){\r\n");
            out.write("      \r\n");
            out.write("      var row = value.response.docs[i];\r\n");
            out.write("       \r\n");
            out.write("      var tr = $('<tr></tr>');\r\n");
            out.write("\r\n");
            out.write("      tr.append($(\"<td width='5%'>\"+ ipaddress+'</td>'));\r\n");
            out.write("\r\n");
            out.write("      var content = $('<td> </td>');\r\n");
            out.write("      for(var key in row){\r\n");
            out.write("       content.append( '<strong>'+ key+\"</strong>:\"+row[key] +\" &nbsp;\");\r\n");
            out.write("      }\r\n");
            out.write("      tr.append(content);\r\n");
            out.write("      \r\n");
            out.write("      $(\"#messagebox\").append(tr);\r\n");
            out.write("    \r\n");
            out.write("    }\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   \r\n");
            out.write("   function btnSfieldClick(){\r\n");
            out.write("        $(\"#selectColumn\").show('slow', function(){});\r\n");
            out.write("        return false;\r\n");
            out.write("   }\r\n");
            out.write("  \r\n");
            out.write("   \r\n");
            out.write("    function btnHideFieldSelectClick(){\r\n");
            out.write("        $(\"#selectColumn\").hide('slow', function(){});\r\n");
            out.write("        return false;\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   function btnserverSelectClick(){\r\n");
            out.write("        $(\"#serverSelectDialog\").show('slow', function(){});\r\n");
            out.write("        return false;\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("    function btnHideServerSelectClick(){\r\n");
            out.write("        $(\"#serverSelectDialog\").hide('slow', function(){});\r\n");
            out.write("        return false;\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("   function opendialog(title,src,width,height,callbackfunc){\r\n");
            out.write("   try\r\n");
            out.write("{ \r\n");
            out.write("     var dialog = $(\"<div style='height:\"+(height)+\"'> <iframe id='wdialog' frameborder='0' height='\"+(height-60)+\"' width='100%' src='\"+src+\"' ></iframe></div>\")\r\n");
            out.write("\t\t.dialog({\r\n");
            out.write("\t\t\tautoOpen: false,\r\n");
            out.write("\t\t\twidth:width,\r\n");
            out.write("\t\t\theight:height,\r\n");
            out.write("\t\t\ttitle: title,\r\n");
            out.write("\t\t\tmodal: true,\r\n");
            out.write("\t\t\tautoResize:false,\r\n");
            out.write("\t\t\t//close: function(event, ui) { \r\n");
            out.write("\t\t\t//  if(!notRefeshWhenDialogClose){ \r\n");
            out.write("\t\t\t//     window.location.reload();\r\n");
            out.write("\t\t\t//  }  \r\n");
            out.write("\t\t\t//}\r\n");
            out.write("\t\t\tclose: callbackfunc\r\n");
            out.write("\t\t});\r\n");
            out.write("\t \r\n");
            out.write("    dialog.dialog('open');\r\n");
            out.write("    return dialog;\r\n");
            out.write("}catch(err)\r\n");
            out.write("{\r\n");
            out.write("   alert(err);\r\n");
            out.write("}\r\n");
            out.write("   }\r\n");
            out.write("   \r\n");
            out.write("</script>\r\n");
            out.write("</head>\r\n");
            out.write("<body>\r\n");
            out.write("<form method=\"post\" action=\"");
            out.print(request.getContextPath());
            out.write("/query-index\">\r\n");
            out.write('\r');
            out.write('\n');
            Map<Short, List<ServerJoinGroup>> canidateServer = (Map<Short, List<ServerJoinGroup>>) request.getAttribute("querySelectServerCandiate");
            Collection<String> selectedCanidateServers = (Collection<String>) request.getAttribute("selectedCanidateServers");
            if (canidateServer != null) {
                out.write("\r\n");
                out.write(" \r\n");
                out.write("  <a id=\"btnserverSelect\" href=\"#\" onclick=\"return btnserverSelectClick();\">设置目标服务器</a>\r\n");
                out.write("  <a target='_blank' href='/runtime/view_pojo.htm'  onclick=\"opendialog(null,this.href,$(window).width()-15,($(document).height()>$(window).height()?$(document).height():$(window).height())-15,function(){}); return false;\">POJO</a>\r\n");
                out.write("  \r\n");
                out.write("  \r\n");
                out.write("<div>\r\n");
                out.write("\r\n");
                out.write("<div id=\"serverSelectDialog\">\r\n");
                out.write("\r\n");
                out.write("  <table width=\"100%\">\r\n");
                out.write("  <tr><td width=\"50%\"><button onclick=\"return btnHideServerSelectClick()\">关闭</button></td>\r\n");
                out.write("      <td align=\"right\"><button id=\"selectall\">全选</button><button  id=\"unselectall\">全不选</button></td></tr>\r\n");
                out.write("  </table>\r\n");
                out.write("  <table width=\"100%\" border=\"1\">\r\n");
                out.write("   ");
                for (Map.Entry<Short, List<ServerJoinGroup>> groupindex : canidateServer.entrySet()) {
                    out.write("\r\n");
                    out.write("    <tr>\r\n");
                    out.write("      <td width=\"40px\">第");
                    out.print(groupindex.getKey());
                    out.write("组</td>\r\n");
                    out.write("      <td>\r\n");
                    out.write("         ");
                    for (ServerJoinGroup server : groupindex.getValue()) {
                        out.write("\r\n");
                        out.write("            \r\n");
                        out.write("            <input id=\"lab");
                        out.print(server.hashCode());
                        out.write('"');
                        out.write(' ');
                        if (selectedCanidateServers.contains(server.getIpAddress() + "_" + groupindex.getKey())) {
                            out.write("checked");
                        }
                        out.write(" type=\"checkbox\" name=\"servergroup");
                        out.print(groupindex.getKey());
                        out.write("\" value=\"");
                        out.print(server.getIpAddress());
                        out.write("\" />\r\n");
                        out.write("            <lable for=\"lab");
                        out.print(server.hashCode());
                        out.write("\" style=\"font-weight:");
                        out.print(server.isLeader() ? "bold" : "");
                        out.write('"');
                        out.write('>');
                        out.print(server.getIp());
                        out.write("</lable>\r\n");
                        out.write("                  \r\n");
                        out.write("         ");
                    }
                    out.write("\r\n");
                    out.write("      </td>\r\n");
                    out.write("    </tr>\r\n");
                    out.write("   ");
                }
                out.write("\r\n");
                out.write("  </table>\r\n");
                out.write("</div>\r\n");
                out.write("</div>\r\n");
            }
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<fieldset>\r\n");
            out.write(" <legend>设置查询参数</legend>\r\n");
            out.write(" <input type=\"hidden\" name=\"appName\" value=\"");
            out.print(request.getParameter("appName"));
            out.write("\" />\r\n");
            out.write(" <div>\r\n");
            out.write(" <span>query:</span><span class=\"help\"><a target=\"_blank\"\r\n");
            out.write("  href=\"http://wiki.apache.org/solr/SolrQuerySyntax\">Solr查询语法</a></span> <br/>\r\n");
            out.write(" \r\n");
            out.write(" <textarea name=\"q\" cols=\"60\" rows=\"2\">");
            out.print(org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("q"), "*:*"));
            out.write("</textarea> \r\n");
            out.write(" <span style=\"color:#cccccc;font-size:16px\"> example:  *:*,id:478222</span>\r\n");
            out.write(" </div>\r\n");
            out.write(" <div>\r\n");
            out.write("  <span style=\"display:inline-block;width:5em;\">sort:</span>\r\n");
            out.write("  <input type=\"text\" name=\"sort\" value=\"");
            out.print(org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("sort"), ""));
            out.write("\" size=\"40\"/> \r\n");
            out.write("   <span style=\"color:#cccccc;font-size:16px\"> example:  \"create_time desc\"</span>\r\n");
            out.write("  </div>\r\n");
            out.write(" <div><span style=\"display:inline-block;width:5em;\">show rows:</span>\r\n");
            out.write("  <input type=\"text\" name=\"shownum\" value=\"");
            out.print(org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("shownum"), "3"));
            out.write("\" />\r\n");
            out.write(" </div>\r\n");
            out.write(" <!--**************************添加代码--begin******************************** -->\r\n");
            out.write(" <div><span style=\"display:inline-block;width:5em;\">fq:</span>\r\n");
            out.write("  <input type=\"text\" name=\"fq\" placeholder=\"id:[5 TO 12]\" value=\"");
            out.print(org.apache.commons.lang.StringUtils.defaultIfEmpty(request.getParameter("fq"), ""));
            out.write("\" />\r\n");
            out.write("  <span style=\"color:#cccccc;font-size:16px\"> example:  \"id:[1 TO 10]\"</span>\r\n");
            out.write(" </div>\r\n");
            out.write(" <!--**************************添加代码--end********************************** -->\r\n");
            out.write("  <div style=\"position:relative\"><span style=\"display:inline-block;width:3em;\">column:</span>\r\n");
            out.write("  <a id=\"select_column\" href=\"#\" onclick=\"return btnSfieldClick();\">选择</a>\r\n");
            out.write("  ");
            out.write("\r\n");
            out.write("  <div id=\"selectColumn\" style=\"display:none;\">\r\n");
            out.write("    <p>\r\n");
            out.write("      <button onclick=\"return btnHideFieldSelectClick()\">关闭</button>\r\n");
            out.write("      <button id=\"fieldselectall\">全选</button><button  id=\"fieldunselectall\">全不选</button>\r\n");
            out.write("    </p>\r\n");
            out.write("     <div>\r\n");
            out.write("  ");
            List<com.qlangtech.tis.solrdao.pojo.PSchemaField> slist = (List<com.qlangtech.tis.solrdao.pojo.PSchemaField>) request.getAttribute("sfields");
            List<String> selectedFields = (List<String>) request.getAttribute("selectedFields");
            if (selectedFields == null) {
                throw new IllegalStateException(" the attr of the key 'selectedFields' can not be null in httprequest");
            }
            for (com.qlangtech.tis.solrdao.pojo.PSchemaField field : slist) {
                if (field.isStored()) {
                    out.write("\r\n");
                    out.write("  <input type=\"checkbox\" name=\"sfields\" ");
                    out.print(selectedFields.contains(field.getName()) ? "checked" : "");
                    out.write(" \r\n");
                    out.write("    id=\"sfields");
                    out.print(field.hashCode());
                    out.write("\"  value=\"");
                    out.print(field.getName());
                    out.write("\" >\r\n");
                    out.write("  <label for=\"sfields");
                    out.print(field.hashCode());
                    out.write('"');
                    out.write('>');
                    out.print(field.getName());
                    out.write("</label>\r\n");
                    out.write("  ");
                }
            }
            out.write("\r\n");
            out.write("     </div>\r\n");
            out.write("\r\n");
            out.write("</div>\r\n");
            out.write("&nbsp;&nbsp;&nbsp;\r\n");
            out.write("<input type=\"checkbox\" value=\"true\" id=\"debugQuery\" name=\"debugQuery\" ");
            out.print(ManageUtils.isTrue("debugQuery") ? "checked" : "");
            out.write(" />\r\n");
            out.write("<label for=\"debugQuery\">debugQuery:</label>\r\n");
            out.write(" ");
            out.write("\r\n");
            out.write(" </div>\r\n");
            out.write(" <p>\r\n");
            out.write("   <input type=\"submit\" style=\"width:100px;height:30px\" onclick=\"\" value=\"query\" />\r\n");
            out.write("   <span id=\"resultcount\"></span>\r\n");
            out.write(" </p>\r\n");
            out.write("</fieldset>\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("</form>\r\n");
            out.write("\r\n");
            out.write("<table style=\"display:none;\" id=\"messagebox\" width=\"100%\" border=\"1\" style=\"table-layout:fixed;\">\r\n");
            out.write("</table>\r\n");
            out.write(" \r\n");
            out.write("\r\n");
            out.write("</body>\r\n");
            out.write("</html>\r\n");
            out.write("\r\n");
            out.write("<script>\r\n");
            out.write("  $(document).ready(function(){\r\n");
            out.write("    \r\n");
            out.write("    $(\"#selectall\").click(function(){\r\n");
            out.write("      $(\"#serverSelectDialog\").find(\"input[type='checkbox']\").attr(\"checked\",true);\r\n");
            out.write("        return false;\r\n");
            out.write("      }\r\n");
            out.write("    );\r\n");
            out.write("    \r\n");
            out.write("   $(\"#fieldselectall\").click(function(){ \r\n");
            out.write("       $(\"#selectColumn\").find(\"input[type='checkbox']\").attr(\"checked\",true);\r\n");
            out.write("        return false;\r\n");
            out.write("   });\r\n");
            out.write("   \r\n");
            out.write("    $(\"#fieldunselectall\").click(function(){     \r\n");
            out.write("       $(\"#selectColumn\").find(\"input[type='checkbox']\").attr(\"checked\",false);\r\n");
            out.write("        return false;\r\n");
            out.write("    });\r\n");
            out.write("    \r\n");
            out.write("   \r\n");
            out.write("    \r\n");
            out.write("    \r\n");
            out.write("    $(\"#unselectall\").click(function(){\r\n");
            out.write("    $(\"#serverSelectDialog\").find(\"input[type='checkbox']\").attr(\"checked\",false);\r\n");
            out.write("      return false;\r\n");
            out.write("    });\r\n");
            out.write("  })\r\n");
            out.write("</script>\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write(" \r\n");
            out.flush();
        } catch (Throwable t) {
            throw new ServletException(t);
        }
    }
}
