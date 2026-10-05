package app.exceptions;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ApiException extends RuntimeException {
    @Getter
    private int code;

    public ApiException(int code, String msg){
        super(msg);
        this.code = code;
    }

    public static ApiException notFound(String entity, Object id){
        return new ApiException(404, entity + " with id " + id + " not found");
    }
}