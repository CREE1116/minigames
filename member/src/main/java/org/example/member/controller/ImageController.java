package org.example.member.controller;

import lombok.RequiredArgsConstructor;
import org.example.member.domain.ImageStar;
import org.example.member.domain.UploadedFile;
import org.example.member.repo.ImageStarRepository;
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
    private final ImageStarRepository starRepository; // [추가] 즐겨찾기 레포지토리
    private final String UPLOAD_DIR = "/app/uploads/";

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

            file.transferTo(path.toFile());

            String accessUrl = "/api/images/view/" + savedFileName;
            // UploadedFile 생성 시 isStarred 필드는 이제 사용 안 함(무시)
            fileRepository.save(new UploadedFile(username, accessUrl, file.getOriginalFilename(), gameType));

            return ResponseEntity.ok(accessUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 링크 저장
    @PostMapping("/link")
    public ResponseEntity<?> saveLink(
            @RequestParam("url") String url,
            @RequestParam("username") String username,
            @RequestParam("gameType") String gameType
    ) {
        if (url == null || url.trim().isEmpty()) return ResponseEntity.badRequest().body("URL 공백");
        try {
            fileRepository.save(new UploadedFile(username, url, "External Link", gameType));
            return ResponseEntity.ok("Link Saved");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // 2. 리스트 조회 (유저별 즐겨찾기 반영)
    @GetMapping("/list")
    public ResponseEntity<?> getImageList(
            @RequestParam(required = false) String username // [변경] 현재 접속자 확인용
    ) {
        // 1. 해당 게임의 모든 파일 조회
        List<UploadedFile> files = fileRepository.findAllByOrderByIdDesc();;

        // 2. 현재 유저가 즐겨찾기한 파일 ID 목록 조회
        Set<Long> myStarredIds = new HashSet<>();
        if (username != null && !username.isEmpty()) {
            starRepository.findAllByUsername(username)
                    .forEach(star -> myStarredIds.add(star.getFileId()));
        }

        // 3. 응답 생성 (isStarred 여부 판단)
        List<Map<String, Object>> response = files.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("url", f.getFilePath());
            map.put("name", f.getOriginalFileName());
            map.put("uploader", f.getUploaderUsername());
            // 내 즐겨찾기 목록에 있으면 true
            map.put("isStarred", myStarredIds.contains(f.getId()));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 3. 즐겨찾기 토글 (유저별)
    @PostMapping("/{id}/star")
    @Transactional
    public ResponseEntity<?> toggleStar(
            @PathVariable Long id,
            @RequestParam String username // [변경] 누가 즐겨찾기 했는지 필요
    ) {
        Optional<ImageStar> starOpt = starRepository.findByFileIdAndUsername(id, username);

        if (starOpt.isPresent()) {
            // 이미 있으면 삭제 (즐겨찾기 해제)
            starRepository.delete(starOpt.get());
            return ResponseEntity.ok(false); // 결과: 꺼짐
        } else {
            // 없으면 추가 (즐겨찾기 설정)
            starRepository.save(new ImageStar(id, username));
            return ResponseEntity.ok(true);  // 결과: 켜짐
        }
    }

    // 4. 이미지 삭제
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteImage(@PathVariable Long id) {
        UploadedFile fileEntity = fileRepository.findById(id).orElse(null);
        if (fileEntity == null) return ResponseEntity.notFound().build();

        try {
            // 실제 파일 삭제 (외부 링크가 아닌 경우)
            if (!"External Link".equals(fileEntity.getOriginalFileName())) {
                String savedFileName = fileEntity.getFilePath().substring(fileEntity.getFilePath().lastIndexOf("/") + 1);
                File file = new File(UPLOAD_DIR + savedFileName);
                if (file.exists()) file.delete();
            }

            // 연관된 즐겨찾기 데이터 삭제
            starRepository.deleteAllByFileId(id);

            // DB 삭제
            fileRepository.delete(fileEntity);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("삭제 실패: " + e.getMessage());
        }
    }

    // 5. 이미지 보기 (기존 동일)
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