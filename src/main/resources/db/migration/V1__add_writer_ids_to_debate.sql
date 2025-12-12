-- ==========================================
-- Debate 테이블에 writer_id, co_writer_id 추가
-- ==========================================

-- 1. debate_post 테이블에 새 컬럼 추가
ALTER TABLE debate_post
ADD COLUMN writer_id BIGINT NULL AFTER writer,
ADD COLUMN co_writer_id BIGINT NULL AFTER co_writer;

-- 2. debate_comment 테이블에 새 컬럼 추가
ALTER TABLE debate_comment
ADD COLUMN writer_id BIGINT NULL AFTER writer;

-- 3. 기존 데이터 마이그레이션 (3명의 유저에게 균등 분배)
-- 주의: 실제 유저 ID는 데이터베이스 확인 후 수정 필요

-- 유저 정보 확인용 (실행 전 확인)
SELECT id, username, riot_name, riot_tag
FROM users
ORDER BY id
LIMIT 3;

-- debate_post 데이터 마이그레이션
-- 방법: 전체 게시글 수를 3으로 나눠서 각 유저에게 할당
SET @total_posts = (SELECT COUNT(*) FROM debate_post);
SET @posts_per_user = FLOOR(@total_posts / 3);
SET @user1_id = (SELECT id FROM users ORDER BY id LIMIT 1);
SET @user2_id = (SELECT id FROM users ORDER BY id LIMIT 1 OFFSET 1);
SET @user3_id = (SELECT id FROM users ORDER BY id LIMIT 1 OFFSET 2);

-- User 1에게 첫 1/3 할당
UPDATE debate_post dp
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_post
) ranked ON dp.id = ranked.id
SET dp.writer_id = @user1_id,
    dp.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user1_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user1_id)
    )
WHERE ranked.rn <= @posts_per_user;

-- User 2에게 중간 1/3 할당
UPDATE debate_post dp
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_post
) ranked ON dp.id = ranked.id
SET dp.writer_id = @user2_id,
    dp.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user2_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user2_id)
    )
WHERE ranked.rn > @posts_per_user AND ranked.rn <= @posts_per_user * 2;

-- User 3에게 나머지 할당
UPDATE debate_post dp
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_post
) ranked ON dp.id = ranked.id
SET dp.writer_id = @user3_id,
    dp.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user3_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user3_id)
    )
WHERE ranked.rn > @posts_per_user * 2;

-- co_writer는 랜덤하게 일부만 할당 (약 30%의 게시글에만)
UPDATE debate_post dp
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY RAND()) as rn
    FROM debate_post
) ranked ON dp.id = ranked.id
SET dp.co_writer_id = CASE
    WHEN MOD(ranked.rn, 3) = 0 THEN @user2_id
    WHEN MOD(ranked.rn, 3) = 1 THEN @user3_id
    ELSE @user1_id
END,
dp.co_writer = CASE
    WHEN MOD(ranked.rn, 3) = 0 THEN CONCAT(
        (SELECT riot_name FROM users WHERE id = @user2_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user2_id)
    )
    WHEN MOD(ranked.rn, 3) = 1 THEN CONCAT(
        (SELECT riot_name FROM users WHERE id = @user3_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user3_id)
    )
    ELSE CONCAT(
        (SELECT riot_name FROM users WHERE id = @user1_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user1_id)
    )
END
WHERE ranked.rn <= FLOOR(@total_posts * 0.3);

-- debate_comment 데이터 마이그레이션
SET @total_comments = (SELECT COUNT(*) FROM debate_comment);
SET @comments_per_user = FLOOR(@total_comments / 3);

-- User 1에게 첫 1/3 할당
UPDATE debate_comment dc
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_comment
) ranked ON dc.id = ranked.id
SET dc.writer_id = @user1_id,
    dc.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user1_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user1_id)
    )
WHERE ranked.rn <= @comments_per_user;

-- User 2에게 중간 1/3 할당
UPDATE debate_comment dc
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_comment
) ranked ON dc.id = ranked.id
SET dc.writer_id = @user2_id,
    dc.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user2_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user2_id)
    )
WHERE ranked.rn > @comments_per_user AND ranked.rn <= @comments_per_user * 2;

-- User 3에게 나머지 할당
UPDATE debate_comment dc
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) as rn
    FROM debate_comment
) ranked ON dc.id = ranked.id
SET dc.writer_id = @user3_id,
    dc.writer = CONCAT(
        (SELECT riot_name FROM users WHERE id = @user3_id),
        '#',
        (SELECT riot_tag FROM users WHERE id = @user3_id)
    )
WHERE ranked.rn > @comments_per_user * 2;

-- 4. NOT NULL 제약조건 추가
ALTER TABLE debate_post
MODIFY COLUMN writer_id BIGINT NOT NULL;

ALTER TABLE debate_comment
MODIFY COLUMN writer_id BIGINT NOT NULL;

-- 5. 외래키 추가 (선택사항)
ALTER TABLE debate_post
ADD CONSTRAINT fk_debate_post_writer
FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE debate_post
ADD CONSTRAINT fk_debate_post_cowriter
FOREIGN KEY (co_writer_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE debate_comment
ADD CONSTRAINT fk_debate_comment_writer
FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE CASCADE;

-- 7. 인덱스 추가 (성능 최적화)
CREATE INDEX idx_debate_post_writer_id ON debate_post(writer_id);
CREATE INDEX idx_debate_post_cowriter_id ON debate_post(co_writer_id);
CREATE INDEX idx_debate_comment_writer_id ON debate_comment(writer_id);

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully' as status;
SELECT COUNT(*) as total_posts, COUNT(DISTINCT writer_id) as unique_writers FROM debate_post;
SELECT COUNT(*) as total_comments, COUNT(DISTINCT writer_id) as unique_writers FROM debate_comment;

