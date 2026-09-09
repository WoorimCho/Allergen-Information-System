package com.allergen_info_service.bff;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * Turns downstream-call failures and access-control errors into sensible BFF
 * responses. Scoped to the BFF controller so it doesn't touch the app's
 * Thymeleaf error handling.
 */
@RestControllerAdvice(assignableTypes = {ComposedRecipeController.class, RecipeCalculatorController.class})
class BffExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    ProblemDetail notFound(HttpClientErrorException.NotFound ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Recipe not found.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    ProblemDetail downstreamUnavailable(RestClientException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "A catalogue service is unavailable or returned an error.");
        problem.setTitle("Bad Gateway");
        return problem;
    }
}
