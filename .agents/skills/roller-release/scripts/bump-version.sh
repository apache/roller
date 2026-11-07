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

# Preview or update Roller module versions; never commits.
# Usage: bump-version.sh <old> <new> [--write]
# Requires Git and Python 3. Run from any directory in the target checkout.
set -euo pipefail
root=$(git rev-parse --show-toplevel) || exit 1
python3 - "$root" "$@" <<'PY'
import argparse
from pathlib import Path
import re
import subprocess
import sys

root = Path(sys.argv[1])
parser = argparse.ArgumentParser(description="Preview or update Roller module versions.")
parser.add_argument("old")
parser.add_argument("new")
parser.add_argument("--write", action="store_true")
args = parser.parse_args(sys.argv[2:])
for value in (args.old, args.new):
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:-[A-Za-z0-9.-]+)?", value):
        parser.error("versions must be major.minor.patch with an optional suffix")
expected = ["pom.xml", "app/pom.xml", "db-utils/pom.xml",
            "assembly-release/pom.xml", "it-selenium/pom.xml"]
missing = [name for name in expected if not (root / name).is_file()]
if missing:
    sys.exit("Missing expected files; no changes made: " + ", ".join(missing))
# Match complete version element contents, not prefixes of other versions.
pattern = re.compile(r"(?P<start><version>\s*)" + re.escape(args.old)
                     + r"(?P<end>\s*</version>)")
changes = []
for name in expected:
    path = root / name
    original = path.read_bytes()
    text = original.decode("utf-8")
    updated, count = pattern.subn(lambda m: m['start'] + args.new + m['end'], text)
    if count:
        changes.append((path, updated.encode("utf-8")))
    print(f"{'update' if args.write else 'would update'} {name}: {count} version element(s)")
if args.write:
    for path, content in changes:
        path.write_bytes(content)
result = subprocess.run(["git", "grep", "-l", "-I", "-F", "--", args.old],
                        cwd=root, capture_output=True, text=True)
if result.returncode not in (0, 1):
    sys.exit(result.stderr)
others = [name for name in result.stdout.splitlines() if name not in expected]
print("Other tracked references to review manually:")
print("\n".join(others) if others else "(none)")
print("Review dependency version elements, release notes and packaging inputs manually.")
print("The signing script takes runtime version/RC inputs; it is not rewritten.")
print("Review git diff." if args.write else "Dry run; pass --write to apply.")
PY
