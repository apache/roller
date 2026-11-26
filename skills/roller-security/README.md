# Roller Security Triage Graph (LangGraph)

An Obsidian-free LangGraph state machine and SQLite case tracker built directly into `skills/roller-security/` for the **Apache Roller** private security triage workflow. See [TUTORIAL.md](TUTORIAL.md) for a step-by-step walkthrough.

## Model Tiering & Privacy Air-Gap

| Tier | Where It Runs | Default Model |
| :--- | :--- | :--- |
| **Tier 0 (No LLM)** | Workspace privacy guard, case scaffolding, Maven tests, `check-private`, cross-case overlap checks, git ancestor verification, archive URL checks, SQLite & Markdown views | Pure Python + shell |
| **Tier 1 (Cheap / Local LLM)** | Provenance extraction, email drafts (ack, accept/reject, reporter review, disclosure), air-gapped neutral PR draft | `ROLLER_SEC_CHEAP_MODEL` (`anthropic:claude-sonnet-5-5`, or Ollama with `--local-llm`) |
| **Tier 2 (Frontier LLM)** | Codebase reachability investigation, ASF severity & CVE portal drafting, semantic disclosure audit | `ROLLER_SEC_FRONTIER_MODEL` (`anthropic:claude-opus-5-5`, or Ollama with `--local-llm`) |

**Air-gapped public drafting:** `draft_pr_and_reporter_review` passes **only** the sanitized `PublicBrief` (`neutral_change_summary`, `affected_modules`, `upgrade_guidance`, `branch_name`) with `private_context=False` to draft public PR titles, bodies, and commit messages—never raw reports, CVE IDs, or exploit details.

## Installation

```bash
cd skills/roller-security
uv sync
```

## Quickstart (Dry-Run, No API Keys Needed)

Run a full end-to-end case locally using `--dry-run --mock-llm`:

```bash
# 1. Start a new case (runs workspace guard + ingest/ack, pauses at Gate 1: acknowledge)
uv run roller-security start \
  --repo ../.. \
  --triage-dir /tmp/roller-triage-demo \
  --slug demo-validation \
  --case-id 01 \
  --owner snoopdave \
  --dry-run --mock-llm

# 2. Gate 1 — Confirm acknowledgement sent (runs Tier 2 investigation, pauses at Gate 2: pmc_decision)
uv run roller-security resume 2026-01-demo-validation \
  --json '{"acknowledged": true, "thread_url": "https://lists.apache.org/thread/example"}'

# 3. Gate 2 — Record PMC decision (accept -> severity/CVE form; reject/duplicate/wont-fix -> closes case)
uv run roller-security resume 2026-01-demo-validation \
  --json '{"decision": "accept", "target_release": "7.0.0", "owner": "snoopdave"}'

# 4. Gate 3 — Record CVE ID & private fix branch (runs Maven tests, check-private, disclosure audit)
uv run roller-security resume 2026-01-demo-validation \
  --json '{"cve_id": "CVE-2026-99999", "branch": "fix/validate-weblog-params", "tests": ["org.apache.roller.weblogger.ExampleTest"]}'

# 5. Gate 4 — Record reporter review & merged commit (runs post-merge check-private)
uv run roller-security resume 2026-01-demo-validation \
  --json '{"reporter_approved": true, "fix_commit": "0123456789abcdef0123456789abcdef01234567", "pr_number": 42}'

# 6. Gate 5 — Resume after the release (roller-release skill) is published
uv run roller-security resume 2026-01-demo-validation \
  --json '{"release_tag": "roller-7.0.0"}'

# 7. Gate 6 — Record public disclosure & archive URLs (verifies archives & closes case)
uv run roller-security resume 2026-01-demo-validation \
  --json '{"announcement_url": "https://lists.apache.org/thread/announce", "advisory_archive_urls": ["https://lists.apache.org/thread/advisory"], "closed": true}'
```

## Privacy-First Local LLM Mode (Ollama)

Without `--local-llm`, the raw report and every private-context step go to the
cloud models above. Nothing here checks the provider account's data-retention
terms; use an account the PMC accepts for undisclosed reports.

To keep all private vulnerability reports 100% on your machine, install the `ollama` extra and pass `--local-llm`:

```bash
uv sync --extra ollama
export ROLLER_SEC_LOCAL_MODEL="ollama:qwen2.5:14b"
uv run roller-security start \
  --repo ../.. \
  --triage-dir /path/to/private-triage-dir \
  --slug my-case \
  --local-llm
```

## Status Board & Release Handoff

View the live status board from SQLite (with consistency warnings):

```bash
uv run roller-security board
uv run roller-security board --mine snoopdave --release 7.0.0
uv run roller-security board --markdown
```

Export leak-safe `PublicBrief` JSON for the release notes. Only cases with a committed fix (`fix-committed` or later, with `fix_commit` set) are included:

```bash
uv run roller-security export-briefs --target-release 7.0.0
```

## Visualizing the Graph & LangGraph Studio

Render the workflow graph in Mermaid, ASCII, or PNG:

```bash
uv run roller-security graph --format mermaid
uv run roller-security graph --format ascii
uv run roller-security graph --format png -o roller-security-graph.png
```

Launch in LangGraph Studio (configured via `langgraph.json` pointing to `roller_security.graph:graph`):

```bash
langgraph dev
```

Checkpoints and case records default to `<triage-dir>/cases.db` when `--triage-dir` is passed (override with `--db` or `ROLLER_SECURITY_DB`, fallback `~/.local/state/roller-security/cases.db`).
