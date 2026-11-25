# 金仓 CDC 断点恢复失败修复：WalPositionLocator 的 LSN 记账缺陷

> **文档时间戳**: 2026-11-25 14:51:51
> **文档类型**: 详细实施方案（供执行模型直接照做）
> **前置结论**: 根因已在源码级别坐实，**不需要再重新做根因分析**。执行时请直接跳到"第三章 逐文件改动"。
> **失败现场**: `io.debezium.DebeziumException: Message with LSN 'LSN{20/ED94A338}' not present among LSNs seen in the location phase '[...]'` @ `WalPositionLocator.skipMessage`

---

## 执行须知（强制，违反即为执行错误）

1. **只改本文件"第四章"列出的文件**。不要顺手改 `PgProtoMessageDecoder`、`PostgresOffset`、`PostgresStreamingChangeEventSource`、TIS 插件工程的任何 Java 文件。这些在第九章里说明了为什么**不在本次范围**。
2. **三个仓库的绝对路径不要搞混**：

   | 角色 | 路径 | 本次是否修改 |
   |---|---|---|
   | Debezium 金仓 fork（**唯一改动目标**） | `/opt/misc/debezium/debezium-connector-kingbase` | ✅ 只改这里 |
   | Flink CDC 源码（只读参考） | `/opt/misc/flink-cdc` | ❌ 不改 |
   | TIS 进程侧插件工程（构建产物侧） | `/Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/...` | ❌ 不改源码，只重新打包 |
   | TIS 内核工程 | `/Users/mozhenghua/j2ee_solution/project/tis-solr` | ❌ 不改 |

3. **不要 `git commit` / `git push`**。改动停在 working tree，最后给出 `git -C /opt/misc/debezium diff --stat`。
4. 每个改动点的代码块都是**完整可粘贴**的，不要"优化"命名、不要调整缩进风格、不要新增 import（本次改动不需要任何新 import）。
5. 如果编译或测试失败，**停下来报告具体错误**，不要自行发明替代方案。

---

## 一、背景

TIS 5.1.0 的金仓 CDC 链路（KingbaseES V8R6，MySQL 兼容模式，解码插件 `decoderbufs`）在以下操作后失败：

1. 首次启动 → 增删改实时同步正常；
2. 停止 Flink，通过 TIS 从原任务的 Checkpoint/Savepoint 恢复 → 状态加载成功、作业提交成功；
3. 随后 Source 抛 `WalPositionLocator.skipMessage` 的 `DebeziumException`，作业失败。

同时日志里出现一次：

```
WARN io.debezium.connector.postgresql.connection.pgproto.PgProtoMessageDecoder
  - Received message with type 'UNKNOWN' that is unknown to this version of connector, consider upgrading
```

现场另一现象：有一次恢复后作业持续 RUNNING 且 Checkpoint 一直成功，但源位点长期停在 `20/ED94A208`；
后来写入新数据，日志出现"找到接续位置 `20/F53BA298`"，重读时遇到 `20/ED94A338` 后失败。

---

## 二、根因（已确认，执行模型无需再论证）

### 2.1 三个代码事实

**事实 A：`lsnSeen` 只在"被解码器真正派发"的消息上记账。**

`WalPositionLocator.resumeFromLsn()` 第一行是 `lsnSeen.add(currentLsn)`（当前 `WalPositionLocator.java:63`）。
它唯一的调用点是 `PostgresStreamingChangeEventSource.searchWalPosition()` 里的 processor 回调。
也就是说：**只有走到 processor 的消息，其 LSN 才会进 `lsnSeen`**。

**事实 B：decoderbufs 解码器把不认识的 op 消息静默丢弃，不调用 processor。**

`PgProtoMessageDecoder.processNotEmptyMessage()`（`/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/pgproto/PgProtoMessageDecoder.java`）：

```java
private static final Set<Op> SUPPORTED_OPS = Collect.unmodifiableSet(Op.INSERT, Op.UPDATE, Op.DELETE, Op.BEGIN, Op.COMMIT);
...
if (!SUPPORTED_OPS.contains(message.getOp())) {
    if (!warnedOnUnkownOp) {
        LOGGER.warn("Received message with type '{}' that is unknown to this version of connector, consider upgrading", message.getOp());
        warnedOnUnkownOp = true;
    }
    return;                    // ← 直接 return，不调 processor
}
processor.process(new PgProtoReplicationMessage(message, typeRegistry));
```

