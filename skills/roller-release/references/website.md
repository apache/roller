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

# Release website update

Use the user's chosen checkout of the Apache Roller website repository:
<https://gitbox.apache.org/repos/asf/roller-website.git>.
Inspect its README and `.asf.yaml` to confirm the publishing branch and build
instructions; do not assume that a sibling checkout exists or is up to date.

The JBake layout uses `sources/src/main/jbake/content/` for source pages and
`content/` for rendered output. Find the download page and edit its source version,
download URLs and verification guidance. Build with the repository's documented
command (normally `mvn package` under `sources/`) and review both source and
rendered changes before publishing.

Use HTTPS and current ASF download infrastructure. Fetch signatures, checksums
and KEYS from the official distribution site rather than an arbitrary mirror.
Verify versioned archive links and all verification sidecars after propagation.
A redirect response alone is not evidence that its destination archive exists.

Publish the website only when the approved release artifacts are available.
Verify the live page and links before announcement, then prune superseded
releases within the agreed scope. Keep release-specific website defects in the
release work record, not as permanent instructions in this reference.
