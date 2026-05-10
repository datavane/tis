# 05 - 基于 Falcon 数据集的测试方案

> **前置依赖**：02（数据通路）、03（GraphRAG）、04（NL→SQL）。
> Falcon 已克隆到本机：`/opt/misc/Falcon`

## 1. 为什么选 Falcon

| 优点 | 适配 ChatBI |
|------|-------------|
| **企业级中文** Text-to-SQL benchmark（涵盖金融/互联网/零售） | 业务上贴近实际目标用户 |
| 77% 多表 JOIN，含 CTE/窗口/Rank/Type Cast 等高复杂样本 | 能压测 GraphRAG 的多跳能力 |
| 提供 dev（含真值）+ test（盲测）+ 评估脚本 | 可建立基线并跑 CI |
| 题面与执行结果都给了，便于 EX (Execution Accuracy) 评测 | 与 04 文档的 trace 直接对接 |

> **注意**：Falcon 原生面向 SQLite 与 MaxCompute/Hive 方言。本方案要把数据迁到 Doris，并把方言适配作为测试不可或缺的一环。

## 2. Falcon 仓库内容速览

```
/opt/misc/Falcon
├── dev_data/               # 评测开发集（带真值）
│   ├── dev.json            # question_id, db_id, question, SQL, answer
│   ├── tables.json         # schema (含 sample_values)
│   └── dev_databases/      # 17 个 SQLite/CSV 库（按 db_id 分子目录）
├── test_data/              # 盲测集（仅题面）
│   ├── test.json
│   ├── tables.json
│   └── test_databases/
├── simple_agent/           # 官方提供的最小评测脚手架
│   ├── simple_benchmark.py
│   ├── comparator.py       # 行级别比对（含数值精度处理）
│   └── utils.py            # SQL 提取（从 LLM 文本中抽 SQL 块）
└── submission/             # 提交模板
```

`dev.json` 单条样本字段：
- `question_id` / `db_id`
- `question`：自然语言问题（中文）
- `SQL`：标准答案（数组，可有多条等价）
- `answer`：真值结果集（按列名 → 列值数组）
- `is_order`：1 表示需按答案顺序比对

## 3. 测试目标

| ID | 指标 | 口径 | dev 基线目标 |
|----|------|------|-------------|
| M1 | **EX@1（Execution Accuracy）** | 生成 SQL 在 Doris 上执行的结果与真值 `answer` 一致 | ≥ 60% |
| M2 | **EX@3** | 允许重试 3 次，任一次正确即算对 | ≥ 70% |
| M3 | **AST 校验通过率** | 04 文档的静态校验通过比例 | ≥ 90% |
| M4 | **召回质量** | 真值 SQL 涉及的表/列在 GraphRAG 子图中被命中的比例 | ≥ 95% |
| M5 | **平均延迟** | 单条 NLQ 端到端 ms（不含 Falcon I/O） | P95 ≤ 4s |
| M6 | **稳定性** | 重复跑 3 次 EX@1 标准差 ≤ 1.5pt | - |

## 4. 数据准备：Falcon → Doris

### 4.1 SQLite → CSV → Doris 灌数路径

Falcon 数据物理上在 SQLite（`dev_databases/<db_id>/<db_id>.sqlite`）。两条可选路径：

- **路径 A（推荐）**：SQLite → CSV → Doris Stream Load
  - 简单、无外部依赖；不走 ChatBI 真实同步链路（仅做"数据落库"）
- **路径 B**：SQLite → MySQL（脚本灌入）→ TIS 同步任务 → Doris
  - 完整跑通 02 文档的同步链路；耗时较长但与生产路径一致

> 一期用 **路径 A** 做端到端打通；二期再切 **路径 B** 做集成验证。

#### 4.1.1 SQLite → CSV（脚本：`tool/falcon-eval/sqlite_to_csv.py`）

推荐使用 Python pandas，便于统一编码、NULL 与精度处理：

```python
import sqlite3, pandas as pd, pathlib, sys

db_id   = sys.argv[1]                    # e.g. "14"
db_path = f"/opt/misc/Falcon/dev_data/dev_databases/{db_id}/{db_id}.sqlite"
out_dir = pathlib.Path(f"./csv/{db_id}")
out_dir.mkdir(parents=True, exist_ok=True)

with sqlite3.connect(db_path) as conn:
    tables = pd.read_sql(
        "SELECT name FROM sqlite_master WHERE type='table'", conn
    )["name"].tolist()
    for t in tables:
        df = pd.read_sql(f"SELECT * FROM `{t}`", conn)
        df.to_csv(out_dir / f"{t}.csv", index=False, encoding="utf-8")
```

或用 `sqlite3` CLI（无 Python 依赖）：

