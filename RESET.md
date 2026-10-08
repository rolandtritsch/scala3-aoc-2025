# RESET.md — Reset / Restart the AoC Solver 2025 Demo

This documents how to reset the demo to its starting state so the
[AoC Solver 2025](../companies/aoc-solver-2025) agent company starts over
with **Day 01**: the ProblemGatherer fetches the Day 01 problem + input,
then solver → submitter take it from there.

## What "reset" means

1. `scala3-aoc-2025` repo contains only **Day00** (template) again.
2. Paperclip (local docker container) is up and healthy.
3. `AOC_SESSION` secret holds a **working** Advent of Code session cookie (might need to be refreshed).
4. The CTO heartbeat is **enabled**.
5. The board has **no leftover Day01 issues**, so the CTO starts at Day 01.
6. The CTO is kicked once; the ProblemGatherer picks up Day 01.

## Prerequisites

- Docker running.
- `paperclipai` CLI installed (`~/.local/bin/paperclipai`).
- Board auth for the **local** instance exists in `~/.paperclip/auth.json`
  under the `http://localhost:3100` key (created by the first browser login).
  All `curl` commands below use it:
  ```bash
  TOKEN=$(python3 -c "import json; print(json.load(open('$HOME/.paperclip/auth.json'))['credentials']['http://localhost:3100']['token'])")
  API=http://localhost:3100
  COMPANY=fb2e8844-5e85-4830-8327-94c95aab789f   # AoC Solver 2025 (local)
  CTO=b5669920-1d7c-4af6-8036-d1d9267c7358
  GATHERER=61c64b0c-90d8-434c-baca-1a4126e2d4c1
  ```
  If the company/agent IDs differ on your instance, re-list them:
  ```bash
  paperclipai company list --api-base $API
  paperclipai agent list --api-base $API --company-id $COMPANY
  ```

## Step 1 — Delete all days (keep Day00) from the repo

Per-day files (here `NN = 01`, repeat for every solved day):

| File | Day01 instance |
| ---- | -------------- |
| Problem text | `problems/DayNNProblem.txt` |
| Personal input | `src/main/resources/inputs/DayNN.txt` |
| Sample input | `src/main/resources/inputs/DayNNTest.txt` |
| Solution | `src/main/scala/aoc2025/DayNN.scala` |
| Tests | `src/test/scala/aoc2025/DayNNTest.scala` |
| Runner block | `DayNN` block in `src/main/scala/aoc2025/Main.scala` |

Commands (run in `scala3-aoc-2025`):

```bash
git rm -q problems/Day01Problem.txt \
  src/main/resources/inputs/Day01.txt \
  src/main/resources/inputs/Day01Test.txt \
  src/main/scala/aoc2025/Day01.scala \
  src/test/scala/aoc2025/Day01Test.scala
```

Revert `src/main/scala/aoc2025/Main.scala` to the Day00-only runner:

```scala
package aoc2025

@main
def solve() =

  val input00 = Day00.readFile("inputs/Day00.txt")
  println(s"Day00 - part1: ${Day00.part1(input00)}")
  println(s"Day00 - part2: ${Day00.part2(input00)}")

end solve
```

Verify the template is still green, then commit:

```bash
sbt "testOnly aoc2025.Day00Test"   # expect: Passed: Total 8, Failed 0
git add -A
git commit -m "Reset demo: remove Day01, keep Day00 template"
```

> Note: plain `sbt test` currently reports `No tests to run for Test /
> testQuick` (sbt 2 thin-client quirk); `testOnly` is the reliable check.
> Push to `origin/main` only if the demo agents work from GitHub rather
> than the local bind mount (they use the bind mount by default — see
> Step 3 — so pushing is optional).

## Step 2 — Get a new AOC_SESSION key and rotate it (if necessary)

This is an optional step. It is only needed, if/when the currect session
cookie has expired. Check first. Just curl adventofcode.com and check,
if 'Roland Tritsch' is still logged in.

Advent of Code has no API; the gatherer/submitter authenticate with the
user's login session cookie (see `aoc-gather` / `aoc-submit` skills).

