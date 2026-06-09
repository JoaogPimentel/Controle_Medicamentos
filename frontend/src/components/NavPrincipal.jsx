import { Link, useLocation } from 'react-router-dom'

export default function NavPrincipal({ papel }) {
    const location = useLocation()

    function ativo(path) {
        return location.pathname === path ? 'nav-link ativo' : 'nav-link'
    }

    return (
        <nav className="nav-principal">
            <Link to="/dashboard" className={ativo('/dashboard')}>Dashboard</Link>
            {papel !== 'CUIDADOR' && (
                <Link to="/medicamentos" className={ativo('/medicamentos')}>Medicamentos</Link>
            )}
            <Link to="/catalogo" className={ativo('/catalogo')}>Catálogo</Link>
            {papel === 'CUIDADOR' && (
                <Link to="/vinculos" className={ativo('/vinculos')}>Vínculos</Link>
            )}
        </nav>
    )
}
