-- =============================================================================
-- PLATAFORMA DE CONTROLE DE MEDICAMENTOS
-- Script DDL - Criação de schema, tipos, tabelas, constraints e índices
-- SGBD: PostgreSQL 13+
-- =============================================================================
-- Decisões de modelagem documentadas inline.
-- Modelo em 3FN. Especialização parcial e sobreposta (Pessoa -> Paciente/Cuidador)
-- para permitir que a mesma pessoa exerça ambos os papéis.
-- Estoque rastreável via movimentacao_estoque, com sincronização por trigger.
-- Desacoplamento entre "farmácia doméstica" (medicamento em estoque) e
-- "tratamento em curso" (posologia ativa) via enum status_medicamento_enum.
-- =============================================================================

-- Limpeza segura para reexecução em ambiente de desenvolvimento.
DROP TABLE IF EXISTS log_acesso            CASCADE;
DROP TABLE IF EXISTS alerta                CASCADE;
DROP TABLE IF EXISTS movimentacao_estoque  CASCADE;
DROP TABLE IF EXISTS historico_uso         CASCADE;
DROP TABLE IF EXISTS posologia             CASCADE;
DROP TABLE IF EXISTS medicamento           CASCADE;
DROP TABLE IF EXISTS medicamento_catalogo  CASCADE;
DROP TABLE IF EXISTS paciente_cuidador     CASCADE;
DROP TABLE IF EXISTS cuidador              CASCADE;
DROP TABLE IF EXISTS paciente              CASCADE;
DROP TABLE IF EXISTS pessoa                CASCADE;

DROP TYPE IF EXISTS forma_farmaceutica_enum   CASCADE;
DROP TYPE IF EXISTS status_dose_enum          CASCADE;
DROP TYPE IF EXISTS tipo_alerta_enum          CASCADE;
DROP TYPE IF EXISTS status_medicamento_enum   CASCADE;
DROP TYPE IF EXISTS tipo_movimentacao_enum    CASCADE;

-- =============================================================================
-- TIPOS ENUMERADOS
-- =============================================================================

CREATE TYPE forma_farmaceutica_enum AS ENUM (
    'COMPRIMIDO',
    'CAPSULA',
    'LIQUIDO_ML',
    'GOTAS',
    'INJECAO',
    'POMADA',
    'SPRAY',
    'ADESIVO',
    'OUTRO'
);

CREATE TYPE status_dose_enum AS ENUM (
    'PREVISTA',
    'TOMADA',
    'ATRASADA',
    'PULADA'
);

CREATE TYPE tipo_alerta_enum AS ENUM (
    'DOSE_PROXIMA',
    'DOSE_ATRASADA',
    'ESTOQUE_BAIXO',
    'ESTOQUE_ZERADO',
    'VENCIMENTO_PROXIMO'
);

-- Desacopla "tenho em casa" de "estou tomando".
CREATE TYPE status_medicamento_enum AS ENUM (
    'EM_USO',       -- há posologia ativa
    'EM_ESTOQUE',   -- sobrou de tratamento anterior, disponível na farmácia doméstica
    'DESCARTADO',   -- vencido ou descartado
    'ARQUIVADO'     -- ocultado pelo paciente
);

CREATE TYPE tipo_movimentacao_enum AS ENUM (
    'ENTRADA_COMPRA',
    'ENTRADA_AJUSTE',
    'SAIDA_DOSE',
    'SAIDA_AJUSTE',
    'SAIDA_DESCARTE'
);

-- =============================================================================
-- TABELA: pessoa
-- =============================================================================

CREATE TABLE pessoa (
    id_pessoa        SERIAL       PRIMARY KEY,
    nome             VARCHAR(120) NOT NULL,
    email            VARCHAR(120) NOT NULL UNIQUE,
    senha_hash       VARCHAR(255) NOT NULL,
    data_nascimento  DATE         NOT NULL,
    telefone         VARCHAR(20),
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    data_cadastro    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_pessoa_email_formato
        CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_pessoa_nascimento_passado
        CHECK (data_nascimento < CURRENT_DATE)
);

