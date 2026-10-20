package app.services;

import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import app.security.TokenService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

public class TokenServiceTest {
    String ISSUER = "third-semester-backend-project";
    long TOKEN_EXPIRE_TIME = 1000000;
    String SECRET_KEY = ".JgKf@D;:kqIQG]aoM:vFwf[]W|YS?a{}4[:2D[H-Jv";

    TokenService tokenService;
    UserVerifiedPasswordDTO testUser;
    Base64.Decoder decoder;
    Base64.Encoder encoder;

    @BeforeEach
    void beforeEach(){
        tokenService = new TokenService(ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        Set<String> roles = Set.of("User");
        testUser = new UserVerifiedPasswordDTO(7,"mohel", roles);
        decoder = Base64.getUrlDecoder();
        encoder = Base64.getUrlEncoder().withoutPadding();
    }

    @Test
    void createThenVerify_returnsSameUsernameAndRoles(){
        String token = tokenService.createToken(testUser);
        UserVerifiedPasswordDTO verified = tokenService.verify(token);

        assertThat(testUser.username(), is(verified.username()));
        assertThat(testUser.roles(), is(verified.roles()));
    }

    @Test
    void tokenConstructor_EmptySecret_throwsIllegalArgument(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new TokenService(ISSUER, TOKEN_EXPIRE_TIME, ""));
        assertThat(ex.getMessage(), is("Secret key is empty"));
    }

    @Test
    void tokenConstructor_TooShortSecret_throwsIllegalArgument(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new TokenService(ISSUER, TOKEN_EXPIRE_TIME, "not-at-least-32-chars"));
        assertThat(ex.getMessage(), is("Secret key is too short"));
    }

    @Test
    void tokenConstructor_emptyIssuer_throwsIllegalArgument(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new TokenService("", TOKEN_EXPIRE_TIME, SECRET_KEY));
        assertThat(ex.getMessage(), is("Issuer is empty"));
    }

    @Test
    void tokenConstructor_expirationTimeIsZero_throwsIllegalArgument(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new TokenService(ISSUER, 0, SECRET_KEY));
        assertThat(ex.getMessage(), is("Token expiration time too short"));
    }

    @Test
    void tokenConstructor_expirationTimeIsNegative_throwsIllegalArgument(){
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new TokenService(ISSUER, -360000, SECRET_KEY));
        assertThat(ex.getMessage(), is("Token expiration time too short"));
    }

    @Test
    void verifyTokenFromOtherSecret_throws401(){
        TokenService serviceA = new TokenService("issuer", 60_000, "secret-A-which-is-at-least-32-chars!!");
        TokenService serviceB = new TokenService("issuer", 60_000, "secret-B-which-is-at-least-32-chars!!");
        String token = serviceA.createToken(testUser); // signed with secret A

        ApiException ex = assertThrows(ApiException.class, () -> serviceB.verify(token));
        assertThat(ex.getCode(), is(401));
        assertThat(ex.getMessage(), is("Invalid or expired token"));
    }

    @Test
    void verifyTamperedPayload_throws401(){
        String token = tokenService.createToken(testUser);
        System.out.println(token);
        String[] parts = token.split("\\.");

        String payload = new String(decoder.decode(parts[1]), StandardCharsets.UTF_8);
        String reencoded = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String rebuilt = parts[0] + "." + reencoded + "." + parts[2]; //first reencode original payload to assert
        assertDoesNotThrow(() -> tokenService.verify(rebuilt)); //assert reencoded payload is verified

        assertThat(payload, containsString("User")); //verify original role is user

        payload = payload.replace("User", "Admin"); //most likely type of tampered tokens

        //rebuild the token with the tampered payload
        parts[1] = encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String forgedToken = parts[0] + "." + parts[1] + "." + parts[2];

        invalidTokenCall(forgedToken);
    }

    @Test
    void verifyBlankUsername_throws401(){
        UserVerifiedPasswordDTO emptyUser = new UserVerifiedPasswordDTO(1,"", new HashSet<>(Set.of("User")));
        String token = tokenService.createToken(emptyUser);
        invalidTokenCall(token);
    }

    @Test
    void verifyNullToken_throws401(){
        invalidTokenCall(null);
    }

    @Test
    void verifyBlankToken_throws401(){
        invalidTokenCall("");
    }

    @Test
    void verifyWrongIssuer_throws401(){
        TokenService serviceA = new TokenService("issuer", 60_000, "secret-A-which-is-at-least-32-chars!!");
        TokenService serviceB = new TokenService("wrong-issuer", 60_000, "secret-A-which-is-at-least-32-chars!!");
        String token = serviceA.createToken(testUser); // signed with secret A

        ApiException ex = assertThrows(ApiException.class, () -> serviceB.verify(token));
        assertThat(ex.getCode(), is(401));
        assertThat(ex.getMessage(), is("Invalid or expired token"));
    }

    @Test
    void verifyExpiredToken_throws401(){
        Instant now = Instant.now();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(testUser.username())
                .withArrayClaim("roles", testUser.roles().toArray(String[]::new))
                .withIssuedAt(now)
                .withExpiresAt(Instant.now().minusSeconds(60))
                .sign(Algorithm.HMAC256(SECRET_KEY));

        invalidTokenCall(token);
    }

    @Test
    void verifyTokenWithoutRoles_throws401(){
        Instant now = Instant.now();
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withSubject(testUser.username())
                    // missing ArrayClaim that checks roles
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(TOKEN_EXPIRE_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));

        invalidTokenCall(token);
    }

    private void invalidTokenCall(String token){
        ApiException ex = assertThrows(ApiException.class, () -> tokenService.verify(token));
        assertThat(ex.getCode(), is(401));
        assertThat(ex.getMessage(), is("Invalid or expired token"));
    }
}
