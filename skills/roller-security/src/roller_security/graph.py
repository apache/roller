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

"""LangGraph StateGraph implementing the Apache Roller security triage workflow."""

from __future__ import annotations

import datetime
from pathlib import Path
from typing import Any, Callable, Literal

from langgraph.graph import END, StateGraph
from langgraph.types import interrupt

from roller_security.markdown_views import write_case_markdown, write_summary_board
from roller_security.models import ModelTier, invoke_structured
from roller_security.state import (
    DisclosureAudit,
    EmailDraft,
    InvestigationReport,
    ProvenanceExtract,
    PublicBrief,
    PublicPrDraft,
    SecurityCaseState,
    SeverityAssessment,
)
from roller_security.store import CaseStore
from roller_security.tools import (
    append_case_section,
    git_range_summary,
    guard_private_workspace,
    read_skill_ref,
    run_check_private,
    run_maven_tests,
    save_original_report,
    scaffold_case_dir,
    verify_archive_urls,
    verify_fix_commit_ancestor,
)


def _today_iso() -> str:
    return datetime.date.today().isoformat()


def _append_history(
    state: SecurityCaseState,
    step: str,
    detail: str,
    *,
    stage: str | None = None,
) -> list[dict[str, Any]]:
    hist = list(state.get("history") or [])
    hist.append(
        {
            "step": step,
            "stage": stage or state.get("stage", "new"),
            "detail": detail,
        }
    )
    return hist


def _ask(
    request: dict[str, Any],
    validate: Callable[[dict[str, Any]], str | None],
) -> dict[str, Any]:
    """Interrupt until the human's answer passes ``validate``.

    ``validate`` returns an error message, or None when the answer is usable.
    Each rejected answer is shown back with the error and the gate asks again.
    """
    prompt = dict(request)
    while True:
        response = interrupt(prompt)
        payload = response if isinstance(response, dict) else {}
        error = validate(payload)
        if error is None:
            return payload
        prompt = {**request, "error": error, "rejected_answer": payload}


def _base_branch(state: SecurityCaseState) -> str:
    """The branch a fix is based on: ``base_branch`` if set, else from the target release."""
    if state.get("base_branch"):
        return str(state["base_branch"])
    return "roller-6.1.x" if str(state.get("target_release", "")).startswith("6.") else "master"


def _with_brief_edits(state: SecurityCaseState, payload: dict[str, Any]) -> PublicBrief:
    """Apply a human's edits to the public brief, keeping only allowed keys."""
    brief: PublicBrief = dict(state.get("public_brief") or {})  # type: ignore[assignment]
    edits = payload.get("public_brief")
    if isinstance(edits, dict):
        for key in ("neutral_change_summary", "affected_modules", "upgrade_guidance"):
            if key in edits:
                brief[key] = edits[key]  # type: ignore[literal-required]
    return brief


def _format_email(draft: EmailDraft) -> str:
    return f"Subject: {draft.subject}\n\n{draft.body}"


def _case_record_from_state(
    state: SecurityCaseState,
    overrides: dict[str, Any] | None = None,
) -> dict[str, Any]:
    merged: dict[str, Any] = dict(state)
    if overrides:
        merged.update(overrides)

    fields = (
        "case_key",
        "case_id",
        "slug",
        "title",
        "owner",
        "reporter",
        "reported_on",
        "forwarded_on",
        "thread_url",
        "verified_against",
        "decision",
        "actor",
        "preconditions",
        "affected",
        "severity",
        "cvss",
        "cvss_score",
        "cve_id",
        "branch",
        "fix_commit",
        "tests",
        "target_release",
        "credit",
        "announcement_url",
        "related",
        "duplicates",
        "shares_code_with",
        "blocks",
        "stage",
        "blocked_reason",
        "reporter_deadline",
        "triage_dir",
        "case_dir",
        "public_brief",
    )
    return {k: merged[k] for k in fields if k in merged}


def _sync_store(
    state: SecurityCaseState,
    *,
    overrides: dict[str, Any] | None = None,
    milestone: str | None = None,
    milestone_status: str = "done",
    extra_milestones: list[tuple[str, str]] | None = None,
    note: str | None = None,
    mark_na_exclude: set[str] | None = None,
) -> None:
    """Persist current state to SQLite and refresh Markdown views when not in dry-run."""
    db_path = state.get("db_path")
    case_key = (overrides or {}).get("case_key") or state.get("case_key")
    if not db_path or not case_key:
        return

    store = CaseStore(db_path)
    record = _case_record_from_state(state, overrides)
    store.upsert_case(record)

    today = _today_iso()
    if milestone:
        store.set_milestone(
            case_key,
            milestone,
            milestone_status,
            completed_on=today if milestone_status == "done" else None,
            note=note or "",
        )
    if extra_milestones:
        for ms_name, ms_status in extra_milestones:
            store.set_milestone(
                case_key,
                ms_name,
                ms_status,
                completed_on=today if ms_status == "done" else None,
            )
    if mark_na_exclude is not None:
        store.mark_remaining_na(case_key, exclude=mark_na_exclude)
    if note:
        store.add_activity_note(case_key, note, noted_on=today)

    if state.get("dry_run"):
        return

    case = store.get_case(case_key)
    if case:
        write_case_markdown(case)

    triage_dir = state.get("triage_dir")
    if triage_dir and Path(triage_dir).is_dir():
        cases = store.list_cases(triage_dir=triage_dir)
        warnings = store.check_consistency(cases)
        write_summary_board(triage_dir, cases, warnings)


# ===========================================================================
# Node 1: Tier 0 — Guard private workspace
# ===========================================================================


def guard_workspace_node(state: SecurityCaseState) -> dict[str, Any]:
    safe, report = guard_private_workspace(
        state["repo_dir"],
        state["triage_dir"],
        dry_run=state.get("dry_run", False),
    )
    return {
        "workspace_safe": safe,
        "workspace_report": report,
        "stage": state.get("stage") or "new",
        "status": "workspace_safe" if safe else "workspace_unsafe",
        "history": _append_history(
            state,
            "guard_workspace",
            "passed" if safe else "; ".join(report),
            stage="new",
        ),
    }


def _route_after_guard(state: SecurityCaseState) -> Literal["safe", "unsafe"]:
    return "safe" if state.get("workspace_safe") else "unsafe"


