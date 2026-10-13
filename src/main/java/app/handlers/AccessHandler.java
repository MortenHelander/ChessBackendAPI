package app.handlers;

import app.security.TokenService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;

import java.util.Set;
import java.util.stream.Collectors;

public class AccessHandler {
    private TokenService tokenService;

    public AccessHandler(TokenService tokenService){
        this.tokenService = tokenService;
    }

    public void authenticate(Context ctx) {
//        // This is a preflight request => no need for authentication
//        if (ctx.method().toString().equals("OPTIONS")) {
//            ctx.status(200);
//            return;
//        }
//        // If the endpoint is not protected with roles or is open to ANYONE role, then skip
//        Set<String> allowedRoles = ctx.routeRoles().stream().map(role -> role.toString().toUpperCase()).collect(Collectors.toSet());
//        if (isOpenEndpoint(allowedRoles))
//            return;
//
//        // If there is no token we do not allow entry
//        UserDTO verifiedTokenUser = validateAndGetUserFromToken(ctx);
//        ctx.attribute("user", verifiedTokenUser); // -> ctx.attribute("user") in ApplicationConfig beforeMatched filter
    }

    public void authorize(Context ctx) {
//        Set<String> allowedRoles = ctx.routeRoles()
//                .stream()
//                .map(role -> role.toString().toUpperCase())
//                .collect(Collectors.toSet());
//
//        // 1. Check if the endpoint is open to all (either by not having any roles or having the ANYONE role set
//        if (isOpenEndpoint(allowedRoles))
//            return;
//        // 2. Get user and ensure it is not null
//        UserDTO user = ctx.attribute("user");
//        if (user == null) {
//            throw new ForbiddenResponse("No user was added from the token");
//        }
//        // 3. See if any role matches
//        if (!userHasAllowedRole(user, allowedRoles))
//            throw new ForbiddenResponse("User was not authorized with roles: " + user.getRoles() + ". Needed roles are: " + allowedRoles);
    }

//    private boolean isOpenEndpoint(Set<String> allowedRoles) {
//        // If the endpoint is not protected with any roles:
//        if (allowedRoles.isEmpty())
//            return true;
//
//        // 1. Get permitted roles and Check if the endpoint is open to all with the ANYONE role
//        if (allowedRoles.contains("ANYONE")) {
//            return true;
//        }
//        return false;
//    }
//
//    private static boolean userHasAllowedRole(UserDTO user, Set<String> allowedRoles) {
//        return user.getRoles().stream()
//                .anyMatch(role -> allowedRoles.contains(role.toUpperCase()));
//    }
}
