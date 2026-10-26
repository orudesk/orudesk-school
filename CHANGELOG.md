# Changelog

Every release of Orudesk School, newest first ([Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
[Semantic Versioning](https://semver.org/spec/v2.0.0.html)). Each release names the core release it is built on.

## [Unreleased]

### Added

- `web/`: the school edition's web app, the core's screens plus Students: the list by class, admitting a child
  with up to three guardians at the school picked in the switcher (one primary contact; the server's messages
  under each field), and a student's page with their guardians. Officers given the role at one school see it.
- Admissions are recorded in the core's audit log ("Admitted Anika Nair to class 5 (no. 2026/001)"); the app
  includes the core's `orudesk-audit` module.
- The edition's OpenAPI contract, saved by `OpenApiContractTest` to `build/openapi/orudesk-school.json`; CI
  checks the web app's copy is up to date.

### Changed

- The contract says 201 Created for `POST /api/v1/admissions`, and is sorted and indented.
- The database pool waits at most 5 s for a connection (fail fast with a 503 when the database is down).

## [1.0.0] - 2026-10-26

Release 1, built on Orudesk core 1.0.0.

### Added

- Admissions: admit a student with their guardians, at one school (org unit); admissions officers see only their own school.
- Docker image `ghcr.io/orudesk/orudesk-school`, and `docker/compose.yaml` to run the edition with its database.
