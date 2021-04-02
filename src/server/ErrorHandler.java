package server;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import api.ApiError;
import api.RequestDBMSException;

@RestControllerAdvice
public class ErrorHandler {

	@ExceptionHandler(RequestDBMSException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleDBException(RequestDBMSException e) {
		return new ApiError(e.getMessage());
	}
}