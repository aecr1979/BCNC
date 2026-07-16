package com.bcnc.ecomerce.catalog.adapter.out.persistence.mapper;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.AuthCredentialEntity;
import com.bcnc.ecomerce.catalog.domain.model.AuthCredential;
import org.springframework.stereotype.Component;

@Component
public class AuthCredentialPersistenceMapper {

    public AuthCredential toDomain(AuthCredentialEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AuthCredential(
            entity.getId(),
            entity.getClientKey(),
            entity.getUsername(),
            entity.getPassword(),
            entity.isEnabled()
        );
    }
}

