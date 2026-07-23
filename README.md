# Academic Registration App

Sistema acadêmico de matrículas: gestão de cursos, disciplinas, turmas e matrículas com autenticação JWT e controle de vagas.

Monólito com backend Java/Spring Boot, frontend Angular (TypeScript) e PostgreSQL com migrations Flyway.

---

## 1. Visão do domínio


| Entidade   | Responsabilidade                               |
| ---------- | ---------------------------------------------- |
| Usuário    | Alunos e administradores (`STUDENT` / `ADMIN`) |
| Curso      | Cursos oferecidos                              |
| Disciplina | Disciplinas vinculadas ao currículo            |
| Turma      | Oferta de disciplina com limite de vagas       |
| Matrícula  | Vínculo aluno ↔ turma (com status)             |


### Regras de negócio

- Aluno só pode se matricular em turmas **abertas**
- Turma possui **limite de vagas**
- Aluno **não** pode ter duas matrículas ativas (`PENDING` / `CONFIRMED`) na mesma turma
- Status: `PENDING`, `CONFIRMED`, `CANCELLED`
- Criar matrícula inicia como `PENDING` e **não** consome vaga
- Confirmar matrícula `PENDING` **consome** vaga (`ADMIN ONLY`) ; cancelar matrícula `CONFIRMED` **libera** vaga
- Listagens paginadas (`page`, `size`) via `PageResponse`

---

## 2. Tecnologias


| Camada       | Tecnologia                                                              |
| ------------ | ----------------------------------------------------------------------- |
| Backend      | Java 21, Spring Boot 3.4, Spring Web, Validation, Spring Security (JWT) |
| Persistência | JPA/Hibernate + PostgreSQL 16                                           |
| Migrations   | Flyway                                                                  |
| API docs     | springdoc-openapi (Swagger UI)                                          |
| Frontend     | Angular 18, TypeScript, SCSS, RxJS                                      |
| Testes       | JUnit 5, Mockito, MockMvc, Testcontainers; Karma/Jasmine no frontend    |
| Qualidade    | Spotless, Checkstyle (backend); ESLint, Prettier (frontend)             |
| Ambiente     | Docker Compose (PostgreSQL)                                             |
| CI           | GitHub Actions (`backend-ci.yml`, `frontend-ci.yml`)                    |


---

## 3. Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+ e npm
- Docker e Docker Compose

---

## 4. Como rodar o projeto localmente

### 4.1. Banco de dados (Docker Compose)

```bash
docker compose up -d
```


| Variável | Valor                   |
| -------- | ----------------------- |
| Host     | `localhost:5432`        |
| Database | `academic_registration` |
| User     | `academic`              |
| Password | `academic`              |


O container usa a imagem `postgres:16-alpine`, volume persistente `postgres_data` e healthcheck com `pg_isready`.

### 4.2. Backend

```bash
cd backend
mvn spring-boot:run
```

