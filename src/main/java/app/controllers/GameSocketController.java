package app.controllers;

import app.config.GameConnectionRegistry;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.security.AccessRoleName;
import app.security.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.websocket.WsContext;

import static io.javalin.apibuilder.ApiBuilder.ws;

public class GameSocketController implements EndpointGroup {
    private TokenService tokenService;
    private GameConnectionRegistry registry;

    public GameSocketController(TokenService tokenService){
        this.tokenService = tokenService;
        this.registry = new GameConnectionRegistry();
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
        }catch (JWTVerificationException e){
            wsContext.closeSession(401, "Invalid or expired token");
            return;
        }
        wsContext.enableAutomaticPings();
        registry.join(gameId, wsContext);
    }

    public void onClose(WsContext wsContext){

    }

    public void onError(WsContext wsContext){

    }
}
