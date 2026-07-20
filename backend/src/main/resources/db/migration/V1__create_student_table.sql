CREATE TABLE student (
    id          BIGSERIAL PRIMARY KEY,
    public_id   UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT uk_student_public_id UNIQUE (public_id)
);

CREATE UNIQUE INDEX uk_student_email ON student (email) WHERE deleted_at IS NULL;
