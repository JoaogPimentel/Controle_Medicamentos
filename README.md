# Plataforma de Controle de Medicamentos

Sistema web para gerenciamento de medicamentos de pacientes, com controle de estoque, posologia, histórico de doses e alertas. Desenvolvido em Java com Servlets, JSP e PostgreSQL, seguindo o padrão MVC.

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Front-end | HTML5, CSS3, JavaScript, JSP (JavaServer Pages) |
| Back-end | Java 11+, Jakarta Servlets |
| Banco de dados | PostgreSQL 13+ |
| Servidor | Apache Tomcat 10.1 (embutido) |

## Arquitetura MVC

```
Requisição HTTP
      │
      ▼
 AuthFilter          ← verifica sessão em todas as rotas protegidas
      │
      ▼
  Servlet            ← Controller: processa dados, chama Services/DAOs
      │
      ├─ forward() ──► JSP (View)     ← renderiza HTML com atributos da requisição
      │
      └─ JSON ────────► Cliente API   ← para chamadas REST
```

### Controller
Servlets em `servlet/` interceptam as requisições HTTP, interagem com a camada de serviço e utilizam `RequestDispatcher.forward()` para repassar dados às Views JSP.

### Model
JavaBeans em `model/`, DAOs em `dao/` e regras de negócio em `services/`. O acesso ao banco é centralizado via `ConexaoDB`, que delega a um pool de conexões.

### View
Páginas JSP em `src/main/webapp/WEB-INF/views/` responsáveis apenas por renderizar os atributos preparados pelo Controller via Expression Language (EL) e scriptlets.

## Funcionalidades

- Cadastro e autenticação de usuários com hash de senha (PBKDF2-SHA256)
- Papéis de acesso: `PACIENTE`, `CUIDADOR`, `ADMIN`
- Vínculo N:N entre pacientes e cuidadores (apenas cuidadores podem gerenciar vínculos)
- Catálogo central de medicamentos com princípio ativo e forma farmacêutica
- Controle de estoque rastreável por movimentações imutáveis e auditáveis
- Definição de posologia com intervalo de doses e duração do tratamento
- Histórico de adesão ao tratamento (dose tomada, atrasada, pulada)
- Sistema de alertas para doses próximas, estoque baixo e vencimento

## Segurança

- **Autenticação:** login por formulário (`/login`) com sessão `HttpSession` (timeout: 30 min)
- **Cookie:** opção "lembrar e-mail" grava cookie `HttpOnly` com validade de 30 dias
- **Autorização:** `AuthFilter` protege todas as rotas; rotas de API retornam `401 JSON` sem sessão
- **Papéis:** endpoints críticos verificam o papel do usuário na sessão (ex: `403` para PACIENTE em rotas de vínculo)
- **Cache:** páginas autenticadas recebem `Cache-Control: no-store`; arquivos estáticos recebem `max-age=86400`
- **Validação:** formulários validados no front-end (JavaScript) e no back-end (Servlet)

## Armazenamento

| Mecanismo | Uso |
|-----------|-----|
| PostgreSQL | Persistência principal de todos os dados |
| Connection Pool | Pool de 10 conexões com reuso via `java.lang.reflect.Proxy` |
| `HttpSession` | Objeto `UsuarioSessao` (id, nome, email, papel) durante a navegação |
| Cookie | `lembrar_email` — preferência persistida no browser do cliente |
| Cache HTTP | Headers `Cache-Control` configurados por tipo de recurso |

## Estrutura do projeto

```
devweb/
├── lib/                            # Dependências JAR
│   ├── tomcat-embed-core.jar
│   ├── tomcat-embed-jasper.jar
│   ├── tomcat-embed-el.jar
│   ├── servlet-api.jar
│   ├── el-api.jar
│   ├── jakarta.annotation-api.jar
│   ├── jakarta.servlet.jsp-api.jar
│   ├── ecj.jar
│   └── postgresql-*.jar
├── src/main/
│   ├── java/
│   │   ├── Main.java               # Inicialização do Tomcat embutido
│   │   ├── db/
│   │   │   ├── ConnectionPool.java # Pool de conexões (ArrayBlockingQueue + Proxy)
│   │   │   └── ConexaoDB.java      # Fachada para o pool
│   │   ├── model/                  # Entidades e enums (JavaBeans)
│   │   ├── dao/                    # Acesso a dados (PreparedStatement)
│   │   ├── services/               # Regras de negócio
│   │   ├── servlet/
│   │   │   ├── filter/
│   │   │   │   ├── AuthFilter.java          # Filtro de autenticação global
│   │   │   │   └── AuthFilterRegistrar.java # Registra o filtro via ServletContextListener
│   │   │   ├── AuthServlet.java      # POST /api/auth/login | cadastrar | GET /logout
│   │   │   ├── LoginPageServlet.java # GET /login (exibe JSP) | POST /login (processa form)
│   │   │   ├── DashboardServlet.java # GET /dashboard → dashboard.jsp
│   │   │   ├── MedicamentoServlet.java
│   │   │   ├── AlertaServlet.java
│   │   │   └── VinculoServlet.java
│   │   └── utils/
│   │       ├── Hasher.java         # PBKDF2-SHA256 (310.000 iterações)
│   │       └── JsonUtil.java       # Serialização/parsing JSON manual
│   ├── resources/
│   │   └── db.properties           # Credenciais do banco (não versionado)
│   └── webapp/
│       ├── WEB-INF/views/
│       │   ├── login.jsp           # View: tela de login
│       │   └── dashboard.jsp       # View: dashboard com alertas
│       └── css/
│           └── style.css
└── sql/
    └── DB_schema.sql               # DDL completo (não versionado)
```

## Configuração e execução

### Pré-requisitos

- Java JDK 11 ou superior
- PostgreSQL 13 ou superior

