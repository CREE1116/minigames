package org.example.member.controller;

import lombok.RequiredArgsConstructor;
import org.example.member.domain.GameRecord;
import org.example.member.domain.User;
import org.example.member.dto.RecordRequestDTO;
import org.example.member.repo.GameRecordRepository;
import org.example.member.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;

    @GetMapping("/getRecords/{username}/{gameType}")
    public ResponseEntity<List<GameRecord>> getRecords(@RequestParam String username, @RequestParam String gameType) {
        List<GameRecord> records =null;
        if(username!=null && gameType!=null){
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));
            records = gameRecordRepository.findAllByUserandGameType(user,gameType);
        }else if(username!=null && gameType==null){
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));
            records = gameRecordRepository.findAllByUser(user);
        }else if(username==null && gameType!=null){
            records = gameRecordRepository.findAllByGameType(gameType);
        }else{
            records = gameRecordRepository.findAll();
        }
        return ResponseEntity.ok(records);
    }

    @PostMapping("/record")
    public ResponseEntity<?> saveScore(@RequestBody RecordRequestDTO req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        GameRecord usersRecord = gameRecordRepository.findByUser(user);
        if(usersRecord == null) {
            GameRecord record = new GameRecord(user, req.getGameType(), req.getScore());
            gameRecordRepository.save(record);
        } else{
            usersRecord.setScore(req.getScore());
        }
        return ResponseEntity.ok("기록 저장 완료");
    }
}