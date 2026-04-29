# Chronicle-Test-Framework: JUnit 4 + JUnit 5 Compatibility TODO (CTF_TODO.md)

## Goal

Add first-class JUnit 5 support in Chronicle Test Framework (CTF) while keeping existing JUnit 4 consumers working unchanged.

Non-negotiables:
- Keep `net.openhft:chronicle-test-framework` source/binary compatible (no removals, no renames, no signature changes, no behaviour breaks).
- Binary compatibility guarantee: do not move/rename existing published public types (FQCN stability); changes must be additive only.
- Do not force JUnit 5 onto JUnit 4 consumers (or the reverse).
- Keep new APIs optional and additive.

## Binary compatibility guardrails (rules of thumb)

Allowed (usually binary compatible):
- Add new classes in new packages (e.g. `...junit4.*`, `...junit5.*`).
- Add new overloads / methods to existing classes (careful: keep semantics stable).
- Add new interfaces/classes that consumers do not have to implement/extend.

Avoid (commonly binary incompatible for consumers):
- Moving/renaming any existing public/protected class/interface/enum or member (old bytecode links by name).
- Changing any existing method/constructor signature, return type, or visibility.
- Changing class vs interface, changing superclass, or tightening `final`/`abstract` in ways that alter linkage.
- Adding new abstract methods to interfaces or abstract base types that downstream might implement/extend.

If you must refactor internals:
- Keep the old public class where it is and delegate to a new internal implementation (shim, not move).

Enforceability:
- Use the existing binary compatibility check plugin (do not add another); Phase 0 must confirm it runs for the core artefact and fails the build on breaks.

## Target architecture (recommended)

1) Keep `chronicle-test-framework` as the JUnit-agnostic core
- No `org.junit.*` types in main code or public signatures.
- All framework-neutral utilities stay here.

2) Add opt-in integration artefacts
- `net.openhft:chronicle-test-framework-junit4`
  - JUnit 4 adapters (Rules / Statements / optional Runner glue).
  - Depends on `junit:junit` (compile scope for this artefact only).
- `net.openhft:chronicle-test-framework-junit5`
  - JUnit Jupiter adapters (Extensions / annotations / parameter resolvers).
  - Depends on `org.junit.jupiter:junit-jupiter-api` (compile scope for this artefact only).

3) Package naming (avoid collisions; make discovery obvious)
- `net.openhft.chronicle.testframework.junit4.*`
- `net.openhft.chronicle.testframework.junit5.*`

4) Configuration model
- Define a tiny, stable, JUnit-agnostic config model in core:
  - `ChronicleTestConfig` (immutable; builder or simple value object)
  - feature toggles and thresholds (timeouts, retries, thread check mode, etc)
- Adapters map annotations / system properties to `ChronicleTestConfig`.

## Work plan

### Phase 0 - Baseline, inventory, safety rails

Tasks:
- [ ] Record baseline build:
  - From this repo root: `mvn -q clean verify`
  - From the Build-All root: `mvn -q -f Chronicle-Test-Framework/pom.xml clean verify`
- [ ] Snapshot public surface of `chronicle-test-framework`:
  - [ ] Use the existing binary compatibility check plugin; document how to run it and what baseline it compares against
  - [ ] Minimum (if needed for review): list exported packages + public classes/methods
- [ ] Inventory downstream usage (Build-All):
  - [ ] grep for `net.openhft.chronicle.testframework.*` usage
  - [ ] capture patterns and "hidden contracts" relied upon downstream

Deliverables:
- Baseline build is green.
- A repeatable API-compat check exists (automated preferred).
- A usage inventory doc or section exists (links to files/modules using each feature).

Acceptance criteria:
- Any new adapter work can prove it does not add `org.junit.*` to the core artefact.
- Any new core addition is strictly additive and does not change existing semantics.

### Phase 1 - Define feature contracts (parity matrix inputs)

For each "test hygiene" capability, write 1-3 paragraphs:
- what it guarantees
- when it fails (and what exceptions are thrown)
- ordering constraints (setup/teardown; runs on failure? runs on aborted?)
- diagnostics emitted (message format, thread dumps, suppressed exceptions, etc)
- thread-safety / parallel execution assumptions

Candidate feature list (fill in from inventory):
- exception tracking lifecycle (capture + rethrow + suppression rules)
- flaky retry semantics (what is retried; max attempts; backoff; classification)
- thread leak checks / thread dump behaviour
- temporary directory / file lifecycle
- system property scoping
- system.out/err capture (if any)
- GC / memory / JVM controls (if any)
- timeouts and polling helpers
- resource leak checks (if any)
- test name / context propagation (for logging)

