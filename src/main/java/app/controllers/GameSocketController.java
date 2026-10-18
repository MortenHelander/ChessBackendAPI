package app.controllers;

import app.websocket.GameConnectionRegistry;
import app.dtos.games.GameEventDTO;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import app.security.AccessRoleName;
import app.security.TokenService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsErrorContext;
import lombok.extern.slf4j.Slf4j;

import static io.javalin.apibuilder.ApiBuilder.ws;

@Slf4j
public class GameSocketController implements EndpointGroup {
    private final TokenService tokenService;
    private final GameConnectionRegistry registry;

    public GameSocketController(TokenService tokenService, GameConnectionRegistry gameConnectionRegistry){
        this.tokenService = tokenService;
        this.registry = gameConnectionRegistry;
    }

    @Override
    public void addEndpoints() {
        ws("/api/v1/games/{id}/ws", ws -> {
            ws.onConnect(this::onConnect);
            ws.onClose(this::onClose);
            ws.onError(this::onError);
        }, AccessRoleName.ANYONE);
    }

    public void onConnect(WsContext wsContext){
        Integer gameId = wsContext.pathParamAsClass("id", Integer.class).get();
        String token = wsContext.queryParamAsClass("token", String.class).get();

        UserVerifiedPasswordDTO user;
        try{
            user = tokenService.verify(token);
        }catch (ApiException e){
            wsContext.closeSession(1008, "Invalid or expired token");
            return;
        }
        wsContext.enableAutomaticPings();
        registry.join(gameId, wsContext);
        registry.broadcast(gameId, new GameEventDTO("PLAYER_JOINED", user.username()));
    }

    public void onClose(WsContext wsContext){
        Integer gameId = wsContext.pathParamAsClass("id", Integer.class).get();
        registry.leave(gameId, wsContext);
    }

    public void onError(WsErrorContext wsContext){
        Integer gameId = wsContext.pathParamAsClass("id", Integer.class).get();
        log.error("WebSocket error in game {}", gameId, wsContext.error());
        registry.leave(gameId, wsContext);
    }
}
