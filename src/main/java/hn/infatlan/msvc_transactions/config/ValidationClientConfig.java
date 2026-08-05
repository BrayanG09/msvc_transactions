package hn.infatlan.msvc_transactions.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ValidationClientConfig {

        @Bean
        RestClient validationRestClient(
                        @Value("${validation.service.url}") String baseUrl,
                        @Value("${validation.service.connect-timeout}") Duration connectTimeout,
                        @Value("${validation.service.read-timeout}") Duration readTimeout) {
                java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                                .connectTimeout(connectTimeout)
                                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                                .build();

                JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
                requestFactory.setReadTimeout(readTimeout);

                return RestClient.builder()
                                .baseUrl(baseUrl)
                                .requestFactory(requestFactory)
                                .build();
        }
}
