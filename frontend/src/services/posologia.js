import { apiFetch } from './api'

export async function buscarPosologias(idMedicamento) {
    const res = await apiFetch(`/api/posologias?medicamento=${idMedicamento}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar posologias')
    return data
}

export async function iniciarTratamento(body) {
    const res = await apiFetch('/api/medicamentos/tratamento', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao iniciar tratamento')
    return data
}

export async function desativarPosologia(id) {
    const res = await apiFetch(`/api/posologias/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao desativar')
    return data
}

export async function reativarPosologia(id) {
    const res = await apiFetch(`/api/posologias/${id}`, { method: 'PUT' })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao reativar')
    return data
}
