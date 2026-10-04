package lk.serenetravels.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String OAUTH2_SCHEME = "oauth2ClientCredentials";

    private final KeycloakProperties keycloakProperties;

    public OpenApiConfig(KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
    }

    @Bean
    public OpenAPI userServiceOpenApi() {
        String tokenUrl = keycloakProperties.getServerUrl()
                + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("User management API backed by Keycloak (realm: serenetravels). "
                                + "Click Authorize and fill in a client id/secret with a service account "
                                + "(e.g. user-service) to let Swagger fetch a token for you, or paste an "
                                + "existing JWT into the bearerAuth field instead.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(OAUTH2_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(OAUTH2_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .flows(new OAuthFlows()
                                        .clientCredentials(new OAuthFlow().tokenUrl(tokenUrl))))
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
