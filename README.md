# Orudesk School

**The Orudesk edition for schools**: everything in the [Orudesk core](https://github.com/orudesk/orudesk)
(login, roles per school, organisation units, the people registry) plus what schools need, starting with
student admission.

> Status: release 1 (1.0.0), built on Orudesk core 1.0.0.

## Quick start: run it

You need only **Docker**, and the core checked out next to this repository (the image is built from both):

```bash
git clone https://github.com/orudesk/orudesk.git
git clone https://github.com/orudesk/orudesk-school.git
cp orudesk/api/.env.example orudesk-school/api/.env       # then set DB_PASSWORD, ORUDESK_JWT_SECRET and the first admin
docker compose --env-file orudesk-school/api/.env -f orudesk-school/docker/compose.yaml up -d --build --wait
```

Swagger UI: <http://localhost:8081/swagger-ui.html>. Log in, then try `admitStudent`.

## Quick start: develop it

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
