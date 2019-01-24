Ext.namespace("Dbmis");
Ext.override(Ext.grid.PropertyGrid, {
    syncFocusEl: Ext.emptyFn
});
var user_nick = user_nick == null ? "Guest" : user_nick;
var pubfields = ["data", "label"];
var user_role = user_role == null ? 0 : user_role;
var OT = "Oracle",
    MT = "MySQL";

function getDbTypeNameByTypeId(A) {
    if (A == 0) {
        return OT
    } else {
        return MT
    }
}
var pubTempates = {
    cell: new Ext.Template('<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>', '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>', "</td>")
};

function regiestSpecialKey(C, D) {
    for (var B = 0, A = D.length; B < A; B++) {
        D[B].on("specialkey", function (F, E) {
            if (E.keyCode != 13) {
                return
            }
            C.call(this)
        })
    }
}
function regiestSelect(C, D) {
    for (var B = 0, A = D.length; B < A; B++) {
        D[B].on("select", function () {
            C.call(this)
        })
    }
}
function forEachDs(A, D) {
    for (var B = 0, C = A.data.length; B < C; B++) {
        D.call(this, A.getAt(B).data, B)
    }
}
function getRowModel() {
    return new Ext.grid.RowSelectionModel({
        singleSelect: true
    })
}
function ajax(A) {
    Ext.Ajax.request(A)
}
var testEnv = testEnv;
var databaselist = null;

