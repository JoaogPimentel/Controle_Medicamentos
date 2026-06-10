# Plataforma de Controle de Medicamentos

Sistema para gerenciamento de medicamentos de pacientes — controle de estoque,
posologia, histórico de doses e alertas. O projeto é dividido em uma **API REST
desacoplada** (Java + Jakarta Servlets) e um **front-end SPA** (React + Vite) que
a consome via HTTP, autenticando-se por **JWT**.

> Link do front publicado (GitHub Pages): _a preencher_

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Front-end | React 18, Vite, JavaScript |
| Back-end | Java 11+, Jakarta Servlets, Apache Tomcat 10.1 (embutido) |
| Autenticação | JWT (HS256) stateless |
| Banco de dados | PostgreSQL 16 |

## Arquitetura

API pura: o back-end não renderiza HTML — todas as rotas respondem JSON. O front
React é servido separadamente (Vite em dev; GitHub Pages em produção) e consome a
API com `Authorization: Bearer <token>`.

```
React (Vite / GitHub Pages)
      │  fetch + Authorization: Bearer <jwt>
      ▼
 AuthFilter        ← CORS + valida o JWT, publica o usuário no request
      │
      ▼
  Servlet          ← Controller: valida entrada, chama Services/DAOs
      │
      ▼
  JSON ──────────► Cliente
```

- **Controller:** Servlets em `servlet/` processam as requisições e respondem JSON via `JsonUtil`.
- **Model:** JavaBeans em `model/`, DAOs em `dao/` (PreparedStatement) e regras em `services/`. Acesso ao banco via `ConnectionPool`.
- **Autenticação:** `JwtUtil` gera/valida tokens; `AuthFilter` intercepta todas as rotas.

## Funcionalidades

- Cadastro e autenticação de usuários com hash de senha (PBKDF2-SHA256)
- Papéis de acesso: `PACIENTE`, `CUIDADOR`, `ADMIN`
- Vínculo N:N entre pacientes e cuidadores (apenas cuidadores gerenciam vínculos)
- Catálogo central de medicamentos com princípio ativo e forma farmacêutica
- Controle de estoque rastreável por movimentações imutáveis e auditáveis
- Posologia com intervalo de doses e duração do tratamento
- Histórico de adesão ao tratamento (dose tomada, atrasada, pulada)
- Alertas para doses próximas, estoque baixo e vencimento

## Segurança

- **Autenticação JWT (HS256):** o login retorna `{ token, usuario }`. O token carrega
  `id_pessoa`, `nome`, `papel`, `iat` e `exp` (validade de 8 horas). Não há sessão
  no servidor — o estado fica no cliente.
- **Segredo fora do código:** a assinatura usa a variável de ambiente `JWT_SECRET`.
  Sem ela, um segredo de desenvolvimento é usado **apenas com aviso no log** (nunca em produção).
- **Autorização:** `AuthFilter` exige `Authorization: Bearer <token>` em todas as
  rotas `/api/*`, exceto login/cadastro. Sem token válido → `401 JSON`.
- **Papéis:** rotas críticas verificam o papel do token (ex.: `403` para PACIENTE em vínculos).
- **CORS configurável:** origens permitidas via `CORS_ORIGIN`; o preflight `OPTIONS`
  é respondido com `204`. Como a auth é por `Bearer` (não cookies), a origem exata é
  ecoada sem `Access-Control-Allow-Credentials`.
- **Cache:** rotas autenticadas recebem `Cache-Control: no-store`; estáticos `max-age=86400`.

## Estrutura do projeto

