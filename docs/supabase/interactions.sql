CREATE TABLE IF NOT EXISTS interactions (
  id BIGSERIAL PRIMARY KEY,
  "fromUserId" TEXT NOT NULL DEFAULT '',
  "toUserId" TEXT NOT NULL DEFAULT '',
  "type" TEXT NOT NULL DEFAULT '',
  "text" TEXT NOT NULL DEFAULT '',
  "targetStatusTime" BIGINT NOT NULL DEFAULT 0,
  "createdAt" BIGINT NOT NULL DEFAULT 0,
  "readAt" BIGINT
);

CREATE INDEX IF NOT EXISTS interactions_to_user_created_at_idx
  ON interactions ("toUserId", "createdAt" DESC);

CREATE INDEX IF NOT EXISTS interactions_from_user_created_at_idx
  ON interactions ("fromUserId", "createdAt" DESC);
