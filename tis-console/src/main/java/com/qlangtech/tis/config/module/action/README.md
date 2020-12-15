## 一键自动化构建TiDB 单表REST接口描述

### Url

http://host:8080/solr/config/config.ajax?action=collection_action&emethod=do_create

### Request Body

``` javascript
{
 datasource: {
    plugin: "TiKV",
    pdAddrs: "192.168.28.201:2379",
    dbName: "employees"
 },
 table: "employess",
 indexName: "employess",
 columns: [
  {name:"id",token:"ik",search:true}
 ]
 ,
 incr: {
   plugin: "TiCDC-Kafka"，
   mqAddress: "192.168.28.201:9092" ,
   topic: "test_topic"  ,
   groupId: "test_group" ,
   offsetResetStrategy: "earliest" #earliest or latest or none
 }
}
```

### Response 

``` javascript
{
 success: true,
 errormsg:["err1"],
 bizresult: {
  taskid: 123
 }
}
```

## 轮询索引构建结果

### Url

http://host:8080/solr/config/config.ajax?action=collection_action&emethod=do_get_status

### Request Body

``` javascript
{
 taskid: 123
 log: true # 是否显示日志
}
```

### Response 

``` javascript
{
 success: true,
 errormsg:["err1"], # 系统级异常信息
 bizresult: {
  complete: false,
  faild: false,
  stage: "dump", # dump,join,indexBuild,indexBackflow
  errs: "" ,
  logs: "" # 提交参数中 log 为true
 }
}
```

## 创建增量通道

如果之前创建的索引实例没有创建

## 执行全量构建

## 删除已创建的索引



