import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import { buscarMedicamentos, adicionarMedicamento, arquivar, desarquivar, excluir } from '../services/medicamentos'
import { buscarCatalogo } from '../services/catalogo'

const statusLabel = { EM_USO: 'Em uso', EM_ESTOQUE: 'Em estoque', ARQUIVADO: 'Arquivado', DESCARTADO: 'Descartado' }

export default function MedicamentosPage() {
    const [medicamentos, setMedicamentos] = useState([])
    const [catalogo, setCatalogo] = useState([])
    const [mostrarForm, setMostrarForm] = useState(false)
    const [feedback, setFeedback] = useState(null)
    const [idCatalogo, setIdCatalogo] = useState('')
    const [dosagem, setDosagem] = useState('')
    const [estoqueMinimo, setEstoqueMinimo] = useState('')
    const [qtdInicial, setQtdInicial] = useState('')
    const [dataValidade, setDataValidade] = useState('')
    const navigate = useNavigate()

    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        buscarCatalogo()
            .then(lista => setCatalogo(lista))
            .catch(err => console.error(err))
        carregarMedicamentos()
    }, [])

    function carregarMedicamentos() {
        buscarMedicamentos(usuario.id_pessoa)
            .then(data => setMedicamentos(data))
            .catch(() => mostrarFeedback('Erro ao carregar medicamentos.', 'erro'))
    }

    function mostrarFeedback(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 4000)
    }

    async function handleAdicionar(e) {
        e.preventDefault()
        const hoje = new Date().toISOString().substring(0, 10)

        if (!idCatalogo) { mostrarFeedback('Selecione um medicamento do catálogo.', 'erro'); return }
        if (!dosagem.trim()) { mostrarFeedback('Informe a dosagem.', 'erro'); return }
        if (dataValidade && dataValidade <= hoje) {
            mostrarFeedback('A data de validade deve ser posterior à data de hoje.', 'erro')
            return
        }

        const body = {
            id_paciente: usuario.id_pessoa,
            id_catalogo: parseInt(idCatalogo),
            dosagem: dosagem.trim(),
            estoque_minimo: parseFloat(estoqueMinimo) || 0,
            quantidade_inicial: parseFloat(qtdInicial) || 0
        }
        if (dataValidade) body.data_validade = dataValidade

        try {
            await adicionarMedicamento(body)
            mostrarFeedback('Medicamento adicionado com sucesso.', 'sucesso')
            setIdCatalogo('')
            setDosagem('')
            setEstoqueMinimo('')
            setQtdInicial('')
            setDataValidade('')
            setMostrarForm(false)
            carregarMedicamentos()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    async function handleArquivar(id) {
        if (!confirm('Deseja arquivar este medicamento?')) return
        try {
            await arquivar(id)
            mostrarFeedback('Medicamento arquivado.', 'sucesso')
            carregarMedicamentos()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    async function handleDesarquivar(id) {
        if (!confirm('Deseja desarquivar este medicamento?')) return
        try {
            await desarquivar(id)
            mostrarFeedback('Medicamento desarquivado.', 'sucesso')
            carregarMedicamentos()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    async function handleExcluir(id) {
        if (!confirm('Excluir permanentemente este medicamento? Todo o histórico e posologia serão apagados. Esta ação não pode ser desfeita.')) return
        try {
            await excluir(id)
            mostrarFeedback('Medicamento excluído.', 'sucesso')
            carregarMedicamentos()
        } catch (err) {
            mostrarFeedback(err.message, 'erro')
        }
    }

    if (!usuario) return null

    const catMap = {}
    catalogo.forEach(c => { catMap[c.id_catalogo] = c })

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <main className="conteudo">
                {feedback && (
                    <div className={feedback.tipo === 'sucesso' ? 'mensagem-sucesso' : 'alerta-erro'}>
                        {feedback.msg}
                    </div>
                )}

                <div className="card">
                    <div className="card-header">
                        <h2>Medicamentos</h2>
                        <button className="btn-secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Adicionar'}
                        </button>
                    </div>

                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleAdicionar} noValidate>
                                <div className="campos-grade">
                                    <div className="campo">
                                        <label htmlFor="sel-catalogo">Medicamento</label>
                                        <select id="sel-catalogo" value={idCatalogo} onChange={e => setIdCatalogo(e.target.value)}>
                                            <option value="">Selecione...</option>
                                            {catalogo.map(c => (
                                                <option key={c.id_catalogo} value={c.id_catalogo}>
                                                    {c.nome} ({c.forma_farmaceutica})
                                                </option>
                                            ))}
                                        </select>
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="dosagem">Dosagem</label>
                                        <input type="text" id="dosagem" placeholder="Ex: 500mg"
                                            value={dosagem} onChange={e => setDosagem(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="estoque-minimo">Estoque mínimo</label>
                                        <input type="number" id="estoque-minimo" min="0" step="0.1" placeholder="0"
                                            value={estoqueMinimo} onChange={e => setEstoqueMinimo(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="qtd-inicial">Quantidade inicial</label>
                                        <input type="number" id="qtd-inicial" min="0" step="0.1" placeholder="0"
                                            value={qtdInicial} onChange={e => setQtdInicial(e.target.value)} />
                                    </div>
                                    <div className="campo">
                                        <label htmlFor="data-validade">Data de validade</label>
                                        <input type="date" id="data-validade"
                                            value={dataValidade} onChange={e => setDataValidade(e.target.value)} />
                                    </div>
                                </div>
                                <button type="submit" className="btn-primario" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Salvar
                                </button>
                            </form>
                        </div>
                    )}

                    <div style={{ marginTop: '1rem' }}>
                        {medicamentos.length === 0 ? (
                            <p className="vazio">Nenhum medicamento cadastrado.</p>
                        ) : (
                            <div className="tabela-wrapper">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Medicamento</th>
                                            <th>Dosagem</th>
                                            <th>Estoque / Mín.</th>
                                            <th>Status</th>
                                            <th>Validade</th>
                                            <th>Ações</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {medicamentos.map(m => {
                                            const c = catMap[m.id_catalogo] || {}
                                            const validade = m.data_validade ? m.data_validade.substring(0, 10) : '—'
                                            const estoqueBaixo = m.estoque_atual <= m.estoque_minimo
                                            return (
                                                <tr key={m.id_medicamento}>
                                                    <td>
                                                        <strong>{c.nome || '—'}</strong><br />
                                                        <small style={{ color: '#a0aec0' }}>{c.principio_ativo || ''}</small>
                                                    </td>
                                                    <td>{m.dosagem}</td>
                                                    <td style={estoqueBaixo ? { color: '#e53e3e', fontWeight: 600 } : {}}>
                                                        {m.estoque_atual} / {m.estoque_minimo}
                                                    </td>
                                                    <td>
                                                        <span className={`tag-status tag-${m.status}`}>
                                                            {statusLabel[m.status] || m.status}
                                                        </span>
                                                    </td>
                                                    <td>{validade}</td>
                                                    <td className="acoes">
                                                        <Link to={`/posologia?medicamento=${m.id_medicamento}`} className="btn-secundario">Posologia</Link>
                                                        <Link to={`/estoque?medicamento=${m.id_medicamento}`} className="btn-secundario">Estoque</Link>
                                                        {m.status !== 'ARQUIVADO'
                                                            ? <button className="btn-perigo" onClick={() => handleArquivar(m.id_medicamento)}>Arquivar</button>
                                                            : <button className="btn-secundario" onClick={() => handleDesarquivar(m.id_medicamento)}>Desarquivar</button>
                                                        }
                                                        <button className="btn-perigo" onClick={() => handleExcluir(m.id_medicamento)}>Excluir</button>
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
