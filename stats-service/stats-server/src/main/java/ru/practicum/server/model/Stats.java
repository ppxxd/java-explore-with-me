package ru.practicum.server.model;

import jakarta.persistence.Entity;
import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "hits")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 64)
    private String app;

    @Column(nullable = false, length = 256)
    private String uri;

    @Column(nullable = false, length = 64)
    private String ip;

    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timestamp;
}