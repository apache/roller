#!/usr/bin/env bash
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Verify a staged Roller release candidate the way a voter will.
#
#   check-release.sh <dir> [--build]
#
# Checks every archive in <dir> for: a valid detached signature, a matching
# checksum, an RSA signing key (>= 2048; warns below 4096), a
# signature digest that is not SHA-1, and LICENSE/NOTICE inside the archive.
# The source archive is additionally checked for stray compiled artifacts, and
# with --build it is unpacked and built on the current JDK.
#
# Exit status is the number of failed checks, so it is usable in a gate.

set -uo pipefail

dir="${1:?usage: check-release.sh <dir> [--build]}"
do_build=""
[ "${2:-}" = "--build" ] && do_build=1

fail=0
pass() { printf 'ok    %s\n' "$1"; }
bad()  { printf 'FAIL  %s\n' "$1"; fail=$((fail + 1)); }
warn() { printf 'warn  %s\n' "$1"; }

shopt -s nullglob
archives=( "$dir"/*.tar.gz "$dir"/*.zip )

if [ "${#archives[@]}" -eq 0 ]; then
    bad "no .tar.gz or .zip archives found in $dir"
    exit $fail
fi

# --- signatures -------------------------------------------------------------

for f in "${archives[@]}"; do
    base=$(basename "$f")

    if [ ! -f "$f.asc" ]; then
        bad "$base has no detached signature"
        continue
    fi

    if ! gpg --verify "$f.asc" "$f" >/dev/null 2>&1; then
        bad "$base signature does not verify (is the key imported?)"
        continue
    fi
    pass "$base signature verifies"

    # Which key, and does it meet policy? algo 1 = RSA, 17 = DSA.
    keyid=$(gpg --list-packets "$f.asc" 2>/dev/null \
        | awk -F'keyid ' '/keyid/ {print $2; exit}')
    digest=$(gpg --list-packets "$f.asc" 2>/dev/null \
        | awk -F'digest algo ' '/digest algo/ {split($2,a,","); print a[1]; exit}')

    if [ -n "$keyid" ]; then
        read -r algo bits <<<"$(gpg --with-colons --list-keys "$keyid" 2>/dev/null \
            | awk -F: '/^pub:/ {print $4, $3; exit}')"
        case "$algo" in
            1)  if [ "${bits:-0}" -ge 4096 ]; then
                    pass "$base signed with RSA-$bits key $keyid"
                elif [ "${bits:-0}" -ge 2048 ]; then
                    warn "$base signed with RSA-$bits key $keyid (policy prefers 4096)"
                else
                    bad "$base signed with RSA-$bits key $keyid (policy requires >= 2048)"
                fi ;;
            17) bad "$base signed with DSA key $keyid — policy says do not use DSA" ;;
            "") warn "$base signing key $keyid not in local keyring; cannot check strength" ;;
            *)  warn "$base signed with key $keyid of unexpected algorithm $algo" ;;
        esac
    fi

    # digest algo 2 = SHA-1.
    if [ "${digest:-}" = "2" ]; then
        bad "$base signature uses SHA-1 — policy says avoid further use of SHA-1"
    elif [ -n "${digest:-}" ]; then
        pass "$base signature digest algo $digest (not SHA-1)"
    fi
done

# --- checksums --------------------------------------------------------------

for f in "${archives[@]}"; do
    base=$(basename "$f")
    found=""
    for alg in 512 256; do
        [ -f "$f.sha$alg" ] || continue
        found=1
        have=$(shasum -a "$alg" "$f" | awk '{print $1}')

        # shasum(1) format is what the policy asks for, and "shasum -c" is
        # the authoritative check, so try it first.
        if ( cd "$dir" && shasum -a "$alg" -c "$base.sha$alg" >/dev/null 2>&1 ); then
            pass "$base sha$alg matches"
        else
            # Legacy "gpg --print-md" checksum formatting writes
            # "<name>:" then uppercase hex in space-separated groups across
            # following lines; "shasum -c" cannot read that. Normalise to bare
            # lowercase hex so a correct digest in the wrong format is reported
            # as a format problem rather than a mismatch.
            #
            # Truncate to the digest's own length. Filenames contain hex
            # letters ("apache" starts with one), so an unbounded [0-9a-f]+
            # runs past the digest and into the name.
            width=$(( alg / 4 ))
            want=$(tr -d ' \n' < "$f.sha$alg" \
                | sed 's/^.*://' \
                | tr 'A-Z' 'a-z' \
                | grep -oE '[0-9a-f]+' \
                | head -1 | cut -c1-"$width")

            if [ "$want" = "$have" ]; then
                warn "$base sha$alg digest is correct, but the file is not in"
                warn "      shasum(1) format or names a different path, so"
                warn "      'shasum -c' fails for downloaders"
            else
                bad "$base sha$alg does NOT match"
            fi
        fi
    done
    [ -n "$found" ] || bad "$base has no .sha512 or .sha256"
    [ -f "$f.md5" ]  && bad "$base has a .md5 — MD5 files must not be published"
    [ -f "$f.sha1" ] && bad "$base has a .sha1 — SHA-1 files must not be published"
    [ -f "$f.sig" ]  && bad "$base has a .sig — binary signatures must not be published"
done

# --- archive contents -------------------------------------------------------

list_archive() {
    case "$1" in
        *.tar.gz) tar tzf "$1" 2>/dev/null ;;
        *.zip)    unzip -Z1 "$1" 2>/dev/null ;;
    esac
}

for f in "${archives[@]}"; do
    base=$(basename "$f")
    names=$(list_archive "$f")

    for required in LICENSE NOTICE; do
        if printf '%s\n' "$names" | grep -qE "(^|/)$required(\.txt)?$"; then
            pass "$base contains $required"
        else
            bad "$base is missing $required"
        fi
    done

    case "$base" in
        *source*)
            junk=$(printf '%s\n' "$names" | grep -E '\.(class|jar|war)$' || true)
            if [ -n "$junk" ]; then
                n=$(printf '%s\n' "$junk" | wc -l | tr -d ' ')
                bad "$base (source) contains $n compiled file(s):"
                printf '        %s\n' $junk
                echo   "        A source release should not ship third-party binaries."
                echo   "        Review packaging and licensing before the vote."
            else
                pass "$base (source) has no compiled artifacts"
            fi ;;
    esac
done

# --- optional build ---------------------------------------------------------

if [ -n "$do_build" ]; then
    src=""
    for candidate in "${archives[@]}"; do
        case "$candidate" in *source*.tar.gz) src="$candidate"; break ;; esac
    done
    if [ -z "$src" ]; then
        warn "no source tar.gz found; skipping build check"
    else
        tmp=$(mktemp -d)
        if tar xzf "$src" -C "$tmp" \
           && ( cd "$tmp"/*/ && mvn -q -ntp -DskipTests=true install >/dev/null 2>&1 ); then
            pass "$(basename "$src") builds from source"
        else
            bad "$(basename "$src") does NOT build from source"
        fi
        rm -rf "$tmp"
    fi
fi

echo
if [ "$fail" -eq 0 ]; then
    echo "All checks passed. Still download it yourself and look at it."
else
    echo "$fail check(s) failed — do not call the vote."
fi
exit "$fail"
