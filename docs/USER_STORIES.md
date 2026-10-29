# Chess API – User Stories

## How to read this

- **Format:** As a *role*, I want *goal*, so that *benefit*. Acceptance criteria are written as short testable checks, so they translate directly into JUnit or RestAssured tests.
- **Roles:** *Player* (anyone in a game), *Registered user* (has an account), *Spectator* (watches a game), *Developer* (me).
- **Priority (MoSCoW):** Must = needed for a playable classic game over WebSocket. Should = expected for the semester hand-in. Could = nice to have. Won't = parked for now.
- **Status:** set from the code as it currently stands, using one of the four statuses below.

1. Not started: depending on the priority status, may or may not be started depending on time left.
2. In progress: work on the acceptance criteria has begun, the process may or may not be in standby.
3. Has bugs: the task either went from in progress and is now almost "done", but has bugs that needs fixing, or the task went from "done" into "has bugs" after new discovery of errors.
4. Done: code works as intended and is tested thoroughly, might move to "Has bugs" later.

## Epic 1: Classic chess rules

The engine is the strongest part of the project: all 9 stories are done, including castling, en passant, promotion, check detection and every automatic draw rule.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| CR-01 | As a player, I want each piece to move according to the rules, so that the game is real chess. | All six piece types only reach legal squares; pieces cannot jump except the knight; own pieces block, enemy pieces can be captured. | Must | Done |
| CR-02 | As a player, I want moves that leave my king in check to be rejected, so that I cannot make an illegal move by accident. | Move that exposes or keeps own king in check returns 400 "Illegal move"; board and turn are unchanged. | Must | Done |
| CR-03 | As a player, I want to castle on both sides, so that I can use a standard opening plan. | Allowed only if king and rook have not moved, squares between are empty, and king is not in, through or into check; rook lands on the correct square. | Must | Done |
| CR-04 | As a player, I want to capture en passant, so that pawn play follows the full rules. | Only on the move right after the enemy double step; the captured pawn is removed. | Must | Done |
| CR-05 | As a player, I want to choose which piece my pawn promotes to, so that I can underpromote when it matters. | Pawn on last rank requires q, r, b or n; missing letter returns 400; stored UCI ends with the lowercase letter. | Must | Done |
| CR-06 | As a player, I want the game to end at checkmate, so that a winner is declared. | Status becomes CHECKMATE, winner colour is set, further moves return 400 "Game is finished". | Must | Done |
| CR-07 | As a player, I want stalemate to end the game as a draw, so that the result is correct. | Status becomes DRAW with winner NO\_COLOR; GET /games and GET /games/{id} still return 200. | Must | Done |
| CR-08 | As a player, I want to be told when my king is in check, so that the frontend can warn me. | Every move response and board state includes an inCheck flag for the side to move. | Must | Done |
| CR-09 | As a player, I want automatic draws by threefold repetition, the fifty-move rule and insufficient material, so that games cannot go on forever. | Each rule ends the game as DRAW with endReason INSUFFICIENT\_MATERIAL, FIFTY\_MOVE\_RULE or THREEFOLD\_REPETITION; DrawCheckerTest covers each rule. | Should | Done |

## Epic 2: Game lifecycle

A game can be created, played move by move and resigned through the API, and its moves and result are saved. Still open: inviting a friend with a code, the current board as FEN, legal-move hints and draw offers.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| GL-01 | As a player, I want to start a new classic game, so that I can play without anyone touching the database. | POST /games creates the game and both players, returns 201 with the game id; white moves first. | Must | Done |
| GL-02 | As a player, I want to invite a friend with a game code, so that we end up in the same game. | Creator gets a short code; second player joins with it and is assigned the free colour; a full game rejects a third player. | Should | Not started |
| GL-03 | As a player, I want to fetch the current board, so that the frontend can draw it without replaying every move. | Game response includes a FEN string (or a square-to-piece list), side to move and inCheck. | Must | Not started |
| GL-04 | As a player, I want to see the legal moves of a selected piece, so that the frontend can highlight them. | GET /games/{id}/legal-moves?from=e2 returns the squares from MoveValidator.getLegalMoves; empty list if it is not that side's turn. | Should | Not started |
| GL-05 | As a player, I want to resign, so that I can end a lost game. | Status becomes RESIGNED, the opponent is the winner, further moves return 400. Done through POST /games/{id}/resign, and both players' stats update. | Must | Done |
| GL-06 | As a player, I want to offer a draw that my opponent can accept or decline, so that we can agree on a result. | Offer is stored until the next move; accept ends the game as DRAW; a move by the opponent counts as a decline. | Should | Not started |
| GL-07 | As a player, I want to see all moves of a game in order, so that I can review or replay it. | GET /games/{id}/moves returns moves sorted by move number with UCI and timestamp. | Should | Done |

## Epic 3: Real-time play over WebSocket

