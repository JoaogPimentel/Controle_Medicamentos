import { apiFetch } from './api'

export async function buscarHistorico(idPosologia) {
    const res = await apiFetch(`/api/historico?posologia=${idPosologia}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar histórico')
    return data
}

export async function registrarDose(body) {
    const res = await apiFetch('/api/medicamentos/dose', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao registrar dose')
    return data
}