### 1. Banco de dados

```bash
psql -U postgres -c "CREATE DATABASE devweb;"
psql -U postgres -d devweb -f sql/DB_schema.sql
```

Crie o arquivo `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/devweb
db.user=postgres
db.password=sua_senha
```

### 2. Compilação

```powershell
New-Item -ItemType Directory -Force -Path "out\classes" | Out-Null
$arquivos = (Get-ChildItem -Recurse -Path "src\main\java" -Filter "*.java").FullName
[System.IO.File]::WriteAllLines("sources.txt", $arquivos)
javac -encoding UTF-8 -cp "lib\*" -d "out\classes" "@sources.txt"
```

### 3. Execução

```powershell
java -cp "out\classes;lib\*" Main
```

O servidor sobe em `http://localhost:8080`.

### 4. Primeiro acesso

Crie um usuário via API e faça login pelo browser:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/cadastrar" `
  -Method POST -ContentType "application/json" `
  -Body '{"nome":"Seu Nome","email":"seu@email.com","senha":"senha123","data_nascimento":"1995-01-01"}'
```

Acesse `http://localhost:8080/login` no browser.

## Rotas

### Páginas (MVC — retornam HTML via JSP)

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/login` | Exibe formulário de login |
| `POST` | `/login` | Processa credenciais, cria sessão |
| `GET` | `/dashboard` | Dashboard do usuário logado |
| `GET` | `/api/auth/logout` | Encerra sessão e limpa cookie |

### API REST (retornam JSON)

| Método | Rota | Autenticação | Descrição |
|--------|------|-------------|-----------|
| `POST` | `/api/auth/cadastrar` | Pública | Cadastra novo usuário |
| `POST` | `/api/auth/login` | Pública | Autentica e cria sessão |
| `GET` | `/api/catalogo` | Sessão | Lista catálogo (ou `?nome=` para busca) |
| `GET` | `/api/catalogo/{id}` | Sessão | Retorna entrada do catálogo por ID |
| `POST` | `/api/catalogo` | Sessão | Cadastra medicamento no catálogo |
| `PUT` | `/api/catalogo/{id}` | Sessão | Atualiza entrada do catálogo |
| `GET` | `/api/medicamentos?paciente={id}` | Sessão | Lista medicamentos do paciente |
| `GET` | `/api/medicamentos/{id}` | Sessão | Retorna medicamento por ID |
| `POST` | `/api/medicamentos` | Sessão | Cadastra medicamento |
| `PUT` | `/api/medicamentos/{id}` | Sessão | Atualiza dosagem, estoque mínimo, validade, status |
| `DELETE` | `/api/medicamentos/{id}` | Sessão | Arquiva medicamento |
| `POST` | `/api/medicamentos/dose` | Sessão | Registra dose tomada |
| `POST` | `/api/medicamentos/tratamento` | Sessão | Inicia posologia |
| `GET` | `/api/posologias?medicamento={id}` | Sessão | Lista posologias do medicamento |
| `DELETE` | `/api/posologias/{id}` | Sessão | Desativa posologia |
| `GET` | `/api/historico?posologia={id}` | Sessão | Lista histórico de doses |
| `GET` | `/api/estoque?medicamento={id}` | Sessão | Lista movimentações de estoque |
| `POST` | `/api/estoque` | Sessão | Registra entrada de estoque |
| `GET` | `/api/alertas?pessoa={id}` | Sessão | Lista alertas não lidos |
| `POST` | `/api/alertas/{id}/lido` | Sessão | Marca alerta como lido |
| `GET` | `/api/vinculos?paciente={id}` | Sessão | Lista vínculos ativos do paciente |
| `GET` | `/api/vinculos?cuidador={id}` | Sessão | Lista vínculos ativos do cuidador |
| `POST` | `/api/vinculos` | Sessão + CUIDADOR | Cria vínculo paciente-cuidador |
| `DELETE` | `/api/vinculos/{id}` | Sessão + CUIDADOR | Encerra vínculo |

## Modelo de dados

### Especialização de Pessoa

`Pessoa` é a entidade base. Um mesmo registro pode existir simultaneamente em `Paciente` e `Cuidador` (especialização parcial e sobreposta). O vínculo entre eles é gerenciado por `PacienteCuidador`, com histórico via `data_fim` e flag `ativo`.

### Controle de estoque

`estoque_atual` em `Medicamento` é derivado — nunca atualizado diretamente. Toda variação passa por `MovimentacaoEstoque`, e a trigger `trg_movimentacao_aplica_estoque` aplica a movimentação com verificação de consistência (optimistic locking via `estoque_antes`).

### Ciclo de vida do medicamento

O status (`EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO`) é mantido automaticamente pela trigger `trg_posologia_status_medicamento`.

## Enums

| Enum Java | Valores |
|-----------|---------|
| `FormaFarmaceutica` | `COMPRIMIDO`, `CAPSULA`, `LIQUIDO_ML`, `GOTAS`, `INJECAO`, `POMADA`, `SPRAY`, `ADESIVO`, `OUTRO` |
| `StatusMedicamento` | `EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO` |
| `StatusDose` | `PREVISTA`, `TOMADA`, `ATRASADA`, `PULADA` |
| `TipoAlerta` | `DOSE_PROXIMA`, `DOSE_ATRASADA`, `ESTOQUE_BAIXO`, `ESTOQUE_ZERADO`, `VENCIMENTO_PROXIMO` |
| `TipoMovimentacao` | `ENTRADA_COMPRA`, `ENTRADA_AJUSTE`, `SAIDA_DOSE`, `SAIDA_AJUSTE`, `SAIDA_DESCARTE` |
| `RolePessoa` | `PACIENTE`, `CUIDADOR`, `ADMIN` |
