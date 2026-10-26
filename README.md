# Orudesk School

**The Orudesk edition for schools**: everything in the [Orudesk core](https://github.com/orudesk/orudesk)
(login, roles per school, organisation units, the people registry) plus what schools need, starting with
student admission.

> Status: early. Expect breaking changes before 1.0.

## Quick start

You need **JDK 25** and **Docker**, and the core checked out next to this repository:

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-school.git
cd orudesk
cp api/.env.example api/.env                       # then edit the values
docker compose --env-file api/.env -f docker/compose.yaml up -d --wait
cp api/.env ../orudesk-school/api/.env
cd ../orudesk-school/api
./gradlew bootRun
```

Swagger UI: <http://localhost:8080/swagger-ui.html>. Create a school (`createOrgUnit`, type `SCHOOL`), give
someone `ADMISSIONS_OFFICER` there (`assignRole` with its `orgUnitId`), then `admitStudent` with the
school's id in the `X-Org-Unit` header.

## Run the tests

```bash
cd api
./gradlew test       # needs Docker
```

## Licence

GNU Affero General Public License v3.0 ([LICENSE](LICENSE)), like the core.
