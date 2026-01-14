package org.example.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "uploaded_files")
public class UploadedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploaderUsername;
    private String filePath; // 클라이언트가 접근할 URL 경로 (예: /api/images/view/filename.jpg)
    private String originalFileName;
    private String gameType;
    private LocalDateTime createdAt;
    @Column(columnDefinition = "boolean default false")
    private boolean isStarred = false;

    public UploadedFile(String uploaderUsername, String filePath, String originalFileName, String gameType) {
        this.uploaderUsername = uploaderUsername;
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.gameType = gameType;
        this.createdAt = LocalDateTime.now();
    }

    public void toggleStar() {
        this.isStarred = !this.isStarred;
    }
}