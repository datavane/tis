以下给出通过解析用户提交的描述任务内容，结合系统提供的json结构说明，输出结构化json解析结果的例子：

# 用户提交的内容

``` markdown
MySQL源端：host=192.168.1.10,port=3306,user=admin,password=pass123,database=orders,调用端所在的地点是北京
```

# 系统json结构说明
## MySQL-V8 json结构示例
```json
{
  "impl": "com.qlangtech.tis.plugin.ds.mysql.MySQLV8DataSourceFactory",
  "pkField": "name",
  "displayName": "MySQL-V8",
  "extendPoint": "com.qlangtech.tis.plugin.ds.BasicDataSourceFactory",
  "attrs": [
    {
      "ord": 0,
      "eprops": {
        "help": "数据源实例名称，请起一个有意义且唯一的名称",
        "label": "实例ID"
      },
      "describable": false,
      "pk": true,
      "type": 1,
      "key": "name",
      "required": true
    },
    {
      "ord": 1,
      "descriptors": {
        "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy": {
          "impl": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy",
          "displayName": "off",
          "extendPoint": "com.qlangtech.tis.plugin.ds.SplitTableStrategy",
          "attrs": [
            {
              "ord": 1,
              "eprops": {
                "help": "服务器节点连接地址，可以为IP或者域名",
                "placeholder": "192.168.28.200"
              },
              "describable": false,
              "pk": false,
              "type": 1,
              "key": "host",
              "required": true
            }
          ]
        },
        "com.qlangtech.tis.plugin.ds.split.DefaultSplitTableStrategy": {
          "impl": "com.qlangtech.tis.plugin.ds.split.DefaultSplitTableStrategy",
          "displayName": "on",
          "extendPoint": "com.qlangtech.tis.plugin.ds.SplitTableStrategy",
          "attrs": [
            {
              "ord": 1,
              "eprops": {
                "help": "\n将分布在多个数据库冗余节点中的物理表视作一个逻辑表，在数据同步管道中进行配置，输入框中可输入以下内容：\n\n* `192.168.28.200[00-07]` ： 单节点多库，导入 192.168.28.200:3306 节点的 order00,order01,order02,order03,order04,order05,order06,order078个库。也可以将节点描述写成：`192.168.28.200[0-7]`，则会导入 192.168.28.200:3306 节点的 order0,order1,order2,order3,order4,order5,order6,order78个库\n* `192.168.28.200[00-07],192.168.28.201[08-15]`：会导入 192.168.28.200:3306 节点的 order00,order01,order02,order03,order04,order05,order06,order078个库 和 192.168.28.201:3306 节点的 order08,order09,order10,order11,order12,order13,order14,order158个库，共计16个库\n\n[详细说明](http://tis.pub/docs/guide/datasource/multi-ds-rule)\n",
                "label": "分库节点",
                "placeholder": "127.0.0.1[00-31],127.0.0.2[32-63],127.0.0.3,127.0.0.4[9],baisui.com[0-9]",
                "rows": 3,
                "asyncHelp": true
              },
              "describable": false,
              "pk": false,
              "type": 2,
              "key": "nodeDesc",
              "required": true
            },
            {
              "ord": 8,
              "eprops": {
                "help": "\n识别分表的正则式，默认识别分表策略为 `(tabname)_\\d+` , 如需使用其他分表策略，如带字母[a-z]的后缀则需要用户自定义\n\n`注意`：如输入自定义正则式，表达式中逻辑表名部分，必须要用括号括起来，不然无法从物理表名中抽取出逻辑表名。\n\n**可参考**：https://github.com/qlangtech/tis/issues/361\n",
                "label": "分表识别",
                "placeholder": "(\\S+?)(_\\d+)?",
                "asyncHelp": true
              },
              "describable": false,
              "pk": false,
              "type": 1,
              "key": "tabPattern",
              "required": false
            },
            {
              "ord": 9,
              "eprops": {
                "help": "提交表单用户测试，所填正则式是否能正确识别物理分表。输入需要识别的逻辑表名，点击‘校验’按钮会进行自动识别。",
                "label": "测试表",
                "placeholder": "orderdetail"
              },
              "describable": false,
              "pk": false,
              "type": 1,
              "key": "testTab",
              "required": true
            },
            {
              "ord": 10,
              "eprops": {
                "help": "\n使用前缀匹配的样式，在flink-cdc表前缀通配匹配的场景中使用\n* 选择`是`：在增量监听流程中使用`逻辑表`+`*`的方式对目标表监听，例如，逻辑表名为`base`,启动时使用`base*` 对数据库中 `base01`,`base02`启用增量监听，在运行期用户又增加了`base03`表则执行逻辑会自动对`base03`表开启监听\n* 选择`否`：在增量监听流程中使用物理表全匹配的方式进行匹配。在运行期用户增加的新的分表忽略，如需对新加的分表增量监听生效，需要重启增量执行管道。",
                "dftVal": false,
                "label": "增量前缀匹配",
                "asyncHelp": true,
                "enum": [
                  {
                    "val": true,
                    "label": "是"
                  },
                  {
                    "val": false,
                    "label": "否"
                  }
                ]
              },
              "describable": false,
              "pk": false,
              "type": 5,
              "key": "prefixWildcardStyle",
              "required": true
            }
          ]
        }
      },
      "eprops": {
        "help": "\n如数据库中采用分表存放，可以开启此选项，默认为： `off`(不启用)\n\n`on`: 分表策略支持海量数据存放，每张表的数据结构需要保证相同，且有规则的后缀作为物理表的分区规则，逻辑层面视为同一张表。\n如逻辑表`order` 对应的物理分表为：  `order_01`,`order_02`,`order_03`,`order_04`\n\n[详细说明](https://tis.pub/docs/guide/datasource/multi-table-rule/)\n",
        "dftVal": "off",
        "label": "分库分表",
        "asyncHelp": true
      },
      "describable": true,
      "extendPoint": "com.qlangtech.tis.plugin.ds.SplitTableStrategy",
      "pk": false,
      "type": 1,
      "extensible": true,
      "key": "splitTableStrategy",
      "required": true
    },
    {
      "ord": 2,
      "eprops": {
        "dftVal": 3306,
        "label": "端口"
      },
      "describable": false,
      "pk": false,
      "type": 4,
      "key": "port",
      "required": true
    },
    {
      "ord": 3,
      "eprops": {
        "help": "数据库名,创建JDBC实例时用",
        "label": "数据库名"
      },
      "describable": false,
      "pk": false,
      "type": 1,
      "key": "dbName",
      "required": true
    },
    {
      "ord": 5,
      "eprops": {
        "dftVal": "root",
        "label": "用户名"
      },
      "describable": false,
      "pk": false,
      "type": 1,
      "key": "userName",
      "required": true
    },
    {
      "ord": 7,
      "eprops": {
        "label": "密码"
      },
      "describable": false,
      "pk": false,
      "type": 7,
      "key": "password",
      "required": true
    },
    {
      "ord": 12,
      "descriptors": {
        "com.qlangtech.tis.plugin.timezone.CustomizeTISTimeZone": {
          "impl": "com.qlangtech.tis.plugin.timezone.CustomizeTISTimeZone",
          "displayName": "customize",
          "extendPoint": "com.qlangtech.tis.plugin.timezone.TISTimeZone",
          "attrs": [
            {
              "ord": 1,
              "eprops": {
                "help": "设置服务端所在时区编码",
                "label": "时区编码",
                "placeholder": "example：Asia/Shanghai"
              },
              "describable": false,
              "pk": false,
              "type": 1,
              "key": "timeZone",
              "required": true
            }
          ]
        },
        "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone": {
          "impl": "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone",
          "displayName": "default",
          "extendPoint": "com.qlangtech.tis.plugin.timezone.TISTimeZone",
          "attrs": [
            {
              "ord": 1,
              "eprops": {
                "help": "选择服务端所在时区",
                "dftVal": "Asia/Shanghai",
                "label": "时区编码",
                "enum": [
                  {
                    "val": "Asia/Tokyo",
                    "label": "Asia/Tokyo"
                  },
                  {
                    "val": "Asia/Shanghai",
                    "label": "Asia/Shanghai"
                  }
                ]
              },
              "describable": false,
              "pk": false,
              "type": 5,
              "key": "timeZone",
              "required": true
            }
          ]
        }
      },
      "eprops": {
        "help": "设置服务端所在时区，有两种输入方式：1. default 从下拉框中选择，2. customize：用户手动输入时区编码",
        "dftVal": "default",
        "label": "所在时区"
      },
      "describable": true,
      "extendPoint": "com.qlangtech.tis.plugin.timezone.TISTimeZone",
      "pk": false,
      "type": 1,
      "extensible": false,
      "key": "timeZone",
      "required": true
    },
    {
      "ord": 13,
      "eprops": {
        "help": "数据数据",
        "label": "编码",
        "enum": [
          {
            "val": "gbk",
            "label": "GBK"
          },
          {
            "val": "utf8",
            "label": "UTF-8"
          }
        ]
      },
      "describable": false,
      "pk": false,
      "type": 5,
      "key": "encode",
      "required": true
    },
    {
      "ord": 14,
      "eprops": {
        "help": "\n与服务端通信时采用zlib进行压缩，效果请参考[https://blog.csdn.net/Shadow_Light/article/details/100749537](https://blog.csdn.net/Shadow_Light/article/details/100749537)\n",
        "dftVal": true,
        "label": "传输压缩",
        "asyncHelp": true,
        "enum": [
          {
            "val": true,
            "label": "是"
          },
          {
            "val": false,
            "label": "否"
          }
        ]
      },
      "describable": false,
      "pk": false,
      "type": 5,
      "key": "useCompression",
      "required": true,
      "advance": true
    },
    {
      "ord": 15,
      "eprops": {
        "label": "附加参数",
        "placeholder": "a=123&b=456"
      },
      "describable": false,
      "pk": false,
      "type": 1,
      "key": "extraParams",
      "required": false,
      "advance": true
    }
  ]
}
```
## MySQL-V8 json结构属性说明

