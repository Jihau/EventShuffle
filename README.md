# EventShuffle

EventShuffle is a small event scheduling REST API. Users can create events
with candidate dates, collect participant availability, and find dates that
work for everyone.

## Requirements

- Java 21 or newer

## Run the application

Run the packaged JAR from the project root:

```powershell
java -jar dist/EventShuffle-1.0-SNAPSHOT.jar
```

The application runs at `http://localhost:8080`.

## API

### Service status

```http
GET /
```

Returns the service name and the event-list endpoint.

### List events

```http
GET /api/v1/event/list
```

Example response:

```json
{
  "events": [
    {
      "id": 1,
      "name": "Jake's secret party"
    }
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
  "dates": [
    "2026-10-01",
    "2026-10-05",
    "2026-10-12"
  ]
}
```

Returns `201 Created`:

```json
{
  "id": 1
}
```

### Get an event

```http
GET /api/v1/event/{id}
```

Returns the event, its candidate dates, and the participants available on
each date.

### Update an event

```http
PUT /api/v1/event/{id}
Content-Type: application/json
```

Use the same request body as event creation. The event ID is preserved.

### Record participant availability

```http
POST /api/v1/event/{id}/vote
Content-Type: application/json
```

```json
{
  "name": "Dick",
  "votes": [
    "2026-10-01",
    "2026-10-05"
  ]
}
```

Submitting another vote for the same participant replaces their previous
availability.

### Find suitable dates

```http
GET /api/v1/event/{id}/results
```

Returns dates selected by every participant:

```json
{
  "id": 1,
  "name": "Jake's secret party",
  "suitableDates": [
    {
      "date": "2026-10-01",
      "people": [
        "Dick"
      ]
    }
  ]
}
```

If no participants have voted, the result is empty.

### Delete an event

```http
DELETE /api/v1/event/{id}
```

Deletes the event, its candidate dates, and its votes.

## Validation and errors

- Event and participant names must not be blank.
- Events and votes must contain at least one date.
- Dates use ISO-8601 format: `yyyy-MM-dd`.
- Duplicate dates are normalized while preserving their first-seen order.
- Participants can vote only for dates offered by the event.
- A missing event returns `404`.
- Invalid request data returns `400` with an error response:

```json
{
  "error": "..."
}
```

## Persistence

The application uses H2 in file mode. The database is created at:

```text
./data/eventshuffle.mv.db
```

The `data/` directory is local runtime state and is ignored by Git. Delete it
only if you intentionally want to reset the local database.

## Project structure

```text
src/main/java/org/example
├── EventShuffleApplication.java
├── ApiController.java
└── event
    ├── EventController.java
    ├── EventService.java
    ├── EventRepository.java
    ├── Event.java
    ├── EventVote.java
    ├── EventDtos.java
    └── ApiExceptionHandler.java
```

Tests are located under `src/test/java`.
