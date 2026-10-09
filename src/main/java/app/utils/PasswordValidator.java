package app.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PasswordValidator {

    public static List<String> validate(String password, String passwordCheck){
        List<String> messages = new ArrayList<>();
        if (password == null || password.isBlank()){
            messages.add("Password is empty");
            return messages;
        }
        if (password.length() < 8) {
            messages.add("Password must be at least 8 characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72){
            messages.add("Password must be at most 72 UTF8 bytes");
        }
        if (password.chars().noneMatch(Character::isUpperCase) || password.chars().noneMatch(Character::isLowerCase)){
            messages.add("Password must contain a mixture of upper and lower case letters");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            messages.add("Password must contain at least one number 0-9");
        }
        if (password.chars().allMatch(Character::isLetterOrDigit)) {
            messages.add("Password must contain at least one special character");
        }
        if (!password.equals(passwordCheck)){
            messages.add("Passwords don't match");
        }
        return messages;
    }
}
