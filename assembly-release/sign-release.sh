#!/usr/bin/env bash
#
# Sign and checksum the Roller release archives produced by "mvn package".
#
#   ROLLER_SIGNING_KEY=<keyid> ./sign-release.sh <version> [rc-suffix]
#
#   ./sign-release.sh 6.1.6           # final release
#   ./sign-release.sh 6.1.6 -rc1      # release candidate
#
# For a release candidate the archives are renamed to carry the suffix before
# they are signed, so the signatures match the names that are voted on.

set -euo pipefail

vstring="${1:-}"
rcstring="${2:-}"

if [ -z "$vstring" ]; then
    echo "usage: ROLLER_SIGNING_KEY=<keyid> $0 <version> [rc-suffix]" >&2
    echo "   eg: ROLLER_SIGNING_KEY=ABCD1234 $0 6.1.6 -rc1" >&2
    exit 2
fi

key="${ROLLER_SIGNING_KEY:-}"
if [ -z "$key" ]; then
    echo "ROLLER_SIGNING_KEY is not set." >&2
    echo "Name the signing key explicitly: older, non-compliant keys may still" >&2
    echo "be in the keyring and gpg would otherwise pick one of them." >&2
    exit 2
fi

# Refuse to sign with a key that does not meet current ASF policy, which
# requires RSA and forbids new DSA keys:
# https://infra.apache.org/release-signing.html
if ! keyline=$(gpg --with-colons --list-keys "$key" 2>/dev/null | grep -m1 '^pub:'); then
    echo "no key matching '$key' in the keyring" >&2
    exit 1
fi
algo=$(echo "$keyline" | cut -d: -f4)
bits=$(echo "$keyline" | cut -d: -f3)
if [ "$algo" != "1" ]; then
    echo "key $key is not RSA (public key algorithm $algo); ASF policy forbids" >&2
    echo "new DSA keys for code signing" >&2
    exit 1
fi
if [ "$bits" -lt 4096 ]; then
    echo "key $key is only $bits bits; ASF policy asks for at least 4096" >&2
    exit 1
fi

cd "$(dirname "$0")"

for kind in source binary; do
    for ext in tar.gz zip; do
        built="target/apache-roller-${vstring}-${kind}.${ext}"
        final="target/apache-roller-${vstring}${rcstring}-${kind}.${ext}"

        if [ ! -f "$built" ] && [ ! -f "$final" ]; then
            echo "missing $built — run 'mvn package' first" >&2
            exit 1
        fi

        # Only rename when there is a suffix to add and the rename has not
        # already happened. The previous version of this script tested
        # [ rcstring != "" ], which compares the literal word against the empty
        # string and is therefore always true, so it also "renamed" final
        # releases onto themselves.
        if [ -n "$rcstring" ] && [ "$built" != "$final" ] && [ -f "$built" ]; then
            mv "$built" "$final"
        fi

        gpg --local-user "$key" --armor --detach-sig --yes "$final"

        # shasum(1) format, which downloaders can feed straight back to
        # "shasum -c". SHA-512 and SHA-256 are what current policy asks for;
        # MD5 and SHA-1 must not be published.
        base=$(basename "$final")
        ( cd target && shasum -a 512 "$base" > "$base.sha512" )
        ( cd target && shasum -a 256 "$base" > "$base.sha256" )
    done
done

echo "Signed apache-roller-${vstring}${rcstring} with key $key:"
ls -1 "target/apache-roller-${vstring}${rcstring}-"*
