package org.example.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "game_type")
    private String gameType; // 예: "catchmind"

    private int score; // 점수
    @Column(name = "count_w")
    private int count; // 횟수
    private LocalDateTime playedAt;

    protected GameRecord() {}

    public GameRecord(User user, String gameType, int score , int count) {
        this.user = user;
        this.gameType = gameType;
        this.score = score;
        this.count = count;
        this.playedAt = LocalDateTime.now();
    }
}