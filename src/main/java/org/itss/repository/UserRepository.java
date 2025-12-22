package org.itss.repository;

import org.springframework.lang.NonNull;

import org.itss.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(@NonNull String email);
    Optional<User> findByUsername(@NonNull String username);

    boolean existsByEmail(@NonNull String email);

    boolean existsByUsername(@NonNull String username);

    Optional<User> findByRefreshToken(@NonNull String refreshToken);
}
