package app.controllers;

import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.security.AccessRoleName;
import app.services.DailyPuzzleService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;

public class DailyPuzzleController implements EndpointGroup {
    private final DailyPuzzleService dailyPuzzleService;

    public DailyPuzzleController(DailyPuzzleService dailyPuzzleService) {
        this.dailyPuzzleService = dailyPuzzleService;
    }

    @Override
    public void addEndpoints() {
        get("/api/v1/puzzles/daily", this::getTodaysPuzzle, AccessRoleName.ANYONE);
    }

    public void getTodaysPuzzle(Context ctx){
        DailyPuzzleResponseDTO response = dailyPuzzleService.getTodaysPuzzle();
        ctx.status(HttpStatus.OK);
        ctx.json(response);
    }
}
