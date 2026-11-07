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

# Apache Roller — [year] security triage (batch summary)

> **PRIVATE — do not commit, do not discuss publicly.** `triage-[year]/` is
> to be verified Git-excluded before use. These are undisclosed reports under the ASF
> security process.

The queries below are optional views for Obsidian with the Tasks plugin.
Each item's `TRACKING.md` is authoritative: YAML frontmatter stores structured case
facts, and Tasks checkboxes store all workflow state. Do not copy stage or next
actions into prose here; they will drift.

## Open work

```tasks
not done
tags include #roller-security
group by folder
sort by priority
sort by due
```

## Blocked work

```tasks
not done
tags include #roller-security/blocked
group by folder
```

For a terminal-friendly board and consistency checks, run:

```sh
python3 skills/roller-security/scripts/triage-status.py triage-[year]/
```

This file is for what the board cannot hold: provenance, the reasoning behind
decisions, and the shape of the batch.

## Provenance

- Source: [who forwarded, from where, on what date]
- Reporter(s): [name, contact, any affiliation]
- Submitted: [date]  Verified against: [version @ commit]
- Dashboard: <https://dash.security.apache.org/project/roller>
- Notes on the forward itself: [what was supplied and what remains to be independently verified]

## Batch shape

Which items cluster, and why it matters for sequencing:

- **[cluster name]** — items NN, NN. Same underlying pattern; one fix branch or
  coordinated branches. [Merge order.]
- **[cluster name]** — items NN, NN. Shared configuration prerequisite; assess each item independently.
- **Duplicates / merges** — [item NN partly duplicates the closed report of
  DATE; CVE merge or split discussed with security@apache.org on DATE].

## Release plan

Target: [version]. Rationale for bundling or splitting. Anything whose severity
or reporter deadline argues for its own release, and the date that decision gets
revisited.

## Decision log

Reasoning that would otherwise be lost. Append, don't rewrite — when a reporter
or ASF Security asks in six months why something was closed, this is the answer.

| Date | Item | Decision | Reasoning |
|---|---|---|---|

## Open questions for the PMC

Things needing a `private@roller.apache.org` discussion rather than one person's
call — scope of the security model, whether a behaviour is intended, whether to
publish a Roller security page.
