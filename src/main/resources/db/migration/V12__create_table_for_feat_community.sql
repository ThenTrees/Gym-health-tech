CREATE TABLE IF NOT EXISTS posts (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                                   content TEXT,
                                   media_urls JSONB,
                                   tags JSONB,
                                   plan_id UUID NULL REFERENCES plans(id) ON DELETE SET NULL,

                                   is_featured BOOLEAN DEFAULT FALSE,
                                   is_active BOOLEAN DEFAULT TRUE,

                                   like_count INTEGER DEFAULT 0,
                                   comment_count INTEGER DEFAULT 0,
                                   share_count INTEGER DEFAULT 0,
                                   save_count INTEGER DEFAULT 0,

                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3️⃣ Indexes
CREATE INDEX IF NOT EXISTS idx_posts_user_created ON posts(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_posts_featured ON posts(is_featured, created_at);

-- 4️⃣ Post Likes
CREATE TABLE IF NOT EXISTS post_likes (
                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                        post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                                        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        UNIQUE (post_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_likes_post ON post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_likes_user ON post_likes(user_id);

-- 5️⃣ Comments
CREATE TABLE IF NOT EXISTS post_comments (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
                                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           parent_comment_id UUID NULL REFERENCES post_comments(id) ON DELETE CASCADE,

                                           content TEXT NOT NULL,
                                           media_url TEXT NULL,
                                           like_count INTEGER DEFAULT 0,
                                           reply_count INTEGER DEFAULT 0,
                                           is_active BOOLEAN DEFAULT TRUE,
                                           is_pinned BOOLEAN DEFAULT FALSE,

                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_comments_post ON post_comments(post_id, created_at);
CREATE INDEX IF NOT EXISTS idx_comments_user ON post_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON post_comments(parent_comment_id);

-- 6️⃣ Comment Likes
CREATE TABLE IF NOT EXISTS comment_likes (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           comment_id UUID NOT NULL REFERENCES post_comments(id) ON DELETE CASCADE,
                                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           UNIQUE (comment_id, user_id)
);
