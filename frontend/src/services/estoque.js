export async function buscarEstoque(idMedicamento) {
    const res = await fetch(`/api/estoque?medicamento=${idMedicamento}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar estoque')
    return data
}

export async function registrarEntrada(body) {
    const res = await fetch('/api/estoque', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao registrar entrada')
    return data
}
