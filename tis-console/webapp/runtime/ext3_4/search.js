var searchKey = searchKey;
var searchType = searchType;
var SEARCH_CONSOLE_MANAGER = null;
Dbmis.SearchConsoleManager = function(i_searchType , i_searchKey) {
    var manager = this;
    SEARCH_CONSOLE_MANAGER = this;
    var structureConsole = new Dbmis.SearchTableStructureConsole(manager);
    var dbPanel = new Dbmis.SearchDataBase(i_searchType , i_searchType , manager , structureConsole);
   // var defaultConsole = new Dbmis.SQLConsole("", "", "", "" , "1");
   // defaultConsole.title = "SQL Plus";
   // defaultConsole.closable = false;
    //if (user_nick != 'Guest') {
    //    defaultConsole.fillWorkspaceText();
   // }
    
    var welcomePanel = new Ext.Panel( { 
      id:"welcomePanel",
      collapsible:false,
      title:"欢迎页面",
      contentEl:"bodycontent",
      margins: "5 5 5 5",
      bodyStyle: "background-color:#D4E4F9;",
      border: false
     });
        
    Dbmis.SearchConsoleManager.superclass.constructor.call(this, {
        id:'console_manager',
        activeTab: 0,
        region:'center',
        resizeTabs:true,
        deferredRender:false,
        minTabWidth: 160,
        tabWidth:160,
        enableTabScroll:true,
        defaults: {
            autoScroll:true
        },
        items: [
           welcomePanel
           // dbPanel , structureConsole, defaultConsole
           // structureConsole,
           // defaultConsole
          
        ]
    });
    
    // refresh every tab view in main panel
    this.refreshAllTab = function(){
   // (this.items.items.length);
  // alert("ddd");
   $("iframe").each(function(){
     // alert($(this).attr("src") );
   this.src = this.src;
   });
   
    }


    this.resetTypeKey = function(v_searchType , v_searchKey) {
       // manager.setActiveTab(defaultConsole);
    }; 
    
     
     
    manager.resetTypeKey(i_searchType , i_searchKey);


    /**
     * 添加SQL Console
     * @param {int} dbId db id
     * @param {int} dbName db name
     */
    this.addConsole = function(dbId, dbName , dbType) {
        var vDbtype = (dbType == 0 ? 1 : dbType);
        manager.normalOpt(new Dbmis.SQLConsole(dbId, dbName , dbType ,'' , vDbtype));
    };

    this.addConsole2 = function(dbId , dbName , dbType ,tabName) {
        var vDbtype = (dbType == 0 ? 1 : dbType);
        manager.normalOpt(new Dbmis.SQLConsole(dbId, dbName , dbType , tabName , vDbtype));
    };

    /**
     *打开project panel
     */
    this.addProjectPanel = function() {
        manager.normalOpt(new Dbmis.ProjectsDisplayPanel());
    };

    /**
     * 获取TableStructureConsole
     * @type Dbmis.TableStructureConsole
     */
    this.getTableStructureConsole = function() {
        return structureConsole;
    };

    this.addRecycleBin = function(v) {
        manager.normalOpt(new Dbmis.RecycleBinItems(v));
    };

    this.addAllUserItem = function() {
        manager.normalOpt(new Dbmis.AllCreateTab());
    };

    this.addDispOp = function() {
        manager.normalOpt(new Dbmis.DispOptionPanel());
    };

    this.normalOpt = function(panel) {
        panel.closable = true;
        manager.add(panel);
        manager.setActiveTab(panel);
    };
};
Ext.extend(Dbmis.SearchConsoleManager, Ext.TabPanel);