这就是那条 WARN 的唯一来源。`src/main/protobuf/pg_logicaldec.proto` 的 `enum Op` 只有 6 个值且
`UNKNOWN = -1` 是第一个（proto2 语义：线上出现枚举外取值时落到 unknown fields，`getOp()` 返回首值 → 打印成 `UNKNOWN`）。
**结论：日志里出现"type 'UNKNOWN'"，就必然存在一条消息，它的 LSN 在定位阶段被吞掉了。**

**事实 C：过滤判定发生在解码之前，对每一条裸消息都执行。**

`AbstractMessageDecoder.shouldMessageBeSkipped()` 被 `PostgresReplicationConnection.readPending()/read()` 调用，
位置在 `deserializeMessages()` **之前**，即比 protobuf 解析还早。它拿的是驱动上报的 LSN（`stream.getLastReceiveLSN()`），
不是从消息体里解析出来的 LSN。

### 2.2 合起来就是异常

恢复路径（`PostgresStreamingChangeEventSource.execute()`）是两段式：

```
定位阶段 searchWalPosition()   —— passMessages == true，skipMessage 直接放行，LSN 进 lsnSeen（仅限被派发的消息）
        ↓
walPosition.enableFiltering()  —— 从这里开始 skipMessage 真正开始过滤
        ↓
reconnect + startStreaming(walPosition.getLastEventStoredLsn(), walPosition)
        ↓
重读阶段 processMessages()     —— 从"最后存储的事件 LSN"重新拉，每一条裸消息都过 skipMessage
```

重读阶段会**再次投递**定位阶段见过的那条 UNKNOWN 消息。它在定位阶段被 `PgProtoMessageDecoder` 丢掉、没进 `lsnSeen`；
在重读阶段它既不等于 `startStreamingLsn`，也不在 `lsnSeen` 里 → 命中：

```java
if (lsn.isValid() && !lsnSeen.contains(lsn)) {
    throw new DebeziumException("Message with LSN '%s' not present among LSNs seen in the location phase ...");
}
```

现场数据完全对得上：存储 LSN `20/ED94A208` < 抛错 LSN `20/ED94A338` < 接续位置 `20/F53BA298`。

### 2.3 为什么首次启动不炸、为什么"位点冻结"

- **首次启动**：无存量 offset → `new WalPositionLocator()`（两个锚点都是 null）→ `searchingEnabled() == false`
  → 根本不做过滤，UNKNOWN 消息只是 WARN + 丢弃。所以只有恢复才炸。
- **位点冻结**：定位阶段只在"遇到 COMMIT 且 LSN > lastCommitStoredLsn"或"找到 lastEvent 之后那条"时才退出。
  恢复后若源库空闲，它会一直空转，一条记录都不派发；而 Flink 的 checkpoint 不依赖是否有记录流过，
  于是一直成功、把同一个 LSN 反复刷进 slot。这就是异常文案里 "infinite loop or a data loss" 描述的场景。

---

## 三、修复方案总览

**一句话**：让 `lsnSeen` 记录"定位阶段在裸消息层面见到过的 LSN 全集"，而不是"解码器认账的 LSN 子集"，
使其与重读阶段 `skipMessage` 的输入集合同构。

改动两处生产代码 + 新增一个单元测试：

| # | 文件 | 改动 |
|---|---|---|
| 1 | `/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/WalPositionLocator.java` | 新增 `recordSeenLsn(Lsn)` |
| 2 | `/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/AbstractMessageDecoder.java` | `shouldMessageBeSkipped()` 中先记账再判定 |
| 3 | `/opt/misc/debezium/debezium-connector-kingbase/src/test/java/io/debezium/connector/postgresql/connection/WalPositionLocatorTest.java` | **新建**，钉住行为 |

**保留抛错的保护逻辑不变**：如果某 LSN 在重读阶段出现、却在定位阶段根本没被投递（例如 slot 已越过该位置），仍然会抛。
本次改动只是把"被解码器丢弃"这一合法场景从误报里摘出去。

---

## 四、逐文件精确改动

### 4.1 `WalPositionLocator.java`

**路径**：`/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/WalPositionLocator.java`

**定位点**：`enableFiltering()` 方法与 `searchingEnabled()` 方法之间（当前文件第 159-168 行附近）。

**现状**：

```java
    /**
     * Enables filtering of message LSNs based on calculated position.
     */
    public void enableFiltering() {
        passMessages = false;
    }

    /**
     * @return true if searching of WAL position should be executed
     */
    public boolean searchingEnabled() {
        return lastEventStoredLsn != null;
    }
```

**改为**（在两者之间插入 `recordSeenLsn`，其余一字不动）：

