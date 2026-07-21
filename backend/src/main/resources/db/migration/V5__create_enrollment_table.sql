CREATE TABLE enrollment (
    id             BIGSERIAL PRIMARY KEY,
    public_id      UUID        NOT NULL,
    student_id     BIGINT      NOT NULL,
    class_group_id BIGINT      NOT NULL,
    status         VARCHAR(20) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMPTZ,
    CONSTRAINT uk_enrollment_public_id UNIQUE (public_id),
    CONSTRAINT fk_enrollment_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_enrollment_class_group FOREIGN KEY (class_group_id) REFERENCES class_group (id),
    CONSTRAINT chk_enrollment_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'))
);

CREATE INDEX idx_enrollment_student_id ON enrollment (student_id);

CREATE INDEX idx_enrollment_class_group_id ON enrollment (class_group_id);

CREATE UNIQUE INDEX uk_enrollment_active ON enrollment (student_id, class_group_id)
    WHERE status <> 'CANCELLED' AND deleted_at IS NULL;
