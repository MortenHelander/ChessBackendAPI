package app.security;

import app.dtos.users.VerifiedUserDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public class JWTokenSecurity {


    public String createToken(VerifiedUserDTO user, String issuer, String expirationTime, String secret) {
        long expirationMillis = Long.parseLong(expirationTime);
        if (expirationMillis <= 0 || issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("Issuer and positive expiration are required");
        }
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.username())
                .withArrayClaim("roles", user.roles().toArray(String[]::new))
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(expirationMillis))
                .sign(signingAlgorithm(secret));
    }

    public boolean tokenIsValid(String token, String secret) {
        Algorithm algorithm = signingAlgorithm(secret);
        try {
            var jwt = JWT.require(algorithm)
                    .withClaimPresence("sub")
                    .withClaimPresence("roles")
                    .withClaimPresence("exp")
                    .build().verify(token);
            if (jwt.getExpiresAtAsInstant() == null) return false;
            getUserWithRolesFromToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public UserDTO getUserWithRolesFromToken(String token) {
        var jwt = JWT.decode(token);
        String username = jwt.getSubject();
        List<String> roles = jwt.getClaim("roles").asList(String.class);
        if (username == null || username.isBlank() || roles == null
                || roles.stream().anyMatch(role -> role == null || role.isBlank())) {
            throw new JWTVerificationException("Token must contain a username and roles");
        }
        return new UserDTO(username, Set.copyOf(roles));
    }



    private Algorithm signingAlgorithm(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("SECRET_KEY must be configured");
        }
        return Algorithm.HMAC256(secret);
    }
}
