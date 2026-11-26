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

"""Tier 0 deterministic tools for Apache Roller security triage.

All wrappers invoke existing repository scripts and standard tooling read-only
(except ``scaffold_case_dir``, which initializes private case working files
outside Git tracking) and honor ``dry_run=True`` safely.
"""

from __future__ import annotations

import shutil
import subprocess
import urllib.error
import urllib.request
from pathlib import Path

_SKILL_DIR = Path(__file__).resolve().parents[2]
_CHECK_PRIVATE_REL = Path("scripts") / "check-private.sh"
_SKILL_REFS_REL = Path("references")
_SKILL_TEMPLATES_REL = Path("assets") / "item-template"
_SCAFFOLD_TEMPLATES: tuple[str, ...] = ("IMPLEMENTATION.md", "CVE_FORM.md")


def _resolve_skill_root(repo_dir: str) -> Path:
    """Resolve the skill root directly from this package, falling back to repo_dir/skills/roller-security."""
    if (_SKILL_DIR / _CHECK_PRIVATE_REL).is_file():
        return _SKILL_DIR
    return (Path(repo_dir).resolve() / "skills" / "roller-security").resolve()


def _run_cmd(
    cmd: list[str],
    *,
    cwd: Path,
    timeout: int | None = None,
) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        cmd,
        cwd=str(cwd),
        capture_output=True,
        text=True,
        check=False,
        timeout=timeout,
    )


def _format_output(proc: subprocess.CompletedProcess[str]) -> str:
    parts = [part.strip() for part in (proc.stdout, proc.stderr) if part and part.strip()]
    return "\n".join(parts)


def guard_private_workspace(
    repo_dir: str,
    triage_dir: str,
    *,
    dry_run: bool = False,
) -> tuple[bool, list[str]]:
    """Verify that ``triage_dir`` cannot leak into Git and ``check-private.sh`` exists.

    Checks:
    1. ``repo_dir/skills/roller-security/scripts/check-private.sh`` exists.
    2. ``triage_dir`` is either completely outside ``repo_dir`` OR excluded by Git
       (``git check-ignore -q``).
    3. Zero files under ``triage_dir`` are tracked (``git ls-files``) or staged
       (``git diff --cached --name-only``).
    """
    del dry_run  # All workspace guard checks are read-only.

    repo_path = Path(repo_dir).resolve()
    triage_path = (repo_path / triage_dir).resolve()
    report: list[str] = []
    ok = True

    if not repo_path.is_dir():
        return False, [f"FAIL: repo_dir does not exist: {repo_path}"]

    skill_root = _resolve_skill_root(repo_dir)
    check_script = skill_root / _CHECK_PRIVATE_REL
    if check_script.is_file():
        report.append(f"ok: found {check_script}")
    else:
        ok = False
        report.append(f"FAIL: missing required script {check_script}")

    if triage_path == repo_path:
        ok = False
        report.append("FAIL: triage_dir must not be the repository root itself")
        return ok, report

    if not triage_path.is_relative_to(repo_path):
        report.append(f"ok: triage_dir ({triage_path}) is outside repo_dir ({repo_path})")
        return ok, report

    rel = triage_path.relative_to(repo_path).as_posix()

    # Check git exclusion (test both rel and rel/ so directory-only rules match
    # even before triage_dir is created on disk).
    ign_plain = _run_cmd(["git", "check-ignore", "-q", "--", rel], cwd=repo_path)
    ign_dir = _run_cmd(["git", "check-ignore", "-q", "--", f"{rel}/"], cwd=repo_path)
    if ign_plain.returncode == 0 or ign_dir.returncode == 0:
        report.append(f"ok: triage_dir '{rel}' is excluded by Git")
    elif ign_plain.returncode == 128 or ign_dir.returncode == 128:
        ok = False
        err = _format_output(ign_plain) or _format_output(ign_dir)
        report.append(f"FAIL: git check-ignore failed for '{rel}': {err}")
    else:
        ok = False
        report.append(
            f"FAIL: triage_dir '{rel}' is inside repo_dir and NOT excluded by Git "
            f"(add '/{rel}/' to .git/info/exclude)"
        )

    tracked = _run_cmd(["git", "ls-files", "--", rel], cwd=repo_path)
    if tracked.returncode != 0:
        ok = False
        report.append(f"FAIL: git ls-files failed: {_format_output(tracked)}")
    elif tracked.stdout.strip():
        ok = False
        files = tracked.stdout.strip().splitlines()
        report.append(
            f"FAIL: {len(files)} file(s) under '{rel}' are tracked by Git: "
            + ", ".join(files[:5])
        )
    else:
        report.append(f"ok: zero files tracked under '{rel}'")

    staged = _run_cmd(
        ["git", "diff", "--cached", "--name-only", "--", rel],
        cwd=repo_path,
    )
    if staged.returncode != 0:
        ok = False
        report.append(f"FAIL: git diff --cached failed: {_format_output(staged)}")
    elif staged.stdout.strip():
        ok = False
        files = staged.stdout.strip().splitlines()
        report.append(
            f"FAIL: {len(files)} file(s) under '{rel}' are staged in Git: "
            + ", ".join(files[:5])
        )
    else:
        report.append(f"ok: zero files staged under '{rel}'")

    return ok, report


