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

# Distribution staging and promotion

Confirm the current project layout before preparing paths:

```sh
svn ls https://dist.apache.org/repos/dist/dev/roller/
svn ls https://dist.apache.org/repos/dist/release/roller/
```

The conventional layout is `roller-<major.minor>/v<version>/` below each project
root. RC suffixes are typically in archive filenames. Verify this convention for
the target branch; do not infer paths from an older wiki example.

Use separate user-selected working directories for dev and release SVN checkouts.
Stage only the intended candidate archives and signature/checksum sidecars. Review
`svn status` and `svn diff` before committing. Download staged files to a fresh
directory and verify them before starting the vote. Development staging is publicly
accessible even though it is not an official release; do not upload private notes.

## Approved-candidate promotion

Record the passed vote, source SVN revision, candidate filenames and destination.
Use an SVN working copy or a single reviewed repository transaction to promote
only that candidate. Preserve archive bytes and detached signatures. If removing
an RC suffix from filenames, update the filename references in checksum sidecars
and verify each digest against the unchanged archive.

Do not blindly promote everything in a version directory: it may contain cancelled
candidates or unrelated files. Verify the final inventory, signature fingerprints
and checksum checks after promotion. Never rebuild to remove an RC suffix.

Wait for distribution propagation and check the public download URLs, not merely
SVN success. Follow the current
[release publishing guidance](https://infra.apache.org/release-publishing.html)
for timing. Update the website and verify its links before announcing.

## Cleanup

Remove cancelled candidate files only within the agreed cleanup scope, checking
exact filenames first. For superseded official releases, confirm archive
availability and update website links before pruning. Preserve the KEYS history.
A successful delete does not remove copies already downloaded from public staging.