- API base: [http://localhost:8080/api](http://localhost:8080/api)
- CORS padrão: `http://localhost:4200`

### 4.3. Frontend

```bash
cd frontend
npm install
npm start
```

Aplicação: [http://localhost:4200](http://localhost:4200)  
URL da API: `frontend/src/environments/environment.development.ts`

### 4.4. Usuários demo (seed)

Senha de todos: `admin`


| Email                      | Papel     | Origem         |
| -------------------------- | --------- | -------------- |
| `admin@admin`              | `ADMIN`   | migration `V6` |
| `peter.parker@example.com` | `STUDENT` | migration `V7` |
| `bruce.wayne@example.com`  | `STUDENT` | migration `V7` |
| `clark.kent@example.com`   | `STUDENT` | migration `V7` |
| `diana.prince@example.com` | `STUDENT` | migration `V7` |


O seed `V7` também cria 5 cursos (1 inativo), 25 disciplinas e 50 turmas (algumas fechadas / com limites baixos) para exercitar a UI.

---

## 5. Documentação Swagger / OpenAPI

Com o backend no ar:

- **Swagger UI:** [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

Autenticação: use `POST /auth/login`, copie o `accessToken` e autorize no Swagger com `Bearer <token>`.

---

## 6. Como executar os testes automatizados

### 6.1. Backend

```bash
cd backend

# Unitários + integração (requer Docker para Testcontainers)
mvn test

# Somente integração / API
mvn test -Dtest=AcademicRegistrationApplicationTests
```

A pipeline `.github/workflows/backend-ci.yml` executa Spotless, Checkstyle e `mvn test` (inclui os testes de integração).

### 6.2. Frontend

```bash
cd frontend
npm run test -- --watch=false --browsers=ChromeHeadless
npm run build
```

---

## 7. Arquitetura em camadas

```
backend/src/main/java/br/com/techne/lyceum/academic/
├── controller/      # API REST — DTOs de entrada/saída
├── service/         # Use cases, regras e transações
├── domain/          # Entidades, enums e invariantes
├── repository/      # Spring Data JPA (+ Specifications)
├── dto/             # Contratos HTTP
├── config/          # OpenAPI, CORS, beans
├── security/        # JWT, filtros, autorização
└── shared/          # Erros e utilitários

frontend/src/app/
├── core/            # Auth, interceptor JWT, guards
├── layout/          # Shell com sidebar
├── features/        # Telas por domínio
├── services/        # Cliente HTTP tipado
├── shared/          # Alert, confirm-modal, pager
└── models/          # Tipos do contrato da API

Migrations do Database: backend/src/main/resources/db/migration/
```

### Papéis no frontend


| Role      | Home       | Navegação                                                       |
| --------- | ---------- | --------------------------------------------------------------- |
| `STUDENT` | Matrículas | Perfil, Matrículas, Sair                                        |
| `ADMIN`   | Matrículas | Perfil, Matrículas, Usuários, Cursos, Disciplinas, Turmas, Sair |


---

## 8. Principais decisões técnicas

1. **Monólito modular** — um backend Spring Boot e um frontend Angular compartilhando o mesmo domínio, com camadas claras (controller → service → repository). Para o tamanho deste domínio, isso simplifica deploy e desenvolvimento sem o custo operacional de microserviços.
2. **`User` com `role`** — em vez de entidade `Student` isolada; `ADMIN` e `STUDENT` no mesmo modelo, com autorização por role/ownership. Um único fluxo de autenticação e menos duplicação de dados de pessoa.
3. **JWT Bearer** — autenticação stateless; registro público cria apenas `STUDENT`; criação de `ADMIN` exige admin autenticado. A API escala sem sessão no servidor e deixa explícito o que é público vs. privilegiado.
4. **`publicId` (UUID) na API** — IDs internos (`BIGINT`) não vazam no contrato HTTP. Evita enumeração de recursos e mantém o identificador externo estável mesmo se o schema interno mudar.
5. **Soft delete** — `deleted_at` + filtro Hibernate; unicidade de e-mail/matrícula ativa via índices parciais. Preserva histórico e auditoria sem quebrar regras de unicidade dos registros ativos.
6. **DTOs aninhados no retorno das APIs** — evita o problema de `N+1` consultas. O cliente recebe o grafo necessário em uma resposta, sem idas e vindas extras para montar a tela.
7. **Mensagens de API em inglês + códigos** — o frontend traduz por `code` (`CLASS_GROUP_FULL`, etc.), desacoplando UI do contrato. O contrato fica estável e a experiência em português (ou outro idioma) fica só no front.

---

## 9. Como a regra de vagas foi protegida

Camadas de proteção:

1. **Contador desnormalizado** `class_group.enrolled_students` + `vacancy_limit`.
2. **Confirmação** (`EnrollmentServiceImpl.confirmEnrollment`):
   - só `ADMIN`;
   - só matrícula `PENDING`;
   - se `enrolledStudents >= vacancyLimit` → `ConflictException` (`CLASS_GROUP_FULL`);
   - senão incrementa o contador e marca `CONFIRMED`.
3. **Cancelamento** de `CONFIRMED` decrementa o contador (libera vaga); `PENDING` cancela sem liberar (não havia consumido).
4. **Update de turma** impede reduzir `vacancyLimit` abaixo de `enrolledStudents`.
5. **Constraints no banco** (`V4`):
   - `enrolled_students >= 0`
   - `vacancy_limit > 0`
   - `enrolled_students <= vacancy_limit`
6. **Índice único parcial** (`uk_enrollment_active`) impede duas matrículas ativas do mesmo aluno na mesma turma.

---

## 10. Como foram testadas as regras críticas de matrícula

### Testes unitários (`EnrollmentServiceImplTest`)

Com repositórios mockados, cobrem entre outros:

- criação `PENDING` em turma aberta
- rejeição de turma fechada (`CLASS_GROUP_NOT_OPEN`)
- rejeição de matrícula ativa duplicada (`ENROLLMENT_ALREADY_EXISTS`)
- re-matrícula após `CANCELLED`
- confirmação consome vaga; turma cheia → `CLASS_GROUP_FULL`
- cancelamento de `CONFIRMED` libera vaga; de `PENDING` não altera contador
- ownership: aluno só cria/cancela/lista a própria; `confirm` só `ADMIN`

### Testes de integração (`AcademicRegistrationApplicationTests`)

Com Spring Boot + MockMvc + Postgres via Testcontainers + Flyway:

- contexto e migrations
- `GET /health`
- registro + login (persistência de usuário)
- login admin → cria curso → lista (JWT + persistência)

As regras de vaga/matrícula ficam principalmente nos unitários de serviço; a integração valida o caminho HTTP + banco nos fluxos principais de auth/CRUD.

---

## 11. Limitações conhecidas

- **Concorrência na confirmação de vagas** — sem lock pessimista/otimista dedicado; a constraint SQL mitiga inconsistência extrema, mas pode gerar erro de constraint em race em vez de `CLASS_GROUP_FULL` amigável.
- **Contador desnormalizado** — `enrolled_students` pode divergir se dados forem alterados fora do fluxo de serviço.
- **Senhas do seed** — todas `admin` (apenas para demo local).
- **Sem E2E de UI** — frontend tem specs unitárias; não há Cypress/Playwright.
- **Integração depende de Docker** — Testcontainers precisa do daemon ativo.
- **Sem refresh token / revogação** — JWT até expirar.
- **Um único banco Postgres** — sem multi-tenancy nem fila assíncrona.

---

## 12. Uso de ferramentas de IA

Ferramenta: **Cursor (agente Composer)**  
Onde foi usada:

- Criação de algumas estruturas nos projetos de backend e frontend
- Criação de migrations Flyway
- Criação de testes (unitários e de integração)
- Documentação (criação do README)

### O processo com uso de IA

O uso de IA em trechos importantes do código foi tratado com a seguinte sistemática:  
Criação de um plano de implementação (com o Cursor no modo `Plan`), que era revisado até chegar em um resultado satisfatório. Implementação do plano. Finalmente, após a implementação, o código era commitado em um PR no GitHub, onde a pipeline rodava `build + lint + tests` e seus arquivos eram revisados novamente para garantir a segurança da aplicação.

### O que foi revisado manualmente

- Regras de matrícula/vagas e códigos de erro
- Autorização por role/ownership
- Contrato da API (DTOs, paginação, filtros)
- Seed demo e credenciais
- Execução dos testes unitários e de integração

### Trechos mais críticos (revisão humana priorizada, ou desenvolvimento feito inteiramente sem IA)

1. `EnrollmentServiceImpl` — abertura de turma, unicidade ativa, consumo/liberação de vaga
2. `SecurityConfig` + filtros JWT + `@PreAuthorize`
3. Migrations (`V5`/`V6`) — índices parciais e constraints de capacidade
4. Tradução de erros no frontend (`extractApiError`) alinhada aos `code`s da API
5. Testes de `EnrollmentServiceImplTest` e smoke de integração com Testcontainers

---

## 13. Estrutura do repositório

```
academic_registration_app/
├── backend/
├── frontend/
├── docker-compose.yml
├── .github/workflows/
└── README.md
```