```bash
DB="/opt/misc/Falcon/dev_data/dev_databases/14/14.sqlite"
OUT="./csv/14"
mkdir -p "$OUT"
for tbl in $(sqlite3 "$DB" ".tables"); do
  sqlite3 "$DB" <<SQL
.headers on
.mode csv
.output $OUT/$tbl.csv
SELECT * FROM $tbl;
SQL
done
```

#### 4.1.2 Doris CREATE TABLE 来源

提供两条产 DDL 的路径：

**(A) 直接从 Falcon `tables.json` 生成**（测试场景**默认推荐**，最快）

`tables.json` 已含完整 schema（含 column_type + sample_values），脚本 `tool/falcon-eval/falcon_tables_to_doris_ddl.py`：

```python
import json

# 与 §4.3 类型映射对齐
FALCON_TO_DORIS = {
    "integer":   "BIGINT",
    "real":      "DOUBLE",
    "numeric":   "DOUBLE",
    "text":      "STRING",
    "boolean":   "BOOLEAN",
    "date":      "DATE",
    "datetime":  "DATETIME",
    "timestamp": "DATETIME",
}

def gen_doris_ddl(db_id: str, table: dict) -> str:
    cols = []
    for c in table["columns"]:
        doris_type = FALCON_TO_DORIS.get(c["column_type"].lower(), "STRING")
        cols.append(f"  `{c['column_name']}` {doris_type}")
    cols_sql = ",\n".join(cols)
    # Falcon tables.json 不显式标 PK；测试场景统一 Duplicate + RANDOM bucket，
    # 避开 schema 不规整带来的麻烦
    first_col = table["columns"][0]["column_name"]
    return f"""CREATE TABLE IF NOT EXISTS falcon_{db_id}.`{table['table_name']}` (
{cols_sql}
)
DUPLICATE KEY(`{first_col}`)
DISTRIBUTED BY RANDOM BUCKETS 4
PROPERTIES ("replication_num" = "1");
"""

with open("/opt/misc/Falcon/dev_data/tables.json") as f:
    schemas = json.load(f)

with open("doris_init.sql", "w") as out:
    for db in schemas:
        out.write(f"-- DB: {db['db_id']}\n")
        out.write(f"CREATE DATABASE IF NOT EXISTS falcon_{db['db_id']};\n")
        for tbl in db["tables"]:
            out.write(gen_doris_ddl(db["db_id"], tbl))
```

执行：
```bash
mysql -h <fe_host> -P 9030 -u root < doris_init.sql
```

> `replication_num=1` 仅用于单机/测试 Doris；生产部署改 3。

**(B) 经本体走生产路径** `DorisDDLGenerator`（与 02 §5 同构）

```java
for (OntologyObjectType ot : OntologyObjectType.load("falcon_14")) {
    DorisPhysicalHints hints = new DorisPhysicalHints(
        /*physicalTableName=*/ ot.getName(),
        /*partitionColumn=*/   null,            // 测试集小，不分区
        /*bucketColumns=*/     new String[]{},  // 由 SemanticRole/PK 自动推
        /*buckets=*/           4
    );
    String ddl = ddlGenerator.generate(ot, "doris_falcon", hints);
    runOnDoris(ddl);
}
```

优点：把"本体 → Doris DDL"这条生产代码路径也用 Falcon 数据集回归一遍。

> 一期跑 (A) 做最小闭环；二期切 (B) 顺便做生产代码集成验证。

#### 4.1.3 CSV → Doris Stream Load

CSV 准备好后用 Doris 的 **Stream Load**（HTTP 接口）灌入：

```bash
curl --location-trusted -u root: \
  -T toy_products.csv \
  -H "label:falcon_14_toy_products_$(date +%s)" \
  -H "column_separator:," \
  -H "format:csv" \
  -H "skip_lines:1" \
  -H "max_filter_ratio:0.05" \
  http://<fe_host>:8030/api/falcon_14/toy_products/_stream_load
```

或 Python 包一层（`tool/falcon-eval/load_to_doris.py`）：

```python
import requests, time, pathlib

def stream_load(host, db, table, csv_path, user="root", pwd=""):
    url = f"http://{host}:8030/api/{db}/{table}/_stream_load"
    headers = {
        "label": f"{db}_{table}_{int(time.time())}",
        "column_separator": ",",
        "format": "csv",
        "skip_lines": "1",          # 跳过 CSV 表头
        "max_filter_ratio": "0.05",
        "Expect": "100-continue",
    }
    with open(csv_path, "rb") as f:
        r = requests.put(url, headers=headers, data=f, auth=(user, pwd))
    resp = r.json()
    if resp.get("Status") not in ("Success", "Publish Timeout"):
        raise RuntimeError(f"Stream Load failed: {resp}")
    return resp

# 主流程：遍历 csv/<db_id>/*.csv 全部灌入
db_id = "14"
for csv_file in pathlib.Path(f"./csv/{db_id}").glob("*.csv"):
    table_name = csv_file.stem
    stream_load("127.0.0.1", f"falcon_{db_id}", table_name, str(csv_file))
```

