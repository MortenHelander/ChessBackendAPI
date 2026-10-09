package app.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    public static List<String> validate(String email){
        List<String> messages = new ArrayList<>();

        if (email == null || email.isBlank()){
            messages.add("Email is empty");
            return messages;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            messages.add("Email must consist of: (your-email)@(email-provider).(address)");
        }
        return messages;
    }
}
