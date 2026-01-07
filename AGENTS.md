# AGENTS.md

## Scope
- Chronicle Test Framework is a Maven-based Java library.
- Sources live in `src/main/java`, tests in `src/test/java`, docs in `src/main/adoc`.
- Multi-release module info lives in `src/main/java11/module-info.java.txt`.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -Dtest=ClassName test -l logs/mvn-test.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Constraints
- Java baseline: 8 (avoid newer language features in main sources).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Treat warnings as defects; keep logs clean.

## Docs and review checklist
- Keep documentation, tests, and code in sync; update `.adoc` when behaviour changes.
- Javadoc must add contracts, edge cases, thread safety, units, or performance notes.
- For large mechanical changes, declare the transformation rule and keep it consistent.

## References
- `src/main/adoc/project-requirements.adoc` and `src/main/adoc/decision-log.adoc`.
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and AsciiDoc conventions.
