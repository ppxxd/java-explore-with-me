package ru.practicum.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    Long id;

    @Column(name = "user_name", nullable = false)
    String name;

    @Email
    @Column(name = "user_email", nullable = false)
    String email;

}
