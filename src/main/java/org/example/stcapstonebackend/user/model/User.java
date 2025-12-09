package org.example.stcapstonebackend.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.debate.model.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "users")
@Builder(toBuilder = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;


    @Column(nullable = false, length = 50)
    private String riotName;

    @Column(nullable = false, length = 10)
    private String riotTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;
}

