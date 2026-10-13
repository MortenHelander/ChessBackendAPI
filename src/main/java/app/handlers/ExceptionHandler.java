package app.handlers;

import app.exceptions.ApiException;
import app.gameengine.exceptions.InvalidGameActionException;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExceptionHandler {

    public void configureExceptionHandling(JavalinConfig config){
        config.routes.exception(ApiException.class, (e, ctx) -> {
            if (e.getCode() >= 500) {
                log.error("Server error: {}", e.getMessage());
            } else {
                log.warn("Client error ({}): {}", e.getCode(), e.getMessage());
            }
            ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
        });

        config.routes.exception(ValidationException.class, (e, ctx) -> {
            String messages = e.getErrors().values().stream()
                    .flatMap(List::stream)
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", messages);
            ctx.status(400).json(Map.of("error", messages));
        });

        config.routes.exception(InvalidGameActionException.class, (e, ctx) -> {
            log.warn("Invalid game action: {}", e.getMessage());
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        config.routes.exception(Exception.class, (exception, ctx) -> {
            log.error("Unhandled exception", exception);
            ctx.status(500).json(Map.of("error", "Internal server error"));
        });
    }
}
