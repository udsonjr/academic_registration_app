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

Backend:

```
backend/src/main/java/br/com/techne/lyceum/academic/
├── controller/
├── service/
├── domain/
├── repository/
├── dto/
├── config/
├── security/
└── shared/
```

Frontend:

```
frontend/src/app/
├── core/         # Auth, interceptor, guards
├── layout/       # Shell com sidebar
├── features/     # Telas (auth, profile, enrollments, CRUDs)
├── services/     # Consumo HTTP tipado
├── shared/       # Alert, modal, pager
└── models/       # Contratos TypeScript
```

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

Admin seed: `admin@admin` / `admin`

CORS padrão inclui `http://localhost:4200`.

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

Aplicação em http://localhost:4200.

A URL da API fica em `src/environments/environment.development.ts` (`apiBaseUrl`).

---

## Testes

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
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
├── backend/                 # Spring Boot
├── frontend/                # Angular + TypeScript
├── frontend-patterns/       # Referência visual da sidebar
├── docker-compose.yml
└── README.md
```
