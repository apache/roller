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

"""SQLite-backed state store for Apache Roller security triage cases."""

from __future__ import annotations

import contextlib
import datetime
import json
import os
import sqlite3
from pathlib import Path
from typing import Any, Iterator

from roller_security.state import ALL_MILESTONES, PROGRESS_STAGES, TERMINAL_STAGES

VALID_MILESTONE_STATUSES: set[str] = {"open", "done", "n/a"}

LIST_FIELDS: tuple[str, ...] = (
    "tests",
    "related",
    "duplicates",
    "shares_code_with",
    "blocks",
)

PUBLIC_BRIEF_ALLOWED_KEYS: tuple[str, ...] = (
    "case_key",
    "branch_name",
    "neutral_change_summary",
    "affected_modules",
    "upgrade_guidance",
)

CASE_COLUMNS: tuple[str, ...] = (
    "case_key",
    "triage_dir",
    "case_dir",
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
    "tests_json",
    "target_release",
    "credit",
    "announcement_url",
    "related_json",
    "duplicates_json",
    "shares_code_with_json",
    "blocks_json",
    "stage",
    "blocked_reason",
    "reporter_deadline",
    "public_brief_json",
    "updated_at",
)

_SCHEMA_SQL = """
CREATE TABLE IF NOT EXISTS cases (
    case_key TEXT PRIMARY KEY,
    triage_dir TEXT NOT NULL DEFAULT '',
    case_dir TEXT NOT NULL DEFAULT '',
    case_id TEXT NOT NULL DEFAULT '',
    slug TEXT NOT NULL DEFAULT '',
    title TEXT NOT NULL DEFAULT '',
    owner TEXT NOT NULL DEFAULT '',
    reporter TEXT NOT NULL DEFAULT '',
    reported_on TEXT NOT NULL DEFAULT '',
    forwarded_on TEXT NOT NULL DEFAULT '',
    thread_url TEXT NOT NULL DEFAULT '',
    verified_against TEXT NOT NULL DEFAULT '',
    decision TEXT NOT NULL DEFAULT 'pending',
    actor TEXT NOT NULL DEFAULT '',
    preconditions TEXT NOT NULL DEFAULT '',
    affected TEXT NOT NULL DEFAULT '',
    severity TEXT NOT NULL DEFAULT '',
    cvss TEXT NOT NULL DEFAULT '',
    cvss_score TEXT NOT NULL DEFAULT '',
    cve_id TEXT NOT NULL DEFAULT '',
    branch TEXT NOT NULL DEFAULT '',
    fix_commit TEXT NOT NULL DEFAULT '',
    tests_json TEXT NOT NULL DEFAULT '[]',
    target_release TEXT NOT NULL DEFAULT '',
    credit TEXT NOT NULL DEFAULT '',
    announcement_url TEXT NOT NULL DEFAULT '',
    related_json TEXT NOT NULL DEFAULT '[]',
    duplicates_json TEXT NOT NULL DEFAULT '[]',
    shares_code_with_json TEXT NOT NULL DEFAULT '[]',
    blocks_json TEXT NOT NULL DEFAULT '[]',
    stage TEXT NOT NULL DEFAULT 'new',
    blocked_reason TEXT NOT NULL DEFAULT '',
    reporter_deadline TEXT NOT NULL DEFAULT '',
    public_brief_json TEXT NOT NULL DEFAULT '{}',
    updated_at TEXT NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS milestones (
    case_key TEXT NOT NULL REFERENCES cases(case_key) ON DELETE CASCADE,
    milestone TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'open',
    completed_on TEXT,
    note TEXT NOT NULL DEFAULT '',
    PRIMARY KEY (case_key, milestone)
);

CREATE TABLE IF NOT EXISTS activity_notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    case_key TEXT NOT NULL REFERENCES cases(case_key) ON DELETE CASCADE,
    noted_on TEXT NOT NULL,
    note TEXT NOT NULL
);
"""


