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

"""Plain-Markdown (GFM) and terminal renderers for Roller security cases.

Generated Markdown files are read-only projections of SQLite state and contain
zero Obsidian syntax (no emojis, no #roller-security tags, no ```tasks blocks).
"""

from __future__ import annotations

from pathlib import Path
from typing import Any

from roller_security.state import ALL_MILESTONES, PROGRESS_STAGES, TERMINAL_STAGES
from roller_security.store import derive_stage, stage_rank

GENERATED_BANNER = "<!-- Generated from SQLite by roller-security; do not edit manually -->"
DISPLAY_STAGES: list[str] = PROGRESS_STAGES[:-1] + ["rejected", "complete"]


def _md_cell(value: Any) -> str:
    if value is None:
        return "-"
    if isinstance(value, list):
        text = ", ".join(str(item) for item in value if str(item).strip())
    else:
        text = str(value).strip()
    if not text:
        return "-"
    return text.replace("|", "\\|").replace("\n", " ")


def _term_cell(value: Any, width: int) -> str:
    text = str(value or "-").strip() or "-"
    if len(text) > width:
        text = text[: width - 1] + "..." if width > 3 else text[:width]
    return text.ljust(width)


def _next_open_milestone(case: dict[str, Any]) -> str | None:
    milestones = case.get("milestones") or {}
    stage = str(case.get("stage") or "").strip() or derive_stage(milestones)
    if stage in TERMINAL_STAGES:
        return None
    for name in PROGRESS_STAGES[1:]:
        info = milestones.get(name)
        status = info.get("status", "open") if isinstance(info, dict) else "open"
        if status == "open":
            return name
    return None


def render_case_markdown(case: dict[str, Any]) -> str:
    """Render a single case as clean GitHub-Flavored Markdown."""
    milestones = case.get("milestones") or {}
    stage = str(case.get("stage") or "").strip() or derive_stage(milestones)
    case_key = str(case.get("case_key") or case.get("slug") or "case").strip()
    title = str(case.get("title") or "").strip()
    heading = f"# {case_key} -- {title}" if title else f"# {case_key}"

    lines: list[str] = [
        GENERATED_BANNER,
        "",
        heading,
        "",
        "> **PRIVATE.** Undisclosed vulnerability under the ASF security process. "
        "Do not commit, paste into a public issue, or quote in a commit message.",
        "",
        "## Metadata",
        "",
        "| Field | Value |",
        "| --- | --- |",
    ]

    metadata_rows: list[tuple[str, Any]] = [
        ("Case key", case_key),
        ("Case ID", case.get("case_id")),
        ("Slug", case.get("slug")),
        ("Stage", stage),
        ("Owner", case.get("owner")),
        ("Decision", case.get("decision")),
        ("Severity", case.get("severity")),
        ("CVSS", case.get("cvss")),
        ("CVSS score", case.get("cvss_score")),
        ("CVE ID", case.get("cve_id")),
        ("Target release", case.get("target_release")),
        ("Branch", case.get("branch")),
        ("Fix commit", case.get("fix_commit")),
        ("Tests", case.get("tests")),
        ("Reporter", case.get("reporter")),
        ("Reported on", case.get("reported_on")),
        ("Forwarded on", case.get("forwarded_on")),
        ("Thread URL", case.get("thread_url")),
        ("Verified against", case.get("verified_against")),
        ("Minimum actor", case.get("actor")),
        ("Preconditions", case.get("preconditions")),
        ("Affected", case.get("affected")),
        ("Credit", case.get("credit")),
        ("Announcement URL", case.get("announcement_url")),
        ("Reporter deadline", case.get("reporter_deadline")),
        ("Related", case.get("related")),
        ("Duplicates", case.get("duplicates")),
        ("Shares code with", case.get("shares_code_with")),
        ("Blocks", case.get("blocks")),
        ("Updated at", case.get("updated_at")),
    ]
    for label, value in metadata_rows:
        lines.append(f"| {label} | {_md_cell(value)} |")

    lines.extend([
        "",
        "## Milestones",
        "",
        "| Milestone | Status | Completed | Note |",
        "| --- | --- | --- | --- |",
    ])
    for name in ALL_MILESTONES:
        info = milestones.get(name) if isinstance(milestones, dict) else None
        status = info.get("status", "open") if isinstance(info, dict) else "open"
        completed_on = info.get("completed_on") if isinstance(info, dict) else None
        note = info.get("note", "") if isinstance(info, dict) else ""
        lines.append(
            f"| {name} | {_md_cell(status)} | {_md_cell(completed_on)} | {_md_cell(note)} |"
        )

    lines.extend(["", "## Blockers", ""])
    blocked_reason = str(case.get("blocked_reason") or "").strip()
    deadline = str(case.get("reporter_deadline") or "").strip()
    if blocked_reason or deadline:
        if blocked_reason:
            lines.append(f"- Blocked: {blocked_reason}")
        if deadline:
            lines.append(f"- Reporter deadline: {deadline}")
    else:
        lines.append("None.")

    lines.extend(["", "## Activity notes", ""])
    notes = case.get("activity_notes") or []
    if notes:
        for entry in notes:
            noted_on = str(entry.get("noted_on") or "").strip()
            note_text = str(entry.get("note") or "").strip()
            if not note_text:
                continue
            if noted_on:
                lines.append(f"- {noted_on}: {note_text}")
            else:
                lines.append(f"- {note_text}")
    else:
        lines.append("None.")

    return "\n".join(lines) + "\n"


