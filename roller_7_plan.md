# Roller 7 Plan

Status: draft, 2026-09-06
Owner: Dave Johnson
Predecessor: 6.1.6, currently at `roller-6.1.6-rc2` — not yet voted

Nothing in this plan starts before two things finish: the triage-2026
vulnerability work, and the 6.1.6 release vote.

## 1. What Roller 7 is

Roller 7.0.0 is the modernization release. It takes Roller off Java EE 8
(`javax.*`) and onto Jakarta EE 11, replaces the authentication, UI and test
stacks that were holding the dependency tree back, and removes ROME Propono —
the single dependency that has pinned ROME at 1.19.0 for years.

Everything currently open on GitHub lands in 7.0.0. There are eleven open pull
requests; nine of them are Roller 7 material, two are dead and get closed.

**Decisions taken 2026-09-06:**

| Decision | Choice | Consequence |
|---|---|---|
| Java baseline | **21** | One step past the 17 baseline in #154. CI matrix becomes 21 + 25; the 11 and 17 legs go away. |
| AtomPub / Propono order | **Jakarta stack first, #161 after** | #154 merges with its forked Propono `AtomServlet`; #161 is then rebased onto Jakarta, rewritten against `jakarta.*`, and deletes the fork it inherits. |
| Release shape | **One 7.0.0 carrying everything** | Jakarta + OIDC + Bootstrap 5 + Playwright + themes + AtomPub in a single release and a single PMC vote. Longer beta, one upgrade for users. |

## 2. Where the code actually is

`master` is 6.1.6-rc2 — cut, not yet voted. Nineteen commits landed on it
2026-09-05/06: the security triage fixes plus release plumbing. Matt's stack branched from `f1422f444`
(2026-08-13), *before* all of that. So every PR in the stack is a month behind a
master that moved hard in exactly the areas the stack touches: AtomPub, XML-RPC,
OAuth, salt filters, media handling, authoring JSPs.

Measured against `f1422f444`:

| PR | Own change set | Files also touched by 6.1.6 work |
|---|---|---|
| #154 Jakarta EE 11 | 189 files¹ | **40** |
| #155 OIDC | 208 cumulative | 41 |
| #156 Bootstrap 5 | 265 cumulative | 55 |
| #157 Playwright | 287 cumulative | 59 |
| #159 media link markup | 1 file | 1 |
| #160 three themes | 46 files¹ | **0** |
| #161 AtomPub StAX | 40 files | 8 |

¹ Measured locally from the branch point; GitHub reports 187 and 44 because
the branches were cut a commit or two before `f1422f444`.

That 40-file overlap on #154 is the real cost of Roller 7 and it is not
mechanical — several of the collisions are two different answers to the same
question (section 5).

## 3. Pull request inventory and disposition

### Merge to master now, ahead of everything (small, clean, 6.1.x-class)

| PR | Title | Author | Notes |
|---|---|---|---|
| #159 | Use double-quoted attributes in media file links inserted into new entries | mraible | One file. Approved by mbien. Real bug on master — image markup from the "create a post from your uploaded files" flow does not render. Found by Greg Huber on dev@. |
| #151 | Fix incorrect PostgreSQL port in Dockerfile | MustafaCelal | One line, outside contributor, open since Feb. Branched long before `.asf.yaml` existed, so rebase rather than merge, then land it. |
| #160 | Add three responsive light and dark blog themes | mraible | 46 files, **zero overlap** with 6.1.6 work — all new theme directories plus a `ThemeManagerTest` addition. Velocity templates and CSS, no `javax` surface, so it neither helps nor hurts the migration. Land it early and get it out of the rebase blast radius. |

### Close as superseded

| PR | Title | Why |
|---|---|---|
| #120 | Bump spring-web 5.3.20 → 6.0.0 | dependabot, March 2023. #154 takes Spring to 7.0.8. |
| #125 | Bump struts2-core 2.5.29 → 2.5.31 | dependabot, July 2023. #154 takes Struts to 7.1.1. |
| #129 | Bump spring-security-config 5.8.3 → 5.8.5 | dependabot, July 2023. #154/#155 take Spring Security to 7.0.6. |