**关键参数说明**：
- `column_separator`：CSV 分隔符，pandas 默认 `,`；遇含逗号的字段需切换为 `\t` 等
- `skip_lines: 1`：跳过 CSV 第一行表头
- `max_filter_ratio: 0.05`：允许 5% 行因脏数据被过滤；测试集小可调严
- `label`：Stream Load 幂等键，重复 label 会被拒绝；实际脚本中带时间戳保证唯一

#### 4.1.4 一键编排

`tool/falcon-eval/bootstrap_falcon.sh`（按顺序执行四步）：

```bash
#!/bin/bash
set -e
cd "$(dirname "$0")"

# Step 1: 生成 Doris DDL
python falcon_tables_to_doris_ddl.py
mysql -h $DORIS_FE -P 9030 -u root < doris_init.sql

# Step 2: 全部 SQLite 库导出 CSV
for db_id in $(ls /opt/misc/Falcon/dev_data/dev_databases); do
    python sqlite_to_csv.py "$db_id"
done

# Step 3: CSV 灌入 Doris
python load_to_doris.py --doris-fe $DORIS_FE

# Step 4: 构建本体（§4.2）
python falcon_to_ontology.py --domain-prefix falcon_

echo "✅  Falcon dev set 数据准备完成"
```

### 4.2 自动建本体

Falcon 提供的 `tables.json` 是结构化 schema（表/列/类型/sample_values）。新增脚本 `tool/falcon-eval/falcon_to_ontology.py`：
- 解析 `tables.json` 中每个 db
- 调用 TIS 的 OpenAPI（或直接写文件）创建 OntologyDomain（命名 `falcon_<db_id>`）
- 对每张表生成 OntologyObjectType + Property（type 用 §4.3 映射）
- **自动启发式**填充：
  - 数值列（integer/real）→ 默认 `Measure`
  - DATE/DATETIME → `TimeDimension`
  - 含 `id` 后缀的列 → `Identifier`
  - 其他字符串 → `Dimension`
- **隐式外键探测**：扫描 PK 与他表同名列，自动建立 `ObjectTypeForeignKeys` 类型的 `OntologyLinker`
- sample_values → 暂作为列描述附加到 Property.description（一期不做 Glossary 映射）

### 4.3 Falcon 列类型 → OntologyType / Doris 类型

| Falcon column_type | OntologyType | Doris 类型 |
|---|---|---|
| integer | INTEGER | BIGINT |
| real / numeric | DOUBLE | DOUBLE |
| text | STRING | STRING |
| boolean | BOOLEAN | BOOLEAN |
| date | DATE | DATE |
| datetime / timestamp | TIMESTAMP | DATETIME |
| 其他 | STRING | STRING |

## 5. 评测流程

### 5.1 整体编排（独立目录 `tool/falcon-eval/`）

> **绝对路径**：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/tool/falcon-eval/`
> 该路径位于独立的 `tis-ontology-plugin` 插件工程下（仓库 `/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/`），落地时新建 `tool/falcon-eval/` 子目录。本文档其它涉及 `tool/falcon-eval/...` 的相对引用都以此为前缀。

```
tool/falcon-eval/
├── bootstrap_falcon.sh                 # §4.1.4 一键编排
├── sqlite_to_csv.py                    # §4.1.1 SQLite → CSV
├── falcon_tables_to_doris_ddl.py       # §4.1.2 (A) tables.json → Doris DDL
├── load_to_doris.py                    # §4.1.3 CSV → Stream Load
├── falcon_to_ontology.py               # §4.2 构建本体
├── run_dev.py                          # 主入口：跑 dev 集
├── run_test.py                         # 主入口：跑 test 集（产出 submission.zip）
├── chatbi_client.py                    # 调用 ChatBIService（HTTP 或 JVM in-process）
├── adapter_dialect.py                  # 方言适配（可选）
└── reporter.py                         # 报告生成（EX@1、失败原因聚合）
```

### 5.2 单条样本流程

```
loop sample in dev.json:
    1. ensure_doris_loaded(sample.db_id)
    2. ensure_ontology_built(sample.db_id)
    3. result = chatbi.ask(domain="falcon_" + db_id, nlq=sample.question)
    4. predicted_rows = doris.execute(result.sql)
    5. ok = comparator.compare(sample.answer, predicted_rows, is_order=sample.is_order)
    6. write_trace(sample.question_id, ok, result.trace)