COMMENT ON TABLE  pessoa IS 'Superclasse da especialização. Todo usuário do sistema é uma pessoa.';
COMMENT ON COLUMN pessoa.senha_hash IS 'Hash BCrypt da senha. Nunca armazenar a senha em texto plano.';

-- =============================================================================
-- TABELA: paciente
-- =============================================================================

CREATE TABLE paciente (
    id_pessoa             INTEGER      PRIMARY KEY,
    convenio              VARCHAR(100),
    observacoes_medicas   TEXT,
    data_virou_paciente   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paciente_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
        ON DELETE CASCADE ON UPDATE CASCADE
);

COMMENT ON TABLE paciente IS 'Especialização de pessoa. Mesma pessoa pode existir aqui e em cuidador simultaneamente.';

-- =============================================================================
-- TABELA: cuidador
-- =============================================================================

CREATE TABLE cuidador (
    id_pessoa             INTEGER      PRIMARY KEY,
    profissional_saude    BOOLEAN      NOT NULL DEFAULT FALSE,
    registro_profissional VARCHAR(30),
    data_virou_cuidador   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cuidador_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_cuidador_registro_profissional
        CHECK (profissional_saude = FALSE OR registro_profissional IS NOT NULL)
);

COMMENT ON TABLE cuidador IS 'Especialização de pessoa. Permite sobreposição com paciente.';

-- =============================================================================
-- TABELA: paciente_cuidador
-- =============================================================================

CREATE TABLE paciente_cuidador (
    id_vinculo       SERIAL       PRIMARY KEY,
    id_paciente      INTEGER      NOT NULL,
    id_cuidador      INTEGER      NOT NULL,
    parentesco       VARCHAR(50),
    data_vinculo     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_fim         TIMESTAMP,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    data_atualizacao TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_pc_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente(id_pessoa)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pc_cuidador
        FOREIGN KEY (id_cuidador) REFERENCES cuidador(id_pessoa)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_pc_pessoas_distintas
        CHECK (id_paciente <> id_cuidador),

    CONSTRAINT uk_pc_vinculo_ativo
        UNIQUE (id_paciente, id_cuidador, ativo)
);

COMMENT ON TABLE paciente_cuidador IS 'Vínculo N:N entre paciente e cuidador, com histórico.';

-- =============================================================================
-- TABELA: medicamento_catalogo
-- =============================================================================

