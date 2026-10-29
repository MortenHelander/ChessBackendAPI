# Chess Backend API – Endpoint Documentation

Every endpoint of the API: what it expects, what it returns, and which status codes and error messages it can give.

---

## General

### Base URL

| Environment | REST | WebSocket |
|---|---|---|
| Local | `http://localhost:7070/api/v1` | `ws://localhost:7070/api/v1` |
| Deployed | `https://cheaoss.mortenhelander.dk/api/v1` | `wss://cheaoss.mortenhelander.dk/api/v1` |

All paths below are relative to the base URL.

### Content type

Request and response bodies are JSON. Send `Content-Type: application/json` on requests that have a body.

Dates are ISO strings: `"2026-10-29"` for a date, `"2026-10-29T14:30:00.123456"` for a date and time.

### Authentication

1. Log in with `POST /auth/login` to get a JWT.
2. Send it on protected endpoints as a header:

```
Authorization: Bearer <token>
```

The WebSocket takes the token as a query parameter instead (`?token=<token>`), because browsers can't set headers on a WebSocket connection.

### Roles

| Role | Who | Notes |
|---|---|---|
| `ANYONE` | everybody, no token needed | |
| `USER` | every registered user | given on registration |
| `ADMIN` | administrators | can't be obtained through the API |

The **Access** line on each endpoint says which roles may call it.

### Error format

Every error has the same shape:

```json
{ "error": "Illegal move" }
```

### Status codes used

| Code | Meaning in this API |
|---|---|
| `200 OK` | request succeeded, body contains the result |
| `201 Created` | something was created (user, game, move, puzzle move) |
| `204 No Content` | deleted, no body |
| `400 Bad Request` | invalid input: validation errors, illegal moves, wrong turn, finished game |
| `401 Unauthorized` | no token, a malformed `Authorization` header, an invalid or expired token, or a wrong password at login |
| `403 Forbidden` | logged in, but not allowed: wrong role, not a player in the game, or not your profile |
| `404 Not Found` | the user, game or stats don't exist, or the route doesn't exist |
| `409 Conflict` | already exists (username, email, puzzle attempt), or two requests changed the same game/attempt at the same time |
| `500 Internal Server Error` | unexpected server failure; the body is always `{"error": "Internal server error"}` and the details are only in the server log |
| `502 Bad Gateway` | Lichess could not be reached or sent unusable data |

### Errors that can happen on any protected endpoint

| Code | `error` | When |
|---|---|---|
| `401` | `Unauthorized` | no `Authorization` header, or it doesn't start with `Bearer `, or the token is blank |
| `401` | `Invalid or expired token` | the token is invalid, tampered with or expired |
| `403` | `Forbidden` | the token is valid, but the user doesn't have the required role |
| `400` | `ID must be positive` | a path `{id}` that is 0 or negative |
| `400` | (Javalin validation message) | a path `{id}` that isn't a number |

---

## Data types

### User (`UserResponseDTO`)

```json
{
  "id": 1,
  "username": "mohel",
  "email": "morten@example.com",
  "firstName": "Morten",
  "userStatsDTO": {
    "id": 1,
    "gamesPlayed": 0,
    "wins": 0,
    "losses": 0,
    "draws": 0,
    "mmr": 500.0
  }
}
```

### Stats (`UserStatsDTO`)

```json
{ "id": 1, "gamesPlayed": 3, "wins": 2, "losses": 1, "draws": 0, "mmr": 500.0 }
```

`id` is the user's id. `mmr` is stored but not calculated yet.

### Game (`GameResponseDTO`)

```json
{
  "id": 7,
  "startedAt": "2026-10-29T14:30:00.123456",
  "gameMode": "CLASSIC",
  "gameStatus": "IN_PROGRESS",
  "winnerColor": "NO_COLOR",
  "endReason": null,
  "isWhitesTurn": true,
  "players": [
    { "id": 13, "username": "mohel", "color": "WHITE", "isAi": false },
    { "id": 14, "username": "anderson", "color": "BLACK", "isAi": false }
  ]
}
```