```java
    /**
     * Enables filtering of message LSNs based on calculated position.
     */
    public void enableFiltering() {
        passMessages = false;
    }

    /**
     * Records an LSN observed at the wire level during the location phase.
     *
     * <p>
     * The {@code lsnSeen} set is normally populated from {@link #resumeFromLsn(Lsn, ReplicationMessage)}, i.e. only for
     * messages that the decoder actually dispatches to the replication processor. Some decoders (notably decoderbufs,
     * see {@code PgProtoMessageDecoder}) drop messages with an operation they do not model without invoking the
     * processor, so their LSN would never be recorded. The replay phase however re-delivers such messages and asks
     * {@link #skipMessage(Lsn)} about them, which would then fail with
     * "not present among LSNs seen in the location phase".
     *
     * <p>
     * Recording the LSN before the skip decision makes the two phases consistent, while the sanity check in
     * {@link #skipMessage(Lsn)} still catches LSNs that were never delivered at all (e.g. the slot already moved past
     * them).
     *
     * <p>
     * The recording window closes as soon as the resume position has been located ({@code startStreamingLsn != null}).
     * This is required because {@link #skipMessage(Lsn)} flips {@code passMessages} back to {@code true} once the
     * replay reaches the resume position; without the {@code startStreamingLsn == null} condition the set — which is
     * cleared at that very moment — would start growing again for the rest of the connector's lifetime.
     *
     * @param lsn the LSN reported by the replication driver for the raw message just received
     */
    public void recordSeenLsn(Lsn lsn) {
        if (passMessages && searchingEnabled() && startStreamingLsn == null && lsn != null && lsn.isValid()) {
            lsnSeen.add(lsn);
        }
    }

    /**
     * @return true if searching of WAL position should be executed
     */
    public boolean searchingEnabled() {
        return lastEventStoredLsn != null;
    }
```

**实现要点（不要改动这四个条件）**：

- `passMessages && startStreamingLsn == null` —— 两个条件合起来把记账窗口严格限定在
  「定位阶段且尚未找到接续位点」。只用 `passMessages` 不够：`skipMessage()` 在重读命中 `startStreamingLsn`
  时会把 `passMessages` 翻回 `true` 并清空集合（`WalPositionLocator.java:140-144`），若没有
  `startStreamingLsn == null` 条件，恢复后进入正常消费的每一条消息都会重新进集合，`lsnSeen`
  会在作业剩余生命周期内无界增长（内存泄漏）。找到位点之后、`enableFiltering()` 之前这段尾巴上到达的消息
  不记账是安全的：它们的 LSN 都大于 `startStreamingLsn`，重读阶段按 LSN 序必然先命中
  `startStreamingLsn` 关闭过滤，轮不到它们参与集合校验。
- `searchingEnabled()` —— 首次启动用的是 `new WalPositionLocator()`，两个锚点为 null，此时不做定位，
  记账毫无意义且会无界增长，必须排除。
- `lsn != null && lsn.isValid()` —— 避免把 null 或 `INVALID_LSN(0/0)` 塞进集合，否则后续
  `lsnSeen.contains(null)` 会出现误匹配。

### 4.2 `AbstractMessageDecoder.java`

**路径**：`/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/AbstractMessageDecoder.java`

**现状**（第 40-49 行，原文照抄）：

```java
    @Override
    public boolean shouldMessageBeSkipped(ByteBuffer buffer, Lsn lastReceivedLsn, Lsn startLsn, WalPositionLocator walPosition) {
        // the lsn we started from is inclusive, so we need to avoid sending back the same message twice
        // but for the first record seen ever it is possible we received the same LSN as the one obtained from replication slot
        if (walPosition.skipMessage(lastReceivedLsn)) {
            LOGGER.info("Streaming requested from LSN {}, received LSN {} identified as already processed", startLsn, lastReceivedLsn);
            return true;
        }
        return false;
    }
```

**改为**（只加两行：一行注释、一行调用）：

```java
    @Override
    public boolean shouldMessageBeSkipped(ByteBuffer buffer, Lsn lastReceivedLsn, Lsn startLsn, WalPositionLocator walPosition) {
        // Record the LSN before the skip decision: this method runs for every raw message, including the ones the
        // decoder will later drop (e.g. decoderbufs messages with an op it does not support). The replay phase
        // re-delivers those messages, so they must be part of the location-phase LSN set.
        walPosition.recordSeenLsn(lastReceivedLsn);
        // the lsn we started from is inclusive, so we need to avoid sending back the same message twice
        // but for the first record seen ever it is possible we received the same LSN as the one obtained from replication slot
        if (walPosition.skipMessage(lastReceivedLsn)) {
            LOGGER.info("Streaming requested from LSN {}, received LSN {} identified as already processed", startLsn, lastReceivedLsn);
            return true;
        }
        return false;
    }
```

