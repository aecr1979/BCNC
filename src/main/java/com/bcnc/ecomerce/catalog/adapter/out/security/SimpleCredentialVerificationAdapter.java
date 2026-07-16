package com.bcnc.ecomerce.catalog.adapter.out.security;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper.AuthCredentialPersistenceMapper;
import com.bcnc.ecomerce.catalog.adapter.out.persistence.repository.AuthCredentialRepository;
import com.bcnc.ecomerce.catalog.auth.application.port.out.CredentialVerificationPort;
import com.bcnc.ecomerce.catalog.auth.domain.model.AuthenticatedUser;
import com.bcnc.ecomerce.catalog.domain.model.AuthCredential;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
public class SimpleCredentialVerificationAdapter implements CredentialVerificationPort {
    private final AuthCredentialRepository authCredentialRepository;
    private final AuthCredentialPersistenceMapper authCredentialPersistenceMapper;
    private final PasswordEncoder passwordEncoder;
    public SimpleCredentialVerificationAdapter(AuthCredentialRepository authCredentialRepository,
                                               AuthCredentialPersistenceMapper authCredentialPersistenceMapper,
                                               PasswordEncoder passwordEncoder) {
        this.authCredentialRepository = authCredentialRepository;
        this.authCredentialPersistenceMapper = authCredentialPersistenceMapper;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public Optional<AuthenticatedUser> verify(String clientKey, String username, String password) {
        return authCredentialRepository.findByClientKeyAndUsernameAndEnabledTrue(clientKey, username)
            .map(authCredentialPersistenceMapper::toDomain)
            .filter(credential -> matchesPassword(credential, password))
            .map(credential -> new AuthenticatedUser(credential.clientKey(), credential.username()));
    }
    private boolean matchesPassword(AuthCredential credential, String rawPassword) {
        return rawPassword != null && passwordEncoder.matches(rawPassword, credential.password());
    }
}
