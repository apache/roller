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

# ASF board reporting

Verify against the authoritative sources each quarter; this is a summary.

## Sources

- Reporting guide: <https://www.apache.org/foundation/board/reporting>
- Report wizard: <https://reporter.apache.org/wizard/?roller>
- Board agenda tool: `https://agenda.apache.org/meetings/<YYYY-MM-DD>#roller`
- Past Roller reports: <https://whimsy.apache.org/board/minutes/Roller.html>
- Board calendar: <https://svn.apache.org/repos/private/committers/board/calendar.txt>

## Schedule

Roller reports in February, May, August and November. The board usually meets
on the third Wednesday of the month; check the board calendar for the date.
Reports are due one week before the meeting; a late report is postponed to the
next meeting. The board-chair reminder mail, sent to the chair with private@ in
copy, gives both dates.

## Submission mechanisms

In order of preference:

1. The reporter wizard. It prefills the description and membership data and
   files the report to the agenda.
2. The agenda tool.
3. A commit to the dated agenda in the private board SVN repository.
4. Mail to board@apache.org with the subject `[REPORT] Roller`.

## Report sections

The wizard uses this template:

```
## Description:
## Project Status:
Current project status:
Issues for the board:
## Membership Data:
## Project Activity:
## Community Health:
```

The board wants the chair's own observations and assessment, not only raw
statistics. If a full report is not possible, report that; postponing by a
month is acceptable.

## Private text

Approved reports are published in the board minutes. Text in
`<private>...</private>` is kept out of the published minutes. Keep undisclosed
security details out of the public text.

## Feedback

Directors comment in the agenda tool during the review week. The secretary
mails the comments to the chair and private@ as "Board feedback on <date>
Roller report", and the wizard lists them under "Comments on recent board
reports". Comments that ask a question are answered in the next report unless
the comment says otherwise. Questions about comments go to board@.

Past comments that still apply:

- Include the dates of the latest release(s).
- Keep security issue details private.
