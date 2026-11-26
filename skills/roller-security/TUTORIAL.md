# Tutorial: Apache Roller Security Triage with LangGraph

This tutorial walks through using the Obsidian-free, SQLite-backed **LangGraph security triage system** inside [skills/roller-security/](SKILL.md).

By the end, you will know how to:
1. Set up a private triage workspace safely excluded from Git.
2. Drive a new vulnerability report from intake through all **6 human approval gates** to disclosure.
3. Hand off leak-safe `PublicBrief` summaries to `tools/roller-release-graph` for a security release.
4. Run with local-only Ollama models, cloud models, or completely offline (`--mock-llm`).

---

## 1. Mental Model

Everything for a triage year lives in one private folder (e.g., `triage-2026/`):

- **`triage-2026/cases.db`** (`chmod 600`) — SQLite single source of truth holding both structured case metadata/milestones and LangGraph's pause/resume checkpoints.
- **`triage-2026/<NN-slug>/CASE.md`** and **`triage-2026/SUMMARY_BOARD.md`** — Read-only plain GitHub-Flavored Markdown regenerated automatically from SQLite (no Obsidian, no plugins, no emoji/hashtag syntax).
- **`triage-2026/<NN-slug>/{original-report.md, REPRODUCTION.md, IMPLEMENTATION.md, CVE_FORM.md}`** — Your private evidence, technical notes, and portal worksheet.

Each step in the graph uses the cheapest/safest tier needed:
- **Tier 0 (Pure Python/Shell, 0 LLM tokens):** Git-exclusion guard, `check-private.sh`, Maven tests, `git merge-base --is-ancestor`, SQLite sync, Markdown rendering.
- **Tier 1 (Cheap or Local LLM):** Extracting provenance from emails, filling routine email templates, drafting neutral PR text from an **air-gapped `PublicBrief`** (never sees the raw report or CVE ID).
- **Tier 2 (Frontier or ZDR LLM):** Codebase reachability investigation, ASF severity/CVSS rating, and semantic disclosure/diff auditing.

---

## 2. One-Time Setup

### Install dependencies
```bash
cd skills/roller-security

# Core install (works immediately with --mock-llm)
uv sync

# Optional: add real LLM provider SDKs
uv sync --extra all-llms
# or pick just what you use: --extra ollama, --extra google, --extra anthropic, --extra openai
```

### Verify your private workspace is excluded from Git
If your triage folder lives inside the Roller checkout (e.g., `triage-2026/`), add it to `.git/info/exclude` (**not** `.gitignore`):

```bash
echo '/triage-*/' >> ../../.git/info/exclude
git check-ignore -v ../../triage-2026
```

You can also place your triage folder completely outside the repo (e.g., `~/private/roller-triage-2026/`).

---

## 3. End-to-End Walkthrough: Triaging a New Report

To practice safely without touching real files or spending API tokens, try this against a temporary folder with `--dry-run --mock-llm`. (For a real case, drop `--dry-run --mock-llm` and point `--triage-dir` at `../../triage-2026`.)

```bash
mkdir -p /tmp/demo-triage
cat <<'EOF' > /tmp/demo-triage/incoming.eml
From: Reporter <reporter@example.org>
Subject: Potential issue in feed parser
Date: 2026-03-01

Details of the reported behavior...
EOF
```

### Step 1 — Start the case (pauses at Gate 1: Acknowledgement)
```bash
cd skills/roller-security

uv run roller-security start \
  --repo ../.. \
  --triage-dir /tmp/demo-triage \
  --case-id 01 \
  --slug feed-parser \
  --owner dave \
  --report /tmp/demo-triage/incoming.eml \
  --dry-run --mock-llm
```
What happens automatically:
- **Tier 0** verifies `/tmp/demo-triage` is safely outside Git and scaffolds `IMPLEMENTATION.md` + `CVE_FORM.md`.
- **Tier 1** extracts provenance facts and drafts a neutral receipt acknowledgement email (no verdict or fix commitment).
- The graph **pauses at Gate 1** and prints the exact draft + `resume` command.

### Step 2 — Gate 1: Confirm receipt acknowledgement sent
After you review and send the ack email to the reporter:
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"acknowledged": true}'
```
What happens next:
- Marks `acknowledged` done in SQLite and regenerates `CASE.md`.
- **Tier 2** investigates reachability, minimum actor, preconditions, and affected versions using `skills/roller-security/references/roller-codebase-map.md`, separating verified evidence from inference.
- **Pauses at Gate 2** for the PMC's verdict.

### Step 3 — Gate 2: Record PMC decision (`accept` or `reject`)

**If the PMC rejects or closes as duplicate:**
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"decision": "reject", "owner": "dave"}'
```
The graph drafts a verified rejection explanation, marks all downstream milestones `n/a`, updates SQLite + `CASE.md`, and exits cleanly.

