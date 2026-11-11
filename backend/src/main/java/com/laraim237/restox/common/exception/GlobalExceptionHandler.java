package com.laraim237.restox.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final String PROP_CODE = "code";
	private static final String PROP_ERRORS = "errors";

	private static final String MSG_INVALID_CREDENTIALS = "Invalid email or password";
	private static final String CODE_INVALID_CREDENTIALS = "INVALID_CREDENTIALS";

	private static final String MSG_ACCOUNT_LOCKED = "Account temporarily locked. Try again later";
	private static final String CODE_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";

	private static final String MSG_ACCOUNT_DISABLED = "Account disabled. Contact your administrator";
	private static final String CODE_ACCOUNT_DISABLED = "ACCOUNT_DISABLED";

	private static final String MSG_VALIDATION_FAILED = "Validation failed";
	private static final String CODE_VALIDATION_FAILED = "VALIDATION_FAILED";

	@ExceptionHandler(AuthenticationException.class)
	public ProblemDetail handleAuthentication() {
		return problem(HttpStatus.UNAUTHORIZED, MSG_INVALID_CREDENTIALS, CODE_INVALID_CREDENTIALS);
	}

	@ExceptionHandler(LockedException.class)
	public ProblemDetail handleLocked() {
		return problem(HttpStatus.FORBIDDEN, MSG_ACCOUNT_LOCKED, CODE_ACCOUNT_LOCKED);
	}

	@ExceptionHandler(DisabledException.class)
	public ProblemDetail handleDisabled() {
		return problem(HttpStatus.FORBIDDEN, MSG_ACCOUNT_DISABLED, CODE_ACCOUNT_DISABLED);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

		ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, MSG_VALIDATION_FAILED, CODE_VALIDATION_FAILED);
		problem.setProperty(PROP_ERRORS, errors);
		return problem;
	}

	private ProblemDetail problem(HttpStatus status, String detail, String code) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setProperty(PROP_CODE, code);
		return problem;
	}
}