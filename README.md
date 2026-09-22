# EventShuffle

EventShuffle is a deliberately small event scheduling API inspired by Doodle. It lets a user create an event with candidate dates, retrieve events, collect participant availability, and find dates accepted by every participant.

This repository is also intended as a practice project: the sections below explain the decisions and a repeatable way to build a similar backend assignment.

## Technology choices

- **Java 21** and **Spring Boot 3.4** for a well-supported, production-oriented HTTP application.
- **Spring Web** for REST controllers and JSON serialization.
- **Spring Data JPA** for repository-based persistence.
- **H2 in file mode** so data survives application restarts without requiring a separate database server.
- **Bean Validation** for basic request validation.
- **JUnit/Spring Boot Test** is available for adding unit and HTTP integration tests.

The code uses a simple layered design:

```text
HTTP request
  -> EventController       maps routes and validates request bodies
  -> EventService          owns business rules and result calculation
  -> EventRepository       persists aggregates
  -> H2 database            stores data in ./data/eventshuffle
```

The service layer is intentionally independent of HTTP details. That makes the rules easy to unit test and leaves room to add another adapter, such as a command-line client or messaging consumer.

## Run locally

Prerequisites:

- JDK 21 or newer
- Maven 3.9 or newer

On Windows, verify that both commands are available in a new PowerShell window:

```powershell
java -version
mvn -version
```

