import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import { buscarPosologias, iniciarTratamento, desativarPosologia, reativarPosologia } from '../services/posologia'
import { buscarMedicamento } from '../services/medicamentos'
import { buscarItemCatalogo } from '../services/catalogo'

const statusMedLabel = { EM_USO: 'Em uso', EM_ESTOQUE: 'Em estoque', ARQUIVADO: 'Arquivado', DESCARTADO: 'Descartado' }

const hoje = new Date().toISOString().substring(0, 10)

export default function PosologiaPage() {
    const [posologias, setPosologias] = useState([])
    const [infoMed, setInfoMed] = useState(null)
    const [mostrarForm, setMostrarForm] = useState(false)
    const [feedback, setFeedback] = useState(null)
    const [horario, setHorario] = useState('08:00')
    const [intervalo, setIntervalo] = useState('')
    const [qtdDose, setQtdDose] = useState('')
    const [duracao, setDuracao] = useState('')
    const [dataInicio, setDataInicio] = useState(hoje)
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
        buscarPosologias(idMedicamento)
            .then(data => setPosologias(data))
            .catch(() => mostrarFeedbackMsg('Erro ao carregar posologias.', 'erro'))
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

    function mostrarFeedbackMsg(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 4000)
    }

    async function handleIniciar(e) {
        e.preventDefault()
        if (!horario || !intervalo || !qtdDose) {
            alert('Preencha: horário, intervalo e quantidade por dose.')
            return
        }
        const body = {
            id_medicamento: parseInt(idMedicamento),
            horario_primeira_dose: horario + ':00',
            intervalo_horas: parseInt(intervalo),
            quantidade_por_dose: parseFloat(qtdDose),
            data_inicio: dataInicio || hoje
        }
        if (duracao) body.duracao_dias = parseInt(duracao)

        try {
            await iniciarTratamento(body)
            mostrarFeedbackMsg('Tratamento iniciado com sucesso.', 'sucesso')
            setIntervalo('')
            setQtdDose('')
            setDuracao('')
            setMostrarForm(false)
            carregar()
            carregarInfo()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    async function handleDesativar(id) {
        if (!confirm('Desativar esta posologia?')) return
        try {
            await desativarPosologia(id)
            mostrarFeedbackMsg('Posologia desativada.', 'sucesso')
            carregar()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    async function handleReativar(id) {
        if (!confirm('Reativar esta posologia?')) return
        try {
            await reativarPosologia(id)
            mostrarFeedbackMsg('Posologia reativada.', 'sucesso')
            carregar()
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
                &nbsp;/&nbsp; Posologia
            </div>

            <main className="conteudo">
                {feedback && (
                    <div className={feedback.tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
                        {feedback.msg}
                    </div>
                )}

                {infoMed && (
                    <div className="card">
                        <h2>{infoMed.nomeCatalogo || 'Posologias'}</h2>
                        <div className="info-bar">
                            <div className="info-item"><strong>{infoMed.dosagem}</strong>Dosagem</div>
                            <div className="info-item"><strong>{infoMed.estoque_atual}</strong>Estoque atual</div>
                            <div className="info-item"><strong>{statusMedLabel[infoMed.status] || infoMed.status}</strong>Status</div>
                        </div>
                    </div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Posologias</h2>
                        <button className="btn-secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Iniciar tratamento'}
                        </button>
                    </div>

                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleIniciar} noValidate>
                                <div className="campos-grade">
                                    <div className="campo">
                                        <label htmlFor="horario">Horário da 1ª dose</label>
                                        <input type="time" id="horario" value={horario} onChange={e => setHorario(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="intervalo">Intervalo entre doses (h)</label>
                                        <input type="number" id="intervalo" min="1" max="168" placeholder="Ex: 8"
                                            value={intervalo} onChange={e => setIntervalo(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="qtd-dose">Quantidade por dose</label>
                                        <input type="number" id="qtd-dose" min="0.1" step="0.1" placeholder="Ex: 1"
                                            value={qtdDose} onChange={e => setQtdDose(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="duracao">Duração (dias, opcional)</label>
                                        <input type="number" id="duracao" min="1" placeholder="Deixe vazio para contínuo"
                                            value={duracao} onChange={e => setDuracao(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="data-inicio">Data de início</label>
                                        <input type="date" id="data-inicio" value={dataInicio} onChange={e => setDataInicio(e.target.value)} />
                                    </div>
                                </div>
                                <button type="submit" className="btn-primario" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Iniciar
                                </button>
                            </form>
                        </div>
                    )}

                    <div style={{ marginTop: '1rem' }}>
                        {posologias.length === 0 ? (
                            <p className="vazio">Nenhuma posologia cadastrada.</p>
                        ) : (
                            <div className="tabela-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>1ª Dose</th>
                                            <th>Intervalo</th>
                                            <th>Qtd/dose</th>
                                            <th>Duração</th>
                                            <th>Início</th>
                                            <th>Status</th>
                                            <th>Ações</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {posologias.map(p => (
                                            <tr key={p.id_posologia}>
                                                <td>{p.horario_primeira_dose}</td>
                                                <td>A cada {p.intervalo_horas}h</td>
                                                <td>{p.quantidade_por_dose}</td>
                                                <td>{p.duracao_dias ? p.duracao_dias + ' dias' : 'Contínuo'}</td>
                                                <td>{p.data_inicio.substring(0, 10)}</td>
                                                <td>
                                                    <span className={`tag-status ${p.ativo ? 'tag-ativo' : 'tag-inativo'}`}>
                                                        {p.ativo ? 'Ativo' : 'Inativo'}
                                                    </span>
                                                </td>
                                                <td className="acoes">
                                                    <Link to={`/historico?posologia=${p.id_posologia}&medicamento=${idMedicamento}`} className="btn-secundario">Histórico</Link>
                                                    {p.ativo
                                                        ? <button className="btn-perigo" onClick={() => handleDesativar(p.id_posologia)}>Desativar</button>
                                                        : <button className="btn-secundario" onClick={() => handleReativar(p.id_posologia)}>Reativar</button>
                                                    }
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