**注意**：

- 调用必须在 `skipMessage` **之前**（`skipMessage` 在命中 `startStreamingLsn` 时会清空 `lsnSeen`，顺序不能反）。
- `PgOutputMessageDecoder.shouldMessageBeSkipped()` 会先调用 `super.shouldMessageBeSkipped(...)`，
  因此 pgoutput 路径自动受益，**不需要单独修改** `PgOutputMessageDecoder`。
- 本次改动**不需要新增任何 import**。`WalPositionLocator` 与 `Lsn` 都已在同包内。

---

## 五、单元测试（新建文件）

**路径**：`/opt/misc/debezium/debezium-connector-kingbase/src/test/java/io/debezium/connector/postgresql/connection/WalPositionLocatorTest.java`

**目录现状**：该目录已存在（内有 `PostgresConnectionIT.java` 等），只需新增文件。
模块使用 **JUnit 4**（`pom.xml` 中 `junit:junit`，`scope=test`），测试类名以 `Test` 结尾才会被 surefire 拾取。

**完整文件内容**（可直接粘贴）：

```java
/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.connector.postgresql.connection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;

import org.junit.Test;

import io.debezium.DebeziumException;

/**
 * Verifies that the LSN bookkeeping of the location phase and the filtering of the replay phase stay consistent
 * when a message is dropped by the decoder (e.g. a decoderbufs message with an unsupported operation).
 */
public class WalPositionLocatorTest {

    private static final Lsn LSN_200 = Lsn.valueOf(200L);
    private static final Lsn LSN_300 = Lsn.valueOf(300L);

    private static ReplicationMessage noopMessage() {
        return new ReplicationMessage.NoopMessage(1L, Instant.EPOCH);
    }

    /**
     * Mirrors a restore of a TIS checkpoint: {@code lastCommitStoredLsn} is null (the Flink CDC offset never
     * persists {@code lsn_proc}), {@code lastEventStoredLsn} is the stored position. Neither the resume position
     * has been located nor filtering enabled yet — callers drive those steps themselves so that the test can
     * interleave {@link WalPositionLocator#recordSeenLsn(Lsn)} at the exact point where the wire-level hook
     * would see messages.
     */
    private static WalPositionLocator locatorAfterRestore() {
        return new WalPositionLocator(null, LSN_200);
    }

    @Test
    public void shouldNotThrowForLsnRecordedAtWireLevel() {
        final WalPositionLocator locator = locatorAfterRestore();

        // an UNKNOWN message is dropped by the decoder: the wire-level hook records its LSN, but resumeFromLsn()
        // never sees it (startStreamingLsn is still null here, i.e. the recording window is still open)
        locator.recordSeenLsn(LSN_300);

        // the next message is dispatched and locates the resume position
        assertTrue(locator.resumeFromLsn(LSN_200, noopMessage()).isPresent());
        locator.enableFiltering();

        // the replay phase re-delivers the dropped message: it must be filtered, not rejected
        assertTrue("an LSN seen on the wire during the location phase must be filtered, not rejected",
                locator.skipMessage(LSN_300));
    }

    @Test
    public void shouldStillThrowForLsnNeverDelivered() {
        final WalPositionLocator locator = locatorAfterRestore();
        assertTrue(locator.resumeFromLsn(LSN_200, noopMessage()).isPresent());
        locator.enableFiltering();
        try {
            locator.skipMessage(LSN_300);
            fail("an LSN that never appeared in the location phase must still be rejected");
        }
        catch (DebeziumException e) {
            assertTrue(e.getMessage().contains("not present among LSNs seen in the location phase"));
        }
    }

    @Test
    public void shouldPassMessagesAtResumeLsn() {
        final WalPositionLocator locator = locatorAfterRestore();
        assertTrue(locator.resumeFromLsn(LSN_200, noopMessage()).isPresent());
        locator.enableFiltering();
        assertFalse("the message at startStreamingLsn must switch the filtering off", locator.skipMessage(LSN_200));
        // filtering is now off, everything passes
        assertFalse(locator.skipMessage(LSN_300));
    }

    @Test
    public void shouldNotRecordAfterReplaySwitchedFilteringOff() {
        final WalPositionLocator locator = locatorAfterRestore();
        assertTrue(locator.resumeFromLsn(LSN_200, noopMessage()).isPresent());
        locator.enableFiltering();
        // replay reaches the resume position: filtering switches off, passMessages flips back to true, lsnSeen cleared
        assertFalse(locator.skipMessage(LSN_200));

        // normal streaming after a restore: recording must be a no-op (startStreamingLsn != null),
        // otherwise lsnSeen would grow unboundedly for the rest of the connector's lifetime
        locator.recordSeenLsn(LSN_300);

        // prove LSN_300 was NOT recorded: with filtering on again it must be treated as never-seen
        locator.enableFiltering();
        try {
            locator.skipMessage(LSN_300);
            fail("LSNs observed after the replay phase must not enter lsnSeen");
        }
        catch (DebeziumException e) {
            // expected
        }
    }

    @Test
    public void shouldIgnoreRecordedLsnWhenNotSearching() {
        // fresh start: no stored position, the locator never filters and must not accumulate LSNs
        final WalPositionLocator locator = new WalPositionLocator();
        locator.recordSeenLsn(LSN_300);
        // passMessages is still true, so nothing is filtered
        assertFalse(locator.skipMessage(LSN_300));
    }
}
```

