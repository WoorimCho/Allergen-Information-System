package com.allergen_info_service.bff;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * The BFF's login delegates credential checks to the User service. On success
 * the session principal is a {@link BffPrincipal} carrying the account id.
 */
@Component
class UserAuthenticationProvider implements AuthenticationProvider {

    private final UserServiceClient users;

    UserAuthenticationProvider(UserServiceClient users) {
        this.users = users;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String identifier = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());
        try {
            UserServiceClient.Identity identity = users.authenticate(identifier, password);
            BffPrincipal principal = new BffPrincipal(
                    identity.accountId(), identity.username(), identity.displayName());
            return new UsernamePasswordAuthenticationToken(principal, null, List.of());
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
