package app.controllers;

import app.dtos.games.GameResponseDTO;
import app.dtos.games.MoveResponseDTO;
import app.dtos.games.MoveRequestDTO;
import app.services.GameService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

@Slf4j
public class GameController implements EndpointGroup {
    private GameService gameService;

    public GameController(GameService gameService){
        this.gameService = gameService;
    }


    @Override
    public void addEndpoints() {

        get("/api/v1/games", this::getAllGames);
        get("/api/v1/games/{id}", this::getGame);
        delete("/api/v1/games/{id}", this::deleteGame);

        get("/api/v1/games/{id}/moves", this::getMoves);
        post("/api/v1/games/{id}/moves", this::moveAndShiftTurn);
        post("/api/v1/games/{id}/finish", this::endGame);
    }


    public void getAllGames(Context ctx){
        List<GameResponseDTO> games = gameService.getAllGames();
        ctx.status(HttpStatus.OK);
        ctx.json(games);
    }

    public void getGame(Context ctx){
        Integer id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();
        GameResponseDTO gameResponseDTO = gameService.getGame(id);
        ctx.status(HttpStatus.OK); //200
        ctx.json(gameResponseDTO);
    }

    public void deleteGame(Context ctx){
        Integer id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        gameService.deleteGame(id);
        ctx.status(HttpStatus.NO_CONTENT); //204
    }

    public void getMoves(Context ctx){
        Integer id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        List<MoveResponseDTO> moves = gameService.getMoves(id);
        ctx.status(HttpStatus.OK);
        ctx.json(moves);
    }

    public void moveAndShiftTurn(Context ctx){
        Integer id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        MoveRequestDTO move = ctx.bodyValidator(MoveRequestDTO.class)
                .check(dto -> dto.from() != null && !dto.from().isBlank(), "From position is required")
                .check(dto -> dto.to() != null && !dto.to().isBlank(), "To position is required")
                .get();

        MoveResponseDTO response = gameService.moveAndShiftTurn(id, move);
        ctx.status(HttpStatus.CREATED);
        ctx.json(response);
    }

    public void endGame(Context ctx){
        //to do for when learning about tokens
    }
}
