package com.routex.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.auth-service.url:http://auth-service:8081}")
    private String authServiceUrl;

    public JwtAuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Add correlation ID
            String correlationId = java.util.UUID.randomUUID().toString();
            exchange = exchange.mutate()
                .request(r -> r.header("X-Correlation-ID", correlationId))
                .build();

            // Validate with auth service
            return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/api/v1/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(TokenValidationResponse.class)
                .flatMap(validation -> {
                    if (!validation.valid()) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                    // Forward user info in headers to downstream services
                    var mutatedExchange = exchange.mutate()
                        .request(r -> r
                            .header("X-User-Id", validation.userId())
                            .header("X-User-Email", validation.email())
                            .header("X-User-Role", validation.role())
                            .header("X-Correlation-ID", correlationId)
                        ).build();
                    return chain.filter(mutatedExchange);
                })
                .onErrorResume(ex -> {
                    log.error("Auth validation failed: {}", ex.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
        };
    }

    public static class Config {}

    public record TokenValidationResponse(boolean valid, String userId, String email, String role) {}
}
