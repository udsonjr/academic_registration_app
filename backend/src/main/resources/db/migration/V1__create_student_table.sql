CREATE TABLE student (
    id          BIGSERIAL PRIMARY KEY,
    public_id   UUID         NOT NULL,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    CONSTRAINT uk_student_public_id UNIQUE (public_id),
    CONSTRAINT uk_student_email UNIQUE (email)
);
