package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="debate_post")
@Builder(toBuilder = true)
public class DebatePost extends BaseEntity{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    private String writer;
    private String coWriter;
}