# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""CLI for managing Apache Roller security triage cases with LangGraph."""

from __future__ import annotations

import argparse
import datetime
import json
import os
import sqlite3
from pathlib import Path
from typing import Any

from langgraph.checkpoint.sqlite import SqliteSaver
from langgraph.types import Command

from roller_security.graph import build_security_case_graph
from roller_security.markdown_views import render_board_markdown, render_board_terminal
from roller_security.store import CaseStore, default_db_path


def _resolve_db(args: argparse.Namespace) -> Path:
    if args.db:
        db = Path(args.db).expanduser().resolve()
    elif os.environ.get("ROLLER_SECURITY_DB"):
        db = default_db_path()
    elif getattr(args, "triage_dir", None):
        db = Path(args.triage_dir).expanduser().resolve() / "cases.db"
    else:
        db = default_db_path()
    db.parent.mkdir(parents=True, exist_ok=True)
    return db


def _print_result(graph, thread_id: str) -> None:
    cfg = {"configurable": {"thread_id": thread_id}}
    snap = graph.get_state(cfg)
    vals = snap.values or {}

    print(f"\n=== Thread: {thread_id} ===")
    print(f"Case Key:    {vals.get('case_key', 'unknown')}")
    print(f"Stage:       {vals.get('stage', 'unknown')}")
    print(f"Status:      {vals.get('status', 'unknown')}")
    if vals.get("cve_id"):
        print(f"CVE ID:      {vals.get('cve_id')}")
    if vals.get("target_release"):
        print(f"Target Rel:  {vals.get('target_release')}")

    if vals.get("workspace_report") and vals.get("status") in ("workspace_safe", "workspace_unsafe"):
        print("\nWorkspace guard report:")
        for line in vals["workspace_report"]:
            print(f"  {line}")

    interrupts = []
    for task in snap.tasks:
        for intr in getattr(task, "interrupts", ()):
            interrupts.append(intr.value)

    if interrupts:
        print("\n--- PAUSED AT HUMAN GATE ---")
        print(json.dumps(interrupts[0], indent=2))
        print(f"\nResume with:\n  roller-security resume {thread_id} --json '<JSON payload>'")
    elif not snap.next:
        print("\n--- WORKFLOW FINISHED ---")
        if vals.get("history"):
            print("Execution trace:")
            for item in vals["history"]:
                print(f"  [{item.get('stage', '-')}] {item.get('step')}: {item.get('detail')}")


