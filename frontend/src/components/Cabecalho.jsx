import { useNavigate } from 'react-router-dom'
import { logout } from '../services/auth'

export default function Cabecalho({ usuario }) {
    const navigate = useNavigate()

    async function handleLogout() {
        await logout()
        navigate('/login')
    }

    return (
        <header className="cabecalho">
            <h1>MediControl</h1>
            <nav className="nav-usuario">
                <span className="badge-papel">{usuario.papel}</span>
                <span>Olá, <strong>{usuario.nome}</strong></span>
                <button onClick={handleLogout} className="btn-sair" style={{ border: 'none', cursor: 'pointer' }}>Sair</button>
            </nav>
        </header>
    )
}
