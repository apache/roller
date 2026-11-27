---
name: roller-security
description: Triage Apache Roller security reports, maintain private SQLite-backed case tracking via this skill's LangGraph CLI, prepare CVE records, coordinate fixes and reporter review, and prepare disclosure with a release.
---

<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Apache Roller security response

Use this workflow for vulnerability response, not ordinary bug fixes. The PMC
owns acceptance, severity, release scope, and disclosure decisions. This skill
supports those decisions; loading it does not authorize sending mail, opening
PRs, or publishing advisories.

Case tracking and workflow state are managed by this skill's local LangGraph CLI
(`skills/roller-security/`) backed by SQLite (`<triage-dir>/cases.db`), with
generated read-only Markdown views (`CASE.md` and `SUMMARY_BOARD.md`). No
external note app or plugin is required.

## Confidentiality and project practice

- Keep reports, reproductions, CVE reservations, reporter identities,
  investigation notes, `cases.db`, and disclosure schedules in a private
  workspace (`TRIAGE_DIR`, e.g. `triage-<year>/`). Verify its local Git
  exclusion in `.git/info/exclude` before adding case material. Never
  force-add private records.
- Roller uses public code review for fixes. Keep vulnerability characterization
  out of branch names, commits, PR text, tests, and comments until disclosure.
  Neutral wording alone does not prove a diff is safe: review what the entire
  change reveals, including related cases (`shares_code_with`, `related`).
- When preparing public text (PR bodies, release notes, announcements, website
  updates) before disclosure, pass only the sanitized `PublicBrief` (permitted
  facts and tone constraints)—never reveal what is being withheld or why.
- Keep only generic procedures and synthetic templates in this skill. Do not
  add live portal screenshots, case-derived examples, or release execution notes.

## Operating the LangGraph triage runner

Run commands from `skills/roller-security/` with `uv run roller-security`:

```sh
# View the current board, blockers, release gates, and consistency checks
uv run roller-security board --triage-dir ../../triage-<year>

# Start a new security case (creates <triage-dir>/cases.db if needed)
uv run roller-security start \
  --repo ../.. \
  --triage-dir ../../triage-<year> \
  --case-id 01 --slug short-neutral-slug --owner <asf-id> \
  --report ../../triage-<year>/raw-report.eml

# Inspect or resume a paused human gate
uv run roller-security status <case-key> --triage-dir ../../triage-<year>
uv run roller-security resume <case-key> --triage-dir ../../triage-<year> --json '<payload>'

# Export leak-safe PublicBrief JSON for release notes (roller-release skill)
uv run roller-security export-briefs --target-release 7.0.0 --triage-dir ../../triage-<year>
```

By default the private-context steps (the raw report, investigation, severity
and CVE drafting, reporter emails) go to the configured cloud models. Use them
only under an account whose data-retention terms the PMC accepts for undisclosed
reports. Pass `--local-llm` to keep every private-context call on local Ollama.
`--dry-run --mock-llm` tests a workflow offline; it still records the case in
`cases.db`.

Cases opened before this runner are tracked in their `TRACKING.md` files and
are not in `cases.db`, so `board` does not show them. Keep running
`python3 scripts/triage-status.py ../../triage-<year>` for those cases until each
one is closed or restarted in the runner.

## Workflow summary

Read [ASF process and routing](references/asf-process.md) for policy sources and
project-specific decisions. Each stage is enforced by this skill's
`roller-security` CLI with explicit human approval gates. A gate asks again when
an answer is incomplete, and a failed check returns to the gate before it:

1. **Workspace guard & acknowledgement:** Verify Git exclusion, scaffold
   `IMPLEMENTATION.md` and `CVE_FORM.md` from `assets/item-template/`, and
   acknowledge receipt using [communication templates](references/comms-templates.md)
   without inventing a verdict or fix commitment.
2. **Investigation:** Assess reachability, minimum actor, preconditions,
   affected versions, impact, and duplicates using
   [codebase orientation](references/roller-codebase-map.md). Record verified
   evidence separately from inference in `IMPLEMENTATION.md`.
3. **PMC verdict:** Record acceptance, rejection, or duplicate decision. For
   rejections, explain with verified reasons and close with downstream
   milestones marked `n/a`.
4. **Severity & CVE request:** Rate [severity and CVSS](references/severity-and-cvss.md),
   reconcile `CVE_FORM.md`, and request an ID through the
   [portal workflow](references/cve-portal.md).
5. **Fix, disclosure lint & reporter review:** Run supported-JDK tests,
   `scripts/check-private.sh --range <base>..<branch>` (base `roller-6.1.x` or
   `master`; and `--pr <num>`), semantic
   disclosure + cross-case overlap review, and send the fix + draft advisory to
   the reporter with a reasonable deadline. Record the landed merge/squash
   commit as `fix_commit` and verify it is an ancestor of the release tag.
6. **Release & disclosure:** Hand off sanitized `PublicBrief`s
   (`export-briefs`) to the release work in the companion `roller-release`
   skill. Once the fixed release is live,
   advance the CVE portal record, verify delivery in every list archive, notify
   reporters separately with their advisory links, and close the case. Never
   rewrite pushed Git commits to add CVE IDs.

## Rules the CLI does not enforce

- Search the correspondence before stating what a reporter said or did not say.
  Do not infer that a message was sent from a draft, or that none arrived
  because the case record lacks one.
- Do not assume that derivability from public source makes an unannounced
  finding appropriate for publication. Resolve doubt with the PMC and ASF Security.
- Use the target branch's supported JDK and documented build commands, not a
  machine-specific SDK path.
- Reconcile `CVE_FORM.md` (including the generated draft the CLI appends) before
  using the CVE portal or drafting an advisory.

- Keep the report in the case folder as `original-report.md`. The runner saves
  the text it was given; for a case recorded by hand, save the text or a summary
  with the mail-thread link. Keep the case folder 0700 and its files 0600. The
  runner tightens the folders it writes to; check folders created by hand.
- A case folder grows with the workflow: `original-report.md`, `IMPLEMENTATION.md`
  and the case record at triage; `REPRODUCTION.md` once it is reproduced;
  `CVE_FORM.md` after acceptance; reporter and advisory drafts when requested.

## References and helpers

- [ASF process](references/asf-process.md): policy sources and project decisions.
- [Codebase map](references/roller-codebase-map.md): Roller entry points and auth boundaries.
- [Communication templates](references/comms-templates.md): reporter and PMC message drafts.
- [Severity and CVSS](references/severity-and-cvss.md): ASF severity ratings and CVSS guidance.
- [CVE portal](references/cve-portal.md): ASF Security portal entry workflow.
- `scripts/triage-status.py <triage-dir>`: read-only board for cases still tracked in
  `TRACKING.md`. Remove it when no such cases are open.
- `scripts/check-private.sh [--range <base..tip>] [--pr <number>]`: disclosure lint for
  private paths, branch names, commit messages, staged diffs, and PR text.
