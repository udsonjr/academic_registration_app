-- Demo seed data for local testing.
-- Student passwords: same BCrypt as admin → login password is "admin"
-- (admin@admin remains the ADMIN from V6)

-- ---------------------------------------------------------------------------
-- Students
-- ---------------------------------------------------------------------------
INSERT INTO users (public_id, name, email, password, role, created_at)
VALUES
    (gen_random_uuid(), 'Peter Parker', 'peter.parker@example.com',
     '$2b$10$plzqSxlEtMSWDn9xbZgIFOCzK0xtjD/fzgqw2zFMG7cDZDSVqS/7C', 'STUDENT', NOW()),
    (gen_random_uuid(), 'Bruce Wayne', 'bruce.wayne@example.com',
     '$2b$10$plzqSxlEtMSWDn9xbZgIFOCzK0xtjD/fzgqw2zFMG7cDZDSVqS/7C', 'STUDENT', NOW()),
    (gen_random_uuid(), 'Clark Kent', 'clark.kent@example.com',
     '$2b$10$plzqSxlEtMSWDn9xbZgIFOCzK0xtjD/fzgqw2zFMG7cDZDSVqS/7C', 'STUDENT', NOW()),
    (gen_random_uuid(), 'Diana Prince', 'diana.prince@example.com',
     '$2b$10$plzqSxlEtMSWDn9xbZgIFOCzK0xtjD/fzgqw2zFMG7cDZDSVqS/7C', 'STUDENT', NOW());

-- ---------------------------------------------------------------------------
-- Courses, subjects and class groups (5 × 5 × 2)
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    course_defs CONSTANT text[][] := ARRAY[
        ARRAY['Ciência da Computação', 'Formação em computação e sistemas.', 'true'],
        ARRAY['Engenharia de Software', 'Foco em projeto, qualidade e entrega.', 'true'],
        ARRAY['Matemática Aplicada', 'Modelagem matemática e estatística.', 'true'],
        ARRAY['Administração', 'Gestão organizacional e finanças.', 'true'],
        ARRAY['Letras (Descontinuado)', 'Curso encerrado para novas turmas.', 'false']
    ];
    subject_names CONSTANT text[] := ARRAY[
        'Fundamentos I',
        'Fundamentos II',
        'Laboratório',
        'Projeto Integrador',
        'Tópicos Especiais'
    ];
    subject_id bigint;
    subject_name text;
    i int;
    j int;
    course_id bigint;
    course_active boolean;
    open_a boolean;
    open_b boolean;
    limit_a int;
    limit_b int;
BEGIN
    FOR i IN 1..array_length(course_defs, 1) LOOP
        course_active := course_defs[i][3]::boolean;

        INSERT INTO course (public_id, name, description, active, created_at)
        VALUES (
            gen_random_uuid(),
            course_defs[i][1],
            course_defs[i][2],
            course_active,
            NOW()
        )
        RETURNING id INTO course_id;

        FOR j IN 1..array_length(subject_names, 1) LOOP
            subject_name := subject_names[j] || ' — ' || course_defs[i][1];

            INSERT INTO subject (public_id, name, description, course_id, created_at)
            VALUES (
                gen_random_uuid(),
                subject_name,
                'Disciplina de ' || lower(subject_names[j]) || ' do curso ' || course_defs[i][1] || '.',
                course_id,
                NOW()
            )
            RETURNING id INTO subject_id;

            -- Varied class-group cases per subject
            -- Turma A: usually open; some closed; Letras mostly closed
            -- Turma B: alternate open/closed; some near capacity later via enrollments
            open_a := course_active AND NOT (i = 4 AND j = 5); -- Administração / Tópicos Especiais: A fechada
            open_b := course_active AND (j % 2 = 1); -- B aberta só em disciplinas ímpares
            IF NOT course_active THEN
                open_a := false;
                open_b := false;
            END IF;

            limit_a := CASE WHEN j = 1 THEN 2 ELSE 40 END;
            limit_b := CASE WHEN j = 3 THEN 5 ELSE 30 END;

            INSERT INTO class_group (
                public_id, name, description, subject_id,
                enrolled_students, vacancy_limit, open_for_enrollment, created_at
            ) VALUES (
                gen_random_uuid(),
                'Turma A',
                CASE
                    WHEN NOT open_a THEN 'Turma fechada para novas matrículas.'
                    WHEN limit_a = 2 THEN 'Turma pequena (limite 2 vagas).'
                    ELSE 'Turma diurna.'
                END,
                subject_id,
                0,
                limit_a,
                open_a,
                NOW()
            );

            INSERT INTO class_group (
                public_id, name, description, subject_id,
                enrolled_students, vacancy_limit, open_for_enrollment, created_at
            ) VALUES (
                gen_random_uuid(),
                'Turma B',
                CASE
                    WHEN NOT open_b THEN 'Turma noturna fechada.'
                    WHEN limit_b = 5 THEN 'Turma com poucas vagas.'
                    ELSE 'Turma noturna.'
                END,
                subject_id,
                0,
                limit_b,
                open_b,
                NOW()
            );
        END LOOP;
    END LOOP;
END $$;
