---
name: roller-board-report
description: Prepare, review and submit the Apache Roller quarterly report to the ASF board through reporter.apache.org, and handle board feedback on earlier reports.
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

# Apache Roller board reports

The PMC reports to the ASF board in February, May, August and November. The
chair owns the report. This skill collects facts and drafts text; loading it
does not authorize sending mail or submitting the report.
See [ASF reporting](references/asf-reporting.md) for policy and mechanics.

## Establish inputs

Record the report due date, the board meeting date and the reporting window
(from the day after the previous meeting to now). Take the dates from the
board-chair reminder mail or the wizard's Base Data, not from memory.

## Read what came before

1. Read the previous report in the
   [Roller board minutes](https://whimsy.apache.org/board/minutes/Roller.html).
   The most recent meeting may not be there yet; use the agenda tool for it.
2. Read board comments: the wizard's "Comments on recent board reports" and
   any "Board feedback on <date> Roller report" mail. Answer every comment that
   asks a question in this report. Take other comments into account.

## Gather facts

Use [data sources](references/data-sources.md). Start with
`scripts/quarter-activity.sh <since> [<until>]` from this skill, run from the
Roller checkout after `git fetch origin --tags`. Then check the wizard's
Membership Data, the reporter statistics page (`https://reporter.apache.org/wizard/statistics?roller`) and mailing list volume.
Record the date of the latest release; the board has asked for it.

## Draft

Use the wizard's sections: Description, Project Status (with "Issues for the
board"), Membership Data, Project Activity and Community Health. Copy the
wizard's Membership Data text unchanged.

- Give the chair's assessment of project health, not only statistics.
- Give the latest release and its date, and the next planned release.
- Name new contributors and say whether the PMC is considering them for
  committer or PMC membership. State only what the PMC has actually done.
- Say "None" under "Issues for the board" unless there is one.
- Keep the tone plain and factual. No jokes or filler to reach a length.

Write the draft to a local file outside the repository and give it to the
chair to edit. Sharing it on private@ for comment is optional; ask first.

## Keep security details out

Board reports become public minutes. Do not name undisclosed vulnerabilities,
CVE IDs before publication, affected features, severities, reporters or
disclosure dates in the public text. Public text may say that reports were
received and handled through the ASF security process and which release will
carry the fixes. Put anything the board needs beyond that inside
`<private>...</private>`, which the agenda tool keeps out of published minutes;
confirm the convention in the current agenda tool before relying on it. If in
doubt, leave it out and offer details on request. Use the companion
`roller-security` skill for case status, and take counts only.

## Submit

Submit only after the chair approves the final text in chat.

1. Open `https://reporter.apache.org/wizard/?roller` in the chair's browser
   (ASF login required). Confirm the Base Data shows the expected meeting.
2. Enter each section, then go to "Review and Publish". Show the chair the
   rendered text and confirm it matches the approved draft.
3. Publish only on an explicit yes. Then check that Base Data shows "Filed to
   agenda", or check the Roller entry in the board agenda for that meeting.

If the wizard fails, use the agenda tool, or mail board@apache.org with the
subject `[REPORT] Roller`, after the chair agrees. If a report cannot be ready
on time, tell the board that instead of skipping it.

## After the meeting

Watch for "Board feedback on <date> Roller report". Note each comment for the
next report and reply on board@ only when the chair asks.
