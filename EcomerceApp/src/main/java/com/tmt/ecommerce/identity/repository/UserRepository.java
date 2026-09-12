package com.tmt.ecommerce.identity.repository;

import com.tmt.ecommerce.identity.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.Query("select new com.tmt.ecommerce.identity.dto.ProfileResponse(u.id,u.email,u.fullName,u.phone) from User u where u.id=:id")
    Optional<com.tmt.ecommerce.identity.dto.ProfileResponse> findProfileById(@org.springframework.data.repository.query.Param("id") Long id);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query("update User u set u.fullName=:name,u.phone=:phone where u.id=:id")
    int updateProfile(@org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("name") String name,
            @org.springframework.data.repository.query.Param("phone") String phone);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