Dbmis.SearchTableStructureConsole = function (manager) {
    var structureConsole = this;
    var tableGrid = new Dbmis.SearchTableGridPanel(manager);
    tableGrid.region = "center";
    var columnGridPanel = new Dbmis.TableColumnGridPanel();
    var indexGridPanel = new Dbmis.TableIndexGridPanel();
    var partitionPanel = new Dbmis.TablePartitionGridPanel();
    var extendPanel = new Dbmis.TableExtendInfo();
    var tabPanel = new Ext.TabPanel({
        region:'south',
        activeTab:0,
        resizeTabs:true,
        deferredRender:false,
        minTabWidth: 160,
        tabWidth:240,
        height:240,
        split:true,
        enableTabScroll:true,
        defaults: {
            autoScroll:true
        },
        items: [
            columnGridPanel, indexGridPanel, partitionPanel, extendPanel
        ]
    });
    var tableNameV = new Ext.form.TextField({width:180,value:''});
    var dbNameV = new Ext.form.TextField({width:160,value:''});
    var isWatchV = createNormalComm3(140 , [['' , '所有'] , ['1' , '我关注的']] , '' , '');
    var searchToolBar = new Ext.Toolbar(['-' ,{text:'表名:'} , '-' ,tableNameV , '-', {text:'库名'} , '-' ,dbNameV , '-',
        {text:'是否关注'} , '-' , isWatchV , '-',{text:'搜索',iconCls:'tableIndexStructure',handler:function() {
            structureConsole.refreshTab();
        }}, '-']);
    Dbmis.TableStructureConsole.superclass.constructor.call(this, {
        layout:'border',
        title:'表结构',
        iconCls:'tableStructure',
        items: [tableGrid,tabPanel],
        tbar: [
            {
                tooltip:'编辑表的描述',
                text:'编辑表',
                iconCls:'editTable',
                handler: function() {
                    if (user_nick == 'Guest' && !testEnv) {
                        Ext.Msg.alert("Error", "登录后才能进行表的描述更新！");
                        return;
                    }
                    var table = structureConsole.getSelectedTable();
                    if (table) {
                        Ext.Msg.show({
                            title: '更新表的描述（请分行写描述）',
                            msg: '请输入表（' + table.name + '）描述:',
                            minWidth: 480,
                            defaultTextHeight:240,
                            buttons: Ext.MessageBox.OKCANCEL,
                            multiline: true,
                            value:table.description,
                            fn: function(btn, text) {
                                if (btn == 'ok' && text != "") {
                                    Ext.Ajax.request({
                                        url : '/table/updateTableComment.jsn?_input_charset=UTF-8' ,
                                        params : { tableId : table.id, description:text},
                                        method: 'POST',
                                        success: function (result, request) {
                                            table.description = text;
                                            tableGrid.store.reload();
                                        },
                                        failure: function(response, opts) {
                                            alert(response.responseText);
                                        }
                                    });
                                    structureConsole.getSelectedTableModel().set("description", text);
                                }
                            },
                            icon: Ext.MessageBox.INFO
                        });
                    } else {
                        Ext.Msg.alert('错误', "请选择某一表进行编辑！");
                    }
                }
            },'-',{
                text:'代码生成器',
                tooltip:'这里可以为开发团队生成常用的各类自动代码',
                iconCls:'userIcon',
                handler: function() {
                   if(rowTableId != null) {
                        var tab = tableGrid.getSelectionModel().getSelected().data;
                        showCoding(2 , tab.name , 0 , rowTableId , tab.description , tab.dbId);
                   }else {
                        show2('提示','请先选择一个表，再执行代码生成操作。');
                   }
                }
            },'-',{
                text:'创建语句',
                tooltip:'选择一个表后点击这里导出创建语句',
                iconCls:'moveIcon',
                handler:function() {
                    if(rowTableId != null) {
                        var tab = tableGrid.getSelectionModel().getSelected().data;
                        structureConsole.explortCreateSql(tab.name);
                    }else {
                        show2('提示','请先选择一个表，再执行创建语句导出。');
                    }
                }
            },'-'
        ],listeners:{
            'render':function() {
                searchToolBar.render(structureConsole.tbar);
            }
        }
    });
    var rowTableId = null;
    tableGrid.getSelectionModel().on("selectionchange", function(sm) {
        if (sm.getSelected()) {
            var table = sm.getSelected().data;
            rowTableId = table.id;
            columnGridPanel.reloadByTableId(table.id, table.name,table.dbId);
            indexGridPanel.reloadByTableId(table.id, table.name);
            partitionPanel.reloadByVirtualId(table.dbId, table.name, table.virtualId);
            extendPanel.loadInfoByTable(table , tableGrid);
        }
    });

    this.explortCreateSql = function(name) {
        var content = new Ext.form.TextArea({region:'center'});
        Ext.Ajax.request({
            url : '/table/getCreateTabSql.jsn?_input_charset=UTF-8',
            params:{tableId:rowTableId,tableName:name},
            success: function (result, request) {
                content.setValue(result.responseText);
            },failure: function(response, opts) {
                alert(result.responseText);
            }
        });
        var win = new Ext.Window({
            title:'表:' + name + ' 创建语句，付：<font color="red">该创建语句仅提供参考</font>',iconCls:'showIcon',
            height:550, width:900,layout:'border',maximizable:true,
            items:[content]
        });
        win.show();
    };
    /**
     * 获取当前选中的表，返回数据
     * @type Table
     */
    this.getSelectedTable = function() {
        var sm = tableGrid.getSelectionModel();
        if (sm.getSelected()) {
            return sm.getSelected().data;
        } else {
            return null;
        }
    };

    /**
     * 获取当前选中的表，返回模型
     * @type Ext.data.Record
     */
    this.getSelectedTableModel = function() {
        var sm = tableGrid.getSelectionModel();
        if (sm.getSelected()) {
            return sm.getSelected();
        } else {
            return null;
        }
    };

    /**
     * 获取当前选中的列，返回数据
     * @type Column
     */
    this.getSelectedColumn = function() {
        var sm = columnGridPanel.getSelectionModel();
        if (sm.getSelected()) {
            return sm.getSelected().data;
        } else {
            return null;
        }
    };
    /**
     * 获取当前选中的列，返回模型
     * @type Column
     */
    this.getSelectedColumnModel = function() {
        var sm = columnGridPanel.getSelectionModel();
        if (sm.getSelected()) {
            return sm.getSelected();
        } else {
            return null;
        }
    };
    /**
     * 获取table grid
     * @type Dbmis.TableGridPanel
     */
    this.getTableGrid = function() {
        return tableGrid;
    };

    this.resetByDb = function(dbName) {
        dbNameV.setValue(dbName);
        tableNameV.setValue('');
        isWatchV.setValue('');
    };

    this.refreshThis = function() {
        tableGrid.refresh(filerString(tableNameV.getValue()) , filerString(dbNameV.getValue()) , isWatchV.getValue());
    };

    this.refreshTab = function() {
        dbNameV.setValue(null);
        if(searchType == 1) {
            tableNameV.setValue(searchKey);
            isWatchV.setValue('');
        }else if(searchType == 5) {
            isWatchV.setValue(1);
            tableNameV.setValue(null)
        }
        structureConsole.refreshThis();
    };
    regiestSpecialKey(structureConsole.refreshThis , [tableNameV , dbNameV]);
    regiestSelect(structureConsole.refreshThis , [isWatchV]);
};
Ext.extend(Dbmis.SearchTableStructureConsole, Ext.Panel);
var SEARCH_TABLE_GRID_PANEL;
Dbmis.SearchTableGridPanel = function (manager) {
    var panelObj = this;
    SEARCH_TABLE_GRID_PANEL = this;
    var sm = new Ext.grid.RowSelectionModel({singleSelect:true});
    this.store = new Ext.data.JsonStore({
        url:'/table/querySearch.jsn?_input_charset=UTF-8',
        id:'id',
        fields:[
            {
                name:'id'
            },
            {
                name:'dbId'
            },
            {
                name:'name',
                type:'string'
            },
            {
                name:'appName',
                type:'string'
            },
            {
                name:'devOwner',
                type:'string'
            },
            {
                name:'dbaOwner',
                type:'string'
            },
            {
                name:'dwOwner',
                type:'string'
            },
            {
                name:'seOwner',
                type:'string'
            },
            {
                name:'rowCount'
            },
            {
                name:'virtualId'
            },
            {
                name:'storeCapacity'
            },
            {
                name:'flag'
            },
            {
                name:'description',
                type:'string'
            },
            {
                name:'dbName',
                type:'string'
            },
            {
                name:'updatedAt',
                type:'string'
            },
            {
                name:'dbType'
            },
            {
                name:'isUserDefine'
            },
            {
                name:'tnsName'
            },
            {
                name:'realDbName'
            },
            {
                name:'jdbcUrl'
            }
        ],
        autoLoad:false
    });
    this.columns = [
        new Ext.grid.RowNumberer(),
        {
            header:'操作',
            width:140,
            dataIndex:'isUserDefine',
            sortable:false,
            renderer:function (v) {
                var back = '';
                if (v) {
                    back = '<a href="javascript:SEARCH_TABLE_GRID_PANEL.watchTable(1)" style="font-weight: bold; color:green;text-decoration: underline">取消关注</a>';
                } else {
                    back = '<a href="javascript:SEARCH_TABLE_GRID_PANEL.watchTable(2)" style="font-weight: bold; color:red;text-decoration: underline">关注</a>';
                }
                back += ' <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:SEARCH_TABLE_GRID_PANEL.refreshTab()"/>同步</a>'
                    + ' <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:SEARCH_TABLE_GRID_PANEL.toBeidou()">性能</a>'
                    + ' <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:SEARCH_TABLE_GRID_PANEL.toTddl()">TDDL</a>';
                return back;
            }
        },
        {
            header:'表名',
            width:150,
            dataIndex:'name',
            sortable:true
        },
        {
            header:'数据库',
            width:120,
            dataIndex:'dbName',
            sortable:true
        }, {
            header:'JDBC路径',
            width:300,
            dataIndex:'jdbcUrl',
            sortable:false
        }, {
            header:'容量(M)',
            width:70,
            dataIndex:'storeCapacity',
            sortable:true
        },
        {
            header:'CSV结构',
            width:60,
            dataIndex:'id',
            sortable:false,
            renderer:function (val) {
                return '<a href="/table/structurePrint.htm?id=' + val + '" target="_blank">下载</a>';
            }
        },
        {
            header:'描述',
            width:226,
            dataIndex:'description',
            sortable:false
        }
    ];
    Dbmis.SearchTableGridPanel.superclass.constructor.call(this, {
        id:'table_grid',
        sm:sm,
        store:this.store,
        viewConfig:createView('没有找到表!'),
        listeners:{
            'rowdblclick':function (thiz, row, e) {
                var rowObj = panelObj.getStore().getAt(row).data;
                manager.addConsole2(rowObj['dbId'], rowObj['dbName'], rowObj['dbType'], rowObj['name']);
            }
        }
    });

    this.refreshTab = function () {
        var table = panelObj.getSelectionModel().getSelected().data;
        if (table) {
            Ext.Msg.show({
                title: '请修改要同步的表',
                msg: '请输入（'+table.dbName+'）下要修改的表名',
                minWidth: 480,
                defaultTextHeight:240,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                value:table.name,
                fn: function(btn, text) {
                    if (btn == 'ok' && text != "") {
                        Ext.Ajax.request({
                            url:'/project/syncTables.jsn',
                            params:{tnsName:table.tnsName, dbId:table.dbId, tableName:text , tableName2:table.name, virtualId:table.virtualId},
                            success:function (result, request) {
                                show2('提示', '增量同步已经提交，可能需要大概几秒左右的时间进行同步.');
                            }
                        });
                    }
                },
                icon: Ext.MessageBox.INFO
            });
        } else {
            Ext.Msg.alert('错误', "请选择某一表进行编辑！");
        }

    };

    this.toBeidou = function () {
        var table = panelObj.getSelectionModel().getSelected().data;
        Ext.Ajax.request({
            url:'/project/getHostByIp.jsn',
            params:{dbType:table.dbType, jdbcUrl:table.jdbcUrl},
            success:function (result, request) {
                var object = getJson(result.responseText);
                var url = beidou_path + 'mysql/dashboard.php?leftshow=1&hostname=' + object['hostName'] + '&port=' + object['port'];
                if(table.dbType==0){
                    url=beidou_path+'index.php?controller=curves&action=dispbyh&serv='+object['sid']+'&menuid=200';
                }
                window.open(url, 'beidou', pubfeather);
            }
        });
    };
 
    this.watchTable = function (v) {
        var table = panelObj.getSelectionModel().getSelected().data;
        Ext.Ajax.request({
            url:'/user/tableWatch.jsn',
            params:{ tableId:table.id, op:v},
            method:'POST',
            success:function (result, request) {
                var respText = Ext.util.JSON.decode(result.responseText);
                show2('提示', respText['message']);
                panelObj.refresh(tmp_tableName , tmp_dbName , tmp_isWatch);
            }
        });
    };
    var lastLoadStatus = 0;
    var tmp_tableName , tmp_dbName , tmp_isWatch;

    this.refresh = function(tableName , dbName , isWatch) {
        tmp_tableName = tableName;
        tmp_dbName = dbName;
        tmp_isWatch = isWatch;

        panelObj.getStore().load({
           params:{tableName:tmp_tableName , dbName:tmp_dbName , isWatchTable:tmp_isWatch}
        });
    };
};
Ext.extend(Dbmis.SearchTableGridPanel, Ext.grid.GridPanel);

