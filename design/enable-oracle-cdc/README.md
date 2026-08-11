# Oracle Docker 启动与 CDC（LogMiner）开启指南

> 适用环境：CentOS / Intel(x86_64) 芯片  
> 目标：在 Docker 中启动 Oracle 19c，并正确开启 Flink CDC（Debezium LogMiner）所需的数据库级与表级配置。

---

## 1. 镜像与执行模式说明

Flink CDC 单元测试中使用的是 `goodboy008/oracle-19.3.0-ee` 镜像：

- Intel 芯片请使用 tag：`non-cdb`
- ARM 芯片请使用 tag：`arm-non-cdb`

`non-cdb` 表示**非 CDB 架构**，数据库名与 SID 均为 `ORCLCDB`，JDBC 连接时使用 SID 格式。

---

## 2. Docker 启动命令

```bash
docker run -d \
  --name oracle-cdc \
  --privileged \
  --restart unless-stopped \
  -p 1521:1521 \
  -p 8080:8080 \
  -e ORACLE_PWD=top_secret \
  -e ORACLE_DATABASE=ORCLCDB \
  -e APP_USER=dbzuser \
  -e APP_USER_PASSWORD=dbz \
  -e ENABLE_ARCHIVELOG=true \
  -v /root/oracle/oradata:/opt/oracle/oradata \
  swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/goodboy008/oracle-19.3.0-ee:non-cdb
```

### 参数说明

| 参数 | 说明                                                                                                 |
|------|----------------------------------------------------------------------------------------------------|
| `--privileged` | Oracle 容器需要 `/dev/shm` 等系统资源，必须加。                                                                  |
| `-p 1521:1521` | Oracle 监听端口。                                                                                       |
| `-p 8080:8080` | APEX HTTP 端口（测试镜像也暴露此端口）。                                                                          |
| `ORACLE_PWD` | **镜像实际识别的 SYS 密码变量**。镜像元数据已烘焙默认值为 `top_secret`，DBeaver中以‘sys’作为用户名登录,测试代码中 `sys as sysdba` 也使用此密码。 |
| `ORACLE_DATABASE=ORCLCDB` | non-cdb 模式下的数据库名/SID。                                                                              |
| `APP_USER=dbzuser` / `APP_USER_PASSWORD=dbz` | 镜像预创建的 CDC 连接用户，对应测试代码中的 `CONNECTOR_USER` / `CONNECTOR_PWD`。                                       |
| `ENABLE_ARCHIVELOG=true` | 镜像默认已开启归档，显式写上更易读。                                                                                 |
| `-v /root/oracle/oradata:/opt/oracle/oradata` | 数据持久化，避免容器删除后数据丢失。                                                                                 |

### 等待容器就绪

镜像启动完成后会输出：

```text
DATABASE IS READY TO USE!
```

与 `OracleContainer` 中的 `waitStrategy` 保持一致。首次启动通常需要 3~5 分钟。

```bash
docker logs -f oracle-cdc
```

---

## 3. JDBC 连接串

non-cdb 模式下使用 **SID 格式**：

```text
jdbc:oracle:thin:@<宿主机IP>:1521:ORCLCDB
```

示例：

```text
jdbc:oracle:thin:@192.168.1.100:1521:ORCLCDB
```

### 内置用户

| 用户 | 密码 | 用途 |
|------|------|------|
| `sys as sysdba` | `top_secret` | DBA 用户，执行系统级 CDC 配置。 |
| `debezium` | `dbz` | 业务 schema 用户，拥有 `DEBEZIUM` schema。 |
| `dbzuser` | `dbz` | CDC 连接用户（已预创建）。 |

> `debezium` 和 `dbzuser` 用户已内置于镜像，通常无需手动创建。

---

## 4. 使用 sqlplus 验证归档日志（ARCHIVELOG）

### 4.1 进入容器并登录

```bash
docker exec -it oracle-cdc sqlplus / as sysdba
```

### 4.2 查询数据库归档模式

```sql
SELECT LOG_MODE FROM V$DATABASE;
```

### 4.3 期望结果

```text
LOG_MODE
------------
ARCHIVELOG
```

### 4.4 如果仍是 NOARCHIVELOG

需要手动停库并切换到归档模式：

```sql
SHUTDOWN IMMEDIATE;
STARTUP MOUNT;
ALTER DATABASE ARCHIVELOG;
ALTER DATABASE OPEN;
```

再次验证：

```sql
SELECT LOG_MODE FROM V$DATABASE;
```

---

## 5. 使用 sqlplus 验证补充日志（Supplemental Logging）

### 5.1 查询数据库级最小补充日志

```sql
SELECT SUPPLEMENTAL_LOG_DATA_MIN FROM V$DATABASE;
```

### 5.2 期望结果

```text
SUPPLEMENTAL_LOG_DATA_MI
------------------------
YES
```

或显示为 `IMPLICIT` 也表示已开启。

### 5.3 如果未开启，执行

```sql
ALTER DATABASE ADD SUPPLEMENTAL LOG DATA;
```

---

## 6. 给 CDC 目标表开启表级补充日志

Flink CDC 测试 SQL 中的标准写法：

```sql
ALTER TABLE DEBEZIUM.CUSTOMERS ADD SUPPLEMENTAL LOG DATA (ALL) COLUMNS;
```

如果只希望补充主键列，可使用：

```sql
ALTER TABLE DEBEZIUM.CUSTOMERS ADD SUPPLEMENTAL LOG DATA (PRIMARY KEY) COLUMNS;
```

