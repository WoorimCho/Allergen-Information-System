# Allergen Information System — BFF / auth gateway

> Formerly the monolith. Since the **Phase 3 cutover** this service holds **no
> database and no CRUD** — it is a stateless **backend-for-frontend** and
> **auth gateway** that composes the catalogue services for the logged-in user.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![Build](https://img.shields.io/badge/build-Maven-blue)
![Port](https://img.shields.io/badge/port-8080-lightgrey)

## Role in the system

```
                 ┌─────────────┐
  browser ─────▶ │  UI  :8081  │
                 └──────┬──────┘
        composed view / │ calculators / login       direct reads+writes
        (relays JSESSIONID)                          │
                 ┌──────▼──────┐            ┌─────────┼─────────┬───────────┐
                 │  BFF  :8080 │──────────▶ │ Ingredient  Recipe   User     │
                 │ (this repo) │            │  :8082      :8083    :8084     │
                 └─────────────┘            └───────────────────────────────┘
```

The BFF is the **only place a session is enforced** (decision: *BFF-enforced
session*). `POST /bff/login` checks credentials against the User service and
issues a `JSESSIONID`; `GET /bff/**` requires it. The downstream services are
identity-agnostic — the BFF is the enforcement point (swap point for OAuth2:
`bff/BffSecurityConfig`).

## API

All under `/bff/**`, session required (`POST /bff/login` first).

| Method & path | Purpose |
|---|---|
| `POST /bff/login` (form: `username`, `password`) | 204 + session cookie, or 401 |
| `POST /bff/logout` | 204 |
| `GET  /bff/recipes/{id}` | recipe composed from RecipeCatalogue + IngredientCatalogue + the caller's restrictions/preferred-swaps (`restrictionConflicts`, `inheritedTags`, per-line `amount`/`unit`) |
| `GET  /bff/accounts/{accountId}/favorite-recipes` | the caller's favourites as summaries — **ownership-checked** (403 for another account) |
| `GET  /bff/recipes/{id}/nutrition?servings=N` | per-line contribution + `total` + `perServing` + **`per100g`** + `countedGrams`; un-weighable lines listed in `notCounted` |
| `GET  /bff/recipes/{id}/calories?servings=N` | `totalKcal`, `perServingKcal`, **`kcalPer100g`**, **`kcalPerGram`**, `countedGrams`, counted/total lines |
| `GET  /bff/recipes/{id}/portions?scale=X` **or** `?anchorIngredientId=&anchorAmount=&anchorUnit=` | every line's numeric amount × factor |

`GET /request` / `POST /response` remain for the GoodNight round-trip demo.
`templates/` still holds the retired Thymeleaf CRUD views with `<!-- RETIRED -->`
banners and commented-out routes — kept for history, unreachable.

## Run

```bash
# whole system (from Projects/)
cd .. && docker compose up --build

# this service + its dependencies only
docker compose up --build bff
```

Local (needs the catalogues + User running):

```bash
./mvnw spring-boot:run
```

## Configuration (env)

| Var | Default | |
|---|---|---|
| `INGREDIENT_CATALOGUE_URL` | `http://localhost:8082` | |
| `RECIPE_CATALOGUE_URL` | `http://localhost:8083` | |
| `USER_SERVICE_URL` | `http://localhost:8084` | |
| `GOODNIGHT_URL` | `http://localhost:8085` | |
| `ZIPKIN_ENDPOINT` | `http://localhost:9411/api/v2/spans` | tracing (URLConnection sender — see `config/TracingSenderConfig`) |
| `DOWNSTREAM_CONNECT_TIMEOUT` / `DOWNSTREAM_READ_TIMEOUT` | `2s` / `5s` | outbound HTTP timeouts |

No datasource — the `SPRING_DATASOURCE_*` lines in `compose.yaml` are commented
out.

## Tests

```bash
./mvnw test        # 26: context, BFF security, composition, calculators,
                   #     GoodNight client, circuit-breaker interceptor
```

## Stack notes

- Spring Boot **4.1.1** — `spring-boot-starter-webmvc` (not `-web`),
  `spring-boot-starter-restclient` (auto-configured `RestClient.Builder`),
  Jackson 3 (`tools.jackson.*`).
- Distributed tracing to Zipkin via `zipkin-sender-urlconnection` + a
  `BytesMessageSender` bean (Boot 4.1's JDK-HttpClient sender fails in
  containers).
- **Resilience** — every downstream `RestClient` has a short connect/read
  timeout (`spring.http.clients.*`) and a per-service **circuit breaker**
  (`config/DownstreamResilience` + `CircuitBreakerInterceptor`, resilience4j).
  A connect/read timeout or a 5xx counts as a fault; a 4xx doesn't. When a
  breaker opens, the call fails fast as a 502 instead of hanging the composed
  request. Breaker state is on `/actuator/prometheus`
  (`resilience4j_circuitbreaker_*`).

## Security

⚠️ Development posture: `/bff/**` is session-gated and does an ownership check on
favourites, **but the downstream services trust the network** and every port is
published on `0.0.0.0`. See **`../IngredientCatalogue/SECURITY.md`** for the full
assessment. Do not deploy as-is.

## Status

BFF **done**. Phase 4 calculators + composition include structured quantities and
per-gram density. Remaining roadmap: RAG recipe parsing, webcrawler (not started).
