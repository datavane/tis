## 需求描述
已知达梦数据库的内核大部份与Oracle数据库一致，目前需要在flink-cdc的达梦项目中（/opt/misc/flink-cdc/flink-cdc-connect/flink-cdc-source-connectors/flink-connector-dameng-cdc）
，以DamengSourceBuilder类作为入口（对应文件路径：/opt/misc/flink-cdc/flink-cdc-connect/flink-cdc-source-connectors/flink-connector-dameng-cdc/src/main/java/org/apache/flink/cdc/connectors/oracle/source/DamengSourceBuilder.java)
可以通过以下代码构建出 flink的 org.apache.flink.api.connector.source.Source 实例（DamengIncrementalSource 实现了 org.apache.flink.api.connector.source.Source 接口）
```java
  DamengIncrementalSource<DTO> incrSource
                = DamengIncrementalSource.builder()
                .hostname("")
                .password("")
                .username("")
                .build();
```
需要通过以上的方式获得一个incrSource实例（在线上生产环境中能够可靠运行的），目前已经在flink-connector-dameng-cdc项目中已经有一个骨架雏形，DamengIncrementalSource内部依赖的DamengDialect类（路径为：/opt/misc/flink-cdc/flink-cdc-connect/flink-cdc-source-connectors/flink-connector-dameng-cdc/src/main/java/org/apache/flink/cdc/connectors/oracle/source/DamengDialect.java）
目前还有多处编译不通过的点，需要再考虑具体实现使其编译通过。
## 基本现状

* 达梦Dameng debezium社区包
  我已经从达梦的开发社区获得了 达梦dameng的debezium的代码实现。由于达梦是商业软件，所以在开源的debezium项目中，没有针对达梦数据库提供封装实现，幸好达梦的社区按照debezium的规范提供达梦数据库的实现。
  目前我已经将代码下载下来，安放在/opt/misc/debezium/debezium-connector-dameng中。
* debezium-connector-dameng 中的实现可以参照的代码
  由于dameng数据库的内核是按照Oracle的内核开发的（debezium-connector-oracle工程路径为/opt/misc/debezium/debezium-connector-oracle），所以在debezium项目的封装中的确有很大一部分代码的共通的，例如：LogMinerHelper类
  1. dameng debezium实现：/opt/misc/debezium/debezium-connector-dameng/src/main/java/org/devlive/connector/dameng/logminer/LogMinerHelper.java
  2. oracle debezium实现：/opt/misc/debezium/debezium-connector-oracle/src/main/java/io/debezium/connector/oracle/logminer/LogMinerHelper.java
 
  相信debezium-connector-dameng的开发者在开发是基于debezium-connector-oracle项目而来的，里面有大量的代码是共通的
  1. 如上，类名是相同的，如上LogMinerHelper类，不同的只是package路径将`io/debezium/connector/oracle`改成了`org/devlive/connector/dameng`
  2. 有的代码是功能是一致的，例如：
     * OracleConnection.java: /opt/misc/debezium/debezium-connector-oracle/src/main/java/io/debezium/connector/oracle/OracleConnection.java
     * DamengConnection.java: /opt/misc/debezium/debezium-connector-dameng/src/main/java/org/devlive/connector/dameng/DamengConnection.java
     以上两个只是 Connection的前缀（Oracle，Dameng）不同，他们继承的debezium基础类 JdbcConnection（路径：/opt/misc/debezium/debezium-core/src/main/java/io/debezium/jdbc/JdbcConnection.java）是相同的
  不过debezium的 dameng 和oracle的实现版本还是有区别的，例如在文件（/opt/misc/debezium/debezium-connector-dameng/doc/log-mining-configuration-comparison.md）总结到的（可能还有其他区别，这里只总结了一方面），所以debezium-connector-dameng有实现细节上和debezium-connector-oracle是有不少区别的。

* 可以借鉴的flink-connector-oracle-cdc
  在Flink-CDC项目中依赖debezium-connector-oracle项目实现了提供给Flink流运算的Oracle Source算子（工程路径：/opt/misc/flink-cdc/flink-cdc-connect/flink-cdc-source-connectors/flink-connector-oracle-cdc），
  正因为debezium-connector-dameng和debezium-connector-oracle是有不少区别，所以在flink-cdc层的flink-connector-dameng-cdc代码不能照搬flink-connector-oracle-cdc工程中的代码。
  
  以上说到的DamengIncrementalSource代码在flink-connector-oracle-cdc工程中对应的代码为 OracleSourceBuilder.java(路径为/opt/misc/flink-cdc/flink-cdc-connect/flink-cdc-source-connectors/flink-connector-oracle-cdc/src/main/java/org/apache/flink/cdc/connectors/oracle/source/OracleSourceBuilder.java) 中的 OracleIncrementalSource类
  

