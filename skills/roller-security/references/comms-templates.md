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

# Communication templates

These are synthetic templates. Replace bracketed fields from the private case;
never guess recipients, findings, release commitments, IDs or credit. Drafts
stay in the case directory. Preparing a draft does not authorize sending it.
Use the routing in [asf-process.md](asf-process.md).

## Acknowledge

Subject: Re: [original report subject]

Thank you for reporting this. We have received your report and are investigating.
We will follow up when we have assessed it. Please keep the details private while
we coordinate the response.

[Name, on behalf of the Apache Roller project]

## Accept

Subject: Re: [original report subject]

We have accepted your report. We plan to address it by [concrete agreed remedy].
[Agreed target release or next update date, if known.] We will share the fix and
draft advisory for your review. Please let us know if this approach misses any
part of the behavior you reported.

[Allocated CVE ID, if already available.]
[Confirm existing credit preference, or ask if none was supplied.]

[Name, on behalf of the Apache Roller project]

## Reject or close as duplicate

Subject: Re: [original report subject]

We investigated [reported behavior] against [revision and configuration]. Our
conclusion is [decision], because [verified evidence and relevant preconditions].
[For a duplicate, explain the relationship through the private thread.]
Please let us know if we have misunderstood a prerequisite or missed evidence
that changes this assessment.

[Name, on behalf of the Apache Roller project]

## Request reporter review

Subject: Re: [original report subject]

The proposed fix is available at [agreed review location]. The draft advisory
is [attached or included through the agreed private channel]. Could you review
both by [date and timezone], particularly [specific validation question]?
Our proposed release/disclosure schedule is [agreed schedule]. Please tell us
if you need more time or identify a problem with the fix or advisory.

Credit is currently [agreed wording]. Please confirm any corrections.

[Name, on behalf of the Apache Roller project]

## Public advisory

Use the portal-generated advisory and review its fields before sending:

- Subject: [CVE identifier]: Apache Roller: [public title]
- Affected versions: [verified range]
- Severity: [PMC-approved rating]
- Description: [impact and prerequisites sufficient for operator assessment]
- Recommended action: [available fixed release and official download link]
- Credit: [approved wording]
- References: [public advisory and other appropriate public links]

Do not include private correspondence, internal record links, or reproduction
steps by default. Coordinate recipients and timing with the ASF process.