1. Log in at https://adventofcode.com in a browser.
2. Open dev tools → Application/Storage → Cookies → copy the `session`
   cookie value (long hex string). Never commit this value anywhere.
3. Rotate the vault secret (current secret id lookup + rotate):
   ```bash
   SECRET_ID=$(curl -s -H "Authorization: Bearer $TOKEN" \
     $API/api/companies/$COMPANY/secrets \
     | python3 -c "import json,sys; print([s for s in json.load(sys.stdin) if s['key']=='aoc_session'][0]['id'])")
   curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -X POST $API/api/secrets/$SECRET_ID/rotate \
     -d '{"value":"<paste-fresh-session-cookie-here>"}'
   ```
   Alternative: Paperclip UI → company → Secrets → `aoc_session` → rotate.
4. Verify: the secret list should show a fresh `lastRotatedAt`, and both
   ProblemGatherer and ProblemSubmitter reference it as `AOC_SESSION`:
   ```bash
   curl -s -H "Authorization: Bearer $TOKEN" \
     $API/api/companies/$COMPANY/secrets | python3 -m json.tool | grep -E '"key"|lastRotatedAt'
   ```

Troubleshooting: AoC answers gathering with an HTML "puzzled" page or HTTP
400 → the cookie is expired/invalid; repeat this step. Back off on HTTP
errors (minutes, not seconds); never re-download inputs unnecessarily.

## Step 3 — Start the Paperclip docker container

Normal case (container already exists, e.g. after a reboot):

```bash
docker start docker-paperclip-1
curl -sf http://localhost:3100/api/health   # expect {"status":"ok",...}
```

Verify the Scala toolchain the solver needs is present (asdf installs live
on the `/paperclip` volume; the image only wires the shims onto `PATH`):

```bash
docker exec docker-paperclip-1 sh -c \
  'cd /workspaces/scala3-aoc-2025 && java -version 2>&1 | head -1 && sbt --version 2>&1 | tail -1'
# expect: openjdk version "25" ... + an sbt launcher line
```

If `java: not found` (fresh volume, or installs never ran), install once as
the runtime user from the repo's `.tool-versions`:

```bash
docker exec -u node -e HOME=/paperclip -e ASDF_DATA_DIR=/paperclip/.asdf docker-paperclip-1 sh -c \
  'export PATH="/paperclip/.asdf/bin:/paperclip/.asdf/shims:$PATH" && cd /workspaces/scala3-aoc-2025 && asdf install'
```

The container is configured with (see `docker inspect docker-paperclip-1`):

- Image `docker-paperclip` (built from the `paperclip` repo), port `3100:3100`.
- Binds:
  - `paperclip/data/docker-paperclip:/paperclip` (rw — all instance data),
  - `scala3-aoc-2025:/workspaces/scala3-aoc-2025` (rw — the agents edit this checkout),
  - `companies:/workspaces/companies` (ro).
- Env: `HOST=0.0.0.0`, `PAPERCLIP_HOME=/paperclip`,
  `PAPERCLIP_DEPLOYMENT_MODE=authenticated`,
  `PAPERCLIP_DEPLOYMENT_EXPOSURE=private`,
  `OPENROUTER_API_KEY=<secret>`, `BETTER_AUTH_SECRET=<secret>`.

If the container is gone, recreate it (generate fresh secrets first):

```bash
cd ../paperclip && docker build -t docker-paperclip .
docker run -d --name docker-paperclip-1 --pids-limit 2048 \
  -p 3100:3100 \
  -e HOST=0.0.0.0 -e PAPERCLIP_HOME=/paperclip \
  -e PAPERCLIP_DEPLOYMENT_MODE=authenticated \
  -e PAPERCLIP_DEPLOYMENT_EXPOSURE=private \
  -e PAPERCLIP_PUBLIC_URL=http://localhost:3100 \
  -e OPENROUTER_API_KEY="<key>" \
  -e BETTER_AUTH_SECRET="$(openssl rand -hex 32)" \
  -v "$HOME/Development/Home/paperclip/data/docker-paperclip:/paperclip" \
  -v "$HOME/Development/Home/scala3-aoc-2025:/workspaces/scala3-aoc-2025:rw" \
  -v "$HOME/Development/Home/companies:/workspaces/companies:ro" \
  docker-paperclip
```