**行为说明（供执行模型核对，不要"简化"掉任何一个用例）**：

- 用例 1 是本次缺陷的直接回归：改前必然抛异常，改后返回 `true`（过滤）。**注意时序：`recordSeenLsn`
  必须发生在 `resumeFromLsn` 之前**——记账窗口在 `startStreamingLsn` 被赋值后即关闭，真实链路里
  UNKNOWN 消息也是先于「定位到接续位点的那条消息」到达的。
- 用例 2 是保护逻辑的回归：确保没有把 `throw` 整个降级成 warn。
- 用例 3 验证 `startStreamingLsn` 命中时关闭过滤的既有行为未被破坏。
- 用例 4 钉住「记账窗口随位点找到而关闭」：重读命中 `startStreamingLsn` 后 `passMessages` 翻回 `true`，
  此时 `recordSeenLsn` 必须是空操作，否则恢复后正常消费期间 `lsnSeen` 会无界增长（内存泄漏）。
- 用例 5 验证首次启动场景（`searchingEnabled()==false`）下 `recordSeenLsn` 是空操作。
- `ReplicationMessage.NoopMessage` 是现成的实现类（`ReplicationMessage.java:200-251`），
  `getOperation()` 返回 `NOOP`，`isLastEventForLsn()` 返回 `true`，可直接用于驱动 `resumeFromLsn` 走
  `lastCommitStoredLsn == null` 分支。

**运行（surefire 旁路，原因见 6.2）**：

```bash
cd /opt/misc/debezium
mvn -o dependency:build-classpath -pl debezium-connector-kingbase \
    -Dmdep.outputFile=/tmp/wpl-test/cp.txt -Dmdep.includeScope=test -Dquick
mkdir -p /tmp/wpl-test/classes && rm -rf /tmp/wpl-test/classes/*
JAR=~/j2ee_solution/mvn_repository/io/debezium/debezium-connector-kingbase/1.9.8.Final/debezium-connector-kingbase-1.9.8.Final.jar
CP="$JAR:$(cat /tmp/wpl-test/cp.txt)"
javac -cp "$CP" -d /tmp/wpl-test/classes \
  debezium-connector-kingbase/src/test/java/io/debezium/connector/postgresql/connection/WalPositionLocatorTest.java
java -cp "/tmp/wpl-test/classes:$CP" org.junit.runner.JUnitCore \
  io.debezium.connector.postgresql.connection.WalPositionLocatorTest
```

预期：`OK (5 tests)`。

> 若离线解析失败（`-o` 报 artifact 缺失），去掉 `-o` 重试一次；仍失败则报告错误，不要改测试。

---

## 六、构建与产物替换

### 6.1 确认本地仓库路径

本地仓库由 Maven 全局 settings 指定（`~/.m2/settings.xml` 不存在，取
`/Users/mozhenghua/j2ee_solution/apache-maven-3.9.9/conf/settings.xml` 第 6 行）：
`/Users/mozhenghua/j2ee_solution/mvn_repository`。

### 6.2 安装打补丁的 Debezium（实测可用命令）

**必须加 `-Dquick`**：`impsort-maven-plugin:1.6.2` 与 Maven 3.9.9 不兼容（核心不再带 plexus-utils），
该插件挂在 debezium-parent 的 `qa` profile（激活条件 `!quick`）里，`-Dquick` 一并关闭
formatter/impsort/checkstyle。**不要**用首次构建时不带 `-Dquick` 的命令——formatter 会顺手重排
仓库里 5 个无关源文件，制造格式化噪声。