CREATE TABLE medicamento_catalogo (
    id_catalogo         SERIAL                    PRIMARY KEY,
    nome                VARCHAR(150)              NOT NULL UNIQUE,
    principio_ativo     VARCHAR(150)              NOT NULL,
    forma_farmaceutica  forma_farmaceutica_enum   NOT NULL,
    data_cadastro       TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao    TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE medicamento_catalogo IS 'Catálogo central de medicamentos comuns.';

-- =============================================================================
-- TABELA: medicamento
-- "status" desacopla farmácia doméstica de tratamento em curso.
-- "estoque_atual" é campo derivado, mantido pela trigger de movimentacao_estoque.
-- =============================================================================

CREATE TABLE medicamento (
    id_medicamento                  SERIAL                    PRIMARY KEY,
    id_paciente                     INTEGER                   NOT NULL,
    id_catalogo                     INTEGER,
    nome_customizado                VARCHAR(150),
    forma_farmaceutica_customizada  forma_farmaceutica_enum,
    dosagem                         VARCHAR(50)               NOT NULL,
    estoque_atual                   NUMERIC(10,2)             NOT NULL DEFAULT 0,
    estoque_minimo                  NUMERIC(10,2)             NOT NULL DEFAULT 0,
    data_validade                   DATE,
    status                          status_medicamento_enum   NOT NULL DEFAULT 'EM_ESTOQUE',
    id_cadastrado_por               INTEGER                   NOT NULL,
    data_cadastro                   TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao                TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_medicamento_paciente
        FOREIGN KEY (id_paciente) REFERENCES paciente(id_pessoa)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_medicamento_catalogo
        FOREIGN KEY (id_catalogo) REFERENCES medicamento_catalogo(id_catalogo)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_medicamento_cadastrado_por
        FOREIGN KEY (id_cadastrado_por) REFERENCES pessoa(id_pessoa)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    -- XOR: ou catálogo, ou customizado. Resolve 3FN.
    CONSTRAINT chk_medicamento_origem_xor
        CHECK (
            (id_catalogo IS NOT NULL AND nome_customizado IS NULL AND forma_farmaceutica_customizada IS NULL)
         OR (id_catalogo IS NULL AND nome_customizado IS NOT NULL AND forma_farmaceutica_customizada IS NOT NULL)
        ),

    CONSTRAINT chk_medicamento_estoques_nao_negativos
        CHECK (estoque_atual >= 0 AND estoque_minimo >= 0)
);

COMMENT ON TABLE  medicamento IS 'Medicamento de um paciente. Coexiste "em uso" e "em estoque" (farmácia doméstica).';
COMMENT ON COLUMN medicamento.status IS 'Desacopla ciclo do medicamento do ciclo do tratamento. Mantido por trigger.';
COMMENT ON COLUMN medicamento.estoque_atual IS 'Campo derivado, mantido por trigger a partir de movimentacao_estoque. Jamais atualizado diretamente pela aplicação.';

-- =============================================================================
-- TABELA: posologia
-- =============================================================================

CREATE TABLE posologia (
    id_posologia           SERIAL        PRIMARY KEY,
    id_medicamento         INTEGER       NOT NULL,
    horario_primeira_dose  TIME          NOT NULL,
    intervalo_horas        SMALLINT      NOT NULL,
    quantidade_por_dose    NUMERIC(10,2) NOT NULL,
    duracao_dias           SMALLINT,
    data_inicio            DATE          NOT NULL DEFAULT CURRENT_DATE,
    ativo                  BOOLEAN       NOT NULL DEFAULT TRUE,
    data_cadastro          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_posologia_medicamento
        FOREIGN KEY (id_medicamento) REFERENCES medicamento(id_medicamento)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT chk_posologia_intervalo
        CHECK (intervalo_horas > 0 AND intervalo_horas <= 24),
    CONSTRAINT chk_posologia_quantidade
        CHECK (quantidade_por_dose > 0),
    CONSTRAINT chk_posologia_duracao
        CHECK (duracao_dias IS NULL OR duracao_dias > 0)
);

COMMENT ON TABLE posologia IS 'Cronograma de tomada. Um medicamento pode ter múltiplas posologias ao longo do tempo.';

-- =============================================================================
-- TABELA: historico_uso
-- =============================================================================

CREATE TABLE historico_uso (
    id_historico        SERIAL             PRIMARY KEY,
    id_posologia        INTEGER            NOT NULL,
    horario_previsto    TIMESTAMP          NOT NULL,
    horario_real        TIMESTAMP,
    status              status_dose_enum   NOT NULL DEFAULT 'PREVISTA',
    quantidade_tomada   NUMERIC(10,2),
    id_registrado_por   INTEGER,
    observacao          TEXT,
    data_registro       TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_historico_posologia
        FOREIGN KEY (id_posologia) REFERENCES posologia(id_posologia)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_historico_registrado_por
        FOREIGN KEY (id_registrado_por) REFERENCES pessoa(id_pessoa)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT chk_historico_tomada_completa
        CHECK (
            status <> 'TOMADA'
            OR (horario_real IS NOT NULL AND id_registrado_por IS NOT NULL)
        ),
    CONSTRAINT chk_historico_quantidade
        CHECK (quantidade_tomada IS NULL OR quantidade_tomada >= 0)
);

COMMENT ON TABLE historico_uso IS 'Adesão ao tratamento. Cada linha = uma dose agendada, com desfecho.';

-- =============================================================================
-- TABELA: movimentacao_estoque
-- Fonte da verdade para estoque. medicamento.estoque_atual é derivado daqui.
-- =============================================================================

CREATE TABLE movimentacao_estoque (
    id_movimentacao     SERIAL                  PRIMARY KEY,
    id_medicamento      INTEGER                 NOT NULL,
    tipo                tipo_movimentacao_enum  NOT NULL,
    quantidade          NUMERIC(10,2)           NOT NULL,
    estoque_antes       NUMERIC(10,2)           NOT NULL,
    estoque_depois      NUMERIC(10,2)           NOT NULL,
    id_historico_uso    INTEGER,
    id_registrado_por   INTEGER                 NOT NULL,
    observacao          TEXT,
    data_movimentacao   TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_mov_medicamento
        FOREIGN KEY (id_medicamento) REFERENCES medicamento(id_medicamento)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mov_historico
        FOREIGN KEY (id_historico_uso) REFERENCES historico_uso(id_historico)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_mov_registrado_por
        FOREIGN KEY (id_registrado_por) REFERENCES pessoa(id_pessoa)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT chk_mov_quantidade_positiva
        CHECK (quantidade > 0),
    CONSTRAINT chk_mov_estoques_nao_negativos
        CHECK (estoque_antes >= 0 AND estoque_depois >= 0),

    -- SAIDA_DOSE deve referenciar a dose específica.
    CONSTRAINT chk_mov_saida_dose_vinculada
        CHECK (tipo <> 'SAIDA_DOSE' OR id_historico_uso IS NOT NULL),

    -- Coerência: depois = antes +/- quantidade, conforme o tipo.
    CONSTRAINT chk_mov_aritmetica
        CHECK (
            (tipo IN ('ENTRADA_COMPRA', 'ENTRADA_AJUSTE')
                AND estoque_depois = estoque_antes + quantidade)
         OR (tipo IN ('SAIDA_DOSE', 'SAIDA_AJUSTE', 'SAIDA_DESCARTE')
                AND estoque_depois = estoque_antes - quantidade)
        )
);

COMMENT ON TABLE  movimentacao_estoque IS 'Fonte da verdade para estoque. Cada linha documenta uma variação auditável.';
COMMENT ON COLUMN movimentacao_estoque.id_historico_uso IS 'Obrigatório quando tipo = SAIDA_DOSE.';

-- =============================================================================
-- TABELA: alerta
-- =============================================================================

CREATE TABLE alerta (
    id_alerta       SERIAL            PRIMARY KEY,
    id_pessoa       INTEGER           NOT NULL,
    id_medicamento  INTEGER,
    tipo            tipo_alerta_enum  NOT NULL,
    mensagem        VARCHAR(500)      NOT NULL,
    lido            BOOLEAN           NOT NULL DEFAULT FALSE,
    data_geracao    TIMESTAMP         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_leitura    TIMESTAMP,

    CONSTRAINT fk_alerta_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_alerta_medicamento
        FOREIGN KEY (id_medicamento) REFERENCES medicamento(id_medicamento)
        ON DELETE SET NULL ON UPDATE CASCADE,

    CONSTRAINT chk_alerta_leitura_coerente
        CHECK ((lido = FALSE AND data_leitura IS NULL) OR (lido = TRUE AND data_leitura IS NOT NULL))
);

COMMENT ON TABLE alerta IS 'Notificações geradas pelo sistema.';

-- =============================================================================
-- TABELA: log_acesso
-- =============================================================================

CREATE TABLE log_acesso (
    id_log          SERIAL       PRIMARY KEY,
    id_pessoa       INTEGER,
    email_tentado   VARCHAR(120) NOT NULL,
    ip              VARCHAR(45)  NOT NULL,
    user_agent      VARCHAR(255),
    sucesso         BOOLEAN      NOT NULL,
    data_hora       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_log_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id_pessoa)
        ON DELETE SET NULL ON UPDATE CASCADE
);

