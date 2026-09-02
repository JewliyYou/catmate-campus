USE catmate;

CREATE TABLE IF NOT EXISTS user_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(50) NOT NULL,
  role ENUM('USER','ADMIN') NOT NULL,
  enabled BIT NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campus_cat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  aliases VARCHAR(255), sex VARCHAR(10), age_text VARCHAR(30), enrollment_time VARCHAR(30),
  area VARCHAR(255), status VARCHAR(255), school_status VARCHAR(255), health VARCHAR(255),
  personality VARCHAR(1000), appearance VARCHAR(1000), notes VARCHAR(3000), friendliness INT,
  image_url VARCHAR(255), map_x INT, map_y INT,
  created_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS campus_cat_image (
  cat_id BIGINT NOT NULL,
  sort_order INT NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  PRIMARY KEY(cat_id, sort_order),
  CONSTRAINT fk_cat_image_cat FOREIGN KEY(cat_id) REFERENCES campus_cat(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rescue_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cat_name VARCHAR(50) NOT NULL, title VARCHAR(500) NOT NULL, area VARCHAR(255) NOT NULL,
  priority VARCHAR(10) NOT NULL, status VARCHAR(30) NOT NULL, owner_name VARCHAR(50),
  created_at DATETIME(6) NOT NULL, INDEX idx_rescue_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS volunteer_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  schedule_text VARCHAR(100) NOT NULL,
  owner_name VARCHAR(50),
  status VARCHAR(30) NOT NULL,
  notes VARCHAR(500),
  created_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS auth_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(64) NOT NULL UNIQUE, user_id BIGINT NOT NULL, expires_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_session_user FOREIGN KEY(user_id) REFERENCES user_account(id) ON DELETE CASCADE,
  INDEX idx_session_expiry(expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SHOW TABLES;
