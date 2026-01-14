package org.example.member.repo;

import org.example.member.domain.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    List<UploadedFile> findAllByGameTypeOrderByIsStarredDescIdDesc(String gameType);
}