export async function buscarCatalogo() {
    const res = await fetch('/api/catalogo')
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar catálogo')
    return data
}

export async function adicionarCatalogo(body) {
    const res = await fetch('/api/catalogo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao adicionar.')
    return data
}

export async function editarCatalogo(id, body) {
    const res = await fetch(`/api/catalogo/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao atualizar.')
    return data
}

export async function excluirCatalogo(id) {
    const res = await fetch(`/api/catalogo/${id}`, { method: 'DELETE' })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao excluir.')
    return data
}
