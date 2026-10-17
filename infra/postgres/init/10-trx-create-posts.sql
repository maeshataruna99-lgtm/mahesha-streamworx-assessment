-- user_id has no foreign key: users are stored in Redis, not in this database.
CREATE TABLE posts (
    id      INTEGER      PRIMARY KEY,
    user_id INTEGER      NOT NULL,
    title   VARCHAR(255) NOT NULL,
    body    TEXT         NOT NULL
);

CREATE INDEX idx_posts_user_id ON posts (user_id);
