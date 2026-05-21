#!/usr/bin/env python3
"""
将 sqlite_to_csv.py 导出的 CSV 文件通过 Doris Stream Load 接口灌入 Doris。

用法：
    python3 load_to_doris.py --db-id 14
    python3 load_to_doris.py --db-id all
    python3 load_to_doris.py --db-id 14 --doris-fe 192.168.1.10 --user root --pwd secret

前置条件：
    1. 已执行 sqlite_to_csv.py 生成 ./csv/<db_id>/*.csv
    2. 已执行 falcon_tables_to_doris_ddl.py 并在 Doris 中建好库/表
"""

import argparse
import base64
import http.client
import json
import pathlib
import sys
import time
import urllib.parse

CSV_ROOT = pathlib.Path("./csv")


def _do_put(host: str, port: int, path: str, headers: dict, body: bytes) -> dict:
    """用 http.client 发一次真正带 Expect:100-continue 的 PUT，并手动跟 307 重定向。"""
    conn = http.client.HTTPConnection(host, port, timeout=120)
    conn.request("PUT", path, body=body, headers=headers)
    resp = conn.getresponse()

    # Doris FE 会 307 到 BE
    if resp.status in (301, 302, 307, 308):
        location = resp.getheader("Location")
        print(f"[debug] redirect {resp.status} → {location}", file=sys.stderr)
        resp.read()
        conn.close()
        parsed = urllib.parse.urlparse(location)
        be_host, be_port = parsed.hostname, parsed.port or 8040
        conn2 = http.client.HTTPConnection(be_host, be_port, timeout=120)
        conn2.request("PUT", parsed.path + (f"?{parsed.query}" if parsed.query else ""),
                      body=body, headers=headers)
        resp = conn2.getresponse()

    raw = resp.read().decode("utf-8")
    conn.close()
    return json.loads(raw)


def stream_load(host: str, db: str, table: str, csv_path: str,
                user: str = "root", pwd: str = "") -> dict:
    token = base64.b64encode(f"{user}:{pwd}".encode()).decode()
    label = f"{db}_{table}_{int(time.time())}"
    path = f"/api/{db}/{table}/_stream_load"

    with open(csv_path, "rb") as f:
        body = f.read()

    headers = {
        "Authorization": f"Basic {token}",
        "Expect": "100-continue",
        "label": label,
        "column_separator": ",",
        "format": "csv",
        "skip_lines": "1",
        "max_filter_ratio": "0.05",
        "Content-Length": str(len(body)),
        "Content-Type": "text/plain",
    }

    resp = _do_put(host, 8030, path, headers, body)
    if resp.get("Status") not in ("Success", "Publish Timeout"):
        raise RuntimeError(f"Stream Load failed: {resp}")
    return resp


def load_db(db_id: str, host: str, user: str, pwd: str) -> None:
    csv_dir = CSV_ROOT / db_id
    if not csv_dir.exists():
        print(f"[skip] {csv_dir} 不存在，请先运行 sqlite_to_csv.py {db_id}", file=sys.stderr)
        return

    csv_files = sorted(csv_dir.glob("*.csv"))
    if not csv_files:
        print(f"[skip] {csv_dir} 下没有 CSV 文件", file=sys.stderr)
        return

    doris_db = f"falcon_{db_id}"
    for csv_file in csv_files:
        table_name = csv_file.stem
        try:
            resp = stream_load(host, doris_db, table_name, str(csv_file), user, pwd)
            loaded = resp.get("NumberLoadedRows", "?")
            print(f"  OK  {db_id}/{table_name}.csv  已导入 {loaded} 行")
        except Exception as e:
            print(f"  FAIL {db_id}/{table_name}.csv  {e}", file=sys.stderr)


def main() -> None:
    parser = argparse.ArgumentParser(description="CSV → Doris Stream Load")
    parser.add_argument("--db-id", required=True,
                        help="要导入的 db_id，或 all 表示全部")
    parser.add_argument("--doris-fe", default="127.0.0.1",
                        help="Doris FE host（默认：127.0.0.1）")
    parser.add_argument("--user", default="root", help="Doris 用户名（默认：root）")
    parser.add_argument("--pwd", default="", help="Doris 密码（默认：空）")
    args = parser.parse_args()

    if args.db_id == "all":
        db_ids = sorted(p.name for p in CSV_ROOT.iterdir() if p.is_dir())
        print(f"导入全部 {len(db_ids)} 个库：{db_ids}")
        for db_id in db_ids:
            print(f"[{db_id}]")
            load_db(db_id, args.doris_fe, args.user, args.pwd)
    else:
        load_db(args.db_id, args.doris_fe, args.user, args.pwd)

    print("完成。")


if __name__ == "__main__":
    main()