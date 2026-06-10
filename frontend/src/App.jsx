import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import CadastroPage from './pages/CadastroPage'
import DashboardPage from './pages/DashboardPage'
import MedicamentosPage from './pages/MedicamentosPage'
import CatalogoPage from './pages/CatalogoPage'

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
            </Routes>
        </BrowserRouter>
    )
}
