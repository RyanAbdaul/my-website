CREATE TABLE article_tag (
                             article_id UUID NOT NULL REFERENCES articles(id),
                             tag_id UUID NOT NULL REFERENCES tags(id),
                             PRIMARY KEY (article_id, tag_id)
);