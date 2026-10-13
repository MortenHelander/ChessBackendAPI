package app.security;

import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
public class TokenService {
    private final String issuer;
    private final long expirationMillis;
    private final Algorithm signingAlgorithm;

    public TokenService(String issuer, long expirationMillis, String secretKey) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Secret key is empty");
        }
        if (secretKey.length() < 32) {
            throw new IllegalArgumentException("Secret key is too short");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("Issuer is empty");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("Token expiration time too short");
        }
        this.issuer = issuer;
        this.expirationMillis = expirationMillis;
        this.signingAlgorithm = Algorithm.HMAC256(secretKey);
    }


    public String createToken(UserVerifiedPasswordDTO user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.username())
                .withArrayClaim("roles", user.roles().toArray(String[]::new))
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(expirationMillis))
                .sign(signingAlgorithm);
    }

    public UserVerifiedPasswordDTO verify(String token) {
        try {
            var decodedJwt = JWT.require(signingAlgorithm)
                    .withIssuer(issuer)
                    .build().verify(token);

            String username = decodedJwt.getSubject();
            List<String> roles = decodedJwt.getClaim("roles").asList(String.class);
            if (username == null || username.isBlank() || roles == null
                    || roles.stream().anyMatch(role -> role == null || role.isBlank())) {
                throw new JWTVerificationException("Token must contain a username and roles");
            }
            return new UserVerifiedPasswordDTO(username, Set.copyOf(roles));
        } catch (JWTVerificationException e) {
            log.error("Could not verify token: {}", String.valueOf(e));
            throw new ApiException(401, "Invalid or expired token");
        }
    }
}
