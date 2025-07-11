package ru.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.client.ApiClient;
import ru.practicum.client.api.PaymentApi;

@Configuration
public class WebClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public WebClient paymentWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(paymentServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    ApiClient apiClient(WebClient webClient) {
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);
        return apiClient;
    }

    @Bean
    PaymentApi paymentApiClient(ApiClient apiClient) {
        return new PaymentApi(apiClient);
    }
}
