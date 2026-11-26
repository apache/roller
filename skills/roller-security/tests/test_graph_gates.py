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

"""Gate validation and failure routing in the security case graph."""

from __future__ import annotations

from pathlib import Path

import pytest
from langgraph.checkpoint.memory import MemorySaver
from langgraph.types import Command

from roller_security import graph as graph_module
from roller_security.graph import _base_branch, build_security_case_graph

REPO = Path(__file__).resolve().parents[3]


@pytest.fixture
def run(tmp_path):
    graph = build_security_case_graph(checkpointer=MemorySaver())
    cfg = {"configurable": {"thread_id": "t1"}}
    report = tmp_path / "report.txt"
    report.write_text("synthetic report", encoding="utf-8")
    graph.invoke(
        {
            "db_path": str(tmp_path / "cases.db"),
            "repo_dir": str(REPO),
            "triage_dir": str(tmp_path / "triage"),
            "case_dir": str(tmp_path / "triage" / "2026-01-demo"),
            "case_key": "2026-01-demo",
            "case_id": "01",
            "slug": "demo",
            "decision": "pending",
            "stage": "new",
            "raw_report_path": str(report),
            "raw_report_text": "synthetic report",
            "dry_run": True,
            "mock_llm": True,
            "local_llm_only": False,
            "history": [],
        },
        config=cfg,
    )

    class Run:
        def resume(self, payload):
            graph.invoke(Command(resume=payload), config=cfg)
            return self

        @property
        def gate(self):
            snap = graph.get_state(cfg)
            for task in snap.tasks:
                for intr in task.interrupts:
                    return intr.value
            return None

        @property
        def values(self):
            return graph.get_state(cfg).values

    return Run()


ACK = {"acknowledged": True}
ACCEPT = {"decision": "accept", "target_release": "6.1.9"}
CVE = {"cve_id": "CVE-2026-0001", "branch": "tidy-params", "tests": ["FooTest"]}
MERGE = {"reporter_approved": True, "fix_commit": "abc123", "pr_number": "#42"}
RELEASE = {"release_tag": "roller-6.1.9"}
CLOSE = {
    "announcement_url": "https://lists.apache.org/thread/abc",
    "advisory_archive_urls": ["https://lists.apache.org/thread/def"],
}


def test_acknowledgement_must_be_true(run):
    run.resume({"acknowledged": False})
    assert run.gate["gate"] == "acknowledge"
    assert "acknowledged" in run.gate["error"]
    run.resume(ACK)
    assert run.gate["gate"] == "pmc_decision"


def test_unknown_decision_asks_again_instead_of_rejecting(run):
    run.resume(ACK).resume({"decision": "accepted"})
    assert run.gate["gate"] == "pmc_decision"
    assert "accepted" in run.gate["error"]
    assert run.values["stage"] != "rejected"


def test_accept_needs_a_target_release(run):
    run.resume(ACK).resume({"decision": "accept"})
    assert run.gate["gate"] == "pmc_decision"
    assert "target_release" in run.gate["error"]


def test_reject_closes_the_case(run):
    run.resume(ACK).resume({"decision": "reject"})
    assert run.gate is None
    assert run.values["stage"] == "rejected"


def test_public_brief_is_shown_and_editable_at_the_decision(run):
    run.resume(ACK)
    assert "public_brief" in run.gate
    run.resume({**ACCEPT, "public_brief": {"neutral_change_summary": "Edited", "cve_id": "x"}})
    brief = run.values["public_brief"]
    assert brief["neutral_change_summary"] == "Edited"
    assert "cve_id" not in brief


def test_merge_needs_a_commit_and_an_approval(run):
    run.resume(ACK).resume(ACCEPT).resume(CVE)
    assert run.gate["gate"] == "reporter_review_and_merge"
    run.resume({"reporter_approved": True})
    assert "fix_commit" in run.gate["error"]
    run.resume({"fix_commit": "abc123", "reporter_approved": False})
    assert "reporter_approved" in run.gate["error"]
    run.resume(MERGE)
    assert run.gate["gate"] == "release_handoff"
    assert run.values["pr_number"] == 42


def test_full_path_completes(run):
    run.resume(ACK).resume(ACCEPT).resume(CVE).resume(MERGE).resume(RELEASE).resume(CLOSE)
    assert run.gate is None
    assert run.values["stage"] == "complete"


def test_failed_ancestor_check_returns_to_release_gate(run, monkeypatch):
    monkeypatch.setattr(
        graph_module, "verify_fix_commit_ancestor", lambda *a, **k: (False, "NOT an ancestor")
    )
    run.resume(ACK).resume(ACCEPT).resume(CVE).resume(MERGE).resume(RELEASE)
    assert run.gate["gate"] == "release_handoff"
    assert run.gate["previous_ancestor_check"] == "NOT an ancestor"


def test_failed_merge_check_returns_to_merge_gate(run, monkeypatch):
    run.resume(ACK).resume(ACCEPT).resume(CVE)
    monkeypatch.setattr(graph_module, "run_check_private", lambda *a, **k: (False, "leak"))
    run.resume(MERGE)
    assert run.gate["gate"] == "reporter_review_and_merge"
    assert run.gate["previous_merge_check"] == "leak"


def test_unverified_archives_keep_the_case_open(run, monkeypatch):
    monkeypatch.setattr(graph_module, "verify_archive_urls", lambda *a, **k: (False, "404"))
    run.resume(ACK).resume(ACCEPT).resume(CVE).resume(MERGE).resume(RELEASE).resume(CLOSE)
    assert run.gate["gate"] == "disclosure_and_close"
    assert run.gate["previous_archive_check"] == "404"
    assert run.values["stage"] != "complete"


def test_base_branch_follows_the_target_release():
    assert _base_branch({"target_release": "6.1.9"}) == "roller-6.1.x"
    assert _base_branch({"target_release": "7.0.0"}) == "master"
    assert _base_branch({"target_release": "6.1.9", "base_branch": "other"}) == "other"
