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
# Summarize Roller activity for a board report window.
#
#   quarter-activity.sh <since> [<until>]     dates as YYYY-MM-DD
#
# Run from a Roller checkout after `git fetch origin --tags`. Prints commits
# per author on each line of development, authors whose first commit falls in
# the window, tags created in the window, and merged pull requests (with gh).
# Read-only: it does not fetch, write or contact anything except GitHub via gh.

set -uo pipefail

since="${1:?usage: quarter-activity.sh <since> [<until>]}"
until="${2:-$(date +%Y-%m-%d)}"
# git reads a bare date as that date at the current time of day, so pin the
# window to whole days
git_since="$since 00:00:00"
git_until="$until 23:59:59"
branches=(origin/master origin/roller-6.1.x)
repo="apache/roller"

git rev-parse --git-dir >/dev/null 2>&1 || { echo "not a git checkout" >&2; exit 1; }

refs=()
for b in "${branches[@]}"; do
    git rev-parse --verify -q "$b" >/dev/null && refs+=("$b")
done
[ ${#refs[@]} -gt 0 ] || { echo "none of ${branches[*]} exist; fetch origin first" >&2; exit 1; }

echo "Window: $since .. $until"
echo

for b in "${refs[@]}"; do
    n=$(git rev-list --count --no-merges --since="$git_since" --until="$git_until" "$b")
    echo "== Commits on $b (no merges): $n"
    git shortlog -sn --no-merges --since="$git_since" --until="$git_until" "$b" | sed 's/^/  /'
    echo
done

echo "== Commits on both lines, counted once"
git shortlog -sn --no-merges --since="$git_since" --until="$git_until" "${refs[@]}" | sed 's/^/  /'
echo

echo "== Authors whose first commit is in the window"
found=""
while IFS= read -r email; do
    [ -n "$email" ] || continue
    if [ -z "$(git log --all -1 --format=%h --until="$git_since" --author="<$email>")" ]; then
        name=$(git log "${refs[@]}" -1 --format=%aN --author="<$email>")
        echo "  $name <$email>"
        found=1
    fi
done < <(git log --no-merges --since="$git_since" --until="$git_until" --format=%aE "${refs[@]}" | sort -u)
[ -n "$found" ] || echo "  (none)"
echo

echo "== Tags created in the window"
git for-each-ref --sort=creatordate --format='%(creatordate:short) %(refname:short)' refs/tags \
    | awk -v s="$since" -v u="$until" '$1 >= s && $1 <= u { print "  " $0 }'
echo "  (a tag date is when the tag was made, not the release announcement date)"
echo

echo "== Merged pull requests"
if command -v gh >/dev/null 2>&1; then
    gh pr list --repo "$repo" --state merged --limit 300 \
        --search "merged:$since..$until" \
        --json number,title,baseRefName,author \
        --template '{{range .}}  #{{.number}} [{{.baseRefName}}] {{.title}} ({{.author.login}}){{"\n"}}{{end}}' \
        || echo "  gh failed; check authentication"
else
    echo "  gh not installed; skipped"
fi
