#!/usr/bin/env python3
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

"""Migrate legacy status.yml files into Tasks-backed TRACKING.md notes.

The default is a dry run. With --write, each source becomes status.yml.legacy
after its TRACKING.md has been generated and validated. No source is deleted.
"""

import argparse
import datetime
import importlib.util
import json
import pathlib
import re
import sys

SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
TEMPLATE = SCRIPT_DIR.parent / "assets" / "item-template" / "TRACKING.md"
BOARD_PATH = SCRIPT_DIR / "triage-status.py"

spec = importlib.util.spec_from_file_location("roller_triage_status", BOARD_PATH)
if spec is None or spec.loader is None:
    sys.exit(f"cannot load {BOARD_PATH}")
board = importlib.util.module_from_spec(spec)
spec.loader.exec_module(board)

FIELD_ORDER = [
    "id", "slug", "title", "owner",
    "reporter", "reported_on", "forwarded_on", "thread", "verified_against",
    "decision", "actor", "preconditions", "affected", "severity", "cvss",
    "cvss_score", "cve", "branch", "fix_commit", "tests", "target_release",
    "credit", "announcement_url", "related", "duplicates", "shares_code_with",
    "blocks",
]
WORKFLOW_FIELDS = {
    "stage", "next_action", "blocked_on", "acknowledged_on",
    "reporter_last_contact", "reporter_deadline", "accepted_on", "rejected_on",
    "cve_requested_on", "cve_state", "fix_ready_on", "fix_committed_on",
    "released_on", "announced_on", "completed_on",
}
MILESTONE_DATES = {
    "acknowledged": "acknowledged_on",
    "accepted": "accepted_on",
    "rejected": "rejected_on",
    "cve-requested": "cve_requested_on",
    "fix-ready": "fix_ready_on",
    "fix-committed": "fix_committed_on",
    "released": "released_on",
    "announced": "announced_on",
    "complete": "completed_on",
}


def scalar(value):
    if value is None or value == "":
        return ""
    if isinstance(value, list):
        return "[" + ", ".join(json.dumps(str(part)) for part in value) + "]"
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, (int, float)):
        return str(value)
    return json.dumps(str(value), ensure_ascii=False)


def render_frontmatter(data):
    keys = [key for key in FIELD_ORDER if key in data and key not in WORKFLOW_FIELDS]
    keys.extend(sorted(key for key in data if key not in keys and key not in WORKFLOW_FIELDS))
    lines = ["---"]
    for key in keys:
        rendered = scalar(data.get(key))
        lines.append(f"{key}: {rendered}" if rendered else f"{key}:")
    lines.append("---")
    return "\n".join(lines)


def one_line(value):
    return re.sub(r"\s+", " ", str(value or "")).strip()


def add_done_date(text, date_value):
    if not date_value or "✅" in text:
        return text
    parsed = board.parse_date(date_value)
    return f"{text} ✅ {parsed}" if parsed else text


def set_checkbox(line, done, not_applicable=False, done_date=None):
    match = board.TASK_RE.match(line)
    if not match:
        return line
    text = match.group(2)
    if not_applicable and "#not-applicable" not in text:
        text += " #not-applicable"
    if done:
        text = add_done_date(text, done_date)
    prefix = line[:line.index("[")]
    return f"{prefix}[{'x' if done else ' '}] {text}"


def mark_legacy_stage(body, data):
    stage = one_line(data.get("stage") or "new")
    allowed = set(board.PROGRESS_STAGES) | {"rejected"}
    if stage not in allowed:
        raise ValueError(f"unknown legacy stage {stage!r}")

    lines = body.splitlines()
    task_rows = []
    stage_rows = {}
    for index, line in enumerate(lines):
        match = board.TASK_RE.match(line)
        if not match:
            continue
        task_rows.append(index)
        milestone = board.MILESTONE_TAG.search(match.group(2))
        if milestone:
            stage_rows[milestone.group(1)] = index

    target = stage_rows.get(stage) if stage != "new" else None
    for index in task_rows:
        milestone_match = board.MILESTONE_TAG.search(lines[index])
        milestone = milestone_match.group(1) if milestone_match else ""
        date_field = MILESTONE_DATES.get(milestone)
        done_date = data.get(date_field) if date_field else None
        # A legacy stage proves that milestone was reached, but not that every
        # earlier checklist row was actually completed. Preserve explicit dates
        # and the current milestone; leave everything else open for review.
        done = bool(milestone and (milestone == stage or done_date))
        not_applicable = False

        if stage == "rejected":
            if milestone == "accepted" or index > target:
                done = True
                not_applicable = True
        elif stage in board.PROGRESS_STAGES[3:]:
            if milestone == "rejected":
                done = True
                not_applicable = True

        lines[index] = set_checkbox(lines[index], done, not_applicable, done_date)
    return "\n".join(lines)


