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

# Vote and announcement drafts

Replace bracketed values from the actual release record. Verify URLs, tag commit,
artifact checksums and signer fingerprint. Do not send messages merely because a
template has been filled. Keep undisclosed security characterization out of public
vote threads and release notes before coordinated announcement.

## Vote

Subject: [VOTE] Release Apache Roller [version] ([candidate])

Please review and vote on Apache Roller [version], candidate [candidate].

Source and convenience binary artifacts: [staging URL]
Source tag: [tag URL and commit]
KEYS: https://downloads.apache.org/roller/KEYS
Signing fingerprint: [release manager's verified fingerprint]
Release notes: [appropriate public release notes URL]

The vote will remain open until at least [date, time and timezone], allowing
at least 72 hours for review under the normal process.

[ ] +1 Release this candidate
[ ] +0 No opinion
[ ] -1 Do not release, because [reason]

[Release manager name]

## Result

Subject: [RESULT][VOTE] Release Apache Roller [version] ([candidate])

The vote [passed/did not pass].

Binding votes: [positive, neutral and negative counts; voters]
Non-binding votes: [counts; voters]
Vote thread: [public permalink]
Approved candidate, if passed: [exact tag, commit and artifact location]

[Next step consistent with the result]

[Release manager name]

## Release announcement

Subject: [ANNOUNCE] Apache Roller [version] released

The Apache Roller project is pleased to announce Apache Roller [version].

[Concise description of the release and publicly releasable changes]

Downloads: https://roller.apache.org/downloads/downloads.html
Release notes: [public URL]
[Upgrade guidance and coordinated advisory links, when ready for disclosure]

[Release manager name], on behalf of the Apache Roller project

Check the current ASF and project announcement guidance for recipients and
formatting. Tally binding votes by PMC membership; do not count every +1 as binding.