```

### 5.3 比对器复用

直接复用 Falcon 的 `simple_agent/comparator.py`：
- 行/列级 MD5 比较
- 数值精度归一化（默认 round 到 2 位）
- NULL/Boolean 标准化

### 5.4 方言适配（关键）

Falcon 真值 SQL 含 SQLite 特有函数（`julianday`, `cast as REAL` 等）。
**注意**：我们不需要执行 Falcon 给的真值 SQL —— 只需把"真值结果 `answer`"作为对照，而我们自己生成的是 Doris SQL，本来就该 Doris 方言。所以方言适配主要是单向：**"我们的 SQL 必须能在 Doris 跑"**，与 Falcon 真值 SQL 是否能在 SQLite 跑无关。

但有一个例外：**校验时若想再跑一次真值 SQL 反查**（如真值 answer 不全），需要把真值 SQL 改写为 Doris。这部分作为可选的"自适应真值"模块，一期不实现。

## 6. 报告输出

```
reports/
├── 2026-05-08_dev_run_42.json   # 单次原始结果
├── summary.csv                   # 汇总（按 db_id / 复杂度分组）
└── failures/                    # 失败 case 集
    ├── 14_q123.md               # 含 NLQ + 我们生成的 SQL + 真值 SQL + diff
    └── ...
```

按维度交叉分析：
- 按 `db_id`（领域）：定位是否某领域 schema 表达较弱
- 按 SQL 复杂度（JOIN 数 / CTE 数 / 窗口函数）：识别难点类型
- 按失败原因：`AST_INVALID` / `WRONG_TABLE` / `WRONG_COLUMN` / `WRONG_VALUE` / `EXEC_ERROR` / `SEMANTIC_DIFF`

## 7. CI 集成

| 频次 | 触发 | 范围 | 耗时 |
|------|------|------|------|
| 每 PR | tag `chatbi-touch` 时跑 | dev 集前 50 条 | ≤ 10min |
| 每日定时 | nightly | dev 集全量 | ≤ 60min |
| 发版前 | 手动 | dev + test 全量 | ≤ 120min |

CI 失败条件：EX@1 较上一次基线下降超过 3pt → 阻断合并。

## 8. 任务拆解

| 任务 | 估时 | 依赖 |
|------|------|------|
| T1 `falcon_to_ontology.py` | 2d | 01 |
| T2a `sqlite_to_csv.py` + `falcon_tables_to_doris_ddl.py` | 0.5d | - |
| T2b `load_to_doris.py`（CSV → Stream Load）+ `bootstrap_falcon.sh` 编排 | 0.5d | T2a, 02 (Doris 部署) |
| T3 `chatbi_client.py` + 调用打通 | 1d | 04 |
| T4 `reporter.py`（按 db/复杂度/失败原因聚合） | 1.5d | T3 |
| T5 `run_dev.py` 主流程 + 复用 comparator | 1d | T1-T4 |
| T6 `run_test.py` + 提交格式打包（参考 `submission/format_submission.py`） | 0.5d | T5 |
| T7 CI 集成 | 1d | T5 |
| T8 首轮基线评测 + 失败聚类分析报告 | 2d | 全部 |

## 9. 验收标准

- 跑通完整 dev 集，输出 `summary.csv` 与失败 case 集
- 至少 3 个失败案例的根因分析能反推到 03 或 04 的具体改进点（验证测试-改进闭环）
- CI 上 PR 触发评测 ≤ 10min 完成

## 10. 后续可扩展

- **二期接入 DB-GPT 评测**：Falcon 已与 DB-GPT 集成，可走可视化路径
- **leaderboard 提交**：用 `submission/format_submission.py` 把 trace 整理成官方格式（注意原生需要 trace.jsonl 在 zip 中）
- **自有数据回流**：把内部 1~2 个 domain 的人工标注 NLQ-SQL 对加入测试集，形成"Falcon + 私有"双轨

## 11. 风险

| 风险 | 缓解 |
|------|------|
| Falcon 题面对应的真值是 SQLite/Hive 方言，与 Doris 的精度/排序结果有差 | comparator 已做精度归一；必要时 case-by-case 加白名单 |
| Falcon 题量大，全集跑一次成本高（LLM 调用费用） | CI 仅跑 50 条；夜间 + 缓存历史成功 case 跳过 |
| Doris 部署不可用 → 测试链路阻塞 | 部署 docker-compose 模板（02 文档预留），或临时切回 SQLite 执行链路（仅做语法跑通） |
| Falcon 评测脚本依赖 OpenAI Python SDK | 我们绕开 simple_benchmark.py，仅复用 comparator/utils；自己包 LLM 调用 |