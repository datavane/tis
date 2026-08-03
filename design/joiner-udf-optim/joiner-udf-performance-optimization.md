# JoinerUDF Join 算子性能优化方案

> 日期：2026-08-02
> 涉及代码库：`plugins/tis-transformer`
> 核心类：`com.qlangtech.tis.plugin.datax.transformer.impl.JoinerUDF`

---

## 1. 背景与问题现象

`JoinerUDF` 是 TIS 提供的 Joiner Transformer 算子：DataX 从源表每读出一条记录，进入 `JoinerUDF.evaluate()`（`JoinerUDF.java:94`），以主表记录的匹配列值为条件，到目标（维度）表中查出对应记录，把目标列合并进 `ColumnAwareRecord` 后流向 Writer。

测试环境实测：开启该 Join transformer 后，DataX 任务整体吞吐被显著拖慢。

---

## 2. 现状实现梳理

### 2.1 类结构

```
JoinerUDF (MultiStepsSupportHost，三步向导)
├── Step1: JoinerSelectDataSource          — 选择维度表数据源
├── Step2: JoinerSelectTable               — 选择目标表 + cache 策略（多态 TargetRowsCache）
│     └── cache: TargetRowsCache
│           ├── TargetRowsCacheOn          — Caffeine LRU（maxSize ≤ 10万，TTL ≤ 600s）
│           └── TargetRowsCacheOff         — 不缓存，每条记录直查 DB
└── Step3: JoinerSetMatchConditionAndCols  — matchCondition / filterConditions / targetCols / colPrefix / skipError
```

### 2.2 单条记录执行链路

```
evaluate(record)
  ├─ getOneStepOf(Step1/2/3) + getDataSourceFactory()     # 每条记录都重新取
  ├─ 构造 JoinCacheKey（2 个 ArrayList，hashCode 时 params.toArray() 再分配）
  ├─ cache.isOn() ?
  │    ├─ On  : Caffeine 查找，miss → selectFromDB() → set2Cache
  │    └─ Off : selectFromDB()                            # 每条记录都查
  └─ 命中 → record.setColumn(prefix + col, val)

selectFromDB()                                            # JoinerUDF.java:187
  ├─ getJdbcConnection()（首次创建，之后复用 transient 实例字段）
  ├─ getSelectSQL()（首次拼接，之后复用）
  └─ try (PreparedStatement ps = connection.preparedStatement(sql)) {   # ← 每次 miss 新建+关闭
       设置参数 → executeQuery → 取首行
     }
```

### 2.3 已确认的架构事实

| 事实 | 依据 |
|---|---|
| UDF 实例**每通道一个、长生命周期**，单线程访问 | `_jdbcConnection` / `_selectSQL` transient 缓存能正常工作即为佐证；JDBC Connection 非线程安全，若跨线程共享早已出错 |
| Flink-CDC 增量路径（`ReocrdTransformerMapper`）同样每 subtask 一个实例，且**无 init/open 生命周期钩子** | 任何重初始化必须在 `evaluate()` 内懒加载（线程安全） |
| `JDBCConnection` 是裸驱动连接，无连接池、无 statement 缓存 | `JDBCConnection.preparedStatement()` 直接委托 `conn.prepareStatement()` |
| 连接生命周期 = 线程生命周期 | `JDBCConnectionPool`（ThreadLocal）的 pooled wrapper `close()` 是 no-op |
| 维度表重复 key 取首行 | `selectFromDB` 只调一次 `resultSet.next()` |
| 主表 key 为 null 时抛 NPE 走 skipError 逻辑 | `Objects.requireNonNull(cacheKey.getPrimaryVal(index))` |

---

## 3. 性能根因分析

### 3.1 主因：N+1 同步点查

每条源记录产生**一次同步 JDBC 点查**（cache miss 时），耗时 = 网络 RTT + SQL parse/plan/execute。单通道为单线程串行执行，因此：

```
单通道吞吐 ≈ 1 / 单次点查耗时
```

举例：单次点查 2ms → 单通道上限 500 rec/s。而 DataX 无 transformer 时可达 1万~10万 rec/s。Transformer 成为全链路瓶颈，任务总耗时被锁定在 `记录数 × 单次点查耗时 / 通道数`。

### 3.2 加剧因素

