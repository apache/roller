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

# Severity and CVSS

Use the [ASF severity guidance](https://www.apache.org/security/impact_levels.html)
for the project's qualitative rating. Use the relevant
[FIRST CVSS specification](https://www.first.org/cvss/) when a numerical score is
needed. State the CVSS version explicitly; metrics from different versions are
not interchangeable.

Assess the demonstrated scenario: reachable interfaces, required privileges,
user interaction, configuration prerequisites and confidentiality, integrity and
availability impact. A non-default configuration is a condition to describe,
not automatic grounds for rejection or an arbitrary score reduction.

Keep the rationale with the private case. Separate verified facts from plausible
but untested outcomes. Do not copy a score from a similar report or infer severity
from a vulnerability class alone. Have the PMC review the rating and vector, and
check that the advisory's prerequisites agree with them.

Enter the agreed ASF rating in the portal's supported textual severity field.
If also entering CVSS, verify its generated version, vector and score against
the selected specification/calculator. Leave unresolved worksheet fields blank
rather than providing a severity or vector that looks like a settled assessment.