def cmd_start(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    CaseStore(db)  # Ensure case tables are initialized.

    repo_dir = str(Path(args.repo).resolve())
    triage_dir = str(Path(args.triage_dir).resolve())
    year = datetime.date.today().year
    case_id = args.case_id
    case_key = f"{year}-{case_id}-{args.slug}"
    case_dir = str(Path(triage_dir) / case_key)
    thread_id = args.thread_id or case_key

    raw_report_path = str(Path(args.report).resolve()) if args.report else ""
    raw_report_text = ""
    if raw_report_path and Path(raw_report_path).is_file():
        raw_report_text = Path(raw_report_path).read_text(encoding="utf-8")

    initial_state: dict[str, Any] = {
        "db_path": str(db),
        "repo_dir": repo_dir,
        "triage_dir": triage_dir,
        "case_dir": case_dir,
        "case_key": case_key,
        "case_id": case_id,
        "slug": args.slug,
        "owner": args.owner or "",
        "decision": "pending",
        "stage": "new",
        "raw_report_path": raw_report_path,
        "raw_report_text": raw_report_text,
        "dry_run": args.dry_run,
        "mock_llm": args.mock_llm,
        "local_llm_only": args.local_llm,
        "history": [],
    }

    with SqliteSaver.from_conn_string(str(db)) as checkpointer:
        graph = build_security_case_graph(checkpointer=checkpointer)
        cfg = {"configurable": {"thread_id": thread_id}}
        graph.invoke(initial_state, config=cfg)
        _print_result(graph, thread_id)


def cmd_status(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    with SqliteSaver.from_conn_string(str(db)) as checkpointer:
        graph = build_security_case_graph(checkpointer=checkpointer)
        _print_result(graph, args.thread_id)


def cmd_resume(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    payload = json.loads(args.json)
    with SqliteSaver.from_conn_string(str(db)) as checkpointer:
        graph = build_security_case_graph(checkpointer=checkpointer)
        cfg = {"configurable": {"thread_id": args.thread_id}}
        graph.invoke(Command(resume=payload), config=cfg)
        _print_result(graph, args.thread_id)


def cmd_board(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    store = CaseStore(db)
    triage_dir = str(Path(args.triage_dir).resolve()) if args.triage_dir else None
    cases = store.list_cases(
        triage_dir=triage_dir,
        target_release=args.release or None,
    )
    warnings = store.check_consistency(cases)
    if args.markdown:
        print(render_board_markdown(cases, warnings))
    else:
        print(render_board_terminal(cases, warnings, mine=args.mine or None))


def cmd_export_briefs(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    store = CaseStore(db)
    briefs = store.export_public_briefs(args.target_release)
    print(json.dumps(briefs, indent=2))


def cmd_graph(args: argparse.Namespace) -> None:
    graph = build_security_case_graph().get_graph()
    if args.format == "mermaid":
        print(graph.draw_mermaid())
    elif args.format == "ascii":
        print(graph.draw_ascii())
    elif args.format == "png":
        out = Path(args.output or "roller-security-graph.png")
        out.write_bytes(graph.draw_mermaid_png())
        print(f"Wrote {out.resolve()}")


def cmd_list(args: argparse.Namespace) -> None:
    db = _resolve_db(args)
    if not db.is_file():
        print(f"No security triage database yet at {db}")
        return
    conn = sqlite3.connect(str(db))
    try:
        rows = conn.execute(
            "SELECT DISTINCT thread_id FROM checkpoints ORDER BY thread_id"
        ).fetchall()
    except sqlite3.OperationalError:
        rows = []
    finally:
        conn.close()
    if not rows:
        print("No saved security triage threads.")
        return
    for (tid,) in rows:
        print(tid)


def main() -> None:
    p = argparse.ArgumentParser(
        description="Apache Roller Security Triage Runner (LangGraph, Obsidian-free)"
    )
    p.add_argument(
        "--db",
        help="Path to SQLite DB (default: <triage-dir>/cases.db when --triage-dir is given, else ~/.local/state/roller-security/cases.db)",
    )
    sub = p.add_subparsers(dest="command", required=True)

    st_start = sub.add_parser("start", help="Start a new security triage workflow")
    st_start.add_argument("--repo", required=True, help="Path to Apache Roller git checkout")
    st_start.add_argument("--triage-dir", required=True, help="Path to private triage directory")
    st_start.add_argument("--slug", required=True, help="Short neutral case slug (e.g. feed-parser)")
    st_start.add_argument("--case-id", default="01", help="Two-digit case sequence ID (default: 01)")
    st_start.add_argument("--owner", default="", help="PMC owner handle")
    st_start.add_argument("--report", default="", help="Path to raw vulnerability report file")
    st_start.add_argument("--dry-run", action="store_true", help="Do not modify repo, triage dir, or hit network")
    st_start.add_argument("--mock-llm", action="store_true", help="Use offline mock LLMs (no API keys required)")
    st_start.add_argument("--local-llm", action="store_true", help="Restrict all private LLM calls to local Ollama")
    st_start.add_argument("--thread-id", default="", help="Custom checkpoint thread ID")
    st_start.set_defaults(func=cmd_start)

    st = sub.add_parser("status", help="Inspect current state and active human gate")
    st.add_argument("thread_id")
    st.add_argument("--triage-dir", default="", help="Use <triage-dir>/cases.db")
    st.set_defaults(func=cmd_status)

    res = sub.add_parser("resume", help="Resume a paused human gate with a JSON payload")
    res.add_argument("thread_id")
    res.add_argument("--json", required=True, help="JSON object answering the current gate")
    res.add_argument("--triage-dir", default="", help="Use <triage-dir>/cases.db")
    res.set_defaults(func=cmd_resume)

    bd = sub.add_parser("board", help="Render the security triage status board")
    bd.add_argument("--triage-dir", default="", help="Use <triage-dir>/cases.db and filter cases")
    bd.add_argument("--mine", default="", help="Filter terminal board by owner handle")
    bd.add_argument("--release", default="", help="Filter cases by target release version")
    bd.add_argument("--markdown", action="store_true", help="Output GitHub-flavored Markdown table")
    bd.set_defaults(func=cmd_board)

    exp = sub.add_parser(
        "export-briefs",
        help="Export leak-safe PublicBrief JSON array for roller-release-graph --security",
    )
    exp.add_argument("--target-release", required=True, help="Target release version (e.g. 7.0.0)")
    exp.add_argument("--triage-dir", default="", help="Use <triage-dir>/cases.db")
    exp.set_defaults(func=cmd_export_briefs)

    gr = sub.add_parser("graph", help="Render the LangGraph nodes and edges")
    gr.add_argument("--format", choices=("mermaid", "png", "ascii"), default="mermaid")
    gr.add_argument("-o", "--output", default="", help="Output path for --format png")
    gr.set_defaults(func=cmd_graph)

    ls = sub.add_parser("list", help="List saved security triage threads")
    ls.add_argument("--triage-dir", default="", help="Use <triage-dir>/cases.db")
    ls.set_defaults(func=cmd_list)

    args = p.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
