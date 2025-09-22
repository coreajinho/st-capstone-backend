CREATE TABLE test_post (
                           id BIGINT NOT NULL AUTO_INCREMENT,
                           title VARCHAR(255) NOT NULL,
                           content TEXT NOT NULL,
                           writer VARCHAR(50) NOT NULL,
                           created_at DATETIME(6),
                           updated_at DATETIME(6),
                           PRIMARY KEY (id)
) ENGINE=InnoDB;