Dbmis.SearchTableStructurePanel = function () {
    Dbmis.SearchTableStructurePanel.superclass.constructor.call(this, {
       title:'搜索表',iconCls:'userIcon'
    });
};
Ext.extend(Dbmis.SearchTableStructurePanel, Ext.Panel);
var SEARCH_DATABASE_PANEL;
Dbmis.SearchDataBase = function(searchType , searchKey , manager , structureConsole) {
    var panelObj = this;
    SEARCH_DATABASE_PANEL = this;
    var objArr = converArr([
        {name:'id',hidden:true},{name:'attention',width:220,header:'关注',renderer:function(v) {
            var result = '';
            if (v) {
                 result = '<a href="javascript:SEARCH_DATABASE_PANEL.attention(1)" class="input_alt1">取消关注</a>';
            } else {
                 result = '<a href="javascript:SEARCH_DATABASE_PANEL.attention(2)" class="input_alt1">关注</a>';
            }
            return result + ' <a href="javascript:SEARCH_DATABASE_PANEL.searchTab()" class="input_alt1">查看表</a> '
                  + '<a href="javascript:SEARCH_DATABASE_PANEL.addNewSqlConsole()" class="input_alt1">SQL查询</a>'
        } },{name:'displayName',width:140 , header:'显示名' , sortable:true},{name:'name',hidden:true},
        {name:'groupName',width:120,header:'分组'},{name:'jdbcUrl',width:340,header:'JDBC URL'},
        {name:'dbType',width:120,header:'类型',renderer:function(v) {
            return getDbTypeNameByTypeId(v)
        }}
    ] , true);
   // this.store = new Ext.data.JsonStore({
   //     url: '/database/dataBaseSearch.jsn?_input_charset=UTF-8',
   //     autoLoad: false,
   //     fields: objArr.fields
   // });
    this.columns = objArr.columns;
    var searchDbArea = createNormalComm3(160 , [['0' , '所有数据库'] , ['1' , '我关的数据库'] , ['2' , '我近期用的库']] , '' , '0');
    var searchDbName = new Ext.form.TextField({width:160 , value:''});
    var searchDbType = createNormalComm3(160 , [['','---请选择---'] , ['1' , 'MySQL'] , ['0' , 'Oracle']] , '' , '');
    var searchGName = new Ext.form.TextField({fieldLabel:'组名' , width:160 , value:''});
    Dbmis.SearchDataBase.superclass.constructor.call(this, {
        title:'搜索库',iconCls:'userIcon',
        sm:new Ext.grid.RowSelectionModel({singleSelect:true}),
        viewConfig: createView('没有路由规则信息'),
        tbar:['-',{
            text:'范围：'
        },'-',searchDbArea , '-', {
            text:'库名：'
        },'-',searchDbName,'-',{
            text:'类型：'
        },'-',searchDbType,'-',{
            text:'分组名'
        },'-',searchGName,'-',{text:'搜索',iconCls:'tableIndexStructure',handler:function() {
            panelObj.refresh();
        }},'-']
    });
    this.refresh = function() {
       // panelObj.getStore().load({
       //     params:{limit:200 , dbType: searchDbType.getValue() , dbName:filerString(searchDbName.getValue()) ,
       //             gName:filerString(searchGName.getValue()) , type:searchDbArea.getValue()}
       // });
    };

    this.searchTab = function() {
        manager.setActiveTab(structureConsole);
        var db = panelObj.getSelectionModel().getSelected().data;
        structureConsole.resetByDb(db.displayName);
        structureConsole.refreshThis();
    };
    this.addNewSqlConsole = function() {
        var db = panelObj.getSelectionModel().getSelected().data;
        manager.addConsole(db['id'], db['displayName'], db['dbType']);
    };
    this.attention = function(v) {
        var addIds = [] , delIds = [];
        var db = panelObj.getSelectionModel().getSelected().data;
        if(v == 2) addIds[0] = db.id;
        else delIds[0] = db.id;
        ajax({
            url:'/user/addDelUserAttentionDbs.jsn?_input_charset=UTF-8',
            params:{addIds:addIds, delIds:delIds},
            success:function (resp) {
                panelObj.refresh();
            }
        });
    };

    this.resetByTypeKey = function(searchType , searchKey) {
        searchDbName.setValue(searchKey);
        searchDbType.setValue('');
        searchGName.setValue(null);
        if(searchType == 2) {
            searchDbArea.setValue(0);
            panelObj.refresh();
        }else if(searchType == 3) {
            searchDbArea.setValue(2);
            panelObj.refresh();
        }else if(searchType == 4) {
            searchDbArea.setValue(1);
            panelObj.refresh();
        }
    };

    panelObj.resetByTypeKey(searchType , searchKey);
    regiestSpecialKey(panelObj.refresh , [searchDbName , searchGName]);
    regiestSelect(panelObj.refresh , [searchDbArea , searchDbType]);
};
Ext.extend(Dbmis.SearchDataBase, Ext.grid.GridPanel);