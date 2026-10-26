package app.testutils;

import app.dtos.users.UserVerifiedPasswordDTO;
import app.security.TokenService;
import app.utils.Utils;

import java.util.Set;

public final class TestTokens {

    private static final TokenService TOKEN_SERVICE = new TokenService(
            Utils.getPropertyValue("ISSUER", "config.properties"),
            Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties")),
            Utils.getPropertyValue("SECRET_KEY", "config.properties"));

    private TestTokens() {
    }

    public static String userToken() {
        return TOKEN_SERVICE.createToken(new UserVerifiedPasswordDTO(4, "testuser", Set.of("USER")));
    }

    public static String adminToken() {
        return TOKEN_SERVICE.createToken(new UserVerifiedPasswordDTO(5, "testadmin", Set.of("ADMIN")));
    }

    public static String userAndAdminToken(){
        return TOKEN_SERVICE.createToken(new UserVerifiedPasswordDTO(6, "testadminanduser", Set.of("USER", "ADMIN")));
    }

    public static String userWithStatsToken() {
        return TOKEN_SERVICE.createToken(new UserVerifiedPasswordDTO(2, "svg-man", Set.of("USER")));
    }

    public static String tokenFor(Integer id, String username) {
        return TOKEN_SERVICE.createToken(new UserVerifiedPasswordDTO(id, username, Set.of("USER")));
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
