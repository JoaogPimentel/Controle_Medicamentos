import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
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
                {feedback && (
                    <div className={feedback.tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
                        {feedback.msg}
                    </div>
                )}

                {infoMed && (
                    <div className="card">
                        <div className="info-bar">
                            <div className="info-item"><strong>{infoMed.nomeCatalogo || '—'}</strong>Medicamento</div>
                            <div className="info-item"><strong>{infoMed.estoque_atual}</strong>Estoque atual</div>
                            <div className="info-item"><strong>{infoMed.estoque_minimo}</strong>Estoque mínimo</div>
                            <div className="info-item"><strong>{statusMedLabel[infoMed.status] || infoMed.status}</strong>Status</div>
                        </div>
                    </div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Movimentações de estoque</h2>
                        <button className="btn-secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Registrar entrada'}
                        </button>
                    </div>

                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleRegistrar} noValidate>
                                <div className="campos-grade">
                                    <div className="campo">
                                        <label htmlFor="tipo">Tipo de entrada</label>
                                        <select id="tipo" value={tipo} onChange={e => setTipo(e.target.value)}>
                                            <option value="ENTRADA_COMPRA">Compra</option>
                                            <option value="ENTRADA_AJUSTE">Ajuste</option>
                                        </select>
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="quantidade">Quantidade</label>
                                        <input type="number" id="quantidade" step="0.1" placeholder="Ex: 30 ou -10"
                                            value={quantidade} onChange={e => setQuantidade(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="observacao">Observação (opcional)</label>
                                        <input type="text" id="observacao" placeholder="Ex: Caixa com 30 comprimidos"
                                            value={observacao} onChange={e => setObservacao(e.target.value)} />
                                    </div>
                                </div>
                                <button type="submit" className="btn-primario" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Registrar
                                </button>
                            </form>
                        </div>
                    )}

                    <div style={{ marginTop: '1rem' }}>
                        {movimentacoes.length === 0 ? (
                            <p className="vazio">Nenhuma movimentação registrada.</p>
                        ) : (
                            <div className="tabela-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Data</th>
                                            <th>Tipo</th>
                                            <th>Quantidade</th>
                                            <th>Antes</th>
                                            <th>Depois</th>
                                            <th>Observação</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {movimentacoes.map(mov => {
                                            const isEntrada = mov.tipo.startsWith('ENTRADA')
                                            return (
                                                <tr key={mov.id_movimentacao}>
                                                    <td>{formatarData(mov.data_movimentacao)}</td>
                                                    <td>{tipoLabel[mov.tipo] || mov.tipo}</td>
                                                    <td style={{ fontWeight: 600, color: isEntrada ? '#276749' : '#c53030' }}>
                                                        {isEntrada ? '+' : '-'}{mov.quantidade}
                                                    </td>
                                                    <td>{mov.estoque_antes}</td>
                                                    <td>{mov.estoque_depois}</td>
                                                    <td style={{ color: mov.observacao ? undefined : '#a0aec0' }}>
                                                        {mov.observacao || '—'}
                                                    </td>
                                                </tr>
                                            )
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </main>
        </div>
    )
}
