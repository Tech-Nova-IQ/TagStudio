package com.technova.tagstudio.repository;

import com.technova.tagstudio.model.AccountStatus;
import com.technova.tagstudio.model.Role;
import com.technova.tagstudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    long countByAccountStatus(AccountStatus status);

    long countByEmailVerified(boolean emailVerified);

    long countByRoleAndAccountStatus(Role role, AccountStatus status);

    List<User> findAllByOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR u.accountStatus = :status) " +
           "ORDER BY u.createdAt DESC")
    List<User> searchUsers(@Param("query") String query, @Param("status") AccountStatus status);
}

