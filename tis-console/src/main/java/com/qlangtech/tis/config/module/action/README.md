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
}
```

### Response 

``` javascript
{
 success: true,
 bizresult: {
  
 }
}
```



