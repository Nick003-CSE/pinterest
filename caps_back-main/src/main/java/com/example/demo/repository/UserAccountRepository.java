package com.example.demo.repository;

import com.example.demo.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByPhoneNumber(String phoneNumber);

    long count();
}

