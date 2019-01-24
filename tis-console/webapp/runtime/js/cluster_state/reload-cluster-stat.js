
function generateData(){
	
    var data = [];
    for(var i = 0; i < 24; ++i){
       // data.push([Date.monthNames[i], (Math.floor(Math.random() *  11) + 1) * 100]);
         data.push([i, (Math.floor(Math.random() *  11) + 1) * 100]);
    }
    
     data.push([24, 10001]);
    return data;
}

var chartPanel ;

function createStateBlock(blockName,store,title_name,showcolumn){
	chartPanel = new Ext.Panel({
        width: 1400,
        height: 800,
        renderTo: blockName ,
        title: title_name,
       // tbar: [{
       //     text: 'Load new data set',
       //     handler: function(){
       //         store.loadData(generateData());
       //     }
       // }],
        items: {
            xtype: 'linechart',
            store: store,
            yField: showcolumn ,
            xField: 'createTime',
	        url: '/runtime/imgs/charts.swf',
            
            xAxis: new Ext.chart.CategoryAxis({
                title: 'Daily'
            }),
            yAxis: new Ext.chart.NumericAxis({
                title: 'Hits'
            }),
            extraStyle: {
               xAxis: {
                    labelRotation: -30
                }
            }
        }
    });
	}

var store = new Ext.data.JsonStore({
    // store configs
    autoDestroy: true,
    url: "/runtime/cluster_status.ajax?action=cluster_state_collect_action&event_submit_do_collect=y&resulthandler=advance_query_result",
    baseParams:{m:1440},
    idProperty: 'createTime',
    fields: [{name:'createTime'}, {name:'serviceName'}, {name:'qps'}, {name:'requestCount',type:'int'},{name:'docNumber',type:'int'},{name:'avgConsumeTimePerRequest',type:'float'}]
});

Ext.onReady(function(){
   // var store = new Ext.data.ArrayStore({
   //     fields: ['month', 'hits'],
   //     data: generateData()
   // });
   //console.debug(store);
   store.load();
   
  // alert(store.getTotalCount());
   
   //createStateBlock("qps",store,"QPS每秒访问请求数",'qps');
   //createStateBlock("avg_time_per_request",store,"每次请求平均处理时间","avgConsumeTimePerRequest");
   createStateBlock("request",store,"当天请求量分布","requestCount");
   //createStateBlock("docCount",store,"总记录数目","docNumber");
});
// 重新加载
function reload_cluster_state(minute,btn){
if(chartPanel){
 
  chartPanel.setTitle(btn.innerHTML+"的请求分布图")
}
  store.load({params:{m:minute}})
}
