package org.example.member.controller;

import lombok.RequiredArgsConstructor;
import org.example.member.domain.GameRecord;
import org.example.member.domain.User;
import org.example.common.dto.RecordRequestDTO;
import org.example.member.repo.GameRecordRepository;
import org.example.member.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/api")
@RequiredArgsConstructor
public class InternalApiController {

    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;

    @Transactional
    @PostMapping("/records")
    public ResponseEntity<?> saveRecord(
            @RequestHeader("X-SERVER-KEY") String serverKey, // 헤더 검사
            @RequestBody RecordRequestDTO req) {

        if (!"MY_SUPER_SECRET_KEY".equals(serverKey)) {
            return ResponseEntity.status(403).body("누구세요? (잘못된 서버 키)");
        }

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        GameRecord usersRecord = gameRecordRepository.findByUserAndGameType(user,req.getGameType());
        if(usersRecord == null) {
            GameRecord record;
            if(req.checkIsScore())
                    record = new GameRecord(user, req.getGameType(), req.getScore());
            else record = new GameRecord(user, req.getGameType(), 1);
            gameRecordRepository.save(record);
        } else{
            if(req.checkIsScore()) usersRecord.setScore(req.getScore());
            else usersRecord.setScore(usersRecord.getScore() + 1);
        }
        return ResponseEntity.ok("기록 저장 완료");
    }

    @GetMapping("/records")
    public ResponseEntity<?> getRecords(
            @RequestHeader("X-SERVER-KEY") String serverKey,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String gameType) {

        if (!"MY_SUPER_SECRET_KEY".equals(serverKey)) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        List<GameRecord> records;

        if (username != null && gameType != null) {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("유저 없음"));
            records = gameRecordRepository.findAllByUserAndGameType(user, gameType);
        } else if (username != null) {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("유저 없음"));
            records = gameRecordRepository.findAllByUser(user);
        } else if (gameType != null) {
            records = gameRecordRepository.findAllByGameType(gameType);
        } else {
            records = gameRecordRepository.findAll();
        }

        return ResponseEntity.ok(records);
    }
    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings(
            @RequestHeader("X-SERVER-KEY") String serverKey,
            @RequestParam String gameType) {

        // 1. 보안 키 검사
        if (!"MY_SUPER_SECRET_KEY".equals(serverKey)) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        // 2. DB 조회 (Repository에 findTop5... 메서드가 있어야 함)
        List<GameRecord> top5 = gameRecordRepository.findTop5ByGameTypeOrderByScoreDesc(gameType);

        return ResponseEntity.ok(top5);
    }
}