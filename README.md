# Academic Registration App

Sistema acadêmico de matrículas — desafio técnico **Desenvolvedor(a) Pleno Full Stack** (Tribe Lyceum / Techne).

Monólito com backend Java/Spring Boot, frontend TypeScript (React + Vite) e PostgreSQL com migrations Flyway.

> **Status atual:** infraestrutura e organização em camadas prontas. Regras de negócio, CRUD e fluxo de matrícula ainda **não** foram implementados.

---

## Visão do domínio

Gestão de matrículas acadêmicas com as entidades:

| Entidade   | Responsabilidade                          |
|------------|-------------------------------------------|
| Aluno      | Cadastro de estudantes                    |
| Curso      | Cursos oferecidos                         |
| Disciplina | Disciplinas vinculadas ao currículo       |
| Turma      | Oferta de disciplina com limite de vagas  |
| Matrícula  | Vínculo aluno ↔ turma (com status)        |

### Regras de negócio (a implementar)

- Aluno só pode se matricular em turmas **abertas**
- Turma possui **limite de vagas**
- Aluno **não** pode se matricular duas vezes na mesma turma
- Status da matrícula: `PENDENTE`, `CONFIRMADA`, `CANCELADA`
- Confirmar matrícula **consome** vaga; cancelar matrícula confirmada **libera** vaga
- Consultas de matrículas por aluno e por turma

---

## Stack

| Camada        | Tecnologia                                      |
|---------------|-------------------------------------------------|
| Backend       | Java 21, Spring Boot 3.4, Spring Web, Validation |
| Persistência  | JPA/Hibernate + PostgreSQL 16                   |
| Migrations    | Flyway                                          |
| API docs      | springdoc-openapi (Swagger UI)                  |
| Frontend      | TypeScript, React 18, Vite                      |
| Ambiente      | Docker Compose (PostgreSQL)                     |

---

## Arquitetura (camadas)

Backend organizado em camadas, conforme expectativa do desafio:

```
backend/src/main/java/br/com/techne/lyceum/academic/
├── controller/      # API REST — recebe/retorna DTOs
├── application/     # Use cases / services — orquestra regras e transações
├── domain/          # Entidades, enums e invariantes de negócio
├── repository/      # Spring Data JPA
├── dto/             # Contratos de request/response
├── config/          # OpenAPI, CORS e beans transversais
└── shared/          # Tratamento de erros e utilitários compartilhados
```

**Fluxo pretendido:** `Controller → Application Service → Domain / Repository`

O frontend segue a mesma ideia de responsabilidades claras:

```
frontend/src/
├── pages/        # Telas
├── components/   # Componentes reutilizáveis
├── services/     # Consumo HTTP da API
└── types/        # Tipos TypeScript do contrato
```

Migrations Flyway ficam em:

```
backend/src/main/resources/db/migration/
```

Convenção: `V{versão}__{descricao}.sql` (ex.: `V1__create_schema.sql`).

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- Node.js 20+ e npm
- Docker e Docker Compose

---

## Como rodar localmente

### 1. Banco de dados (Docker Compose)

Na raiz do repositório:

```bash
docker compose up -d
```

Isso sobe o PostgreSQL em `localhost:5432` com:

| Variável | Valor                   |
|----------|-------------------------|
| Database | `academic_registration` |
| User     | `academic`              |
| Password | `academic`              |

Verificar saúde do container:

```bash
docker compose ps
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

- API base: [http://localhost:8080/api](http://localhost:8080/api)
- Swagger UI: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

Variáveis opcionais: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `CORS_ALLOWED_ORIGINS`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Aplicação em [http://localhost:5173](http://localhost:5173).

Configure a URL da API copiando o exemplo de env:

```bash
cp .env.example .env
```

(`VITE_API_BASE_URL` padrão: `http://localhost:8080/api`)

---

## Testes

### Backend

```bash
cd backend
mvn test
```

Hoje há apenas um smoke test de scaffold. Testes unitários das regras de matrícula e testes de integração/API serão adicionados na implementação das funcionalidades.

### Frontend

```bash
cd frontend
npm run build
```

---

## Decisões técnicas

| Decisão | Motivo |
|---------|--------|
| Monólito modular em camadas | Atende o nível Pleno sem complexidade distribuída injustificada |
| `application` (use cases) em vez de “service anêmico solto” | Clareza de responsabilidades e testabilidade das regras |
| Flyway + `ddl-auto: validate` | Evolução explícita do schema; Hibernate não altera o banco sozinho |
| PostgreSQL via Docker Compose | Ambiente reproduzível, exigido pela especificação |
| springdoc-openapi | Documentação e exploração da API sem custo de manutenção alto |
| React + Vite + TypeScript | Frontend tipado, com componentes/páginas/services separados |
| Context-path `/api` | Separa contrato da API e facilita proxy/CORS no frontend |

### Proteção da regra de vagas (planejado)

Ainda não implementado. A intenção é concentrar confirmação/cancelamento em serviço de aplicação **transacional**, com checagem de vagas e unicidade aluno+turma no domínio/persistência (constraint + regra), cobertos por testes.

---

## Limitações conhecidas (estado atual)

- Sem CRUD de entidades e sem fluxo de matrícula
- Sem migrations de schema (pasta Flyway preparada)
- Sem tratamento padronizado de erros de domínio (pacote `shared` reservado)
- Sem testes das regras críticas de matrícula
- Frontend apenas com estrutura e placeholder

---

## Uso de IA

| Item | Detalhe |
|------|---------|
| Ferramenta | Cursor (agente de código) |
| Onde foi usada | Scaffold do monólito, camadas do backend, Docker Compose, configs Spring/Flyway/OpenAPI/CORS, estrutura do frontend e este README |
| Revisão manual | Alinhamento com o documento do desafio; escolha de stack frontend (TS/React); decisão de **não** implementar regras ainda |
| Trechos mais críticos (futuros) | Controle transacional de vagas, unicidade de matrícula, confirmação/cancelamento e testes dessas regras — devem ser revisados com cuidado humano |

---

## Estrutura do repositório

```
academic_registration_app/
├── backend/                 # Spring Boot
├── frontend/                # React + TypeScript (Vite)
├── docker-compose.yml       # PostgreSQL
├── README.md
└── DESAFI_2 - ...DOC        # Especificação do desafio
```

---

## Próximos passos

1. Migrations Flyway (schema das entidades)
2. Domínio + repositories + DTOs + services
3. Endpoints REST e tratamento de erros
4. Testes das regras de matrícula
5. Telas do frontend consumindo a API
