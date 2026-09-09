package com.allergen_info_service.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeCalculatorController.class)
@Import({BffSecurityConfig.class, BffExceptionHandler.class})
class RecipeCalculatorControllerTest {

    @Autowired
    MockMvc mvc;
    @MockitoBean
    RecipeCalculatorService calculator;

    private static UsernamePasswordAuthenticationToken session() {
        return new UsernamePasswordAuthenticationToken(
                new BffPrincipal(5L, "woorim", "Woorim"), null, List.of());
    }

    @Test
    void requiresASession() throws Exception {
        mvc.perform(get("/bff/recipes/1/nutrition")).andExpect(status().isUnauthorized());
    }

    @Test
    void withASessionItComputes() throws Exception {
        when(calculator.calories(eq(1L), eq(2)))
                .thenReturn(new RecipeCalculatorService.CalorieResult(
                        1L, "Loaf", 2, 728.0, 364.0, 291.2, 2.91, 250.0, 1, 3));

        mvc.perform(get("/bff/recipes/1/calories?servings=2").with(authentication(session())))
                .andExpect(status().isOk());
    }

    @Test
    void badCalculatorInputBecomes400() throws Exception {
        when(calculator.portions(anyLong(), eq(-1.0), eq(null), eq(null), eq(null)))
                .thenThrow(new IllegalArgumentException("scale must be > 0"));

        mvc.perform(get("/bff/recipes/1/portions?scale=-1").with(authentication(session())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aDownstreamFailureBecomes502() throws Exception {
        when(calculator.nutrition(anyLong(), eq(1)))
                .thenThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        mvc.perform(get("/bff/recipes/1/nutrition").with(authentication(session())))
                .andExpect(status().isBadGateway());
    }
}
