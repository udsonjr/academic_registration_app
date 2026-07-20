CREATE TABLE course (
    id          BIGSERIAL PRIMARY KEY,
    public_id   UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at  TIMESTAMPTZ,
    CONSTRAINT uk_course_public_id UNIQUE (public_id)
);
