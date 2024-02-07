package com.example.gateway.Config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
            return builder.routes()
                    .route("r1", r -> r.path("/produits/**")
                            .uri("lb://PRODUIT-SERVICE"))
                    .route("r2", r -> r.path("/machines/**")
                            .uri("lb://MACHINE-SERVICE"))
                    .build();
        }
    }
