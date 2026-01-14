package org.example.member.controller;

import lombok.RequiredArgsConstructor;
import org.example.member.domain.UploadedFile;
import org.example.member.repo.UploadedFileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final UploadedFileRepository fileRepository;
    private final String UPLOAD_DIR = "/app/uploads/"; // 컨테이너 내부 경로

    // 1. 업로드
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("username") String username,
            @RequestParam("gameType") String gameType) {

        if (file.isEmpty()) return ResponseEntity.badRequest().body("파일 없음");

        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String uuid = UUID.randomUUID().toString();
            String ext = extractExt(file.getOriginalFilename());
            String savedFileName = uuid + "." + ext;
            Path path = Paths.get(UPLOAD_DIR + savedFileName);

            file.transferTo(path.toFile()); // 저장

            String accessUrl = "/api/images/view/" + savedFileName;
            fileRepository.save(new UploadedFile(username, accessUrl, file.getOriginalFilename(), gameType));

            return ResponseEntity.ok(accessUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/link")
    public ResponseEntity<?> saveLink(
            @RequestParam("url") String url,
            @RequestParam("username") String username,
            @RequestParam("gameType") String gameType
    ) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("URL이 비어있습니다.");
        }

        try {
            // 외부 링크도 UploadedFile 엔티티로 저장 (originalFileName은 'External Link'로 고정하거나 파싱)
            // filePath에 URL을 그대로 저장합니다.
            UploadedFile linkFile = new UploadedFile(username, url, "External Link", gameType);
            fileRepository.save(linkFile);

            return ResponseEntity.ok("Link Saved");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("저장 실패: " + e.getMessage());
        }
    }
    // 2. 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<?> getImageList(@RequestParam String gameType) {
        List<Map<String, String>> response = fileRepository.findAllByGameTypeOrderByIsStarredDescIdDesc(gameType)
                .stream().map(f -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("url", f.getFilePath());
                    map.put("name", f.getOriginalFileName());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/star")
    @Transactional
    public ResponseEntity<?> toggleStar(@PathVariable Long id) {
        return fileRepository.findById(id)
                .map(file -> {
                    file.toggleStar(); // 상태 반전
                    // Transactional 덕분에 save 호출 안해도 더티체킹으로 저장됨 (또는 fileRepository.save(file) 명시)
                    return ResponseEntity.ok(file.isStarred());
                })
                .orElse(ResponseEntity.notFound().build());
    }
    // 3. 이미지 보기
    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(UPLOAD_DIR).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                String lower = filename.toLowerCase();
                MediaType type = lower.endsWith(".png") ? MediaType.IMAGE_PNG :
                        lower.endsWith(".gif") ? MediaType.IMAGE_GIF : MediaType.IMAGE_JPEG;
                return ResponseEntity.ok().contentType(type).body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private String extractExt(String name) {
        int pos = name.lastIndexOf(".");
        return pos == -1 ? "jpg" : name.substring(pos + 1);
    }
}