```bash
cd /opt/misc/debezium
mvn -o install -pl debezium-connector-kingbase -Dmaven.test.skip=true -Dquick
```

注意用 `-Dmaven.test.skip=true` 而不是 `-DskipTests`：本模块 test 源码**先天编译不过**
（`DecoderDifferences.java` 引用了 fork 已删除的 `LogicalDecoder.WAL2JSON` 等枚举），
`-DskipTests` 只跳过执行、不跳过编译，照样炸。

**必须验证安装产物已更新**：

```bash
javap -classpath ~/j2ee_solution/mvn_repository/io/debezium/debezium-connector-kingbase/1.9.8.Final/debezium-connector-kingbase-1.9.8.Final.jar \
  io.debezium.connector.postgresql.connection.WalPositionLocator | grep recordSeenLsn
```

必须输出一行 `public void recordSeenLsn(io.debezium.connector.postgresql.connection.Lsn);`。
**没有这一行就不许继续**。

### 6.3 重打 TIS 金仓 shade jar 与插件（实测可用命令）

```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins
# ① 先 install shade 模块（插件模块经 classifier=shaded 从本地仓库取它，reactor 顺序无效）
mvn clean install -o -Dmaven.test.skip=true -Dappname=all \
  -Daether.enhancedLocalRepository.trackingFilename=_remote.repositories.disabled \
  -pl tis-incr/tis-flink-cdc-kingbase-shade-4-debezium-connector-postgresql
# ② 再 package 插件模块
mvn clean package -o -Dmaven.test.skip=true -Dappname=all \
  -Daether.enhancedLocalRepository.trackingFilename=_remote.repositories.disabled \
  -pl tis-incr/tis-flink-cdc-kingbase-plugin
```

三个参数缺一不可的原因：

- `-Dappname=all` —— 激活 tis-incr 的 `release` profile，shade 模块才进 reactor（否则报
  "Could not find the selected project in the reactor"）。
- `-Daether.enhancedLocalRepository.trackingFilename=...` —— 本地仓库里 `org/apache/flink/*:tis-1.20.1`
  等产物的 `_remote.repositories` 记录的来源仓库 ID（tis-releases/rdc-releases/tis-repo-releases）
  在当前 settings 里已不存在，离线模式会拒认（"has not been downloaded from it before"）。
  改跟踪文件名让全部缓存按"本地安装"对待，**不碰仓库文件**。
- shade 模块要 `install` 而不是 `package` —— 插件模块以
  `<classifier>shaded</classifier>` 依赖它；实测同 reactor 只 `package` 时插件仍取到本地仓库的旧 jar。

**验证 shade jar 里是新类**：

```bash
javap -classpath /Users/mozhenghua/j2ee_solution/project/plugins/tis-incr/tis-flink-cdc-kingbase-plugin/target/tis-flink-cdc-kingbase-plugin/WEB-INF/lib/tis-flink-cdc-kingbase-shade-4-debezium-connector-postgresql-5.1.0.jar \
  io.debezium.connector.postgresql.connection.WalPositionLocator | grep recordSeenLsn
# AbstractMessageDecoder 的调用顺序也要核（recordSeenLsn 必须在 skipMessage 之前）：
javap -c -classpath <同上 jar> io.debezium.connector.postgresql.connection.AbstractMessageDecoder | grep -E "recordSeenLsn|skipMessage"
```