def write_case_markdown(case: dict[str, Any]) -> Path | None:
    """Write <case_dir>/CASE.md if case_dir exists, returning the written path."""
    case_dir_raw = str(case.get("case_dir") or "").strip()
    if not case_dir_raw:
        return None
    case_dir = Path(case_dir_raw)
    if not case_dir.is_dir():
        return None
    out_path = case_dir / "CASE.md"
    out_path.write_text(render_case_markdown(case), encoding="utf-8")
    return out_path


def render_board_terminal(
    cases: list[dict[str, Any]],
    warnings: list[tuple[str, str]],
    *,
    mine: str | None = None,
) -> str:
    """Render a compact terminal status board, next actions, release gates, and checks."""
    rows = sorted(
        cases,
        key=lambda item: (
            stage_rank(str(item.get("stage") or "new")),
            str(item.get("case_key") or ""),
        ),
    )
    width = max([len(str(item.get("case_key") or "")) for item in rows] + [4])
    divider = "-" * (width + 68)

    out: list[str] = [
        "",
        f"{'CASE'.ljust(width)}  {'STAGE'.ljust(14)}{'OWNER'.ljust(12)}  "
        f"{'SEV'.ljust(10)}  {'CVE'.ljust(16)}  BRANCH",
        divider,
    ]

    current_stage: str | None = None
    for item in rows:
        stage = str(item.get("stage") or "new")
        if stage != current_stage:
            current_stage = stage
            out.append("")
        owner = str(item.get("owner") or "").strip()
        marker = "* " if mine and owner and mine.lower() in owner.lower() else "  "
        case_key = str(item.get("case_key") or "")
        out.append(
            f"{case_key.ljust(width)}{marker}{stage.ljust(14)}"
            f"{_term_cell(owner, 12)}  "
            f"{_term_cell(item.get('severity'), 10)}  "
            f"{_term_cell(item.get('cve_id'), 16)}  "
            f"{str(item.get('branch') or '').strip() or '-'}"
        )

    out.extend(["", "NEXT ACTIONS", divider])
    any_next = False
    for item in rows:
        next_ms = _next_open_milestone(item)
        if not next_ms:
            continue
        any_next = True
        case_key = str(item.get("case_key") or "")
        line = f"{case_key.ljust(width)}  {next_ms}"
        owner = str(item.get("owner") or "").strip()
        deadline = str(item.get("reporter_deadline") or "").strip()
        blocked = str(item.get("blocked_reason") or "").strip()
        if owner:
            line += f"   [owner: {owner}]"
        if deadline:
            line += f"   [deadline: {deadline}]"
        if blocked:
            line += f"   [blocked: {blocked}]"
        out.append(line)
    if not any_next:
        out.append("None.")

    counts: dict[str, int] = {}
    for item in rows:
        stage = str(item.get("stage") or "new")
        counts[stage] = counts.get(stage, 0) + 1
    summary = "  ".join(f"{s}={counts[s]}" for s in DISPLAY_STAGES if s in counts)
    out.extend(["", f"{len(rows)} case(s):  {summary}" if summary else f"{len(rows)} case(s)"])

    releases = sorted(
        {str(item.get("target_release") or "").strip() for item in rows if str(item.get("target_release") or "").strip()}
    )
    for release in releases:
        pending = [
            str(item.get("case_key") or "")
            for item in rows
            if str(item.get("target_release") or "").strip() == release
            and str(item.get("stage") or "new") not in TERMINAL_STAGES
            and stage_rank(str(item.get("stage") or "new")) < stage_rank("fix-ready")
        ]
        if pending:
            out.append(
                f"Release {release} is gated on {len(pending)} case(s) not fix-ready: "
                f"{', '.join(pending)}"
            )

    if warnings:
        out.extend(["", "CHECKS", divider])
        for case_key, message in warnings:
            out.append(f"{case_key.ljust(width)}  {message}")

    return "\n".join(out) + "\n"


