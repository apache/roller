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

# ASF process and Roller coordination

Use the [ASF committer security process](https://www.apache.org/security/committers.html)
as the authoritative workflow. Check the
[project security list directory](https://security.apache.org/projects/) for
current routing; do not infer a list from a project name.

If Roller has no dedicated security list, use its private PMC list for case
coordination and explicitly copy `security@apache.org`. Existing forwarded
threads help establish recipients, but inspect them before replying. Public
`dev@roller.apache.org` and `user@roller.apache.org` lists are for announcement
at the agreed disclosure point, not live case tracking.

The default process covers acknowledgement, investigation, acceptance or rejection,
CVE allocation, agreement on a fix, reporter review, commit, release and disclosure.
The default places reporter review before commit. Agree and document any project
variation with the PMC and ASF Security; do not turn one past release's sequence
into a permanent rule.

After or alongside release announcement, coordinate the advisory through the
portal. Check that it reaches the release-announcement audiences, reporter,
security contact and `oss-security@lists.openwall.com`. Then update project
security information and add the public announcement reference to the CVE.
Do not rewrite pushed Git commits afterward.

Keep case decisions and evidence private. A code change can reveal a vulnerability
even without security terminology. Review public changes in context; there is no
list of words that makes publication safe. If accidental disclosure occurs,
coordinate the response and timeline with ASF Security rather than assuming
editing a public message removes copies or notifications.