function getJson(A) {
    return Ext.util.JSON.decode(A)
}
if (Ext.isChrome) {
    var chromeDatePickerCSS = ".x-date-picker {border-color: #1b376c;background-color:#fff;position: relative;width: 185px;}";
    Ext.util.CSS.createStyleSheet(chromeDatePickerCSS, "chromeDatePickerStyle")
}
function addWatchTabPanel() {
    var A = Ext.getCmp("console_manager");
    A.getTableStructureConsole().reloadByWatcher(user_nick)
}
function isInputEvent(A) {
    var B = A.keyCode;
    return (B >= 65 && B <= 90) || (B >= 48 && B <= 57) || B == 8
}
function showCoding(W, V, e, J, E, I) {
    var O = "代码自生成器 [" + V + "]";
    var D = [
        ["1", "JavaDO"],
        ["7", "JavaDO(Hibernate)"],
        ["2", "ibatis"],
        ["3", "javaDAO(ibatis)"],
        ["4", "javaDAO(jdbcTemplate)"],
        ["5", "json(列表)"],
        ["8", "json(分页列表)"],
        ["9", "Extjs(列表)"],
        ["10", "Extjs(分页列表)"],
        ["6", "其它"]
    ];
    var Z = D.concat();
    var G = createNormalComStore([
        ["1", "pojo"],
        ["2", "map"]
    ]);
    var C = createNormalComStore(Z);
    var P = createNormlCommon(200, 22, G, "基准规则", 1, true);
    var U = createNormlCommon(200, 22, C, "代码模板", 1, true);
    var T = new Ext.ux.form.EditArea({
        xtype: "ux-editearea",
        id: "ea1",
        syntax: "js",
        region: "center",
        toolbar: "search, go_to_line, |, undo, redo, |, select_font"
    });
    var N = new Ext.Window({
        title: O,
        iconCls: "showIcon",
        height: 550,
        width: 900,
        layout: "border",
        maximizable: true,
        items: [T],
        tbar: ["-", {
            text: "基准规则"
        }, "-", P, "-", {
            text: "代码模板"
        }, "-", U, "-", {
            text: "生成代码",
            iconCls: "highlighIcon",
            handler: function () {
                if (U.getValue() != 6) {
                    a()
                } else {
                    Q.show()
                }
            }
        }, "-", {
            text: "个性化参数配置",
            iconCls: "userIcon",
            handler: function () {
                B.show()
            }
        }, "-"]
    });
    N.show();
    var a = function () {
        Ext.Ajax.request({
            url: "/table/showTableScript.jsn?_input_charset=UTF-8",
            params: {
                basedOn: P.getValue(),
                tabName: V,
                from: W,
                scriptType: U.getValue(),
                dbType: e,
                tableId: J,
                tableDesc: E,
                start: d.getValue(),
                end: M.getValue(),
                con: b.getValue(),
                dbId: I
            },
            success: function (h, f) {
                var g = Ext.util.JSON.decode(h.responseText);
                T.setValue(convertText(g.content))
            },
            failure: function (g, f) {
                alert("错误信息", g.responseText)
            }
        })
    };
    var d = new Ext.form.TextArea({
        fieldLabel: "前缀内容",
        height: 45,
        width: 450,
        value: '<s:iterator value=":tabDOList">\n\t<tr>'
    });
    var M = new Ext.form.TextArea({
        fieldLabel: "后缀内容",
        height: 45,
        width: 450,
        value: "\t</tr>\n</s:iterator>"
    });
    var b = new Ext.form.TextArea({
        fieldLabel: "单列内容",
        height: 80,
        width: 450,
        value: '<td><s:property value=":colDO"/></td>'
    });
    var K = new Ext.form.FormPanel({
        region: "center",
        width: 50,
        labelAlign: "center",
        frame: true,
        items: [d, b, M]
    });
    var Q = new Ext.Window({
        title: O,
        iconCls: "showIcon",
        height: 310,
        width: 600,
        layout: "border",
        items: [K],
        tbar: ["-", {
            text: "help",
            iconCls: "helpIcon",
            handler: function () {
                show2("help", ":tab 代表表名称 <br/>:tabDO\t代表对应的DO名称 <br/>:col\t代表列名称 :colDO\t代表DO中列名称 <br/>:getCol\t代表通过get方法获取属性")
            }
        }, "-"],
        buttons: [{
            text: "确 定",
            handler: function () {
                a();
                Q.hide()
            }
        }, {
            text: "取 消",
            handler: function () {
                Q.hide()
            }
        }]
    });
    var X = new Ext.form.TextField({
        fieldLabel: "javaDO统一后缀",
        width: 190
    });
    var A = new Ext.form.TextField({
        fieldLabel: "页面输出字符集",
        width: 190
    });
    var H = new Ext.form.TextField({
        fieldLabel: "javaDO的package",
        width: 190
    });
    var R = new Ext.form.TextField({
        fieldLabel: "javaDate统一类型",
        width: 190
    });
    var S = new Ext.form.TextField({
        fieldLabel: "控件默认宽度",
        width: 190
    });
    var c = new Ext.form.TextField({
        fieldLabel: "列表默认宽度",
        width: 190
    });
    var Y = new Ext.form.TextField({
        fieldLabel: "默认分页大小",
        width: 190
    });
    var F = new Ext.form.FormPanel({
        height: 260,
        width: 400,
        labelWidth: 140,
        frame: true,
        region: "center",
        bodyStyle: "padding:5px",
        items: [X, A, H, R, S, c, Y]
    });
    var B = new Ext.Window({
        title: "自定义配置内容",
        height: 290,
        width: 400,
        items: [F],
        buttons: [{
            text: "确 定",
            handler: function () {
                L();
                B.hide()
            }
        }, {
            text: "取 消",
            handler: function () {
                B.hide()
            }
        }]
    });
    Ext.Ajax.request({
        url: "/table/getUserCodeSet.jsn?_input_charset=UTF-8",
        success: function (h, f) {
            var g = Ext.util.JSON.decode(h.responseText);
            X.setValue(g["javalast"]);
            A.setValue(g["charset"]);
            H.setValue(g["dopackage"]);
            R.setValue(g["datetype"]);
            S.setValue(g["fieldwidth"]);
            c.setValue(g["textwidth"]);
            Y.setValue(g["pagesize"])
        },
        failure: function (g, f) {
            alert("错误信息", g.responseText)
        }
    });
    var L = function () {
        Ext.Ajax.request({
            url: "/table/saveUserCodeSet.jsn?_input_charset=UTF-8",
            params: {
                javalast: X.getValue(),
                charset: A.getValue(),
                dopackage: H.getValue(),
                datetype: R.getValue(),
                fieldwidth: S.getValue(),
                textwidth: c.getValue(),
                pagesize: Y.getValue()
            },
            success: function (g, f) {
                show2("提示", "个人配置保存成功。");
                a()
            },
            failure: function (g, f) {
                alert(g.responseText, "错误信息")
            }
        })
    }
}
function formatComment(A) {
    return "<pre>" + A + "</pre>"
}
function filerString(A) {
    return Ext.util.Format.trim(A)
}
function isStrEmpty(A) {
    return Ext.isEmpty(filerString(A))
}
function replaceAll(D, E, F) {
    var C = "";
    for (var B = 0, A = D.length; B < A; B++) {
        var G = D.charAt(B);
        if (G == E) {
            C += F
        } else {
            C += G
        }
    }
    return C
}
function replaceAll2(C, I, B) {
    var F = "",
        E = 0,
        D = C.length,
        G = I.charAt(0),
        H = I.length;
    while (E < D) {
        var A = C.charAt(E);
        if (G = A && (I == C.substring(E, E + H))) {
            F += B;
            E += H
        } else {
            F += A;
            E++
        }
    }
    return F
}
function convertText(A) {
    var B = replaceAll2(A, "&#039;", "'");
    B = replaceAll2(B, "&gt;", ">");
    B = replaceAll2(B, "&lt;", "<");
    B = replaceAll2(B, "&#034;", '"');
    return B
}
function splitWord(G, C) {
    var F = G.split(/\s/);
    if (F.length <= C) {
        return ""
    }
    var E = "",
        D = "";
    for (var B = 0, A = F.length; B < C; B++) {
        D += F[B] + " "
    }
    return filerString(G.substring(G.indexOf(F[C], filerString(D).length)))
}
function copyToClicb(B) {
    if (window.clipboardData) {
        window.clipboardData.setData("text", B)
    } else {
        if (window.netscape) {
            try {
                netscape.security.PrivilegeManager.enablePrivilege("UniversalXPConnect")
            } catch (A) {
                alert("你使用的FF浏览器,复制功能被浏览器拒绝！\n请在浏览器地址栏输入'about:config'并回车\n然后将 'signed.applets.codebase_principal_support'设置为'true'")
            }
            var H = Components.classes["@mozilla.org/widget/clipboard;1"].createInstance(Components.interfaces.nsIClipboard);
            if (!H) {
                return
            }
            var E = Components.classes["@mozilla.org/widget/transferable;1"].createInstance(Components.interfaces.nsITransferable);
            if (!E) {
                return
            }
            E.addDataFlavor("text/unicode");
            var D = new Object();
            var F = new Object();
            var D = Components.classes["@mozilla.org/supports-string;1"].createInstance(Components.interfaces.nsISupportsString);
            var C = B;
            D.data = C;
            E.setTransferData("text/unicode", D, C.length * 2);
            var G = Components.interfaces.nsIClipboard;
            if (!H) {
                return false
            }
            H.setData(E, null, G.kGlobalClipboard)
        }
    }
}
function dbTypeCheck(A, B) {
    return new Ext.Window({
        title: "请选择数据库类型",
        layout: "fit",
        id: "tableCreateFormWindow",
        width: 320,
        height: 120,
        resizable: false,
        closeAction: "hide",
        plain: true,
        items: [new Ext.FormPanel({
            bodyStyle: "padding:5px",
            deferredRender: false,
            defaultType: "textfield",
            labelWidth: 120,
            items: [{
                xtype: "hidden",
                name: "id"
            }, {
                xtype: "hidden",
                name: "dbId"
            },
            B],
            buttons: A
        })]
    })
}
function getDataBaseList(A) {
    var B = new Ext.form.ComboBox({
       // store: new Ext.data.JsonStore({
       //     url: "/user/dataBases.jsn",
       //     autoDestroy: true,
       //     id: "id",
       //     fields: [{
       //         name: "id"
       //     }, {
       //         name: "name",
       //         type: "string"
       //     }, {
       //         name: "type"
       //     }],
       //     autoLoad: false
       // }),
        displayField: "name",
        valueField: "id",
        typeAhead: true,
        mode: "local",
        value: "",
        forceSelection: true,
        emptyText: "请选择数据库...",
        fieldLabel: "数据库",
        selectOnFocus: true,
        width: 200,
        getListParent: function () {
            return this.el.up(".x-menu")
        },
        iconCls: "no-icon",
        listeners: {
            keyup: function (E, C) {
                if (isInputEvent(C)) {
                    var D = B.getRawValue();
                    B.getStore().load({
                        params: {
                            searchKey: D
                        }
                    })
                } else {
                    return true
                }
            }
        }
    });
    if (A) {
        B.getStore().load({
            params: {
                dbId: A["dbId"]
            }
        })
    } else {
        B.getStore().load()
    }
    return B
}
function getMyAnsyGrid(D, R) {
    var P = converArr([{
        name: "JOB_NAME",
        type: "string",
        header: "文件列表",
        width: 100,
        sortable: true,
        renderer: function (T) {
            return '<a href="' + downloadbaseUrl + T + '" target="_blank" style="color:blue;text-decoration:underline;font-weight:bold;"><img src="' + static_path + '/statics/img3/shared/icons/save.gif" /> 文件列表</a>'
        }
    }, {
        name: "STATUS",
        type: "string",
        header: "操作",
        width: 60,
        sortable: true,
        renderer: function (T) {
            if (T == 1) {
                return '<img src="/statics/images/delete.png" ext:qtip="删除" height="16" width="16"/>'
            }
        }
    }, {
        name: "FROM_NICK",
        type: "string",
        header: "创建人",
        width: 60,
        sortable: true
    }, {
        name: "CREATE_DATE",
        type: "string",
        header: "创建日期",
        width: 130,
        sortable: true
    }, {
        name: "JOB_DESC",
        type: "string",
        header: "备注",
        width: 120,
        sortable: true
    }, {
        name: "STATUS",
        type: "string",
        header: "任务状态",
        width: 70,
        sortable: true,
        renderer: function (T) {
            if (T == 1) {
                return '<img src="/statics/images/state_0.png" ext:qtip="待执行" height="16" width="16"/>待执行'
            } else {
                if (T == 2) {
                    return '<img src="/statics/images/state_1.png" ext:qtip="执行中" height="16" width="16"/>执行中'
                } else {
                    if (T == 3) {
                        return '<img src="/statics/images/icon_right.gif" ext:qtip="成功" height="16" width="16"/>成功'
                    } else {
                        return '<img src="/statics/images/state_3.png" ext:qtip="失败" height="16" width="16"/>失败'
                    }
                }
            }
        }
    }, {
        name: "EXPORTED_DATAS",
        type: "string",
        header: "行数",
        width: 50,
        sortable: true
    }, {
        name: "EXPORTED_FILES",
        type: "string",
        header: "文件数",
        width: 50,
        sortable: true
    }, {
        name: "ORA_SQL",
        type: "string",
        header: "SQL语句",
        width: 300,
        hidden: true,
        renderer: function (T) {
            return "<pre>" + T + "</pre>"
        }
    }, {
        name: "NEED_DATE",
        type: "string",
        header: "预计执行时间",
        width: 130,
        sortable: true
    }, {
        name: "EXE_DATE",
        type: "string",
        header: "实际执行时间",
        width: 130,
        sortable: true
    }, {
        name: "END_DATE",
        type: "string",
        header: "运行结束时间",
        width: 130,
        sortable: true
    }, {
        name: "ANALYZE_DELAY",
        type: "string",
        header: "时长（毫秒）",
        width: 100,
        hidden: true
    }, {
        name: "ERROR_LOG",
        type: "string",
        header: "错误日志",
        width: 400,
        hidden: true
    }], true);
    var K = new Ext.data.HttpProxy({
        url: "/user/userJobList.jsn?_input_charset=UTF-8&type=" + D,
        autoLoad: false
    });
    var I = new Ext.data.JsonStore({
        proxy: K,
        root: "rows",
        totalProperty: "total",
        successProperty: "success",
        idProperty: "JOB_NAME",
        fields: P.fields
    });
    var C = 20;
    var E = new Ext.PagingToolbar({
        pageSize: C,
        store: I,
        displayInfo: true,
        displayMsg: "显示第 {0} 条到 {1} 条记录，一共 {2} 条",
        emptyMsg: "没有记录"
    });
    var H = new Ext.form.TextField({
        width: 160
    });
    var S = new Ext.form.DateField({
        width: 90,
        format: "Y-m-d"
    });
    var A = new Ext.form.DateField({
        width: 90,
        format: "Y-m-d"
    });
    var L = ["-", {
        text: "备注："
    }, "-", H, "-", {
        text: "创建日期："
    }, "-", S, "-", {
        text: "~"
    },
    A, "-", {
        text: "刷新",
        iconCls: "tableIndexStructure",
        handler: function () {
            Q()
        }
    }, "-"];
    if (D == 1) {
        L.push({
            text: "单库导出",
            iconCls: "add",
            handler: function () {
                M(null)
            }
        }, "-", {
            text: "分表导出",
            iconCls: "add",
            handler: function () {
                O(null)
            }
        }, "-", {
            text: "权限申请",
            iconCls: "editIcon2",
            handler: function () {
                window.location = "http://askdba.corp.taobao.com/main#location=dbflow&menuId=145"
            }
        }, "-", {
            text: "文本导出配置",
            iconCls: "userIcon",
            handler: function () {
                G()
            }
        }, "-")
    }
    var Q = function () {
        I.reload({
            params: {
                start: 0,
                limit: C
            }
        })
    };
    I.on("beforeload", function (T, U) {
        T.baseParams["desc"] = H.getValue();
        if (S.getValue()) {
            T.baseParams["startDate"] = S.getValue().getTime()
        }
        if (A.getValue()) {
            T.baseParams["endDate"] = A.getValue().getTime()
        }
    });
    H.on("specialkey", function (U, T) {
        if (T.keyCode == 13) {
            Q()
        }
    });
    S.on("select", function () {
        Q()
    });
    A.on("select", function () {
        Q()
    });
    var O = function (f) {
        var b = new Ext.form.ComboBox({
            store: new Ext.data.JsonStore({
                url: "/database/dispInfoList.jsn",
                autoDestroy: true,
                autoLoad: true,
                fields: [{
                    name: "id"
                }]
            }),
            fieldLabel: "逻辑表名",
            displayField: "id",
            mode: "local",
            typeAhead: true,
            editable: true,
            forceSelection: true,
            triggerAction: "all",
            emptyText: "请选择分表...",
            selectOnFocus: true,
            width: 320,
            getListParent: function () {
                return this.el.up(".x-menu")
            },
            iconCls: "no-icon",
            listeners: {
                "select": function () {
                    d.store.reload({
                        add: false,
                        params: {
                            tabName: b.getValue()
                        },
                        callback: function () {
                            if (d.getStore().data.length > 0) {
                                d.setValue(d.getStore().getAt(0).data["dispColumn"])
                            }
                        }
                    })
                }
            }
        });
        var d = new Ext.form.ComboBox({
            store: new Ext.data.JsonStore({
                url: "/database/dispColumns.jsn?_input_charset=UTF-8",
                autoDestroy: true,
                autoLoad: false,
                fields: [{
                    name: "id"
                }, {
                    name: "dispColumn",
                    type: "string"
                }]
            }),
            displayField: "dispColumn",
            fieldLabel: "分表字段",
            mode: "local",
            typeAhead: true,
            editable: true,
            forceSelection: true,
            triggerAction: "all",
            emptyText: "请选择分表字段...",
            selectOnFocus: true,
            width: 320,
            getListParent: function () {
                return this.el.up(".x-menu")
            },
            iconCls: "no-icon"
        });
        var Z = createNormlCommon(100, 26, createNormalComStore([
            ["=", " ="],
            ["IN", " IN"],
            ["where", "where条件"]
        ]), "匹配条件", "=", false);
        var a = createNormalComm3(320, [
            ["1", "Excel文件(单个文件5W行)"],
            ["2", "文本文件(单个文件50W行)"]
        ], "文件类型", "1");
        var Y = new Ext.form.TextArea({
            width: 430,
            height: 120,
            fieldLabel: "匹配值",
            emptyText: "where id in(1,2,3)......"
        });
        var W = true;
        if (f) {
            Z.setValue(f["compare"]);
            Y.setValue(f["compareVal"]);
            b.getStore().on("load", function () {
                b.setValue(f["tableName"]);
                if (W) {
                    d.store.load({
                        params: {
                            tabName: b.getValue()
                        },
                        callback: function () {
                            if (d.getStore().data.length > 0) {
                                d.setValue(f["dispCol"])
                            }
                        }
                    })
                }
            });
            d.getStore().on("load", function () {
                if (W) {
                    d.setValue(f["dispCol"]);
                    W = false
                }
            })
        }
        var g = createNormalComm3(320, [
            ["0", "立即导出(立即开启后台任务)"],
            ["1", "延迟导出（次日凌晨导出）"]
        ], "导出方法", "0");
        var e = new Ext.form.TextArea({
            width: 430,
            height: 60,
            fieldLabel: "描述"
        });
        var V = createNormalComm3(320, [
            ["1", "*（所有列）"],
            ["2", "过滤大字段（BLOB、CLOB、Text等）"],
            ["3", "自定义列"],
            ["4", "数据量（count 但不支持GROUP BY）"]
        ], "列类型", "2");
        var X = new Ext.form.TextArea({
            width: 430,
            height: 40,
            fieldLabel: "select部分",
            emptyText: "select a,b,c ....."
        });
        X.hide();
        var c = new Ext.FormPanel({
            bodyStyle: "padding:5px",
            deferredRender: false,
            defaultType: "textfield",
            labelWidth: 85,
            items: [g, a, b, d, V, X, Z, Y, e]
        });
        V.on("select", function () {
            if (V.getValue() == "3") {
                X.show()
            } else {
                X.hide()
            }
        });
        var U = new Ext.Window({
            title: "新增异步导出任务(分库分表)",
            layout: "fit",
            width: 600,
            height: 455,
            resizable: false,
            plain: true,
            items: [c],
            buttons: [{
                text: "确 定",
                handler: function () {
                    if (isStrEmpty(b.getValue())) {
                        return show2("提示", "请先选择逻辑表名。")
                    }
                    if (isStrEmpty(d.getValue())) {
                        return show2("提示", "请选择分表字段。")
                    }
                    if (isStrEmpty(Y.getValue())) {
                        return show2("提示", "请输入匹配值。")
                    }
                    if (V.getValue() == "3" && !X.getValue()) {
                        return show2("提示", "自定义列的select语句部分不能为空。")
                    }
                    T()
                }
            }, {
                text: "关 闭",
                handler: function () {
                    U.close()
                }
            }]
        });
        U.show();
        var T = function () {
            U.disable();
            Ext.Ajax.request({
                url: "/user/saveUserJob.jsn?_input_charset=UTF-8",
                params: {
                    tableName: b.getValue(),
                    dispCol: d.getValue(),
                    compare: Z.getValue(),
                    compareVal: Y.getValue(),
                    desc: e.getValue(),
                    method: g.getValue(),
                    isDisp: 1,
                    fileType: a.getValue(),
                    columnType: V.getValue(),
                    columnInput: X.getValue()
                },
                success: function (j, h) {
                    U.enable();
                    var i = Ext.util.JSON.decode(j.responseText);
                    if (i["status"] == "0") {
                        show2("提示", i["message"])
                    } else {
                        Q();
                        show2("提示", "任务新增成功!");
                        U.close()
                    }
                },
                failure: function (i, h) {
                    U.enable();
                    alert(i.responseText)
                }
            })
        }
    };
    var M = function (W) {
        var Z = getDataBaseList(W);
        Z.width = 320;
        var Y = new Ext.form.TextArea({
            width: 500,
            height: 140,
            fieldLabel: "SQL语句"
        });
        var a = new Ext.form.TextArea({
            width: 500,
            height: 80,
            fieldLabel: "描述"
        });
        var V = createNormalComm3(320, [
            ["0", "立即导出(立即开启后台任务)"],
            ["1", "延迟导出（次日凌晨导出）"]
        ], "导出方法", "0");
        var X = createNormalComm3(320, [
            ["1", "Excel文件(单个文件5W行)"],
            ["2", "文本文件(单个文件50W行)"]
        ], "文件类型", "1");
        if (W) {
            Y.setValue(W["sql"]);
            var U = 0;
            Z.getStore().on("load", function () {
                if (U == 0) {
                    Z.setValue(W["dbId"]);
                    U = 1
                }
            })
        }
        var T = new Ext.FormPanel({
            bodyStyle: "padding:5px",
            deferredRender: false,
            defaultType: "textfield",
            labelWidth: 85,
            items: [Z, V, X, Y, a]
        });
        var b = new Ext.Window({
            title: "新增异步导出任务(单表,请注意控制数据量和资源限制)",
            layout: "fit",
            width: 630,
            height: 360,
            resizable: false,
            plain: true,
            items: [T],
            buttons: [{
                text: "确 定",
                handler: function () {
                    if (isStrEmpty(Z.getValue())) {
                        show2("提示", "请先选择数据库。");
                        return
                    }
                    if (isStrEmpty(Y.getValue())) {
                        show2("提示", "SQL语句不能为空。");
                        return
                    }
                    c()
                }
            }, {
                text: "关 闭",
                handler: function () {
                    b.close()
                }
            }]
        });
        b.show();
        var c = function () {
            b.disable();
            Ext.Ajax.request({
                url: "/user/saveUserJob.jsn?_input_charset=UTF-8",
                params: {
                    dbId: Z.getValue(),
                    sql: Y.getValue(),
                    desc: a.getValue(),
                    method: V.getValue(),
                    isDisp: 0,
                    fileType: X.getValue()
                },
                success: function (f, d) {
                    b.enable();
                    var e = Ext.util.JSON.decode(f.responseText);
                    if (e["status"] == "0") {
                        show2("提示", e["message"])
                    } else {
                        Q();
                        show2("提示", "任务新增成功!");
                        b.close()
                    }
                },
                failure: function (e, d) {
                    b.enable();
                    alert(e.responseText)
                }
            })
        }
    };
    var F = -1;
    var J = function (T) {
        if (T == "yes") {
            var U = I.getAt(F).data;
            Ext.Ajax.request({
                url: "/user/deleteOneAsynJob.jsn?_input_charset=UTF-8",
                params: {
                    jobName: U["JOB_NAME"]
                },
                success: function (X, V) {
                    var W = Ext.util.JSON.decode(X.responseText);
                    if (W["status"] == "0") {
                        show2("提示", W["message"])
                    } else {
                        Q();
                        show2("提示", "任务删除成功!")
                    }
                },
                failure: function (W, V) {
                    win.enable();
                    alert(W.responseText)
                }
            })
        }
    };
    Q();
    var B = new Ext.grid.GridPanel({
        title: "任务列表",
        store: I,
        region: "center",
        columns: P.columns,
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true
        }),
        viewConfig: new Ext.grid.GridView({
            emptyText: "没有路由规则信息",
            templates: {
                cell: new Ext.Template('<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>', '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>', "</td>")
            }
        }),
        bbar: E,
        listeners: {
            "cellclick": function (T, W, U, V) {
                var X = I.getAt(W).data;
                if (U == 2 && X["STATUS"] == "1") {
                    F = W;
                    Ext.MessageBox.confirm("警告", "是否真的要删除此任务？", J)
                }
                var Y = "SQL语句：\n" + X["ORA_SQL"] + "\n\n描述：" + X["JOB_DESC"] + "\n\n时长：" + X["ANALYZE_DELAY"] + "\n\n行数：" + X["EXPORTED_DATAS"] + "\n\n文件数：" + X["EXPORTED_FILES"] + "\n\n错误日志：\n" + X["ERROR_LOG"];
                N.setValue(Y)
            }
        },
        tbar: L
    });
    var N = new Ext.form.TextArea({
        title: "任务详情",
        region: "south",
        height: 180,
        split: true,
        collapsible: true,
        autoScroll: true
    });
    if (R) {
        if (!R["isDisp"]) {
            M(R)
        } else {
            O(R)
        }
    }
    var G = function () {
        var U = createNormalComm3(280, [
            [",", "逗号"],
            [" ", "空格"],
            ["    ", "4个空格"],
            ["\t", "tab"]
        ], "字段分隔符", ",");
        var V = createNormalComm3(280, [
            ["1", "\\n"],
            ["2", "\\r\\n"]
        ], "换行符格式", "1");
        var X = createNormalComm3(280, [
            ["gbk", "gbk"],
            ["utf-8", "utf-8"]
        ], "文件字符集", "gbk");
        ajax({
            url: "/user/getUserExportConfig.jsn",
            success: function (Z) {
                var Y = getJson(Z.responseText);
                U.setValue(Y["split"]);
                V.setValue(Y["warp"]);
                X.setValue(Y["charset"])
            }
        });
        var W = new Ext.form.FormPanel({
            region: "center",
            labelAlign: "left",
            labelWidth: 80,
            items: [U, V, X]
        });
        var T = new Ext.Window({
            height: 180,
            width: 400,
            layout: "border",
            items: [W],
            title: "请修改你的基本配置后进行保存",
            bodyStyle: "padding:5px",
            buttons: [{
                text: "确 定",
                handler: function () {
                    ajax({
                        url: "/user/saveUserExportConfig.jsn?_input_charset=UTF-8",
                        params: {
                            split: U.getValue(),
                            warp: V.getValue(),
                            charset: X.getValue()
                        },
                        success: function (Z) {
                            var Y = getJson(Z.responseText);
                            show2("提示", Y["msg"]);
                            if (Y["status"] == "1") {
                                T.close()
                            }
                        }
                    })
                }
            }, {
                text: "取 消",
                handler: function () {
                    T.close()
                }
            }]
        });
        T.show()
    };
    return new Ext.Panel({
        region: "center",
        layout: "border",
        items: [B, N],
        title: (D == 1 ? "我的异步导出任务" : "所有异步导出任务")
    })
}
function show2(B, A) {
    Ext.Msg.alert(B, A);
    return false
}
function show3(B, A, C) {
    show2(B, A);
    return C
}
function downLoadFile(D, E, B) {
    var A = "/" + E + "/" + B + ".do?_input_charset=UTF-8&";
    var C = document.createElement("iframe");
    C.src = A + Ext.urlEncode(D);
    C.style.display = "none";
    document.body.appendChild(C)
}
function createView(A) {
    return new Ext.grid.GridView({
        emptyText: A,
        templates: pubTempates
    })
}
function keyIndex(A) {
    return A ? '<img src="/statics/images/database_key.png" ext:qtip="表的索引字段"/>' : ""
}
function columnQTipRenderer(A) {
    return String.format('<span ext:qtip="{0}"><pre>{0}</pre></span>', A)
}
function createNormalComStore(A) {
    return new Ext.data.SimpleStore({
        fields: pubfields,
        data: A,
        id: 0
    })
}
function converArr(C, H, D, B) {
    var L = [];
    var M = [];
    var I = 0,
        J = 0,
        E = 0;
    if (H) {
        L[0] = new Ext.grid.RowNumberer({
            width: 30
        });
        I = 1
    } else {
        if (B) {
            L[0] = new Ext.grid.CheckboxSelectionModel();
            I = 1
        }
    }
    if (D) {
        L[I] = D;
        I += 1
    }
    for (var G = 0, F = C.length; G < F; G++) {
        var K = C[G];
        if (K["display"] == null || !K["display"]) {
            M[G] = {
                name: K["name"]
            };
            if (K["type"] != undefined && K["type"] != "") {
                M[G]["type"] = K["type"]
            }
        }
        if (K["hidden"] == null || !K["hidden"]) {
            var A = J + I;
            L[A] = {
                header: K["header"],
                width: K["width"],
                dataIndex: K["name"]
            };
            if (K["sortable"]) {
                L[A]["sortable"] = K["sortable"]
            }
            if (K["renderer"]) {
                L[A]["renderer"] = K["renderer"]
            }
            if (K["editor"]) {
                L[A]["editor"] = K["editor"]
            }
            J++
        }
    }
    return {
        columns: L,
        fields: M
    }
}
function createNormlCommon(F, E, B, D, A, C) {
    return new Ext.form.ComboBox({
        width: F,
        height: E,
        store: B,
        valueField: "data",
        displayField: "label",
        editable: C,
        triggerAction: "all",
        mode: "local",
        value: A,
        fieldLabel: D
    })
}
function createNormlCommon2(F, E, B, D, A, C) {
    return new Ext.form.ComboBox({
        width: F,
        height: E,
        store: B,
        valueField: "label",
        displayField: "label",
        editable: C,
        triggerAction: "all",
        mode: "local",
        value: A,
        fieldLabel: D
    })
}
function createNormalComm3(D, E, B, C) {
    var A = createNormalComStore(E);
    return createNormlCommon(D, 22, A, B, C, false)
}
function toAnsyDownLoad(C) {
    var B = Ext.getCmp("console_manager");
    var A = getMyAnsyGrid(1, C);
    A.closable = true;
    B.add(A);
    B.setActiveTab(A)
}
function createProxyCommon(E, B, G, A, D, C, F) {
    return createProxyCommon2(E, F, G, C, B, A, D, true)
}
function createProxyCommon2(C, D, G, A, F, H, B, E) {
    return new Ext.form.ComboBox({
        store: new Ext.data.JsonStore({
            url: F,
            autoDestroy: true,
            autoLoad: E,
            fields: G
        }),
        displayField: H,
        fieldLabel: D,
        valueField: B,
        mode: "local",
        typeAhead: true,
        editable: true,
        forceSelection: true,
        triggerAction: "all",
        emptyText: A,
        selectOnFocus: true,
        width: C,
        getListParent: function () {
            return this.el.up(".x-menu")
        },
        iconCls: "no-icon"
    })
}
Dbmis.MyTextField = Ext.extend(Ext.form.TextField, {
    name: "q",
    border: false,
    width: 260,
    height: 26,
    margins: "5 0 0 0",
    hideLabel: true,
    x: 20,
    y: 22,
    changeText: function (A) {
        this.emptyText = A;
        this.reset()
    }
});
Dbmis.SearchPanel = function (K, J) {
    var F = this;
    var H = "查询表，模糊查询请带上 % ......";
    var I = new Dbmis.MyTextField({
        emptyText: H
    });
    var E = new Ext.Button({
        text: "普通查询",
        iconCls: "tableIndexStructure",
        name: "key",
        x: 300,
        y: 25,
        border: false
    });
    var A = new Ext.Button({
        text: "分词检索",
        iconCls: "tableIndexStructure",
        name: "key",
        x: 400,
        y: 25,
        border: false
    });
    var C = new Ext.form.Radio({
        name: "search",
        checked: true,
        inputValue: "1",
        x: 20,
        y: 0,
        listeners: {
            check: function (L, M) {
                F.convrtNum(L.getGroupValue())
            }
        }
    });
    var G = new Ext.form.Radio({
        name: "search",
        checked: false,
        inputValue: "2",
        x: 60,
        y: 0,
        listeners: {
            check: function (L, M) {
                F.convrtNum(L.getGroupValue())
            }
        }
    });
    var B = new Ext.Panel({
        x: 385,
        y: 25,
        width: 98,
        height: 25,
        border: false,
        items: [A]
    });
    this.convrtNum = function (L) {
        if (L == null) {
            return
        }
        D = L;
        if (L == "1") {
            I.changeText(H);
            B.show()
        } else {
            I.changeText("数据库名......");
            B.hide()
        }
    };
    var D = 1;
    Dbmis.SearchPanel.superclass.constructor.call(this, {
        id: "dbims_searchPanel",
        region: "east",
        layout: "absolute",
        border: false,
        width: 800,
        items: [I, {
            x: 285,
            y: 25,
            width: 98,
            height: 22,
            border: false,
            items: [E]
        },
        B, C, {
            xtype: "label",
            text: "表",
            style: "font-size:12px",
            x: 35,
            y: 3
        },
        G, {
            xtype: "label",
            text: "数据库",
            style: "font-size:12px",
            x: 75,
            y: 3
        }]
    });
    I.on("specialkey", function (M, L) {
        if (L.keyCode == 13) {
            F.searchInfo(M.getValue())
        }
    });
    E.on("click", function () {
        F.searchInfo(I.getValue(), "0")
    });
    A.on("click", function () {
        F.searchInfo(I.getValue(), "1")
    });
    this.searchInfo = function (L, M) {
        L = filerString(L);
        if (D == 1) {
            K.reloadByWord(L.toLowerCase(), M)
        } else {
            J.refreshByByKey(filerString(L.toLowerCase()))
        }
    }
};
Ext.extend(Dbmis.SearchPanel, Ext.Panel);
Dbmis.HeaderPanel = function (F, G) {
    var E = "http://sqldev.corp.taobao.com:8989/",
        A = "http://dba.tools.taobao.com:9999/";
    var C = (isTestEnv ? E : A);
    var D = new Dbmis.SearchPanel(F, G);
    var H = '<img src="/statics/images/logo.png" alt="Web SQL Plus" align="left"/><a href="#" onclick="Dbmis.Utils.addEmptySqlConsole();"><img src="/statics/images/notepad.gif" alt="Web SQL Plus" title="Web SQL Plus"/></a>&nbsp;&nbsp;<a href="/tair.htm" target="_blank"><img src="/statics/images/cache_icon.png" alt="Tair Console" title="Tair Console"/></a>&nbsp;&nbsp;<a title="客服:钟隐" href="mailto:zhongyin.xy@taobao.com"><img src="/statics/images/email_go.png" alt="发送反馈" title="发送反馈"/></a>&nbsp;&nbsp;<a href="/statics/pages/help.html" target="_blank"><img src="/statics/images/help.png" alt="Help" title="Help"/></a><br/>Welcome <span id="visitor_nick">' + user_nick + '</span> <a href="https://backyard.seraph.taobao.com/login/?action=logout&url=' + C + '">退出</a></td>';
    var B = '<br/><span style="color:red;font-size:16px;font-weight:bold;">';
    if (isTestEnv) {
        B += ' Web SQL daily 环境</span><a href="' + A + '"> 到线上环境</a>'
    } else {
        B += ' Web SQL 线上环境</span><a href="' + E + '"> 到daily环境</a>'
    }
    Dbmis.HeaderPanel.superclass.constructor.call(this, {
        id: "header",
        el: "header",
        layout: "border",
        border: false,
        height: 50,
        items: [D, {
            region: "center",
            border: false,
            html: B
        }, {
            region: "west",
            border: false,
            html: H,
            width: 240
        }]
    })
};
Ext.extend(Dbmis.HeaderPanel, Ext.Panel);
Dbmis.WelcomeConsole = function () {
    Dbmis.WelcomeConsole.superclass.constructor.call(this, {
        title: "Welcome",
        iconCls: "webIcon",
        autoLoad: "/welcome.htm"
    })
};
Ext.extend(Dbmis.WelcomeConsole, Ext.Panel);
Dbmis.StatusBarPanel = function (F, A) {
    var E = new Ext.Toolbar.TextItem("Databases: " + F);
    var B = new Ext.Toolbar.TextItem("Tables: " + A);
    var C = new Ext.Toolbar.TextItem("");
    var D = new Ext.StatusBar({
        defaultText: "状态栏",
        items: [C, " ", E, " ", B, " "]
    });
    Dbmis.StatusBarPanel.superclass.constructor.call(this, {
        region: "south",
        height: 28,
        layout: "fit",
        id: "status_panel",
        el: "status_panel",
        bbar: D,
        listeners: {
            "render": {
                fn: function () {
                    Ext.fly(E.getEl().parentNode).addClass("x-status-text-panel");
                    Ext.fly(B.getEl().parentNode).addClass("x-status-text-panel");
                    Ext.fly(C.getEl().parentNode).addClass("x-status-text-panel");
                    Ext.fly(C.getEl()).update(new Date().format("F j, Y"))
                }
            }
        }
    });
    this.updateStatusText = function (G) {
        D.setText(G)
    }
};
Ext.extend(Dbmis.StatusBarPanel, Ext.Panel);
var pubfeather = "height=650,width=1000,top=100,left=100,toolbar=no,menubar=no,scrollbars=yes,resizable=yes,location=no,status=no";
Dbmis.DatabaseAllTreePanel = function (C, G, I, H, E) {
    var F = this;
    var D = new Ext.tree.AsyncTreeNode({
        text: "所有数据库",
        icon: "/statics/images/home.png",
        expanded: true
    });
    Dbmis.DatabaseAllTreePanel.superclass.constructor.call(this, {
        checkModel: "cascade",
        autoScroll: true,
        animate: true,
        enableDD: false,
        layout: "fit",
        containerScroll: false,
        rootVisible: Ext.isIE,
        region: "center",
        width: 180,
        loader: new Ext.tree.TreeLoader({
            dataUrl: "/database/groupsPageAll.jsn?start=0&limit=300",
            baseAttrs: {
                uiProvider: Ext.tree.TreeCheckNodeUI
            }
        }),
        root: D,
        listeners: {
            "click": function (K, J) {
                if (K.isLeaf()) {
                    C.getTableStructureConsole().reloadByDatabase(K.attributes["id"], K.attributes["text"])
                }
            },
            "dblclick": function (K) {
                if (K.isLeaf()) {
                    var J = K.attributes["id"];
                    C.addConsole(J, K.attributes["text"], K.attributes["dbType"])
                }
            }
        }
    });
    F.on("load", function (J) {
        if (isNaN(J.attributes["id"])) {
            J.expand(false, true, A)
        }
    });
    var B = {};
    var A = function (J) {
        if (J.hasChildNodes()) {
            Ext.each(J.childNodes, function () {
                var K = this;
                if (K.attributes["levelId"] == 1 || (!K.isLeaf() && K.attributes["checked"])) {
                    K.expand(false, true, A)
                } else {
                    if (K.isLeaf() && K.attributes["checked"]) {
                        B["id_" + K.attributes["id"]] = "a"
                    }
                }
            })
        }
    };
    this.refreshByByKey = function (J) {
        I.setValue(J);
        G.setValue("0");
        E.setValue("");
        H.setValue("");
        F.refreshTree()
    };
    this.saveUserAttention = function () {
        var K = F.getChecked();
        var O = [],
            N = 0,
            L = {}, P = [],
            M = 0;
        Ext.each(K, function () {
            var Q = this;
            if (Q.isLeaf()) {
                var R = "id_" + Q.attributes["id"];
                L[R] = "a";
                if (!B[R]) {
                    O[N++] = Q.attributes["id"]
                } else {
                    B[R] = "b"
                }
            }
        });
        for (var J in B) {
            if (B[J] == "a") {
                P[M++] = J.substring(3)
            }
        }
        Ext.Ajax.request({
            url: "/user/addDelUserAttentionDbs.jsn?_input_charset=UTF-8",
            params: {
                addIds: O,
                delIds: P
            },
            success: function (R) {
                var Q = getJson(R.responseText);
                show2("提示", Q["msg"]);
                B = L
            }
        })
    };
    this.refreshTree = function () {
        B = {};
        var J = F.getLoader();
        J.dataUrl = "/database/groupsPageAll.jsn?_input_charset=UTF-8&start=0&limit=300";
        J.baseParams.dbName = I.getValue();
        J.baseParams.dbType = H.getValue();
        J.baseParams.gName = E.getValue();
        J.baseParams.type = G.getValue();
        J.load(D, function () {
            if (I.getValue() || E.getValue()) {
                D.expand(true, true, A)
            } else {
                D.expand(false, true, A)
            }
        })
    }
};
Ext.extend(Dbmis.DatabaseAllTreePanel, Ext.tree.TreePanel);
Dbmis.SequenceGridPanel = function (C, A) {
    var B = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    this.store = new Ext.data.JsonStore({
        url: "/database/sequences.jsn?dbId=" + C,
        id: "id",
        fields: [{
            name: "id"
        }, {
            name: "name",
            type: "string"
        }, {
            name: "initialValue"
        }, {
            name: "currentValue"
        }, {
            name: "createdAt",
            type: "string"
        }],
        autoLoad: true
    });
    this.columns = [new Ext.grid.RowNumberer(), {
        header: "名称",
        width: 240,
        dataIndex: "name",
        sortable: true
    }, {
        header: "初始值",
        width: 120,
        dataIndex: "initialValue",
        sortable: true
    }, {
        header: "当前值",
        width: 120,
        dataIndex: "currentValue",
        sortable: true
    }, {
        header: "创建时间",
        width: 120,
        dataIndex: "createdAt",
        sortable: false
    }];
    Dbmis.SequenceGridPanel.superclass.constructor.call(this, {
        sm: B,
        iconCls: "procedureIcon",
        store: this.store,
        title: "Sequence@" + A,
        viewConfig: new Ext.grid.GridView({
            emptyText: "没有找到Sequence!"
        })
    })
};
Ext.extend(Dbmis.SequenceGridPanel, Ext.grid.GridPanel);
var TAB_GRID_PANEL = null;
Dbmis.TableGridPanel = function (G) {
    var D = this;
    TAB_GRID_PANEL = this;
    var E = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    this.store = new Ext.data.JsonStore({
        url: "/table/query.jsn?_input_charset=UTF-8",
        id: "id",
        fields: [{
            name: "id"
        }, {
            name: "dbId"
        }, {
            name: "name",
            type: "string"
        }, {
            name: "appName",
            type: "string"
        }, {
            name: "devOwner",
            type: "string"
        }, {
            name: "dbaOwner",
            type: "string"
        }, {
            name: "dwOwner",
            type: "string"
        }, {
            name: "seOwner",
            type: "string"
        }, {
            name: "rowCount"
        }, {
            name: "virtualId"
        }, {
            name: "storeCapacity"
        }, {
            name: "flag"
        }, {
            name: "description",
            type: "string"
        }, {
            name: "dbName",
            type: "string"
        }, {
            name: "updatedAt",
            type: "string"
        }, {
            name: "dbType"
        }, {
            name: "isUserDefine"
        }, {
            name: "tnsName"
        }, {
            name: "realDbName"
        }, {
            name: "jdbcUrl"
        }],
        autoLoad: false
    });
    this.columns = [new Ext.grid.RowNumberer(), {
        header: "操作",
        width: 140,
        dataIndex: "isUserDefine",
        sortable: false,
        renderer: function (J) {
            if (J == -1) {
                return "NOSQL表-无操作"
            }
            var I = "";
            if (J == "true") {
                I = '<a href="javascript:TAB_GRID_PANEL.watchTable(1)" style="font-weight: bold; color:green;text-decoration: underline">取消关注</a>'
            } else {
                I = '<a href="javascript:TAB_GRID_PANEL.watchTable(2)" style="font-weight: bold; color:red;text-decoration: underline">关注</a>'
            }
            I += ' <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:TAB_GRID_PANEL.refreshTab()"/>同步</a> <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:TAB_GRID_PANEL.toBeidou()">性能</a> <a style="font-weight: bold; color:green;text-decoration: underline" href="javascript:TAB_GRID_PANEL.toTddl()">TDDL</a>';
            return I
        }
    }, {
        header: "表名",
        width: 150,
        dataIndex: "name",
        sortable: true
    }, {
        header: "数据库",
        width: 120,
        dataIndex: "dbName",
        sortable: true
    }, {
        header: "路径",
        width: 300,
        dataIndex: "jdbcUrl",
        sortable: false
    }, {
        header: "容量(M)",
        width: 70,
        dataIndex: "storeCapacity",
        sortable: true
    }, {
        header: "CSV结构",
        width: 60,
        dataIndex: "id",
        sortable: false,
        renderer: function (K, J, I) {
            if (I.data["dbType"] > 1) {
                return ""
            }
            return '<a href="/table/structurePrint.htm?id=' + K + '" target="_blank">下载</a>'
        }
    }, {
        header: "描述",
        width: 226,
        dataIndex: "description",
        sortable: false
    }];
    Dbmis.TableGridPanel.superclass.constructor.call(this, {
        id: "table_grid",
        sm: E,
        store: this.store,
        viewConfig: createView("没有找到表!"),
        listeners: {
            "rowdblclick": function (I, K, J) {
                var L = D.getStore().getAt(K).data;
                G.addConsole2(L["dbId"], L["dbName"], L["dbType"], L["name"])
            }
        }
    });
    this.refreshTab = function () {
        var I = D.getSelectionModel().getSelected().data;
        if (I) {
            Ext.Msg.show({
                title: "请修改要同步的表",
                msg: "请输入（" + I.dbName + "）下要修改的表名",
                minWidth: 480,
                defaultTextHeight: 240,
                buttons: Ext.MessageBox.OKCANCEL,
                prompt: true,
                value: I.name,
                fn: function (K, J) {
                    if (K == "ok" && J != "") {
                        Ext.Ajax.request({
                            url: "/project/syncTables.jsn",
                            params: {
                                tnsName: I.tnsName,
                                dbId: I.dbId,
                                tableName: J,
                                tableName2: I.name,
                                virtualId: I.virtualId
                            },
                            success: function (L, M) {
                                show2("提示", "增量同步已经提交，可能需要大概几秒左右的时间进行同步.")
                            }
                        })
                    }
                },
                icon: Ext.MessageBox.INFO
            })
        } else {
            Ext.Msg.alert("错误", "请选择某一表进行编辑！")
        }
    };
    this.toBeidou = function () {
        var I = D.getSelectionModel().getSelected().data;
        Ext.Ajax.request({
            url: "/project/getHostByIp.jsn",
            params: {
                dbType: I.dbType,
                jdbcUrl: I.jdbcUrl
            },
            success: function (K, M) {
                var L = getJson(K.responseText);
                var J = beidou_path + "tianji/mysql.php?leftshow=1&orderby=hostname&db_role=all&search_text=" + L["hostName"];
                if (I.dbType == 0) {
                    J = beidou_path + "index.php?controller=curves&action=dispbyh&serv=" + L["sid"] + "&menuid=200"
                }
                window.open(J, "beidou", pubfeather)
            }
        })
    };
    this.toTddl = function () {
        var I = D.getSelectionModel().getSelected().data;
        Ext.Ajax.request({
            url: "/project/getDbNameById.jsn",
            params: {
                dbId: I.dbId
            },
            success: function (K, M) {
                var L = getJson(K.responseText);
                var J = beidou_path + "mysql/tddl.php?ln=0&envId=1&action=config-manage-all2&queryKey=" + L["dbName"];
                window.open(J, "tddl", pubfeather)
            }
        })
    };
    this.watchTable = function (I) {
        var J = D.getSelectionModel().getSelected().data;
        Ext.Ajax.request({
            url: "/user/tableWatch.jsn",
            params: {
                tableId: J.id,
                op: I
            },
            method: "POST",
            success: function (K, M) {
                var L = Ext.util.JSON.decode(K.responseText);
                show2("提示", L["message"]);
                if (H == 1) {
                    D.reloadByWord(C, F)
                } else {
                    if (H == 2) {
                        D.reloadByOwner(C)
                    } else {
                        if (H == 3) {
                            D.reloadByWatcher(C)
                        } else {
                            if (H == 4) {
                                D.reloadByAppName(C)
                            } else {
                                if (H == 5) {
                                    D.reloadByDatabase(C, A, B)
                                }
                            }
                        }
                    }
                }
            }
        })
    };
    var H = 0;
    var C = "",
        A = false,
        B = false,
        F = "0";
    this.reloadByWord = function (I, J) {
        F = J;
        H = 1;
        C = I;
        this.store.load({
            params: {
                q: I,
                isK: J
            },
            add: false
        })
    };
    this.reloadByOwner = function (I) {
        C = I;
        H = 2;
        this.store.load({
            params: {
                ownerNick: I
            },
            add: false
        })
    };
    this.reloadByWatcher = function (I) {
        H = 3;
        C = I;
        this.store.load({
            params: {
                watcherNick: I
            },
            add: false
        })
    };
    this.reloadByAppName = function (I) {
        H = 4;
        C = I;
        this.store.load({
            params: {
                appName: I
            },
            add: false
        })
    };
    this.reloadByDatabase = function (K, J, I) {
        H = 5;
        C = K;
        A = J;
        B = I;
        this.store.load({
            params: {
                dbId: K,
                isSearchDump: J,
                isDwDump: I
            },
            add: false
        })
    };
    this.loadAllTableIds = function () {
        var I = 0,
            J = "";
        forEachDs(D.store, function (K) {
            if (I < 50) {
                J += K["id"] + ","
            }
            I++
        });
        return J
    };
    this.loadAllTableStruture = function () {
        downLoadFile({
            ids: D.loadAllTableIds()
        }, "table", "allStructurePrint")
    }
};
Ext.extend(Dbmis.TableGridPanel, Ext.grid.GridPanel);
Dbmis.TablePartitionGridPanel = function () {
    var A = this;
    var B = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    this.store = new Ext.data.JsonStore({
        url: "/table/partitions.jsn",
        id: "id",
        fields: [{
            name: "id"
        }, {
            name: "dbId"
        }, {
            name: "name",
            type: "string"
        }, {
            name: "storeCapacity"
        }, {
            name: "rowCount"
        }],
        autoLoad: false
    });
    this.columns = [new Ext.grid.RowNumberer(), {
        header: "分表名",
        width: 320,
        dataIndex: "name",
        sortable: true
    }, {
        header: "容量",
        width: 120,
        dataIndex: "storeCapacity",
        sortable: true
    }, {
        header: "表结构MD5",
        width: 120,
        dataIndex: "columHash",
        sortable: true
    }, {
        header: "索引结构MD5",
        width: 120,
        dataIndex: "indexHash",
        sortable: true
    }];
    Dbmis.TablePartitionGridPanel.superclass.constructor.call(this, {
        sm: B,
        iconCls: "partitionsIcon",
        store: this.store,
        title: "分表:",
        viewConfig: createView("没有找到分表信息")
    });
    this.reloadByVirtualId = function (F, C, E, D) {
        A.setTitle("分表@" + C);
        if (D > 1) {
            return
        }
        this.store.load({
            params: {
                dbId: F,
                virtualId: E
            },
            add: false
        })
    }
};
Ext.extend(Dbmis.TablePartitionGridPanel, Ext.grid.GridPanel);
var WEB_SQL_TAB_EXTEND_PANEL = null;
Dbmis.TableExtendInfo = function () {
    var E = this;
    WEB_SQL_TAB_EXTEND_PANEL = this;
    var F = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    var I = converArr([{
        name: "type",
        hidden: true
    }, {
        name: "title",
        width: 180,
        header: "角色"
    }, {
        name: "user",
        width: 180,
        header: "责任人"
    }, {
        name: "type",
        width: 90,
        header: "操作",
        renderer: function () {
            return '<a href="javascript:WEB_SQL_TAB_EXTEND_PANEL.editInfo()">编辑</a>'
        }
    }], true);
    var G = [];
    var D = new Ext.data.Store({
        proxy: new Ext.data.MemoryProxy(G),
        reader: new Ext.data.ArrayReader({
            id: 0
        }, I.fields)
    });
    var C = new Ext.grid.ColumnModel(I.columns);
    var A = null,
        H = null;
    Dbmis.TableExtendInfo.superclass.constructor.call(this, {
        sm: F,
        iconCls: "userIcon",
        title: "DBA&应用&DW&搜索&开发",
        store: D,
        viewConfig: createView("没有数据...."),
        cm: C,
        listeners: {
            "rowclick": function (J, K) {
                H = D.getAt(K).data["type"]
            }
        }
    });
    var B = {
        "k_1": {
            title: "DBA",
            name: "dba",
            owner: "dbaOwner"
        },
        "k_4": {
            title: "DW",
            name: "dw",
            owner: "dwOwner"
        },
        "k_3": {
            title: "搜索",
            name: "se",
            owner: "seOwner"
        },
        "k_2": {
            title: "应用",
            name: "appName"
        },
        "k_5": {
            title: "开发",
            name: "dev",
            owner: "devOwner"
        }
    };
    this.loadInfoByTable = function (J) {
        A = J;
        if (J.dbType > 1) {
            G = []
        } else {
            G[0] = ["1", "DBA", J["dbaOwner"], "1"];
            G[1] = ["2", "应用", J["appName"], "2"];
            G[2] = ["3", "搜索", J["seOwner"], "3"];
            G[3] = ["4", "DW", J["dwOwner"], "4"];
            G[4] = ["5", "开发", J["devOwner"], "5"]
        }
        D.load()
    };
    this.editInfo = function () {
        if (user_nick == "Guest" && !testEnv) {
            return show2("Error", "登录后才能进行设置表的开发负责人！")
        }
        if (A) {
            var J = B["k_" + H];
            if (H == "2") {
                Ext.Msg.show({
                    title: "设置表所属的应用",
                    msg: "请输入" + A.name + "对应的应用名称（多个逗号分隔）:",
                    minWidth: 480,
                    defaultTextHeight: 240,
                    buttons: Ext.MessageBox.OKCANCEL,
                    multiline: true,
                    value: A.appName,
                    fn: function (M, L) {
                        if (M == "ok" && L != "") {
                            Ext.Ajax.request({
                                url: "/table/updateTableApp.jsn?_input_charset=UTF-8",
                                params: {
                                    tableId: A.id,
                                    appName: L
                                },
                                method: "POST",
                                success: function (N, O) {},
                                failure: function (O, N) {
                                    alert(O.responseText)
                                }
                            });
                            A["appName"] = L;
                            E.loadInfoByTable(A)
                        }
                    },
                    icon: Ext.MessageBox.INFO
                })
            } else {
                var K = A[J["owner"]];
                Ext.Msg.show({
                    title: "设置" + J["title"] + "负责人",
                    msg: A.name,
                    minWidth: 360,
                    defaultTextHeight: 120,
                    buttons: Ext.MessageBox.OKCANCEL,
                    multiline: true,
                    value: K ? K : user_nick,
                    fn: function (M, L) {
                        if (M == "ok" && L != "") {
                            Ext.Ajax.request({
                                url: "/user/tableOwner.jsn?_input_charset=UTF-8",
                                params: {
                                    tableId: A.id,
                                    type: J["name"],
                                    owner: L
                                },
                                method: "POST",
                                success: function (N, O) {}
                            });
                            A[J["owner"]] = L;
                            E.loadInfoByTable(A)
                        }
                    }
                })
            }
        } else {
            return show2("提示", "请先选择一张表再进行操作。")
        }
    }
};
Ext.extend(Dbmis.TableExtendInfo, Ext.grid.GridPanel);
Dbmis.TableColumnGridPanel = function () {
    var D = this;
    var H = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    this.store = new Ext.data.JsonStore({
        url: "/table/columns.jsn",
        id: "id",
        fields: [{
            name: "id"
        }, {
            name: "name",
            type: "string"
        }, {
            name: "description",
            type: "string"
        }, {
            name: "dataType",
            type: "string"
        }, {
            name: "length",
            type: "int"
        }, {
            name: "precision",
            type: "int"
        }, {
            name: "nullable",
            type: "string"
        }, {
            name: "defaultValue",
            type: "string"
        }, {
            name: "indexed",
            type: "boolean"
        }, {
            name: "securityLevel",
            type: "string"
        }, {
            name: "securityDesc",
            type: "string"
        }, {
            name: "autoIncrement"
        }],
        autoLoad: false
    });
    var K = createNormalComStore([
        ["-1", "B1"],
        ["0", "B2"],
        ["1", "B3"],
        ["2", "B4"],
        ["-3", "C1"],
        ["-2", "C2"],
        ["3", "C3"],
        ["4", "C4"]
    ]);
    var L = {};
    var C = 0;
    var E = [new Ext.grid.RowNumberer(), {
        header: "列名",
        width: 140,
        dataIndex: "name",
        sortable: true
    }, {
        header: "索引",
        width: 48,
        dataIndex: "indexed",
        sortable: true,
        renderer: keyIndex
    }, {
        header: "描述",
        width: 220,
        dataIndex: "description",
        sortable: true,
        renderer: formatComment
    }, {
        header: "类型",
        width: 80,
        dataIndex: "dataType",
        sortable: true
    }, {
        header: "长度",
        width: 55,
        dataIndex: "length",
        sortable: false
    }, {
        header: "精度",
        width: 55,
        dataIndex: "precision",
        sortable: false
    }, {
        header: "可空",
        width: 55,
        dataIndex: "nullable",
        sortable: false
    }, {
        header: "缺省值",
        width: 60,
        dataIndex: "defaultValue",
        sortable: false
    }, {
        header: "自增",
        width: 55,
        dataIndex: "autoIncrement",
        sortable: false,
        renderer: function (N) {
            if (N == 1) {
                return "Y"
            }
            return "N"
        }
    }, {
        header: "安全级别",
        width: 100,
        dataIndex: "securityLevel",
        editor: new Ext.form.ComboBox({
            width: 100,
            height: 26,
            store: K,
            valueField: "data",
            displayField: "label",
            editable: false,
            triggerAction: "all",
            mode: "local",
            value: "0",
            listeners: {
                "change": function () {
                    D.changeOneRow()
                }
            }
        }),
        renderer: function (O) {
            var N = K.find("data", O);
            return K.getAt(N).get("label")
        },
        sortable: false
    }, {
        header: "安全说明",
        width: 180,
        dataIndex: "securityDesc",
        editor: new Ext.form.TextField({
            width: 140,
            height: 26,
            maxLength: 512,
            maxLengthText: "备注信息太长。",
            listeners: {
                "change": function () {
                    D.changeOneRow()
                }
            }
        }),
        sortable: false
    }];
    this.columns = E;
    var G, J = -1,
        B = -1;
    if (user_role >= 7) {
        this.tbar = [{
            xtype: "tbfill"
        }, "-", {
            text: "保存",
            iconCls: "saveIcon",
            handler: function () {
                if (C == 0) {
                    Ext.Msg.alert("提示", "你没有对这个表做任何修改，提交无效。");
                    return
                }
                var T = D.getStore();
                var N = [],
                    U = 0;
                var P = [];
                var V = [];
                var O = [];
                for (var R = 0, Q = T.data.length; R < Q; R++) {
                    var S = T.getAt(R).data;
                    if (L["key" + S["id"]] == "1") {
                        N[U] = S["id"];
                        P[U] = S["securityLevel"];
                        O[U] = S["name"];
                        V[U++] = S["securityDesc"]
                    }
                }
                D.disable();
                Ext.Ajax.request({
                    url: "/table/updateColumnSecurity.jsn?_input_charset=UTF-8",
                    params: {
                        tableId: G,
                        idArr: N,
                        securityArr: P,
                        secDescArr: V,
                        dbId: B,
                        tableName: F,
                        colNameArr: O
                    },
                    method: "POST",
                    success: function (W, X) {
                        D.enable();
                        C = 0;
                        L = {};
                        Ext.Msg.alert("提示", "保存成功！")
                    },
                    failure: function (X, W) {
                        alert(X.responseText)
                    }
                })
            }
        }, "-"]
    }
    Dbmis.TableColumnGridPanel.superclass.constructor.call(this, {
        id: "table_column_grid",
        sm: H,
        store: this.store,
        title: "Columns:",
        iconCls: "tableColumnStructure",
        clicksToEdit: 1,
        viewConfig: createView("没有找到列!"),
        listeners: {
            "cellclick": function (N, Q, O, P) {
                J = Q
            },
            "cellcontextmenu": function (N, R, O, Q) {
                if (O > 0 && O != 9) {
                    Q.preventDefault();
                    var P = new Ext.menu.Menu({
                        items: [{
                            text: "复制单元格",
                            handler: function () {
                                var S = E[O]["dataIndex"];
                                var T = D.getStore().getAt(R).data[S];
                                copyToClicb(T)
                            }
                        }]
                    });
                    P.showAt(Q.getXY())
                }
            },
            "rowdblclick": function (N, P, O) {
                var Q = D.getStore().getAt(P).data;
                Ext.Msg.show({
                    title: "更新列的描述（请分行写描述）",
                    msg: "请输入列（" + Q.name + "）描述:",
                    minWidth: 480,
                    defaultTextHeight: 240,
                    buttons: Ext.MessageBox.OKCANCEL,
                    multiline: true,
                    value: Q.description,
                    fn: function (S, R) {
                        if (S == "ok" && R != "") {
                            Ext.Ajax.request({
                                url: "/table/updateColumnComment.jsn?_input_charset=UTF-8",
                                params: {
                                    columnId: Q.id,
                                    description: R
                                },
                                method: "POST",
                                success: function (T, U) {
                                    Q.description = R;
                                    D.store.reload()
                                },
                                failure: function (U, T) {
                                    alert(U.responseText)
                                }
                            })
                        }
                    },
                    icon: Ext.MessageBox.INFO
                })
            }
        }
    });
    this.reloadByTableId = function (P, N, Q, O) {
        F = N;
        A = P;
        B = Q;
        I = O;
        if (O > 1) {
            D.getColumnModel().setEditable(10, false);
            D.getColumnModel().setEditable(11, false)
        } else {
            if (user_role >= 7) {
                D.getColumnModel().setEditable(10, true);
                D.getColumnModel().setEditable(11, true)
            }
        }
        if (C > 0) {
            Ext.MessageBox.confirm("警告", "刚才您对这个表的信息进行了编辑，若刷新内容，则编辑信息无效，是否继续。", D.reLoadByIdGoing)
        } else {
            D.reLoadByIdGoing("yes")
        }
    };
    if (user_role < 7) {
        this.getColumnModel().setEditable(10, false);
        this.getColumnModel().setEditable(11, false)
    }
    var F = "",
        A = "",
        M = 0,
        I = 0;
    this.reLoadByIdGoing = function (N) {
        if (N != "yes") {
            return
        }
        L = {};
        G = A;
        D.setTitle("列@" + F);
        D.store.load({
            params: {
                tableId: A,
                dbId: B,
                tableName: F,
                dbType: I
            },
            add: false
        });
        C = 0;
        J = -1
    };
    this.changeOneRow = function () {
        if (J == -1) {
            return
        }
        C++;
        var N = "key" + D.getStore().getAt(J).data["id"];
        L[N] = "1"
    }
};
Ext.extend(Dbmis.TableColumnGridPanel, Ext.grid.EditorGridPanel);
Dbmis.TableIndexGridPanel = function () {
    var A = new Ext.grid.RowSelectionModel({
        singleSelect: true
    });
    this.store = new Ext.data.JsonStore({
        url: "/table/indexList.jsn",
        id: "id",
        fields: [{
            name: "id"
        }, {
            name: "name",
            type: "string"
        }, {
            name: "columns",
            type: "string"
        }, {
            name: "type",
            type: "string"
        }, {
            name: "description",
            type: "string"
        }],
        autoLoad: false
    });
    this.columns = [new Ext.grid.RowNumberer(), {
        header: "索引名称",
        width: 180,
        dataIndex: "name",
        sortable: true
    }, {
        header: "类型",
        width: 120,
        dataIndex: "type",
        sortable: "true"
    }, {
        header: "列名",
        width: 380,
        dataIndex: "columns",
        sortable: false
    }, {
        header: "描述",
        width: 360,
        dataIndex: "description",
        sortable: false
    }];
    Dbmis.TableIndexGridPanel.superclass.constructor.call(this, {
        id: "table_index_grid",
        sm: A,
        store: this.store,
        title: "索引:",
        iconCls: "tableIndexStructure",
        viewConfig: createView("没有找到索引信息")
    });
    this.reloadByTableId = function (D, B, E, C) {
        this.store.load({
            params: {
                tableId: D,
                dbId: E,
                dbType: C,
                tableName: B
            },
            add: false
        });
        this.setTitle("索引@" + B)
    }
};
Ext.extend(Dbmis.TableIndexGridPanel, Ext.grid.GridPanel);
Dbmis.TableStructureConsole = function (I) {
    var A = this;
    var D = -1;
    var F = new Dbmis.TableGridPanel(I);
    F.region = "center";
    var B = new Dbmis.TableColumnGridPanel();
    var J = new Dbmis.TableIndexGridPanel();
    var K = new Dbmis.TablePartitionGridPanel();
    var L = new Dbmis.TableExtendInfo();
    var E = new Ext.TabPanel({
        region: "south",
        activeTab: 0,
        resizeTabs: true,
        deferredRender: false,
        minTabWidth: 160,
        tabWidth: 240,
        height: 240,
        split: true,
        enableTabScroll: true,
        defaults: {
            autoScroll: true
        },
        items: [B, J, K, L]
    });
    var H = new Ext.form.Checkbox({
        name: "isSearchDump",
        value: false
    });
    var G = new Ext.form.Checkbox({
        name: "isDwDump",
        value: false
    });
    Dbmis.TableStructureConsole.superclass.constructor.call(this, {
        layout: "border",
        title: "表结构",
        iconCls: "tableStructure",
        items: [F, E],
        tbar: [{
            tooltip: "编辑表的描述",
            text: "编辑表",
            iconCls: "editTable",
            handler: function () {
                if (user_nick == "Guest" && !testEnv) {
                    Ext.Msg.alert("Error", "登录后才能进行表的描述更新！");
                    return
                }
                var M = A.getSelectedTable();
                if (M) {
                    if (M.dbType > 1) {
                        return show2("提示", "NO SQL表不支持该操作")
                    }
                    Ext.Msg.show({
                        title: "更新表的描述（请分行写描述）",
                        msg: "请输入表（" + M.name + "）描述:",
                        minWidth: 480,
                        defaultTextHeight: 240,
                        buttons: Ext.MessageBox.OKCANCEL,
                        multiline: true,
                        value: M.description,
                        fn: function (O, N) {
                            if (O == "ok" && N != "") {
                                Ext.Ajax.request({
                                    url: "/table/updateTableComment.jsn?_input_charset=UTF-8",
                                    params: {
                                        tableId: M.id,
                                        description: N
                                    },
                                    method: "POST",
                                    success: function (P, Q) {
                                        M.description = N;
                                        F.store.reload()
                                    },
                                    failure: function (Q, P) {
                                        alert(Q.responseText)
                                    }
                                });
                                A.getSelectedTableModel().set("description", N)
                            }
                        },
                        icon: Ext.MessageBox.INFO
                    })
                } else {
                    Ext.Msg.alert("错误", "请选择某一表进行编辑！")
                }
            }
        }, "-", {
            text: "代码生成器",
            tooltip: "这里可以为开发团队生成常用的各类自动代码",
            iconCls: "userIcon",
            handler: function () {
                if (C != null) {
                    var M = F.getSelectionModel().getSelected().data;
                    if (M.dbType > 1) {
                        return show2("提示", "NO SQL表不支持该操作")
                    }
                    showCoding(2, M.name, 0, C, M.description, M.dbId)
                } else {
                    show2("提示", "请先选择一个表，再执行代码生成操作。")
                }
            }
        }, "-", {
            text: "创建语句",
            tooltip: "选择一个表后点击这里导出创建语句",
            iconCls: "moveIcon",
            handler: function () {
                if (C != null) {
                    var M = F.getSelectionModel().getSelected().data;
                    if (M.dbType > 1) {
                        return show2("提示", "NO SQL表不支持该操作")
                    }
                    A.explortCreateSql(M.name)
                } else {
                    show2("提示", "请先选择一个表，再执行创建语句导出。")
                }
            }
        }, "-", H, "-", {
            text: "搜索dump"
        }, "-", G, "-", {
            text: "dw dump"
        }, "-", {
            tooltip: "重新查询数据",
            text: "刷新",
            iconCls: "tableIndexStructure",
            handler: function () {
                F.reloadByDatabase(A.dbId, H.getValue(), G.getValue())
            }
        }, "-", {
            text: "导出当前所有表结构",
            iconCls: "saveIcon",
            handler: function () {
                F.loadAllTableStruture()
            }
        }, "-"]
    });
    var C = null;
    F.getSelectionModel().on("selectionchange", function (M) {
        if (M.getSelected()) {
            var N = M.getSelected().data;
            C = N.id;
            B.reloadByTableId(N.id, N.name, N.dbId, N.dbType);
            J.reloadByTableId(N.id, N.name, N.dbId, N.dbType);
            K.reloadByVirtualId(N.dbId, N.name, N.virtualId, N.dbType);
            L.loadInfoByTable(N, F)
        }
    });
    this.explortCreateSql = function (N) {
        var O = new Ext.form.TextArea({
            region: "center"
        });
        Ext.Ajax.request({
            url: "/table/getCreateTabSql.jsn?_input_charset=UTF-8",
            params: {
                tableId: C,
                tableName: N
            },
            success: function (P, Q) {
                O.setValue(P.responseText)
            },
            failure: function (Q, P) {
                alert(result.responseText)
            }
        });
        var M = new Ext.Window({
            title: "表:" + N + ' 创建语句，付：<font color="red">该创建语句仅提供参考</font>',
            iconCls: "showIcon",
            height: 550,
            width: 900,
            layout: "border",
            maximizable: true,
            items: [O]
        });
        M.show()
    };
    this.getSelectedTable = function () {
        var M = F.getSelectionModel();
        if (M.getSelected()) {
            return M.getSelected().data
        } else {
            return null
        }
    };
    this.getSelectedTableModel = function () {
        var M = F.getSelectionModel();
        if (M.getSelected()) {
            return M.getSelected()
        } else {
            return null
        }
    };
    this.getSelectedColumn = function () {
        var M = B.getSelectionModel();
        if (M.getSelected()) {
            return M.getSelected().data
        } else {
            return null
        }
    };
    this.getSelectedColumnModel = function () {
        var M = B.getSelectionModel();
        if (M.getSelected()) {
            return M.getSelected()
        } else {
            return null
        }
    };
    this.getTableGrid = function () {
        return F
    };
    this.reloadByDatabase = function (N, M) {
        this.setTitle(M);
        D = N;
        F.reloadByDatabase(N, "", "");
        A.dbId = N;
        A.dbName = M;
        Ext.getCmp("console_manager").activate(A)
    };
    this.reloadByWord = function (M, N) {
        D = -1;
        F.reloadByWord(M, N);
        this.setTitle("查询结果:" + M);
        Ext.getCmp("console_manager").activate(A)
    };
    this.reloadByOwner = function (M) {
        D = -1;
        F.reloadByOwner(M);
        this.setTitle("所有者:" + M);
        Ext.getCmp("console_manager").activate(A)
    };
    this.reloadByWatcher = function (M) {
        D = -1;
        F.reloadByWatcher(M);
        this.setTitle("关注者:" + M);
        Ext.getCmp("console_manager").activate(A)
    }
};