1. **PreparedStatement 每次 miss 新建并关闭**（try-with-resources）：驱动无法复用服务端预编译句柄，MySQL 每次都要重新 parse；同时产生大量短命对象。
2. **LRU 缓存约束**：`maxSize ≤ 100,000`、`expireAfterWrite/Access ≤ 600s`（`TargetRowsCacheOn.OnDesc` 校验硬编码）。主表 join key 基数高（如每行 key 都不同）时命中率趋近 0，缓存只剩开销；任务运行超过 10 分钟后热点 key 过期，重复回源。
3. **无预加载**：每个不同的 key 至少付出 1 次 DB 往返。
4. **每条记录的固定开销**：`getOneStepOf` ×3 + `getDataSourceFactory()` + key 构造的多次对象分配。
5. **运维层面（需自查）**：若维度表匹配列**无索引**，每次点查都是一次全表扫描 —— 测试环境尤其常见，属于雪上加霜。

### 3.3 为什么不能简单并行化

DataX transformer 契约是**严格的逐条流式**（`evaluate(record)` 进一条出一条），在 transformer 内部做记录缓冲/批量 `IN(...)` 查询会破坏管道模型（背压、错误处理、记录顺序全部复杂化），故否决。提高并行度的正确姿势是调大 DataX 通道数（见 §8）。

---

## 4. 优化方案总览

| 方案 | 收益 | 改动量 | 结论 |
|---|---|---|---|
| **A. 新增全量预加载缓存策略 `TargetRowsCacheFull`** | 维度表一次性批量读入 HashMap，每条记录纯内存查找；DB 压力从 N 次降为 1 次；吞吐可提升到 10万+ rec/s | 中 | **采纳（主方案）** |
| **B. `selectFromDB` 复用 PreparedStatement** | 省掉每次 miss 的 prepare/close 开销（约占单次查询 20~40%） | 小 | **采纳** |
| **C. `evaluate()` 提升每记录固定开销**（step 插件引用、prefix、keyBuilder 等改懒初始化 transient 字段） | 消除每记录的重复反射式取值与对象分配 | 小 | **采纳** |
| D. 微批 `IN(...)` 查询 | 点查次数降为 N/batchSize | 大，破坏流式契约 | 否决 |
| E. 源表与维度表同库时把 join 下推到 Reader SQL | 最优性能 | 改变产品语义，仅适用于同库 | 作为使用建议写入文档，不改代码 |

> A/B/C 全部为**增量改动**：`TargetRowsCacheOn` / `TargetRowsCacheOff` 的现有行为完全不变，用户在建管道时自行选择缓存策略。

---

## 5. 详细设计

### 5.1 `TargetRowsCache` 增加扩展钩子（default 方法，On/Off 零改动）

文件：`plugins/tis-transformer/src/main/java/com/qlangtech/tis/plugin/datax/transformer/impl/joiner/TargetRowsCache.java`

```java
/** 是否为全量预加载模式（是则 JoinerUDF 首次 evaluate 时触发全量加载，之后不再访问 DB） */
public boolean isFullPreload() {
    return false;
}

/** 触发全量加载（仅 isFullPreload()=true 的实现需要覆写） */
public void preload(BulkLoader loader) {
    // no-op for On/Off
}

public interface BulkLoader {
    void load(RowSink sink) throws Exception;
}

public interface RowSink {
    void accept(JoinCacheKey key, JoinCacheValue value);
}
```

`JoinCacheKey` 增加辅助方法：

```java
public boolean hasNullPrimaryVal() {
    return primaryVals.stream().anyMatch(Objects::isNull);
}
```

### 5.2 新增 `TargetRowsCacheFull`（全量预加载策略）

新文件：`.../impl/joiner/cache/TargetRowsCacheFull.java`

