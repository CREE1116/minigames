package org.example.member.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "image_stars",
        indexes = @Index(name = "idx_star_user", columnList = "username"))
public class ImageStar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;      // 즐겨찾기한 파일 ID
    private String username;  // 즐겨찾기한 유저명

    public ImageStar(Long fileId, String username) {
        this.fileId = fileId;
        this.username = username;
    }

    public Long getFileId() {
        return fileId;
    }
}