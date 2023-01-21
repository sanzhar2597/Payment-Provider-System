package kz.ufo.exceptions;

import kz.ufo.dto.ValidationErrorResponse;
import kz.ufo.dto.Violation;
import oracle.jdbc.OracleDatabaseException;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;



@ControllerAdvice
public class ErrorHandlingControllerAdvice {
//    @ResponseBody
//    @ExceptionHandler(GenericJDBCException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public ValidationErrorResponse onGenericExceptionViolation(
//            GenericJDBCException e
//    ) {
//       Violation violation = new Violation();
//       violation.setErrNumber(e.getErrorCode());
//       violation.setMessage(e.getMessage());
//        return new ValidationErrorResponse(violation);
//    }
//
//
//    @ExceptionHandler(OracleDatabaseException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ValidationErrorResponse onMethodArgumentNotValidException(
//            OracleDatabaseException e
//    ) {
//            Violation violation = new Violation();
//            violation.setErrNumber(e.getOracleErrorNumber());
//            violation.setMessage(e.getMessage());
//        return new ValidationErrorResponse(violation);
//    }
}
