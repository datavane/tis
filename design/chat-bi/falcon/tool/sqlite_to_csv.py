#!/usr/bin/env python3
"""
将 Falcon dev_databases 中指定 db_id 的 SQLite 库导出为 CSV 文件。

用法：
    python3 sqlite_to_csv.py <db_id>
    python3 sqlite_to_csv.py 14          # 导出到 ./csv/14/
    python3 sqlite_to_csv.py all         # 导出全部 17 个库

输出目录：./csv/<db_id>/<table_name>.csv
"""

import sqlite3
import pathlib
import sys

FALCON_DB_ROOT = pathlib.Path("/opt/misc/Falcon/dev_data/dev_databases")


def export_db(db_id: str) -> None:
    db_path = FALCON_DB_ROOT / db_id / f"{db_id}.sqlite"
    if not db_path.exists():
        print(f"[skip] {db_path} 不存在", file=sys.stderr)
        return

    out_dir = pathlib.Path(f"./csv/{db_id}")
    out_dir.mkdir(parents=True, exist_ok=True)

    with sqlite3.connect(db_path) as conn:
        cur = conn.execute("SELECT name FROM sqlite_master WHERE type='table'")
        tables = [row[0] for row in cur.fetchall()]
        for t in tables:
            rows = conn.execute(f'SELECT * FROM "{t}"').fetchall()
            col_names = [d[0] for d in conn.execute(f'SELECT * FROM "{t}" LIMIT 0').description]
            out_file = out_dir / f"{t}.csv"
            import csv
            with open(out_file, "w", newline="", encoding="utf-8") as f:
                writer = csv.writer(f)
                writer.writerow(col_names)
                writer.writerows(rows)
            print(f"  {db_id}/{t}.csv  ({len(rows)} 行)")


def main() -> None:
    if len(sys.argv) < 2:
        print("用法: python3 sqlite_to_csv.py <db_id|all>")
        sys.exit(1)

    arg = sys.argv[1]
    if arg == "all":
        db_ids = sorted(p.name for p in FALCON_DB_ROOT.iterdir() if p.is_dir())
        print(f"导出全部 {len(db_ids)} 个库：{db_ids}")
        for db_id in db_ids:
            print(f"[{db_id}]")
            export_db(db_id)
    else:
        export_db(arg)

    print("完成。")


if __name__ == "__main__":
    main()