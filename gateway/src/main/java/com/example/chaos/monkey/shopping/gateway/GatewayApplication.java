package com.example.chaos.monkey.shopping.gateway;

import com.example.chaos.monkey.shopping.domain.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@RestController
public class GatewayApplication {

    @Value("${rest.endpoint.fashion}")
    private String urlFashion;

    @Value("${rest.endpoint.toys}")
    private String urlToys;

    @Value("${rest.endpoint.hotdeals}")
    private String urlHotDeals;

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @GetMapping("/fallback")
    public ResponseEntity<List<Product>> fallback() {

        System.out.println("fallback enabled");
        HttpHeaders headers = new HttpHeaders();
        headers.add("fallback", "true");
        return ResponseEntity.ok().headers(headers).body(Collections.emptyList());
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("hotdeals", p -> p.path("/hotdeals**")
                        .filters(f ->
                                f.hystrix(c -> c.setName("hotdeals").setFallbackUri("forward:/fallback")))
                        .uri(urlHotDeals))

                .route("fashion", p -> p.path("/fashion/**")
                        .filters(f -> f.hystrix(c -> c.setName("fashion").setFallbackUri("forward:/fallback")))
                        .uri(urlFashion))

                .route("toys", p -> p.path("/toys/**")
                        .filters(f -> f.hystrix(c -> c.setName("toys").setFallbackUri("forward:/fallback")))
                        .uri(urlToys))























                // Load-balanced routes
                .route("loadbalanced-hotdeals", p -> p.path("/lb/hotdeals**")
                        .filters(f -> f.retry(c -> c.setRetries(1).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("hotdeals").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri("lb://hotdeals"))

                .route("loadbalanced-fashion", p -> p.path("/lb/fashion/**")
                        .filters(f -> f.retry(c -> c.setRetries(1).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("fashion").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri("lb://fashion-bestseller"))

                .route("loadbalanced-toys", p -> p.path("/lb/toys/**")
                        .filters(f -> f.retry(c -> c.setRetries(1).setSeries(HttpStatus.Series.SERVER_ERROR))
                                .hystrix(c -> c.setName("toys").setFallbackUri("forward:/fallback"))
                                .rewritePath("(\\/lb)", ""))
                        .uri("lb://toys-bestseller"))
                .build();
    }

}
