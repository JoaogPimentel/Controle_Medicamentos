export async function buscarAlertas(idPessoa) {
    const res = await fetch(`/api/alertas?pessoa=${idPessoa}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar alertas')
    return data
}

export async function marcarComoLido(idAlerta) {
    const res = await fetch(`/api/alertas/${idAlerta}/lido`, { method: 'POST' })
    if (!res.ok) throw new Error('Erro ao marcar alerta como lido')
}
