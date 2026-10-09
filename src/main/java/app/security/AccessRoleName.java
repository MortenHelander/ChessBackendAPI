package app.security;

import io.javalin.security.RouteRole;

public enum AccessRoleName implements RouteRole {
    ANYONE, USER, ADMIN
}
