-- Add video_url column to debate_post table
ALTER TABLE debate_post ADD COLUMN IF NOT EXISTS video_url VARCHAR(255);

