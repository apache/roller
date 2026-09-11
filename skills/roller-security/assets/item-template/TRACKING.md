---
id: "NN"
slug: short-kebab-slug
title: >-
  One-line private description of the report.

# Structured case metadata belongs in this frontmatter. Workflow state does not:
# task checkboxes below are the sole source for stage, next action, blockers,
# action dates, and individual task assignments.
owner:                       # case coordinator; blank = unclaimed

# Provenance
reporter: "name <email>"
reported_on:                 # YYYY-MM-DD
forwarded_on:                # YYYY-MM-DD
thread:                      # private mail-thread link
verified_against:            # version and full commit ID

# Assessment facts
decision: pending            # pending | accept | reject | duplicate | wont-fix
actor:                       # anonymous | authenticated | edit_draft | weblog_admin | global_admin
preconditions: >-
  Unknown.
affected:
severity:                    # critical | important | moderate | low
cvss:                        # CVSS:3.1/AV:.../...
cvss_score:

# External identifiers and implementation artifacts
cve:                         # CVE-YYYY-NNNNN
branch:                      # name the change, never the flaw
fix_commit:
tests: []
target_release:
credit: >-
  Use the reporter's stated preference; ask if unclear.
announcement_url:

# Cross-item relationships
related: []
duplicates: []
shares_code_with: []
blocks: []
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

# Item NN — title

> **PRIVATE.** Undisclosed vulnerability under the ASF security process. Verify this
> workspace is excluded from Git before recording case details. Do not commit,
> paste into a public issue, or quote in a commit message.

## Workflow

These checkboxes are the authoritative status record. Complete them in Markdown
or Obsidian Tasks and preserve the `✅ YYYY-MM-DD` completion date. Keep the
`#roller-security/item-NN` and milestone tags intact.

When one path does not apply, complete its tasks and add `#not-applicable`;
never delete checklist rows. Add `#roller-security/blocked` to an open task and
put the blocker in that task's text. Add `#owner/name` to assign an individual
task when its owner differs from the case coordinator above.

### Triage

- [ ] Acknowledge receipt to the reporter #roller-security/item-NN #roller-security/stage/acknowledged
- [ ] Investigate: reachability, minimum actor, preconditions, duplicates — record in `IMPLEMENTATION.md` #roller-security/item-NN #roller-security/stage/investigating

Complete exactly one of the next two normally; complete the other with
`#not-applicable`. For a rejection, mark every later task `#not-applicable` too.

- [ ] Accept the report #roller-security/item-NN #roller-security/stage/accepted
- [ ] Reject, merge, or close the report and explain why to the reporter #roller-security/item-NN #roller-security/stage/rejected

### Fix

- [ ] Tell the reporter we accept it, intend to fix it, and how #roller-security/item-NN
- [ ] Rate ASF severity (and CVSS if needed), prepare `CVE_FORM.md`, request the CVE, and record its ID #roller-security/item-NN #roller-security/stage/cve-requested
- [ ] Prepare the fix and regression tests under the agreed review workflow #roller-security/item-NN
- [ ] Review the fix: supported-JDK tests and disclosure review #roller-security/item-NN #roller-security/stage/fix-ready
- [ ] Send the fix and draft advisory to the reporter for comment; agree a deadline #roller-security/item-NN
- [ ] Merge the reviewed fix under the agreed workflow #roller-security/item-NN #roller-security/stage/fix-committed

### Release and disclosure

- [ ] Release the fixed Roller version #roller-security/item-NN #roller-security/stage/released
- [ ] At the agreed disclosure point, advance the portal record and verify announcements #roller-security/item-NN #roller-security/stage/announced
- [ ] Add the announcement permalink to the CVE record, update the batch summary, and close #roller-security/item-NN #roller-security/stage/complete

## Activity notes

Use dated bullets for context that is not an actionable task. Do not restate
stage, next action, or blockers here.
