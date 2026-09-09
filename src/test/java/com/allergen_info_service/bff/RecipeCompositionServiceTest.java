package com.allergen_info_service.bff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The composition logic against stubbed downstream services. No Spring context —
 * each client is built from a {@link MockRestServiceServer}-bound builder.
 */
class RecipeCompositionServiceTest {

    private MockRestServiceServer recipeServer;
    private MockRestServiceServer ingredientServer;
    private MockRestServiceServer userServer;
    private RecipeCompositionService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder recipeBuilder = RestClient.builder();
        RestClient.Builder ingredientBuilder = RestClient.builder();
        RestClient.Builder userBuilder = RestClient.builder();
        recipeServer = MockRestServiceServer.bindTo(recipeBuilder).build();
        ingredientServer = MockRestServiceServer.bindTo(ingredientBuilder).build();
        userServer = MockRestServiceServer.bindTo(userBuilder).build();
        service = new RecipeCompositionService(
                new RecipeCatalogueClient(recipeBuilder.build()),
                new IngredientCatalogueClient(ingredientBuilder.build()),
                new UserServiceClient(userBuilder.build()));
    }

    @Test
    void resolvesNamesAndUnionsInheritedTagsAndToleratesUnknownIds() {
        recipeServer.expect(requestTo("/api/recipes/42")).andRespond(withSuccess("""
                {"id":42,"name":"Pad Thai","creator":"woorim","version":1,
                 "steps":[{"position":0,"text":"Fry","tools":["wok"]}],
                 "ingredients":[
                   {"ingredientId":10,"quantity":"200g","optional":false,"replaceable":false,"replacements":[]},
                   {"ingredientId":12,"quantity":null,"optional":false,"replaceable":true,
                    "replacements":[{"ingredientId":99,"recipeId":7}]}],
                 "tags":[{"name":"cuisine:thai"}]}""", MediaType.APPLICATION_JSON));

        // id 99 is deliberately absent from the response -> unresolved replacement
        ingredientServer.expect(requestTo(startsWith("/api/ingredients/by-ids?"))).andRespond(withSuccess("""
                [{"id":10,"name":"rice noodles","tags":[{"name":"allergen:none"}]},
                 {"id":12,"name":"tamarind paste","tags":[{"name":"allergen:none"},{"name":"form:processed"}]}]
                """, MediaType.APPLICATION_JSON));

        RecipeCompositionService.RecipeView view = service.compose(42);

        assertThat(view.name()).isEqualTo("Pad Thai");
        assertThat(view.ingredients()).hasSize(2);
        assertThat(view.restrictionConflicts()).isNull();       // no account -> not evaluated
        assertThat(view.compatibleWithAccount()).isNull();

        RecipeCompositionService.Line noodles = view.ingredients().get(0);
        assertThat(noodles.name()).isEqualTo("rice noodles");
        assertThat(noodles.resolved()).isTrue();
        assertThat(noodles.preferredReplacementId()).isNull();

        RecipeCompositionService.Replacement sub = view.ingredients().get(1).replacements().get(0);
        assertThat(sub.name()).isEqualTo("unknown ingredient (#99)");
        assertThat(sub.resolved()).isFalse();
        assertThat(sub.recipeId()).isEqualTo(7L);

        assertThat(view.tags()).containsExactly("cuisine:thai");
        assertThat(view.inheritedTags()).containsExactlyInAnyOrder("allergen:none", "form:processed");

        recipeServer.verify();
        ingredientServer.verify();
    }

    @Test
    void personalisesForAnAccount() {
        recipeServer.expect(requestTo("/api/recipes/42")).andRespond(withSuccess("""
                {"id":42,"name":"Pad Thai","creator":"woorim","version":1,"steps":[],
                 "ingredients":[
                   {"ingredientId":10,"quantity":"200g","optional":false,"replaceable":false,"replacements":[]},
                   {"ingredientId":12,"quantity":null,"optional":false,"replaceable":true,"replacements":[]}],
                 "tags":[]}""", MediaType.APPLICATION_JSON));
        userServer.expect(requestTo("/api/accounts/5/restrictions"))
                .andRespond(withSuccess("[\"allergen:peanut\"]", MediaType.APPLICATION_JSON));
        userServer.expect(requestTo("/api/accounts/5/favorites/alternatives")).andRespond(withSuccess("""
                [{"id":1,"recipeId":42,"ingredientId":12,"replacementIngredientId":88}]
                """, MediaType.APPLICATION_JSON));
        ingredientServer.expect(requestTo(startsWith("/api/ingredients/by-ids?"))).andRespond(withSuccess("""
                [{"id":10,"name":"rice noodles","tags":[{"name":"allergen:none"}]},
                 {"id":12,"name":"tamarind paste","tags":[{"name":"allergen:peanut"}]},
                 {"id":88,"name":"peanut-free tamarind","tags":[{"name":"allergen:none"}]}]
                """, MediaType.APPLICATION_JSON));

        RecipeCompositionService.RecipeView view = service.compose(42, 5L);

        assertThat(view.compatibleWithAccount()).isFalse();
        assertThat(view.restrictionConflicts()).singleElement().satisfies(conflict -> {
            assertThat(conflict.restriction()).isEqualTo("allergen:peanut");
            assertThat(conflict.ingredientIds()).containsExactly(12L);
            assertThat(conflict.ingredientNames()).containsExactly("tamarind paste");
        });

        assertThat(view.ingredients().get(0).preferredReplacementId()).isNull();
        RecipeCompositionService.Line tamarind = view.ingredients().get(1);
        assertThat(tamarind.preferredReplacementId()).isEqualTo(88L);
        assertThat(tamarind.preferredReplacementName()).isEqualTo("peanut-free tamarind");

        recipeServer.verify();
        userServer.verify();
        ingredientServer.verify();
    }

    @Test
    void listsFavouriteRecipeSummaries() {
        userServer.expect(requestTo("/api/accounts/5/favorites/recipes"))
                .andRespond(withSuccess("[7,9]", MediaType.APPLICATION_JSON));
        recipeServer.expect(requestTo(startsWith("/api/recipes/by-ids?"))).andRespond(withSuccess("""
                [{"id":7,"name":"Focaccia","creator":"woorim","version":2,"steps":[],"ingredients":[],
                  "tags":[{"name":"category:bread"}]},
                 {"id":9,"name":"Congee","creator":"anonymous","version":1,"steps":[],"ingredients":[],"tags":[]}]
                """, MediaType.APPLICATION_JSON));

        var summaries = service.favoriteRecipeSummaries(5);

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0).name()).isEqualTo("Focaccia");
        assertThat(summaries.get(0).version()).isEqualTo(2);
        assertThat(summaries.get(0).tags()).containsExactly("category:bread");
        assertThat(summaries.get(1).tags()).isEmpty();

        userServer.verify();
        recipeServer.verify();
    }

    @Test
    void unknownRecipeSurfacesAsNotFound() {
        recipeServer.expect(requestTo("/api/recipes/404")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.compose(404))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
    }
}