COMMENT ON TABLE log_acesso IS 'Auditoria de login. Detecção de tentativas de força bruta.';

-- =============================================================================
-- ÍNDICES ESTRATÉGICOS
-- =============================================================================

CREATE INDEX idx_pessoa_email_ativo
    ON pessoa(email) WHERE ativo = TRUE;

-- Dashboard "tomar hoje": apenas medicamentos em uso
CREATE INDEX idx_medicamento_paciente_em_uso
    ON medicamento(id_paciente) WHERE status = 'EM_USO';

-- Farmácia doméstica: medicamentos disponíveis (em uso OU em estoque)
CREATE INDEX idx_medicamento_paciente_disponivel
    ON medicamento(id_paciente) WHERE status IN ('EM_USO', 'EM_ESTOQUE');

-- Alerta de vencimento
CREATE INDEX idx_medicamento_validade
    ON medicamento(data_validade)
    WHERE data_validade IS NOT NULL AND status IN ('EM_USO', 'EM_ESTOQUE');

CREATE INDEX idx_pc_cuidador_ativo
    ON paciente_cuidador(id_cuidador) WHERE ativo = TRUE;
CREATE INDEX idx_pc_paciente_ativo
    ON paciente_cuidador(id_paciente) WHERE ativo = TRUE;

