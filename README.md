# Academic Registration App

Sistema acadêmico de matrículas — desafio técnico **Desenvolvedor(a) Pleno Full Stack** (Tribe Lyceum / Techne).

Monólito com backend Java/Spring Boot, frontend Angular (TypeScript) e PostgreSQL com migrations Flyway.

---

## Visão do domínio

Gestão de matrículas acadêmicas com as entidades:

| Entidade   | Responsabilidade                          |
|------------|-------------------------------------------|
| Usuário    | Alunos e administradores                  |
| Curso      | Cursos oferecidos                         |
| Disciplina | Disciplinas vinculadas ao currículo       |
| Turma      | Oferta de disciplina com limite de vagas  |
| Matrícula  | Vínculo aluno ↔ turma (com status)        |

### Regras de negócio

- Aluno só pode se matricular em turmas **abertas**
- Turma possui **limite de vagas**
- Aluno **não** pode se matricular duas vezes na mesma turma
- Status da matrícula: `PENDING`, `CONFIRMED`, `CANCELLED`
- Confirmar matrícula **consome** vaga; cancelar matrícula confirmada **libera** vaga
- Listagens paginadas (`page`, `size`) via `PageResponse`

---

## Stack

| Camada        | Tecnologia                                      |
|---------------|-------------------------------------------------|
| Backend       | Java 21, Spring Boot 3.4, Spring Web, Validation |
| Persistência  | JPA/Hibernate + PostgreSQL 16                   |
| Migrations    | Flyway                                          |
| API docs      | springdoc-openapi (Swagger UI)                  |
| Frontend      | Angular 18, TypeScript, SCSS                    |
| Ambiente      | Docker Compose (PostgreSQL)                     |

---

## Arquitetura (camadas)

Backend organizado em camadas:

```
backend/src/main/java/br/com/techne/lyceum/academic/
├── controller/      # API REST — recebe/retorna DTOs
├── service/         # Use cases — orquestra regras e transações
├── domain/          # Entidades, enums e invariantes de negócio
├── repository/      # Spring Data JPA
├── dto/             # Contratos de request/response (inclui PageResponse)
├── config/          # OpenAPI, CORS e beans transversais
├── security/        # JWT e autorização
└── shared/          # Tratamento de erros e utilitários
```

Frontend:

```
frontend/src/app/
├── core/           # AuthService, interceptor JWT, guards
├── layout/         # Shell com sidebar constante
├── features/       # Telas por domínio
├── services/       # Consumo HTTP tipado da API
├── shared/         # Alert, confirm-modal, pager
└── models/         # Tipos TypeScript do contrato
```

Migrations Flyway: `backend/src/main/resources/db/migration/`

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+ e npm
- Docker e Docker Compose

---

## Como rodar localmente

### 1. Banco de dados (Docker Compose)

```bash
docker compose up -d
```

| Variável | Valor                   |
|----------|-------------------------|
| Database | `academic_registration` |
| User     | `academic`              |
| Password | `academic`              |

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

- API base: http://localhost:8080/api
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- Admin seed: `admin@admin` / `admin`

CORS padrão: `http://localhost:4200`.

Após a migration `V7__seed_demo_data.sql`, há também alunos de teste (senha `admin`):
`ana.silva@example.com`, `bruno.costa@example.com`, `carla.dias@example.com`, `diego.martins@example.com`
— além de 5 cursos (1 inativo), 25 disciplinas, 50 turmas (algumas fechadas) e matrículas com status variados.

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

Aplicação em http://localhost:4200.

URL da API: `frontend/src/environments/environment.development.ts`.

---

## Testes

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend
npm run test -- --watch=false --browsers=ChromeHeadless
npm run build
```

---

## Papéis no frontend

| Role | Home | Navegação |
|------|------|-----------|
| STUDENT | Matrículas | Perfil, Matrículas, Sair |
| ADMIN | Matrículas | Perfil, Matrículas, Usuários, Cursos, Disciplinas, Turmas, Sair |

---

## Estrutura do repositório

```
academic_registration_app/
├── backend/
├── frontend/
├── frontend-patterns/
├── docker-compose.yml
└── README.md
```
