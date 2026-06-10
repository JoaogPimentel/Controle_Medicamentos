import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import { buscarHistorico, registrarDose } from '../services/historico'

const statusDoseLabel = { TOMADA: 'Tomada', ATRASADA: 'Atrasada', PREVISTA: 'Prevista', PULADA: 'Pulada' }

function formatarData(str) {
    if (!str) return '—'
    const d = new Date(str)
    return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
}

export default function HistoricoPage() {
    const [historico, setHistorico] = useState([])
    const [mostrarForm, setMostrarForm] = useState(false)
    const [feedback, setFeedback] = useState(null)
    const [observacao, setObservacao] = useState('')
    const navigate = useNavigate()
    const [searchParams] = useSearchParams()

    const idPosologia = searchParams.get('posologia')
    const idMedicamento = searchParams.get('medicamento')
    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        if (!idPosologia) { navigate('/medicamentos'); return }
        carregar()
    }, [])

    function carregar() {
        buscarHistorico(idPosologia)
            .then(data => setHistorico(data))
            .catch(() => mostrarFeedbackMsg('Erro ao carregar histórico.', 'erro'))
    }

    function mostrarFeedbackMsg(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 4000)
    }

    async function handleRegistrarDose(e) {
        e.preventDefault()
        const body = { id_posologia: parseInt(idPosologia) }
        if (observacao.trim()) body.observacao = observacao.trim()

        try {
            await registrarDose(body)
            mostrarFeedbackMsg('Dose registrada com sucesso.', 'sucesso')
            setObservacao('')
            setMostrarForm(false)
            carregar()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    if (!usuario || !idPosologia) return null

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <div className="breadcrumb">
                <Link to="/medicamentos">← Medicamentos</Link>
                {idMedicamento && (
                    <> &nbsp;/&nbsp; <Link to={`/posologia?medicamento=${idMedicamento}`}>Posologia</Link></>
                )}
                &nbsp;/&nbsp; Histórico
            </div>

            <main className="conteudo">
                {feedback && (
                    <div className={feedback.tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
                        {feedback.msg}
                    </div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Histórico de doses</h2>
                        <button className="btn-secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Registrar dose'}
                        </button>
                    </div>

                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleRegistrarDose} noValidate>
                                <div className="campos-grade" style={{ maxWidth: '400px' }}>
                                    <div className="campo">
                                        <label htmlFor="observacao">Observação (opcional)</label>
                                        <input type="text" id="observacao" placeholder="Ex: Tomado com alimento"
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
                        {historico.length === 0 ? (
                            <p className="vazio">Nenhum registro de dose encontrado.</p>
                        ) : (
                            <div className="tabela-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Data / Hora</th>
                                            <th>Status</th>
                                            <th>Observação</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {historico.map((h, i) => (
                                            <tr key={i}>
                                                <td>{formatarData(h.data_hora)}</td>
                                                <td>
                                                    <span className={`tag-status tag-${h.status}`}>
                                                        {statusDoseLabel[h.status] || h.status}
                                                    </span>
                                                </td>
                                                <td style={{ color: h.observacao ? undefined : '#a0aec0' }}>
                                                    {h.observacao || '—'}
                                                </td>
                                            </tr>
                                        ))}
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