def render_board_markdown(
    cases: list[dict[str, Any]],
    warnings: list[tuple[str, str]],
) -> str:
    """Render a clean GFM summary board with zero Obsidian syntax."""
    rows = sorted(
        cases,
        key=lambda item: (
            stage_rank(str(item.get("stage") or "new")),
            str(item.get("case_key") or ""),
        ),
    )

    lines: list[str] = [
        GENERATED_BANNER,
        "",
        "# Security Triage Summary Board",
        "",
        "| Case | Stage | Owner | Severity | CVE | Target Release | Branch | Next Milestone | Blocked |",
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for item in rows:
        next_ms = _next_open_milestone(item) or "-"
        lines.append(
            f"| {_md_cell(item.get('case_key'))} "
            f"| {_md_cell(item.get('stage') or 'new')} "
            f"| {_md_cell(item.get('owner'))} "
            f"| {_md_cell(item.get('severity'))} "
            f"| {_md_cell(item.get('cve_id'))} "
            f"| {_md_cell(item.get('target_release'))} "
            f"| {_md_cell(item.get('branch'))} "
            f"| {_md_cell(next_ms)} "
            f"| {_md_cell(item.get('blocked_reason'))} |"
        )

    counts: dict[str, int] = {}
    for item in rows:
        stage = str(item.get("stage") or "new")
        counts[stage] = counts.get(stage, 0) + 1
    summary = ", ".join(f"{s}={counts[s]}" for s in DISPLAY_STAGES if s in counts)
    lines.extend(["", f"**Total:** {len(rows)} case(s)" + (f" ({summary})" if summary else "")])

    releases = sorted(
        {str(item.get("target_release") or "").strip() for item in rows if str(item.get("target_release") or "").strip()}
    )
    gate_lines: list[str] = []
    for release in releases:
        pending = [
            str(item.get("case_key") or "")
            for item in rows
            if str(item.get("target_release") or "").strip() == release
            and str(item.get("stage") or "new") not in TERMINAL_STAGES
            and stage_rank(str(item.get("stage") or "new")) < stage_rank("fix-ready")
        ]
        if pending:
            gate_lines.append(
                f"- Release `{release}` is gated on {len(pending)} case(s) not fix-ready: "
                f"{', '.join(pending)}"
            )
    if gate_lines:
        lines.extend(["", "## Release gates", ""])
        lines.extend(gate_lines)

    if warnings:
        lines.extend(["", "## Consistency checks", ""])
        for case_key, message in warnings:
            lines.append(f"- `{case_key}`: {message}")

    return "\n".join(lines) + "\n"


def write_summary_board(
    triage_dir: str | Path,
    cases: list[dict[str, Any]],
    warnings: list[tuple[str, str]],
) -> Path:
    """Write <triage_dir>/SUMMARY_BOARD.md and return its path."""
    root = Path(triage_dir)
    root.mkdir(parents=True, exist_ok=True)
    out_path = root / "SUMMARY_BOARD.md"
    out_path.write_text(render_board_markdown(cases, warnings), encoding="utf-8")
    return out_path
