package com.allergen_info_service.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComposedRecipeController.class)
@Import({BffSecurityConfig.class, BffExceptionHandler.class})
class ComposedRecipeControllerSecurityTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    RecipeCompositionService composition;

    private static UsernamePasswordAuthenticationToken session(long accountId) {
        return new UsernamePasswordAuthenticationToken(
                new BffPrincipal(accountId, "woorim", "Woorim"), null, List.of());
    }

    @Test
    void bffRequiresASession() throws Exception {
        mvc.perform(get("/bff/recipes/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void withASessionItComposesForThePrincipalsAccount() throws Exception {
        when(composition.compose(eq(1L), eq(5L))).thenReturn(new RecipeCompositionService.RecipeView(
                1L, "Pad Thai", "anonymous", 1, List.of(), List.of(), List.of(), List.of(), List.of(), true));

        mvc.perform(get("/bff/recipes/1").with(authentication(session(5L))))
                .andExpect(status().isOk());

        verify(composition).compose(1L, 5L);
    }

    @Test
    void youCannotSeeAnotherAccountsFavourites() throws Exception {
        mvc.perform(get("/bff/accounts/9/favorite-recipes").with(authentication(session(5L))))
                .andExpect(status().isForbidden());
    }

    @Test
    void aDownstreamFailureBecomes502() throws Exception {
        when(composition.compose(eq(1L), eq(5L)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(get("/bff/recipes/1").with(authentication(session(5L))))
                .andExpect(status().isBadGateway());
    }
}
