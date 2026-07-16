package com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.AuthCredentialEntity;
import com.bcnc.ecomerce.catalog.domain.model.AuthCredential;
import org.junit.jupiter.api.Test;

class AuthCredentialPersistenceMapperTest {

    private final AuthCredentialPersistenceMapper mapper = new AuthCredentialPersistenceMapper();

    @Test
    void shouldMapEntityToDomain() {
        AuthCredentialEntity entity = new AuthCredentialEntity();
        entity.setId(1L);
        entity.setClientKey("BCNC-CLIENT");
        entity.setUsername("catalog-user");
        entity.setPassword("encoded-password");
        entity.setEnabled(true);

        AuthCredential result = mapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.clientKey()).isEqualTo("BCNC-CLIENT");
        assertThat(result.username()).isEqualTo("catalog-user");
        assertThat(result.password()).isEqualTo("encoded-password");
        assertThat(result.enabled()).isTrue();
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }
}