```java
public class TargetRowsCacheFull extends TargetRowsCache implements IPluginStore.AfterPluginSaved {

    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER,
               validate = {Validator.require, Validator.integer})
    public Long maxRows = 200000L;                       // 护栏：防 OOM

    private transient Map<JoinCacheKey, JoinCacheValue> _fullCache;
    private transient volatile boolean loaded;
    private transient final Object loadLock = new Object();

    @Override public boolean isOn()         { return true; }   // 对 UI 呈现为"缓存开启"
    @Override public boolean isFullPreload() { return true; }

    @Override
    public void preload(BulkLoader loader) {
        if (loaded) return;
        synchronized (loadLock) {
            if (loaded) return;
            Map<JoinCacheKey, JoinCacheValue> map = new HashMap<>();
            loader.load((key, val) -> {
                if (map.size() >= maxRows) {
                    throw new IllegalStateException(
                        "dimension table row count exceeds maxRows=" + maxRows
                        + ", please switch to LRU cache mode for large dimension tables");
                }
                map.putIfAbsent(key, val);          // 首行赢，与现有点查行为一致
            });
            this._fullCache = map;
            this.loaded = true;                      // volatile 后写，保证 map 安全发布
        }
    }

    @Override
    public JoinCacheValue getFromCache(JoinCacheKey key) {
        if (!loaded) throw new IllegalStateException("full cache has not been preloaded");
        return _fullCache.get(key);
    }

    @Override
    public JoinCacheValue set2Cache(JoinCacheKey key, JoinCacheValue val) {
        throw new UnsupportedOperationException("full-preload mode does not cache single rows");
    }

    @Override public List<UDFDesc> getUDFDesc() {
        return Collections.singletonList(new UDFDesc("maxRows", String.valueOf(maxRows)));
    }

    @Override public void afterSaved(IPluginContext ctx, Optional<Context> c) {
        this._fullCache = null;
        this.loaded = false;
    }

    @TISExtension
    public static class FullDesc extends Descriptor<TargetRowsCache> {
        public boolean validateMaxRows(IFieldErrorHandler h, Context c, String f, String v) {
            long n = Long.parseLong(v);
            if (n < 1 || n > 2_000_000L) { h.addFieldError(c, f, "必须在 1 ~ 2,000,000 之间"); return false; }
            return true;
        }
        @Override public String getDisplayName() { return "On(Full Preload)"; }
    }
}
```

要点：

- **护栏**：加载中行数超 `maxRows` 立即 fail-fast，错误信息引导用户改用 LRU 模式。
- **重复 key**：`putIfAbsent` 首行赢，与现有点查（`resultSet.next()` 一次）语义一致。
- **线程安全**：双检锁 + `volatile loaded` 后写保证 map 发布安全；加载完成后 map 只读，查找无锁。

### 5.3 新增 `JoinCacheKeyBuilder`（key 构造 + 类型归一化 —— 正确性核心）

新文件：`.../impl/joiner/JoinCacheKeyBuilder.java`（package-private）

**为什么必须归一化**：DataX record 列值与 JDBC `ResultSet.getObject()` 对同一逻辑值可能返回不同 Java 类型（MySQL `INT` → `Integer`，而 DataX 侧 → `Long`；`DECIMAL` → `BigDecimal` vs `Long`）。若直接以原对象做 HashMap key，`Integer(5).equals(Long(5)) == false`，会**静默全部 miss**。因此主表侧与维度侧的 key 必须经同一套 normalizer 归一化。

构造时按 `matchCondition` 中每列的 `dimensionMatchColType`（TypeVisitor）预建 normalizer：

| JDBC 类型族 | 归一化结果 |
|---|---|
| tinyint / smallint / int / bigint / bit / bool | `((Number) v).longValue()` |
| float / double / decimal / numeric | `((Number) v).doubleValue()` |
| varchar 族 | `String.valueOf(v)` |
| date / time / timestamp / blob 等 | 原样返回（与 `TableJoinMatchCondition.PreparedStatementSetter` 已不支持的类别一致，不做额外承诺） |

```java
class JoinCacheKeyBuilder {
    private final List<Function<Object, Object>> matchNormalizers;
    private final List<TableJoinFilterCondition> dimFilters;   // 构造时预过滤 Dimension 类型

    JoinCacheKeyBuilder(List<TableJoinMatchCondition> matchCondition,
                        List<TableJoinFilterCondition> filterConditions) { ... }

    /** 主表侧：值先经 normalizer 再入 key */
    JoinCacheKey buildFromRecord(ColumnAwareRecord record,
                                 List<TableJoinMatchCondition> matchCondition) {
        JoinCacheKey key = new JoinCacheKey();
        for (int i = 0; i < matchCondition.size(); i++) {
            TableJoinMatchCondition mc = matchCondition.get(i);
            Object raw = record.getColumn(mc.getPrimaryTableMatchColName());
            key.addParam(mc.getDimensionMatchColName())
               .addPrimaryVal(matchNormalizers.get(i).apply(raw));
        }
        for (TableJoinFilterCondition fc : dimFilters) {
            key.addParam(fc.getColumnName()).addParam(fc.getValue());
        }
        return key;
    }

    /** 维度侧：按 SELECT 列序读 match 列（1..matchCount），同 normalizer */
    JoinCacheKey buildFromResultSet(ResultSet rs,
                                    List<TableJoinMatchCondition> matchCondition) throws SQLException {
        JoinCacheKey key = new JoinCacheKey();
        for (int i = 0; i < matchCondition.size(); i++) {
            TableJoinMatchCondition mc = matchCondition.get(i);
            Object raw = rs.getObject(i + 1);
            key.addParam(mc.getDimensionMatchColName())
               .addPrimaryVal(matchNormalizers.get(i).apply(raw));
        }
        for (TableJoinFilterCondition fc : dimFilters) {
            key.addParam(fc.getColumnName()).addParam(fc.getValue());
        }
        return key;
    }
}
```