**If the PMC accepts:**
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"decision": "accept", "target_release": "7.0.0", "owner": "dave"}'
```
What happens next:
- **Tier 2** rates ASF severity/CVSS and drafts the CVE description using `severity-and-cvss.md` and `cve-portal.md`.
- **Tier 1** populates `CVE_FORM.md` and drafts the acceptance email to the reporter.
- **Pauses at Gate 3** while you request the CVE ID in the ASF Security portal and prepare your fix branch.

### Step 4 — Gate 3: Record CVE ID, neutral branch & tests
Once ASF Security allocates the CVE ID and you've implemented the fix + regression tests on a neutrally named branch:
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"cve_id": "CVE-2026-12345", "branch": "harden-feed-parser", "tests": ["FeedParserTest"]}'
```
What happens next:
- **Tier 0** runs targeted Maven tests, runs `skills/roller-security/scripts/check-private.sh`, and checks SQLite for cross-case overlaps (`shares_code_with`, `blocks`).
- **Tier 2** runs `semantic_disclosure_audit` on the diff. If a leak or overlap risk is detected, the graph loops back to Gate 3 with findings.
- **Tier 1** drafts a neutral PR title/body/commit message from the **air-gapped `PublicBrief` only** (zero access to the raw report or CVE ID) and drafts the reporter review email.
- **Pauses at Gate 4** for reporter review and merge.

### Step 5 — Gate 4: Reporter review & merge
After the reporter reviews the fix/advisory and the PR is merged to the release branch (record the **landed merge/squash commit**, not the PR branch head):
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"reporter_approved": true, "fix_commit": "<merge-sha>", "pr_number": 42, "reporter_deadline": "2026-03-15"}'
```
- **Tier 0** runs `check-private.sh --pr 42` and **pauses at Gate 5** to wait for the fixed Roller release.

### Step 6 — Gate 5: Hand off to `roller-release-graph` & wait for release
Export leak-safe briefs for the release manager (contains **no** CVE IDs, reporter info, or withheld details):
```bash
uv run roller-security export-briefs \
  --target-release 7.0.0 \
  --triage-dir /tmp/demo-triage
```
Once `tools/roller-release-graph` publishes Roller `7.0.0` (`--security`), resume Gate 5:
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"release_tag": "roller-7.0.0"}'
```
- **Tier 0** verifies `git merge-base --is-ancestor <fix_commit> roller-7.0.0`.
- **Tier 1** drafts the reporter notification email with advisory links.
- **Pauses at Gate 6** for coordinated disclosure.

### Step 7 — Gate 6: Disclose, verify archives & close
After advancing the ASF CVE portal record, confirming delivery in the mailing list archives, and emailing the reporter directly:
```bash
uv run roller-security resume 2026-01-feed-parser \
  --triage-dir /tmp/demo-triage \
  --json '{"announcement_url": "https://lists.apache.org/thread/...", "advisory_archive_urls": ["https://lists.apache.org/thread/..."], "closed": true}'
```
The graph verifies HTTP 200 on the archive URLs, marks `announced` and `complete` in SQLite, regenerates `CASE.md` and `SUMMARY_BOARD.md`, and finishes.

---

## 4. Day-to-Day Commands

```bash
# Terminal status board + next actions + release gates + consistency checks
uv run roller-security board --triage-dir ../../triage-2026

# Highlight your own cases or filter by target release
uv run roller-security board --triage-dir ../../triage-2026 --mine dave --release 7.0.0

# Emit a plain GitHub-Flavored Markdown board table
uv run roller-security board --triage-dir ../../triage-2026 --markdown

# Inspect a paused case and see its exact next gate payload
uv run roller-security status 2026-01-feed-parser --triage-dir ../../triage-2026

# List all checkpointed threads in cases.db
uv run roller-security list --triage-dir ../../triage-2026

# Render the graph topology (Mermaid, PNG, or ASCII)
uv run roller-security graph --format mermaid
uv run roller-security graph --format png -o security-graph.png
```

---

## 5. Choosing Your LLM & Privacy Mode

| Mode | How to Run | Where Private Data Goes |
| :--- | :--- | :--- |
| **Offline / Dry-Run** | `--dry-run --mock-llm` | Nowhere (zero API calls, zero writes) |
| **Local-Only (Ollama)** | `--local-llm` + `ROLLER_SEC_LOCAL_MODEL="ollama:qwen2.5:14b"` | Stays 100% on your machine for all private-context nodes |
| **Cloud Tiered (ZDR)** | `ROLLER_SEC_CHEAP_MODEL="anthropic:claude-sonnet-5-5"`<br>`ROLLER_SEC_FRONTIER_MODEL="anthropic:claude-opus-5-5"` | Sonnet 5.5 for dull formatting; Opus 5.5 only for investigation, severity, and diff audit |

---

## 6. Interactive Visual Debugging (LangGraph Studio)

To inspect nodes, edges, and paused states in a browser:

```bash
cd skills/roller-security
uv run --with "langgraph-cli[inmem]" python -m langgraph_cli dev
```

Open the printed Studio URL and select `roller_security_case`.

---

## 7. Safety & Troubleshooting Notes

- **Backing up `triage-2026/` on macOS:** Always use `cp -a triage-2026 ~/private/triage-2026-backup` (not `cp -r`) so symlinks are preserved cleanly.
- **Database location precedence:** `--db <path>` > `ROLLER_SECURITY_DB` env var > `<triage-dir>/cases.db` > `~/.local/state/roller-security/cases.db`.
- **Never commit private data:** Periodically run `bash skills/roller-security/scripts/check-private.sh` from the repo root to confirm `triage-*/` and `cases.db` remain excluded and untracked.
