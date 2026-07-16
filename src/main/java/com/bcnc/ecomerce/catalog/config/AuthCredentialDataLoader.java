package com.bcnc.ecomerce.catalog.config;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.AuthCredentialEntity;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.AuthCredentialRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthCredentialDataLoader {

    private static final Logger log = LoggerFactory.getLogger(AuthCredentialDataLoader.class);
    private static final String AUTH_CREDENTIALS_JSON_PATH = "data/auth-credentials.json";

    @Bean
    CommandLineRunner initAuthCredentials(AuthCredentialRepository authCredentialRepository,
                                          ObjectMapper objectMapper,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (authCredentialRepository.count() == 0) {
                log.info("Loading initial authentication credentials from JSON...");
                ClassPathResource resource = new ClassPathResource(AUTH_CREDENTIALS_JSON_PATH);
                try (InputStream inputStream = resource.getInputStream()) {
                    List<AuthCredentialJsonDto> credentialDtos = objectMapper.readValue(
                        inputStream,
                        new TypeReference<List<AuthCredentialJsonDto>>() {}
                    );
                    List<AuthCredentialEntity> entities = credentialDtos.stream()
                        .map(dto -> {
                            AuthCredentialEntity entity = new AuthCredentialEntity();
                            entity.setClientKey(dto.clientKey());
                            entity.setUsername(dto.username());
                            entity.setPassword(encodeIfNeeded(dto.password(), passwordEncoder));
                            entity.setEnabled(dto.enabled());
                            return entity;
                        })
                        .toList();
                    authCredentialRepository.saveAll(entities);
                    log.info("Authentication credentials loaded successfully! Total records: {}",
                        authCredentialRepository.count());
                } catch (Exception e) {
                    log.error("Failed to load authentication credentials", e);
                    throw new RuntimeException("Failed to load authentication credentials", e);
                }
            }
        };
    }

    private String encodeIfNeeded(String password, PasswordEncoder passwordEncoder) {
        return password != null && password.startsWith("$2") ? password : passwordEncoder.encode(password);
    }
}
