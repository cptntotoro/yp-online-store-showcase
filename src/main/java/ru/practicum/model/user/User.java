package ru.practicum.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Пользователь
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @CreationTimestamp
    private LocalDateTime createdAt;
//
//    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
//    private Cart cart;
}