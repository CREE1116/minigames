package org.example.member.repo;

import org.example.member.domain.ImageStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ImageStarRepository extends JpaRepository<ImageStar, Long> {
    // 특정 유저가 이 파일을 찜했는지 확인
    Optional<ImageStar> findByFileIdAndUsername(Long fileId, String username);

    // 특정 유저가 찜한 모든 파일 ID 조회
    List<ImageStar> findAllByUsername(String username);

    // 파일 삭제 시 관련 즐겨찾기 데이터도 삭제
    @Transactional
    void deleteAllByFileId(Long fileId);
}