//baisui
Ext.extend(Dbmis.TableStructureConsole, Ext.Panel);

//Schema panel///////////////////////////////////////
Dbmis.SchemaPanel = function ( servicename ) {
    
    var myReader2 = new Ext.data.JsonReader({
        root: 'solrschema',
        totalProperty: 'results',
        id: "name",
        fields: [{
            name: 'id',
            mapping: 'id'
        }, {
            name: 'name',
            mapping: 'name'
        }, {
            name: 'indexed',
            mapping: 'indexed'
        }, {
            name: 'stored',
            mapping: 'stored'
        }, {
            name: 'required',
            mapping: 'required'
        }, {
            name: 'type',
            mapping: 'type'
        }, ]
    });


    var SK_Store =  new Ext.data.Store({
                proxy: new Ext.data.HttpProxy({
                    url: "/runtime/index_query.ajax?action=index_query_action&event_submit_do_query_schema=y&resulthandler=advance_query_result&servicename="+ servicename,
                    method: "post"
                }),
                reader: myReader2
            });

     SK_Store.load();
     this.add = function(schemaFields){
       for(var i =0;i<schemaFields.length;i++){
         this.jdata.solrschema.push(schemaFields[i]);
       }
     
     SK_Store.reload();
    
    }
    var RR = new Ext.grid.ColumnModel({
        columns: [new Ext.grid.RowNumberer(), {
            header: "fieldname",
            width: 200,
            sortable: true,
            dataIndex: "name",
            allowBlank: true,
            renderer: function (W) {
                return '<span style="font-weight: bold ;color:blue ; font-size:14px ;">' + W + "</span>"
            }
        }, {
            header: "type",
            width: 200,
            sortable: true,
            dataIndex: "type",
            allowBlank: true,
            editor: new Ext.form.TextField({})
        }, {
            header: "indexed",
            width: 80,
            sortable: true,
            dataIndex: "indexed",

            allowBlank: true,
            editor: new Ext.form.TextField({})
        }, {
            header: "stored",
            width: 80,
            sortable: true,
            dataIndex: "stored",

            allowBlank: true,
            editor: new Ext.form.TextField({})
        }, {
            header: "required",
            width: 80,
            sortable: true,
            dataIndex: "required",

            allowBlank: true,
            editor: new Ext.form.TextField({})
        }]
    });

    SK_Store.load();
    Dbmis.SchemaPanel.superclass.constructor.call(this, {
        store: SK_Store,
        title:"SCHEMA",
       // renderTo: "content",
        colModel: RR,
        frame: false,
        sm:new Ext.grid.RowSelectionModel({
            singleSelect: true
        }),
        iconCls: 'rowForm',
        autoHeight:true
    })
}
Ext.extend(Dbmis.SchemaPanel , Ext.grid.GridPanel);
//schema panel///////////////////////////////////////////////////////

