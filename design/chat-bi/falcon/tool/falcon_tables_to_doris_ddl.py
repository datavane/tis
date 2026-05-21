#!/usr/bin/env python3
"""
从 Falcon tables.json 生成 Doris 建库/建表 DDL。

用法：
    python3 falcon_tables_to_doris_ddl.py
    python3 falcon_tables_to_doris_ddl.py --tables-json /opt/misc/Falcon/dev_data/tables.json
    python3 falcon_tables_to_doris_ddl.py --db-id 14   # 只生成指定库

输出：当前目录下的 doris_init.sql
执行：mysql -h <fe_host> -P 9030 -u root < doris_init.sql
"""

import argparse
import json
import pathlib
import sys

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

DEFAULT_TABLES_JSON = "/opt/misc/Falcon/dev_data/tables.json"


def gen_doris_ddl(db_id: str, table: dict) -> str:
    cols = []
    for c in table["columns"]:
        doris_type = FALCON_TO_DORIS.get(c["column_type"].lower(), "STRING")
        cols.append(f"  `{c['column_name']}` {doris_type}")
    cols_sql = ",\n".join(cols)
    # Falcon tables.json 不显式标 PK；测试场景统一 Duplicate + RANDOM bucket，
    # 避开 schema 不规整带来的麻烦
    first_col = table["columns"][0]["column_name"]
    return (
        f"CREATE TABLE IF NOT EXISTS falcon_{db_id}.`{table['table_name']}` (\n"
        f"{cols_sql}\n"
        f")\n"
        f"DUPLICATE KEY(`{first_col}`)\n"
        f"DISTRIBUTED BY RANDOM BUCKETS 4\n"
        f'PROPERTIES ("replication_num" = "1");\n'
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Falcon tables.json → Doris DDL")
    parser.add_argument(
        "--tables-json",
        default=DEFAULT_TABLES_JSON,
        help=f"tables.json 路径（默认：{DEFAULT_TABLES_JSON}）",
    )
    parser.add_argument(
        "--db-id",
        default=None,
        help="只生成指定 db_id 的 DDL（默认生成全部）",
    )
    parser.add_argument(
        "--output",
        default="doris_init.sql",
        help="输出文件路径（默认：doris_init.sql）",
    )
    args = parser.parse_args()

    tables_json = pathlib.Path(args.tables_json)
    if not tables_json.exists():
        print(f"错误：找不到 {tables_json}", file=sys.stderr)
        sys.exit(1)

    with open(tables_json, encoding="utf-8") as f:
        schemas = json.load(f)

    if args.db_id:
        schemas = [db for db in schemas if db["db_id"] == args.db_id]
        if not schemas:
            print(f"错误：未找到 db_id={args.db_id}", file=sys.stderr)
            sys.exit(1)

    with open(args.output, "w", encoding="utf-8") as out:
        for db in schemas:
            out.write(f"-- DB: {db['db_id']}\n")
            out.write(f"CREATE DATABASE IF NOT EXISTS falcon_{db['db_id']};\n")
            for tbl in db["tables"]:
                out.write(gen_doris_ddl(db["db_id"], tbl))
            out.write("\n")

    print(f"已生成 {args.output}（共 {len(schemas)} 个库）")
    print(f"执行方式：mysql -h <fe_host> -P 9030 -u root < {args.output}")


if __name__ == "__main__":
    main()