package symphony.bm.generics.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import symphony.bm.generics.exceptions.MicroserviceProcessingException;
import symphony.bm.generics.exceptions.RequestProcessingException;
import symphony.bm.generics.exceptions.RestControllerProcessingException;
import symphony.bm.generics.jeep.response.JeepMicroserviceErrorMessage;
import symphony.bm.generics.messages.MicroserviceUnsuccessfulMessage;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestControllerErrorHandler extends ResponseEntityExceptionHandler {
    private final Logger LOG = LoggerFactory.getLogger(RestControllerErrorHandler.class);

    /**
     * For exceptions during Jackson parsing of JSON to POJO
     * @param e
     * @return
     */
    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<JeepMicroserviceErrorMessage> handleRequestBodyConversionException(HttpMessageConversionException e) {
        String error = e.getCause().getCause().getMessage();
        LOG.error("Request Error: " + error, e);
        return buildResponseEntity(new JeepMicroserviceErrorMessage(HttpStatus.BAD_REQUEST, error));
    }

    /**
     * For exceptions during general parsing of JSON to POJO. Commonly thrown from non-JEEP request processing.
     * @param ex
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String error = ex.getCause().getCause().getMessage();
        LOG.error("Request Error: " + error, ex);
        return buildGenericResponseEntity(new MicroserviceUnsuccessfulMessage(error));
//        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * For exceptions in JEEP request processing
     * @param e
     * @return
     */
    @ExceptionHandler(RequestProcessingException.class)
    protected ResponseEntity<JeepMicroserviceErrorMessage> handleRequestProcessingException(RequestProcessingException e) {
        String error = e.getMessage();
        LOG.error("Request Error: " + error, e);
        return buildResponseEntity(new JeepMicroserviceErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    /**
     * For exceptions in non-JEEP microservice request processing
     * @param e
     * @return
     */
    @ExceptionHandler(MicroserviceProcessingException.class)
    protected ResponseEntity<MicroserviceUnsuccessfulMessage> handleMicroserviceProcessingException(MicroserviceProcessingException e) {
        String error = e.getMessage();
        LOG.error("Microservice Processing Exception: " + error, e);
        return buildResponseEntity(new MicroserviceUnsuccessfulMessage(error));
    }

    @ExceptionHandler(RestControllerProcessingException.class)
    protected ResponseEntity<MicroserviceUnsuccessfulMessage> handleRestMicroserviceProcessingException(RestControllerProcessingException e) {
        String error = e.getMessage();
        LOG.error(RestControllerProcessingException.class.getSimpleName() + ": " + error, e);
        return buildResponseEntity(new MicroserviceUnsuccessfulMessage(error), e.getHttpStatus());
    }

    private ResponseEntity<Object> buildGenericResponseEntity(MicroserviceUnsuccessfulMessage msg) {
        return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<MicroserviceUnsuccessfulMessage> buildResponseEntity(MicroserviceUnsuccessfulMessage msg) {
        return new ResponseEntity<>(msg, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<MicroserviceUnsuccessfulMessage> buildResponseEntity(MicroserviceUnsuccessfulMessage msg,
                                                                                HttpStatus httpStatus) {
        return new ResponseEntity<>(msg, httpStatus);
    }

    private ResponseEntity<JeepMicroserviceErrorMessage> buildResponseEntity(JeepMicroserviceErrorMessage error) {
        return new ResponseEntity<>(error, error.getStatus());
    }
}
