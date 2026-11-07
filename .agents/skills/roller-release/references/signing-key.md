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

# Release signing

Read the current [ASF signing guidance](https://infra.apache.org/release-signing.html)
and [distribution policy](https://infra.apache.org/release-distribution.html).
Select the acting release manager's key explicitly. Never copy another person's
fingerprint or assume the default local key is suitable.

Check ownership, expiry, revocation, algorithm and strength, including the signing
subkey. For a new RSA signing key, use at least 4096 bits and an appropriate ASF
identity. Keep private keys, passphrases, keyring inventories and local configuration
backups outside the checkout. Let GnuPG prompt through the user's normal pinentry;
do not capture a passphrase in shell commands or agent output.

Publish the public key through the project's KEYS process and the release manager's
ASF profile as applicable. The project KEYS file is available at
<https://downloads.apache.org/roller/KEYS>. Add new public keys without removing
historical signing keys. Verify fingerprints independently before trusting an import.

Set `ROLLER_SIGNING_KEY` to the selected fingerprint and `VERSION` / `RC_SUFFIX`
to the release inputs. Inspect the checkout's `assembly-release/sign-release.sh`
usage; in versions supporting positional inputs, run from the repository root:

```sh
assembly-release/sign-release.sh "$VERSION" "$RC_SUFFIX"
```

The current script expects `ROLLER_SIGNING_KEY` in its environment. Export it
explicitly in the release session. Confirm that the script signs with the intended
key and uses an approved digest. Do not transplant an old signing script from
release notes. Generate armored detached signatures and SHA-256 and/or SHA-512
checksum sidecars; avoid legacy digest formats.

Verify every archive's signature against its bytes and every checksum from the
artifact directory. Check the expected fingerprint, not just GnuPG's exit status.
A cryptographically valid signature does not itself establish signer identity or
full release compliance. Keep the signer fingerprint in release execution notes
and the vote draft, not as a fixed default in this skill.
