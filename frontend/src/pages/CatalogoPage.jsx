import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import Card from '../components/Card'
import Campo from '../components/Campo'
import Botao from '../components/Botao'
import Feedback from '../components/Feedback'
import Tabela from '../components/Tabela'
import { buscarCatalogo, adicionarCatalogo, editarCatalogo, excluirCatalogo } from '../services/catalogo'

const formaLabel = {
    COMPRIMIDO: 'Comprimido', CAPSULA: 'Cápsula', LIQUIDO_ML: 'Líquido (mL)',
    GOTAS: 'Gotas', INJECAO: 'Injeção', POMADA: 'Pomada',
    SPRAY: 'Spray', ADESIVO: 'Adesivo', OUTRO: 'Outro'
}

const formasDisponiveis = Object.entries(formaLabel)

export default function CatalogoPage() {
    const [catalogo, setCatalogo] = useState([])
    const [busca, setBusca] = useState('')
    const [mostrarForm, setMostrarForm] = useState(false)
    const [editando, setEditando] = useState(null)
    const [feedback, setFeedback] = useState(null)
    const [nome, setNome] = useState('')
    const [princAti, setPrincAti] = useState('')
    const [forma, setForma] = useState('')
    const navigate = useNavigate()
    const refEdicao = useRef(null)

    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) { navigate('/login'); return }
        carregar()
    }, [])

    function carregar() {
        buscarCatalogo()
            .then(lista => setCatalogo(lista))
            .catch(() => mostrarFeedbackMsg('Erro ao carregar catálogo.', 'erro'))
    }

    function mostrarFeedbackMsg(msg, tipo) {
        setFeedback({ msg, tipo })
        setTimeout(() => setFeedback(null), 4000)
    }

    async function handleAdicionar(e) {
        e.preventDefault()
        if (!nome.trim() || !princAti.trim() || !forma) {
            alert('Preencha todos os campos.')
            return
        }
        try {
            await adicionarCatalogo({ nome: nome.trim(), principio_ativo: princAti.trim(), forma_farmaceutica: forma })
            mostrarFeedbackMsg('Medicamento adicionado ao catálogo.', 'sucesso')
            setNome('')
            setPrincAti('')
            setForma('')
            setMostrarForm(false)
            carregar()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    function abrirEdicao(item) {
        setEditando({ ...item })
        setTimeout(() => {
            if (refEdicao.current) refEdicao.current.scrollIntoView({ behavior: 'smooth' })
        }, 50)
    }

    function fecharEdicao() {
        setEditando(null)
    }

    async function handleSalvarEdicao(e) {
        e.preventDefault()
        if (!editando.nome.trim() || !editando.principio_ativo.trim() || !editando.forma_farmaceutica) {
            alert('Preencha todos os campos.')
            return
        }
        try {
            await editarCatalogo(editando.id_catalogo, {
                nome: editando.nome.trim(),
                principio_ativo: editando.principio_ativo.trim(),
                forma_farmaceutica: editando.forma_farmaceutica
            })
            mostrarFeedbackMsg('Catálogo atualizado.', 'sucesso')
            fecharEdicao()
            carregar()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    async function handleExcluir() {
        if (!confirm(`Excluir "${editando.nome}" do catálogo? Esta ação não pode ser desfeita.`)) return
        try {
            await excluirCatalogo(editando.id_catalogo)
            mostrarFeedbackMsg('Medicamento excluído do catálogo.', 'sucesso')
            fecharEdicao()
            carregar()
        } catch (err) {
            mostrarFeedbackMsg(err.message, 'erro')
        }
    }

    if (!usuario) return null

    const listaFiltrada = catalogo.filter(c =>
        c.nome.toLowerCase().includes(busca.toLowerCase()) ||
        c.principio_ativo.toLowerCase().includes(busca.toLowerCase())
    )

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <main className="conteudo">
                <Feedback tipo={feedback?.tipo}>{feedback?.msg}</Feedback>

                <Card
                    titulo="Catálogo de Medicamentos"
                    acao={
                        <Botao variante="secundario" onClick={() => setMostrarForm(prev => !prev)}>
                            {mostrarForm ? '− Cancelar' : '+ Adicionar'}
                        </Botao>
                    }
                >
                    {mostrarForm && (
                        <div>
                            <hr className="separador" />
                            <form onSubmit={handleAdicionar} noValidate>
                                <div className="campos-grade">
                                    <Campo label="Nome" type="text" id="nome" placeholder="Ex: Paracetamol 500mg"
                                        value={nome} onChange={e => setNome(e.target.value)} />
                                    <Campo label="Princípio ativo" type="text" id="principio-ativo" placeholder="Ex: Paracetamol"
                                        value={princAti} onChange={e => setPrincAti(e.target.value)} />
                                    <Campo as="select" label="Forma farmacêutica" id="forma"
                                        value={forma} onChange={e => setForma(e.target.value)}>
                                        <option value="">Selecione...</option>
                                        {formasDisponiveis.map(([val, label]) => (
                                            <option key={val} value={val}>{label}</option>
                                        ))}
                                    </Campo>
                                </div>
                                <Botao type="submit" style={{ width: 'auto', padding: '.6rem 2rem' }}>
                                    Salvar
                                </Botao>
                            </form>
                        </div>
                    )}

                    <hr className="separador" />

                    <div style={{ maxWidth: '300px', marginBottom: '1rem' }}>
                        <Campo label="Buscar por nome" type="text" id="busca" placeholder="Digite para filtrar..."
                            value={busca} onChange={e => setBusca(e.target.value)} />
                    </div>

                    <Tabela
                        colunas={[
                            { chave: 'nome', titulo: 'Nome', render: c => <strong>{c.nome}</strong> },
                            { chave: 'principio', titulo: 'Princípio ativo', render: c => c.principio_ativo },
                            { chave: 'forma', titulo: 'Forma farmacêutica', render: c => formaLabel[c.forma_farmaceutica] || c.forma_farmaceutica },
                            { chave: 'cadastro', titulo: 'Cadastro', render: c => c.data_cadastro ? c.data_cadastro.substring(0, 10) : '—' },
                            { chave: 'acoes', titulo: 'Ações', className: 'acoes', render: c => (
                                <Botao variante="secundario" onClick={() => abrirEdicao(c)}>Editar</Botao>
                            ) },
                        ]}
                        dados={listaFiltrada}
                        chaveLinha="id_catalogo"
                    />
                </Card>

                {editando && (
                    <Card ref={refEdicao} titulo="Editar entrada do catálogo">
                        <form onSubmit={handleSalvarEdicao} noValidate>
                            <div className="campos-grade">
                                <Campo label="Nome" type="text" id="edit-nome"
                                    value={editando.nome}
                                    onChange={e => setEditando(prev => ({ ...prev, nome: e.target.value }))} />
                                <Campo label="Princípio ativo" type="text" id="edit-principio"
                                    value={editando.principio_ativo}
                                    onChange={e => setEditando(prev => ({ ...prev, principio_ativo: e.target.value }))} />
                                <Campo as="select" label="Forma farmacêutica" id="edit-forma"
                                    value={editando.forma_farmaceutica}
                                    onChange={e => setEditando(prev => ({ ...prev, forma_farmaceutica: e.target.value }))}>
                                    {formasDisponiveis.map(([val, label]) => (
                                        <option key={val} value={val}>{label}</option>
                                    ))}
                                </Campo>
                            </div>
                            <div style={{ display: 'flex', gap: '.5rem' }}>
                                <Botao type="submit" style={{ width: 'auto', padding: '.6rem 2rem' }}>Salvar</Botao>
                                <Botao type="button" variante="secundario" onClick={fecharEdicao}>Cancelar</Botao>
                                <Botao type="button" variante="perigo" style={{ width: 'auto', padding: '.6rem 2rem', marginLeft: 'auto' }} onClick={handleExcluir}>Excluir</Botao>
                            </div>
                        </form>
                    </Card>
                )}
            </main>
        </div>
    )
}
