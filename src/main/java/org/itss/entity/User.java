package org.itss.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "refresh_token")
    private String refreshToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_favorite_spots",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "spot_id")
    )
    private Set<Spot> favorites = new HashSet<>();
}
