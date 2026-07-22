ALTER TABLE student RENAME TO users;

ALTER TABLE users RENAME CONSTRAINT student_pkey TO users_pkey;
ALTER TABLE users RENAME CONSTRAINT uk_student_public_id TO uk_users_public_id;
ALTER INDEX uk_student_email RENAME TO uk_users_email;

ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT';

ALTER TABLE users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'ADMIN'));

ALTER TABLE enrollment DROP CONSTRAINT fk_enrollment_student;
ALTER TABLE enrollment RENAME COLUMN student_id TO user_id;
ALTER TABLE enrollment
    ADD CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER INDEX idx_enrollment_student_id RENAME TO idx_enrollment_user_id;

DROP INDEX uk_enrollment_active;
CREATE UNIQUE INDEX uk_enrollment_active ON enrollment (user_id, class_group_id)
    WHERE status <> 'CANCELLED' AND deleted_at IS NULL;

-- Seed ADMIN: login admin@admin / admin (BCrypt hash of "admin")
INSERT INTO users (public_id, name, email, password, role, created_at)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@admin',
    '$2b$10$plzqSxlEtMSWDn9xbZgIFOCzK0xtjD/fzgqw2zFMG7cDZDSVqS/7C',
    'ADMIN',
    NOW()
);