以上是标准化MySQL-V8版本的插件的数据结构，以下是`插件结构`属性的说明：
1. impl: 对应插件的具体实现类,
2. pkField: `attrs`属性中罗列的列中作为主键的列,
3. displayName: 插件实例的名称
4. extendPoint: 插件的扩展点，其实也就是以上impl属性是继承于此
5. `attrs` 数组下元组
   1. ord: 属性的排序，可以当作属性重要性说明，优先级高的该值就小,
   2. eprops：内包含扩展属性
      1. label: 属性label属性 
      2. placeholder: 可当作用户输入值样例
      3. dftVal: 属性默认值,
      4. enum: 属性值可以从罗列的多个枚举值中选择一个作为属性值，内部可包含多个`{ "val": string, "label": string }`元素
      5. help：属性的帮助说明信息
   3. describable: 属性值表明是否为一个嵌套的`插件结构`
   4. descriptors：当以上`describable`=true时会有此属性，内部是一个Map数据结构，`Key`为插件插件完整实现类名，Value为内嵌插件结构说明。注意：在最终生成的json结果中，对应属性的`descVal`下的`impl`属性值必须与Map属性下Value对应实例的`impl`属性值严格一致
   5. pk: 是否为主键，当`pk=true`时，在用户提交的本文中根据抽取到对应的内容，作如下处理：
      * 没有抽取到对应值： 输出的`_primaryVal`属性对应的值不要自动生成（切记）
      * 抽取到对应的值：输出的`_primaryVal`属性值必须严格匹配正则式： `[A-Z\\da-z_]+` ，如有非法字符须进行**合理替换**以符合正则式，例如：识别得到“mysql-mysql-2”不符合正则式规范，**必须**进行**合理替换**变成“mysql_mysql_2”
   6. type: 属性值类型，值为int的类型，值对应的类型说明参考：`fieldType值说明`
   7. key: 属性键名称
   8. required: 是否必须输入
   9. advance: 是否为高级属性

