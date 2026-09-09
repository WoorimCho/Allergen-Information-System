package com.allergen_info_service.internal;

import com.allergen_info_service.bff.BffPrincipal;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

/**
 * Signs every outbound call to the User service / catalogues with
 * {@link InternalAuth#HEADER}. The acting user is the logged-in
 * {@link BffPrincipal} for the current request, or none (login / anonymous).
 */
public final class InternalAuthClientInterceptor implements ClientHttpRequestInterceptor {

    private final String secret;

    public InternalAuthClientInterceptor(String secret) {
        this.secret = secret;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        Long actingUser = currentAccountId();
        String header = InternalAuth.header(secret, request.getMethod().name(),
                request.getURI().getRawPath(), actingUser);
        request.getHeaders().set(InternalAuth.HEADER, header);
        return execution.execute(request, body);
    }

    private static Long currentAccountId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof BffPrincipal p) {
            return p.accountId();
        }
        return null;
    }
}
