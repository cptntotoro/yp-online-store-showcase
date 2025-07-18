//package ru.practicum.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
//
//import java.util.UUID;
//
//@Configuration
//public class ClientRegistrationConfig {
//
//    @Bean
//    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
//        ClientRegistration registration = ClientRegistration.withRegistrationId("payment-service")
//                .clientId("showcase-client")
//                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
//                .scope("payment.read", "payment.write")
//                .tokenUri("http://auth-server:9000/oauth2/token")
//                .build();
//
//        return new InMemoryReactiveClientRegistrationRepository(registration);
//    }
//}