def run_check_private(
    repo_dir: str,
    *,
    rev_range: str = "",
    pr: int | None = None,
    dry_run: bool = False,
) -> tuple[bool, str]:
    """Run ``skills/roller-security/scripts/check-private.sh`` read-only."""
    repo_path = Path(repo_dir).resolve()
    script_path = _resolve_skill_root(repo_dir) / _CHECK_PRIVATE_REL
    if not script_path.is_file():
        return False, f"check-private.sh not found at {script_path}"

    cmd = ["bash", str(script_path)]
    if rev_range:
        cmd.extend(["--range", rev_range])
    if pr is not None:
        cmd.extend(["--pr", str(pr)])

    if dry_run and (pr is not None or rev_range):
        return True, f"[dry-run] Would run: {' '.join(cmd)}"

    proc = _run_cmd(cmd, cwd=repo_path)
    output = _format_output(proc)
    return proc.returncode == 0, output


def verify_fix_commit_ancestor(
    repo_dir: str,
    fix_commit: str,
    release_tag: str,
    *,
    dry_run: bool = False,
) -> tuple[bool, str]:
    """Verify ``fix_commit`` is an ancestor of ``release_tag`` via ``git merge-base``."""
    if not fix_commit or not release_tag:
        return False, "Both fix_commit and release_tag are required."

    cmd = ["git", "merge-base", "--is-ancestor", fix_commit, release_tag]
    if dry_run:
        return True, f"[dry-run] Would run: {' '.join(cmd)}"

    repo_path = Path(repo_dir).resolve()
    proc = _run_cmd(cmd, cwd=repo_path)
    if proc.returncode == 0:
        return True, f"Verified: {fix_commit} is an ancestor of {release_tag}."
    if proc.returncode == 1:
        return False, f"NOT an ancestor: {fix_commit} is not contained in {release_tag}."
    return False, f"git merge-base error (exit {proc.returncode}): {_format_output(proc)}"


def run_maven_tests(
    repo_dir: str,
    test_classes: list[str],
    *,
    dry_run: bool = False,
) -> tuple[bool, str]:
    """Run ``mvn -V -ntp test`` (optionally scoped to ``test_classes``)."""
    cmd = ["mvn", "-V", "-ntp", "test"]
    cleaned = [c.strip() for c in test_classes if c.strip()]
    if cleaned:
        cmd.append(f"-Dtest={','.join(cleaned)}")

    if dry_run:
        return True, f"[dry-run] Would run: {' '.join(cmd)}"

    repo_path = Path(repo_dir).resolve()
    try:
        proc = _run_cmd(cmd, cwd=repo_path)
    except FileNotFoundError:
        return False, "mvn executable not found on PATH."
    return proc.returncode == 0, _format_output(proc)


def verify_archive_urls(
    urls: list[str],
    *,
    dry_run: bool = False,
) -> tuple[bool, str]:
    """Verify each ASF mailing list archive URL returns HTTP 200 (following redirects)."""
    cleaned = [u.strip() for u in urls if u.strip()]
    if not cleaned:
        return False, "No archive URLs provided to verify."

    if dry_run:
        return (
            True,
            f"[dry-run] Would verify HTTP 200 on {len(cleaned)} archive URL(s): "
            + ", ".join(cleaned),
        )

    lines: list[str] = []
    all_ok = True
    headers = {"User-Agent": "roller-security-graph/0.1"}

    for url in cleaned:
        status: int | None = None
        err_msg = ""
        for method in ("HEAD", "GET"):
            req = urllib.request.Request(url, headers=headers, method=method)
            try:
                with urllib.request.urlopen(req, timeout=15) as resp:
                    status = resp.status
                break
            except urllib.error.HTTPError as exc:
                if method == "HEAD" and exc.code in (405, 501):
                    continue
                status = exc.code
                err_msg = str(exc.reason)
                break
            except urllib.error.URLError as exc:
                err_msg = str(exc.reason)
                break

        if status == 200:
            lines.append(f"ok (200): {url}")
        else:
            all_ok = False
            detail = f"HTTP {status}" if status is not None else "connection error"
            if err_msg:
                detail = f"{detail} ({err_msg})"
            lines.append(f"FAIL ({detail}): {url}")

    return all_ok, "\n".join(lines)


def read_skill_ref(repo_dir: str, filename: str) -> str:
    """Read a file from the skill's ``references/`` or ``assets/item-template/``."""
    skill_root = _resolve_skill_root(repo_dir)

    candidates = [
        (skill_root / _SKILL_REFS_REL / filename).resolve(),
        (skill_root / _SKILL_TEMPLATES_REL / filename).resolve(),
    ]
    for path in candidates:
        if not path.is_relative_to(skill_root):
            raise ValueError(f"Invalid skill reference filename: {filename!r}")
        if path.is_file():
            return path.read_text(encoding="utf-8")

    raise FileNotFoundError(
        f"Skill reference {filename!r} not found in {_SKILL_REFS_REL.as_posix()} "
        f"or {_SKILL_TEMPLATES_REL.as_posix()} under {skill_root}"
    )


def scaffold_case_dir(
    repo_dir: str,
    case_dir: str | Path,
    *,
    dry_run: bool = False,
) -> list[str]:
    """Create ``case_dir`` and copy ``IMPLEMENTATION.md`` and ``CVE_FORM.md`` if absent.

    Intentionally omits ``TRACKING.md`` because SQLite + ``CASE.md`` replace it.
    Returns the list of created file paths (or paths that would be created in dry-run).
    """
    template_dir = _resolve_skill_root(repo_dir) / _SKILL_TEMPLATES_REL
    target_dir = Path(case_dir)

    created: list[str] = []
    if not dry_run:
        target_dir.mkdir(parents=True, exist_ok=True)

    for name in _SCAFFOLD_TEMPLATES:
        src = template_dir / name
        if not src.is_file():
            raise FileNotFoundError(f"Missing template file: {src}")
        dest = target_dir / name
        if not dest.exists():
            if not dry_run:
                shutil.copyfile(src, dest)
            created.append(str(dest))

    return created
