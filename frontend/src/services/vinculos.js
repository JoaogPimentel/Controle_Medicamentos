import { apiFetch } from './api'

export async function buscarVinculosPaciente(idPaciente) {
    const res = await apiFetch(`/api/vinculos?paciente=${idPaciente}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar vínculos do paciente')
    return data
}

export async function buscarVinculosCuidador(idCuidador) {
    const res = await apiFetch(`/api/vinculos?cuidador=${idCuidador}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar vínculos do cuidador')
    return data
}

export async function buscarPacientePorEmail(email) {
    const res = await apiFetch(`/api/vinculos/buscar-paciente?email=${encodeURIComponent(email)}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Paciente não encontrado')
    return data
}

export async function criarVinculo(idPaciente, idCuidador) {
    const res = await apiFetch('/api/vinculos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ id_paciente: idPaciente, id_cuidador: idCuidador })
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao criar vínculo')
    return data
}

export async function encerrarVinculo(idVinculo) {
    const res = await apiFetch(`/api/vinculos/${idVinculo}`, { method: 'DELETE' })
    if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        throw new Error(data.erro || 'Erro ao encerrar vínculo')
    }
}