Deliverables:
- Parity matrix draft (see template below) populated with "current behaviour" notes.
- Decision: which capabilities remain core primitives vs adapter-only glue.

Acceptance criteria:
- Every adapter feature points to an existing core capability OR a new core primitive with a written contract.

### Phase 2 - JUnit 5 (Jupiter) adapters (opt-in)

Design constraints:
- Prefer an extension bundle for easy adoption plus composable extensions for advanced use.
- Keep state in `ExtensionContext.Store` (no static mutable singletons unless necessary).
- Use `Store.CloseableResource` for deterministic cleanup.
- Avoid relying on non-static `@RegisterExtension` unless lifecycle limitations are understood.

Tasks:
- [ ] Create `chronicle-test-framework-junit5` module.
- [ ] Implement core mapping:
  - `@ChronicleTest` (annotation holding configuration; no heavy logic)
  - `ChronicleTestExtension` (bundle; implements `BeforeEach`/`AfterEach`/`TestWatcher` as needed)
- [ ] Implement composable extensions (as required by parity matrix):
  - `ExceptionTrackingExtension`
  - `SystemPropertyScopeExtension`
  - `ThreadLeakCheckExtension`
  - `TempDirExtension` (only if core does not already handle)
  - `FlakyRetryExtension` (careful: see retry policy guidance)
- [ ] Ensure diagnostics are high-signal and stable:
  - consistent prefixes
  - include test unique id / display name when useful
  - include root cause and actionable hints

Deliverables:
- A minimal example snippet for downstream:
  - `@ChronicleTest` on class (or `@ExtendWith` + explicit extensions)
- A small in-repo test suite proving:
  - ordering
  - cleanup on pass/fail/aborted
  - failure message quality

Acceptance criteria:
- No new JUnit dependencies leak into the core artefact.
- A JUnit 5 consumer can adopt with minimal boilerplate.
- Adapter behaviour matches the Phase 1 contracts.

### Phase 3 - JUnit 4 parity adapters (opt-in)

Design constraints:
- Mirror the Jupiter contract semantics as closely as JUnit 4 allows.
- Avoid restricting other Runners unless absolutely necessary.
- Prefer Rules/Statements that can be composed.

Tasks:
- [ ] Create `chronicle-test-framework-junit4` module.
- [ ] Implement:
  - `ChronicleTestRule` (`TestRule`) as the primary entrypoint
  - Optional: `MethodRule` if needed for legacy patterns
  - Optional: Runner glue only if parity cannot be achieved via rules
- [ ] Support a configuration annotation equivalent to `@ChronicleTest` (if useful):
  - `net.openhft.chronicle.testframework.junit4.ChronicleTest`
  - rule reads it via reflection

Deliverables:
- Minimal usage snippet for JUnit 4:
  - `@Rule public final ChronicleTestRule ctf = ChronicleTestRule.create();`
- In-repo tests proving:
  - cleanup on pass/fail
  - behaviour on `@Before`/`@After` failures
  - diagnostics parity with Jupiter where feasible

Acceptance criteria:
- JUnit 4 consumer can adopt without changing test structure.
- No changes required in `chronicle-test-framework` core to support JUnit 4.

### Phase 4 - Build/CI guardrails: "no silent skips"

Problem to avoid:
- Tests compile but do not execute due to provider/engine selection mistakes.

Tasks:
- [ ] Document and enforce surefire approach for mixed estates:
  Option A (preferred for mixed JUnit 4 + 5):
  - Use surefire with JUnit Platform provider and add `junit-vintage-engine` when JUnit 4 tests must run.
  Option B:
  - Separate surefire executions: one for Jupiter, one for JUnit 4 provider.
- [ ] Add CI guardrails:
  - [ ] log check: confirm provider used (`JUnitPlatformProvider` vs surefire-junit4)
  - [ ] test count sanity: fail build if 0 tests discovered unexpectedly
  - [ ] for mixed builds: confirm both JUnit 4 and JUnit 5 tests ran at least once
- [ ] Add a small "consumer simulation" module(s) under the CTF build:
  - one uses JUnit 4 + `chronicle-test-framework-junit4`
  - one uses JUnit 5 + `chronicle-test-framework-junit5`
  - both run in CI to prevent regressions

Deliverables:
- A repeatable build log assertion (grep-able line) and test count thresholds.
- Example POM snippets for each supported strategy.

Acceptance criteria:
- CI makes it hard to accidentally stop running either JUnit 4 or JUnit 5 tests.

### Phase 5 - Docs, examples, adoption guidance

Tasks:
- [ ] Update `README.adoc`:
  - JUnit 4 usage (Rule)
  - JUnit 5 usage (Extension)
  - mixed estate guidance:
    - one framework per file
    - migrate "mixed files" first
    - Vintage strategy if using platform provider
