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
 columns: [
  {name:"id",token:"ik",search:true}
 ]
}
```
