import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import Card from '../components/Card'
import Campo from '../components/Campo'
import Botao from '../components/Botao'
import Feedback from '../components/Feedback'
import Tabela from '../components/Tabela'
import { buscarEstoque, registrarEntrada } from '../services/estoque'
import { buscarMedicamento } from '../services/medicamentos'
import { buscarItemCatalogo } from '../services/catalogo'

const tipoLabel = {
    ENTRADA_COMPRA: 'Compra', ENTRADA_AJUSTE: 'Ajuste',
    SAIDA_DOSE: 'Dose tomada', SAIDA_AJUSTE: 'Ajuste saída', SAIDA_DESCARTE: 'Descarte'
}

const statusMedLabel = { EM_USO: 'Em uso', EM_ESTOQUE: 'Em estoque', ARQUIVADO: 'Arquivado', DESCARTADO: 'Descartado' }

function formatarData(str) {
    if (!str) return '—'
    const d = new Date(str)
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

export default function EstoquePage() {
    const [movimentacoes, setMovimentacoes] = useState([])
    const [infoMed, setInfoMed] = useState(null)
    const [mostrarForm, setMostrarForm] = useState(false)
    const [feedback, setFeedback] = useState(null)
    const [tipo, setTipo] = useState('ENTRADA_COMPRA')
    const [quantidade, setQuantidade] = useState('')
    const [observacao, setObservacao] = useState('')
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()

    const idMedicamento = searchParams.get('medicamento')
    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        if (!idMedicamento) { navigate('/medicamentos'); return }
        carregarInfo()
        carregar()
    }, [])

    function carregar() {
        buscarEstoque(idMedicamento)
            .then(data => setMovimentacoes(data))
            .catch(() => mostrarFeedbackMsg('Erro ao carregar movimentações.', 'erro'))
    }

    function carregarInfo() {
        buscarMedicamento(idMedicamento)
            .then(m => {
                buscarItemCatalogo(m.id_catalogo)
                    .then(c => setInfoMed({ ...m, nomeCatalogo: c.nome }))
                    .catch(() => setInfoMed(m))
            })
            .catch(err => console.error(err))
    }

    function mostrarFeedbackMsg(msg, tipoMsg) {
        setFeedback({ msg, tipo: tipoMsg })
        setTimeout(() => setFeedback(null), 4000)
    }

    async function handleRegistrar(e) {
        e.preventDefault()
        if (!quantidade || parseFloat(quantidade) === 0) {
            alert('Informe uma quantidade válida (diferente de zero).')
            return
        }
        const body = {
            id_medicamento: parseInt(idMedicamento),
            id_responsavel: usuario.id_pessoa,
            tipo: tipo,
            quantidade: parseFloat(quantidade)
        }
        if (observacao.trim()) body.observacao = observacao.trim()

        try {
            await registrarEntrada(body)
            mostrarFeedbackMsg('Entrada registrada com sucesso.', 'sucesso')
            setQuantidade('')
            setObservacao('')
            setMostrarForm(false)
            carregar()
            carregarInfo()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    if (!usuario || !idMedicamento) return null

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <div className="breadcrumb">
                <Link to="/medicamentos">← Medicamentos</Link>
                &nbsp;/&nbsp; Estoque
            </div>

            <main className="conteudo">
                <Feedback tipo={feedback?.tipo}>{feedback?.msg}</Feedback>

                {infoMed && (
                    <Card>
                        <div className="info-bar">
                            <div className="info-item"><strong>{infoMed.nomeCatalogo || '—'}</strong>Medicamento</div>
                            <div className="info-item"><strong>{infoMed.estoque_atual}</strong>Estoque atual</div>
                            <div className="info-item"><strong>{infoMed.estoque_minimo}</strong>Estoque mínimo</div>
                            <div className="info-item"><strong>{statusMedLabel[infoMed.status] || infoMed.status}</strong>Status</div>
                        </div>
                    </Card>
                )}

                <Card
                    titulo="Movimentações de estoque"
                    acao={
                        <Botao variante="secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Registrar entrada'}
                        </Botao>
                    }
                >
                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleRegistrar} noValidate>
                                <div className="campos-grade">
                                    <Campo as="select" label="Tipo de entrada" id="tipo"
                                        value={tipo} onChange={e => setTipo(e.target.value)}>
                                        <option value="ENTRADA_COMPRA">Compra</option>
                                        <option value="ENTRADA_AJUSTE">Ajuste</option>
                                    </Campo>
                                    <Campo label="Quantidade" type="number" id="quantidade" step="0.1"
                                        placeholder="Ex: 30 ou -10"
                                        value={quantidade} onChange={e => setQuantidade(e.target.value)} />
                                    <Campo label="Observação (opcional)" type="text" id="observacao"
                                        placeholder="Ex: Caixa com 30 comprimidos"
                                        value={observacao} onChange={e => setObservacao(e.target.value)} />
                                </div>
                                <Botao type="submit" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Registrar
                                </Botao>
                            </form>
                        </div>
                    )}

                    <div style={{ marginTop: '1rem' }}>
                        <Tabela
                            colunas={[
                                { chave: 'data', titulo: 'Data', render: m => formatarData(m.data_movimentacao) },
                                { chave: 'tipo', titulo: 'Tipo', render: m => tipoLabel[m.tipo] || m.tipo },
                                { chave: 'qtd', titulo: 'Quantidade', render: m => (
                                    <span style={{ fontWeight: 600, color: m.tipo.startsWith('ENTRADA') ? '#276749' : '#c53030' }}>
                                        {m.tipo.startsWith('ENTRADA') ? '+' : '-'}{m.quantidade}
                                    </span>
                                ) },
                                { chave: 'antes', titulo: 'Antes', render: m => m.estoque_antes },
                                { chave: 'depois', titulo: 'Depois', render: m => m.estoque_depois },
                                { chave: 'obs', titulo: 'Observação', render: m => (
                                    <span style={{ color: m.observacao ? undefined : '#a0aec0' }}>
                                        {m.observacao || '—'}
                                    </span>
                                ) },
                            ]}
                            dados={movimentacoes}
                            chaveLinha="id_movimentacao"
                            vazio="Nenhuma movimentação registrada."
                        />
                    </div>
                </Card>
            </main>
        </div>
    )
}
