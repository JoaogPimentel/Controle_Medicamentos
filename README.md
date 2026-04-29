# Plataforma de Controle de Medicamentos

Sistema para gerenciamento de medicamentos de pacientes, com controle de estoque, posologia, histórico de doses e alertas.

## Funcionalidades

- Cadastro de pacientes e cuidadores com suporte a sobreposição de papéis (a mesma pessoa pode ser paciente e cuidador)
- Vínculo N:N entre pacientes e cuidadores com histórico de relacionamentos
- Catálogo central de medicamentos com princípio ativo e forma farmacêutica
- Controle de estoque rastreável por movimentações auditáveis
- Definição de posologia com intervalo de doses e duração do tratamento
- Histórico de adesão ao tratamento (dose tomada, atrasada, pulada)
- Sistema de alertas para doses próximas, estoque baixo e vencimento

## Tecnologias

- **Java** — camada de modelo (entidades e enums)
- **PostgreSQL 13+** — banco de dados relacional com triggers e views

## Estrutura do projeto

```
devweb/
├── sql/
│   └── DB_schema.sql          # DDL completo: tipos, tabelas, índices, triggers e view
└── src/main/java/model/
    ├── Pessoa.java
    ├── Paciente.java
    ├── Cuidador.java
    ├── PacienteCuidador.java
    ├── MedicamentoCatalogo.java
    ├── Medicamento.java
    ├── Posologia.java
    ├── HistoricoUso.java
    ├── MovimentacaoEstoque.java
    ├── Alerta.java
    ├── FormaFarmaceutica.java
    ├── StatusMedicamento.java
    ├── StatusDose.java
    ├── TipoAlerta.java
    └── TipoMovimentacao.java
```

## Modelo de dados

### Especialização de Pessoa

`Pessoa` é a superclasse. Um mesmo registro pode existir simultaneamente em `Paciente` e `Cuidador` (especialização parcial e sobreposta). O vínculo entre eles é gerenciado por `PacienteCuidador`, que mantém histórico com `data_fim` e flag `ativo`.

### Medicamento e catálogo

Todo medicamento deve referenciar uma entrada em `MedicamentoCatalogo` (`id_catalogo NOT NULL`). O catálogo centraliza nome, princípio ativo e forma farmacêutica. A tabela `Medicamento` armazena a instância específica do paciente: dosagem, estoque e status.

### Controle de estoque

`estoque_atual` em `Medicamento` é um campo derivado — nunca atualizado diretamente pela aplicação. Toda variação passa por `MovimentacaoEstoque`, e uma trigger (`trg_movimentacao_aplica_estoque`) aplica a movimentação com verificação de consistência (optimistic locking via `estoque_antes`).

### Ciclo de vida do status do medicamento

O status (`EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO`) é mantido automaticamente pela trigger `trg_posologia_status_medicamento`:
- Posologia ativada → `EM_USO`
- Última posologia desativada + há estoque → `EM_ESTOQUE`

### Posologia e histórico

`Posologia` define o cronograma de um tratamento (horário, intervalo em horas, quantidade por dose). Cada dose agendada gera uma linha em `HistoricoUso` com o desfecho (`PREVISTA`, `TOMADA`, `ATRASADA`, `PULADA`). Saídas de estoque do tipo `SAIDA_DOSE` devem referenciar o `id_historico` correspondente.

## Configuração do banco

Execute o script DDL no PostgreSQL 13 ou superior:

```bash
psql -U <usuario> -d <banco> -f sql/DB_schema.sql
```

O script é idempotente para ambiente de desenvolvimento: inicia com `DROP TABLE/TYPE IF EXISTS CASCADE` para permitir reexecução limpa.

## Enums

| Enum Java | Tipo PostgreSQL | Valores |
|---|---|---|
| `FormaFarmaceutica` | `forma_farmaceutica_enum` | `COMPRIMIDO`, `CAPSULA`, `LIQUIDO_ML`, `GOTAS`, `INJECAO`, `POMADA`, `SPRAY`, `ADESIVO`, `OUTRO` |
| `StatusMedicamento` | `status_medicamento_enum` | `EM_USO`, `EM_ESTOQUE`, `DESCARTADO`, `ARQUIVADO` |
| `StatusDose` | `status_dose_enum` | `PREVISTA`, `TOMADA`, `ATRASADA`, `PULADA` |
| `TipoAlerta` | `tipo_alerta_enum` | `DOSE_PROXIMA`, `DOSE_ATRASADA`, `ESTOQUE_BAIXO`, `ESTOQUE_ZERADO`, `VENCIMENTO_PROXIMO` |
| `TipoMovimentacao` | `tipo_movimentacao_enum` | `ENTRADA_COMPRA`, `ENTRADA_AJUSTE`, `SAIDA_DOSE`, `SAIDA_AJUSTE`, `SAIDA_DESCARTE` |
