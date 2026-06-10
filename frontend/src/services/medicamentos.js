import { apiFetch } from './api'

export async function buscarMedicamentos(idPaciente) {
    const res = await apiFetch(`/api/medicamentos?paciente=${idPaciente}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar medicamentos')
    return data
}

export async function adicionarMedicamento(body) {
    const res = await apiFetch('/api/medicamentos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao adicionar medicamento')
    return data
}

export async function arquivar(id) {
    const res = await apiFetch(`/api/medicamentos/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao arquivar')
    return data
}

export async function desarquivar(id) {
    const res = await apiFetch(`/api/medicamentos/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: 'EM_ESTOQUE' })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao desarquivar')
    return data
}

export async function excluir(id) {
    const res = await apiFetch(`/api/medicamentos/${id}?force=true`, { method: 'DELETE' })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao excluir')
    return data
}
