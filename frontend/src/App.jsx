import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import CadastroPage from './pages/CadastroPage'
import DashboardPage from './pages/DashboardPage'
import MedicamentosPage from './pages/MedicamentosPage'
import CatalogoPage from './pages/CatalogoPage'
import PosologiaPage from './pages/PosologiaPage'
import EstoquePage from './pages/EstoquePage'
import HistoricoPage from './pages/HistoricoPage'
import VinculosPage from './pages/VinculosPage'

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Navigate to="/login" replace />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/cadastro" element={<CadastroPage />} />
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/medicamentos" element={<MedicamentosPage />} />
                <Route path="/catalogo" element={<CatalogoPage />} />
                <Route path="/posologia" element={<PosologiaPage />} />
                <Route path="/estoque" element={<EstoquePage />} />
                <Route path="/historico" element={<HistoricoPage />} />
                <Route path="/vinculos" element={<VinculosPage />} />
            </Routes>
        </BrowserRouter>
    )
}
