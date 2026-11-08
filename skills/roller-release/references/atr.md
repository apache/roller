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

# Apache Trusted Releases

Use [ATR](https://releases.apache.org/docs/) for Roller 7.x and later with local
builds and personal OpenPGP signing. ATR manages staging and publication. The PMC
approves the release; ATR checks supplement human verification. [Trusted Publishing](https://releases.apache.org/docs/trusted-publishing)
is a separate CI signing workflow requiring reproducibility and ASF approvals;
it is outside this process. Recheck current ATR documentation at release time.

## Release prerequisites

Follow [Getting started](https://releases.apache.org/docs/getting-started) and
[Moving to ATR](https://releases.apache.org/docs/moving-to-atr):

- Sign in with ASF MFA. Confirm PMC membership or PMC designation as a release
  manager. Verify the actual Roller project key, committee, repositories and
  catalog; do not assume seeded data is correct.
- Confirm the signing public key is associated with Roller and is present in the
  published committee KEYS file. Retain SVN-managed KEYS (ATR's default import
  mode); update keys in SVN.
- Review [project configuration](https://releases.apache.org/docs/project-configuration):
  metadata, release version policy, source checks, vote recipients and templates,
  download page, announcement recipients and destination suffix. Check the actual
  SVN layout and website before choosing the suffix; do not accept ATR's default
  destination without reviewing it. Record the resolved path in the release work
  record, not as an assumed path in this skill.
- Use Email vote mode and the normal minimum of 72 hours. Keep automatic
  publication and auto-archive off. Review settings shared across release lines;
  preserve the latest release of each supported line.
- Inspect any `.asf.yaml` project configuration before editing settings in ATR:
  synchronization from the default branch can overwrite manual changes. Record
  required configuration work separately; reading this skill does not configure
  the service or establish readiness.

## Compose and verify

1. Build and test the exact recorded RC commit using the main skill. Use final
   archive filenames, without an RC suffix, so publication needs no renaming or
   checksum edits. Run `assembly-release/sign-release.sh "$VERSION"` with the
   selected `ROLLER_SIGNING_KEY` exported. Do not re-sign after the vote.
2. Start a candidate in the confirmed Roller project for the final release
   version. Use the compose page's **Upload files** browser form to upload only
   the intended source and convenience binary archives, `.asc` signatures and
   SHA-256/SHA-512 sidecars. Supply the full 40-character source commit.
   See [Uploading files](https://releases.apache.org/docs/uploading-files).
3. Wait for archive validation and checks to finish. If validation rejects the
   upload, fix the issue and upload the whole rejected set again. For oversized
   browser requests, upload smaller batches; if necessary follow ATR's documented
   rsync route and explicitly record the same source commit.
4. Resolve blockers and investigate other concerns, including source comparison
   and licensing. Document any justified acknowledgments; do not suppress checks
   just to start voting. Record the final ATR revision, candidate URL, manifest,
   archive filenames and digests, signer fingerprint, RC tag and source commit.
   Optionally give the revision an immutable RC tag in ATR.
5. Download that revision to a fresh directory using its candidate page's download
   instructions. Verify the inventory against the
   [file manifest](https://releases.apache.org/docs/file-manifest), signatures,
   checksums and contents; run `scripts/check-release.sh <dir>` and build/test
   the downloaded source. ATR's manifest digests do not replace detached
   signatures or the project's checksum sidecars.

## Vote and replacement candidates

Review the vote email form against [the message guidance](vote-and-announce.md).
Use the ATR candidate page as the sole authoritative artifact location, plus
committee KEYS. Identify the pinned revision and source tag/commit; do not present
another artifact copy as part of the vote. Start the email vote through ATR only
when sending it is authorized. See
[Staging and voting](https://releases.apache.org/docs/staging-and-voting).

Allow the normal voting period, then check ATR's tally against the mailing-list
thread and current PMC membership. Require at least three positive binding votes
and more positive than negative binding votes. Record the result, thread and exact
approved revision before explicitly resolving the vote in ATR.

A failed or cancelled vote returns to compose. Preserve the old revision and vote
evidence; fix the candidate, increment the RC number, record a new immutable RC
Git tag as appropriate, and upload a new ATR revision. Repeat verification and
start a fresh vote on the replacement. Never silently replace voted artifacts or
reuse an earlier approval for changed bytes.

## Finish, website and announcement

Follow [Promoting to release](https://releases.apache.org/docs/promoting-to-release):

1. After the vote passes, review the finish page's destination and approved
   inventory. Confirm KEYS availability and explicitly publish through ATR. ATR
   commits to `svn:dist:release`; do not also run the SVN promotion steps.
   Preserve approved filenames, bytes and signatures. Record the resulting SVN
   revision and download URL; point the final Git tag at the approved RC commit.
2. Check public downloads, signatures and checksums against the approved candidate.
   If propagation is delayed, wait and retry verification; do not rebuild,
   re-upload or announce. Record any required external distributions separately;
   ATR does not publish to package registries for the project.
3. Publish the [website update](website.md) and verify the live download page and
   its links. Review the announcement form and recipients, then trigger ATR's
   announcement once authorized. ATR also checks download reachability. Do not
   send a duplicate manual announcement. Coordinate advisory timing with the PMC
   and ASF Security using the main skill's confidentiality requirements.
4. Leave automatic archiving disabled. Any later cleanup must confirm archive
   availability, website links and which releases remain supported, including
   6.1.x.
