
var StringUtil={
 isEmpty:function(val){
 	if(val==null||val==""){
 		return true;
 	}
 	return false;
 },
 isNotEmpty:function(val){
 	if(val==null||val==""){
 		return false;
 	}
 	return true;
 }
};

function deleteColumn(dom)
{
	$.layer({
		type: 1,  
        area: ['240px', '130px'],
        border: [0],
        closeBtn: [0, true],
        btns: 2,
        btn: ['确定', '取消'],
        title: [
            '字段:'+$("#name_"+dom.split("_")[1]).val(),
            'border:none; background:#387CB7; color:#fff;' 
        ],
        bgcolor: '#eee', 
        page: {
            html: '<div style="padding-left:50px;padding-top:20px;font-size:18px;">是否确认删除？</div>'
        },
        yes: function(index){ 
			deleteColumnAfert(dom);
			layer.close(index);
        },
        no: function(index){
            layer.close(index);
        }
    }); 
}
function deleteColumnAfert(dom){
  var i=parseInt($("#"+dom).attr("id").split("_")[1]);
  var nextBrother = $("#"+dom).nextAll();
  $("#"+dom).remove();
  
  nextBrother.each(function(e){ 
              var  nid = i++;
             
                $("#row-id-"+i).html(nid);
				$("#row-id-"+i).attr({id:"row-id-"+nid});
				$("#row-id-"+nid).parent("td").parent("tr").attr({id:"tr_"+nid});
				$("#name_"+i).attr({id:"name_"+nid,onfocus:"editName("+nid+");"});
				$("#namehidden_"+i).attr({id:"namehidden_"+nid,name:"name_"+nid});
				$("#type_"+i).attr({name:"type_"+nid,onchange:"changeSelectType('"+nid+"');",id:"type_"+nid});
				$("#td_default_"+i).attr({id:"td_default_"+nid});
				$("#default_"+i).attr({id:"default_"+nid,name:"default_"+nid,onfocus:"focusCheckbox("+nid+");"});
				$("#td_search_"+i).attr({id:"td_search_"+nid});
				$("#search_"+i).attr({id:"search_"+nid,name:"search_"+nid,onfocus:"focusCheckbox("+nid+");"});
				$("#td_return_"+i).attr({id:"td_return_"+nid});
				$("#return_"+i).attr({id:"return_"+nid,name:"return_"+nid,onfocus:"focusCheckbox("+nid+");"});
				$("#td_sort_"+i).attr({id:"td_sort_"+nid});
				$("#sort_"+i).attr({id:"sort_"+nid,name:"sort_"+nid,onfocus:"focusCheckbox("+nid+");"});
				$("#td_regular_"+i).attr({id:"td_regular_"+nid});
				$("#analy_"+i).attr({id:"analy_"+nid,onchange:"changeSelectAnaly('"+nid+"');",name:"analy_"+nid});
				$("#del_"+i).attr({id:"del_"+nid,onclick:"deleteColumn('tr_"+nid+"');"});
				$('input[name="metadataorder-'+i+'"]').attr({name:"metadataorder-"+nid});
				$('#mapto_'+i).attr({id:"mapto_"+nid,onchange:"changeMapTo('"+nid+"')"});
				
   });
}
function readTableColumn(data)
{
	if(data!="null")
	{
		for(var i=0;i<data.columns.length;i++)
		{
			var columenid=$('#schemaItems > tr').size()+1;
			var conf = [{ id:columenid,columnName: data.columns[i].columnName}];
			$('#SchemaTemplate').tmpl(conf).appendTo('#schemaItems');
			switch(data.columns[i].type)
			{
				case("char"):;
				case("varchar"):
				case("string"):
					//$("#type_"+columenid).val("string").css({background:"#EBEBE4"});
					$("#type_"+columenid).val("string");
					$("#sort_"+columenid).attr("disabled","disabled");
					break;
				case("tinyint"):
				case("tinyint unsigned"):
				case("bit"):
				case("smallint"):
				case("smallint unsigned"):
				case("mediumint"):
				case("mediumint unsigned"):
				case("int"):
				case("int unsigned"):
				case("tint"):
					$("#type_"+columenid).val("int");
					$("#sort_"+columenid).attr("checked","true");
					$("#analy_"+columenid).attr("disabled","disabled").css({background:"#EBEBE4"});
					break;
				case("bigint"):
				case("bigint unsigned"):
				case("datetime"):
				case("date"):
				case("timestamp"):
				case("time"):
				case("long"):
				case("tlong"):
					$("#type_"+columenid).val("long");
					$("#sort_"+columenid).attr("checked","true");
					$("#analy_"+columenid).attr("disabled","disabled").css({background:"#EBEBE4"});
					break;
				case("float"):
				case("decimal"):
				case("double"):
					$("#type_"+columenid).val("double");
					$("#analy_"+columenid).attr("disabled","disabled").css({background:"#EBEBE4"});
					$("#sort_"+columenid).attr("disabled","disabled");
					break;
				case("tinyblob"):
				case("mediumblob"):
				case("longblob"):
				case("blob"):
				case("mediumtext"):
				case("longtext"):
				case("text"):
					$("#type_"+columenid).val("text");
					$("#sort_"+columenid).attr("disabled","disabled");
					break;
				case("paoding"):
				case("like"):
				case("text_ws"):
					$("#type_"+columenid).val("string");
					$("#analy_"+columenid+" option[value='"+data.columns[i].type+"']").attr("selected",true);
					break;
				case("regular"):
					$("#type_"+columenid).val("string");
					$("#analy_"+columenid+" option[value='"+data.columns[i].type+"']").attr("selected",true);
					$("#td_regular_"+columenid).append("<input name='regular_symbol_"+columenid+"' style='width:80px;' data-placement='top' data-original-title='支持自定义分词方法，例如：该列以标点符号进行分词，那么请输入对应的标点符号' data-toggle='regular-tooltip-"+columenid+"' value='"+data.columns[i].regularSymbol+"'></input");
					break;
				default:
					break;
			}
			$("#select_shareKey").append("<option value='"+data.columns[i].columnName+"'>"+data.columns[i].columnName+"</option>"); 
			$("#select_uniqueKey").append("<option value='"+data.columns[i].columnName+"'>"+data.columns[i].columnName+"</option>");
			if(data.columns[i].stored=="false")
			{
				$("#return_"+columenid).removeAttr("checked");
			}
			if(data.columns[i].indexed=="false")
			{
				$("#search_"+columenid).removeAttr("checked");
			}
			if(data.columns[i].sort=="false")
			{
				$("#sort_"+columenid).removeAttr("checked");
			}
			if(data.columns[i].defaultVal!=null&&data.columns[i].defaultVal!="")
			{
				$("#td_default_"+columenid).children().remove();
				$("#td_default_"+columenid).append("<input name='default_"+columenid+"' onfocus='editDefaultVal("+columenid+");' id='default_"+columenid+"' type='input' placeholder='请输入默认值' value='"+data.columns[i].defaultVal+"'/><a id='del-default' href='#' onclick='deldefault(this);'>x</a>");
			}
			if(data.primaryKe!=null&&$("#name_"+i).val()==data.primaryKey){
				$("#key").attr("src","/jst/img/right_ico.png");
			}
		}
		if(data.primaryKey!=null&&data.primaryKey!="")
		{
			$("#select_uniqueKey option[value='"+data.primaryKey+"']").attr("selected",true);
		}
		if(data.memo!=null)
		{
			$('#history-memo').html("备注："+data.memo);
		}	
		if(data.shareKey!=null)
		{
			$("#select_shareKey option[value='"+data.shareKey+"']").attr("selected",true);
		}
	}
	else
	{
		alert("服务端出错");
	}
}
function adddefault(dom){
	var id = dom.parentNode.id.split("_")[2];
	$("#td_default_"+id).children().remove();
	$("#td_default_"+id).append("<input name='default_"+id+"' onfocus='editDefaultVal("+id+");' id='default_"+id+"' type='input' placeholder='请输入默认值' /><a id='del-default' style='cursor:pointer;' onclick='deldefault(this);'>x</a>");   
}
function deldefault(dom){
	var id = dom.parentNode.id.split("_")[2];
	$("#td_default_"+id).children().remove();
	$("#td_default_"+id).append("<a id='add-default' style='cursor:pointer;' onclick='adddefault(this);'>+</a>");
}
function addRow()
{

	var columnid=parseInt($('#fieldlistbody > tr').last().attr("id").split("_")[1])+1;
	var conf = [{ id:columnid,columnName: "请输入字段名称"
	            ,'fieldtypes': ['请选择'].concat(fieldtypes)
	            ,'tokenerTypes':tokentypes}];
	$('#SchemaRowTemplate').tmpl(conf).appendTo('#fieldlistbody');
	$("#name_"+columnid).removeAttr("disabled");
	$("#name_"+columnid).change(function(){
	  $(this).next().val($(this).val());
	});
	$("#type_"+columnid).removeAttr("disabled");
	$("#sort_"+columnid).attr("disabled","disabled");
	$("#nextstep").removeAttr("disabled");
}
function loseEfficacy(url)
{
	$.layer({
		type: 1,   
        area: ['250px', '150px'],
        shade: [0.5, '#000'],  
        border: [0], 
        closeBtn: [0, false],
        btns: 2,
            btn: ['确定', '取消'],
            title: [
                '提示',
               
                'border:none; background:#387CB7; color:#fff;' 
            ],
            bgcolor: '#eee',
            page: {
                html: '<div style="padding-left:20px;padding-top:20px;font-size:16px;">长时间未操作,缓存已经失效,无法继续创建</div>'
            },
            shift: 'top',
            yes: function(index){    	
            	window.location.href=url;
            },
            no: function(index){
            	window.location.href=url;
            }
	});
}
function changeSelectType(id)
{
  var opt = $("#type_"+id).find("option:selected");
  
  var range = 'true' === opt.attr('range') ;
  var split = 'true' === opt.attr('split');
  
  if(range){
     $("#rage_"+id).css("display","inline-block");
     $("#analy_"+id).css("display","none");
    $("#sort_"+id).removeAttr("checked");
	$("#sort_"+id).attr("disabled","disabled");
	//$("#analy_"+id).removeAttr("style");
  }
  
  if(split){
    
     $("#rage_"+id).css("display","none");
     $("#analy_"+id).css("display","inline-block");
  
    $("#sort_"+id).removeAttr("disabled");
	$("#analy_"+id).val("选择分词方法");
	
  }
  
  console.info('range:'+range+",split:"+split);
}
function changeSelectAnaly(id)
{
	if($("#analy_"+id).val()=="regular"){
		$("#td_regular_"+id).append("<input name='regular_symbol_"+id
		  +"' style='width:80px;' data-placement='top' data-original-title='正则分词支持自定义分词方法，示例：如果该列以逗号进行分词请输入\",\" 如果该列以分号进行分词请输入\";\" 以此类推。' data-toggle='regular-tooltip-"+id+"' placeholder='分词表达式'></input");
		$("[data-toggle='regular-tooltip-"+id+"']").tooltip("show");
	}else{
		$("[data-toggle='regular-tooltip-"+id+"']").tooltip("destroy");
		$('input[name=regular_symbol_'+id+']').remove();
	}
}
function editDefaultVal(id)
{
	$("#default_"+id).removeAttr("style");
}
function editName(id)
{
	$("#name_"+id).removeAttr("style");
	if($("#name_"+id).val()=="请输入字段名称")
	{
		$("#name_"+id).val("");
	}
	$("#name_"+id).bind("focusout",function(){
		var length=$('#schemaItems > tr').size();
		for(var i=1;i<=length;i++)
		{
			var reg = /^\w+$/; 
			if(reg.test($("#name_"+id).val()))
			{			
				if(id!=i&&$("#name_"+id).val()==$("#name_"+i).val())
				{
					$("#name_"+id).css("background","#FFD1CE");
					$("#name_"+id).val($("#name_"+id).val()+"(字段名重复)");
					break;
				}
			}
		}
		$("#name_"+id).unbind( "focusout" );
	});
	
}
function searchChange(chk,id){

  if($(chk).attr("checked")){
    $("#sort_"+id).removeAttr("style");
  }else{
    $("#sort_"+id).attr("checked",false);
    $("#sort_"+id).css({'visibility':'hidden'});
  }
  
}

