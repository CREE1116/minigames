package org.example.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordRequestDTO {
    private String username; // 또는 userId
    private String gameType;
    private int score;

    public RecordRequestDTO(String username, String gameType, int score) {
        this.username = username;
        this.gameType = gameType;
        this.score = score;
    }
}