> 对 LRU 模式而言 key 只来自主表侧，归一化是内部一致的，无行为变化。

### 5.4 `JoinerUDF` 改造

文件：`.../impl/JoinerUDF.java`

#### (a) 新增 transient 字段 + 懒初始化

```java
private transient JoinerSelectDataSource _joinerSelectDataSource;
private transient JoinerSelectTable _joinerSelectTable;
private transient JoinerSetMatchConditionAndCols _joinerSetMatchConditionAndCols;
private transient DataSourceFactory _dataSourceFactory;
private transient String _prefix;
private transient JoinCacheKeyBuilder _keyBuilder;
private transient StringBuffer _preloadSQL;
private transient PreparedStatement _selectPreparedStatement;

private void ensureInit() {           // 仅通道线程调用，无需锁
    if (_keyBuilder != null) return;
    _joinerSelectDataSource = getJoinerSelectDataSource();
    _joinerSelectTable = getJoinerSelectTable();
    _joinerSetMatchConditionAndCols = getJoinerSetMatchConditionAndCols();
    _dataSourceFactory = _joinerSelectDataSource.getDataSourceFactory();
    _prefix = getOutputColPrefix(_joinerSetMatchConditionAndCols);
    _keyBuilder = new JoinCacheKeyBuilder(
            _joinerSetMatchConditionAndCols.matchCondition,
            _joinerSetMatchConditionAndCols.filterConditions);
}
```

#### (b) `evaluate()` 重构

```java
@Override
public void evaluate(ColumnAwareRecord record) {
    ensureInit();
    JoinCacheKey cacheKey = null;
    try {
        cacheKey = _keyBuilder.buildFromRecord(record);
        TargetRowsCache cache = _joinerSelectTable.cache;
        TargetRowsCache.JoinCacheValue exist;

        if (cache.isFullPreload()) {
            if (cacheKey.hasNullPrimaryVal()) return;        // NULL 不参与 join，语义等价且无错误计数
            ((TargetRowsCacheFull) cache).preload(this::preloadAll);   // 幂等
            exist = cache.getFromCache(cacheKey);
            if (exist == null) return;
        } else if (cache.isOn()) {
            exist = cache.getFromCache(cacheKey);
            if (exist == null) {
                exist = selectFromDB(_dataSourceFactory, _joinerSetMatchConditionAndCols.matchCondition,
                        cacheKey, _joinerSelectTable.tagetTable, _joinerSetMatchConditionAndCols);
                cache.set2Cache(cacheKey, exist);
            }
        } else {
            exist = selectFromDB(_dataSourceFactory, _joinerSetMatchConditionAndCols.matchCondition,
                    cacheKey, _joinerSelectTable.tagetTable, _joinerSetMatchConditionAndCols);
        }

        if (exist.isNull()) return;
        for (CMeta tc : _joinerSetMatchConditionAndCols.targetCols) {
            Object colVal = exist.get(tc.getName());
            if (colVal != null) {
                record.setColumn(_prefix + tc.getName(), colVal);
            }
        }
    } catch (Throwable e) {
        if (_joinerSetMatchConditionAndCols.skipError) {
            if (((errorCount++) % 100) == 0) {
                logger.warn(String.valueOf(cacheKey), e);
            }
        } else {
            throw new RuntimeException(e);
        }
    }
}
```

#### (c) 全量加载 `preloadAll`

