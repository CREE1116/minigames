package org.example.member.repo;

import org.example.member.domain.GameRecord;
import org.example.member.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    GameRecord findByUser(User user);

    List<GameRecord> findAllByUserandGameType(User user, String gameType);

    List<GameRecord> findAllByUser(User user);

    List<GameRecord> findAllByGameType(String gameType);

    GameRecord findByUserandGameType(User user, String gameType);

    List<GameRecord> findAllByUserAndGameType(User user, String gameType);
}