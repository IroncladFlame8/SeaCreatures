# Contributing to SeaCreatures

Thanks for your interest in improving SeaCreatures! This document explains how to propose changes, follow the project's conventions, and prepare pull requests.

## Table of Contents
1. Code of Conduct
2. Project Setup
3. Development Workflow
4. Versioning & Releases
5. Code Style & Conventions
6. Commit Messages
7. Adding / Modifying Sea Creatures
8. Testing Guidelines
9. Opening Pull Requests
10. Issue Reporting
11. Security
12. License

---
## 1. Code of Conduct
Be respectful. Harassment, discrimination, and toxic behavior are not tolerated. If no formal CoC is present, default to the spirit of the [Contributor Covenant](https://www.contributor-covenant.org/).

## 2. Project Setup
Requirements:
- Java 21 (Temurin recommended)
- Maven 3.8+
- Paper 1.21.x server for runtime testing

Build:
```bash
mvn clean package
```
Resulting shaded jar: `target/SeaCreatures-<version>.jar`.

Run (drop jar into server `plugins/` folder and start Paper).

## 3. Development Workflow
1. Fork the repository.
2. Create a feature branch: `feat/<short-description>` or `fix/<short-description>`.
3. Make changes with small, logical commits.
4. Rebase (avoid merge commits) before opening a PR: `git fetch origin && git rebase origin/main`.
5. Ensure the project builds: `mvn -q -DskipTests package`.
6. Open a pull request (PR) describing the change, motivation, and testing done.

## 4. Versioning & Releases
- Versions follow `MAJOR.MINOR.PATCH[-SNAPSHOT]`.
- Never bump to a stable release (no `-SNAPSHOT`) in a random feature PR unless it’s the designated release PR.
- After a release (e.g. `1.1.0`), increment to the next snapshot (e.g. `1.2.0-SNAPSHOT`).
- CI automatically tags `v<version>` and creates a GitHub Release. `-SNAPSHOT` builds become prereleases.

## 5. Code Style & Conventions
- Use existing formatting style (minimal changes to unrelated whitespace).
- Prefer clear naming: `SeaCreatureManager`, `FishingListener` etc.
- Avoid using CraftBukkit/NMS internals for forward compatibility.
- Limit external dependencies—plugin should stay lightweight.
- Avoid static singletons; use dependency injection via constructors when possible.

### Null Handling
- Use early returns when validation fails.
- Do not introduce Optional for simple internal flows.

### Logging
- Use plugin logger (`plugin.getLogger()`).
- Keep debug logging behind config-driven verbosity when appropriate.

## 6. Commit Messages
Format (recommended):
```
<type>(scope): short summary

Optional detailed body explaining motivation & context.
```
Types (examples): `feat`, `fix`, `refactor`, `docs`, `build`, `chore`.
Examples:
```
feat(fishing): add fling mechanic when spawning creatures
fix(config): correct default base-percent value
```

## 7. Adding / Modifying Sea Creatures
- Add entries under `creatures:` in `config.yml`.
- If you introduce new creature properties, ensure:
  - Parsing logic added to `SeaCreatureManager.parse`
  - Field added to `SeaCreatureDefinition`
  - Documentation updated in README & sample config
  - Sensible defaults maintain backward compatibility

## 8. Testing Guidelines
Manual tests (suggested):
- Fish with and without Luck of the Sea; verify chance adjustments.
- Kill spawned creatures; confirm XP message & value.
- Edge cases: invalid config entries (should warn, not crash) and empty creature list (should not throw).

If unit tests are introduced later, prefer lightweight tests for parsing logic.

## 9. Opening Pull Requests
Checklist before submitting:
- [ ] Builds successfully (`mvn clean package`)
- [ ] Only intended files changed (no stray IDE files)
- [ ] README/config docs updated if behavior changed
- [ ] No excessive debug logging left in
- [ ] Version bumped only if appropriate

## 10. Issue Reporting
Include:
- Server version (`/version` output)
- Plugin version (release tag or snapshot)
- Steps to reproduce
- Relevant config excerpt
- Stack trace if applicable

## 11. Security
This project is low-surface (no network I/O), but report any exploit (privilege escalation, command injection) privately first if possible.

## 12. License
By contributing, you agree that your contributions are licensed under the MIT License (see `LICENSE`).

---
Thank you for contributing!

