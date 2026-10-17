package app.utils;

import java.util.ArrayList;
import java.util.List;

public class UsernameValidator {

    public static List<String> validate(String username) {
        List<String> messages = new ArrayList<>();
        if (username == null || username.isBlank()) {
            messages.add("Username is empty");
            return messages;
        }
        if (username.length() < 3 || username.length() > 16) {
            messages.add("Username must be between 3 and 16 characters long");
        }
        if (!username.chars().allMatch(Character::isLetterOrDigit)) {
            messages.add("Username must consist of only letters and digits");
        }
        return messages;
    }
}