```java
private void preloadAll(TargetRowsCache.RowSink sink) throws Exception {
    JDBCConnection connection = getJdbcConnection(_dataSourceFactory);
    StringBuffer sql = getPreloadSQL(/* ... */);
    List<TableJoinMatchCondition> matchCondition = _joinerSetMatchConditionAndCols.matchCondition;
    try (Statement stmt = connection.createStatement()) {
        stmt.setFetchSize(1000);          // 通用游标设置，避免 MySQL 默认整包缓冲造成双份内存
        try (ResultSet rs = stmt.executeQuery(sql.toString())) {
            int matchCount = matchCondition.size();
            while (rs.next()) {
                JoinCacheKey key = _keyBuilder.buildFromResultSet(rs, matchCondition);
                TargetRowsCache.JoinCacheValue val = new TargetRowsCache.JoinCacheValue();
                val.setNull(false);
                int colIdx = matchCount + 1;
                for (CMeta tc : _joinerSetMatchConditionAndCols.targetCols) {
                    Object v = rs.getObject(colIdx++);
                    if (v != null) val.put(tc.getName(), v);
                }
                sink.accept(key, val);
            }
        }
    }
}
```

#### (d) `getPreloadSQL()` 与 `getSelectSQL()` 重构

抽出共用 helper（两处复用）：

```java
private void appendDimFilterConditions(StringBuffer sql, DataSourceFactory dataSource,
                                       List<TableJoinFilterCondition> filterConditions,
                                       boolean hasWhere /* 是否已有 WHERE 子句 */);
```

`getPreloadSQL()` 要点：

- SELECT 列表 = **dimensionMatchColName 在前**（保证 `buildFromResultSet` 按序号取值）+ targetCols
- **无** `matchCol = ?` 子句
- 保留全部**维度侧**过滤条件（与点查 SQL 语义一致）

```sql
-- 示例
SELECT `user_id`,`tenant_id`,`user_name`,`level`
FROM `dim_user`
WHERE `is_valid`=1 AND `region`='east'
```

#### (e) `selectFromDB()` 复用 PreparedStatement

```java
PreparedStatement ps = this._selectPreparedStatement;
if (ps == null) {
    ps = getJdbcConnection(dataSource).preparedStatement(getSelectSQL(...).toString());
    this._selectPreparedStatement = ps;
}
for (int index = 0; index < matchCondition.size(); index++) {
    matchCondition.get(index).preparedStatementSetter()
        .setVal(ps, index, Objects.requireNonNull(cacheKey.getPrimaryVal(index), "..."));
}
try (ResultSet resultSet = ps.executeQuery()) { /* 原逻辑 */ }
```

> 每个 `?` 每次执行前都会重设，无需 `clearParameters()`。statement 生命周期 = 通道线程，与连接一致。

#### (f) `afterSaved()` 扩展

```java
@Override
public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
    this._selectSQL = null;
    this._jdbcConnection = null;
    this._joinerSelectDataSource = null;
    this._joinerSelectTable = null;
    this._joinerSetMatchConditionAndCols = null;
    this._dataSourceFactory = null;
    this._prefix = null;
    this._keyBuilder = null;
    this._preloadSQL = null;
    this.errorCount = 0;
    if (this._selectPreparedStatement != null) {
        try { this._selectPreparedStatement.close(); } catch (SQLException ignore) { }
        this._selectPreparedStatement = null;
    }
}
```

### 5.5 执行时序（full preload 模式）

```
通道线程                     JoinerUDF                    TargetRowsCacheFull          维度库
   │    record1 ──evaluate──▶ │                              │                          │
   │                          │ preload(bulkLoader) ────────▶│                          │
   │                          │        preloadAll()          │                          │
   │                          │ ──────── bulk SELECT ──────────────────────────────────▶ │
   │                          │ ◀──────── ResultSet (流式) ───────────────────────────── │
   │                          │ ── sink.accept(k,v) ────────▶│ 装入 HashMap(putIfAbsent) │
   │                          │                              │                          │
   │                          │ getFromCache(key) ──────────▶│ HashMap.get → 纯内存      │
   │    record2..N            │ （不再访问 DB）              │                          │
```

---

## 6. 行为差异与兼容性

| 项 | 现有 On/Off 路径 | 新增 Full 路径 |
|---|---|---|
| 维度数据新鲜度 | LRU TTL ≤ 600s，最长 10 分钟延迟 | 任务启动时快照，任务中途维度变更不可见 |
| 主表 null join key | `Objects.requireNonNull` 抛 NPE → skipError 计数/失败 | 静默跳过（NULL 在 SQL join 中本就不匹配，语义更合理） |
| 重复维度 key | 首行赢 | 首行赢（putIfAbsent，一致） |
| 维度表行数 | 无限制（LRU 逐出） | 超 `maxRows` fail-fast，提示改用 LRU |
| 内存 | Caffeine ≤ 10万条 | 全量 map，受 `maxRows` 护栏约束 |

