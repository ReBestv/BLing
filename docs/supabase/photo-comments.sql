-- 相册照片评论。执行后，删除照片会自动清理其所有评论。
CREATE TABLE IF NOT EXISTS public.photo_comments (
  id BIGSERIAL PRIMARY KEY,
  "photoId" BIGINT NOT NULL REFERENCES public.photos(id) ON DELETE CASCADE,
  "authorDeviceId" TEXT NOT NULL,
  content TEXT NOT NULL DEFAULT '',
  "createdAt" BIGINT NOT NULL DEFAULT 0,
  CONSTRAINT photo_comments_content_length CHECK (char_length(content) BETWEEN 1 AND 100)
);

CREATE INDEX IF NOT EXISTS photo_comments_photo_created_at_idx
  ON public.photo_comments ("photoId", "createdAt" ASC);

CREATE INDEX IF NOT EXISTS photo_comments_author_idx
  ON public.photo_comments ("authorDeviceId");