## fieldType值说明

* 8: MULTI_SELECTABLE 多选
* 1: INPUTTEXT 单行输入
* 6: SELECTABLE 单选值
* 7: PASSWORD 密码
* 9: FILE 可设置文件输入
* 2: TEXTAREA 多行文本输入，例如可使用此类型输入代码块
* 3: DATE 日期
* 11: JDBCColumn
* 4: INT_NUMBER 整型数字
* 5: ENUM 多值枚举，单选
* 10: DateTime，日期时间，精确到秒
* 11: DECIMAL_NUMBER，浮点数字
* 12: 支持两种值： 1.DURATION：时间跨度，单位可使用： SECOND、MINUTE、HOUR，2.MEMORY：存储大小，单位可使用：BYTE、KIBI、MEGA


# 大模型解析说明

1. 解析后生成的json内容中，需要有全部attrs数组下元组对应的值
2. attrs数组下元组：如果不能从用户输入的内容中解析得到对应的值，元组下有`dftVal`（默认值）则就用该值作为输入值，如没有默认值保持输入项值为空即可
3. **重要警告：在处理`describable`为`true`的属性时，`descVal`下的`impl`属性值必须严格使用`descriptors`中对应的完整类名（key值），绝对不允许使用`displayName`的值。例如：**
  - ✅ 正确：`"impl": "com.qlangtech.plugins.incr.flink.launch.statbackend.FileSystemState"`
  - ❌ 错误：`"impl": "FSState"`（这是displayName）
  - ✅ 正确：`"impl": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy"`
  - ❌ 错误：`"impl": "off"`（这是displayName）

