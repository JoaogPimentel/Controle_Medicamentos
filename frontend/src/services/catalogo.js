import { apiFetch } from './api'

export async function buscarCatalogo() {
    const res = await apiFetch('/api/catalogo')
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar catálogo')
    return data
}
