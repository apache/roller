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

# Roller developer skills

Optional developer tooling. Nothing here is part of the Roller build, the
source or binary distributions, or the runtime; the source assembly does not
include this directory. You can ignore it entirely and work on Roller normally.

Each subdirectory is a "skill": a `SKILL.md` describing a project procedure,
plus supporting reference notes, templates and helper scripts. They exist so
that recurring project chores are written down in one reviewable place instead
of living in one committer's head.

| Skill | Covers |
|---|---|
| [`roller-release`](roller-release/SKILL.md) | Preparing, building, signing, verifying, staging, voting on and publishing a release, plus the release website and announcement drafts. |
| [`roller-security`](roller-security/SKILL.md) | Security report triage, private case tracking, CVE preparation, reporter coordination and disclosure planning. |

## Reading them

`SKILL.md` and the files under `references/` are plain Markdown. Read them
directly for a written account of how a release is put together. No tooling is
required for this.

## Using them with an AI coding assistant

The layout follows the convention used by Claude Code and similar agent tools,
which discover skills under `.claude/skills/`. To make them available without
duplicating the files:

```sh
mkdir -p .claude/skills
ln -s ../../skills/roller-release .claude/skills/roller-release
```

`.claude/` is not tracked by this repository, so this is a local choice that
affects only your checkout. Copying the directories instead of symlinking works
equally well.

## Scope and limits

These skills describe procedure; they do not grant authority. The PMC owns
release decisions, and ASF policy governs what a release requires. Where a skill
and current ASF policy disagree, policy wins and the skill needs fixing.

The helper scripts under `scripts/` are deliberately conservative: they preview
by default, they do not commit, tag, push, sign on your behalf, or send mail,
and they are not a substitute for reading the diff and verifying a candidate
yourself. Verify current ASF policy from its authoritative sources rather than
trusting a summary here.

Corrections are welcome as ordinary pull requests.
