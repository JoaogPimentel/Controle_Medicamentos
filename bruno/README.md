# Collection Bruno — Controle de Medicamentos

Testes de API REST cobrindo **GET, POST, PUT e DELETE** de cada recurso.

## Como usar

1. Instale o [Bruno](https://www.usebruno.com/) e abra esta pasta (`bruno/`) como collection.
2. Selecione o ambiente **Local** (canto superior direito). Ajuste `baseUrl`
   se a API não estiver em `http://localhost:8080`.
3. Suba a API (`docker-compose up` para o banco + `java Main`, ou conforme o README do projeto).
4. Rode os requests na ordem abaixo — o `token` e os IDs são salvos
   automaticamente no ambiente entre as chamadas.

## Sequência ponta-a-ponta

1. **Auth › 01 Cadastrar Paciente** e **02 Cadastrar Cuidador** (uma vez; 400 se já existirem)
2. **Auth › 03 Login Paciente** → salva `token` + `idPaciente`
3. **Catalogo › 02 Criar** → salva `idCatalogo`
4. **Medicamentos › 01 Criar** → salva `idMedicamento`
5. **Medicamentos › 04 Iniciar tratamento**
6. **Posologias › 01 Listar** → salva `idPosologia`
7. **Medicamentos › 05 Registrar dose**
8. **Estoque › 01 Movimentar** e **02 Listar**
9. **Historico › 01 Listar por posologia**
10. **Alertas › 01 Listar** → salva `idAlerta` → **02 Marcar como lido**
11. **Dashboard › 01 Resumo**
12. **Auth › 04 Login Cuidador** → troca o `token` para o cuidador
13. **Vinculos › 01 Buscar paciente → 02 Criar → 03 Listar → 04 Encerrar**

## Variáveis de ambiente

| Variável | Origem |
|----------|--------|
| `token` | salvo no login (paciente ou cuidador) |
| `idPaciente` / `idCuidador` | salvos nos respectivos logins |
| `idCatalogo` | criar catálogo |
| `idMedicamento` | criar medicamento |
| `idPosologia` | listar posologias |
| `idVinculo` | criar vínculo |
| `idAlerta` | listar alertas |

## Autenticação

Todas as rotas (exceto `POST /api/auth/login` e `POST /api/auth/cadastrar`)
exigem o header `Authorization: Bearer <token>`, já configurado via `auth: bearer`
em cada request. Sem token válido a API responde **401**. Rotas de vínculos
exigem papel **CUIDADOR** (senão **403**).
