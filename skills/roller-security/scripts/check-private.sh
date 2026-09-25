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

# Disclosure lint for Apache Roller security work.
#
# Usage:
#   scripts/check-private.sh              # check staged changes + recent commits
#   scripts/check-private.sh --range master..HEAD
#   scripts/check-private.sh --pr <number>     # also lint a PR's title and body
#   --range requires an explicit BASE..TIP or BASE...TIP commit range.
#
# This checks selected wording and common private paths. Follow the agreed
# project workflow and review what the whole change reveals before publication.
#
# Checks four things that are cheap to get wrong and impossible to take back:
#   1. triage-<year>/ and security notes are excluded from git
#   2. no triage or security note is staged or tracked
#   3. no branch name, commit message, or added line announces the flaw
#   4. with --pr, no PR title or description announces the flaw
#
# This is a lint, not a guarantee. Read the diff, and read the PR body — the
# grep cannot infer case context and can flag innocent words. It does not
# inspect image contents, archives, all unstaged files or full committed diffs.

set -uo pipefail

RED=$'\033[31m'; YEL=$'\033[33m'; GRN=$'\033[32m'; OFF=$'\033[0m'
[[ -t 1 ]] || { RED=""; YEL=""; GRN=""; OFF=""; }

fail=0
warn=0
range=""
pr=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --range)
      if [[ $# -lt 2 || -z "$2" || "$2" == --* ]]; then
        echo "--range requires BASE..TIP (for example, master..HEAD)" >&2
        exit 2
      fi
      range="$2"; shift 2 ;;
    --pr) pr="$2"; shift 2 ;;
    -h|--help) sed -n '2,26p' "$0"; exit 0 ;;
    *) echo "unknown argument: $1" >&2; exit 2 ;;
  esac
done

cd "$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "not a git repo" >&2; exit 2; }

