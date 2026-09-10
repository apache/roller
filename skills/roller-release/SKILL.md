---
name: roller-release
description: Prepare, build, sign, verify, stage, vote on and publish Apache Roller releases, including release website updates and announcement drafts.
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

# Apache Roller releases

Use the current checkout, project release decisions and
[ASF policy sources](references/asf-policy.md). Keep release execution notes,
personal signing setup and undisclosed security scope outside this skill.
Loading the skill does not authorize sending mail, pushing tags or publishing.

## Establish release inputs

Record the source checkout, target branch and exact commit; release version;
RC number and tag; release-manager ASF ID; signing-key fingerprint; distribution
working copies; and website checkout. Supply these from the actual release,
not defaults from an earlier session. Use a clean tree or isolated checkout.

Check build documentation and CI for the branch's supported JDK. Confirm Maven,
GnuPG, SVN and archive tools are available. Python 3 is used by the version helper.
Use [signing-key guidance](references/signing-key.md) before building a candidate.

## Prepare and verify

1. Agree scope and version, keeping undisclosed vulnerability details private.
2. Inspect version-bearing POMs and packaging scripts. Run
   `scripts/bump-version.sh <old> <new>` from this skill for a preview; use
   `--write` only to apply the reviewed scope. It does not commit. Update release
   notes and other appropriate references manually, preserving historical entries.
3. Commit the release inputs and record an RC tag at the exact source revision.
   Build from that revision so the embedded revision matches the candidate.
   Preserve candidate history; changed artifacts normally require a new candidate
   and vote. Do not silently replace artifacts already being voted on.
4. Build and test from the root using the branch's documented Maven invocation
   (normally `mvn -V -ntp clean install`). Build `assembly-release` separately
   with `mvn -f assembly-release/pom.xml package`; it is outside the root reactor.
5. Inspect the source and binary tar/zip archives. Sign and generate checksums
   using [signing instructions](references/signing-key.md).
6. Run `scripts/check-release.sh <artifact-dir>` from this skill. Its optional
   `--build` compiles the source archive with tests skipped; it does not replace
   the test suite, a full license review, or independent voter verification.
7. Stage and download the candidate using [distribution guidance](references/dist-svn.md).
   Verify signatures, checksums, contents and source build from the downloaded files.

## Vote and publish

Use [vote and announcement templates](references/vote-and-announce.md). Normally
allow at least 72 hours and require at least three positive binding votes and
more positive than negative binding votes. Check current policy for exceptions.
Record the result and the exact approved candidate.

Promote the approved artifact bytes; do not rebuild. If filenames lose the RC
suffix, detached signatures still verify those unchanged bytes, but checksum
filenames must be updated and verified. Point the final Git tag at the approved
RC commit. Inspect remote destination paths before distribution changes.

Update and publish the [website](references/website.md), verify public download
links and propagation, then announce. Prune superseded distributions only after
the website points to the new release and archive availability is confirmed.
Keep historical signing keys available for verification of old releases.

For security releases, coordinate advisory timing with the PMC and ASF Security;
use the companion `roller-security` skill when available. Public vote material
must not expose undisclosed case details. The operator upgrade must be available
when the advisory is published.

## References and helpers

- [ASF policy](references/asf-policy.md): authoritative sources and review boundaries.
- [Signing](references/signing-key.md): explicit signer selection and verification.
- [Distribution](references/dist-svn.md): staging, promotion and pruning.
- [Vote and announcement](references/vote-and-announce.md): reusable message drafts.
- [Website](references/website.md): source edits, rebuild and link verification.
- [Release tooling](references/atr.md): evaluating Apache Trusted Releases.

Resolve helper paths relative to this skill; when using the repository copy,
they are under `skills/roller-release/scripts/`. Helpers do not commit or publish.