# 输出json内容示例：
根据 `用户提交的内容` 与 `系统json结构说明` 内容，期望经过大模型处理生成以下标准化json结构输出。以下是要点说明：

1. 以上 `json结构示例` 说到的 attrs下的`pk`属性true时，在用户提交的本文中如没有抽取到对应的内容，输出的`_primaryVal`属性对应的值不要自动生成（切记）.正如，如下输出的json内容中`vals.name._primaryVal`的值应该是空的，
因为用户提交的内容中并没有明确说明。
2. `json结构示例`中处理`describable`为`true`属性时，最终识别到的`descVal`下的`impl`属性值必须与`descriptors`Map属性下Value对应实例的`impl`属性值严格一致

   **特别注意：必须使用完整的类名路径，不能使用displayName的简写形式。**

   如下输出的json内容中：`vals.splitTableStrategy.descVal.impl`，由于splitTableStrategy的默认值为`off`，所以属性为`com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy`（注意：`off`只是displayName，绝对不能用作impl值）

    ``` json
    {
      "impl": "com.qlangtech.tis.plugin.ds.mysql.MySQLV8DataSourceFactory",
      "vals": {
        "name": {
          "_primaryVal": ""
        },
        "splitTableStrategy": {
          "descVal": {
            "impl": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy",
            "vals": {
              "host": {
                "_primaryVal": "192.168.1.10"
              }
            }
          }
        },
        "port": {
          "_primaryVal": 3306
        },
        "dbName": {
          "_primaryVal": "orders"
        },
        "userName": {
          "_primaryVal": "admin"
        },
        "password": {
          "_primaryVal": "pass123"
        },
        "timeZone": {
          "descVal": {
            "impl": "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone",
            "vals": {
              "timeZone": {
                "_primaryVal": "Asia/Shanghai"
              }
            }
          }
        },
        "encode": {
          "_primaryVal": ""
        },
        "useCompression": {
          "_primaryVal": true
        },
        "extraParams": {
          "_primaryVal": ""
        }
      }
    }
    ```
