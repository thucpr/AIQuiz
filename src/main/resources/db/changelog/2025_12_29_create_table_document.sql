--liquibase formatted sql

--changeset ai-quiz:001-create-document-table
CREATE TABLE documents
(
    id         UUID PRIMARY KEY,
    file_name  VARCHAR(255),
    content    TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);