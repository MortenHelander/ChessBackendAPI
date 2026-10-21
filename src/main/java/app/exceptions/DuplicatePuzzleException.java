package app.exceptions;

public class DuplicatePuzzleException extends ApiException {

    public DuplicatePuzzleException(int code, String message) {
        super(code, message);
    }
}
