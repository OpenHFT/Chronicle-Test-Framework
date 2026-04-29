# CLAUDE.md

CTF-specific notes for Claude Code. House rules and Javadoc/PR conventions live in `AGENTS.md`; do not duplicate them here.

## What is unique to this repo

- PRs target the `ea` branch, not `main`.
- Surefire is configured by the parent POM with `forkCount=4` and `reuseForks=true`. System properties can be set per test class but not per method.
- Run `mvn -P quality clean verify` to execute Checkstyle and SpotBugs locally (the profile lives in this module's `pom.xml`).
- Reference docs live under `src/main/docs/` (currently `project-requirements.adoc`).

## Build commands

```bash
mvn -q verify                    # default build and tests
mvn -q test -Dtest=ClassName     # single test class
mvn -P quality clean verify      # with static analysis
```