# ===========================================================================
# Node 2: Tier 0 scaffold + Tier 1 provenance extract & ack email draft
# ===========================================================================


def ingest_and_ack_node(state: SecurityCaseState) -> dict[str, Any]:
    repo_dir = state["repo_dir"]
    case_dir = state.get("case_dir", "")
    dry_run = state.get("dry_run", False)
    mock_llm = state.get("mock_llm", False)
    local_only = state.get("local_llm_only", False)

    if case_dir:
        scaffold_case_dir(repo_dir, case_dir, dry_run=dry_run)

    raw_text = state.get("raw_report_text", "")
    report_path = state.get("raw_report_path", "")
    if not raw_text and report_path and Path(report_path).is_file():
        raw_text = Path(report_path).read_text(encoding="utf-8")
    if case_dir:
        save_original_report(case_dir, raw_text, source=Path(report_path).name, dry_run=dry_run)

    extract_prompt = (
        "Extract structured provenance facts from this private Apache Roller security report.\n"
        "Only report facts explicitly stated in the text; leave missing fields empty.\n\n"
        f"Case key: {state.get('case_key', '')}\n"
        f"Slug: {state.get('slug', '')}\n"
        f"Raw report:\n{raw_text[:12000]}"
    )

    def _mock_provenance() -> ProvenanceExtract:
        slug_title = (state.get("slug") or "security-report").replace("-", " ").title()
        return ProvenanceExtract(
            title=state.get("title") or slug_title,
            reporter=state.get("reporter") or "Security Researcher <researcher@example.org>",
            reported_on=state.get("reported_on") or _today_iso(),
            affected=state.get("affected") or "Apache Roller <= 6.1.5",
            claimed_entry_points=["app/src/main/java/org/apache/roller/weblogger/ui/struts2/"],
        )

    provenance, used_extract = invoke_structured(
        ModelTier.CHEAP,
        ProvenanceExtract,
        extract_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_provenance,
        local_llm_only=local_only,
        private_context=True,
    )

    title = state.get("title") or provenance.title
    reporter = state.get("reporter") or provenance.reporter
    reported_on = state.get("reported_on") or provenance.reported_on
    affected = state.get("affected") or provenance.affected

    comms_ref = read_skill_ref(repo_dir, "comms-templates.md")
    ack_prompt = (
        "Draft a concise, polite Apache Roller PMC security report acknowledgement email.\n"
        "Do NOT confirm validity or promise a CVE yet.\n\n"
        f"Reference templates:\n{comms_ref[:4000]}\n\n"
        f"Case: {state.get('case_key', '')}\n"
        f"Title: {title}\n"
        f"Reporter: {reporter}\n"
        f"Reported on: {reported_on}\n"
    )

    def _mock_ack() -> EmailDraft:
        return EmailDraft(
            subject=f"Re: Security report: {title}",
            body=(
                f"Hello,\n\n"
                f"Thank you for reporting this issue to the Apache Roller security team. "
                f"We have received your report regarding \"{title}\" and are investigating.\n\n"
                f"We will follow up once the PMC has completed its initial assessment.\n\n"
                f"Best regards,\nApache Roller PMC"
            ),
        )

    ack_draft, used_ack = invoke_structured(
        ModelTier.CHEAP,
        EmailDraft,
        ack_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_ack,
        local_llm_only=local_only,
        private_context=True,
    )

    updates: dict[str, Any] = {
        "title": title,
        "reporter": reporter,
        "reported_on": reported_on,
        "affected": affected,
        "decision": state.get("decision") or "pending",
        "raw_report_text": raw_text,
        "ack_email_draft": _format_email(ack_draft),
        "stage": "new",
        "status": "ack_drafted",
        "history": _append_history(
            state,
            "ingest_and_ack",
            f"extract={used_extract}, ack={used_ack}",
            stage="new",
        ),
    }
    _sync_store(state, overrides=updates, note="Ingested report and drafted acknowledgement")
    return updates


# ===========================================================================
# Node 3: Gate 1 — Human acknowledgement confirmation
# ===========================================================================