function focusCheckbox(id)
{
	//$("#td_search_"+id).removeAttr("style");
	//$("#td_return_"+id).removeAttr("style");
	//$("#td_sort_"+id).removeAttr("style");
}
function checkSchema()
{
	var reg = /^\w+$/;
	var ajaxSubmit=true;
	if($('#schemaItems > tr').size()==0)
	{
		$("#nextstep").attr("disabled","disabled");
		return ajaxSubmit=false;
	}
	var columnid=parseInt($('#schemaItems > tr').last().attr("id").split("_")[1]);
	for(var i=1;i<=columnid;i++)
	{
		if(!reg.test($("#name_"+i).val()))
		{
			ajaxSubmit=false;
			$("#name_"+i).css("background","#FFD1CE");
		}
		if($("#name_"+i).length==1)
		{
			if(!$("#search_"+i).attr("checked")&!$("#return_"+i).attr("checked"))
			{
				ajaxSubmit=false;
				$("#td_search_"+i).css("background","#FFD1CE");
				$("#td_return_"+i).css("background","#FFD1CE");
			}
			switch($("#type_"+i).val())
			{
				case("string"):
				case("text"):
					if($("#default_"+i).val()!=undefined){
						$("#default_"+i).val($("#default_"+i).val().replace(/(^\s*)|(\s*$)/g,""));
						if(!/^([\u4E00-\u9FA5]|[A-Za-z0-9]|[,\;\:"'!])*$/.test($("#default_"+i).val())){
							ajaxSubmit=false;
							$("#default_"+i).css("background","#FFD1CE");
						}
					}
					break;
				case("int"):
				case("long"):
					if($("#default_"+i).val()!=undefined){
						if(!/^(0|[1-9][0-9]*)$/.test($("#default_"+i).val())){
							ajaxSubmit=false;
							$("#default_"+i).css("background","#FFD1CE");
						}
					}
					break;
				case("double"):
					if($("#default_"+i).val()!=undefined){
						if(!/^\d+(\.\d+)?$/.test($("#default_"+i).val())){
							ajaxSubmit=false;
							$("#default_"+i).css("background","#FFD1CE");
						}
					}
					break;
			}
		}
	}

	if($("#select_shareKey").val()=="null")
	{
		ajaxSubmit=false;
		$("#shareKey_uniqueKey").attr("style","margin-left:40px;border:1px solid #D9534F;").fadeOut("slow",function(){
			$("#shareKey_uniqueKey").fadeIn("slow");
			$("#shareKey_uniqueKey").attr("style","margin-left:40px;");
		});
		
	}

	if($("#select_uniqueKey").val()=="null")
	{
		ajaxSubmit=false;
		$("#shareKey_uniqueKey").attr("style","margin-left:40px;border:2px solid #D9534F;").fadeOut("slow",function(){
			$("#shareKey_uniqueKey").fadeIn("slow");
			$("#shareKey_uniqueKey").attr("style","margin-left:40px;");
		});
	}
	return ajaxSubmit;
}

function getSchemaEditPostData(){
    var postform = 	'';
    var idVal = $("#xmleditnav li.active").attr("id");
  
    if(idVal == 'xmleditadvance'){
         postform += "&content="+editor.getValue();
    }else if(idVal == 'xmleditcommon'){
         var fieldcount=$('#fieldlistbody > tr').size();
         postform +="&"+$("#schemaForm").serialize()+"&fieldcount="+ fieldcount;
    }
    
    return postform;
}

function submitSchema(indexname,instance)
{
	
	if($("#select_shareKey").val()=="null"||$("#select_shareKey").val()=="")
	{
		layer.msg("未选择数据分组键", 2, -1);
		return;
		
	}

	if($("#select_uniqueKey").val()=="null"||$("#select_uniqueKey").val()=="")
	{
		layer.msg("未选择uniqueKey", 2, -1);
		return;
	}
	
	//if(checkSchema())
	//{
		//var loadi = layer.load("表单校验中...", 0);	
		var modifydatasource=$("#modifydatasource").val();
		var aid = $("#aid").val();
		
		
		$("[disabled='disabled']").removeAttr("disabled");
	
	var postform = getSchemaEditPostData();
	
    
	TIS.ajax({
	 url:"/jst/ccccc.ajax?event_submit_do_submit_schema=y&action=schema_create_process_acction&instance="+instance,
	 data:postform,
	 success:function(data,textStatus){
	   location.href="/jst/confirmSchema.htm?tasktoken="+TIS.tasktoken;
	 }
	});
}
function ifreamWindow(id,indexname,type,connect,port)
{
	isClose=false;
	$.layer({
        type: 2,
        title: '',
        maxmin: false,
        shadeClose: false, 
        area : ['350px' , '250px'],
		fix: true,
        offset : ['150px', ''],
        iframe: {src: '/jst/login.htm?indexname='+indexname+'&connect='+connect+'&port='+port+'&type='+type+'&id='+id+"&tasktoken="+TIS.tasktoken},
        close:function(index){
        	isClose=true;
        },
        end: function(){
        	if($('#iframe-code').val()=="300"&&!isClose){
        		layer.msg("用户名密码错误",2,function(){
        			$('#iframe-code').val("");
        			ifreamWindow(id,indexname);
        		});	
        	}
        }
    });
    layer.getChildFrame;
	$("#"+id).children("li").remove();
}
function dataBaseSelected(rdsid,dbname,indexname,type,connect,port,token)
{
	var id=rdsid+"-"+dbname;
	//var loadi = layer.load("加载中...", 0);
	TIS.ajax({
		type:"POST",
		dataType:"json",
		url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_check_db_authority=y&action=jst_action",
		data:"rdsid="+rdsid+"&db_name="+dbname+"&indexname="+indexname,
		success:function(data,textStatus)
		{	
			//layer.close(loadi);
			switch(data.code)
			{
				case "200":
							var conf={rdsid:rdsid,dbname:dbname,pre:id,tables:data.extra.tables,host:data.extra.host};
							for(var i=0;i<data.extra.tables.length;i++)
							{
								$("#left--li--"+rdsid+"--"+dbname+"--"+data.extra.tables[i].tablename).remove();
							}
							var appendToid="#"+rdsid+"--"+dbname;
							$('#tablesTemplate').tmpl(conf).appendTo(appendToid);
							$('span[name="database--'+rdsid+'--'+dbname+'"]').removeAttr("onclick");
							break;
				case "300":
							layer.msg(data.desc+" code:"+data.code, 2, function(){
								ifreamWindow(rdsid+"--"+dbname,indexname,type,connect,port);
							});
							break;
				case "400": layer.msg(data.desc+" code:"+data.code, 2, -1);
							break;
				default:layer.msg('缓存失效，请刷新本页面', 2,-1);break;
			}
		},
		error:function(XMLHttpRequest,textStatus,errorThrown){
			layer.close(loadi);
			layer.msg(textStatus, 2,-1);
		},
		complete:function(XMLHttpRequest, textStatus){
			;
		}
	});

}
function tableSelected(id)
{
	if($("#"+id).attr("style")!=null)
	{
		$("#"+id).removeAttr("style");
	}
	else
	{
		$("#"+id).css("background","#3D84C1");
	}
}
function addTables()
{
	var tablelist = $("li[style]");
	if(tablelist.size()<1)
	{
		$("#add-table").popover();
		setTimeout(function () { 
			$('.popover-content').hide();
			$('.arrow').hide();
    	}, 4000);
	}
	else
	{
		for(var i=0; i<tablelist.size();i++)
		{
			var id=tablelist.get(i).id.slice(4);
			if($("#right"+id).length==0)
			{
				$("#right_tables").append(tablelist.get(i));
				$("#"+tablelist.get(i).id).attr("id","right"+id);
				$("span[name=database--"+id.split("--")[1]+"]").removeAttr("onclick");
			}
		}
	}
}
function removeTables()
{
	var tablelist = $("#right_tables").children("li[style]");
	for(var i=0;i<tablelist.size();i++)
	{
		var id= tablelist.get(i).id.slice(5);
		if($("#left"+id).length==0)
		{
			$("ul[id="+tablelist.get(i).id.split("--")[2]+"--"+tablelist.get(i).id.split("--")[3]+"]").append(tablelist.get(i));
			$("#"+tablelist.get(i).id).attr("id","left"+id);
		}
		else
		{
			$("#right"+id).remove();
		}
		
	}
}

function submitRdsForm(indexname,instance,aid){
	var modifydatasource=$("#modifydatasource").val();
	var aid = $("#aid").val();
	if($("#right_tables>li").length>0)
	{	
		var loadi = layer.load("库表校验中...", 0);
		jQuery.ajax({
			type:"POST",
			dataType:"json",
			url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_submit_data_source=y&action=tair_action&instance="+instance+"&aid="+aid,
			data:$("#dataSourceInfoForm").serialize()+"&tasktoken="+window.document.tasktoken,
			success:function(data,textStatus)
			{
				layer.close(loadi);
				if(data==true)
				{
					if(modifydatasource=="1"){
						window.location.href="defSchema.htm?aid="+aid+"&instance="+instance+"&modifydatasource="+modifydatasource+"&tasktoken="+TIS.tasktoken;
					}else{
						window.location.href="defSchema.htm?indexname="+indexname+"&instance="+instance+"&tasktoken="+TIS.tasktoken;
					}		
				}
				else
				{
					layer.msg('数据库表之间列信息不一致，请重新选择数据表', 3,-1);
				}
			},
			error:function(XMLHttpRequest,textStatus,errorThrown){
				layer.close(loadi);
				alert(textStatus);
			},
			complete:function(XMLHttpRequest, textStatus){
				;
			}
		});
		
	}
	else
	{
		layer.msg("没有选择任何数据表...", 2, 0);
	}
}

function checkdailyformat(dailyformat)
{
	if(dailyformat.length!=14){
		return false;
	}
	if( dailyformat.substr(0, 4)!="yyyy" && ( dailyformat.substr(0, 2)!="20" || !/[0-9]/.test(dailyformat.substr(2, 1)) || !/[0-9]/.test(dailyformat.substr(3, 1)) ) ){
		return false;
	}
	if( dailyformat.substr(4, 2)!="MM" && ( !/[0-1]/.test(dailyformat.substr(4, 1)) || !/[0-9]/.test(dailyformat.substr(5, 1)) ) )
	{
		return false;
	}
	if( dailyformat.substr(6, 2)!="dd" && ( !/[0-3]/.test(dailyformat.substr(6, 1)) || !/[0-9]/.test(dailyformat.substr(7, 1)) ) )
	{
		return false;
	}
	if( dailyformat.substr(8, 2)!="HH" && ( !/[0-2]/.test(dailyformat.substr(8, 1)) || !/[0-9]/.test(dailyformat.substr(9, 1)) ) )
	{
		return false;
	}
	if( dailyformat.substr(10, 2)!="mm" && ( !/[0-5]/.test(dailyformat.substr(10, 1)) || !/[0-9]/.test(dailyformat.substr(11, 1)) ) )
	{
		return false;
	}
	if( dailyformat.substr(12, 2)!="ss" && ( !/[0-5]/.test(dailyformat.substr(12, 1)) || !/[0-9]/.test(dailyformat.substr(13, 1)) ) )
	{
		return false;
	}
	return true;
}









function schemaManage(indexname)
{
	jQuery.ajax({
		type:"POST",
		dataType:"json",
		url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_read_schema=y&action=schema_manage_action",
		data:{indexname:indexname},
		success:function(data,textStatus)
		{
			switch(data.code)
			{
				case 200:
							readTableColumn(data.extra);
							break;
				case 300: 
				case 400: 
				case 500: layer.msg(data.desc+" code:"+data.code, 2, -1);break;
				default: layer.msg('未知错误', 2,-1);break;
			}
					
		},
		error:function(XMLHttpRequest,textStatus,errorThrown){
			layer.msg(textStatus, 2, -1);
		},
		complete:function(XMLHttpRequest, textStatus){
			;
		}
	});
}
function doRebuild(index)
{
	layer.close(index);
    $("[disabled='disabled']").removeAttr("disabled");
	var fieldcount=$('#fieldlistbody > tr').size();
	
	var postParm =getSchemaEditPostData();
   
    TIS.ajax({
    	async:true,
    	dataType:"json",
    	timeout : 5000,
    	url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_modifed_schema=y&action=schema_action&fieldcount="+fieldcount,
    	data:postParm ,
    	success:function(data)
        {
        	
        	switch(data.code)
        	{
        		case "200":
	    				realtimeLogInit(data.taskid);
        				break;
        		case "300": 
        		case "400": 
        		case "500": layer.msg(data.reason+" code:"+data.code, 2, function(){location.reload();});break;
        		default: layer.msg('未知错误', 2,function(){location.reload();});break;
        	}
        					
        }
    });
}


function editSchema(aid){

    $("[disabled='disabled']").removeAttr("disabled");
	var fieldcount=$('#fieldlistbody > tr').size();
	
	var postParm =getSchemaEditPostData();
   
    TIS.ajax({
    	async:true,
    	dataType:"json",
    	timeout : 5000,
    	url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_edit_schema=y&action=schema_action&fieldcount="+fieldcount,
    	data:postParm ,
    	success:function(data)
        {
        	switch(data.code)
        	{
        		case "200":
        				layer.msg("成功", 1, 1);
        				schemaManagePage(aid,'');
        				break;
        		case "300": 
        		case "400": 
        		case "500": layer.msg(data.reason+" code:"+data.code, 2, function(){location.reload();});break;
        		default: layer.msg('未知错误', 2,function(){location.reload();});break;
        	}				
        }
    });

}



function xorColumns(data)
{
	var isModif=false;
	for(var i=0;i<data.columns.length;i++)
	{
		var j=1;
		for(;j<=$('#schemaItems > tr').length;j++)
		{
			if(data.columns[i].columnName==$('#name_'+j).attr("value"))
			{
				break;
			}
		}
		if(j>$('#schemaItems > tr').length)
		{
			var columenid=$('#schemaItems > tr').length+1;
			var conf = [{ id:columenid,columnName: data.columns[i].columnName}];
			$('#SchemaTemplate').tmpl(conf).appendTo('#schemaItems');
			$("#tr_"+columenid+" td:first-child").append("<img src=\"/jst/img/new.png\" hight=\"10px\" width=\"20px\"></img>");
			if(data.columns[i].type=="char" || data.columns[i].type=="varchar" || data.columns[i].type=="string" || data.columns[i].type=="tinyblob" || data.columns[i].type=="mediumblob" || data.columns[i].type=="longblob" || data.columns[i].type=="blob" || data.columns[i].type=="mediumtext" || data.columns[i].type=="longtext" || data.columns[i].type=="text" )
			{
				$("#sort_"+columenid).attr("disabled","disabled");
			}
			isModif=true;
		}
	}
	if(isModif)
	{
		layer.msg("同步成功", 1, 1);	
	}
	else
	{
		layer.msg("同步成功,数据库未添加新字段", 1, 1);
	}
	
}





function getAppStatus(aid){
	jQuery.ajax({
		type:"POST",
		dataType:"json",
		url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_get_app_status=y&action=appManage_action&aid="+aid,
		success:function(data,textStatus)
		{
			return data;
		},
		error:function(XMLHttpRequest,textStatus,errorThrown){
			;
		},
		complete:function(XMLHttpRequest, textStatus){
			;
		}
	});
}
function activateApp(aid,quota,qps,test_max_doc,instance){
	var loadi = layer.load("激活中，请稍等...", 0);
	jQuery.ajax({
		type:"POST",
		dataType:"json",
		timeout : 30000,
		url:"/jst/ccccc.ajax?resulthandler=advance_query_result&event_submit_do_activate_app=y&action=appManage_action&aid="+aid,
		data:{quota:quota,qps:qps,test_max_doc:test_max_doc},
		success:function(data,textStatus)
		{
			layer.close(loadi);
			if(data.code=="200"){
				
				invalidAppList(instance);
				
				realtimeLogInit(data.taskid);
				$("#close").attr("onclick","turnToPageAndRemoveTimeOut(" + data.aid + ")"); 
				$("#close2").attr("onclick","turnToPageAndRemoveTimeOut(" + data.aid + ")"); 
			}else{
				layer.alert("[code:"+data.code+"] "+data.desc,-1, "出错了")
			}
		},
		error:function(XMLHttpRequest,textStatus,errorThrown){
			$(".activate-app-body").modal('hide');
			layer.close(loadi);
			alert("触发中心响应超时,服务终止");
		},
		complete:function(XMLHttpRequest, textStatus){
			;
		}
	});
}



function initSchemaEditTabClick(){
 
 $("#xmleditnav li a").click(function(){
 
    var lielmt =$(this).parent();
    if(!lielmt.hasClass("active")){
        
       var idVal = lielmt.attr("id");
       if(idVal == 'xmleditadvance'){
         loadSchemaXmlView($(this).attr('href'));
       }else if(idVal == 'xmleditcommon'){
         loadSchemaCommonView('');
       } 
    }
  return false;
  });
}

var fieldtypes = null;
var fieldtypesMap = {};
var tokentypes = null;
var editor = null;
 // click xiaobai button
function loadSchemaCommonView(href){  
    TIS.ajax({
     url:"/runtime/schemaManage.ajax?aid=$aid&event_submit_do_modifed_context2_cache_xml=y&action=schema_action&"+Math.random(),
     data:"content="+editor.getValue()+"&tasktoken="+window.document.tasktoken,
     async:true,
     success:function(d){
       //loadCommonView();
       showXiaobaiFields(d);
      
     }
    });
}

function showXiaobaiFields(d){
        data = d.bizresult;
        fieldtypes = data.fieldtypes;
        
        var tt = null;
        for(var i =0; i<fieldtypes.length ; i++ ){
         tt = fieldtypes[i];
         fieldtypesMap[tt.name] = tt  ;
        }
        
       // console.debug(fieldtypesMap);
        
        data.tokenerTypes = fieldtypesMap.string.tokensType;
        
        console.debug( data.tokenerTypes);
         
        tokentypes = data.tokenerTypes;
        $('#edit-btn').removeAttr("disabled");
        $('#rebuild-btn').removeAttr("disabled")
        $('#sync').removeAttr("disabled")
        $("#formplate").empty();
        $('#SchemaTemplate').tmpl(data, tmplSpec).appendTo('#formplate');
        $("#xmleditnav li").removeClass("active");
        $("#xmleditcommon").addClass("active");
        for(var i=0;i<data.fields.length;i++){
        	var defaultVal = $("#td_default_"+(i+1)).children().val();
        	if(defaultVal==null || defaultVal==""){
        		$("#td_default_"+(i+1)).children().remove();
        		$("#td_default_"+(i+1)).append("<a id=\"add-default\" style=\"cursor:pointer;\" onclick=\"adddefault(this);\">+</a>");
        	}
        	
        	                                        
          	if(StringUtil.isNotEmpty(data.fields[i].regularSymbol)){
        		var id = data.fields[i].id;
        		$("#td_regular_"+id).append("<input name='regular_symbol_"+id+"' style='width:80px;' data-placement='top' data-original-title='正则分词支持自定义分词方法，示例：如果该列以逗号进行分词请输入\",\" 如果该列以分号进行分词请输入\";\" 以此类推。' data-toggle='tooltip' value='"+data.fields[i].regularSymbol+"' ></input");
        		$("[data-toggle='tooltip']").tooltip();
        		$("#td_regular_"+id).attr("style","width:200px");
        	}
        	
        }
}

var tmplSpec ={
            isSelect: function (val1,val2) {
            	if("regular"==val1&&"string"==val2){
            		return "selected";
            	}
                return (val1 == val2)?"selected":"";
            },
            checked: function (isTrue) {
                return (isTrue)?"checked='checked'":"";
            },
            shallShow: function( fieldtype	){
                return ( ('string' == fieldtype) || ("regular" == fieldtype) )?"":"visibility:hidden";
            },
            shallSortShow:function(indexed){
              return (indexed)?"":"visibility:hidden";
            }
        };

function loadCommonView(	){
   var action = 'schema_action';
 
   TIS.ajax({                             
      url:"/runtime/schemaManage.ajax?aid=$aid&event_submit_do_get_fields=y&action="+ action,
      type:'POST',
      async:false,
      data: $("#schemaForm").serialize(),
      success: function(d) {
      
        showXiaobaiFields(d);
      }
    });
    
   
}

function loadSchemaXmlView(href){
    //var loadi = layer.load("加载中...", 0);
    $("#xmleditnav li").removeClass("active");
    var tag = $("#schemaForm");
    
     var success = false;
    console.debug(tag.serialize());
    TIS.ajax({
      url:"/runtime/schemaManage.ajax?event_submit_do_modifed_context2_cache_common=y&action=schema_action&"+Math.random(),
      type:'POST',
      async:false,
      data: tag.serialize()+"&tasktoken="+window.document.tasktoken+"&fieldcount="+$('#fieldlistbody > tr').size(),
      success: function(data) { 
         success = true;
         $('#edit-btn').attr("disabled","disabled");
         $('#rebuild-btn').attr("disabled","disabled");
         $('#sync').attr("disabled","disabled");
          $("#xmleditadvance").addClass("active");
          updateXmlEditarea(data);
      }
    });
    
  
   if(!success){
     return ;
   }
   

 
 
    return false;
}

function updateXmlEditarea(data){
var tag = $("#schemaForm");
        $("#formplate")[0].innerHTML=(
          "<textarea id='code' class='col-md-12'  name='content'>"
           +data.bizresult.schema+"</textarea>");
           
        var area = tag.find("textarea");
        
        editor = CodeMirror.fromTextArea(area[0], {
           mode: {name: "xml", alignCDATA: true},
           lineNumbers: true
        });
         
        area.css("height",area[0].scrollHeight+20);
}

//倒计时
function CountDown() {
	$("#rebuild-btn").attr("disabled", true);
    $("#rebuild-btn").html("请 " + remaintime + " 秒后再操作");
    if (remaintime == 0) {
		$("#rebuild-btn").html("修改schema并重新构建索引").removeAttr("disabled");
		clearInterval(countdown);
	}
	remaintime--;
}


