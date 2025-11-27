-- Add views column to debate_post table
ALTER TABLE debate_post ADD COLUMN IF NOT EXISTS views INT NOT NULL DEFAULT 0;

-- Create debate_post_tags table for storing tags
CREATE TABLE IF NOT EXISTS debate_post_tags (
    debate_post_id BIGINT NOT NULL,
    tag VARCHAR(10) NOT NULL,
    CONSTRAINT fk_debate_post_tags_debate_post
        FOREIGN KEY (debate_post_id)
        REFERENCES debate_post(id)
        ON DELETE CASCADE,
    PRIMARY KEY (debate_post_id, tag)
);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_debate_post_tags_post_id ON debate_post_tags(debate_post_id);

