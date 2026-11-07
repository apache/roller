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

# Authoritative release policy

Consult these sources when cutting a release; this skill is not a policy snapshot:

- [Release policy](https://www.apache.org/legal/release-policy.html): source artifacts,
  binding vote requirements, licensing and publication.
- [Distribution policy](https://infra.apache.org/release-distribution.html): official
  distribution channels, signatures, digests, download links and archive handling.
- [Release signing](https://infra.apache.org/release-signing.html): key and signature guidance.
- [Release publishing](https://infra.apache.org/release-publishing.html): operational steps.
- [Roller release process](https://cwiki.apache.org/confluence/spaces/ROLLER/pages/75651/Release+Process):
  project instructions; verify paths and commands against the current checkout.

A source release is the approved artifact; binary packages are convenience
artifacts. Release approval requires at least three positive binding votes and
more positive binding votes than negative ones. The normal review period is at
least 72 hours; consult the policy for exceptional expedited releases.

Independently validate signed source packages, compile and test them, and review
licensing. `check-release.sh` checks selected mechanical properties only. Presence
of LICENSE and NOTICE does not prove that bundled material is correctly licensed.
Inspect compiled artifacts, third-party content and exclusions in the actual
source archive. Do not treat a historical release as an exemption from policy.

Publish the artifacts that were approved. Rebuilding changes what voters reviewed
and requires renewed approval. Use the official channels and verify public links
before announcing availability. Keep per-release findings and corrective actions
in release notes or the private coordination workspace as appropriate, not here.