> **建议**：Flink CDC / Debezium 推荐使用 `(ALL) COLUMNS`，否则 UPDATE 语句中非主键列的旧值可能无法完整捕获。

### 6.1 验证单表补充日志

```sql
SELECT SUPPLEMENTAL_LOG_DATA_ALL FROM DBA_TABLES
WHERE OWNER = 'DEBEZIUM' AND TABLE_NAME = 'CUSTOMERS';
```

或查询：

```sql
SELECT * FROM DBA_LOG_GROUPS
WHERE OWNER = 'DEBEZIUM' AND TABLE_NAME = 'CUSTOMERS';
```

---

## 7. CDC 用户权限配置

如需创建自定义 CDC 用户，以 `sys as sysdba` 登录后执行：

```sql
CREATE USER cdc_user IDENTIFIED BY cdc_password;

GRANT CREATE SESSION        TO cdc_user;
GRANT LOGMINING             TO cdc_user;
GRANT SELECT ANY TRANSACTION TO cdc_user;
GRANT SELECT ANY DICTIONARY TO cdc_user;
GRANT EXECUTE_CATALOG_ROLE  TO cdc_user;
GRANT SELECT_CATALOG_ROLE   TO cdc_user;

-- LogMiner 包执行权限
GRANT EXECUTE ON DBMS_LOGMNR   TO cdc_user;
GRANT EXECUTE ON DBMS_LOGMNR_D TO cdc_user;

-- 关键动态性能视图
GRANT SELECT ON V_$DATABASE            TO cdc_user;
GRANT SELECT ON V_$LOG                 TO cdc_user;
GRANT SELECT ON V_$LOGFILE             TO cdc_user;
GRANT SELECT ON V_$ARCHIVED_LOG        TO cdc_user;
GRANT SELECT ON V_$ARCHIVE_DEST_STATUS TO cdc_user;
GRANT SELECT ON V_$LOGMNR_CONTENTS     TO cdc_user;
GRANT SELECT ON V_$LOGMNR_LOGS         TO cdc_user;
GRANT SELECT ON V_$TRANSACTION         TO cdc_user;

-- 对要捕获的表授予 SELECT
GRANT SELECT ON DEBEZIUM.CUSTOMERS TO cdc_user;
```

---

## 8. Flink CDC 连接示例

```java
Properties debeziumProperties = new Properties();
debeziumProperties.setProperty("debezium.log.mining.strategy", "online_catalog");
debeziumProperties.setProperty("debezium.database.history.store.only.captured.tables.ddl", "true");

OracleSource.<String>builder()
    .hostname("192.168.1.100")
    .port(1521)
    .database("ORCLCDB")          // SID，不是 PDB
    .schemaList("DEBEZIUM")
    .tableList("DEBEZIUM.CUSTOMERS")
    .username("cdc_user")
    .password("cdc_password")
    .debeziumProperties(debeziumProperties)
    .build();
```

---

## 9. 完整验证清单

| 检查项 | 命令 | 期望结果 |
|--------|------|----------|
| 容器已就绪 | `docker logs oracle-cdc \| grep "DATABASE IS READY TO USE!"` | 包含该日志 |
| 归档已开启 | `SELECT LOG_MODE FROM V$DATABASE;` | `ARCHIVELOG` |
| 数据库级补充日志 | `SELECT SUPPLEMENTAL_LOG_DATA_MIN FROM V$DATABASE;` | `YES` / `IMPLICIT` |
| 表级补充日志 | `SELECT * FROM DBA_LOG_GROUPS WHERE OWNER='DEBEZIUM' AND TABLE_NAME='CUSTOMERS';` | 有记录 |
| CDC 用户可连接 | `sqlplus cdc_user/cdc_password@//localhost:1521/ORCLCDB` | 登录成功 |

---

## 10. 注意事项

1. **`ORACLE_PWD` 不是 `ORACLE_PASSWORD`**  
   该镜像元数据中实际识别的变量是 `ORACLE_PWD`，默认值为 `top_secret`。`OracleContainer.java` 中写的 `ORACLE_PASSWORD` 可能是历史兼容或同时支持两个变量，但稳妥起见请使用 `ORACLE_PWD`。

2. **non-cdb 仅适合测试**  
   Oracle 19c 生产环境推荐 CDB/PDB 架构，需要额外配置 `CONTAINER=ALL`、common user（`C##` 前缀）等。

3. **务必挂载数据卷**  
   不挂载 `/opt/oracle/oradata` 时，容器删除后所有数据会丢失。

4. **镜像非官方授权**  
   `goodboy008/oracle-19.3.0-ee` 是社区测试镜像，生产环境请使用官方授权镜像或物理/虚拟机部署。

---

## 参考来源

- [docker.io/goodboy008/oracle-19.3.0-ee:non-cdb image metadata](https://docker.aityp.com/image/docker.io/goodboy008/oracle-19.3.0-ee:non-cdb)
- [Flink CDC Oracle Tutorial](https://flink-tpc-ds.github.io/flink-cdc-connectors/release-2.4/content/quickstart/oracle-tutorial.html)
- [Oracle CDC: Supplemental Logging, LogMiner Configuration, and Production Setup](https://streamkap.com/resources-and-guides/oracle-cdc-supplemental-logging)
- [Debezium Connector for Oracle](https://hevodata.com/learn/debezium-connector-for-oracle/)
