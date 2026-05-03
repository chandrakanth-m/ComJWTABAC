ALTER TABLE embeddings
ADD COLUMN category TEXT,
ADD COLUMN product TEXT;

--Enable Keyword Search in Postgres
ALTER TABLE embeddings
ADD COLUMN content_tsv tsvector;

UPDATE embeddings
SET content_tsv = to_tsvector('english', content);