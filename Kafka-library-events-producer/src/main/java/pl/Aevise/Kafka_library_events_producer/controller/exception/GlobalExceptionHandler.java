package pl.Aevise.Kafka_library_events_producer.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.Aevise.Kafka_library_events_producer.controller.exceptions.InvalidBookData;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + " - " + fieldError.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining());

        log.error("Error message : " + errorMessage);

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidBookData.class)
    public ResponseEntity<?> handleException(InvalidBookData e) {
        log.error("Error message : " + e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide the correct Library Event. " + e.getMessage());

    }
}