def gate_acknowledge_node(state: SecurityCaseState) -> dict[str, Any]:
    payload = _ask(
        {
            "gate": "acknowledge",
            "case_key": state.get("case_key"),
            "title": state.get("title"),
            "reporter": state.get("reporter"),
            "ack_email_draft": state.get("ack_email_draft"),
            "instructions": (
                "Send the acknowledgement email via private@roller.apache.org / security@ "
                "and resume with thread metadata."
            ),
            "expected_resume_schema": {
                "acknowledged": True,
                "thread_url": "https://lists.apache.org/thread/...",
                "forwarded_on": "YYYY-MM-DD (optional)",
                "owner": "optional PMC owner handle",
                "credit": "optional reporter credit string",
            },
        },
        lambda answer: None
        if answer.get("acknowledged") is True
        else "Resume with \"acknowledged\": true once the acknowledgement has been sent.",
    )
    updates: dict[str, Any] = {
        "thread_url": payload.get("thread_url", state.get("thread_url", "")),
        "forwarded_on": payload.get("forwarded_on", state.get("forwarded_on", "")),
        "owner": payload.get("owner", state.get("owner", "")),
        "credit": payload.get("credit", state.get("credit", "")),
        "stage": "acknowledged",
        "status": "acknowledged",
        "history": _append_history(
            state,
            "gate_acknowledge",
            "acknowledged=True",
            stage="acknowledged",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="acknowledged",
        milestone_status="done",
        note="Acknowledgement sent to reporter",
    )
    return updates


# ===========================================================================
# Node 4: Tier 2 — Investigate codebase & build air-gapped PublicBrief
# ===========================================================================


def investigate_codebase_node(state: SecurityCaseState) -> dict[str, Any]:
    codebase_map = read_skill_ref(state["repo_dir"], "roller-codebase-map.md")
    prompt = (
        "Investigate this private Apache Roller security report against the codebase architecture.\n"
        "Strictly separate verified evidence from unverified inferences.\n"
        "Also produce a strictly leak-safe neutral_change_summary suitable for public commits/PRs "
        "(no CVE, no vulnerability or exploit wording).\n\n"
        f"Codebase map:\n{codebase_map[:8000]}\n\n"
        f"Case: {state.get('case_key', '')} — {state.get('title', '')}\n"
        f"Claimed affected: {state.get('affected', '')}\n"
        f"Raw report:\n{(state.get('raw_report_text') or '')[:12000]}"
    )

    def _mock_investigation() -> InvestigationReport:
        return InvestigationReport(
            verified_evidence=(
                "Verified request handling path in Roller Struts2 action layer and "
                "confirmed missing server-side validation on the reported parameter."
            ),
            inference_notes="Further testing needed on legacy 6.1.x branch compatibility.",
            minimum_actor="authenticated",
            preconditions="Requires a logged-in user with weblog authoring permissions.",
            affected_versions=state.get("affected") or "Apache Roller <= 6.1.5",
            recommended_decision="accept",
            neutral_change_summary=(
                "Harden request parameter validation and normalization in weblog action handlers."
            ),
            affected_modules=["app"],
        )

    report, used = invoke_structured(
        ModelTier.FRONTIER,
        InvestigationReport,
        prompt,
        mock_llm=state.get("mock_llm", False),
        mock_factory=_mock_investigation,
        local_llm_only=state.get("local_llm_only", False),
        private_context=True,
    )

    public_brief: PublicBrief = {
        "case_key": state.get("case_key", ""),
        "branch_name": state.get("branch", ""),
        "neutral_change_summary": report.neutral_change_summary,
        "affected_modules": list(report.affected_modules),
        "upgrade_guidance": "",
    }

    updates: dict[str, Any] = {
        "evidence_summary": report.verified_evidence,
        "inference_notes": report.inference_notes,
        "actor": report.minimum_actor,
        "preconditions": report.preconditions,
        "affected": report.affected_versions or state.get("affected", ""),
        "public_brief": public_brief,
        "stage": "investigating",
        "status": "investigation_complete",
        "history": _append_history(
            state,
            "investigate_codebase",
            f"model={used}, recommended={report.recommended_decision}",
            stage="investigating",
        ),
    }
    append_case_section(
        state.get("case_dir", ""),
        "IMPLEMENTATION.md",
        f"Generated investigation ({_today_iso()}, {used})",
        f"### Verified evidence\n\n{report.verified_evidence}\n\n"
        f"### Inference (unverified)\n\n{report.inference_notes}\n\n"
        f"- Minimum actor: {report.minimum_actor}\n"
        f"- Preconditions: {report.preconditions}\n"
        f"- Affected versions: {report.affected_versions}\n"
        f"- Recommended decision: {report.recommended_decision}",
        dry_run=state.get("dry_run", False),
    )
    _sync_store(
        state,
        overrides=updates,
        milestone="investigating",
        milestone_status="done",
        note=f"Investigation complete (actor={report.minimum_actor}, recommended={report.recommended_decision})",
    )
    return updates


# ===========================================================================
# Node 5: Gate 2 — PMC decision (accept | reject | duplicate | wont-fix)
# ===========================================================================


def gate_pmc_decision_node(state: SecurityCaseState) -> dict[str, Any]:
    def _check_decision(answer: dict[str, Any]) -> str | None:
        value = str(answer.get("decision", "")).strip().lower()
        if value not in ("accept", "reject", "duplicate", "wont-fix"):
            return f"decision must be accept, reject, duplicate or wont-fix, not {answer.get('decision')!r}"
        if value == "accept" and not str(answer.get("target_release", "")).strip():
            return "target_release is required when accepting"
        return None

    payload = _ask(
        {
            "gate": "pmc_decision",
            "case_key": state.get("case_key"),
            "evidence_summary": state.get("evidence_summary"),
            "inference_notes": state.get("inference_notes"),
            "actor": state.get("actor"),
            "preconditions": state.get("preconditions"),
            "affected": state.get("affected"),
            "public_brief": state.get("public_brief"),
            "instructions": (
                "Record the Apache Roller PMC decision on this report, and check the "
                "public_brief: it becomes public PR and release text, so correct it here. "
                "Use 'accept' to proceed to severity/CVE assessment, or "
                "'reject' / 'duplicate' / 'wont-fix' to draft a closing response."
            ),
            "expected_resume_schema": {
                "decision": "accept | reject | duplicate | wont-fix",
                "target_release": "e.g. 7.0.0 (required when accepting)",
                "owner": "PMC owner handle",
                "verified_against": "optional git commit or tag",
                "related": ["optional case_key list"],
                "duplicates": ["optional case_key list"],
                "shares_code_with": ["optional case_key list"],
                "blocks": ["optional case_key list"],
                "public_brief": {"neutral_change_summary": "optional corrected text"},
            },
        },
        _check_decision,
    )
    decision: Literal["accept", "reject", "duplicate", "wont-fix"] = (
        str(payload["decision"]).strip().lower()  # type: ignore[assignment]
    )

    updates: dict[str, Any] = {
        "decision": decision,
        "target_release": payload.get("target_release", state.get("target_release", "")),
        "owner": payload.get("owner", state.get("owner", "")),
        "verified_against": payload.get("verified_against", state.get("verified_against", "")),
        "related": payload.get("related", state.get("related", [])),
        "duplicates": payload.get("duplicates", state.get("duplicates", [])),
        "shares_code_with": payload.get("shares_code_with", state.get("shares_code_with", [])),
        "blocks": payload.get("blocks", state.get("blocks", [])),
        "public_brief": _with_brief_edits(state, payload),
        "status": f"pmc_decision_{decision}",
        "history": _append_history(
            state,
            "gate_pmc_decision",
            f"decision={decision}, target_release={payload.get('target_release', '')}",
        ),
    }
    _sync_store(state, overrides=updates, note=f"PMC decision recorded: {decision}")
    return updates


def _route_after_pmc_decision(state: SecurityCaseState) -> Literal["accept", "close_rejected"]:
    return "accept" if state.get("decision") == "accept" else "close_rejected"


# ===========================================================================
# Node 6: Tier 1 — Handle rejection / duplicate / wont-fix -> END
# ===========================================================================


def handle_rejection_node(state: SecurityCaseState) -> dict[str, Any]:
    comms_ref = read_skill_ref(state["repo_dir"], "comms-templates.md")
    decision = state.get("decision", "reject")
    prompt = (
        f"Draft a clear, respectful Apache Roller PMC response closing this report as '{decision}'.\n\n"
        f"Reference templates:\n{comms_ref[:4000]}\n\n"
        f"Title: {state.get('title', '')}\n"
        f"Reporter: {state.get('reporter', '')}\n"
        f"Verified evidence: {state.get('evidence_summary', '')}\n"
        f"Duplicates: {state.get('duplicates', [])}\n"
    )

    def _mock_reject_email() -> EmailDraft:
        return EmailDraft(
            subject=f"Re: Security report: {state.get('title', '')}",
            body=(
                f"Hello,\n\n"
                f"Thank you again for your report. After reviewing the codebase, the Apache Roller PMC "
                f"has concluded that this report will be closed as '{decision}'.\n\n"
                f"Summary: {state.get('evidence_summary', 'Does not cross a Roller security boundary.')}\n\n"
                f"Best regards,\nApache Roller PMC"
            ),
        )

    draft, used = invoke_structured(
        ModelTier.CHEAP,
        EmailDraft,
        prompt,
        mock_llm=state.get("mock_llm", False),
        mock_factory=_mock_reject_email,
        local_llm_only=state.get("local_llm_only", False),
        private_context=True,
    )

    updates: dict[str, Any] = {
        "decision_email_draft": _format_email(draft),
        "stage": "rejected",
        "status": "closed_rejected",
        "history": _append_history(
            state,
            "handle_rejection",
            f"decision={decision}, model={used}",
            stage="rejected",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="rejected",
        milestone_status="done",
        mark_na_exclude={"acknowledged", "investigating", "rejected"},
        note=f"Case closed as {decision}",
    )
    return updates


# ===========================================================================
# Node 7: Tier 2 Severity & CVE form + Tier 1 Acceptance email
# ===========================================================================


def _render_cve_form_markdown(state: SecurityCaseState, assessment: SeverityAssessment) -> str:
    return (
        f"# CVE Portal Draft — {state.get('case_key', '')}\n\n"
        f"- **Title**: {assessment.cve_title}\n"
        f"- **ASF Severity**: {assessment.asf_severity}\n"
        f"- **CVSS**: {assessment.cvss_vector or 'N/A'} ({assessment.cvss_score or 'N/A'})\n"
        f"- **Problem Type**: {assessment.cwe_problem_type}\n"
        f"- **Affected Versions**: {state.get('affected', '')}\n"
        f"- **Fixed In**: {state.get('target_release', '')}\n"
        f"- **Credit**: {state.get('credit') or 'TBD (ask the reporter how to be credited)'}\n\n"
        f"## Description\n\n"
        f"{assessment.cve_description}\n\n"
        f"## Upgrade / Mitigation Guidance\n\n"
        f"{assessment.upgrade_guidance or 'Upgrade to the fixed Apache Roller release.'}\n"
    )


def assess_severity_and_cve_node(state: SecurityCaseState) -> dict[str, Any]:
    repo_dir = state["repo_dir"]
    mock_llm = state.get("mock_llm", False)
    local_only = state.get("local_llm_only", False)

    cve_ref = (
        read_skill_ref(repo_dir, "severity-and-cvss.md")
        + "\n\n"
        + read_skill_ref(repo_dir, "cve-portal.md")
    )
    severity_prompt = (
        "Assess the ASF severity rating, optional CVSS vector/score, CWE problem type, "
        "and draft the public ASF CVE portal description for this accepted Apache Roller vulnerability.\n\n"
        f"Reference guide:\n{cve_ref[:5000]}\n\n"
        f"Title: {state.get('title', '')}\n"
        f"Actor: {state.get('actor', '')}\n"
        f"Preconditions: {state.get('preconditions', '')}\n"
        f"Affected: {state.get('affected', '')}\n"
        f"Target release: {state.get('target_release', '')}\n"
        f"Evidence: {state.get('evidence_summary', '')}\n"
    )

    def _mock_severity() -> SeverityAssessment:
        target = state.get("target_release") or "7.0.0"
        return SeverityAssessment(
            asf_severity="moderate",
            cvss_vector="CVSS:3.1/AV:N/AC:L/PR:L/UI:R/S:C/C:L/I:L/A:N",
            cvss_score="5.4",
            cwe_problem_type="CWE-20: Improper Input Validation",
            cve_title=state.get("title") or "Improper Input Validation in Apache Roller",
            cve_description=(
                f"Improper input validation in Apache Roller ({state.get('affected', '<= 6.1.5')}) "
                f"allows an authenticated user to trigger unintended behavior. "
                f"Users are recommended to upgrade to Apache Roller {target}, which fixes the issue."
            ),
            upgrade_guidance=f"Upgrade to Apache Roller {target} or later.",
        )

    assessment, used_sev = invoke_structured(
        ModelTier.FRONTIER,
        SeverityAssessment,
        severity_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_severity,
        local_llm_only=local_only,
        private_context=True,
    )

    cve_form_md = _render_cve_form_markdown(state, assessment)

    comms_ref = read_skill_ref(repo_dir, "comms-templates.md")
    accept_prompt = (
        "Draft an Apache Roller PMC acceptance email notifying the reporter that their report "
        "has been accepted, a CVE is being requested, and a fix is planned.\n\n"
        f"Reference templates:\n{comms_ref[:4000]}\n\n"
        f"Title: {state.get('title', '')}\n"
        f"Severity: {assessment.asf_severity}\n"
        f"Target release: {state.get('target_release', '')}\n"
    )

    def _mock_accept_email() -> EmailDraft:
        return EmailDraft(
            subject=f"Re: Security report: {state.get('title', '')}",
            body=(
                f"Hello,\n\n"
                f"The Apache Roller PMC has confirmed and accepted your report as '{assessment.asf_severity}' severity. "
                f"We are requesting a CVE identifier and preparing a fix targeted for Apache Roller "
                f"{state.get('target_release') or 'the next release'}.\n\n"
                f"Please let us know how you would like to be credited in the advisory.\n\n"
                f"Best regards,\nApache Roller PMC"
            ),
        )

    accept_draft, used_acc = invoke_structured(
        ModelTier.CHEAP,
        EmailDraft,
        accept_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_accept_email,
        local_llm_only=local_only,
        private_context=True,
    )

    public_brief: PublicBrief = dict(state.get("public_brief") or {})  # type: ignore[assignment]
    if assessment.upgrade_guidance:
        public_brief["upgrade_guidance"] = assessment.upgrade_guidance

    updates: dict[str, Any] = {
        "severity": assessment.asf_severity,
        "cvss": assessment.cvss_vector,
        "cvss_score": assessment.cvss_score,
        "cve_form_markdown": cve_form_md,
        "decision_email_draft": _format_email(accept_draft),
        "public_brief": public_brief,
        "stage": "accepted",
        "status": "cve_prepared",
        "history": _append_history(
            state,
            "assess_severity_and_cve",
            f"severity={assessment.asf_severity}, sev_model={used_sev}, email_model={used_acc}",
            stage="accepted",
        ),
    }
    append_case_section(
        state.get("case_dir", ""),
        "CVE_FORM.md",
        f"Generated CVE draft ({_today_iso()}, {used_sev}); reconcile before using the portal",
        cve_form_md,
        dry_run=state.get("dry_run", False),
    )
    _sync_store(
        state,
        overrides=updates,
        milestone="accepted",
        milestone_status="done",
        note=f"Accepted (severity={assessment.asf_severity}); CVE form drafted",
    )
    return updates


# ===========================================================================
# Node 8: Gate 3 — CVE request & fix branch entry
# ===========================================================================


def gate_cve_request_node(state: SecurityCaseState) -> dict[str, Any]:
    gate_payload: dict[str, Any] = {
        "gate": "cve_request",
        "case_key": state.get("case_key"),
        "severity": state.get("severity"),
        "cve_form_markdown": state.get("cve_form_markdown"),
        "decision_email_draft": state.get("decision_email_draft"),
        "public_brief": state.get("public_brief"),
        "base_branch": _base_branch(state),
        "instructions": (
            "Submit the CVE request via cveprocess.apache.org, send the acceptance email, "
            "and prepare a private fix branch with unit tests. Check out the branch in repo_dir "
            "and resume with cve_id, branch and tests. Set java_home to the base branch's JDK "
            "(JDK 11 for roller-6.1.x, 21 for master), and correct public_brief if needed."
        ),
        "expected_resume_schema": {
            "cve_id": "CVE-2026-XXXXX",
            "branch": "fix/neutral-branch-name",
            "tests": ["org.apache.roller.weblogger.SomeTest"],
            "base_branch": "optional; default from target_release",
            "java_home": "optional JDK home for the base branch",
            "public_brief": {"neutral_change_summary": "optional corrected text"},
        },
    }
    if state.get("semantic_disclosure_findings"):
        gate_payload["previous_disclosure_findings"] = state["semantic_disclosure_findings"]
    if state.get("check_private_log") and not state.get("check_private_ok", True):
        gate_payload["previous_check_private_log"] = state["check_private_log"]
    if state.get("overlap_warnings"):
        gate_payload["overlap_warnings"] = state["overlap_warnings"]

    payload = _ask(
        gate_payload,
        lambda answer: None
        if str(answer.get("branch") or state.get("branch") or "").strip()
        else "branch is required: the fix is tested and audited on it.",
    )

    cve_id = payload.get("cve_id", state.get("cve_id", ""))
    branch = payload.get("branch", state.get("branch", ""))
    tests = payload.get("tests", state.get("tests", []))
    if isinstance(tests, str):
        tests = [t.strip() for t in tests.split(",") if t.strip()]

    public_brief = _with_brief_edits(state, payload)
    if branch:
        public_brief["branch_name"] = branch

    updates: dict[str, Any] = {
        "cve_id": cve_id,
        "branch": branch,
        "tests": tests,
        "base_branch": payload.get("base_branch", state.get("base_branch", "")),
        "java_home": payload.get("java_home", state.get("java_home", "")),
        "public_brief": public_brief,
        "stage": "cve-requested",
        "status": "cve_requested",
        "history": _append_history(
            state,
            "gate_cve_request",
            f"cve_id={cve_id}, branch={branch}, tests={len(tests)}",
            stage="cve-requested",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="cve-requested",
        milestone_status="done",
        note=f"CVE requested ({cve_id}); fix branch={branch}",
    )
    return updates


# ===========================================================================
# Node 9: Tier 0 tests + check-private + overlaps + Tier 2 DisclosureAudit
# ===========================================================================


def verify_fix_and_audit_node(state: SecurityCaseState) -> dict[str, Any]:
    repo_dir = state["repo_dir"]
    dry_run = state.get("dry_run", False)
    branch = state.get("branch", "")
    tests = state.get("tests") or []

    tests_ok, tests_log = run_maven_tests(
        repo_dir,
        tests,
        branch=branch,
        java_home=state.get("java_home", ""),
        dry_run=dry_run,
    )
    rev_range = f"{_base_branch(state)}..{branch}" if branch else ""
    check_ok, check_log = run_check_private(repo_dir, rev_range=rev_range, dry_run=dry_run)
    change = "" if dry_run else git_range_summary(repo_dir, rev_range)

    overlaps: list[str] = []
    if state.get("db_path") and state.get("case_key"):
        overlaps = CaseStore(state["db_path"]).check_overlaps(state["case_key"])

    audit_prompt = (
        "Audit this candidate Apache Roller security fix for semantic disclosure leakage.\n"
        "Verify that branch names, commit messages, test names, code comments, the diff "
        "and the public PR text do NOT mention CVE IDs, 'security', 'vulnerability', "
        "'exploit', or hint at undisclosed issues. Consider what the whole change "
        "reveals, including related cases.\n\n"
        f"Branch: {branch} (base {_base_branch(state)})\n"
        f"Tests: {tests}\n"
        f"Public brief: {state.get('public_brief') or {}}\n"
        f"check-private output:\n{check_log[:6000]}\n"
        f"Cross-case overlap warnings: {overlaps}\n\n"
        f"{change}\n"
    )

    def _mock_audit() -> DisclosureAudit:
        return DisclosureAudit(
            safe_to_publish=True,
            findings=[],
            recommended_edits="",
        )

    audit, used = invoke_structured(
        ModelTier.FRONTIER,
        DisclosureAudit,
        audit_prompt,
        mock_llm=state.get("mock_llm", False),
        mock_factory=_mock_audit,
        local_llm_only=state.get("local_llm_only", False),
        private_context=True,
    )

    combined_log = f"[maven tests ok={tests_ok}]\n{tests_log}\n\n[check-private ok={check_ok}]\n{check_log}"
    all_clean = tests_ok and check_ok and audit.safe_to_publish

    updates: dict[str, Any] = {
        "check_private_ok": tests_ok and check_ok,
        "check_private_log": combined_log,
        "semantic_disclosure_ok": audit.safe_to_publish,
        "semantic_disclosure_findings": list(audit.findings),
        "overlap_warnings": overlaps,
        "status": "audit_clean" if all_clean else "audit_needs_work",
        "history": _append_history(
            state,
            "verify_fix_and_audit",
            f"tests_ok={tests_ok}, check_ok={check_ok}, audit_ok={audit.safe_to_publish}, model={used}",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        note=(
            "Fix tests, check-private, and semantic disclosure audit passed"
            if all_clean
            else "Fix verification or disclosure audit requires revisions"
        ),
    )
    return updates


def _route_after_verify_fix(state: SecurityCaseState) -> Literal["clean", "needs_work"]:
    if state.get("check_private_ok") and state.get("semantic_disclosure_ok"):
        return "clean"
    return "needs_work"


# ===========================================================================
# Node 10: Tier 1 air-gapped PublicPrDraft + reporter review email
# ===========================================================================


def draft_pr_and_reporter_review_node(state: SecurityCaseState) -> dict[str, Any]:
    mock_llm = state.get("mock_llm", False)
    local_only = state.get("local_llm_only", False)

    # AIR-GAPPED CONTEXT: ONLY PublicBrief is passed to the public PR drafting prompt.
    # Never include raw_report_text, cve_id, severity, reporter, or evidence_summary.
    brief: PublicBrief = state.get("public_brief") or {}
    pr_prompt = (
        "Draft a strictly neutral GitHub pull request title, body, and commit message "
        "for an Apache Roller code improvement based ONLY on the summary below.\n"
        "Do NOT mention security, vulnerabilities, CVEs, or embargoes.\n\n"
        f"Branch: {brief.get('branch_name', '')}\n"
        f"Change summary: {brief.get('neutral_change_summary', '')}\n"
        f"Affected modules: {', '.join(brief.get('affected_modules') or [])}\n"
        f"Upgrade guidance: {brief.get('upgrade_guidance', '')}\n"
    )

    def _mock_pr() -> PublicPrDraft:
        summary = brief.get("neutral_change_summary") or "Harden request parameter validation"
        return PublicPrDraft(
            pr_title=summary[:72],
            pr_body=(
                f"## Summary\n\n{summary}\n\n"
                f"## Affected Modules\n\n"
                + "\n".join(f"- `{m}`" for m in (brief.get("affected_modules") or ["app"]))
            ),
            commit_message=summary[:72],
        )

    pr_draft, used_pr = invoke_structured(
        ModelTier.CHEAP,
        PublicPrDraft,
        pr_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_pr,
        local_llm_only=local_only,
        # The brief was written by an LLM from the raw report, so --local-llm
        # must keep this call local too.
        private_context=True,
    )

    comms_ref = read_skill_ref(state["repo_dir"], "comms-templates.md")
    review_prompt = (
        "Draft a private email asking the security reporter to review the proposed fix "
        "and confirm it addresses their report, noting a reasonable review deadline.\n\n"
        f"Reference templates:\n{comms_ref[:4000]}\n\n"
        f"Title: {state.get('title', '')}\n"
        f"CVE ID: {state.get('cve_id', '')}\n"
        f"Target release: {state.get('target_release', '')}\n"
    )

    def _mock_review_email() -> EmailDraft:
        return EmailDraft(
            subject=f"Re: Security report: {state.get('title', '')} — fix ready for review",
            body=(
                f"Hello,\n\n"
                f"We have prepared a fix for {state.get('cve_id') or 'your report'} "
                f"targeted for Apache Roller {state.get('target_release') or 'the upcoming release'}.\n"
                f"Please review the attached patch and let us know if it resolves the issue.\n\n"
                f"Best regards,\nApache Roller PMC"
            ),
        )

    review_draft, used_rev = invoke_structured(
        ModelTier.CHEAP,
        EmailDraft,
        review_prompt,
        mock_llm=mock_llm,
        mock_factory=_mock_review_email,
        local_llm_only=local_only,
        private_context=True,
    )

    updates: dict[str, Any] = {
        "pr_draft_title": pr_draft.pr_title,
        "pr_draft_body": f"{pr_draft.pr_body}\n\nCommit message: {pr_draft.commit_message}",
        "reporter_review_email_draft": _format_email(review_draft),
        "stage": "fix-ready",
        "status": "fix_ready",
        "history": _append_history(
            state,
            "draft_pr_and_reporter_review",
            f"pr_model={used_pr}, review_model={used_rev}",
            stage="fix-ready",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="fix-ready",
        milestone_status="done",
        note="Neutral PR draft and reporter review email ready",
    )
    return updates


# ===========================================================================
# Node 11: Gate 4 — Reporter review & merge
# ===========================================================================


def gate_reporter_review_and_merge_node(state: SecurityCaseState) -> dict[str, Any]:
    def _check_merge(answer: dict[str, Any]) -> str | None:
        if not str(answer.get("fix_commit", "")).strip():
            return "fix_commit is required: the merge or squash commit on the release branch"
        if answer.get("reporter_approved") is not True and answer.get("deadline_elapsed") is not True:
            return "set reporter_approved or deadline_elapsed to true before merging"
        pr = answer.get("pr_number")
        if pr not in (None, "") and not str(pr).lstrip("#").isdigit():
            return f"pr_number must be a number, not {pr!r}"
        return None

    payload = _ask(
        {
            "gate": "reporter_review_and_merge",
            "case_key": state.get("case_key"),
            "pr_draft_title": state.get("pr_draft_title"),
            "pr_draft_body": state.get("pr_draft_body"),
            "reporter_review_email_draft": state.get("reporter_review_email_draft"),
            **(
                {"previous_merge_check": state.get("check_private_log")}
                if state.get("status") == "merge_check_warning"
                else {}
            ),
            "instructions": (
                "Send the fix patch to the reporter for review. Once approved (or deadline elapsed), "
                "merge the neutral PR/commit to the target branch and resume with fix_commit."
            ),
            "expected_resume_schema": {
                "reporter_approved": True,
                "deadline_elapsed": "true instead of reporter_approved when the deadline passed",
                "fix_commit": "40-char git SHA",
                "pr_number": 123,
                "reporter_deadline": "YYYY-MM-DD (optional)",
            },
        },
        _check_merge,
    )
    fix_commit = str(payload["fix_commit"]).strip()
    pr_number = payload.get("pr_number", state.get("pr_number"))
    if isinstance(pr_number, str):
        pr_number = pr_number.lstrip("#") or None
    reporter_deadline = payload.get("reporter_deadline", state.get("reporter_deadline", ""))

    updates: dict[str, Any] = {
        "fix_commit": fix_commit,
        "pr_number": int(pr_number) if pr_number is not None else None,
        "reporter_deadline": reporter_deadline,
        "stage": "fix-committed",
        "status": "fix_committed",
        "history": _append_history(
            state,
            "gate_reporter_review_and_merge",
            f"approved={payload.get('reporter_approved') is True}, "
            f"deadline_elapsed={payload.get('deadline_elapsed') is True}, "
            f"fix_commit={fix_commit}, pr={pr_number}",
            stage="fix-committed",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="fix-committed",
        milestone_status="done",
        note=f"Fix committed ({fix_commit})",
    )
    return updates


# ===========================================================================
# Node 12: Tier 0 — Verify merged commit & PR hygiene
# ===========================================================================


def verify_merge_and_pr_node(state: SecurityCaseState) -> dict[str, Any]:
    fix_commit = state.get("fix_commit", "")
    rev_range = f"{fix_commit}~1..{fix_commit}" if fix_commit else ""
    ok, log = run_check_private(
        state["repo_dir"],
        rev_range=rev_range,
        pr=state.get("pr_number"),
        dry_run=state.get("dry_run", False),
    )
    updates: dict[str, Any] = {
        "check_private_ok": ok,
        "check_private_log": log,
        "status": "merge_verified" if ok else "merge_check_warning",
        "history": _append_history(
            state,
            "verify_merge_and_pr",
            f"ok={ok}, pr={state.get('pr_number')}",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        note="Post-merge commit/PR leak check passed" if ok else "Post-merge leak check flagged warnings",
    )
    return updates


# ===========================================================================
# Node 13: Gate 5 — Release handoff (companion roller-release skill)
# ===========================================================================


def gate_release_handoff_node(state: SecurityCaseState) -> dict[str, Any]:
    target = state.get("target_release", "")
    payload = _ask(
        {
            "gate": "release_handoff",
            "case_key": state.get("case_key"),
            "target_release": target,
            "fix_commit": state.get("fix_commit"),
            "public_brief": state.get("public_brief"),
            "instructions": (
                f"Export leak-safe briefs via `roller-security export-briefs --target-release {target or 'X.Y.Z'}` "
                "for the release notes, and release with the roller-release skill. "
                "Resume once the release is published with the final release_tag."
            ),
            "expected_resume_schema": {
                "release_tag": f"roller-{target or '7.0.0'}",
            },
            **(
                {"previous_ancestor_check": state.get("ancestor_log")}
                if state.get("fix_ancestor_ok") is False
                else {}
            ),
        },
        lambda answer: None
        if str(answer.get("release_tag", "")).strip()
        else "release_tag is required",
    )
    release_tag = payload.get("release_tag", state.get("release_tag", ""))

    updates: dict[str, Any] = {
        "release_tag": release_tag,
        "stage": "released",
        "status": "released",
        "history": _append_history(
            state,
            "gate_release_handoff",
            f"release_tag={release_tag}",
            stage="released",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="released",
        milestone_status="done",
        note=f"Fix shipped in release {release_tag}",
    )
    return updates


# ===========================================================================
# Node 14: Tier 0 ancestor check + Tier 1 reporter disclosure notification
# ===========================================================================


def verify_release_and_prepare_disclosure_node(state: SecurityCaseState) -> dict[str, Any]:
    ok, msg = verify_fix_commit_ancestor(
        state["repo_dir"],
        state.get("fix_commit", ""),
        state.get("release_tag", ""),
        dry_run=state.get("dry_run", False),
    )

    comms_ref = read_skill_ref(state["repo_dir"], "comms-templates.md")
    prompt = (
        "Draft a concise email notifying the security reporter that the Apache Roller release "
        "containing the fix is now published and the CVE advisory is being disclosed.\n\n"
        f"Reference templates:\n{comms_ref[:4000]}\n\n"
        f"Title: {state.get('title', '')}\n"
        f"CVE ID: {state.get('cve_id', '')}\n"
        f"Release tag: {state.get('release_tag', '')}\n"
        f"Target release: {state.get('target_release', '')}\n"
    )

    def _mock_notify() -> EmailDraft:
        return EmailDraft(
            subject=f"Re: Security report: {state.get('title', '')} — {state.get('cve_id', '')} disclosed",
            body=(
                f"Hello,\n\n"
                f"Apache Roller {state.get('target_release') or state.get('release_tag', '')} has been released "
                f"with the fix for {state.get('cve_id') or 'your report'}. "
                f"We are publishing the public advisory now.\n\n"
                f"Thank you for responsibly disclosing this issue.\n\n"
                f"Best regards,\nApache Roller PMC"
            ),
        )

    notify_draft, used = invoke_structured(
        ModelTier.CHEAP,
        EmailDraft,
        prompt,
        mock_llm=state.get("mock_llm", False),
        mock_factory=_mock_notify,
        local_llm_only=state.get("local_llm_only", False),
        private_context=True,
    )

    updates: dict[str, Any] = {
        "fix_ancestor_ok": ok,
        "ancestor_log": msg,
        "reporter_notified_email_draft": _format_email(notify_draft),
        "status": "disclosure_ready" if ok else "ancestor_check_failed",
        "history": _append_history(
            state,
            "verify_release_and_prepare_disclosure",
            f"ancestor_ok={ok} ({msg}), model={used}",
        ),
    }
    _sync_store(state, overrides=updates, note=f"Release ancestor verification: {msg}")
    return updates


# ===========================================================================
# Node 15: Gate 6 — Public disclosure verification & close -> END
# ===========================================================================


def gate_disclosure_and_close_node(state: SecurityCaseState) -> dict[str, Any]:
    payload = _ask(
        {
            "gate": "disclosure_and_close",
            "case_key": state.get("case_key"),
            "cve_id": state.get("cve_id"),
            "release_tag": state.get("release_tag"),
            "fix_ancestor_ok": state.get("fix_ancestor_ok"),
            "reporter_notified_email_draft": state.get("reporter_notified_email_draft"),
            "instructions": (
                "Publish the CVE via cveprocess.apache.org, send the public advisory and "
                "reporter notification, and resume with announcement_url and advisory_archive_urls."
            ),
            "expected_resume_schema": {
                "announcement_url": "https://lists.apache.org/thread/...",
                "advisory_archive_urls": ["https://lists.apache.org/thread/..."],
                "closed": True,
            },
            **(
                {"previous_archive_check": state.get("archive_log")}
                if state.get("archives_verified") is False
                else {}
            ),
        },
        lambda answer: None
        if str(answer.get("announcement_url", "")).strip()
        else "announcement_url is required",
    )
    announcement_url = payload.get("announcement_url", state.get("announcement_url", ""))
    archive_urls = payload.get("advisory_archive_urls", state.get("advisory_archive_urls", []))
    if isinstance(archive_urls, str):
        archive_urls = [u.strip() for u in archive_urls.split(",") if u.strip()]

    urls_to_check = list(archive_urls)
    if announcement_url and announcement_url not in urls_to_check:
        urls_to_check.insert(0, announcement_url)

    urls_ok, urls_msg = verify_archive_urls(urls_to_check, dry_run=state.get("dry_run", False))

    if not urls_ok:
        updates: dict[str, Any] = {
            "announcement_url": announcement_url,
            "advisory_archive_urls": archive_urls,
            "archives_verified": False,
            "archive_log": urls_msg,
            "status": "archives_unverified",
            "history": _append_history(
                state, "gate_disclosure_and_close", f"archives_ok=False ({urls_msg})"
            ),
        }
        _sync_store(state, overrides=updates, note=f"Advisory archive check failed: {urls_msg}")
        return updates

    updates = {
        "announcement_url": announcement_url,
        "advisory_archive_urls": archive_urls,
        "archives_verified": True,
        "archive_log": urls_msg,
        "stage": "complete",
        "status": "complete",
        "history": _append_history(
            state,
            "gate_disclosure_and_close",
            f"archives_ok=True ({urls_msg}), closed={payload.get('closed', True)}",
            stage="complete",
        ),
    }
    _sync_store(
        state,
        overrides=updates,
        milestone="announced",
        milestone_status="done",
        extra_milestones=[("complete", "done")],
        note=f"Public disclosure complete ({announcement_url}); archives verified",
    )
    return updates


def _route_after_merge_check(state: SecurityCaseState) -> Literal["ok", "retry"]:
    return "ok" if state.get("check_private_ok") else "retry"


def _route_after_ancestor_check(state: SecurityCaseState) -> Literal["ok", "retry"]:
    return "ok" if state.get("fix_ancestor_ok") else "retry"


def _route_after_disclosure(state: SecurityCaseState) -> Literal["done", "retry"]:
    return "done" if state.get("archives_verified") else "retry"


# ===========================================================================
# Graph builder
# ===========================================================================


def build_security_case_graph(checkpointer=None):
    """Build and compile the Apache Roller security triage StateGraph."""
    builder = StateGraph(SecurityCaseState)

    builder.add_node("guard_workspace", guard_workspace_node)
    builder.add_node("ingest_and_ack", ingest_and_ack_node)
    builder.add_node("gate_acknowledge", gate_acknowledge_node)
    builder.add_node("investigate_codebase", investigate_codebase_node)
    builder.add_node("gate_pmc_decision", gate_pmc_decision_node)
    builder.add_node("handle_rejection", handle_rejection_node)
    builder.add_node("assess_severity_and_cve", assess_severity_and_cve_node)
    builder.add_node("gate_cve_request", gate_cve_request_node)
    builder.add_node("verify_fix_and_audit", verify_fix_and_audit_node)
    builder.add_node("draft_pr_and_reporter_review", draft_pr_and_reporter_review_node)
    builder.add_node("gate_reporter_review_and_merge", gate_reporter_review_and_merge_node)
    builder.add_node("verify_merge_and_pr", verify_merge_and_pr_node)
    builder.add_node("gate_release_handoff", gate_release_handoff_node)
    builder.add_node(
        "verify_release_and_prepare_disclosure",
        verify_release_and_prepare_disclosure_node,
    )
    builder.add_node("gate_disclosure_and_close", gate_disclosure_and_close_node)

    builder.set_entry_point("guard_workspace")

    builder.add_conditional_edges(
        "guard_workspace",
        _route_after_guard,
        {"safe": "ingest_and_ack", "unsafe": END},
    )
    builder.add_edge("ingest_and_ack", "gate_acknowledge")
    builder.add_edge("gate_acknowledge", "investigate_codebase")
    builder.add_edge("investigate_codebase", "gate_pmc_decision")
    builder.add_conditional_edges(
        "gate_pmc_decision",
        _route_after_pmc_decision,
        {
            "accept": "assess_severity_and_cve",
            "close_rejected": "handle_rejection",
        },
    )
    builder.add_edge("handle_rejection", END)
    builder.add_edge("assess_severity_and_cve", "gate_cve_request")
    builder.add_edge("gate_cve_request", "verify_fix_and_audit")
    builder.add_conditional_edges(
        "verify_fix_and_audit",
        _route_after_verify_fix,
        {
            "clean": "draft_pr_and_reporter_review",
            "needs_work": "gate_cve_request",
        },
    )
    builder.add_edge("draft_pr_and_reporter_review", "gate_reporter_review_and_merge")
    builder.add_edge("gate_reporter_review_and_merge", "verify_merge_and_pr")
    builder.add_conditional_edges(
        "verify_merge_and_pr",
        _route_after_merge_check,
        {"ok": "gate_release_handoff", "retry": "gate_reporter_review_and_merge"},
    )
    builder.add_edge("gate_release_handoff", "verify_release_and_prepare_disclosure")
    builder.add_conditional_edges(
        "verify_release_and_prepare_disclosure",
        _route_after_ancestor_check,
        {"ok": "gate_disclosure_and_close", "retry": "gate_release_handoff"},
    )
    builder.add_conditional_edges(
        "gate_disclosure_and_close",
        _route_after_disclosure,
        {"done": END, "retry": "gate_disclosure_and_close"},
    )

    return builder.compile(checkpointer=checkpointer)


# Module-level compiled graph for LangGraph Studio (`langgraph.json`).
graph = build_security_case_graph()