**验证 tpi**：产物在 `tis-flink-cdc-kingbase-plugin/tpis/tis-flink-cdc-kingbase-plugin.tpi`
（**不在 target/**，`clean` 不会删它），`unzip -l` 确认其中 shade jar 的时间戳为本次构建。

---

## 七、端到端验证

### 7.1 环境准备

1. 把 6.3 产出的插件目录部署到 TIS 插件根目录（按现场既有的部署方式，**不要**自行发明新的部署路径）。
2. 重启 TIS / 让插件类加载器重新加载。

### 7.2 复现原缺陷的路径（改前会炸，改后必须不炸）

1. 建一条金仓→Kafka（或金仓→MySQL）链路，源表 `REPLICA IDENTITY FULL`，`dropSolt=false`。
2. 启动，写入若干数据，确认实时同步正常。
3. 触发一次会产生 `Op.UNKNOWN` 消息的操作（DDL 或 TRUNCATE 都可以 —— 见 7.4 的判定方法）。
4. 停止作业，做一次 savepoint。
5. **保持源库空闲**，从 savepoint 恢复。
   - 预期：作业 RUNNING，但源 offset 不推进（这是定位阶段空转，属于既有设计，不是本次要修的东西）。
6. 向源表写入一行新数据。
   - 预期：日志出现 `WAL resume position '...' discovered`，随后进入重读；
   - **判据：不再出现 `Message with LSN '...' not present among LSNs seen in the location phase`**；
   - 新写入的那一行正常到达下游。

### 7.3 回归验证

- 没有 UNKNOWN 消息的普通链路：恢复后照常同步，行为不变。
- 首次启动（无 savepoint）：照常同步，行为不变。
- 两条链路（Kafka / MySQL）各跑一遍。

### 7.4 现场确认 UNKNOWN 消息是什么（**只做取证，不在本次改代码**）

打开 TRACE：

```
io.debezium.connector.postgresql.connection.pgproto.PgProtoMessageDecoder=TRACE
```

重启后日志会打印 `Received protobuf message from the server RowMessage{...}`，从中能看到该消息的
`schema / table / op / transaction_id / new_tuple`。把结果回报即可，用于判断是否需要第二期修复
（第九章 9.2）。**无论看到什么，都不影响本方案的落地**。

---

## 八、回滚

本次改动是纯新增方法 + 一行调用，回滚只需：

```bash
cd /opt/misc/debezium && git checkout -- \
  debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/WalPositionLocator.java \
  debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/AbstractMessageDecoder.java
rm -f debezium-connector-kingbase/src/test/java/io/debezium/connector/postgresql/connection/WalPositionLocatorTest.java
```

再按第六章重新 install + 重打 shade jar 即可恢复原状。

---

## 九、明确不在本次范围的事项（执行时不要做）

### 9.1 `PgProtoMessageDecoder` 丢弃消息本身的行为不改

把 UNKNOWN 消息派发出去（例如构造 `NoopMessage`）会让 offset 前移，看起来更"正确"，但它会改变
`isLastEventForLsn`/`updateWalPosition` 的语义，风险高于本次修复，且与本缺陷的本质（LSN 记账）无关。

### 9.2 如果 UNKNOWN 消息其实是"帧格式解析错位"的产物

7.4 如果发现 `RowMessage` 的字段明显是乱码（schema/table 为空且 tuple 数量异常），说明金仓自己的
`decoderbufs` 输出格式与这份 proto 不一致，那属于**另一个缺陷**，需要单独立项，不在本方案内。
本方案的 LSN 记账修复对这种情况依然有效（属于止血），但不能替代它。

### 9.3 不补 `lsn_proc`

`org.apache.flink.cdc.connectors.postgres.source.offset.PostgresOffset` 只写 `lsn / txId / ts_usec`，
不写 `lsn_proc`，导致恢复时 `lastCompletelyProcessedLsn` 为 null、`WalPositionLocator` 的两个锚点相同，
WAL 回放区间被拉长。这是**放大因素**，不是根因；修改它属于 flink-cdc 上游类的改动，会显著扩大回归面。
单独立项。

### 9.4 不加"定位阶段超时告警"

"任务 RUNNING 但尚未定位到 WAL 接续点"的告警属于运维可观测性改造，与本缺陷无关。单独立项。

---

## 十、验收清单

执行完成后逐条打勾并在最终报告里贴出结果：

- [x] `WalPositionLocator.recordSeenLsn(Lsn)` 已新增，代码与 4.1 一致（含 `startStreamingLsn == null` 条件）
- [x] `AbstractMessageDecoder.shouldMessageBeSkipped()` 已在 `skipMessage` 之前调用 `recordSeenLsn`，代码与 4.2 一致（字节码已核：invokevirtual recordSeenLsn 在 skipMessage 之前）
- [x] `WalPositionLocatorTest` 五个用例全部通过（JUnitCore 旁路，`OK (5 tests)`）
- [x] `mvn install` 后 `javap` 能在 `debezium-connector-kingbase-1.9.8.Final.jar` 中看到 `recordSeenLsn`
- [x] 重打后 `javap` 能在 `WEB-INF/lib` 的 shade jar 中看到 `recordSeenLsn`（7.4MB，2026-11-25 15:53）
- [x] tpi（`tpis/tis-flink-cdc-kingbase-plugin.tpi`）内含新 shade jar（15:53）
- [ ] 端到端按 7.2 复现路径跑通，不再抛 `not present among LSNs seen in the location phase`（**待现场执行**）
- [ ] 7.3 回归通过（**待现场执行**）
- [x] 输出 `git -C /opt/misc/debezium diff --stat` 与 `WalPositionLocatorTest` 的测试输出
- [x] 目标文件改动受控：2 个生产文件（4.1/4.2）+ 1 个新测试文件；另见十一.3 格式化噪声待处理项

---

## 十一、执行实录（2026-11-25 实际执行记录）

### 11.1 实际改动

| 文件 | 改动 |
|---|---|
| `/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/WalPositionLocator.java` | +30 行（`recordSeenLsn`，含 javadoc） |
| `/opt/misc/debezium/debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/AbstractMessageDecoder.java` | +4 行（一行注释 + 一行 `recordSeenLsn` 调用） |
| `/opt/misc/debezium/debezium-connector-kingbase/src/test/java/io/debezium/connector/postgresql/connection/WalPositionLocatorTest.java` | 新建（5 用例，JUnit 4） |

产物链（全部 2026-11-25 15:47–15:53 构建）：

- 本地仓库补丁版 `io/debezium:debezium-connector-kingbase:1.9.8.Final`（原 jar 备份：`/tmp/debezium-connector-kingbase-1.9.8.Final.jar.orig-pre-lsnfix`，SHA-256 `238f08b9…`）
- shade jar：`tis-incr/tis-flink-cdc-kingbase-shade-4-debezium-connector-postgresql/target/…-5.1.0-shaded.jar`
- tpi：`tis-incr/tis-flink-cdc-kingbase-plugin/tpis/tis-flink-cdc-kingbase-plugin.tpi`（15.5MB）

### 11.2 构建期踩坑（已在第六章命令中规避）

1. `impsort-maven-plugin:1.6.2` 与 Maven 3.9.9 不兼容 → 必须 `-Dquick`；且首次不带 `-Dquick` 的构建
   会触发 formatter 重排无关文件（见 11.3）。
2. 模块 test 源码先天编译不过（`DecoderDifferences.java` 引用 fork 已删除的 `WAL2JSON*` 枚举）→
   单测走 `dependency:build-classpath` + `javac` + `JUnitCore` 旁路；`DebeziumException` 在
   `debezium-api` 不在 `debezium-core`；金仓驱动在 `cn/com/kingbase:kingbase8:9.0.1`
   （`com/kingbase8:kingbase8` 目录下只有失败下载残片）。
3. tis-incr 的 shade 模块在 `release` profile（`-Dappname=all`）里；tis-1.20.1 系列 Flink 产物的
   `_remote.repositories` 记录的私有仓库 ID 已从 settings 消失 → 离线构建需
   `-Daether.enhancedLocalRepository.trackingFilename=...` 覆盖（不碰仓库文件）。
4. 插件模块经 `<classifier>shaded</classifier>` 从本地仓库取 shade jar，同 reactor 只 `package`
   取到的仍是旧 jar → shade 模块必须先 `install`。

### 11.3 待处理项

1. **格式化噪声**：首次构建（不带 `-Dquick`）时 `formatter-maven-plugin` 重排了 5 个无关源文件
   （`KingBaseValueConverter` / `PostgresConnectorConfig` / `TypeRegistry` / `PostgresReplicationConnection`
   / `PgProtoReplicationMessage`），纯格式化无语义变化。执行机当时被权限拦住未自动回退，
   需用户确认后执行：
   ```bash
   cd /opt/misc/debezium && git checkout -- \
     debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/KingBaseValueConverter.java \
     debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/PostgresConnectorConfig.java \
     debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/TypeRegistry.java \
     debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/PostgresReplicationConnection.java \
     debezium-connector-kingbase/src/main/java/io/debezium/connector/postgresql/connection/pgproto/PgProtoReplicationMessage.java
   ```
   注意：**回退后必须重新 `mvn -o install … -Dquick` + 重打 shade/tpi**（当前产物是带格式化版本编译的，
   字节码语义相同，回退不重打亦可，只是源码与产物不完全同源）。
2. **依赖足迹**：本地全量重建的 shade jar（7.4MB，含 flink-cdc-base/HikariCP/byte-buddy/mockito/protobuf
   等传递依赖）与官方 5.1.0 部署基线（2.5MB）足迹不一致。已确认 tpi 的 WEB-INF/lib 本就独立携带
   byte-buddy/mockito/objenesis；flink-cdc-base 为原版 3.4.0，若 TIS 定制版 flink-cdc-base 有私有补丁，
   plugin-first 下会被遮蔽——**测试环境验证时留意 CDC 通用行为（分片/增量快照）是否异常**。
   用户已拍板采用本地重建产物。
3. 部署到测试环境时注意：同版本 TPI 是否被 TIS 识别为更新（同事的做法是改 Manifest
   Last-Modify-Time），以现场实际为准；以 `javap` 验证 TaskManager 实际加载的类为准，不以任务提交成功为准。