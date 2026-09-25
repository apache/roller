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

# Roller codebase orientation

This is an architectural starting point, not a list of findings. Confirm paths,
framework versions and behavior on the branch being investigated. Keep case
characterization and reproduction instructions in the private case workspace.

- `app/src/main/java/org/apache/roller/weblogger/` contains application code.
- `app/src/main/resources/` contains application and framework configuration.
- `app/src/main/webapp/` contains web resources and deployment descriptors.
- `app/src/test/` contains application tests.
- `db-utils/` contains database utilities.
- `assembly-release/` defines release packaging outside the default reactor.
- `it-selenium/` contains browser integration tests.

Start from the reported entry point and follow the configured request processing
chain into the action or servlet, business service, and persistence layer. Find
configuration defaults in the branch's `roller.properties`; distinguish them
from deployment overrides. Check permissions and ownership in the actual call
path rather than inferring them from class or method names.

Record the tested revision, runtime, actor, input, observed outcome and expected
invariant in `IMPLEMENTATION.md`. Search related call sites to assess scope, but
avoid expanding a public patch into still-undecided cases without coordination.
Use a supported JDK from the branch's build documentation and CI configuration.