if [[ -n "$range" ]]; then
  base=${range%%..*}
  tip=${range#*..}
  tip=${tip#.}
  if [[ "$range" != *..* || -z "$base" || -z "$tip" || "$base" == -* || "$tip" == -* || "$tip" == .* || "$tip" == *..* ]]; then
    echo "--range requires explicit BASE..TIP or BASE...TIP endpoints; got '$range'" >&2
    exit 2
  fi
  if ! git rev-parse --verify --quiet "${base}^{commit}" >/dev/null ||
     ! git rev-parse --verify --quiet "${tip}^{commit}" >/dev/null; then
    echo "--range endpoints must resolve to commits: '$range'" >&2
    exit 2
  fi
fi

say_fail() { echo "${RED}FAIL${OFF}  $*"; fail=1; }
say_warn() { echo "${YEL}WARN${OFF}  $*"; warn=1; }
say_ok()   { echo "${GRN}ok${OFF}    $*"; }

# --------------------------------------------------------------------------- #
# 1. private directories are excluded
# --------------------------------------------------------------------------- #
shopt -s nullglob
private_paths=( triage-*/ SECURITY-REPORTS-*.md )
shopt -u nullglob

if [[ ${#private_paths[@]} -eq 0 ]]; then
  say_ok "no triage directories present"
else
  for p in "${private_paths[@]}"; do
    if git check-ignore -q "$p"; then
      src=$(git check-ignore -v "$p" | cut -f1)
      say_ok "$p excluded (${src})"
    else
      say_fail "$p is NOT excluded from git — add it to .git/info/exclude:
        echo '/${p%/}/' >> .git/info/exclude
        (use .git/info/exclude, not .gitignore: .gitignore is a committed file
         and this exclusion is a local working preference, not project policy)"
    fi
  done
fi

# --------------------------------------------------------------------------- #
# 2. nothing private is tracked or staged
# --------------------------------------------------------------------------- #
tracked=$(git ls-files -- 'triage-*' 'SECURITY-REPORTS-*' 2>/dev/null)
if [[ -n "$tracked" ]]; then
  say_fail "private files are TRACKED by git:"
  echo "$tracked" | sed 's/^/        /'
  echo "        git rm --cached <file>   # then verify with: git log --all -- <file>"
else
  say_ok "no private files tracked"
fi

staged=$(git diff --cached --name-only 2>/dev/null | grep -E '^(triage-|SECURITY-REPORTS-)' || true)
if [[ -n "$staged" ]]; then
  say_fail "private files are STAGED:"
  echo "$staged" | sed 's/^/        /'
else
  say_ok "no private files staged"
fi

# --------------------------------------------------------------------------- #
# 3. disclosure language
# --------------------------------------------------------------------------- #
# Terms that name a flaw rather than a change. CVE is here because a commit
# carrying a CVE id before announcement is itself the disclosure.
TERMS='CVE-[0-9]{4}|\b(vulnerab|exploit|IDOR|SSRF|SSTI|XXE|CSRF|XSS|RCE)|'\
'\b(security (fix|issue|flaw|hole|bug))|\b(attack(er)?|malicious|injection)|'\
'\b(privilege escalation|auth(oriz|entic)ation bypass|arbitrary (code|file))'

# Retrospective wording: describing what the code USED TO do. TERMS catches the
# words for a flaw; this catches the sentence shape that states the released
# version had one, which is the same disclosure by a different route and is easy
# to write by accident when explaining why a test exists. Warn rather than fail —
# "leak" is ordinary in a resource-handling comment, and past tense is ordinary
# in a javadoc about behaviour that never changed.
RETRO='\b(before|prior to) (this|the) (fix|change|commit|patch)|'\
'\b(previously|formerly|used to) (allow|permit|expos|resolv|accept|admit|be)|'\
'\bthe (hole|flaw|defect|weakness|escape|bypass)\b|'\
'\bleak(s|ed|ing)?\b|'\
'\b(sandbox escape|path traversal|directory traversal|open redirect)|'\
'\b(proof.of.concept|PoC)\b|'\
'\b(unauthenticated|anonymous) [a-z]* ?(can|could|is able to)\b|'\
'\bwould have (allowed|permitted|exposed|resolved)\b'

branch=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [[ -n "$branch" ]] && echo "$branch" | grep -qiE "$TERMS"; then
  say_fail "branch name '$branch' names the flaw — rename it after what the change does:
        git branch -m <new-name>"
else
  say_ok "branch name '$branch' is clean"
fi

if [[ -n "$range" ]]; then
  subjects=$(git log --format='%h %s%n%b' "$range" -- 2>/dev/null) || {
    echo "could not read commits in range '$range'" >&2
    exit 2
  }
  label="commits in $range"
else
  subjects=$(git log --format='%h %s%n%b' -20 2>/dev/null || true)
  label="last 20 commit messages"
fi

hits=$(echo "$subjects" | grep -inE "$TERMS" || true)
if [[ -n "$hits" ]]; then
  say_fail "$label contain disclosure language:"
  echo "$hits" | sed 's/^/        /'
  echo "        Unpushed? amend or rebase. Already pushed? do NOT rewrite —"
  echo "        tell security@apache.org; it may mean accelerating the release."
else
  say_ok "$label are clean"
fi

# Both greps use -E: in BRE, GNU grep treats \+ as the repetition operator, so
# '^\+\+\+' would silently match (and filter) every added line.
added=$(git diff --cached -U0 2>/dev/null | grep -E '^\+' | grep -vE '^\+\+\+' || true)
if [[ -n "$added" ]]; then
  code_hits=$(echo "$added" | grep -inE "$TERMS" || true)
  if [[ -n "$code_hits" ]]; then
    say_warn "staged additions mention flaw terms — check comments and test names:"
    echo "$code_hits" | head -20 | sed 's/^/        /'
    echo "        Some of these are legitimate (a class genuinely named"
    echo "        CsrfFilter, an existing javadoc). Judge each one."
  else
    say_ok "staged additions are clean"
  fi
fi

# Retrospective wording, across both commit messages and staged additions.
retro_scan=$(printf '%s\n%s\n' "$subjects" "$added")
retro_hits=$(echo "$retro_scan" | grep -inE "$RETRO" || true)
if [[ -n "$retro_hits" ]]; then
  say_warn "wording describes what the code used to do — read these:"
  echo "$retro_hits" | head -20 | sed 's/^/        /'
  echo "        Saying the old behaviour was wrong states that the released"
  echo "        version has the flaw. Describe what the code does now instead."
else
  say_ok "no retrospective wording in commits or staged additions"
fi

# --------------------------------------------------------------------------- #
# 4. PR title and body (opt-in, needs gh)
# --------------------------------------------------------------------------- #
# The PR description is the most common place a public fix turns into a public
# disclosure, because a good PR description explains why — and for an
# undisclosed vulnerability the why is the exploit.
if [[ -n "$pr" ]]; then
  if ! command -v gh >/dev/null 2>&1; then
    say_warn "--pr given but 'gh' is not installed; skipping PR check"
  else
    pr_text=$(gh pr view "$pr" --json title,body \
                --template '{{.title}}{{"\n"}}{{.body}}' 2>/dev/null || true)
    if [[ -z "$pr_text" ]]; then
      say_warn "could not read PR #$pr (wrong number, or not authenticated?)"
    else
      pr_hits=$(echo "$pr_text" | grep -inE "$TERMS" || true)
      if [[ -n "$pr_hits" ]]; then
        say_fail "PR #$pr title/body contains disclosure language:"
        echo "$pr_hits" | sed 's/^/        /'
        echo "        gh pr edit $pr --body-file <rewritten.md>"
        echo "        Note: GitHub keeps the original body under 'edited', and"
        echo "        the list notification already carried it. Rewriting"
        echo "        reduces the signal; it does not unpublish it."
      else
        say_ok "PR #$pr title/body has no flaw terms"
      fi
      say_warn "read PR #$pr yourself — a body can disclose with no flagged word,
        e.g. stating the defect plainly, or saying the tests were verified
        failing before the fix (which confirms exploitability in the release)"
    fi
  fi
fi

echo
if [[ $fail -ne 0 ]]; then
  echo "${RED}Potential disclosure or exclusion problems found.${OFF} Investigate the FAIL lines before proceeding."
  exit 1
elif [[ $warn -ne 0 ]]; then
  echo "${YEL}Review the warnings, then proceed if they are false positives.${OFF}"
  exit 0
else
  echo "${GRN}Clean.${OFF} Still read the diff — this lint does not understand context."
  exit 0
fi