If you then face an empty instance (no company/agents/secrets), re-import:

```bash
npx paperclipai company import ./aoc-solver-2025   # run from companies/
```

and re-create the `AOC_SESSION` (+ `OPENROUTER_API_KEY`) secrets per Step 2.

## Step 4 — Enable the CTO heartbeat

The CTO heartbeat starts **paused** by design; enable it explicitly:

```bash
# Inspect current state
curl -s -H "Authorization: Bearer $TOKEN" $API/api/agents/$CTO \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['runtimeConfig'])"
# Enable (intervalSec 21600 = 6h, as configured for this demo)
curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -X PATCH $API/api/agents/$CTO \
  -d '{"runtimeConfig":{"heartbeat":{"enabled":true,"intervalSec":21600}}}'
```

Equivalent in the UI: Agents → CTO → Configuration → heartbeat → enabled.

## Step 5 — Clear old Day01 board state

If previous Day01 issues are still `done`, the CTO considers Day 01 finished
and would skip ahead. Delete them so the loop restarts at Day 01:

```bash
export PB="--api-base http://localhost:3100"
curl -s -H "Authorization: Bearer $TOKEN" \
  $API/api/companies/$COMPANY/issues \
  | python3 -c "import json,sys; [print(x['identifier'],x['status'],x['title']) for x in json.load(sys.stdin)]"
for id in AOCA-1 AOCA-2 AOCA-3; do paperclipai issue delete $PB --yes $id; done
```

(Adjust the identifiers to whatever the issue list shows.)

## Step 6 — Kick the CTO; ProblemGatherer gathers Day01

The 6h timer won't fire during a demo, so invoke the CTO heartbeat once:

```bash
paperclipai agent heartbeat:invoke --api-base $API $CTO
```

Then verify (≈ 2–5 min with the free-tier model):

```bash
# A new issue appears, e.g. AOCA-4 "Fetch day 01 problem and input"
curl -s -H "Authorization: Bearer $TOKEN" \
  $API/api/companies/$COMPANY/issues \
  | python3 -c "import json,sys; [print(x['identifier'],x['status'],x['title']) for x in json.load(sys.stdin)]"
# The ProblemGatherer run is active ...
curl -s -H "Authorization: Bearer $TOKEN" \
  $API/api/companies/$COMPANY/heartbeat-runs \
  | python3 -c "import json,sys; [print(r['agentId'][:8],r['status'],r['invocationSource']) for r in json.load(sys.stdin)][:5]"
# ... and files re-appear in the repo
git -C . status --short   # expect: ?? problems/ ?? src/main/resources/inputs/Day01.txt
```

From here the pipeline runs itself: gather → solve (tested, committed) →
submit (accepted) → next day, one day at a time, part-1 only.

## Quick-reference (cheat sheet)

```bash
# 1. repo
git rm -q problems/Day01Problem.txt src/main/resources/inputs/Day01.txt \
  src/main/resources/inputs/Day01Test.txt src/main/scala/aoc2025/Day01.scala \
  src/test/scala/aoc2025/Day01Test.scala
# + revert Main.scala to Day00-only, then:
sbt "testOnly aoc2025.Day00Test" && git add -A && git commit -m "Reset demo: remove Day01, keep Day00 template"
# 2. fresh AoC cookie -> POST /api/secrets/<aoc_session-id>/rotate {"value":"<cookie>"}
# 3. container
docker start docker-paperclip-1 && curl -sf http://localhost:3100/api/health
# 4. heartbeat
curl -s -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -X PATCH $API/api/agents/$CTO -d '{"runtimeConfig":{"heartbeat":{"enabled":true,"intervalSec":21600}}}'
# 5. clear board
for id in AOCA-1 AOCA-2 AOCA-3; do paperclipai issue delete --api-base $API --yes $id; done
# 6. kick CTO, watch gatherer
paperclipai agent heartbeat:invoke --api-base $API $CTO
```
