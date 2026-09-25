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

"""Print a Tasks-backed board for in-flight vulnerability reports.

Usage:
    python3 scripts/triage-status.py triage-<year>/
    python3 scripts/triage-status.py triage-<year>/ --mine "<coordinator>"

Reads each triage-<year>/<NN-slug>/TRACKING.md. YAML frontmatter supplies stable
case metadata; Obsidian Tasks checkboxes supply stage, next action, blockers,
and dates. This script validates that source directly and never writes status.

No dependencies. PyYAML is used when present; otherwise a small parser covers
the frontmatter subset used by the template.
"""

import argparse
import datetime
import pathlib
import re
import sys

PROGRESS_STAGES = [
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
DISPLAY_STAGES = PROGRESS_STAGES[:-1] + ["rejected", "complete"]
TERMINAL = {"rejected", "complete"}
MILESTONE_TAG = re.compile(r"#roller-security/stage/([a-z0-9-]+)")
TASK_RE = re.compile(r"^\s*[-*]\s+\[([^\]])\]\s+(.*?)\s*$")
DATE_PATTERNS = {
    "due": re.compile(r"📅\s*(\d{4}-\d{2}-\d{2})"),
    "done_on": re.compile(r"✅\s*(\d{4}-\d{2}-\d{2})"),
}


# --------------------------------------------------------------------------- #
# parsing
# --------------------------------------------------------------------------- #

def _fallback_parse(text):
    """Parse the flat key/value YAML subset used by TRACKING.md frontmatter."""
    data = {}
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        raw = lines[i]
        i += 1
        line = raw.split("#", 1)[0].rstrip() if not raw.lstrip().startswith("#") else ""
        if not line.strip():
            continue
        match = re.match(r"^([A-Za-z_][A-Za-z0-9_]*):\s*(.*)$", line)
        if not match:
            continue
        key, value = match.group(1), match.group(2).strip()
        if value in (">-", "|", ">", "|-"):
            block = []
            while i < len(lines):
                nxt = lines[i]
                if nxt.strip() and not nxt.startswith((" ", "\t")):
                    break
                block.append(nxt.strip())
                i += 1
            data[key] = " ".join(part for part in block if part).strip()
            continue
        if value.startswith("[") and value.endswith("]"):
            inner = value[1:-1].strip()
            data[key] = [
                part.strip().strip("\"'") for part in inner.split(",") if part.strip()
            ]
            continue
        data[key] = value.strip().strip("\"'")
    return data


def parse_yaml(text):
    try:
        import yaml  # type: ignore

        data = yaml.safe_load(text) or {}
        if isinstance(data, dict):
            return dict(data)
    except Exception:
        pass
    return _fallback_parse(text)


def split_frontmatter(text, path):
    lines = text.splitlines()
    if not lines or lines[0].strip() != "---":
        raise ValueError(f"{path}: missing YAML frontmatter")
    for index in range(1, len(lines)):
        if lines[index].strip() == "---":
            return "\n".join(lines[1:index]), "\n".join(lines[index + 1:])
    raise ValueError(f"{path}: unterminated YAML frontmatter")


def parse_date(value):
    if not value:
        return None
    try:
        return datetime.date.fromisoformat(str(value)[:10])
    except (TypeError, ValueError):
        return None


def clean_task_text(text):
    text = MILESTONE_TAG.sub("", text)
    text = re.sub(r"\s+#roller-security/item-[A-Za-z0-9_-]+", "", text)
    text = re.sub(r"\s+#roller-security/blocked\b", "", text)
    text = re.sub(r"\s+#not-applicable\b", "", text)
    text = re.sub(r"\s+#owner/[A-Za-z0-9_-]+", "", text)
    for pattern in DATE_PATTERNS.values():
        text = pattern.sub("", text)
    return re.sub(r"\s{2,}", " ", text).strip()


def parse_tasks(body):
    tasks = []
    for line_number, line in enumerate(body.splitlines(), 1):
        match = TASK_RE.match(line)
        if not match:
            continue
        marker, text = match.groups()
        milestone = MILESTONE_TAG.search(text)
        due = DATE_PATTERNS["due"].search(text)
        done_on = DATE_PATTERNS["done_on"].search(text)
        owner = re.search(r"#owner/([A-Za-z0-9_-]+)", text)
        tasks.append({
            "line": line_number,
            "marker": marker,
            "done": marker.lower() == "x",
            "text": text,
            "display": clean_task_text(text),
            "stage": milestone.group(1) if milestone else "",
            "applicable": "#not-applicable" not in text,
            "blocked": "#roller-security/blocked" in text,
            "item_tagged": "#roller-security/item-" in text,
            "due": parse_date(due.group(1)) if due else None,
            "done_on": parse_date(done_on.group(1)) if done_on else None,
            "owner": owner.group(1) if owner else "",
        })
    return tasks


def val(item, key, default=""):
    value = item.get(key, default)
    if value is None:
        return default
    if isinstance(value, list):
        return [str(part) for part in value]
    return str(value).strip()


def as_list(item, key):
    value = item.get(key) or []
    if isinstance(value, str):
        value = [part.strip() for part in value.strip("[]").split(",") if part.strip()]
    return [str(part).strip().strip("\"'") for part in value if str(part).strip()]


def derive_stage(tasks):
    completed = {
        task["stage"] for task in tasks
        if task["done"] and task["applicable"] and task["stage"]
    }
    if "rejected" in completed:
        return "rejected"
    reached = [stage for stage in PROGRESS_STAGES[1:] if stage in completed]
    return reached[-1] if reached else "new"


def next_task(item):
    if val(item, "stage") in TERMINAL:
        return None
    for task in item["_tasks"]:
        if not task["done"] and task["applicable"]:
            return task
    return None


def load_tracking(path):
    frontmatter, body = split_frontmatter(path.read_text(encoding="utf-8"), path)
    data = parse_yaml(frontmatter)
    data["_dir"] = path.parent.name
    data["_path"] = path
    data["_tasks"] = parse_tasks(body)
    data["stage"] = derive_stage(data["_tasks"])
    data["_next_task"] = next_task(data)
    return data


def stage_rank(stage):
    if stage == "rejected":
        return len(PROGRESS_STAGES)
    try:
        return PROGRESS_STAGES.index(stage)
    except ValueError:
        return len(PROGRESS_STAGES) + 1


def cell(value, width):
    text = str(value or "—")
    if len(text) > width:
        text = text[:width - 1] + "…"
    return text.ljust(width)


# --------------------------------------------------------------------------- #
# checks
# --------------------------------------------------------------------------- #

def check(items, today):
    warnings = []
    by_slug = {val(item, "slug") or item["_dir"]: item for item in items}

    for item in items:
        name = item["_dir"]
        stage = val(item, "stage", "new")
        tasks = item["_tasks"]
        normal_milestones = {
            task["stage"] for task in tasks
            if task["done"] and task["applicable"] and task["stage"]
        }

        if not tasks:
            warnings.append((name, "no Tasks checklist found"))
            continue
        untagged = [task for task in tasks if not task["item_tagged"]]
        if untagged:
            warnings.append((name, f"{len(untagged)} workflow task(s) lack an item tag"))
        unknown = sorted({
            task["stage"] for task in tasks
            if task["stage"] and task["stage"] not in set(PROGRESS_STAGES) | {"rejected"}
        })
        for unknown_stage in unknown:
            warnings.append((name, f"unknown milestone stage {unknown_stage!r}"))

        missing = [
            milestone for milestone in PROGRESS_STAGES[1:] + ["rejected"]
            if not any(task["stage"] == milestone for task in tasks)
        ]
        if missing:
            warnings.append((name, "missing milestone task(s): " + ", ".join(missing)))

        if "accepted" in normal_milestones and "rejected" in normal_milestones:
            warnings.append((name, "accepted and rejected are both complete and applicable"))
        if stage in PROGRESS_STAGES[2:]:
            reached = PROGRESS_STAGES.index(stage)
            for prior in PROGRESS_STAGES[1:reached]:
                prior_tasks = [task for task in tasks if task["stage"] == prior]
                if prior_tasks and not any(task["done"] or not task["applicable"] for task in prior_tasks):
                    warnings.append((name, f"stage is {stage}, but prior {prior} milestone is open"))
        for task in tasks:
            if task["done"] and task["stage"] and task["applicable"] and not task["done_on"]:
                warnings.append((name, f"completed {task['stage']} milestone has no ✅ date"))
            if not task["done"] and not task["applicable"]:
                warnings.append((name, f"line {task['line']} is open but tagged #not-applicable"))
            if not task["done"] and task["due"] and task["due"] < today:
                warnings.append((name, f"overdue task ({task['due']}): {task['display']}"))

        if stage not in TERMINAL and not item["_next_task"]:
            warnings.append((name, "no open next task, but item is not terminal"))
        if stage not in TERMINAL and stage != "new" and not val(item, "owner"):
            warnings.append((name, "unclaimed — no case coordinator while in flight"))

        if stage not in {"new", "acknowledged", "investigating", "rejected"} and not val(item, "severity"):
            warnings.append((name, "accepted without a severity rating"))
        if stage_rank(stage) >= stage_rank("cve-requested") and stage != "rejected" and not val(item, "cve"):
            warnings.append((name, "past CVE request but no CVE id recorded"))
        if stage_rank(stage) >= stage_rank("fix-committed") and stage != "rejected" and not val(item, "branch"):
            warnings.append((name, "fix committed but no branch recorded"))
        if stage_rank(stage) >= stage_rank("announced") and stage != "rejected" and not val(item, "announcement_url"):
            warnings.append((name, "announced but announcement_url is empty"))
        if stage == "rejected" and val(item, "decision") in ("", "pending", "accept"):
            warnings.append((name, "rejected milestone complete but decision does not say so"))

        for other_slug in as_list(item, "shares_code_with"):
            other = by_slug.get(other_slug)
            if not other:
                warnings.append((name, f"shares_code_with unknown item {other_slug!r}"))
                continue
            branch, other_branch = val(item, "branch"), val(other, "branch")
            if branch and other_branch and branch != other_branch:
                warnings.append((name, f"shares code with {other_slug} on a different branch "
                                       f"({branch} vs {other_branch}) — coordinate merge order"))
        for blocked_slug in as_list(item, "blocks"):
            other = by_slug.get(blocked_slug)
            if (other and val(other, "stage") not in TERMINAL
                    and stage_rank(val(other, "stage")) > stage_rank(stage)):
                warnings.append((name, f"blocks {blocked_slug}, which is already further along"))

    return warnings


# --------------------------------------------------------------------------- #
# output
# --------------------------------------------------------------------------- #

def render(items, today, mine=None):
    rows = sorted(items, key=lambda item: (stage_rank(val(item, "stage")), item["_dir"]))
    width = max([len(item["_dir"]) for item in rows] + [4])
    out = ["", f"{'ITEM'.ljust(width)}  {'STAGE'.ljust(14)}{'OWNER'.ljust(12)}  "
                 f"{'SEV'.ljust(10)}  {'CVE'.ljust(16)}  BRANCH",
           "-" * (width + 68)]

    current = None
    for item in rows:
        stage = val(item, "stage", "new")
        if stage != current:
            current = stage
            out.append("")
        owner = val(item, "owner")
        marker = "* " if mine and owner and mine.lower() in owner.lower() else "  "
        out.append(
            f"{item['_dir'].ljust(width)}{marker}{stage.ljust(14)}"
            f"{cell(owner, 12)}  "
            f"{cell(val(item, 'severity'), 10)}  "
            f"{cell(val(item, 'cve'), 16)}  "
            f"{val(item, 'branch') or '—'}"
        )

    out.extend(["", "NEXT TASKS", "-" * (width + 68)])
    for item in rows:
        task = item["_next_task"]
        if not task:
            continue
        line = f"{item['_dir'].ljust(width)}  {task['display']}"
        if task["owner"]:
            line += f"   [owner: {task['owner']}]"
        if task["due"]:
            line += f"   [due: {task['due']}]"
        if task["blocked"]:
            line += "   [blocked]"
        out.append(line)

    counts = {}
    for item in items:
        stage = val(item, "stage", "new")
        counts[stage] = counts.get(stage, 0) + 1
    summary = "  ".join(f"{stage}={counts[stage]}" for stage in DISPLAY_STAGES if stage in counts)
    out.extend(["", f"{len(items)} items:  {summary}"])

    releases = sorted({val(item, "target_release") for item in items if val(item, "target_release")})
    for release in releases:
        pending = [
            item["_dir"] for item in items
            if val(item, "target_release") == release
            and val(item, "stage") not in TERMINAL
            and stage_rank(val(item, "stage")) < stage_rank("fix-ready")
        ]
        if pending:
            out.append(f"Release {release} is gated on {len(pending)} item(s) not fix-ready: "
                       f"{', '.join(pending)}")

    warnings = check(items, today)
    if warnings:
        out.extend(["", "CHECKS", "-" * (width + 68)])
        for name, message in warnings:
            out.append(f"{name.ljust(width)}  {message}")
    return "\n".join(out) + "\n"


def main():
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("triage_dir", help="e.g. triage-<year>/")
    parser.add_argument("--mine", help="mark items coordinated by this name with *")
    args = parser.parse_args()

    root = pathlib.Path(args.triage_dir)
    if not root.is_dir():
        sys.exit(f"not a directory: {root}")

    paths = sorted(root.glob("*/TRACKING.md"))
    legacy = sorted(root.glob("*/status.yml"))
    if paths and legacy:
        sys.exit("mixed TRACKING.md and status.yml sources found; finish the one-time "
                 "migration before using the board")
    if not paths:
        if legacy:
            sys.exit("legacy status.yml files found but no TRACKING.md files; run "
                     "scripts/migrate-status.py first")
        sys.exit(f"no */TRACKING.md found under {root} — copy assets/item-template/ "
                 "into each item directory")

    try:
        items = [load_tracking(path) for path in paths]
    except (OSError, ValueError) as error:
        sys.exit(str(error))
    sys.stdout.write(render(items, datetime.date.today(), args.mine))


if __name__ == "__main__":
    main()