Dbmis.SQLConsole = function (B, Q, S, M, U) {
    var L = this;
    this.dbId = B;
    var E = createNormalComm3(90, [
        ["1", "RDBMS"],
        ["2", "OceanBase"],
        ["3", "HBase"]
    ], "", U);
    var A = E.getValue();
    E.on("select", function () {
        if (A != E.getValue()) {
            I.getStore().load({
                params: {
                    type: E.getValue()
                },
                callback: function () {
                    I.setValue("")
                }
            });
            A = E.getValue()
        }
    });
    var C = false;
   
    var V = S;
     
    var N = new Ext.ux.form.CodeMirror({
        enableKeyEvents: true,
        region: "center",
        language: "sql",
        emptyText: "请输入SQL语句！",
        lazyInit: true
    });
    var F = new Dbmis.DynamicGridPanel({
        title: "查询结果：",
        height: 300,
        rowNumberer: true,
        iconCls: "tableRows",
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true
        })
    });
    
    F.store.on("metachange", function () {
       
       //alert(F.store.reader.jsonData.columns);
       
          if (typeof (F.store.reader.jsonData.columns) === "object") {     
                // baisui 设置显示
          //    SK_proxy.length = 0;
              // var schema = F.store.reader.jsonData.solrschema;
               
              // SK.add(schema);
               
              // for(var i =0;i< schema.length;i++){
               
               // SK_proxy[i] =schema[i];
               //  alert(schema[i].name);
               //}  
              // SK.getStore().reload(); 
           }
          
                        
    });
    
    var K = [];
    var G = new Ext.data.Store({
        proxy: new Ext.data.MemoryProxy(K),
        reader: new Ext.data.ArrayReader({
            id: 0
        }, [{name: "name"}, {name: "value"}])
    });
    G.load();
    var R = [new Ext.grid.RowNumberer(), {
        header: "列名",
        width: 200,
        sortable: false,
        dataIndex: "name",
        id: "name",
        allowBlank: false,
        renderer: function (W) {
            return '<span style="font-weight: bold ;color:blue ; font-size:14px ;">' + W + "</span>"
        }
    }, {
        header: "值",
        width: 600,
        sortable: false,
        dataIndex: "value",
        id: "value",
        allowBlank: true,
        editor: new Ext.form.TextField({})
    }];
    var H = new Ext.grid.ColumnModel(R);
    var J = new Ext.grid.EditorGridPanel({
        title: "单行详情",
        autoScroll: true,
        cm: H,
        ds: G,
        sm: new Ext.grid.RowSelectionModel({
            singleSelect: true
        }),
        viewConfig: new Ext.grid.GridView({
            forceFit: false
        }),
        clicksToEdit: 1,
        iconCls: "rowForm"
    });
   /////////////////////////////////////////////
   var RR = [new Ext.grid.RowNumberer(), {
        header: "fieldname",
        width: 200,
        sortable: false,
        dataIndex: "name",
        id: "name",
        allowBlank: false,
        renderer: function (W) {
            return '<span style="font-weight: bold ;color:blue ; font-size:14px ;">' + W + "</span>"
        }
    },{
        header: "type",
        width: 200,
        sortable: false,
        dataIndex: "type",
        id: "type",
        allowBlank: true,
        editor: new Ext.form.TextField({})
    }, {
        header: "indexed",
        width: 80,
        sortable: false,
        dataIndex: "indexed",
        id: "indexed",
        allowBlank: true,
        editor: new Ext.form.TextField({})
    },{
        header: "stored",
        width: 80,
        sortable: false,
        dataIndex: "stored",
        id: "stored",
        allowBlank: true,
        editor: new Ext.form.TextField({})
    },{
        header: "required",
        width: 80,
        sortable: false,
        dataIndex: "required",
        id: "required",
        allowBlank: true,
        editor: new Ext.form.TextField({})
    }];
  // var SK_proxy =[];
  // var SK_Store = new Ext.data.Store({
  //      proxy: new Ext.data.MemoryProxy(SK_proxy),
  //      reader:  new Ext.data.JsonReader( {root: 'solrschema' })
  //  });
   // SK_Store.load();
   ////////////////////////////////////////////
   // var SK_CM  = new Ext.grid.ColumnModel(RR);
   // var SK = new Ext.grid.EditorGridPanel({
   //     title: "SCHEMA详细",
   //     autoScroll: true,
   //     cm: SK_CM,
   //     store: SK_Store,
   //     sm: new Ext.grid.RowSelectionModel({
   //         singleSelect: true
   //     }),
   //     viewConfig: new Ext.grid.GridView({
   //         forceFit: false
   //     }),
   //     clicksToEdit: 1,
   //     iconCls: "rowForm"
   // }); 
     //alert(L.dbId);
    var SK = new Dbmis.SchemaPanel(L.dbId);
    F.getSelectionModel().on("selectionchange", function (W) {
    
        if (W.getSelected()) {
        
            var X = W.getSelected().data;
            
            D(X)
        }
    });
    var D = function (Y) {
        K.length = 0;
        var X = 0;
        for (var W in Y) {
            K[X++] = [W, Y[W]]
        }
        J.getStore().reload()
    };
    F.getStore().on("load", function (W, X, Y) {
        var Z = W.reader.jsonData;
        if (!Z.success) {
            var a = replaceAll2(Z.errorMsg, "{n}", "<br>");
            Ext.Msg.alert("SQL语句错误", a)
        } else {
            if (X.length > 0) {
                D(X[0].data)
            }
        }
    });
    var O = new Ext.TabPanel({
        region: "center",
        activeTab: 2,
        resizeTabs: true,
        deferredRender: false,
        minTabWidth: 160,
        height: 300,
        tabWidth: 160,
        split: true,
        enableTabScroll: true,
        defaults: {
            autoScroll: true
        },
        items: [F, J,SK]
    });
    N.on("initialize", function () {
        N.grabKeys(function (b) {
            if (b.keyCode == 119) {
                F.execute_query(L.dbId, L.getExecutedSQL())
            } else {
                L.downloadCSV(L.dbId, L.getExecutedSQL())
            }
        }, function (b, d) {
            var e = d.character;
            return e != "w" && e != "x" && (b == 119 || b == 120)
        });
        N.setValue(" select * from "+ L.dbId+";");
        if (!isStrEmpty(M) && M != undefined) {
            var Y = M.indexOf("[");
            var W = null;
            if (Y != -1) {
                var X = M.substring(0, Y);
                W = M.substring(Y);
                var a = W.indexOf("-");
                M = X + W.substring(1, a)
            }
            var Z = "select * from " + M;
            if (V == 1 || V == 3) {
                Z += " limit 20;"
            } else {
                if (V == 0) {
                    Z += " where rownum <= 20;"
                }
            }
            if (W) {
                N.setValue("--该库下此分表下标范围：" + W + "\n" + Z)
            } else {
                N.setValue(Z)
            }
            F.execute_query(L.dbId, Z, U)
        }
    });
     //baisui
    Dbmis.SQLConsole.superclass.constructor.call(this, {
        title: Q,
        layout: "border",
        iconCls: "notepad",
        tbar: [
        {
            text: "执行（F8）",
            tooltip: "执行SQL语句(F8)",
            iconCls: "runSQL",
            handler: function () {
              //alert("L.dbId:"+L.dbId+" "+L.getExecutedSQL());
                F.execute_query(L.dbId, L.getExecutedSQL(), E.getValue())
               O.activate(0);
               //alert(SK);
              // F.setTitle("查询结果:xxxxxdddddddd");
            }
        }
       ],
        items: [new Ext.Panel({
            region: "north",
            layout: "border", 
            height:100,
            items: [N]}),O]
    });
    this.changeDataBase = function (X, W) {
        this.dbId = X;
        this.dbName = W;
        this.setTitle(W)
    };
    this.downloadCSV = function (W, X) {
        if (W == "" || X == "") {
            return show2("提示", "请选择正确的数据库以及输入sql后查询。")
        }
        Ext.Ajax.request({
            url: "/user/downLoad.jsn?_input_charset=UTF-8",
            params: {
                dbId: W,
                sql: X
            },
            success: function (Z, Y) {
                downLoadFile({
                    parameterkey: Z.responseText
                }, "database", "execute_download")
            }
        })
    };
    this.getExecutedSQL = function () {
        var W = N.getSelection();
        if (W) {
            return W
        } else {
            return N.getValue()
        }
    };
    this.fillWorkspaceText = function () {
       // Ext.Ajax.request({
       //     url: "/user/workspaceText.htm",
       //     success: function (X, W) {
       //         N.setValue(X.responseText)
       //     }
       // })
    };
    var P = null,
        T = null;
    this.loadUserConfigSql = function () {
        var X = converArr([{
            name: "lastUseTime",
            hidden: true
        }, {
            name: "useTimes",
            hidden: true
        }, {
            name: "red",
            hidden: true
        }, {
            name: "id",
            hidden: true
        }, {
            name: "title",
            header: "标题",
            width: 180,
            renderer: function (Z, a, Y) {
                var b = Y.data;
                if (b["red"] == 1) {
                    return '<div style="color:red">' + Z + "</div>"
                }
                return Z
            }
        }, {
            name: "id",
            header: "使用信息",
            width: 140,
            renderer: function (Z, a, Y) {
                var b = Y.data;
                return "<pre><b>最后使用时间：</b>\n" + b["lastUseTime"] + "\n<b>使用次数：</b>" + b["useTimes"] + "</pre>"
            }
        }, {
            name: "sqlText",
            header: "SQL详情",
            width: 360,
            renderer: formatComment
        }, {
            name: "id",
            header: "操作",
            renderer: function (Z, a, Y) {
                return "<a href=\"javascript:Ext.getCmp('" + L.id + "').editUserSqlWindow(1)\">编辑</a> <a href=\"javascript:Ext.getCmp('" + L.id + "').deleteRow()\">删除</a>"
            }
        }], true);
        var W = new Ext.data.JsonStore({
            url: "/user/getUserSqls.jsn",
            autoLoad: true,
            fields: X.fields
        });
        P = new Ext.grid.GridPanel({
            region: "center",
            sm: new Ext.grid.RowSelectionModel({
                singleSelect: true
            }),
            viewConfig: createView("没有路由规则信息"),
            columns: X.columns,
            store: W,
            listeners: {
                "dblclick": function () {
                    L.checkOneUserSqlRow()
                }
            }
        });
        T = new Ext.Window({
            width: 850,
            height: 530,
            layout: "border",
            title: "用户自定义SQL(双击直接选中)",
            items: [P],
            buttonAlign: "center",
            buttons: [{
                text: "新增",
                handler: function () {
                    L.editUserSqlWindow(0, T)
                }
            }, {
                text: "确定",
                handler: function () {
                    L.checkOneUserSqlRow()
                }
            }, {
                text: "关闭",
                handler: function () {
                    T.close()
                }
            }]
        });
        T.show()
    };
    this.deleteRow = function () {
        Ext.MessageBox.confirm("提示", "确认要删除这个配置吗，删除后无法恢复哦。", function (W) {
            if (W == "yes") {
                var X = P.getSelectionModel().getSelected().data;
                ajax({
                    url: "/user/deleteUserSqls.jsn?_input_charset=UTF-8&id=" + X["id"],
                    success: function (Z) {
                        var Y = getJson(Z.responseText);
                        if (Y.status == 1) {
                            P.getStore().load()
                        } else {
                            show2("提示", Y.msg)
                        }
                    }
                })
            }
        })
    };
    this.checkOneUserSqlRow = function () {
        var W = P.getSelectionModel().getSelected();
        if (!W) {
            return show2("提示", "请选择一条历史SQL进行操作，或直接双击即可。")
        }
        var Y = W.data;
        var X = N.getValue();
        if (X) {
            N.setValue(X + "\n\n--" + Y["title"] + "\n" + Y["sqlText"])
        } else {
            N.setValue("--" + Y["title"] + "\n" + Y["sqlText"])
        }
        T.close()
    };
    this.editUserSqlWindow = function (X) {
        var W = {
            red: "0"
        };
        if (X) {
            W = P.getSelectionModel().getSelected().data
        }
        var Z = new Ext.form.TextField({
            width: 280,
            fieldLabel: "标题",
            emptyText: "为方便您定位SQL",
            value: W["title"]
        });
        var c = createNormalComm3(280, [
            ["0", "否"],
            ["1", "是"]
        ], "红色提示", W["red"]);
        var Y = new Ext.form.TextArea({
            width: 380,
            height: 100,
            fieldLabel: "SQL语句",
            emptyText: "输入SQL长度请小于4000个字符",
            value: W["sqlText"]
        });
        var b = new Ext.FormPanel({
            bodyStyle: "padding:5px",
            defaultType: "textfield",
            items: [Z, c, Y],
            region: "center"
        });
        var a = new Ext.Window({
            width: 520,
            height: 240,
            layout: "border",
            title: "编辑自定义SQL",
            items: [b],
            buttonAlign: "center",
            buttons: [{
                text: "保存",
                handler: function () {
                    if (!Z.getValue()) {
                        return show2("提示", "标题不能为空")
                    }
                    if (!Y.getValue()) {
                        return show2("提示", "SQL语句不能为空")
                    }
                    ajax({
                        url: "/user/saveUserSqls.jsn?_input_charset=UTF-8",
                        params: {
                            title: Z.getValue(),
                            isRed: c.getValue(),
                            sql: Y.getValue(),
                            id: W["id"]
                        },
                        success: function (e) {
                            var d = getJson(e.responseText);
                            if (d.status == 1) {
                                P.getStore().load();
                                a.close()
                            } else {
                                show2("提示", d.msg)
                            }
                        }
                    })
                }
            }, {
                text: "关闭",
                handler: function () {
                    a.close()
                }
            }]
        });
        a.show();
        T.disable();
        a.on("beforedestroy", function () {
            T.enable()
        })
    };
    this.myConfig = function () {
        var g = createNormalComStore([
            ["200", "200"],
            ["100", "100"],
            ["50", "50"],
            ["20", "20"]
        ]);
        var Z = createNormalComStore([
            ["MM/dd/yyyy HH:mm:ss", "MM/dd/yyyy HH:mm:ss"],
            ["yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss"]
        ]);
        var Y = createNormalComStore([
            ["MM/dd/yyyy", "MM/dd/yyyy"],
            ["yyyy-MM-dd", "yyyy-MM-dd"]
        ]);
        var b = createNormalComStore([
            ["Excel", "Excel"],
            ["CSV", "CSV"]
        ]);
        var W = createNormlCommon(200, 24, g, "默认查询量", "200", false);
        var c = createNormlCommon(200, 24, Z, "DateTime格式", "MM/dd/yyyy HH:mm:ss", false);
        var X = createNormlCommon(200, 24, Y, "Date格式", "MM/dd/yyyy", false);
        var e = createNormlCommon(200, 24, b, "导出格式", "Excel", false);
        var a = new Ext.form.FormPanel({
            region: "center",
            width: 50,
            labelAlign: "center",
            frame: true,
            items: [W, c, X, e]
        });
        Ext.Ajax.request({
            url: "/user/getMySearchConfig.jsn?_input_charset=UTF-8",
            success: function (j, h) {
                var i = Ext.util.JSON.decode(j.responseText);
                if (i["hashInfo"] == "1") {
                    W.setValue(i["searchNumRow"]);
                    c.setValue(i["dateTimeFormat"]);
                    X.setValue(i["dateFormat"]);
                    e.setValue(i["downloadModel"])
                }
            }
        });
        var d = new Ext.Window({
            title: "请选择定义部分",
            width: 340,
            height: 220,
            layout: "border",
            items: [a],
            buttons: [{
                text: "确 定",
                handler: function () {
                    f();
                    d.close()
                }
            }, {
                text: "取 消",
                handler: function () {
                    d.close()
                }
            }]
        });
        var f = function () {
            Ext.Ajax.request({
                url: "/user/userSearchConfig.jsn?_input_charset=UTF-8",
                params: {
                    searchNumRow: W.getValue(),
                    dateTimeFormat: c.getValue(),
                    dateFormat: X.getValue(),
                    downloadModel: e.getValue()
                },
                success: function (i, h) {
                    show2("提示", "用户信息保存成功。")
                },
                failure: function (i, h) {
                    alert("错误信息", i.responseText)
                }
            })
        };
        d.show()
    }
};
Ext.extend(Dbmis.SQLConsole, Ext.Panel);


