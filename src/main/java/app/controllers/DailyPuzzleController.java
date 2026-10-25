package app.controllers;

import app.dtos.games.MoveRequestDTO;
import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.dtos.puzzles.PuzzleMoveResponseDTO;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.security.AccessRoleName;
import app.services.DailyPuzzleService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class DailyPuzzleController implements EndpointGroup {
    private final DailyPuzzleService dailyPuzzleService;

    public DailyPuzzleController(DailyPuzzleService dailyPuzzleService) {
        this.dailyPuzzleService = dailyPuzzleService;
    }

    @Override
    public void addEndpoints() {
        get("/api/v1/puzzles/daily", this::getTodaysPuzzle, AccessRoleName.USER, AccessRoleName.ADMIN);
        post("/api/v1/puzzles/daily/moves", this::playMove, AccessRoleName.USER);
    }

    public void getTodaysPuzzle(Context ctx){
        DailyPuzzleResponseDTO response = dailyPuzzleService.getTodaysPuzzle();
        ctx.status(HttpStatus.OK);
        ctx.json(response);
    }

    public void playMove(Context ctx){
        MoveRequestDTO move = ctx.bodyValidator(MoveRequestDTO.class)
                .check(dto -> dto.from() != null && !dto.from().isBlank(), "From position is required")
                .check(dto -> dto.to() != null && !dto.to().isBlank(), "To position is required")
                .get();

        UserVerifiedPasswordDTO user = ctx.attribute("user");

        PuzzleMoveResponseDTO moveResponse = dailyPuzzleService.playMove(user.id(), move);
        ctx.status(HttpStatus.CREATED);
        ctx.json(moveResponse);
    }
}
