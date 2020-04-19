package symphony.bm.services.registry.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import symphony.bm.generics.exceptions.RequestProcessingException;
import symphony.bm.generics.jeep.response.JeepMicroserviceErrorMessage;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class MessageErrorHandler extends ResponseEntityExceptionHandler {
    private final Logger LOG = LoggerFactory.getLogger(MessageErrorHandler.class);
    
    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<JeepMicroserviceErrorMessage> handleRequestBodyConversionException(HttpMessageConversionException e) {
        String error = e.getCause().getCause().getMessage();
        LOG.error("Request Error: " + error, e);
        return buildResponseEntity(new JeepMicroserviceErrorMessage(HttpStatus.BAD_REQUEST, error));
    }
    
    @ExceptionHandler(RequestProcessingException.class)
    protected ResponseEntity<JeepMicroserviceErrorMessage> handleRequestProcessingException(RequestProcessingException e) {
        String error = e.getMessage();
        LOG.error("Request Error: " + error, e);
        return buildResponseEntity(new JeepMicroserviceErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()));
    }
    
    private ResponseEntity<JeepMicroserviceErrorMessage> buildResponseEntity(JeepMicroserviceErrorMessage error) {
        return new ResponseEntity<>(error, error.getStatus());
    }
}