def default_db_path() -> Path:
    """Return the default SQLite path from ROLLER_SECURITY_DB or XDG state dir."""
    env_path = os.environ.get("ROLLER_SECURITY_DB", "").strip()
    if env_path:
        return Path(env_path).expanduser()
    return Path("~/.local/state/roller-security/cases.db").expanduser()


def stage_rank(stage: str) -> int:
    """Return a sortable rank for a workflow stage, matching triage-status.py."""
    if stage == "rejected":
        return len(PROGRESS_STAGES)
    try:
        return PROGRESS_STAGES.index(stage)
    except ValueError:
        return len(PROGRESS_STAGES) + 1


def derive_stage(milestones: dict[str, dict[str, Any]]) -> str:
    """Derive case stage from milestone statuses, matching triage-status.py."""
    completed = {
        name
        for name, info in milestones.items()
        if isinstance(info, dict) and info.get("status") == "done"
    }
    if "rejected" in completed:
        return "rejected"
    reached = [stage for stage in PROGRESS_STAGES[1:] if stage in completed]
    return reached[-1] if reached else "new"


def _utc_now_iso() -> str:
    return datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat()


def _parse_iso_date(value: Any) -> datetime.date | None:
    if not value:
        return None
    try:
        return datetime.date.fromisoformat(str(value).strip()[:10])
    except (TypeError, ValueError):
        return None


def _encode_json_list(value: Any) -> str:
    if value is None:
        return "[]"
    if isinstance(value, str):
        stripped = value.strip()
        if not stripped:
            return "[]"
        try:
            loaded = json.loads(stripped)
            if isinstance(loaded, list):
                return json.dumps([str(item).strip() for item in loaded if str(item).strip()])
        except json.JSONDecodeError:
            pass
        parts = [part.strip().strip("\"'") for part in stripped.strip("[]").split(",") if part.strip()]
        return json.dumps(parts)
    if isinstance(value, (list, tuple, set)):
        return json.dumps([str(item).strip() for item in value if str(item).strip()])
    return json.dumps([str(value).strip()])


def _decode_json_list(raw: str | None) -> list[str]:
    if not raw:
        return []
    try:
        loaded = json.loads(raw)
    except json.JSONDecodeError:
        return []
    if isinstance(loaded, list):
        return [str(item) for item in loaded]
    return []


def _encode_public_brief(value: Any) -> str:
    if value is None:
        return "{}"
    if isinstance(value, str):
        stripped = value.strip()
        if not stripped:
            return "{}"
        try:
            loaded = json.loads(stripped)
            if isinstance(loaded, dict):
                return json.dumps(loaded)
        except json.JSONDecodeError:
            return "{}"
    if isinstance(value, dict):
        return json.dumps(value)
    return "{}"


def _decode_public_brief(raw: str | None) -> dict[str, Any]:
    if not raw:
        return {}
    try:
        loaded = json.loads(raw)
    except json.JSONDecodeError:
        return {}
    return loaded if isinstance(loaded, dict) else {}


def _str_val(value: Any, default: str = "") -> str:
    if value is None:
        return default
    return str(value).strip()


def _case_aliases(case: dict[str, Any]) -> set[str]:
    aliases: set[str] = set()
    for field in ("case_key", "slug"):
        val = _str_val(case.get(field))
        if val:
            aliases.add(val)
    case_dir = _str_val(case.get("case_dir"))
    if case_dir:
        aliases.add(Path(case_dir).name)
    return aliases


