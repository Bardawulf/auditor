package com.example.auditor.repository.user;

import com.example.auditor.domain.user.DbUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<DbUser, Long> {
    DbUser findByUsername(String username);
}
