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

"""Typed state and structured LLM output schemas for Apache Roller security triage."""

from __future__ import annotations

from typing import Any, Literal, TypedDict
from pydantic import BaseModel, Field


PROGRESS_STAGES: list[str] = [
    "new",
    "acknowledged",
    "investigating",
    "accepted",
    "cve-requested",
    "fix-ready",
    "fix-committed",
    "released",
    "announced",
    "complete",
]
ALL_MILESTONES: list[str] = PROGRESS_STAGES[1:] + ["rejected"]
TERMINAL_STAGES: set[str] = {"rejected", "complete"}


class PublicBrief(TypedDict, total=False):
    """Air-gapped, leak-safe context passed to public-text drafting nodes.

    Never include raw vulnerability reports, CVE IDs, exploit steps, or
    descriptions of what is being withheld.
    """

    case_key: str
    branch_name: str
    neutral_change_summary: str
    affected_modules: list[str]
    upgrade_guidance: str


class SecurityCaseState(TypedDict, total=False):
    # --- Workspace & privacy config ---
    db_path: str
    repo_dir: str
    triage_dir: str
    case_dir: str
    case_key: str               # e.g. "2026-01-short-slug"
    local_llm_only: bool
    dry_run: bool
    mock_llm: bool

    # --- Case metadata (stored in SQLite `cases` table) ---
    case_id: str                # e.g. "01"
    slug: str
    title: str
    owner: str
    reporter: str
    reported_on: str
    forwarded_on: str
    thread_url: str
    verified_against: str
    decision: Literal["pending", "accept", "reject", "duplicate", "wont-fix"]
    actor: str                  # anonymous | authenticated | edit_draft | weblog_admin | global_admin
    preconditions: str
    affected: str
    severity: str               # critical | important | moderate | low
    cvss: str
    cvss_score: str
    cve_id: str
    branch: str
    base_branch: str            # branch the fix is based on (roller-6.1.x or master)
    java_home: str              # JDK home for the base branch's build
    fix_commit: str
    tests: list[str]
    target_release: str
    credit: str
    announcement_url: str
    related: list[str]
    duplicates: list[str]
    shares_code_with: list[str]
    blocks: list[str]

    # --- Workflow & audit state ---
    stage: str
    blocked_reason: str
    reporter_deadline: str
    workspace_safe: bool
    workspace_report: list[str]
    raw_report_path: str
    raw_report_text: str
    evidence_summary: str
    inference_notes: str
    ack_email_draft: str
    decision_email_draft: str
    cve_form_markdown: str
    public_brief: PublicBrief
    pr_number: int | None
    pr_draft_title: str
    pr_draft_body: str
    check_private_ok: bool
    check_private_log: str
    semantic_disclosure_ok: bool
    semantic_disclosure_findings: list[str]
    overlap_warnings: list[str]
    reporter_review_email_draft: str
    release_tag: str
    fix_ancestor_ok: bool
    ancestor_log: str
    archive_log: str
    advisory_archive_urls: list[str]
    archives_verified: bool
    reporter_notified_email_draft: str
    status: str
    history: list[dict[str, Any]]


# ---------------------------------------------------------------------------
# Structured outputs for Tier 1 (Cheap/Local) and Tier 2 (Frontier) nodes
# ---------------------------------------------------------------------------

class ProvenanceExtract(BaseModel):
    """Tier 1: Extract structured provenance facts from a raw vulnerability report."""

    title: str
    reporter: str
    reported_on: str = ""
    affected: str = ""
    claimed_entry_points: list[str] = Field(default_factory=list)


class EmailDraft(BaseModel):
    """Tier 1: Routine email draft grounded in comms-templates.md."""

    subject: str
    body: str


class InvestigationReport(BaseModel):
    """Tier 2: Codebase reachability & privilege investigation."""

    verified_evidence: str = Field(description="Directly verified code/behavior facts only.")
    inference_notes: str = Field(description="Hypotheses or unverified inferences, kept separate.")
    minimum_actor: Literal["anonymous", "authenticated", "edit_draft", "weblog_admin", "global_admin"]
    preconditions: str
    affected_versions: str
    recommended_decision: Literal["accept", "reject", "duplicate", "wont-fix"]
    neutral_change_summary: str = Field(
        description="Leak-safe neutral description of the planned code improvement (no flaw wording)."
    )
    affected_modules: list[str] = Field(default_factory=list)


class SeverityAssessment(BaseModel):
    """Tier 2: ASF severity rating, optional CVSS, and public CVE description draft."""

    asf_severity: Literal["critical", "important", "moderate", "low"]
    cvss_vector: str = ""
    cvss_score: str = ""
    cwe_problem_type: str = Field(description="e.g. 'CWE-79: Improper Neutralization of Input'")
    cve_title: str
    cve_description: str = Field(description="Exact public CVE description text to paste into portal.")
    upgrade_guidance: str = ""


class PublicPrDraft(BaseModel):
    """Tier 1 (air-gapped PublicBrief only): Neutral PR title, body, and commit message."""

    pr_title: str
    pr_body: str
    commit_message: str


class DisclosureAudit(BaseModel):
    """Tier 2: Semantic disclosure & cross-case overlap audit on a candidate fix diff."""

    safe_to_publish: bool
    findings: list[str] = Field(default_factory=list)
    recommended_edits: str = ""
