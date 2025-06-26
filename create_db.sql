-- H2 SQL script to create schema manually (optional)
DROP TABLE IF EXISTS uploads;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id IDENTITY PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE uploads (
    id IDENTITY PRIMARY KEY,
    filename VARCHAR(255),
    content CLOB,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE messages (
  id IDENTITY PRIMARY KEY,
  content CLOB,
  user_id BIGINT,
  created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pings (
  id        IDENTITY PRIMARY KEY,
  from_id   BIGINT,
  to_id     BIGINT,
  sent_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (from_id) REFERENCES users(id),
  FOREIGN KEY (to_id)   REFERENCES users(id)
);