def add_carried_forward_task(body, data):
    action = one_line(data.get("next_action"))
    blocker = one_line(data.get("blocked_on"))
    deadline = board.parse_date(data.get("reporter_deadline"))
    if not action and blocker:
        action = "Resolve legacy blocker"
    if not action and deadline:
        action = "Reach the reporter-response deadline"
    if not action:
        return body

    item_id = one_line(data.get("id")) or "NN"
    task = f"- [ ] {action} #roller-security/item-{item_id}"
    if blocker:
        task += f" #roller-security/blocked — blocked on: {blocker}"
    if deadline:
        task += f" 📅 {deadline}"
    carried = "### Carried-forward work\n\n" + task + "\n\n"
    marker = "### Triage"
    if marker not in body:
        raise ValueError("TRACKING.md template has no intake heading")
    return body.replace(marker, carried + marker, 1)


def add_migration_notes(body, data):
    notes = [
        f"- {datetime.date.today()} — Migrated from `status.yml`; legacy stage was "
        f"`{one_line(data.get('stage')) or 'new'}`."
    ]
    for field, label in (
        ("reporter_last_contact", "Legacy reporter last contact"),
        ("cve_state", "Legacy CVE portal state"),
    ):
        value = one_line(data.get(field))
        if value:
            notes.append(f"- {label}: {value}.")
    return body.rstrip() + "\n\n" + "\n".join(notes) + "\n"


def generate(status_path):
    data = board.parse_yaml(status_path.read_text(encoding="utf-8"))
    item_id = one_line(data.get("id")) or status_path.parent.name.split("-", 1)[0]
    slug = one_line(data.get("slug")) or status_path.parent.name.split("-", 1)[-1]
    title = one_line(data.get("title")) or slug.replace("-", " ")
    data["id"], data["slug"], data["title"] = item_id, slug, title

    template_text = TEMPLATE.read_text(encoding="utf-8")
    _, template_body = board.split_frontmatter(template_text, TEMPLATE)
    body = template_body.replace("#roller-security/item-NN", f"#roller-security/item-{item_id}")
    body = body.replace("# Item NN — title", f"# Item {item_id} — {title}")
    body = mark_legacy_stage(body, data)
    body = add_carried_forward_task(body, data)
    body = add_migration_notes(body, data)
    return render_frontmatter(data) + "\n" + body.lstrip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("triage_dir", help="e.g. triage-<year>/")
    parser.add_argument("--write", action="store_true", help="write notes and rename sources")
    args = parser.parse_args()

    root = pathlib.Path(args.triage_dir)
    if not root.is_dir():
        sys.exit(f"not a directory: {root}")
    statuses = sorted(root.glob("*/status.yml"))
    if not statuses:
        sys.exit(f"no */status.yml found under {root}")

    generated = []
    for status in statuses:
        tracking = status.with_name("TRACKING.md")
        legacy = status.with_name("status.yml.legacy")
        if tracking.exists() or legacy.exists():
            sys.exit(f"refusing to overwrite migration output in {status.parent}")
        try:
            text = generate(status)
        except (OSError, ValueError) as error:
            sys.exit(f"{status}: {error}")
        generated.append((status, tracking, legacy, text))
        print(f"would migrate {status} -> {tracking}")

    if not args.write:
        print(f"dry run: {len(generated)} item(s); re-run with --write after review")
        return

    for status, tracking, legacy, text in generated:
        status.rename(legacy)
        try:
            tracking.write_text(text, encoding="utf-8")
            loaded = board.load_tracking(tracking)
            expected = one_line(board.parse_yaml(legacy.read_text(encoding="utf-8")).get("stage")) or "new"
            if board.val(loaded, "stage") != expected:
                raise ValueError(
                    f"derived stage {board.val(loaded, 'stage')!r} != legacy stage {expected!r}"
                )
        except Exception:
            if tracking.exists():
                tracking.unlink()
            legacy.rename(status)
            raise
        print(f"migrated {status.parent.name}; retained {legacy.name}")


if __name__ == "__main__":
    main()
