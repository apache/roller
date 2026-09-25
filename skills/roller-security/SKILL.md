---
name: roller-security
description: Triage Apache Roller security reports, maintain private case tracking, prepare CVE records, coordinate fixes and reporter review, and prepare disclosure with a release.
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
supports those decisions; loading it does not authorize messages or publication.
Parts are designed to work with Obsidian (the triage-<year> directory is a vault),
but Obsidian is optional.

## Confidentiality and project practice

Keep reports, reproductions, CVE reservations, reporter identities, investigation
notes, and disclosure schedules in a private workspace. If using `triage-<year>/`
in a checkout, verify its local Git exclusion before adding case material.
Exclusion prevents accidental additions; it is not access control and does not
remove material from Git history. Never force-add private records.

Roller uses public code review for fixes. Follow the PMC's agreed workflow and
keep vulnerability characterization out of branch names, commits, PR text, tests,
and comments until disclosure. Neutral wording alone does not prove that a diff
is safe: review what the entire change reveals, including related cases. Resolve
uncertainty with the PMC and ASF Security. Do not assume that derivability from
public source makes an unannounced finding appropriate for publication.

Keep only generic procedures and synthetic templates in this skill. Do not add
live portal screenshots, case-derived examples, or release execution notes.

## Read the case before acting

1. Locate the private workspace and read its summary and the item's `TRACKING.md`.
2. Read the original report, reproduction evidence and `IMPLEMENTATION.md`.
3. Check the implementation branch, review state and release target against the
   actual repository. Do not infer that a message was sent from a draft file.
4. Reconcile `CVE_FORM.md` before using the portal or drafting an advisory.

For new cases, copy `assets/item-template/` into a private item directory.
`TRACKING.md` frontmatter stores facts; checkboxes store workflow state. The
terminal board reads those files directly:

```sh
python3 skills/roller-security/scripts/triage-status.py "$TRIAGE_DIR"
```

Set `TRIAGE_DIR` to the actual private workspace. Commands above run from the
Roller checkout root; when installed elsewhere, resolve scripts relative to this
skill. Plain Markdown editing works; Obsidian Tasks is optional. Read
[tracking conventions](references/obsidian-tasks-tracking.md) when creating or
migrating records. Keep completion dates and mark irrelevant tasks explicitly.

## Workflow

Read [ASF process and routing](references/asf-process.md) for policy sources and
project-specific decisions. Verify current policy when performing the workflow.

1. Acknowledge receipt without inventing a verdict or fix commitment.
2. Investigate reachability, actor privileges, configuration, affected versions,
   impact and duplicates. Record evidence separately from inference. Use the
   [codebase orientation](references/roller-codebase-map.md) to find entry points.
3. Record the PMC's acceptance, rejection or duplicate decision. Explain a
   rejection with verified reasons; re-evaluate each report independently.
4. Tell the reporter the accepted remedy and any agreed schedule. Distinguish
   planned work from completed work. Check existing credit preferences first;
   ask when unclear. Use [communication templates](references/comms-templates.md).
5. Assess [severity and CVSS](references/severity-and-cvss.md), reconcile the CVE
   worksheet, and request an ID from ASF Security through the
   [portal workflow](references/cve-portal.md).
6. Implement and review the fix under the agreed project workflow. Demonstrate
   that regression tests detect the reported behavior and pass with the fix;
   keep sensitive reproduction evidence private. Use the target branch's
   supported JDK and documented test commands, not a machine-specific SDK path.
7. Give the reporter the fix and draft advisory for comment with a reasonable
   deadline. Coordinate merge timing with the PMC; the ASF default places
   reporter review before commit. Record any agreed project variation.
8. Release the approved fix. The companion `roller-release` skill covers the
   mechanics when available; otherwise use the project's release documentation.
9. Coordinate disclosure with release availability, verify announcement recipients,
   update public security information, and add announcement references to the CVE.
   Do not rewrite pushed Git commits to add CVE IDs.

## Multiple reports

Give each case an owner and next task. Record shared code and duplicate
relationships in frontmatter. Coordinate merge order when fixes overlap, and
consider whether publishing one change reveals another case. Set release targets
explicitly; the board's release gates are reminders, not release authorization.

## Helpers and limits

- `scripts/triage-status.py <triage-dir> [--mine <name>]` derives stages, next
  tasks, blockers and consistency warnings without writing case data.
- `scripts/migrate-status.py <triage-dir> [--write]` previews migration from
  legacy `status.yml`; writes only when requested and retains legacy originals.
- `scripts/check-private.sh [--range <revision-range>] [--pr <number>]` checks
  common private paths and wording. Use explicit commit endpoints such as
  `master..HEAD` (or `master...HEAD`); a lone revision such as `HEAD` is rejected.
  Without `--range`, it checks the last 20 commit messages.
  It is a heuristic, not publication approval:
  manually inspect the full diff, filenames, screenshots, archives and PR text.
  Public security documentation can legitimately trigger its vocabulary checks.
