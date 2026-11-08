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

# Data sources for the report

| Fact | Source |
|---|---|
| Meeting date, due date, filed status | Wizard Base Data; board-chair reminder mail |
| Committer and PMC counts, last additions | Wizard Membership Data (copy as is) |
| Commits, contributors, merged PRs, tags | `scripts/quarter-activity.sh <since>` |
| Mailing list volume | <https://lists.apache.org/list.html?dev@roller.apache.org> and the reporter statistics page <https://reporter.apache.org/wizard/statistics?roller> (it may count automated GitHub mail; check the list archive) |
| Release dates | Announcement mails on announce@ or user@; the downloads page; Git tags as a fallback (a tag date is the RC date, not the release date) |
| Previous report | <https://whimsy.apache.org/board/minutes/Roller.html> |
| Board comments | Wizard "Comments on recent board reports"; "Board feedback on <date> Roller report" mail |
| Security activity | The private triage workspace through the `roller-security` skill. Counts and target release only |

## Notes

- Roller develops on two lines: `master` and the maintenance branch
  `roller-6.1.x`. Count both, and do not double-count merges between them.
- A first-time author in the window is a contributor to name in Community
  Health. Check whether they are already a committer before calling them new.
- Reporter statistics can lag or miss GitHub-only activity. Prefer Git and
  GitHub for code activity.
