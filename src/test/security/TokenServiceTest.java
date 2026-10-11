package security;

import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import app.security.TokenService;
import app.utils.Utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class TokenServiceTest {

    TokenService tokenService;
    String ISSUER = Utils.getPropertyValue("ISSUER", "config.properties");
    long TOKEN_EXPIRE_TIME = Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties"));
    String SECRET_KEY = Utils.getPropertyValue("SECRET_KEY", "config.properties");
    UserVerifiedPasswordDTO testUser;
    Base64.Decoder decoder;
    Base64.Encoder encoder;

    @BeforeEach
    void beforeEach(){
        tokenService = new TokenService(ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
        Set<String> roles = Set.of("User");
        testUser = new UserVerifiedPasswordDTO("mohel", roles);
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

        ApiException ex = assertThrows(ApiException.class, () -> tokenService.verify(forgedToken));
        assertThat(ex.getCode(), is(401));
        assertThat(ex.getMessage(), is("Invalid or expired token"));
    }



    //verify_garbage / null / empty_throws401
    //verify_wrongIssuer_throws401            // 2nd service: same secret, different issuer
    //verify_expiredToken_throws401           // build by hand with JWT.create().withExpiresAt(past)
    //verify_tokenWithoutRoles_throws401      // hand-built: no "roles" claim
    //constructor_rejects: blank secret, short secret, blank issuer, expiration 0 / negative
}
