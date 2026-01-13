package org.example.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordRequestDTO {
    private String username; // 또는 userId
    private String gameType;
    private int score;

    @JsonProperty("isScore")
    private boolean isScore;
    public boolean checkIsScore(){
        return isScore;
    }
    public RecordRequestDTO(String username, String gameType, int score, boolean isScore) {
        this.username = username;
        this.gameType = gameType;
        this.score = score;
        this.isScore = isScore;
    }
}
