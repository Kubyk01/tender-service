package com.tender_service.core.api.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
public class UserCwk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false, name = "ID")
    private Long id;

    @Column(nullable = false, name = "NAME")
    private String name;

    @Column(nullable = false, name = "SURNAME")
    private String surname;

    @Column(unique = true, nullable = false, name = "EMAIL")
    private String email;

    @Column(unique = true, nullable = false, name = "USERNAME")
    private String username;

    @Column(nullable = false, name = "PASSWORD")
    @JsonIgnore()
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "CREATE_AT")
    private LocalDateTime createdAt;

    @Column(name = "USER_STATUS")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "ID"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}