def check_cases_consistency(
    cases: list[dict[str, Any]],
    today: datetime.date | None = None,
) -> list[tuple[str, str]]:
    """Run consistency checks across a list of case dicts."""
    check_date = today or datetime.date.today()
    warnings: list[tuple[str, str]] = []

    by_ref: dict[str, dict[str, Any]] = {}
    for case in cases:
        for alias in _case_aliases(case):
            by_ref[alias] = case

    for case in cases:
        key = _str_val(case.get("case_key")) or _str_val(case.get("slug")) or "unknown"
        milestones: dict[str, dict[str, Any]] = case.get("milestones") or {}
        stage = _str_val(case.get("stage")) or derive_stage(milestones)

        accepted_done = milestones.get("accepted", {}).get("status") == "done"
        rejected_done = milestones.get("rejected", {}).get("status") == "done"
        if accepted_done and rejected_done:
            warnings.append((key, "accepted and rejected are both complete and applicable"))

        if stage in PROGRESS_STAGES[2:]:
            reached_idx = PROGRESS_STAGES.index(stage)
            for prior in PROGRESS_STAGES[1:reached_idx]:
                prior_status = milestones.get(prior, {}).get("status", "open")
                if prior_status == "open":
                    warnings.append((key, f"stage is {stage}, but prior {prior} milestone is open"))

        for milestone_name in ALL_MILESTONES:
            info = milestones.get(milestone_name)
            if not info:
                continue
            if info.get("status") == "done" and not _str_val(info.get("completed_on")):
                warnings.append((key, f"completed {milestone_name} milestone has no completed_on date"))

        deadline_str = _str_val(case.get("reporter_deadline"))
        deadline_date = _parse_iso_date(deadline_str)
        if stage not in TERMINAL_STAGES and deadline_date and deadline_date < check_date:
            warnings.append((key, f"overdue reporter_deadline ({deadline_date.isoformat()})"))

        if stage not in TERMINAL_STAGES and stage != "new" and not _str_val(case.get("owner")):
            warnings.append((key, "unclaimed — no case coordinator while in flight"))

        if stage not in {"new", "acknowledged", "investigating", "rejected"} and not _str_val(case.get("severity")):
            warnings.append((key, "accepted without a severity rating"))

        if (
            stage_rank(stage) >= stage_rank("cve-requested")
            and stage != "rejected"
            and not _str_val(case.get("cve_id"))
        ):
            warnings.append((key, "past CVE request but no CVE id recorded"))

        if (
            stage_rank(stage) >= stage_rank("fix-committed")
            and stage != "rejected"
            and not _str_val(case.get("branch"))
        ):
            warnings.append((key, "fix committed but no branch recorded"))

        if (
            stage_rank(stage) >= stage_rank("announced")
            and stage != "rejected"
            and not _str_val(case.get("announcement_url"))
        ):
            warnings.append((key, "announced but announcement_url is empty"))

        decision = _str_val(case.get("decision"))
        if stage == "rejected" and decision in ("", "pending", "accept"):
            warnings.append((key, "rejected milestone complete but decision does not say so"))

        branch = _str_val(case.get("branch"))
        for other_ref in case.get("shares_code_with") or []:
            other = by_ref.get(other_ref)
            if not other:
                warnings.append((key, f"shares_code_with unknown item {other_ref!r}"))
                continue
            other_branch = _str_val(other.get("branch"))
            if branch and other_branch and branch != other_branch:
                warnings.append(
                    (
                        key,
                        f"shares code with {other_ref} on a different branch "
                        f"({branch} vs {other_branch}) — coordinate merge order",
                    )
                )

        for blocked_ref in case.get("blocks") or []:
            other = by_ref.get(blocked_ref)
            if other:
                other_stage = _str_val(other.get("stage")) or derive_stage(other.get("milestones") or {})
                if other_stage not in TERMINAL_STAGES and stage_rank(other_stage) > stage_rank(stage):
                    warnings.append((key, f"blocks {blocked_ref}, which is already further along"))

    return warnings


