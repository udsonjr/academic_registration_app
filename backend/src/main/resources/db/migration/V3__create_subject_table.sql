CREATE TABLE subject (
    id          BIGSERIAL PRIMARY KEY,
    public_id   UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    course_id   BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT uk_subject_public_id UNIQUE (public_id),
    CONSTRAINT fk_subject_course FOREIGN KEY (course_id) REFERENCES course (id)
);

CREATE INDEX idx_subject_course_id ON subject (course_id);
