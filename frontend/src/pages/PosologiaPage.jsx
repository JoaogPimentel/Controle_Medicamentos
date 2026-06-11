import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import Card from '../components/Card'
import Campo from '../components/Campo'
import Botao from '../components/Botao'
import Feedback from '../components/Feedback'
import Tabela from '../components/Tabela'
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
                <Feedback tipo={feedback?.tipo}>{feedback?.msg}</Feedback>

                {infoMed && (
                    <Card titulo={infoMed.nomeCatalogo || 'Posologias'}>
                        <div className="info-bar">
                            <div className="info-item"><strong>{infoMed.dosagem}</strong>Dosagem</div>
                            <div className="info-item"><strong>{infoMed.estoque_atual}</strong>Estoque atual</div>
                            <div className="info-item"><strong>{statusMedLabel[infoMed.status] || infoMed.status}</strong>Status</div>
                        </div>
                    </Card>
                )}

                <Card
                    titulo="Posologias"
                    acao={
                        <Botao variante="secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Iniciar tratamento'}
                        </Botao>
                    }
                >
                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleIniciar} noValidate>
                                <div className="campos-grade">
                                    <Campo label="Horário da 1ª dose" type="time" id="horario"
                                        value={horario} onChange={e => setHorario(e.target.value)} />
                                    <Campo label="Intervalo entre doses (h)" type="number" id="intervalo"
                                        min="1" max="168" placeholder="Ex: 8"
                                        value={intervalo} onChange={e => setIntervalo(e.target.value)} />
                                    <Campo label="Quantidade por dose" type="number" id="qtd-dose"
                                        min="0.1" step="0.1" placeholder="Ex: 1"
                                        value={qtdDose} onChange={e => setQtdDose(e.target.value)} />
                                    <Campo label="Duração (dias, opcional)" type="number" id="duracao"
                                        min="1" placeholder="Deixe vazio para contínuo"
                                        value={duracao} onChange={e => setDuracao(e.target.value)} />
                                    <Campo label="Data de início" type="date" id="data-inicio"
                                        value={dataInicio} onChange={e => setDataInicio(e.target.value)} />
                                </div>
                                <Botao type="submit" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Iniciar
                                </Botao>
                            </form>
                        </div>
                    )}

                    <div style={{ marginTop: '1rem' }}>
                        <Tabela
                            colunas={[
                                { chave: 'dose', titulo: '1ª Dose', render: p => p.horario_primeira_dose },
                                { chave: 'intervalo', titulo: 'Intervalo', render: p => `A cada ${p.intervalo_horas}h` },
                                { chave: 'qtd', titulo: 'Qtd/dose', render: p => p.quantidade_por_dose },
                                { chave: 'duracao', titulo: 'Duração', render: p => p.duracao_dias ? `${p.duracao_dias} dias` : 'Contínuo' },
                                { chave: 'inicio', titulo: 'Início', render: p => p.data_inicio.substring(0, 10) },
                                { chave: 'status', titulo: 'Status', render: p => (
                                    <span className={`tag-status ${p.ativo ? 'tag-ativo' : 'tag-inativo'}`}>
                                        {p.ativo ? 'Ativo' : 'Inativo'}
                                    </span>
                                ) },
                                { chave: 'acoes', titulo: 'Ações', className: 'acoes', render: p => (
                                    <>
                                        <Link to={`/historico?posologia=${p.id_posologia}&medicamento=${idMedicamento}`} className="btn-secundario">Histórico</Link>
                                        {p.ativo
                                            ? <Botao variante="perigo" onClick={() => handleDesativar(p.id_posologia)}>Desativar</Botao>
                                            : <Botao variante="secundario" onClick={() => handleReativar(p.id_posologia)}>Reativar</Botao>
                                        }
                                    </>
                                ) },
                            ]}
                            dados={posologias}
                            chaveLinha="id_posologia"
                            vazio="Nenhuma posologia cadastrada."
                        />
                    </div>
                </Card>
            </main>
        </div>
    )
}