If `mvn` is not recognized, Maven is either not installed or its `bin` directory is
not on `PATH`. Install Maven from
https://maven.apache.org/download.cgi, extract it, and add its `bin` directory to
the Windows `Path` environment variable. Alternatively, with
[Chocolatey](https://chocolatey.org/):

```powershell
choco install maven
```

Restart IntelliJ IDEA and PowerShell after changing `PATH`.

Start the application:

```powershell
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Opening `http://localhost:8080/` shows a small JSON health/info response. The first run creates `data/eventshuffle.mv.db`; do not delete that file if you want to preserve data between launches.

Build and test:

```powershell
mvn test
```

Generate browsable HTML Javadoc:

```powershell
mvn javadoc:javadoc
```

Open `target/site/apidocs/index.html` in a browser. The Javadoc covers the
public application classes, REST operations, DTO contracts, persistence
boundaries, and business-service methods.

The project can also be opened directly in IntelliJ IDEA and started by running `EventShuffleApplication`.
In IntelliJ IDEA, use **File > Project Structure > SDK** to select JDK 21, then
open `src/main/java/org/example/EventShuffleApplication.java` and click the
green run icon next to `main`. This does not require the `mvn` command to be
available in PowerShell, although IntelliJ still needs to import the Maven
dependencies.

The controller declares path-variable names explicitly, for example
`@PathVariable("id")`. This keeps routing reliable even when a compiler does
not retain Java method parameter names.

### IntelliJ HTTP Client

The following requests cover every endpoint. Open a `.http` scratch file in
IntelliJ IDEA, paste the requests, and click the green run icon beside a
request. After creating an event, replace the example `1` in later requests
with the `id` returned by the create request.

```http
### Service status
GET http://localhost:8080/

### List events
GET http://localhost:8080/api/v1/event/list

### Create event
POST http://localhost:8080/api/v1/event
Content-Type: application/json

### Show event (replace 1 with the returned id)
GET http://localhost:8080/api/v1/event/1

### Update event
PUT http://localhost:8080/api/v1/event/1
Content-Type: application/json

### Add or replace a participant vote
POST http://localhost:8080/api/v1/event/1/vote
Content-Type: application/json

### Show dates suitable for everyone
GET http://localhost:8080/api/v1/event/1/results

### Delete event
DELETE http://localhost:8080/api/v1/event/1
```

## API

### List events

```http
GET /api/v1/event/list
```

Example response:

```json
{
  "events": [
    { "id": 1, "name": "Jake's secret party" }
  ]
}
```

### Create an event

```http
POST /api/v1/event
Content-Type: application/json
```

```json
{
  "name": "Jake's secret party",
  "dates": ["2026-10-01", "2026-10-05", "2026-10-12"]
}
```

The response is `201 Created`:

```json
{ "id": 1 }
```

### Show an event

```http
GET /api/v1/event/{id}
```

The response includes every offered date. A date with no votes has an empty `people` array.

### Update an event

```http
PUT /api/v1/event/{id}
Content-Type: application/json
```

The request body has the same shape as event creation. The event ID remains
unchanged and the response contains the updated event.

### Delete an event

```http
DELETE /api/v1/event/{id}
```

The event, candidate dates, and participant votes are deleted. The endpoint
uses the event ID because update and delete operate on one specific resource.

### Add or replace a participant's votes

```http
POST /api/v1/event/{id}/vote
Content-Type: application/json
```

```json
{
  "name": "Dick",
  "votes": ["2026-10-01", "2026-10-05"]
}
```

The response is the updated event. Posting again for the same participant replaces their previous vote rather than creating duplicate people.

### Show dates suitable for everyone

```http
GET /api/v1/event/{id}/results
```

```json
{
  "id": 1,
  "name": "Jake's secret party",
  "suitableDates": [
    {
      "date": "2026-10-01",
      "people": ["John", "Dick"]
    }
  ]
}
```

With no participants, `suitableDates` is empty. A date is suitable only when every recorded participant selected it.

## Validation and error behavior

- Event names and participant names must not be blank.
- An event must have at least one date.
- Duplicate dates in a request are normalized while retaining their first-seen order.
- A vote may only contain dates offered by that event.
- Unknown event IDs return `404`.
- Invalid request data returns `400` with `{ "error": "..." }`.

Dates use ISO-8601 `yyyy-MM-dd` JSON strings and are stored as `LocalDate`.

## Persistence model

The file-backed H2 database is configured in
`src/main/resources/application.properties`:

```text
./data/eventshuffle.mv.db
```

The `Event` entity stores the event name and candidate dates. `EventVote`
stores one participant's selected dates, and the relationship is cascaded from
the event. Hibernate's `ddl-auto=update` creates or updates the local schema
for this practice project. For production, use explicit versioned migrations
instead.

## Project structure

```text
src/main/java/org/example
├── EventShuffleApplication.java  Spring Boot entry point
├── ApiController.java            root service-info endpoint
└── event
    ├── EventController.java      REST routes and request validation
    ├── EventService.java         business rules and calculations
    ├── EventRepository.java      Spring Data persistence boundary
    ├── Event.java                event JPA entity
    ├── EventVote.java            participant vote JPA entity
    ├── EventDtos.java             API request/response records
    └── ApiExceptionHandler.java  JSON error mapping
```

Tests are under `src/test/java`. The current unit tests focus on the most
important business rule: a suitable date must be selected by every participant,
and a participant cannot vote for a date that the event did not offer.

## Request lifecycle

1. Spring maps an HTTP request to `EventController`.
2. Bean Validation checks required request fields.
3. The controller delegates to `EventService`.
4. The service loads or changes the `Event` aggregate through `EventRepository`.
5. A DTO is serialized as JSON and returned to the client.
6. Expected validation and domain errors are converted by `ApiExceptionHandler`.

## How to build a similar assignment

1. **Define the API contract first.** Write the routes, request bodies, response bodies, status codes, and validation rules before choosing persistence. This prevents the database model from accidentally becoming the public API.
2. **Model the aggregate around the use cases.** An event owns its candidate dates and participant votes. Keeping these under one `EventService` makes the “suitable for everyone” rule explicit.
3. **Separate transport, business logic, and persistence.** Controllers should translate HTTP to method calls; services should enforce rules; repositories should load and save data. This separation keeps each class small and replaceable.
4. **Choose persistence that satisfies the actual requirement.** An in-memory list would pass a single process run but fail the restart requirement. File-backed H2 is lightweight for an assignment while still demonstrating a real database boundary. A larger deployment could switch to PostgreSQL by changing configuration and migrations.
5. **Make edge cases deliberate.** Decide what happens for missing events, duplicate dates, duplicate participant submissions, invalid vote dates, and an event with no participants. Document those choices and test them.
6. **Design for growth without overengineering.** Versioned routes, DTOs, a service boundary, repository abstraction, and transaction boundaries leave space for authentication, pagination, delete/update operations, and a production database later.
7. **Explain trade-offs in the README.** An interviewer should be able to run the project quickly and understand why the architecture looks the way it does.

## Possible production extensions

- Replace H2 with PostgreSQL and manage schema changes using Flyway or Liquibase.
- Add authentication and authorization so only event owners can modify events.
- Add optimistic locking to protect concurrent vote updates.
- Add pagination to the event listing endpoint.
- Add OpenAPI documentation and contract tests.
- Add structured logging, metrics, and container deployment.
- Add a unique participant identity instead of using a display name as the replacement key.

AI tools were used while creating this practice implementation. The final design, behavior, and documentation should still be reviewed and explained by the candidate.
