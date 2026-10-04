# user-service

Spring Boot microservice (Java 17, Spring Boot 4.1.1) for Serene Travels user
management. Keycloak (realm `serenetravels`, server `http://localhost:8880`) is
the identity provider; this service is both an OAuth2 resource server and the
Admin API front door for creating/managing users. User profile data not held
by Keycloak lives in its own Postgres database (`user_service`).

## Package structure (guardrail — follow for every new class)

Flat, layer-based packages directly under `lk.serenetravels.userservice` —
**not** feature packages (no `user/`, `booking/`, etc. grouping a vertical
slice):

```
lk.serenetravels.userservice
├── config/          Spring @Configuration classes, @ConfigurationProperties
├── controller/       @RestController classes, @RestControllerAdvice
├── dto/              Request/response payloads
├── entity/           @Entity classes and their tightly-coupled enums
├── exception/        Custom exception types
├── repository/        Spring Data JPA repositories
├── service/           Service interfaces (the abstraction) live directly here
│   └── impl/          Implementations of those interfaces go here
└── util/             Shared, stateless helpers with no layer dependency
```

Rules:
- Every service gets an interface in `service/` and its implementation in
  `service/impl/` (e.g. `UserService` + `impl/UserServiceImpl`). Controllers
  and other services depend on the interface, never the impl class directly.
- A class goes in the layer folder matching what it *is*, not which feature
  it supports — e.g. `UserProfile` goes in `entity/`, not a `user/` package.
- Keep this list in sync if the structure changes; it governs where new
  classes should be created.
