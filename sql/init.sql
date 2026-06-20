CREATE DATABASE IF NOT EXISTS `ddz` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ddz`;

DROP TABLE IF EXISTS `ddz_user`;
CREATE TABLE `ddz_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `avatar` VARCHAR(255) DEFAULT '/default-avatar.png',
  `score` INT DEFAULT 1000,
  `wins` INT DEFAULT 0,
  `losses` INT DEFAULT 0,
  `is_guest` TINYINT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `ddz_room`;
CREATE TABLE `ddz_room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_no` VARCHAR(20) NOT NULL,
  `base_score` INT NOT NULL DEFAULT 1,
  `status` TINYINT DEFAULT 0,
  `player1_id` BIGINT DEFAULT NULL,
  `player2_id` BIGINT DEFAULT NULL,
  `player3_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_no` (`room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `ddz_game_record`;
CREATE TABLE `ddz_game_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_no` VARCHAR(20) NOT NULL,
  `player_id` BIGINT NOT NULL,
  `identity` TINYINT NOT NULL,
  `is_win` TINYINT NOT NULL,
  `score_change` INT NOT NULL,
  `base_score` INT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_player_id` (`player_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `ddz_user` (`username`, `password`, `nickname`, `score`, `is_guest`) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', 1000, 0),
('guest_001', '084e0343a0486ff05530df6c705c8bb4', '游客001', 1000, 1);