### Move (`MoveResponseDTO`)

```json
{
  "id": 21,
  "moveNumber": 1,
  "uci": "e2e4",
  "playedAt": "2026-10-29T14:31:02.554310",
  "from": "E2",
  "to": "E4",
  "givesCheck": false
}
```

`uci` is the move in UCI notation: from-square + to-square, plus the promotion letter for a promotion (`e7e8q`).

### Enum values

| Field | Values |
|---|---|
| `gameMode` | `CLASSIC`, `FUN`, `TRAINING` |
| `gameStatus` | `IN_PROGRESS`, `CHECKMATE`, `DRAW`, `RESIGNED` |
| `winnerColor`, `color` | `WHITE`, `BLACK`, `NO_COLOR` (no winner: game in progress or a draw) |
| `endReason` | `CHECKMATE`, `STALEMATE`, `RESIGNATION`, `INSUFFICIENT_MATERIAL`, `FIFTY_MOVE_RULE`, `THREEFOLD_REPETITION`; `null` while the game is in progress |
| puzzle `status` | `IN_PROGRESS`, `SOLVED`, `FAILED` |
| `promotionLetter` | `q` (queen), `r` (rook), `b` (bishop), `n` (knight) |

---

## Auth

### Register

`POST /auth/register` · **Access:** anyone

Creates an account with the `USER` role and empty stats.

**Request**

```json
{
  "username": "mohel",
  "firstName": "Morten",
  "lastName": "Helander",
  "email": "morten@example.com",
  "password": "Str0ng!Passw0rd",
  "passwordCheck": "Str0ng!Passw0rd"
}
```

**Validation rules**

| Field | Rule |
|---|---|
| `username` | 3–16 characters, letters and digits only |
| `firstName`, `lastName` | 2–50 characters, letters only |
| `email` | `name@provider.domain` |
| `password` | at least 8 characters, at most 72 UTF-8 bytes, upper and lower case letters, at least one digit, at least one special character |
| `passwordCheck` | must equal `password` |

**Responses**

| Code | Body |
|---|---|
| `201` | the new user (see *User*) |
| `400` | all validation errors joined with `, `, e.g. `"Username must be between 3 and 16 characters long, Passwords don't match"` |
| `409` | `Username is taken` or `Email is taken` |

### Log in

`POST /auth/login` · **Access:** anyone

**Request**

```json
{ "username": "mohel", "password": "Str0ng!Passw0rd" }
```

**Responses**

| Code | Body |
|---|---|
| `200` | `{ "id": 1, "username": "mohel", "token": "eyJhbGciOi...", "roles": ["USER"] }` |
| `400` | `Username is required` or `Password is required` |
| `401` | `Invalid username or password` (the same message for an unknown user and a wrong password) |

### Health check

`GET /auth/healthcheck` · **Access:** anyone

**Responses**

| Code | Body |
|---|---|
| `200` | the text `{"msg": "API is up and running"}`, sent as a JSON string |

---

## Users

### Get all users

`GET /users` · **Access:** anyone

| Code | Body |
|---|---|
| `200` | list of users (see *User*) |

### Get a user by id

`GET /users/{id}` · **Access:** anyone

| Code | Body |
|---|---|
| `200` | the user |
| `400` | `ID must be positive` |
| `404` | `User with id {id} not found` |

### Get a user by username

`GET /users/username/{username}` · **Access:** anyone

| Code | Body |
|---|---|
| `200` | the user |
| `404` | `User with username {username} not found` |

### Get a user's stats

`GET /users/{id}/stats` · **Access:** user

Stats are updated automatically when a game ends by checkmate, a draw or resignation.

| Code | Body |
|---|---|
| `200` | the stats (see *Stats*) |
| `400` | `ID must be positive` |
| `404` | `User-stats with id {id} not found` |

### Update a user

`PUT /users/{id}` · **Access:** user (only their own profile), admin (any profile)

**Request** (all four fields are required; same rules as registration)

```json
{
  "username": "mohel",
  "firstName": "Morten",
  "lastName": "Helander",
  "email": "morten.new@example.com"
}
```

