package ru.tbank.adminpanel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "t_app_user", schema = "security")
public class AppUser extends AbstractEntity {

    @Column(name = "c_username", unique = true)
    private String username;

    @Column(name = "c_full_name")
    private String fullName;

    @JsonIgnore
    @Column(name = "c_hashed_password")
    private String hashedPassword;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "t_app_user_role", schema = "security", joinColumns = @JoinColumn(name = "c_app_user_id"))
    @Column(name = "c_role", nullable = false, unique = true)
    private Set<Role> roles;
}
