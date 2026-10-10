package app.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static String hashPassword(String passwordRaw){
        return BCrypt.hashpw(passwordRaw, BCrypt.gensalt());
    }

    public static boolean checkPassword(String passwordRaw, String passwordHash){
        return BCrypt.checkpw(passwordRaw, passwordHash);
    }
}
