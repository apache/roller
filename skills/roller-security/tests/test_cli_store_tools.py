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

"""CLI, store and tool behaviour that needs no LLM."""

from __future__ import annotations

import argparse
import subprocess
from pathlib import Path

import pytest

from roller_security import cli, tools
from roller_security.store import CaseStore, check_cases_consistency

REPO = Path(__file__).resolve().parents[3]


def _start_args(tmp_path: Path) -> argparse.Namespace:
    report = tmp_path / "report.txt"
    report.write_text("synthetic report", encoding="utf-8")
    return argparse.Namespace(
        db="",
        repo=str(REPO),
        triage_dir=str(tmp_path / "triage"),
        slug="demo",
        case_id="01",
        owner="",
        report=str(report),
        dry_run=True,
        mock_llm=True,
        local_llm=False,
        thread_id="",
    )


def test_start_refuses_an_existing_case(tmp_path, capsys):
    args = _start_args(tmp_path)
    cli.cmd_start(args)
    with pytest.raises(SystemExit, match="already exists"):
        cli.cmd_start(args)


def test_export_public_briefs_keeps_only_allowed_keys(tmp_path):
    store = CaseStore(tmp_path / "cases.db")
    store.upsert_case(
        {
            "case_key": "2026-01-a",
            "stage": "fix-committed",
            "decision": "accept",
            "target_release": "7.0.0",
            "fix_commit": "a" * 40,
            "public_brief": {"neutral_change_summary": "Tidy", "cve_id": "CVE-1", "raw": "x"},
        }
    )
    store.set_milestone("2026-01-a", "fix-committed", "done", completed_on="2026-01-01")
    store.upsert_case({"case_key": "2026-02-b", "decision": "reject", "target_release": "7.0.0"})
    for key, stage in (("2026-03-c", "accepted"), ("2026-04-d", "cve-requested")):
        store.upsert_case(
            {"case_key": key, "stage": stage, "decision": "accept", "target_release": "7.0.0",
             "public_brief": {"neutral_change_summary": "Not yet"}}
        )
        store.set_milestone(key, stage, "done", completed_on="2026-01-01")
    briefs = store.export_public_briefs("7.0.0")
    assert [b["case_key"] for b in briefs] == ["2026-01-a"]
    assert set(briefs[0]) == {
        "case_key", "branch_name", "neutral_change_summary", "affected_modules", "upgrade_guidance"
    }


def test_consistency_flags_unclaimed_cases():
    warnings = check_cases_consistency([{"case_key": "k", "stage": "acknowledged", "milestones": {}}])
    assert ("k", "unclaimed — no case coordinator while in flight") in warnings


def _git_repo(path: Path) -> Path:
    subprocess.run(["git", "init", "-q", str(path)], check=True)
    return path


def test_guard_requires_exclusion_inside_the_repo(tmp_path):
    repo = _git_repo(tmp_path / "repo")
    ok, report = tools.guard_private_workspace(str(repo), "triage-2026")
    assert not ok
    assert any("NOT excluded" in line for line in report)

    (repo / ".git" / "info" / "exclude").write_text("/triage-2026/\n", encoding="utf-8")
    ok, report = tools.guard_private_workspace(str(repo), "triage-2026")
    assert ok, report


def test_archive_check_uses_the_thread_api(monkeypatch):
    calls = []

    def fake(url, headers):
        calls.append(url)
        if url.endswith("id=real"):
            return 200, "", b'{"thread": {"mid": "real"}}'
        return 404, "Not Found", b""

    monkeypatch.setattr(tools, "_http_status", fake)
    assert tools.verify_archive_urls(["https://lists.apache.org/thread/real"])[0]
    ok, log = tools.verify_archive_urls(["https://lists.apache.org/thread/made-up"])
    assert not ok and "not in archive" in log
    assert calls[0] == "https://lists.apache.org/api/thread.lua?id=real"


def test_maven_command_is_reactor_safe():
    ok, log = tools.run_maven_tests(".", ["FooTest"], branch="b", dry_run=True)
    assert ok
    assert "clean install" in log
    assert "-Dsurefire.failIfNoSpecifiedTests=false" in log


def test_maven_refuses_the_wrong_branch(tmp_path):
    repo = _git_repo(tmp_path / "repo")
    subprocess.run(["git", "-C", str(repo), "checkout", "-q", "-b", "main"], check=True)
    ok, log = tools.run_maven_tests(str(repo), [], branch="fix-branch")
    assert not ok and "not fix-branch" in log
