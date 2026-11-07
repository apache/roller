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

# Obsidian Tasks tracking for security reports

Keep case files in a private `triage-<year>/` workspace. Obsidian with the Tasks
plugin is optional; plain Markdown editing and the terminal board also work.
Every vulnerability directory contains one `TRACKING.md` and one
`CVE_FORM.md`; copy both from `assets/item-template/` when creating a report.
`CVE_FORM.md` is a portal-entry worksheet and does not own stage, next action or
other workflow state; see `references/cve-portal.md`.

## One file, two responsibilities

Do not keep `status.yml` beside `TRACKING.md`. They would contain overlapping
mutable state and eventually disagree.

Use the YAML frontmatter in `TRACKING.md` only for structured facts that Tasks is
not designed to model: report identity and provenance, the assessment outcome,
CVE identifier, implementation artifacts, release target, credit, and
cross-item relationships.

Use Tasks checkboxes as the sole source for:

- current stage (derived from completed `#roller-security/stage/...` tasks);
- next action (the first incomplete workflow task);
- blockers (`#roller-security/blocked` on an incomplete task);
- action dates (`✅ YYYY-MM-DD` and `📅 YYYY-MM-DD`);
- individual assignments (`#owner/name` when different from frontmatter
  `owner`, which names the overall case coordinator).

Never add `stage`, `next_action`, or `blocked_on` to frontmatter. Do not write a
parallel status sentence in `SUMMARY.md` or `IMPLEMENTATION.md`.

## Keep the checklist short

The checklist is a workflow tracker, not a procedure manual. One row per action
somebody actually takes and can tick. Detail about
*how* to do a step belongs in `SKILL.md` or the reference files; detail about
what was found belongs in `IMPLEMENTATION.md` and the activity notes.

A checklist that enumerates every sub-step (rate severity, build the CVSS
vector, write the test, watch it fail, watch it pass, re-fail it, run the
suite…) stops being read, and half-ticked sub-steps make the derived stage
meaningless. Fold them into the one row whose completion they define: "Review
the fix: supported-JDK tests and disclosure review" is
one tickable action, and if it isn't done, it isn't ticked.

Add a row only when it represents a decision or a hand-off that can be
independently late — sending a message, merging, releasing. Resist adding one
because a step is important; importance is what the skill text is for.

## Task conventions

- Preserve `#roller-security/item-NN` on every workflow task. It makes vault-wide
  Tasks queries reliable even if a note moves.
- Preserve exactly one `#roller-security/stage/<stage>` tag on each milestone
  task. `new` means no milestone has been completed.
- Record `✅ YYYY-MM-DD` when completing tasks, manually or through the
  Tasks plugin.
- Put a real response deadline on the reporter-review task with
  `📅 YYYY-MM-DD`; do not keep a separate deadline field in YAML.
- To block work, add `#roller-security/blocked` and describe what is required in
  the task text. Remove the tag when unblocked.
- For alternative or irrelevant tasks, check the task and add
  `#not-applicable`. Do not delete it. The board ignores such tasks when deriving
  stage, but retaining them proves the step was considered.
- Complete exactly one of the `accepted` and `rejected` milestones normally.
  Mark the other `#not-applicable`. For a rejected item, mark all downstream
  tasks `#not-applicable` so they disappear from open-work queries.
- Keep reasoning and red/green evidence in `IMPLEMENTATION.md`; a task should
  say what must be done, not reproduce sensitive technical detail.

## Stage derivation

The terminal board derives the stage from the furthest completed milestone
that is not tagged `#not-applicable`:

`new → acknowledged → investigating → accepted → cve-requested → fix-ready → fix-committed → released → announced → complete`

`rejected` is an alternative terminal milestone after `investigating`.
The current action is the first incomplete, applicable task in file order. This
makes the checklist order operational, not decorative.

## Obsidian dashboard queries

Put vault-wide queries in `SUMMARY.md`:

    ```tasks
    not done
    tags include #roller-security
    group by folder
    sort by priority
    sort by due
    ```

For blocked work:

    ```tasks
    not done
    tags include #roller-security/blocked
    group by folder
    ```

The terminal script is a validator and compact report, not a second tracker:

    python3 skills/roller-security/scripts/triage-status.py "$TRIAGE_DIR"

It reads frontmatter and tasks directly and never writes status.

## Migrating legacy `status.yml`

Set `TRIAGE_DIR` to the private workspace. Run from the Roller checkout root
(or resolve script paths relative to the installed skill). Run the migration
tool once for an existing workspace:

    python3 skills/roller-security/scripts/migrate-status.py "$TRIAGE_DIR"
    python3 skills/roller-security/scripts/migrate-status.py "$TRIAGE_DIR" --write

The first command is a dry run. `--write` creates each `TRACKING.md` and renames
the source to `status.yml.legacy`; it does not delete the source data. Review the
generated tasks and frontmatter, then delete the legacy backups when satisfied.
The migration completes the imported current-stage milestone and any milestones
with explicit legacy dates; it deliberately leaves other checklist rows open
rather than inventing evidence that they were done. Reconcile those open rows in
Obsidian. Never edit a `.legacy` file or use it to answer status questions.
