--changeset ai-quiz:002-create-quiz-table
CREATE TABLE quiz
(
    id               UUID PRIMARY KEY,
    document_id      UUID NOT NULL,
    quiz_type        VARCHAR(50),
    question_count   INTEGER,
    difficulty_level VARCHAR(50),
    quiz_json        JSONB,
    created_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_quiz_document
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE
);