Dbmis.ConsoleManager = function () {
    var A = this;
    var B = new Dbmis.WelcomeConsole();
    var D = new Dbmis.TableStructureConsole(A);
    var C = new Dbmis.SQLConsole("", "", "", "", "1");
    C.title = "SQL Plus";
    C.closable = false;
    if (user_nick != "Guest") {
        C.fillWorkspaceText()
    }
    Dbmis.ConsoleManager.superclass.constructor.call(this, {
        id: "console_manager",
        activeTab: 0,
        resizeTabs: true,
        deferredRender: false,
        minTabWidth: 160,
        tabWidth: 160,
        enableTabScroll: true,
        defaults: {
            autoScroll: true
        },
        items: [B, D, C],
        listeners: {
            tabchange: function (F) {
                var G = A.getActiveTab().id;
                if (G == "createItem") {
                    var E = Ext.getCmp(G);
                    E.refreshItems()
                }
            }
        }
    });
    this.addConsole = function (F, E, G) {
        var H = (G == 0 ? 1 : G);
        A.normalOpt(new Dbmis.SQLConsole(F, E, G, "", H))
    };
    this.addConsole2 = function (F, E, H, G) {
        var I = (H == 0 ? 1 : H);
        A.normalOpt(new Dbmis.SQLConsole(F, E, H, G, I))
    };
    this.addProcedureGrid = function () {
        A.normalOpt(new Dbmis.ProcedureGridPanel())
    };
    this.addProjectPanel = function () {
        A.normalOpt(new Dbmis.ProjectsDisplayPanel())
    };
    this.getTableStructureConsole = function () {
        return D
    };
    this.addRecycleBin = function (E) {
        A.normalOpt(new Dbmis.RecycleBinItems(E))
    };
    this.addAllUserItem = function () {
        A.normalOpt(new Dbmis.AllCreateTab())
    };
    this.addDispOp = function () {
        A.normalOpt(new Dbmis.DispOptionPanel())
    };
    this.normalOpt = function (E) {
        E.closable = true;
        A.add(E);
        A.setActiveTab(E)
    }
};
Ext.extend(Dbmis.ConsoleManager, Ext.TabPanel);
var findLabel = new Ext.form.Label({text:"命中条数："});
Dbmis.DynamicGridPanel = Ext.extend(Ext.grid.GridPanel, {
    initComponent: function () {
        var B = new Ext.data.JsonReader();
        Ext.apply(B,{ read : function(response){
        
        var json = response.responseText;
        var o = Ext.decode(json);
       
       findLabel.setText("命中条数："+o.metaData.allmatchcount+"条");
       
        if(!o) {
            throw {message: 'JsonReader.read: Json object not found'};
        }
        return this.readRecords(o);
    }});
        var A = {
            tbar:[
       findLabel
       ],
            enableColLock: false,
            loadMask: true,
            border: false,
            stripeRows: true,
            autoScroll: true,
            deferredRender: false,
            viewConfig: new Ext.grid.GridView({
                emptyText: "结果集为空!",
                autoFill: true,
                forceFit: false,
                templates: pubTempates
            }),
            store: new Ext.data.Store({
                proxy: new Ext.data.HttpProxy({
                    url: "/runtime/index_query.ajax?action=index_query_action&event_submit_do_query=y&resulthandler=advance_query_result",
                    method: "post"
                }),
                reader: B,
                autoLoad: false
            }),
            columns: []
        };
        Ext.apply(this, A);
        Ext.apply(this.initialConfig, A);
        Dbmis.DynamicGridPanel.superclass.initComponent.apply(this, arguments)
    },
    onRender: function (B, A) {
        this.colModel.defaultSortable = true;
        Dbmis.DynamicGridPanel.superclass.onRender.call(this, B, A);
        this.el.mask("query...");
        this.store.on("metachange", function () {
            if (typeof (this.store.reader.jsonData.columns) === "object") {
                var C = [];
                if (this.rowNumberer) {
                    C.push(new Ext.grid.RowNumberer({
                        width: 30
                    }))
                }
                Ext.each(this.store.reader.jsonData.columns, function (D) {
                    D.renderer = columnQTipRenderer;
                    C.push(D)
                });
                this.getColumnModel().setConfig(C)
                
               
                
            }
            this.el.unmask()
        }, this);
        this.store.load()
    },
    //execute sql
    execute_query: function (appName, B, C) {
      
            this.dbId = appName;
            this.sql = B;
       
            this.store.reload({
                params: {
                    dbId: appName,
                    "execsql":this.sql
                },
                add: false
            });
    },
    execute_last: function () {
        if (this.sql) {
            this.execute_query(this.dbId, this.sql)
        }
    }
});
Dbmis.Utils = {
    addEmptySqlConsole: function () {
        var A = Ext.getCmp("console_manager");
        A.addConsole(0, "未知数据库", 1)
    },
    openProcedurePanel: function () {
        var A = Ext.getCmp("console_manager");
        A.addProcedureGrid()
    },
    openProjectPanel: function () {
        var A = Ext.getCmp("console_manager");
        A.addProjectPanel()
    },
    addDispOp: function () {
        var A = Ext.getCmp("console_manager");
        A.addDispOp()
    }
};