CREATE INDEX idx_historico_posologia_previsto
    ON historico_uso(id_posologia, horario_previsto);
CREATE INDEX idx_historico_status_previsto
    ON historico_uso(status, horario_previsto);

CREATE INDEX idx_alerta_pessoa_nao_lidos
    ON alerta(id_pessoa, data_geracao DESC) WHERE lido = FALSE;

-- Auditoria de estoque: movimentações por medicamento mais recentes primeiro
CREATE INDEX idx_mov_medicamento_data
    ON movimentacao_estoque(id_medicamento, data_movimentacao DESC);

CREATE INDEX idx_log_acesso_email_data
    ON log_acesso(email_tentado, data_hora DESC);
CREATE INDEX idx_log_acesso_ip_data
    ON log_acesso(ip, data_hora DESC);

-- =============================================================================
-- TRIGGERS
-- =============================================================================

-- Trigger genérico para data_atualizacao em todas as tabelas relevantes.
CREATE OR REPLACE FUNCTION fn_atualizar_data_atualizacao()
RETURNS TRIGGER AS $$
BEGIN
    NEW.data_atualizacao = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pessoa_upd
    BEFORE UPDATE ON pessoa
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_paciente_upd
    BEFORE UPDATE ON paciente
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_cuidador_upd
    BEFORE UPDATE ON cuidador
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_paciente_cuidador_upd
    BEFORE UPDATE ON paciente_cuidador
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_medicamento_catalogo_upd
    BEFORE UPDATE ON medicamento_catalogo
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_medicamento_upd
    BEFORE UPDATE ON medicamento
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

CREATE TRIGGER trg_posologia_upd
    BEFORE UPDATE ON posologia
    FOR EACH ROW EXECUTE FUNCTION fn_atualizar_data_atualizacao();

-- =============================================================================
-- TRIGGER: sincronização de estoque
-- A aplicação NUNCA atualiza medicamento.estoque_atual diretamente.
-- Em vez disso, insere uma linha em movimentacao_estoque e este trigger aplica.
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_aplicar_movimentacao_estoque()
RETURNS TRIGGER AS $$
DECLARE
    v_estoque_atual NUMERIC(10,2);
BEGIN
    -- Busca estoque atual com lock para evitar condição de corrida
    SELECT estoque_atual INTO v_estoque_atual
      FROM medicamento
     WHERE id_medicamento = NEW.id_medicamento
     FOR UPDATE;

    IF v_estoque_atual IS NULL THEN
        RAISE EXCEPTION 'Medicamento % não encontrado.', NEW.id_medicamento;
    END IF;

    -- Valida coerência entre o estoque informado e o atual
    IF NEW.estoque_antes <> v_estoque_atual THEN
        RAISE EXCEPTION
            'Inconsistência de estoque no medicamento %: informado % mas atual é %.',
            NEW.id_medicamento, NEW.estoque_antes, v_estoque_atual;
    END IF;

    -- Aplica a movimentação
    UPDATE medicamento
       SET estoque_atual = NEW.estoque_depois
     WHERE id_medicamento = NEW.id_medicamento;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimentacao_aplica_estoque
    BEFORE INSERT ON movimentacao_estoque
    FOR EACH ROW EXECUTE FUNCTION fn_aplicar_movimentacao_estoque();

COMMENT ON FUNCTION fn_aplicar_movimentacao_estoque() IS
    'Sincroniza medicamento.estoque_atual com a movimentação inserida.';

