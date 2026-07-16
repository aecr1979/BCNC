package com.bcnc.ecomerce.catalog.adapter.out.persistence.repository;

import com.bcnc.ecomerce.catalog.adapter.out.persistence.entity.AuthCredentialEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthCredentialRepository extends JpaRepository<AuthCredentialEntity, Long> {

    Optional<AuthCredentialEntity> findByClientKeyAndUsernameAndEnabledTrue(String clientKey, String username);
}