> 数据新鲜度敏感、或维度表超大的场景，文档中引导用户继续选择 LRU 模式。**On/Off 行为零变化。**

---

## 7. 内存与并发估算

- 每通道各持有一份全量 map：**总内存 ≈ 通道数 × 维度表行数 × 单行目标列大小**。
- 经验值：20 万行 × 5 列短文本 ≈ 每通道 100~200MB（需按实际列宽估算）。
- `maxRows` 设置需结合：通道数 × 单通道 map 大小 < DataX 进程可用堆。
- 多通道也是并行度的来源：每通道独立连接 + 独立 map，并行扩容线性提升吞吐，同时线性增加维度库连接数。

---

## 8. 运维检查清单（随方案一并交付）

1. **维度表匹配列必须有索引**（影响 Off/LRU 模式点查耗时；测试环境重点排查）。
2. **DataX 通道数**：join 场景下吞吐 ≈ 通道数 × 单通道上限；Full 模式下通道数主要受内存约束。
3. **maxRows 与堆**：`maxRows × 单行大小 × 通道数 < 堆`，留足余量。
4. 若源表与维度表**同库**，优先考虑在 Reader 侧用 SQL join 下推（不走 transformer），性能最优。

---

## 9. 测试方案

### 9.1 单元测试（JUnit4，仿 `TargetRowsCacheOnTest` 风格，无需真实 DB）

| 测试类 | 用例 |
|---|---|
| `TargetRowsCacheFullTest` | preload 3 行验证命中/miss/重复 key 首行赢；超 `maxRows` 抛 `IllegalStateException`；`afterSaved` 重置后 `getFromCache` 抛未加载异常；`getUDFDesc` 含 maxRows |
| `JoinCacheKeyBuilderTest` | **核心用例**：`Integer(5)` / `Long(5)` / `Short(5)` 生成相等 key；字符串归一化；维度过滤常量按序入 key；`hasNullPrimaryVal` 为 true |
| （可选）`JoinerUDFTest` | mock JDBC 验证：preload SQL 中 match 列在最前且无 `matchCol=?`；维度过滤条件出现在 preload SQL 中；点查 PreparedStatement 只创建一次、执行多次 |

### 9.2 编译与测试命令

```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-transformer
mvn compile -o -Dmaven.test.skip=true
mvn test -o -Dtest='TargetRowsCacheFullTest,JoinCacheKeyBuilderTest'
```

### 9.3 测试环境实测对比

同一 DataX 任务、同一数据源分别跑三遍（cache = Off / On(LRU) / Full），记录：

- DataX 日志中的通道吞吐（rec/s）与任务总耗时
- JVM 堆使用（Full 模式）
- 维度库查询次数（general log / 慢日志）

**预期**：维度表行数 ≤ maxRows 时，Full 模式吞吐接近无 transformer 基线；Off 模式维持在 `1/点查耗时` × 通道数水平。

---

## 10. 备选方案与权衡（暂不实施，留作后续演进）

1. **跨通道共享缓存**：以 `(dataSource, table, filters)` 为 key 的 static LoadingCache，多通道共享一份全量 map，消除 §7 的内存放大。涉及跨任务生命周期与失效设计，复杂度高，待内存成为实际问题再做。
2. **微批 `IN(...)` 回源**（LRU 模式增强）：对 miss 的 key 积累成小批量一次 `IN` 查询。可在不破坏流式契约的前提下以"预取窗口"形式实现，适合大维度表场景，作为 LRU 模式的后续增强。

---

## 11. 实施步骤

1. `TargetRowsCache`：加 `isFullPreload()` / `preload()` / `BulkLoader` / `RowSink` 钩子 + `JoinCacheKey.hasNullPrimaryVal()`
2. 新建 `JoinCacheKeyBuilder`（含归一化 TypeVisitor）
3. 新建 `TargetRowsCacheFull`（含 `FullDesc` 校验）
4. `JoinerUDF`：transient 字段 + `ensureInit()`、`evaluate()` 三分支重构、`preloadAll()`、`getPreloadSQL()` + `appendDimFilterConditions()` 抽取、`selectFromDB()` statement 复用、`afterSaved()` 扩展
5. 新增两个单测类并跑通
6. 测试环境三模式对比实测，回填吞吐数据到本文档