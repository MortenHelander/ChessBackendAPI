package app.services;

import app.dtos.users.VerifiedUserDTO;
import app.exceptions.ApiException;
import com.auth0.jwt.algorithms.Algorithm;


public class AuthService {
    private final String ISSUER;
    private final Long TOKEN_EXPIRE_TIME;
    private final Algorithm SIGNING_ALGORITHM;

    public AuthService(String ISSUER, Long TOKEN_EXPIRE_TIME, String SECRET_KEY) {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()){
            throw new IllegalArgumentException("Secret key is empty");
        }
        if (SECRET_KEY.length() < 32){
            throw new IllegalArgumentException("Secret key is too short");
        }
        if (ISSUER == null || ISSUER.isBlank()){
            throw new IllegalArgumentException("Issuer is empty");
        }
        if (TOKEN_EXPIRE_TIME <= 0){
            throw new IllegalArgumentException("Token expiration time too short");
        }
        this.ISSUER = ISSUER;
        this.TOKEN_EXPIRE_TIME = TOKEN_EXPIRE_TIME;
        this.SIGNING_ALGORITHM=Algorithm.HMAC256(SECRET_KEY);
    }


    public String createToken(VerifiedUserDTO verifiedUserDTO) {

        return tokenSecurity.createToken(verifiedUserDTO, ISSUER, TOKEN_EXPIRE_TIME, SECRET_KEY);
    }
}
