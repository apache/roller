<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
-->

# Roller UI tests

Browser tests for Roller, written in Java with [Playwright](https://playwright.dev/java/).
They are deliberately not part of the main Maven build: nothing here ships to end users, and the build should not depend on a browser being available.
CI runs them on every push and pull request (see `.github/workflows/main.yml`).

The suite points at a running Roller and adapts to how it is configured:

* `NewUserJourneyIT` covers what the old Selenium suite did: register the first user, sign in, create a weblog, publish an entry and read it back on the blog. Roller only accepts registrations while it has no users, so this test skips itself on an instance that already has one, and on an instance that delegates login to an identity provider.
* `OidcLoginIT` signs in through an external OIDC provider as an administrator and as a regular user, checks that only the administrator can reach server administration, and that a signed-in OIDC user can create a weblog and see it rendered. It skips itself when no provider is configured.
* `LoginPageIT` checks the login page offers exactly the sign-in mechanisms of the configured authentication method. It only runs when you declare that method (see below).

Skipping keeps casual local runs friendly, but it also means a misconfigured instance could pass with everything skipped.
Declare what the instance is supposed to be and mismatches become failures instead:

    mvn verify -Droller.expectedAuth=db          only the username/password form
    mvn verify -Droller.expectedAuth=oidc        only identity-provider buttons
    mvn verify -Droller.expectedAuth=db-oidc     both

CI covers all three: `db` on Jetty with Derby, `oidc` and `db-oidc` on the Docker Compose stack (the compose file's authentication method can be overridden with the `AUTHENTICATION_METHOD` environment variable).
Roller's remaining authentication methods have no coverage here: `ldap` needs a directory server and `cma` needs container-managed security, and neither is part of this project's stacks.

## Running against Jetty and Derby (database auth)

From the project root, build once and start Roller with an in-memory database:

    mvn -DskipTests install
    mvn jetty:run

Then, from this directory:

    mvn verify

Every `jetty:run` start gives a fresh database, which is what the new-user journey needs.
Run it again without restarting and the journey reports itself skipped rather than failing, because the first user now exists.

## Running against Docker Compose (OIDC auth)

The compose stack runs Roller against PostgreSQL with Keycloak as the identity provider.
Roller and your browser must both reach Keycloak at the same hostname, so add this line to `/etc/hosts` once:

    127.0.0.1 keycloak

Then, from the project root:

    docker compose up -d

and from this directory:

    mvn verify -Droller.baseUrl=http://localhost:8080/

Keycloak is seeded with an administrator (`admin`/`admin`) and a regular user (`user`/`user`).
To offer form login next to the provider buttons, start the stack with `AUTHENTICATION_METHOD=db-oidc docker compose up -d` instead; on a fresh database the new-user journey then runs against it too.

## Options

    mvn verify -Dplaywright.headed=true      watch the browser
    mvn verify -Droller.baseUrl=<url>        point at any Roller instance
    mvn verify -Dit.test=NewUserJourneyIT    run a single test class

Playwright downloads the browser it needs on first run.
When a test fails, a trace is written to `target/playwright-traces/<test>.zip`; open it with:

    npx playwright show-trace target/playwright-traces/<test>.zip