```
Controle_Medicamentos/
├── bruno/                  # Collection Bruno de testes da API (ver bruno/README.md)
├── frontend/               # SPA React + Vite (consome a API)
├── lib/                    # Dependências JAR do back-end
├── sql/
│   └── database.sql        # DDL completo (carregado pelo Docker no 1º start)
├── docker-compose.yml      # PostgreSQL para desenvolvimento
├── .env.example            # Modelo de variáveis de ambiente
└── src/main/
    ├── java/
    │   ├── Main.java        # Sobe o Tomcat embutido e registra os servlets
    │   ├── db/              # ConnectionPool (pool + Proxy) e fachada
    │   ├── model/           # Entidades e enums
    │   ├── dao/             # Acesso a dados
    │   ├── services/        # Regras de negócio
    │   ├── servlet/
    │   │   ├── filter/      # AuthFilter (CORS + JWT) e registrar
    │   │   ├── AuthServlet.java        # /api/auth/login | cadastrar | logout
    │   │   ├── DashboardServlet.java   # GET /api/dashboard
    │   │   └── ...                     # demais recursos da API
    │   └── utils/
    │       ├── JwtUtil.java     # Geração/validação de JWT (HS256)
    │       ├── CorsConfig.java  # CORS configurável via CORS_ORIGIN
    │       ├── Hasher.java      # PBKDF2-SHA256
    │       └── JsonUtil.java    # Serialização/parsing JSON
    └── webapp/css/style.css
```

## Variáveis de ambiente

Copie `.env.example` para `.env` e ajuste. Resumo:

| Variável | Uso | Padrão |
|----------|-----|--------|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Conexão com o PostgreSQL (lidas pelo `ConnectionPool`) | — |
| `JWT_SECRET` | Segredo de assinatura do JWT | _dev fallback (com aviso)_ |
| `CORS_ORIGIN` | Origens permitidas (lista por vírgula) | `http://localhost:5173` |
| `POSTGRES_*` | Credenciais do container do docker-compose | ver `.env.example` |

## Como rodar

### Pré-requisitos
- Java JDK 11+
- Docker + Docker Compose (ou um PostgreSQL local)
- Node.js 18+ (para o front)

### 1. Banco de dados (Docker)

```bash
cp .env.example .env      # ajuste as credenciais se quiser
docker compose up -d      # sobe o PostgreSQL com o schema já criado
```

O banco fica em `localhost:5432` (db `devweb`). Para um PostgreSQL local em vez do
Docker, crie o db e rode `sql/database.sql`, definindo `DB_URL/DB_USER/DB_PASSWORD`
no ambiente (ou `src/main/resources/db.properties`).

### 2. Back-end (API)

```powershell
# Compilar
New-Item -ItemType Directory -Force -Path "out\classes" | Out-Null
$arquivos = (Get-ChildItem -Recurse -Path "src\main\java" -Filter "*.java").FullName
[System.IO.File]::WriteAllLines("sources.txt", $arquivos)
javac -encoding UTF-8 -cp "lib\*" -d "out\classes" "@sources.txt"

# Executar (defina JWT_SECRET e CORS_ORIGIN antes, se desejar)
java -cp "out\classes;lib\*" Main
```

A API sobe em `http://localhost:8080`.

### 3. Front-end (React)

```bash
cd frontend
npm install
npm run dev               # Vite em http://localhost:5173 (proxy para a API)
```

### 4. Testes de API (Bruno)

