CREATE DATABASE IF NOT EXISTS catchmind_db;
CREATE DATABASE IF NOT EXISTS kkutu_db;
CREATE DATABASE IF NOT EXISTS oh_mock_db;

CREATE DATABASE IF NOT EXISTS member_db; -- 회원 정보 DB

USE member_db;
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE, -- 로그인 ID
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS game_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    game_type VARCHAR(255),
    score INT,
    played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS uploaded_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploader_username VARCHAR(50), -- 업로더 아이디
    file_path VARCHAR(255) NOT NULL, -- 서버 내부 파일 경로 또는 접근 URL
    original_file_name VARCHAR(255),
    game_type VARCHAR(50), -- 어느 게임에서 올렸는지 (예: Yacht_Dice)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_starred BOOLEAN DEFAULT FALSE
    );