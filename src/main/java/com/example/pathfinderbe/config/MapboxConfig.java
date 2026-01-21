package com.example.pathfinderbe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MapboxConfig {

    @Value("${mapbox.api.url}")
    private String apiUrl;

    @Value("${mapbox.api.key}")
    private String apiKey;

    @Bean
    public WebClient mapboxClient(WebClient.Builder builder) {
        return builder.baseUrl(apiUrl)
                      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                      .build();
    }

    public String getApiKey() {
        return apiKey;
    }
}
