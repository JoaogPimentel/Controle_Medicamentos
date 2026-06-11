import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import Card from '../components/Card'
import Campo from '../components/Campo'
import Botao from '../components/Botao'
import Feedback from '../components/Feedback'
import Tabela from '../components/Tabela'
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
                <Feedback tipo={feedback?.tipo}>{feedback?.msg}</Feedback>

                <Card
                    titulo="Histórico de doses"
                    acao={
                        <Botao variante="secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Registrar dose'}
                        </Botao>
                    }
                >
                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleRegistrarDose} noValidate>
                                <div className="campos-grade" style={{ maxWidth: '400px' }}>
                                    <Campo label="Observação (opcional)" type="text" id="observacao"
                                        placeholder="Ex: Tomado com alimento"
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
                                { chave: 'data', titulo: 'Data / Hora', render: h => formatarData(h.data_hora) },
                                { chave: 'status', titulo: 'Status', render: h => (
                                    <span className={`tag-status tag-${h.status}`}>
                                        {statusDoseLabel[h.status] || h.status}
                                    </span>
                                ) },
                                { chave: 'obs', titulo: 'Observação', render: h => (
                                    <span style={{ color: h.observacao ? undefined : '#a0aec0' }}>
                                        {h.observacao || '—'}
                                    </span>
                                ) },
                            ]}
                            dados={historico}
                            vazio="Nenhum registro de dose encontrado."
                        />
                    </div>
                </Card>
            </main>
        </div>
    )
}