Changing your own username makes your current token stop matching your profile for later updates; log in again afterwards.

| Code | Body |
|---|---|
| `200` | the updated user |
| `400` | validation errors joined with `, ` |
| `403` | `You can only update your own profile` |
| `404` | `User with id {id} not found` |
| `409` | `Username is taken` or `Email is taken` |

### Delete a user

`DELETE /users/{id}` · **Access:** admin

| Code | Body |
|---|---|
| `204` | no body |
| `403` | `Forbidden` (not an admin) |
| `404` | `User with id {id} not found` |

---

## Games

### Create a game

`POST /games` · **Access:** user

**Request**

```json
{ "whiteUserId": 1, "blackUserId": 2, "gameMode": "CLASSIC" }
```

| Code | Body |
|---|---|
| `201` | the new game (see *Game*): `IN_PROGRESS`, white to move |
| `400` | `No player 1 connected`, `No player 2 connected`, `Player 1 and player 2 cannot be the same` or `No valid game mode chosen` |
| `404` | `User with id {id} not found` (one of the players doesn't exist) |

### Get all games

`GET /games` · **Access:** user

| Code | Body |
|---|---|
| `200` | list of games |

### Get a game

`GET /games/{id}` · **Access:** user

| Code | Body |
|---|---|
| `200` | the game |
| `400` | `ID must be positive` |
| `404` | `Game with id {id} not found` |

### Delete a game

`DELETE /games/{id}` · **Access:** admin

| Code | Body |
|---|---|
| `204` | no body |
| `403` | `Forbidden` (not an admin) |
| `404` | `Game with id {id} not found` |

### Get a game's moves

`GET /games/{id}/moves` · **Access:** user

| Code | Body |
|---|---|
| `200` | list of moves (see *Move*), ordered by `moveNumber` |
| `400` | `ID must be positive` |
| `404` | `Game with id {id} not found` |

### Make a move

`POST /games/{id}/moves` · **Access:** user (only a player in the game, on their own turn)

**Request** (`promotionLetter` only when a pawn reaches the last rank)

```json
{ "from": "e2", "to": "e4", "promotionLetter": null }
```

After the move, the server checks in this order whether the game is over: checkmate, stalemate, insufficient material, the fifty-move rule and threefold repetition. If it is, the game's `gameStatus`, `winnerColor` and `endReason` are set and both players' stats are updated. Fetch the game to see the result.

On success, every WebSocket connection to the game receives a `MOVE_MADE` event.

| Code | Body |
|---|---|
| `201` | the move (see *Move*); `givesCheck` is `true` if the move puts the opponent in check |
| `400` | `From position is required` or `To position is required` |
| `400` | `Invalid position: {value}` (not a square like `e2`) |
| `400` | `Game is finished` |
| `400` | `Not your turn` |
| `400` | `No piece on selected square` |
| `400` | `You can't move your opponent's pieces` |
| `400` | `Illegal move` |
| `400` | `Promotion piece required` |
| `400` | invalid promotion letter (anything other than `q`, `r`, `b`, `n`) |
| `403` | `You are not a player in this game` |
| `404` | `Game with id {id} not found` |
| `409` | another move on the same game was saved at the same moment; only the first is kept |

### Resign

`POST /games/{id}/resign` · **Access:** user (only a player in the game; allowed on either player's turn)

No request body.

On success, every WebSocket connection to the game receives a `GAME_RESIGNED` event, and both players' stats are updated.

| Code | Body |
|---|---|
| `200` | the game, with `gameStatus` `RESIGNED`, `endReason` `RESIGNATION` and the opponent as `winnerColor` |
| `400` | `ID must be positive` |
| `400` | `Game is finished` |
| `403` | `You are not a player in this game` |
| `404` | `Game with id {id} not found` |
| `409` | the game was changed by another request at the same moment |

---

## Daily puzzle

Today's puzzle comes from the Lichess daily puzzle. A background job fetches it at startup and every day at 00:05 UTC, and it is saved per date (UTC). The first request of the day fetches it if the job hasn't yet.

### Get today's puzzle

`GET /puzzles/daily` · **Access:** user, admin

The solution is never sent.

| Code | Body |
|---|---|
| `200` | see below |
| `502` | `Lichess returned no puzzle` |
| `502` | `Lichess returned a puzzle without a solution` |
| `502` | `Lichess returned an invalid puzzle FEN: {details}` |
| `502` | `Could not reach Lichess: {details}` |
| `502` | `Lichess responded with {http status}` |
| `502` | `Interrupted while calling Lichess` |

```json
{
  "date": "2026-10-22",
  "fen": "2r1b3/1p2kpQ1/p3p3/4r3/3R4/P2B4/1P3PPP/2R3K1 b - - 0 1",
  "rating": 1964,
  "lastMove": "a1c1",
  "isWhitesTurn": false,
  "movesToSolve": 5
}
```

- `fen` – the starting position, already after the opponent's `lastMove`
- `isWhitesTurn` – the colour the user plays
- `movesToSolve` – how many moves the user has to find

### Play a puzzle move

`POST /puzzles/daily/moves` · **Access:** user

Plays a move in today's puzzle. The user's first move creates their attempt; each user has one attempt per puzzle with 10 tries.

**Request**

```json
{ "from": "c8", "to": "c1", "promotionLetter": null }
```

**Rules**

- A **correct** move is answered with the opponent's reply in `opponentMove`. On the last move there is no reply and the status becomes `SOLVED`.
- **Any checkmate** counts as solved, even if it isn't the move Lichess lists.
- A **legal but wrong** move costs one try. After the 10th wrong move the status becomes `FAILED`.
- An **illegal** move is rejected with `400` and doesn't cost a try.

| Code | Body |
|---|---|
| `201` | see below |
| `400` | `From position is required` or `To position is required` |
| `400` | `Invalid position: {value}` |
| `400` | `Puzzle already SOLVED` or `Puzzle already FAILED` |
| `400` | `No piece on selected square` |
| `400` | `Not your piece` |
| `400` | `Illegal move` |
| `400` | `Promotion piece required` |
| `404` | `User with id {id} not found` (the user behind the token no longer exists) |
| `409` | the same user's attempt was created or changed by another request at the same moment |
| `502` | the same Lichess errors as *Get today's puzzle* (if the puzzle isn't saved yet) |

```json
{
  "isCorrect": true,
  "opponentMove": "d3f1",
  "status": "IN_PROGRESS",
  "attemptsLeft": 10,
  "movesLeft": 4
}
```

- `isCorrect` – whether the move was accepted
- `opponentMove` – the reply in UCI notation, or `null` (wrong move, or puzzle solved)
- `attemptsLeft` – tries remaining
- `movesLeft` – user moves still to find

---

## WebSocket

### Live game updates

`WS /games/{id}/ws?token=<jwt>` · **Access:** user (token in the query string)

Receive-only: moves and resignations are sent over HTTP, and the server pushes the results to every connection in the game.

**Connecting**

- A valid token: the connection stays open and everyone in the game receives a `PLAYER_JOINED` event.
- A missing, invalid or expired token: the server closes the connection with code `1008` and reason `Invalid or expired token`.

**Messages from the server** all have the form `{ "type": "...", "payload": ... }`:

| `type` | `payload` | Sent when |
|---|---|---|
| `PLAYER_JOINED` | the username (string) | someone connects to the game |
| `MOVE_MADE` | the move (see *Move*) | a move was accepted through `POST /games/{id}/moves` |
| `GAME_RESIGNED` | the game (see *Game*) | a player resigned through `POST /games/{id}/resign` |

Example:

```json
{
  "type": "MOVE_MADE",
  "payload": {
    "id": 21,
    "moveNumber": 1,
    "uci": "e2e4",
    "playedAt": "2026-10-29T14:31:02.554310",
    "from": "E2",
    "to": "E4",
    "givesCheck": false
  }
}
```

A move that ends the game by checkmate or a draw is sent as a normal `MOVE_MADE`; fetch the game to see its new status.
