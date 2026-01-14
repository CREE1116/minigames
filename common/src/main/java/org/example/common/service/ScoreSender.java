package org.example.common.service;

import lombok.RequiredArgsConstructor;
import org.example.common.dto.RecordRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ScoreSender {

    private final RestTemplate restTemplate;


    @Value("${member.api.url}")
    private String MEMBER_API_URL;

    public void sendScore(String username, String gameType, int score) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-SERVER-KEY", "MY_SUPER_SECRET_KEY");

            RecordRequestDTO requestDto = new RecordRequestDTO(username, gameType, score, 0);

            HttpEntity<RecordRequestDTO> request = new HttpEntity<>(requestDto, headers);

            restTemplate.postForEntity(MEMBER_API_URL+"/records", request, String.class);
            System.out.println("✅ [" + gameType + "] 점수 전송 성공: " + username + " (" + score + "점)");

        } catch (Exception e) {
            System.err.println("❌ 점수 전송 실패: " + e.getMessage());
        }
    }

    public ResponseEntity<Object> ranking(String gameType){
        try {
            String targetUrl = MEMBER_API_URL + "/rankings?gameType=" + gameType;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-SERVER-KEY", "MY_SUPER_SECRET_KEY");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Object> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.GET,
                    entity,
                    Object.class // JSON 그대로 토스하기 위해 Object 사용
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("랭킹 조회 실패: " + e.getMessage());
        }
    }
}