-- =============================================================================
-- TRIGGER: transição automática de status do medicamento
-- Quando posologia é ativada, medicamento vai para EM_USO.
-- Quando posologia é desativada e não há outras ativas, se há estoque,
-- medicamento volta para EM_ESTOQUE (farmácia doméstica).
-- =============================================================================

CREATE OR REPLACE FUNCTION fn_transicionar_status_medicamento()
RETURNS TRIGGER AS $$
DECLARE
    v_tem_posologia_ativa BOOLEAN;
    v_estoque             NUMERIC(10,2);
    v_status_atual        status_medicamento_enum;
    v_id_medicamento      INTEGER;
    v_id_posologia_alvo   INTEGER;
BEGIN
    -- Determina referência conforme operação
    IF TG_OP = 'DELETE' THEN
        v_id_medicamento    := OLD.id_medicamento;
        v_id_posologia_alvo := OLD.id_posologia;
    ELSE
        v_id_medicamento    := NEW.id_medicamento;
        v_id_posologia_alvo := NEW.id_posologia;
    END IF;

    -- Em INSERT ou UPDATE com ativo = TRUE, garante status EM_USO
    IF TG_OP <> 'DELETE' AND NEW.ativo = TRUE THEN
        UPDATE medicamento
           SET status = 'EM_USO'
         WHERE id_medicamento = v_id_medicamento
           AND status IN ('EM_ESTOQUE', 'EM_USO');
        RETURN NEW;
    END IF;

    -- Caso contrário (delete ou desativação), verifica outras posologias ativas
    SELECT EXISTS (
        SELECT 1 FROM posologia
         WHERE id_medicamento = v_id_medicamento
           AND ativo = TRUE
           AND id_posologia <> v_id_posologia_alvo
    ) INTO v_tem_posologia_ativa;

    IF NOT v_tem_posologia_ativa THEN
        SELECT estoque_atual, status
          INTO v_estoque, v_status_atual
          FROM medicamento WHERE id_medicamento = v_id_medicamento;

        IF v_estoque > 0 AND v_status_atual = 'EM_USO' THEN
            UPDATE medicamento
               SET status = 'EM_ESTOQUE'
             WHERE id_medicamento = v_id_medicamento;
        END IF;
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_posologia_status_medicamento
    AFTER INSERT OR UPDATE OF ativo OR DELETE ON posologia
    FOR EACH ROW EXECUTE FUNCTION fn_transicionar_status_medicamento();

COMMENT ON FUNCTION fn_transicionar_status_medicamento() IS
    'Mantém medicamento.status coerente com o ciclo de vida das posologias.';

-- =============================================================================
-- VIEW AUXILIAR
-- =============================================================================

CREATE OR REPLACE VIEW v_medicamento_completo AS
SELECT
    m.id_medicamento,
    m.id_paciente,
    COALESCE(mc.nome, m.nome_customizado)                             AS nome,
    COALESCE(mc.forma_farmaceutica, m.forma_farmaceutica_customizada) AS forma_farmaceutica,
    mc.principio_ativo,
    m.dosagem,
    m.estoque_atual,
    m.estoque_minimo,
    m.data_validade,
    m.status,
    (m.estoque_atual <= m.estoque_minimo)                             AS estoque_em_alerta,
    (m.data_validade IS NOT NULL
        AND m.data_validade <= CURRENT_DATE + INTERVAL '30 days'
        AND m.data_validade >= CURRENT_DATE)                          AS vencimento_proximo,
    (m.data_validade IS NOT NULL
        AND m.data_validade < CURRENT_DATE)                           AS vencido,
    (m.status = 'EM_USO')                                             AS em_tratamento,
    m.id_cadastrado_por,
    m.data_cadastro,
    m.data_atualizacao
FROM medicamento m
LEFT JOIN medicamento_catalogo mc ON mc.id_catalogo = m.id_catalogo;

COMMENT ON VIEW v_medicamento_completo IS
    'Visão desnormalizada para leitura. Expõe status, validade e flags derivadas.';

-- =============================================================================
-- FIM DO SCRIPT DDL
-- =============================================================================