Players connect to a game over a WebSocket and every legal move is broadcast to both players; the moves themselves are sent over HTTP. Still open: sending the board on connect, game-over messages for checkmate and draws, reconnecting and spectators.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| WS-01 | As a player, I want to connect to my game over a WebSocket, so that I get updates without refreshing. | ws://…/ws/games/{id} accepts the connection; unknown game id closes it with a reason; on connect the client receives the current board. | Must | In progress |
| WS-02 | As a player, I want my move to show up on my opponent's screen right away, so that the game feels live. | Client sends a move message (from, to, promotionLetter); after a legal move every connection in that game receives the new board, last move and whose turn it is. | Must | Done |
| WS-03 | As a player, I want errors to go only to me, so that my opponent is not spammed by my mistakes. | Moves are sent over HTTP, so an illegal move, wrong turn or bad JSON returns an error to the sender only; nothing is broadcast and the board is unchanged. | Must | Done |
| WS-04 | As a player, I want both of us to be notified when the game ends, so that nobody keeps waiting. | Checkmate, stalemate, resignation or agreed draw broadcasts a game-over message with status, winner and reason. | Must | In progress |
| WS-05 | As a player, I want to rejoin after losing my connection, so that a network glitch does not cost me the game. | Reconnecting to the same game sends the full current board; moves made while away are not lost. | Should | Not started |
| WS-06 | As a player, I want to know when my opponent disconnects, so that I know why they are not moving. | On close, the other player receives an opponent-left message; on return, an opponent-back message. | Should | Not started |
| WS-07 | As a spectator, I want to watch a game live, so that I can follow friends' games. | Spectator connection receives all broadcasts; any move message from a spectator is rejected. | Could | Not started |
| WS-08 | As a developer, I want two moves sent at the same moment to be handled safely, so that the database never holds two moves for one turn. | The @Version conflict returns a clear error (409 over HTTP, an error message over WebSocket) instead of 500; only the first move is saved. | Must | Done |

## Epic 4: Users and authentication

Registration, JWT login and role-based access are in place, and only the player of a colour can move or resign for it. Stats update automatically when a game ends; a list of a user's own games still needs a route.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| US-01 | As a visitor, I want to create an account, so that my games and stats are saved to me. | POST /users with username, email and password (min. 8 characters) returns 201; duplicate username or email returns 409; the password is stored hashed. | Must | Done |
| US-02 | As a registered user, I want to log in and receive a token, so that the server knows who I am. | POST /auth/login returns a token on valid credentials and 401 otherwise; protected routes and the WebSocket handshake require the token. | Must | Done |
| US-03 | As a player, I want only myself to be able to move my pieces, so that nobody can play my side. | The server compares the user from the token with the player of that colour; a mismatch returns 403 (an error message over WebSocket). | Must | Done |
| US-04 | As a registered user, I want to see a list of my past and ongoing games, so that I can resume or review them. | GET /users/{id}/games returns the user's games, newest first. GameDAO.getAllGamesByUserId already exists. | Should | In progress |
| US-05 | As a registered user, I want to see my wins, losses and draws, so that I can follow my progress. | Stats update automatically when a game ends; GET /users/{id}/stats returns the totals. | Should | Done |
| US-06 | As a registered user, I want to edit or delete my profile, so that I control my own data. | PUT and DELETE /users/{id} work only for the logged-in user; deleting keeps finished games but anonymises the player. | Could | In progress |

## Epic 5: Fun mode power-ups

The data model is ready (PlayerPowerUp, PowerUpType, PowerUpStatus), but no game logic uses it yet, all logic for power-ups is removed from the current main branch and exists purely in a dev branch. PU-05 matters most: random effects break the replay-based board unless they are stored.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| PU-01 | As a player, I want to start a FUN mode game, so that I can play chess with power-ups. | POST /games with gameMode FUN creates the game; CLASSIC games never hand out power-ups. | Should | Not started |
| PU-02 | As a player, I want instant power-ups to take effect when I earn them, so that the game gets a surprise twist. | KILL\_RANDOM\_PIECE, GAIN\_RANDOM\_PIECE and RANDOMIZE\_BOARD apply at once and are saved as AUTOMATICALLY\_APPLIED; if no valid target exists, saved as AUTOMATICALLY\_DISCARDED. A power-up never removes a king or leaves a side already checkmated. | Should | In progress |
| PU-03 | As a player, I want to hold an AI power-up and choose when to use it, so that I can save it for the right moment. | GOOD\_AI and EVIL\_AI are saved as HELD; using one on my turn plays the AI move and sets USED; it cannot be used twice. | Should | In progress |
| PU-04 | As a player, I want to see what power-up my opponent used and what it changed, so that the board change is not confusing. | Over WebSocket both players receive a power-up message with type, player and affected squares. | Could | Not started |
| PU-05 | As a developer, I want power-up effects saved as events in the move history, so that ReplayHelper rebuilds exactly the same board. | Replaying a FUN game from the database gives the same board as before the restart; covered by a test with a fixed random seed. | Should | Not started |

## Epic 6: External APIs

The Lichess daily puzzle is fetched, saved and playable end to end. In a dev branch the Chess.com client exists and is tested but has no endpoint yet; Stockfish is only prepared through the stored UCI strings.

| ID | User story | Acceptance criteria | Priority | Status |
| --- | --- | --- | --- | --- |
| EX-01 | As a player, I want to solve the Lichess daily puzzle, so that I can practise between games. | GET /puzzles/daily returns today's puzzle without the solution; a scheduler fetches it daily at 00:05 UTC and it is saved in DailyPuzzle; Lichess being down or sending invalid data returns 502. POST /puzzles/daily/moves plays it: a correct move gets the opponent's reply, a wrong move costs one of 10 tries, and any checkmate counts as solved. | Should | Done |
| EX-02 | As a registered user, I want to link my Chess.com username, so that my profile shows my real ratings. | Linking checks that the username exists; rapid, blitz and bullet ratings are shown on the profile; an unknown username returns 404. | Could | In progress |
| EX-03 | As a player, I want to play against a Stockfish AI, so that I can play when no friend is online. | A game can be created with an AI player (Player.isAi); after each human move the server sends the UCI history to Stockfish and plays its reply. GOOD\_AI and EVIL\_AI (PU-03) build on this. | Could | Not started |
