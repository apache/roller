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

# Item NN — [title]

> **PRIVATE.** Undisclosed vulnerability under the ASF security process. This
> directory must be verified Git-excluded before recording case details. Do not commit, do not
> paste into a public issue, do not quote in a commit message.

Case metadata and workflow: [[TRACKING]]

Branch base: `master` @ `[sha]`

## Verification

What was checked, and how. The four things that decide severity:

- **Reachability** — entry point, and the filter/interceptor path that reaches
  it. Name files and lines.
- **Actor** — anonymous / any authenticated user / `EDIT_DRAFT` / weblog admin /
  global admin. Justify it; this is the biggest severity lever.
- **Preconditions** — default config or not. Quote the property and its default
  from `roller.properties`.
- **Duplicate check** — which other items and previously closed reports were
  checked, and the outcome.

State plainly what was demonstrated versus inferred. Independently verify
forwarded summaries and record unresolved assumptions.

## Approach

The fix pattern, and why this one rather than the alternatives. If the codebase
already contains a safe version of this pattern elsewhere, name it — matching an
existing idiom is easier to review and less likely to regress.

## Changes

| File | Change |
|---|---|

## Verified safe, deliberately NOT changed

Call sites that look like they belong in the fix but don't, with the reason for
each. This section is worth as much as the fix itself: it is what stops the next
person re-opening settled ground, and it is what a reviewer checks first.

## Tests

Test classes, behavior covered, and evidence that the tests detect the reported
problem and pass with the fix. Record any limits to the verification.

Full suite result and JDK used: [N run, N failures, N errors, N skipped]

## Open questions

Anything left for the PMC or the reporter to decide.
