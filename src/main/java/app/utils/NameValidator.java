package app.utils;

import java.util.ArrayList;
import java.util.List;

public final class NameValidator {

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    private NameValidator() {}

    public static List<String> validateFirstName(String firstName) {
        return validate(firstName, "First name");
    }

    public static List<String> validateLastName(String lastName) {
        return validate(lastName, "Last name");
    }

    private static List<String> validate(String name, String fieldName) {
        List<String> messages = new ArrayList<>();
        if (name == null || name.isBlank()) {
            messages.add(fieldName + " is empty");
            return messages;
        }
        String trimmed = name.strip();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            messages.add(fieldName + " must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long");
        }
        if (!trimmed.codePoints().allMatch(Character::isLetter)) {
            messages.add(fieldName + " must consist of only letters");
        }
        return messages;
    }
}