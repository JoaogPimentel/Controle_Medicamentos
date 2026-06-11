# Plataforma de Controle de Medicamentos

Sistema para gerenciamento de medicamentos de pacientes — controle de estoque,
posologia, histórico de doses e alertas. O projeto é dividido em uma **API REST
desacoplada** (Java + Jakarta Servlets) e um **front-end SPA** (React + Vite) que
a consome via HTTP, autenticando-se por **JWT**.


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

Arquitetura desacoplada em duas pastas distintas: **`backend/`** (API Java) e
**`frontend/`** (React, publicado no GitHub Pages).

```
Controle_Medicamentos/
├── .github/workflows/          # CI: deploy automático do front no GitHub Pages
├── frontend/                   # SPA React + Vite (consome a API)
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js          # base /Controle_Medicamentos/ no build; proxy /api em dev
│   └── src/
│       ├── main.jsx
│       ├── App.jsx             # Rotas (HashRouter)
│       ├── pages/              # Login, Cadastro, Dashboard, Medicamentos, Catalogo,
│       │                       #   Posologia, Estoque, Historico, Vinculos, Perfil
│       ├── components/         # Cabecalho, NavPrincipal
│       └── services/           # api.js (fetch + Bearer) + um serviço por recurso
└── backend/                    # API REST Java (Jakarta Servlets + Tomcat embutido)
    ├── Dockerfile              # Build multi-stage (compila e roda a API)
    ├── .dockerignore
    ├── docker-compose.yml      # Sobe PostgreSQL + API juntos
    ├── .env / .env.example     # Configuração (lida por utils/DotEnv) — .env não versionado
    ├── bruno/                  # Collection Bruno de testes da API (ver bruno/README.md)
    ├── lib/                    # Dependências JAR
    ├── sql/
    │   └── database.sql        # DDL completo (carregado pelo Docker no 1º start)
    └── src/main/
        ├── java/
        │   ├── Main.java            # Sobe o Tomcat embutido e registra os servlets
        │   ├── db/                  # ConnectionPool (pool + Proxy) e fachada
        │   ├── model/               # Entidades e enums
        │   ├── dao/                 # Acesso a dados
        │   ├── services/            # Regras de negócio
        │   ├── servlet/
        │   │   ├── filter/          # AuthFilter (CORS + JWT) e registrar
        │   │   ├── AuthServlet.java        # /api/auth/login | cadastrar | logout
        │   │   ├── DashboardServlet.java   # GET /api/dashboard
        │   │   └── ...                     # demais recursos da API
        │   └── utils/
        │       ├── DotEnv.java      # Leitor do .env (fonte de configuração local)
        │       ├── JwtUtil.java     # Geração/validação de JWT (HS256)
        │       ├── CorsConfig.java  # CORS configurável via CORS_ORIGIN
        │       ├── Hasher.java      # PBKDF2-SHA256
        │       └── JsonUtil.java    # Serialização/parsing JSON
        └── webapp/css/style.css
```

## Variáveis de ambiente

Copie `backend/.env.example` para `backend/.env` e ajuste. A aplicação lê o
`.env` automaticamente (via `utils.DotEnv`) — **não é preciso exportar variáveis no
shell**. Uma variável de ambiente do processo, se definida, tem precedência sobre o
`.env` (é assim que o `docker-compose` injeta `DB_URL` apontando para o serviço `db`).

| Variável | Uso | Padrão |
|----------|-----|--------|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Conexão com o PostgreSQL (lidas pelo `ConnectionPool`) | — |
| `JWT_SECRET` | Segredo de assinatura do JWT | _dev fallback (com aviso)_ |
| `CORS_ORIGIN` | Origens permitidas (lista por vírgula) | `http://localhost:5173` |
| `POSTGRES_*` | Credenciais do container do docker-compose | ver `.env.example` |

## Como rodar

### Pré-requisitos
- Docker + Docker Compose (caminho recomendado — sobe banco e API juntos)
- Node.js 18+ (para o front)
- Java JDK 11+ (apenas se quiser rodar a API sem Docker)

### 1. Back-end (Docker — recomendado)

Sobe **PostgreSQL + API** com um comando. Tudo a partir de `backend/`:

```bash
cd backend
cp .env.example .env      # ajuste as credenciais se quiser
docker compose up -d --build
```

- A API fica em `http://localhost:8080`; o banco na porta `POSTGRES_PORT` (padrão `5432`).
- A API só inicia depois que o banco fica *healthy* (`depends_on: service_healthy`).
- Dentro da rede do compose, a API alcança o banco pelo serviço `db` — o
  `docker-compose` injeta `DB_URL=jdbc:postgresql://db:5432/...`, sobrepondo o `.env`.
- O schema (`sql/database.sql`) é carregado no primeiro start (volume vazio).

Para parar: `docker compose down` (os dados persistem no volume `pgdata`).

### 2. Back-end (sem Docker — alternativa)

Requer JDK 11+ e um PostgreSQL acessível (suba só o banco com
`docker compose up -d db` e ajuste `DB_URL` no `.env` para `localhost`).
Rode **de dentro de `backend/`** (os caminhos são relativos):

```powershell
cd backend
New-Item -ItemType Directory -Force -Path "out\classes" | Out-Null
$arquivos = Get-ChildItem -Recurse -Path "src\main\java" -Filter "*.java" | Resolve-Path -Relative
[System.IO.File]::WriteAllLines("sources.txt", $arquivos)
javac -encoding UTF-8 -cp "lib\*" -d "out\classes" "@sources.txt"

# Executar (credenciais e config lidas do .env automaticamente)
java -cp "out\classes;lib\*" Main
```

A API sobe em `http://localhost:8080`.

### 3. Front-end (React)

```bash
cd frontend
npm install
npm run dev               # Vite em http://localhost:5173 (proxy /api → :8080)
```

### 4. Testes de API (Bruno)

Abra a pasta `backend/bruno/` no [Bruno](https://www.usebruno.com/), selecione o
ambiente **Local** e siga a ordem descrita em `backend/bruno/README.md`. Cobre
GET/POST/PUT/DELETE de todos os recursos, com o token e os IDs preenchidos automaticamente.

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

---

## Módulos desenvolvidos por Lucas Ardelino

### Banco de Dados (`backend/sql/database.sql`)
Modelagem completa do banco: tabelas com herança (`pessoa → paciente/cuidador`), triggers de auditoria, views para consultas frequentes e índices de performance.

### Módulo de Vínculos (`frontend/src/`)
- `services/vinculos.js` — serviço com todas as chamadas à API REST de vínculos (GET, POST, DELETE)
- `pages/VinculosPage.jsx` — página completa com lógica de papel (CUIDADOR cria/encerra vínculos; PACIENTE visualiza cuidadores), busca por e-mail, indicador de carregamento e filtro por status

### Módulo de Perfil (`frontend/src/`)
- `services/perfil.js` — serviço para buscar e atualizar dados do perfil via API
- `pages/PerfilPage.jsx` — página de perfil do usuário logado com formulário de edição de nome
- `components/NavPrincipal.jsx` — adicionados links de Vínculos (todos os papéis) e Perfil na navegação