Close them with a one-line comment pointing at ROL-2183 so the history reads
sensibly. Re-enable dependabot against the 7.0 tree afterwards (section 7).

### The Roller 7 stack, in merge order

1. **#154 — ROL-2183: Migrate from javax to Jakarta EE 11** (mraible, 187 files, 22 commits, targets `master`)
   Java 17 → *we retarget to 21*, Servlet 6.1, Struts 7.1.1, Spring 7.0.8,
   Spring Security 7.0.6, EclipseLink 5.0.1, Jetty 12.1.12, Tomcat 11,
   Derby 10.16.1.1. 320 `javax.*` imports converted across 89 files, 29 ORM
   files renamespaced, XWork → `org.apache.struts2`, Spring Security moved to
   expression-based access control. Removes OAuth 1.0a and the OpenID 2.0
   filter. Forks `XmlRpcServlet` and Propono's `AtomServlet` into Roller because
   neither library has a Jakarta release. 45 review comments, two Copilot passes.
2. **#155 — Replace OpenID 2.0 with OAuth 2.0/OIDC login** (mraible, 28 own files, targets `feature/jakarta-ee-10-migration`)
   `spring-security-oauth2-client`, `RollerClientRegistrationRepository` reading
   provider config from Roller properties, `RollerOidcUserService` resolving the
   Roller account behind the OIDC principal via the existing `openIdUrl` column.
   New `oidc` / `db-oidc` values for `authentication.method`; `openid` and
   `db-openid` are gone. Keycloak in docker-compose. Auto-provision off by
   default; the `users.firstUserAdmin` bootstrap grant no longer applies to
   auto-provisioned accounts.
3. **#156 — Convert the admin and editor UI from Bootstrap 3 to Bootstrap 5.3** (mraible, 63 own files, targets `feature/oidc-login`)
   Bootstrap 5.3.8, bootstrap-icons for glyphicons, form theme via
   struts2-bootstrap-plugin 6.1.0. Fixes dismissible alerts, badge pills, button
   rows, field-help tooltips. Audited page by page against a seeded master
   baseline — 24 admin/editor screens screenshot-compared.
4. **#157 — Replace the Selenium suite with Playwright** (mraible, 29 own files, targets `feature/bootstrap-5`)
   Deletes `it-selenium`; adds `it-playwright` with `NewUserJourneyIT`,
   `OidcLoginIT`, `LoginPageIT` and `WebServicesIT`. `WebServicesIT` is the only
   coverage of the forked XML-RPC and AtomPub servlets and it already caught the
   AtomPub basic-auth 401 bug that exists on master. CI runs it three ways: db on
   Jetty/Derby, plus oidc and db-oidc against docker-compose.
   Open question from mbien on the PR — "what is the trigger for the test
   framework swap?" — still needs an answer in the PR description. Suggested
   answer: Struts 2.5.30+ breaks the Selenium tests, which is why
   `struts.version` has been pinned at 2.5.29 for years, and the suite covers
   one auth method out of five.
5. **#161 — Replace ROME Propono AtomPub server with self-contained StAX implementation** (snoopdave, 40 files, targets `master`)
   RFC 5023 server on JDK StAX and plain DTOs. New `RollerAtomServlet`, wire
   model, `AtomWriter`/`AtomReader` (DTDs and external entities disabled), 34
   tests including a full lifecycle integration test against in-memory Derby and
   RELAX NG schema validation with Jing. **Merges last**, rebased onto the
   Jakarta tree — see section 6.

## 4. Branch and version plan

```
6.1.6 (tag)
  └── roller-6.1.x        <- cut now, maintenance only, matches roller-6.0.x/roller-5.2.x convention
master                    <- becomes 7.0.0-SNAPSHOT, is Roller 7 development
```

Steps, in order:

1. Once 6.1.6 is voted through, cut `roller-6.1.x` from the release tag.
   Nothing goes there unless a security report forces a 6.1.7.
