CREATE TABLE class_group (
    id                  BIGSERIAL PRIMARY KEY,
    public_id           UUID         NOT NULL,
    name                VARCHAR(150) NOT NULL,
    description         VARCHAR(500),
    subject_id          BIGINT       NOT NULL,
    enrolled_students   INTEGER      NOT NULL DEFAULT 0,
    vacancy_limit       INTEGER      NOT NULL,
    open_for_enrollment BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ,
    CONSTRAINT uk_class_group_public_id UNIQUE (public_id),
    CONSTRAINT fk_class_group_subject FOREIGN KEY (subject_id) REFERENCES subject (id),
    CONSTRAINT chk_class_group_vacancy_limit CHECK (vacancy_limit > 0),
    CONSTRAINT chk_class_group_enrolled_students CHECK (enrolled_students >= 0),
    CONSTRAINT chk_class_group_enrollment_capacity CHECK (enrolled_students <= vacancy_limit)
);

CREATE INDEX idx_class_group_subject_id ON class_group (subject_id);
