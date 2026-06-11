import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import Cabecalho from '../components/Cabecalho'
import NavPrincipal from '../components/NavPrincipal'
import Card from '../components/Card'
import Botao from '../components/Botao'
import { buscarAlertas, marcarComoLido } from '../services/alertas'

export default function DashboardPage() {
    const [alertas, setAlertas] = useState([])
    const navigate = useNavigate()

    const usuario = JSON.parse(localStorage.getItem('usuario'))

    useEffect(() => {
        if (!usuario) {
            navigate('/login')
            return
        }
        buscarAlertas(usuario.id_pessoa)
            .then(data => {
                setAlertas(data)
                if (data.length > 0) {
                    document.title = `(${data.length}) Dashboard – MediControl`
                }
            })
            .catch(err => console.error('Erro ao carregar alertas:', err))
    }, [])

    async function handleMarcarLido(idAlerta) {
        try {
            await marcarComoLido(idAlerta)
            setAlertas(prev => prev.filter(a => a.id_alerta !== idAlerta))
        } catch (err) {
            console.error(err)
        }
    }

    if (!usuario) return null

    return (
        <div>
            <Cabecalho usuario={usuario} />
            <NavPrincipal papel={usuario.papel} />

            <main className="conteudo">
                <Card titulo={<>
                    Alertas não lidos
                    {alertas.length > 0 && (
                        <span className="badge-count">{alertas.length}</span>
                    )}
                </>}>
                    {alertas.length === 0 ? (
                        <p className="vazio">Nenhum alerta pendente.</p>
                    ) : (
                        <ul className="lista-alertas">
                            {alertas.map(a => (
                                <li key={a.id_alerta} className="alerta-item">
                                    <span className="tipo-alerta">{a.tipo}</span>
                                    <span className="mensagem-alerta">{a.mensagem}</span>
                                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '.3rem' }}>
                                        <small className="data-alerta">{a.data_geracao}</small>
                                        <Botao variante="secundario" onClick={() => handleMarcarLido(a.id_alerta)}>
                                            Marcar como lido
                                        </Botao>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </Card>
            </main>
        </div>
    )
}