2. On `master`: `roller.version` and `<version>` to `7.0.0-SNAPSHOT` in the
   parent pom, `app`, `db-utils`, `assembly-release`, `it-selenium`
   (until #157 deletes it), and the Docker files.
3. Land #159, #151, #160.
4. Rebase and merge the stack: #154, then #155, then #156, then #157, each
   retargeted to `master` as its parent lands.
5. Rebase and merge #161.
6. Modernization sweep (section 7).
7. Beta, then release (section 8).

The stack merges as four separate PRs, not one squash. The commit-per-phase
structure in #154 is worth keeping in history — when something breaks in
production a year from now, "which phase" is the first question.

## 5. Reconciliation: 6.1.6 master vs. the stack

These are decisions, not conflicts a rebase can resolve. Each needs an answer
before #154 merges, and each answer belongs in the 7.0.0 upgrade notes.

| Collision | 6.1.6 master says | The stack says | Call |
|---|---|---|---|
| **WSSE AtomPub auth** | Retired (#166). `webservices.atomPubAuth` takes `basic` and `oauth`; `wsse` fails closed on startup. | #154 keeps WSSE and advertises "basic or wsse"; #161 also removes WSSE. | Master wins: WSSE stays dead. Strip the WSSE paths and admin labels from #154 during the rebase. |
| **OAuth 1.0a for AtomPub** | Kept, and hardened — #165 fixed the authorize servlet's session handling. | #154 deletes it: `net.oauth` is unmaintained and javax-only. | Stack wins: OAuth 1.0a goes. Note in the upgrade guide that AtomPub is basic-only in 7.0, and that #155's OIDC covers browser login, not AtomPub. Decide whether `OAuthManager`, `OAuthAccessorRecord` and the `roller_oauth*` tables get dropped or left inert — a schema drop needs a migration script. |
| **Trackback** | Both directions removed (#163, #178). `TrackbackServlet`, `WeblogTrackbackRequest` and the outbound action are gone. | #154 still edits those files (they exist at its branch point). | Master wins. These become delete-vs-modify conflicts — resolve to delete. |
| **Authoring UI inline JS** | Moved to data attributes across the authoring JSPs (#168). | #156 rewrites the same JSPs for Bootstrap 5. | Rebase #156 onto the data-attribute markup, not the other way round. This is the largest mechanical conflict in the stack — 55 overlapping files — and the screenshot baseline used for the Bootstrap 5 audit has to be regenerated afterwards. |
| **Media content types** | Derived from file content (#174). | Stack predates it. | Master wins; verify the Struts 7 `UploadedFilesAware` rework in #154 still routes through the content-sniffing path, and that `WebServicesIT` covers it. |
| **Enclosure metadata** | Stored as submitted, no remote fetch (#175). | Stack predates it. | Master wins. |
| **XML-RPC** | Weblog permission checks added (#164), vendor extension types disabled (#171). | #154 forks `XmlRpcServlet` into Roller. | Both. Make sure the forked servlet is wired behind the same checks — `WebServicesIT` publishes over XML-RPC and should assert the permission failure path too. |
| **Salt filters** | Submitted and response salts separated (#167); one-time salt work is on a local branch. | #154 touches `ValidateSaltFilter`. | Master wins. |
| **Frontpage / template resolution** | #169, #170, #172. | #160's themes touch theme resolution. | No conflict measured (0 overlap), but re-run `ThemeManagerTest` and the frontpage tests after #160 lands. |
| **Java baseline** | 11, CI on 11/17/21/25. | 17, CI on 17/21/25. | **21**, CI on 21 and 25. Our delta on top of #154: bump `<release>`, drop the 17 leg, re-check Derby (10.17 is the Java 21 baseline — decide whether to take it or stay on 10.16.1.1), and confirm Mockito still instruments 25. |

## 6. Propono removal (#161), rebased

The chosen order means #161 lands on a tree where Propono has already been
forked in rather than removed. Concretely, after #154–#157 merge:

1. Rebase `replace-propono-atompub` onto `master`.
2. Convert the new AtomPub classes to `jakarta.servlet.*`. `javax.xml.stream` is
   JDK API and does **not** move — that was the point of choosing StAX.
3. Delete the forked Propono `AtomServlet` and `RollerAtomRequestImpl` that #154
   brought in, plus the null-path-info normalization that was ported into it —
   `RollerAtomServlet` must carry that behavior instead (a request to the bare
   `/roller-services/app` mapping serves the service document; #157's smoke test
   guards it).
4. Drop `rome-propono` from `app/pom.xml` and remove the `<!-- todo:
   remove/replace propono -->` comment and the `rome.version` pin comment.
5. Address Matt's review — nine verified findings, of which at least these are
   blockers:
   - `readBody()` reads the whole request body with `readAllBytes()` before any
     quota check. Propono streamed to a temp file. As written, any authenticated
     user can OOM the server with a large POST to the media collection. Stream
     to a temp file or bound the read against the media quota.
   - `getElementText()` throws on `type="xhtml"` content, summary and title,
     which RFC 4287 allows and ROME accepted. Clients publishing xhtml get a
     500. Needs a branch that captures child XML as a string.
   - `MediaCollection.deleteEntry()` strips the `.media-link` suffix for the
     filename but passes the unstripped path to `getMediaFileByPath`, so DELETE
     on the server's own advertised `rel="edit"` URI always NPEs. Pre-existing,
     carried forward; fix it and add a delete test.
   - Unrecognized `webservices.atomPubAuth` values must fail closed with a log
     line naming the property and the valid options, the way 6.1.6 handles a
     stored `wsse`.
6. Port `WebServicesIT` from #157 onto the StAX implementation — Matt offered to
   do this and it is the highest-leverage follow-up on the PR.
7. Run an over-the-wire exerciser (APE or equivalent) against a deployed
   instance. The unit tests do not exercise HTTP transport or BASIC auth over
   the wire, and the previous format was ROME-generated, so interop is the risk.

Then, and only then, unpin ROME.

## 7. Modernization sweep after the stack lands

Ordered by value, not effort:

1. **Unpin ROME.** `rome.version` is at 1.19.0, pinned by a comment in
   `app/pom.xml` saying the next version removes Propono. With #161 in, take the current ROME for
   feed rendering and the Planet aggregator. This is the whole reason Propono had
   to go.
2. **Unpin Struts.** `struts.version` is pinned at 2.5.29 with "`.30+` breaks
   selenium tests". #154 goes to 7.1.1 and #157 deletes the Selenium suite, so
   delete the comment and the reason for it.
3. **Decide XML-RPC's future.** Roller carries a fork of `XmlRpcServlet` because
   `org.apache.xmlrpc` 3.1.3 is long unmaintained and has no Jakarta release. Blogger and
   MetaWeblog are legacy APIs; 6.1.6 already had to add permission checks and
   disable vendor extension types on them. Either commit to owning the fork or
   put deprecation of XML-RPC on the 7.x roadmap. Worth a dev@ thread.
4. **Guice.** Still on `com.google.inject`. Check the version, and whether the
   EclipseLink 5 / Spring 7 tree makes a straight Spring DI migration cheap
   enough to be worth doing while everything else is already moving.
5. **Velocity 2.4.1 and the template layer.** No action forced by Jakarta, but
   confirm the rendering path is clean on 21 and that the three new themes from
   #160 render on the Bootstrap 5 admin.
6. **Re-enable dependabot** against the 7.0 tree once the versions settle, with
   grouped PRs so we do not get another three-year backlog of singles.
7. **Docs.** `docs/roller-install-guide.adoc`, `roller-user-guide.adoc` and
   `roller-template-guide.adoc` all describe a javax/Tomcat 9/OpenID world.
   The install guide needs Java 21, Tomcat 11, the OIDC configuration, and the
   new `authentication.method` values. The user guide needs the Bootstrap 5
   screens reshot.
8. **Docker.** Tomcat 11 base image, PostgreSQL 16, Keycloak for the OIDC demo,
   and the compose file that #151 was trying to fix.
9. **Local branches to reconcile or delete.** `safer-defaults` (4 commits ahead),
   `one-time-salt` (1), `jakarta` (2) — decide whether any of that is Roller 7
   material before the rebase makes them unmergeable. `remove-solr`,
   `jakara-migration`, `jstl-not-provided` and `parse-referrer` are level with
   master and can be deleted.

## 8. Release engineering

Roller 7.0.0 is a bigger release than anything since 6.0, and the changes users
will actually feel are the removals. The release notes lead with those, not with
the framework versions.

**Upgrade notes must cover:**

- Java 21 required. Java 11 and 17 no longer supported.
- Tomcat 11 (Jakarta) required. Tomcat 9 will not run Roller 7.
- `authentication.method`: `openid` and `db-openid` are gone; use `oidc` /
  `db-oidc` and configure a provider. Existing OpenID 2.0 identities in
  `openIdUrl` are reused by the OIDC account linking path — document what
  happens to a user whose provider is dead.
- AtomPub: OAuth 1.0a and WSSE both gone. Basic auth only.
- XML-RPC and AtomPub survive but are reimplemented — anyone with a custom
  client should retest.
- Trackback (both directions) already removed in 6.1.6; repeat it here for
  people upgrading from 6.1.5 or earlier.
- Any schema change from dropping the OAuth 1.0a tables, with a migration
  script and a "you may leave them in place" option.

**Process:** follow the existing release runbook — the `roller-release` skill has
the RC, signing, staging, VOTE and promotion steps. Two things specific to this
release:

- Ship at least one beta or RC that the dev@ list is actually asked to deploy.
  A Jakarta migration plus an auth migration plus a UI migration is not something
  to discover in the GA vote.
- The 72-hour VOTE window will not be enough for people to test this properly.
  Announce the beta on dev@ and user@ with a deadline of its own.

**Definition of done for 7.0.0:**

- All four stack PRs plus #159, #151, #160 and #161 merged to `master`.
- `grep -r "javax\." app/src/main/java` returns only `javax.xml.stream`,
  `javax.sql`, `javax.naming`, `javax.imageio` — JDK packages, not Java EE.
- No `rome-propono` in any pom; ROME on a current version.
- Playwright suite green on all three CI legs.
- Unit tests green on 21 and 25.
- Docker compose comes up and the new-user journey passes against it.
- Install guide reflects Java 21 / Tomcat 11 / OIDC.
- A real AtomPub client round-trips against a deployed instance.

## 9. Risks

| Risk | Mitigation |
|---|---|
| The #156 rebase onto the 6.1.6 authoring JSPs is the single biggest source of silent breakage — 55 overlapping files, and the Bootstrap 5 audit baseline is now stale. | Regenerate the screenshot baseline from post-6.1.6 master before rebasing, and re-run the 24-page comparison after. |
| AtomPub wire-format regression is invisible to the JUnit suite. | #157's `WebServicesIT` plus an over-the-wire exerciser before the vote. Do not ship on unit tests alone. |
| Java 21 narrows the deployment base right when we are also asking users to move to Tomcat 11. | It is one message either way — "Roller 7 needs a current stack". Say it once, loudly, in the release notes. |
| The stack is one contributor's work and it is large. If Matt goes quiet mid-rebase, the PMC owns 187 files of migration it did not write. | Review #154 phase by phase now, while he is around to answer. Two committers should be able to explain the Struts 7 parameter-binding and Spring Security authorization-manager changes without him. |
| A security report arrives mid-flight and forces a 6.1.7. | `roller-6.1.x` exists from day one so a fix does not have to be cherry-picked out of a half-migrated master. |
| Long-lived feature branches drift again. | Merge the stack in weeks, not months. Nothing in section 7 blocks a merge. |

## 10. Open questions for dev@roller

1. Java 21 or 17 as the 7.0 baseline — this plan says 21; the PMC should agree
   before #154 merges, because it is much cheaper to decide now than after.
2. Does XML-RPC (Blogger/MetaWeblog) have a future, given we now maintain a fork
   of its servlet?
3. Do the OAuth 1.0a tables get dropped in 7.0 or left inert?
4. Beta timing and how long the beta window stays open before the GA vote.
5. Should the three new themes from #160 change the default for new weblogs, or
   does Basic stay the default? (#160 explicitly preserves Basic — confirm that
   is what we want long term.)
