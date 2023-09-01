package vn.iotstar.jobhub_hcmute_be.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.iotstar.jobhub_hcmute_be.dto.GenericResponse;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ObjectError> details = ex.getBindingResult().getAllErrors();
        Map<String, String> errors = new HashMap<>();
        details.forEach((error) ->{
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Validation failed for argument")
                .result(errors)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(genericResponse ,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("User don't exist")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Incorrect email or phone")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Not Found")
                .result(ex.getMessage())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> handleAlreadyExistException(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Existed")
                .result(ex.getMessage())
                .statusCode(HttpStatus.CONFLICT.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Your account has not been activated")
                .result(ex.getMessage())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Account doesn't exits or incorrect password")
                .result(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("Invalid token. Please login again!")
                .result(ex.getMessage())
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message("The file size exceeds the maximum upload limit.")
                .result(ex.getMessage())
                .statusCode(HttpStatus.PAYLOAD_TOO_LARGE.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .result(null)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @ExceptionHandler({ RuntimeException.class })
    public ResponseEntity<Object> handleInternal(RuntimeException ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .result("Internal Error")
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(genericResponse,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleBadRequest(Exception ex) {
        GenericResponse genericResponse = GenericResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .result("Bad Requestr")
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .build();


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(genericResponse);
    }


    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                GenericResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .result("Please login again!")
                        .statusCode(HttpStatus.UNAUTHORIZED.value()).build()
        );
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<Object> handleParseException(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                GenericResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .result("Format is not correct!")
                        .statusCode(HttpStatus.BAD_REQUEST.value()).build()
        );
    }
}