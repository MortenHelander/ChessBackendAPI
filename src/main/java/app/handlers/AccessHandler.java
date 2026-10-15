package app.handlers;

import app.dtos.users.UserLoginResponseDTO;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import app.security.AccessRoleName;
import app.security.TokenService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.security.RouteRole;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AccessHandler {
    private TokenService tokenService;

    public AccessHandler(TokenService tokenService){
        this.tokenService = tokenService;
    }

    public void handle(Context ctx){
        if (ctx.method().toString().equals("OPTIONS")) {
            ctx.status(200);
            return;
        }
        Set<RouteRole> routeRoles = ctx.routeRoles();

        if (routeRoles.isEmpty()){
            log.error("Route {} {} has no roles", ctx.method(), ctx.path());
            throw new ApiException(500, "Internal server error");
        }

        if (routeRoles.contains(AccessRoleName.ANYONE)){
            return;
        }
        String token = readBearerToken(ctx);
        UserVerifiedPasswordDTO user = tokenService.verify(token);

        boolean allowed = routeRoles.stream()
                .anyMatch(role -> user.roles().contains(role.toString()));

        if (!allowed){
            throw new ApiException(403, "Forbidden");
        }

        ctx.attribute("user", user);
    }

    private String readBearerToken(Context ctx){
        String header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")){
            throw new ApiException(401, "Unauthorized");
        }
        String token = header.substring(7).trim();
        if (token.isBlank()){
            throw new ApiException(401, "Unauthorized");
        }
        return token;
    }
}