class CaseStore:
    """SQLite store for Roller security triage cases, milestones, and notes."""

    def __init__(self, db_path: str | Path) -> None:
        self.db_path = Path(db_path).expanduser()
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        with self._connect() as conn:
            conn.executescript(_SCHEMA_SQL)
        os.chmod(self.db_path, 0o600)

    @contextlib.contextmanager
    def _connect(self) -> Iterator[sqlite3.Connection]:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON")
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()

    @staticmethod
    def derive_stage(milestones: dict[str, dict[str, Any]]) -> str:
        """Derive workflow stage from milestone statuses."""
        return derive_stage(milestones)

    def upsert_case(self, data: dict[str, Any]) -> None:
        """Insert or update a case record and ensure all milestone rows exist."""
        case_key = _str_val(data.get("case_key"))
        if not case_key:
            raise ValueError("case_key is required")

        with self._connect() as conn:
            existing_row = conn.execute(
                "SELECT * FROM cases WHERE case_key = ?", (case_key,)
            ).fetchone()
            record: dict[str, Any] = dict(existing_row) if existing_row else {
                "case_key": case_key,
                "triage_dir": "",
                "case_dir": "",
                "case_id": "",
                "slug": "",
                "title": "",
                "owner": "",
                "reporter": "",
                "reported_on": "",
                "forwarded_on": "",
                "thread_url": "",
                "verified_against": "",
                "decision": "pending",
                "actor": "",
                "preconditions": "",
                "affected": "",
                "severity": "",
                "cvss": "",
                "cvss_score": "",
                "cve_id": "",
                "branch": "",
                "fix_commit": "",
                "tests_json": "[]",
                "target_release": "",
                "credit": "",
                "announcement_url": "",
                "related_json": "[]",
                "duplicates_json": "[]",
                "shares_code_with_json": "[]",
                "blocks_json": "[]",
                "stage": "new",
                "blocked_reason": "",
                "reporter_deadline": "",
                "public_brief_json": "{}",
                "updated_at": "",
            }

            scalar_cols = [
                col
                for col in CASE_COLUMNS
                if col not in {
                    "case_key",
                    "tests_json",
                    "related_json",
                    "duplicates_json",
                    "shares_code_with_json",
                    "blocks_json",
                    "public_brief_json",
                    "updated_at",
                }
            ]
            for col in scalar_cols:
                if col in data and data[col] is not None:
                    record[col] = _str_val(data[col])

            for field in LIST_FIELDS:
                json_col = f"{field}_json"
                if field in data and data[field] is not None:
                    record[json_col] = _encode_json_list(data[field])
                elif json_col in data and data[json_col] is not None:
                    record[json_col] = _encode_json_list(data[json_col])

            if "public_brief" in data and data["public_brief"] is not None:
                record["public_brief_json"] = _encode_public_brief(data["public_brief"])
            elif "public_brief_json" in data and data["public_brief_json"] is not None:
                record["public_brief_json"] = _encode_public_brief(data["public_brief_json"])

            record["case_key"] = case_key
            record["updated_at"] = _str_val(data.get("updated_at")) or _utc_now_iso()

            placeholders = ", ".join("?" for _ in CASE_COLUMNS)
            col_names = ", ".join(CASE_COLUMNS)
            update_clause = ", ".join(
                f"{col} = excluded.{col}" for col in CASE_COLUMNS if col != "case_key"
            )
            conn.execute(
                f"INSERT INTO cases ({col_names}) VALUES ({placeholders}) "
                f"ON CONFLICT(case_key) DO UPDATE SET {update_clause}",
                [record[col] for col in CASE_COLUMNS],
            )

            for milestone in ALL_MILESTONES:
                conn.execute(
                    "INSERT OR IGNORE INTO milestones (case_key, milestone, status, completed_on, note) "
                    "VALUES (?, ?, 'open', NULL, '')",
                    (case_key, milestone),
                )

    def set_milestone(
        self,
        case_key: str,
        milestone: str,
        status: str,
        completed_on: str | None = None,
        note: str = "",
    ) -> None:
        """Update a milestone status and recompute the case's derived stage."""
        if milestone not in ALL_MILESTONES:
            raise ValueError(f"Unknown milestone: {milestone!r}")
        if status not in VALID_MILESTONE_STATUSES:
            raise ValueError(f"Invalid milestone status: {status!r}")

        with self._connect() as conn:
            conn.execute(
                "INSERT INTO milestones (case_key, milestone, status, completed_on, note) "
                "VALUES (?, ?, ?, ?, ?) "
                "ON CONFLICT(case_key, milestone) DO UPDATE SET "
                "status = excluded.status, completed_on = excluded.completed_on, note = excluded.note",
                (case_key, milestone, status, completed_on, note or ""),
            )
            milestones = self._load_milestones(conn, case_key)
            new_stage = derive_stage(milestones)
            conn.execute(
                "UPDATE cases SET stage = ?, updated_at = ? WHERE case_key = ?",
                (new_stage, _utc_now_iso(), case_key),
            )

    def mark_remaining_na(
        self,
        case_key: str,
        exclude: set[str] | None = None,
    ) -> None:
        """Mark all still-open milestones (other than exclude) as n/a."""
        excluded = exclude or set()
        with self._connect() as conn:
            rows = conn.execute(
                "SELECT milestone FROM milestones WHERE case_key = ? AND status = 'open'",
                (case_key,),
            ).fetchall()
            for row in rows:
                name = row["milestone"]
                if name in excluded:
                    continue
                conn.execute(
                    "UPDATE milestones SET status = 'n/a' WHERE case_key = ? AND milestone = ?",
                    (case_key, name),
                )
            milestones = self._load_milestones(conn, case_key)
            new_stage = derive_stage(milestones)
            conn.execute(
                "UPDATE cases SET stage = ?, updated_at = ? WHERE case_key = ?",
                (new_stage, _utc_now_iso(), case_key),
            )

    def add_activity_note(
        self,
        case_key: str,
        note: str,
        noted_on: str | None = None,
    ) -> None:
        """Append a dated activity note for a case."""
        date_str = _str_val(noted_on) or datetime.date.today().isoformat()
        with self._connect() as conn:
            conn.execute(
                "INSERT INTO activity_notes (case_key, noted_on, note) VALUES (?, ?, ?)",
                (case_key, date_str, note.strip()),
            )
            conn.execute(
                "UPDATE cases SET updated_at = ? WHERE case_key = ?",
                (_utc_now_iso(), case_key),
            )

    def get_case(self, case_key: str) -> dict[str, Any] | None:
        """Return a fully hydrated case dict, or None if not found."""
        with self._connect() as conn:
            row = conn.execute(
                "SELECT * FROM cases WHERE case_key = ?", (case_key,)
            ).fetchone()
            if not row:
                return None
            return self._hydrate_case(conn, row)

    def list_cases(
        self,
        *,
        triage_dir: str | None = None,
        target_release: str | None = None,
    ) -> list[dict[str, Any]]:
        """List hydrated cases, optionally filtered by triage_dir and/or target_release."""
        clauses: list[str] = []
        params: list[Any] = []
        if triage_dir is not None:
            clauses.append("triage_dir = ?")
            params.append(str(triage_dir))
        if target_release is not None:
            clauses.append("target_release = ?")
            params.append(str(target_release))

        where_sql = f" WHERE {' AND '.join(clauses)}" if clauses else ""
        with self._connect() as conn:
            rows = conn.execute(f"SELECT * FROM cases{where_sql}", params).fetchall()
            cases = [self._hydrate_case(conn, row) for row in rows]

        cases.sort(key=lambda item: (stage_rank(_str_val(item.get("stage"), "new")), item["case_key"]))
        return cases

    def check_consistency(
        self,
        cases: list[dict[str, Any]],
        today: datetime.date | None = None,
    ) -> list[tuple[str, str]]:
        """Validate case consistency and cross-case dependencies."""
        return check_cases_consistency(cases, today=today)

    def check_overlaps(self, case_key: str) -> list[str]:
        """Return human-readable cross-case overlap warnings for a case."""
        case = self.get_case(case_key)
        if not case:
            return []

        triage_dir = _str_val(case.get("triage_dir")) or None
        siblings = self.list_cases(triage_dir=triage_dir)
        by_ref: dict[str, dict[str, Any]] = {}
        for sibling in siblings:
            for alias in _case_aliases(sibling):
                by_ref[alias] = sibling

        warnings: list[str] = []
        own_aliases = _case_aliases(case)
        own_branch = _str_val(case.get("branch"))
        own_stage = _str_val(case.get("stage"), "new")

        for ref in case.get("shares_code_with") or []:
            other = by_ref.get(ref)
            if not other:
                warnings.append(f"shares_code_with references unknown sibling {ref!r}")
                continue
            other_key = other["case_key"]
            other_branch = _str_val(other.get("branch"))
            other_stage = _str_val(other.get("stage"), "new")
            if own_branch and other_branch and own_branch != other_branch:
                warnings.append(
                    f"Shares code with {other_key} (stage={other_stage}) on a different branch "
                    f"({own_branch} vs {other_branch}) — coordinate merge order."
                )
            else:
                warnings.append(
                    f"Shares code with {other_key} (stage={other_stage}, branch={other_branch or 'unset'}) "
                    "— review diff for combined disclosure risk."
                )

        for ref in case.get("blocks") or []:
            other = by_ref.get(ref)
            if not other:
                warnings.append(f"blocks references unknown sibling {ref!r}")
                continue
            other_key = other["case_key"]
            other_stage = _str_val(other.get("stage"), "new")
            if other_stage not in TERMINAL_STAGES and stage_rank(other_stage) > stage_rank(own_stage):
                warnings.append(
                    f"Blocks {other_key}, which is already further along ({other_stage} vs {own_stage})."
                )
            elif other_stage not in TERMINAL_STAGES:
                warnings.append(f"Blocks {other_key} (currently at stage={other_stage}).")

        for ref in case.get("related") or []:
            other = by_ref.get(ref)
            if not other:
                warnings.append(f"related references unknown sibling {ref!r}")
                continue
            other_key = other["case_key"]
            other_stage = _str_val(other.get("stage"), "new")
            if other_stage not in TERMINAL_STAGES:
                warnings.append(
                    f"Related to in-flight case {other_key} (stage={other_stage}, "
                    f"branch={_str_val(other.get('branch')) or 'unset'})."
                )

        # Reverse checks: sibling cases pointing at this case
        for sibling in siblings:
            if sibling["case_key"] == case_key:
                continue
            sib_key = sibling["case_key"]
            sib_stage = _str_val(sibling.get("stage"), "new")
            if own_aliases.intersection(sibling.get("blocks") or []) and sib_stage not in TERMINAL_STAGES:
                warnings.append(f"Blocked by sibling case {sib_key} (stage={sib_stage}).")
            if (
                own_aliases.intersection(sibling.get("shares_code_with") or [])
                and not _case_aliases(sibling).intersection(case.get("shares_code_with") or [])
            ):
                warnings.append(
                    f"Sibling case {sib_key} (stage={sib_stage}) lists shares_code_with this case."
                )

        return warnings

    def export_public_briefs(self, target_release: str) -> list[dict[str, Any]]:
        """Return ONLY leak-safe PublicBrief dicts for cases whose fix is committed.

        A case counts once it reaches ``fix-committed`` (or a later stage) and
        has ``fix_commit`` set; accepted cases with no fix yet are left out.
        """
        committed = PROGRESS_STAGES.index("fix-committed")
        cases = self.list_cases(target_release=target_release)
        briefs: list[dict[str, Any]] = []
        for case in cases:
            stage = _str_val(case.get("stage"), "new")
            decision = _str_val(case.get("decision"))
            if stage not in PROGRESS_STAGES or PROGRESS_STAGES.index(stage) < committed:
                continue
            if not _str_val(case.get("fix_commit")):
                continue
            if decision in {"reject", "duplicate", "wont-fix"}:
                continue

            raw_brief = case.get("public_brief")
            brief_src = raw_brief if isinstance(raw_brief, dict) else {}
            affected_modules = brief_src.get("affected_modules")
            if not isinstance(affected_modules, list):
                affected_modules = []

            brief: dict[str, Any] = {
                "case_key": _str_val(brief_src.get("case_key")) or case["case_key"],
                "branch_name": _str_val(brief_src.get("branch_name")) or _str_val(case.get("branch")),
                "neutral_change_summary": _str_val(brief_src.get("neutral_change_summary")),
                "affected_modules": [str(mod) for mod in affected_modules if str(mod).strip()],
                "upgrade_guidance": _str_val(brief_src.get("upgrade_guidance")),
            }
            # Strictly enforce the leak-safe allowlist.
            briefs.append({k: brief[k] for k in PUBLIC_BRIEF_ALLOWED_KEYS})
        return briefs

    def _load_milestones(
        self,
        conn: sqlite3.Connection,
        case_key: str,
    ) -> dict[str, dict[str, Any]]:
        rows = conn.execute(
            "SELECT milestone, status, completed_on, note FROM milestones WHERE case_key = ?",
            (case_key,),
        ).fetchall()
        by_name = {
            row["milestone"]: {
                "status": row["status"],
                "completed_on": row["completed_on"],
                "note": row["note"] or "",
            }
            for row in rows
        }
        ordered: dict[str, dict[str, Any]] = {}
        for name in ALL_MILESTONES:
            ordered[name] = by_name.get(
                name, {"status": "open", "completed_on": None, "note": ""}
            )
        return ordered

    def _load_activity_notes(
        self,
        conn: sqlite3.Connection,
        case_key: str,
    ) -> list[dict[str, Any]]:
        rows = conn.execute(
            "SELECT id, noted_on, note FROM activity_notes "
            "WHERE case_key = ? ORDER BY noted_on ASC, id ASC",
            (case_key,),
        ).fetchall()
        return [
            {"id": row["id"], "noted_on": row["noted_on"], "note": row["note"]}
            for row in rows
        ]

    def _hydrate_case(
        self,
        conn: sqlite3.Connection,
        row: sqlite3.Row,
    ) -> dict[str, Any]:
        raw = dict(row)
        case_key = raw["case_key"]
        case: dict[str, Any] = {
            "case_key": case_key,
            "triage_dir": raw["triage_dir"],
            "case_dir": raw["case_dir"],
            "case_id": raw["case_id"],
            "slug": raw["slug"],
            "title": raw["title"],
            "owner": raw["owner"],
            "reporter": raw["reporter"],
            "reported_on": raw["reported_on"],
            "forwarded_on": raw["forwarded_on"],
            "thread_url": raw["thread_url"],
            "verified_against": raw["verified_against"],
            "decision": raw["decision"],
            "actor": raw["actor"],
            "preconditions": raw["preconditions"],
            "affected": raw["affected"],
            "severity": raw["severity"],
            "cvss": raw["cvss"],
            "cvss_score": raw["cvss_score"],
            "cve_id": raw["cve_id"],
            "branch": raw["branch"],
            "fix_commit": raw["fix_commit"],
            "tests": _decode_json_list(raw["tests_json"]),
            "target_release": raw["target_release"],
            "credit": raw["credit"],
            "announcement_url": raw["announcement_url"],
            "related": _decode_json_list(raw["related_json"]),
            "duplicates": _decode_json_list(raw["duplicates_json"]),
            "shares_code_with": _decode_json_list(raw["shares_code_with_json"]),
            "blocks": _decode_json_list(raw["blocks_json"]),
            "stage": raw["stage"],
            "blocked_reason": raw["blocked_reason"],
            "reporter_deadline": raw["reporter_deadline"],
            "public_brief": _decode_public_brief(raw["public_brief_json"]),
            "updated_at": raw["updated_at"],
            "milestones": self._load_milestones(conn, case_key),
            "activity_notes": self._load_activity_notes(conn, case_key),
        }
        return case