Abra a pasta `bruno/` no [Bruno](https://www.usebruno.com/), selecione o ambiente
**Local** e siga a ordem descrita em `bruno/README.md`. Cobre GET/POST/PUT/DELETE
de todos os recursos, com o token e os IDs preenchidos automaticamente.

## Rotas da API

Todas retornam JSON. Exceto as públicas, exigem `Authorization: Bearer <token>`.

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/api/auth/cadastrar` | Pública | Cadastra novo usuário |
| `POST` | `/api/auth/login` | Pública | Autentica; retorna `{ token, usuario }` |
| `GET` | `/api/auth/logout` | Bearer | Logout stateless (responde `200`) |
| `GET` | `/api/dashboard` | Bearer | Resumo conforme o papel (alertas / pacientes) |
| `GET` | `/api/catalogo` | Bearer | Lista catálogo (ou `?nome=` para busca) |
| `GET` | `/api/catalogo/{id}` | Bearer | Item do catálogo por ID |
| `POST` | `/api/catalogo` | Bearer | Cadastra item no catálogo |
| `PUT` | `/api/catalogo/{id}` | Bearer | Atualiza item do catálogo |
| `DELETE` | `/api/catalogo/{id}` | Bearer | Exclui item (`409` se houver vínculos) |
| `GET` | `/api/medicamentos?paciente={id}` | Bearer | Lista medicamentos do paciente |
| `GET` | `/api/medicamentos/{id}` | Bearer | Medicamento por ID |
| `POST` | `/api/medicamentos` | Bearer | Cadastra medicamento |
| `PUT` | `/api/medicamentos/{id}` | Bearer | Atualiza dosagem/estoque/validade/status |
| `DELETE` | `/api/medicamentos/{id}` | Bearer | Arquiva (`?force=true` exclui) |
| `POST` | `/api/medicamentos/tratamento` | Bearer | Inicia posologia |
| `POST` | `/api/medicamentos/dose` | Bearer | Registra dose tomada |
| `GET` | `/api/posologias?medicamento={id}` | Bearer | Lista posologias |
| `PUT` | `/api/posologias/{id}` | Bearer | Reativa posologia |
| `DELETE` | `/api/posologias/{id}` | Bearer | Desativa posologia |
| `GET` | `/api/estoque?medicamento={id}` | Bearer | Lista movimentações |
| `POST` | `/api/estoque` | Bearer | Registra movimentação |
| `GET` | `/api/historico?posologia={id}` | Bearer | Histórico de doses |
| `GET` | `/api/alertas?pessoa={id}` | Bearer | Alertas não lidos |
| `POST` | `/api/alertas/{id}/lido` | Bearer | Marca alerta como lido |
| `GET` | `/api/vinculos?paciente={id}` | Bearer | Vínculos do paciente |
| `GET` | `/api/vinculos?cuidador={id}` | Bearer | Vínculos do cuidador |
| `GET` | `/api/vinculos/buscar-paciente?email=` | Bearer | Localiza paciente por e-mail |
| `POST` | `/api/vinculos` | Bearer + CUIDADOR | Cria vínculo |
| `DELETE` | `/api/vinculos/{id}` | Bearer + CUIDADOR | Encerra vínculo |

## Modelo de dados

### Especialização de Pessoa
`Pessoa` é a entidade base. Um mesmo registro pode existir em `Paciente` e `Cuidador`
(especialização parcial e sobreposta). O vínculo é gerenciado por `PacienteCuidador`,
com histórico via `data_fim` e flag `ativo`.

### Controle de estoque
`estoque_atual` em `Medicamento` é derivado — nunca atualizado diretamente. Toda
variação passa por `MovimentacaoEstoque`, e a trigger `trg_movimentacao_aplica_estoque`
aplica a movimentação com verificação de consistência.

### Ciclo de vida do medicamento
O status (`EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO`) é mantido pela trigger
`trg_posologia_status_medicamento`.

## Enums

| Enum Java | Valores |
|-----------|---------|
| `FormaFarmaceutica` | `COMPRIMIDO`, `CAPSULA`, `LIQUIDO_ML`, `GOTAS`, `INJECAO`, `POMADA`, `SPRAY`, `ADESIVO`, `OUTRO` |
| `StatusMedicamento` | `EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO` |
| `StatusDose` | `PREVISTA`, `TOMADA`, `ATRASADA`, `PULADA` |
| `TipoAlerta` | `DOSE_PROXIMA`, `DOSE_ATRASADA`, `ESTOQUE_BAIXO`, `ESTOQUE_ZERADO`, `VENCIMENTO_PROXIMO` |
| `TipoMovimentacao` | `ENTRADA_COMPRA`, `ENTRADA_AJUSTE`, `SAIDA_DOSE`, `SAIDA_AJUSTE`, `SAIDA_DESCARTE` |
| `RolePessoa` | `PACIENTE`, `CUIDADOR`, `ADMIN` |