- [ ] Add a short "migration cookbook" section for Build-All modules:
  - recommended order of migration
  - how to keep JUnit 4 stable while adopting JUnit 5

Acceptance criteria:
- Developer can copy/paste minimal snippets for both frameworks.

### Phase 6 - Deprecation strategy (long tail)

Policy:
- Do not remove JUnit 4 support in the short term.
- Once JUnit 5 equivalents exist, deprecate only the JUnit 4 adapter entrypoints (not core).
- Removal only in a major version with a published timeline.

## Parity matrix template (fill in)

| Capability | Current core support | JUnit 4 adapter | JUnit 5 adapter | Notes / edge cases |
|-----------|-----------------------|-----------------|-----------------|--------------------|
| Exception tracking | TBD | TBD | TBD | cleanup ordering, suppression |
| Thread leak checks | TBD | TBD | TBD | parallel execution implications |
| Sysprop scoping | TBD | TBD | TBD | per-test vs per-class |
| Temp dirs/files | TBD | TBD | TBD | delete-on-fail? retain on fail? |
| Flaky retries | TBD | TBD | TBD | when NOT to retry; classification |
| Timeouts/polling | TBD | TBD | TBD | integration with `@Timeout` |
| Output capture | TBD | TBD | TBD | per-test buffering vs pass-through |
| JVM/GC controls | TBD | TBD | TBD | test isolation assumptions |

## Verification checklist (per change)

- `mvn -q clean verify`
- API-compat check:
  - Existing binary compatibility check plugin on `chronicle-test-framework` core artefact
- Consumer simulations:
  - JUnit 4 consumer builds + runs tests
  - JUnit 5 consumer builds + runs tests
- Confirm no `org.junit.*` types appear in core jar bytecode (jdeps or simple constant pool scan).
- Failure diagnostics review:
  - messages are stable, searchable, actionable

## Research topics (best practices and pitfalls)

1) Maven Surefire + JUnit Platform provider selection
- Why: avoid "tests not running" due to provider/engine mismatch.
- Search terms:
  - maven surefire provider selection algorithm
  - surefire junit-platform provider vintage engine dependencies
  - junit-vintage-engine surefire configuration

2) Jupiter extension lifecycle, ordering, and state
- Why: deterministic cleanup and correct ordering around `@BeforeEach`/`@AfterEach` and failures.
- Search terms:
  - JUnit 5 `ExtensionContext.Store` `CloseableResource`
  - relative execution order of user code and extensions
  - `TestWatcher` registration pitfalls and per-test vs per-class lifecycle

3) Parallel execution + global test hygiene
- Why: thread leak checks, sysprop scoping, and shared resources can break under concurrency.
- Search terms:
  - JUnit Jupiter parallel execution configuration parameters
  - `junit.jupiter.execution.parallel.mode.default`
  - `@Execution(CONCURRENT)` resource locks

4) Running JUnit 4 tests under the platform (Vintage) vs separate provider
- Why: Vintage can simplify mixed estates, but can surprise you (engines/providers and parallelism).
- Search terms:
  - junit-vintage-engine requirements
  - mixed junit4 junit5 surefire platform provider
  - junit platform engine filtering include/exclude engine IDs

5) API compatibility automation
- Why: ensure "no public API breaks" is enforced by CI.
- Search terms:
  - net.openhft binary-compatibility-enforcer-plugin configuration and baseline selection
  - underlying japicmp/revapi configuration (if applicable)
  - ignoring intentional changes (approved exclusions / baseline updates)

6) Retry policy for flaky tests
- Why: retries can hide real bugs; should be an escape hatch with clear guidance.
- Search terms:
  - flaky test retry anti-pattern
  - deterministic concurrency testing
  - classify retryable failures

7) Timeouts and polling
- Why: prevent hangs and reduce flakiness in async tests; prefer structured patterns.
- Search terms:
  - JUnit 5 `@Timeout` polling tests
  - await/poll utility patterns

8) JUnit Platform engine rules (if ever considering custom engines)
- Why: engine IDs have restrictions; avoid reserved prefixes.
- Search terms:
  - JUnit Platform `TestEngine` ID restrictions

## Open questions

- Bundle vs composable: do we ship only a single entrypoint per framework, or both?
- Should configuration be annotation-first, system-property-first, or both?
- Do we need a BOM for test dependencies, or rely on existing BOMs?
- If we add a JUnit 4 Runner, how do we avoid clashing with other popular Runners?
- What is the policy for retaining temp dirs or dumps on failure (to aid diagnosis)?
