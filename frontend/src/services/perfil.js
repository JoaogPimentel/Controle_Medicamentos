import { apiFetch } from './api'

/**
 * Busca os dados do perfil de um usuário pelo ID.
 */
export async function buscarPerfil(idPessoa) {
    const res = await apiFetch(`/api/pessoas/${idPessoa}`)
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao buscar perfil')
    return data
}

/**
 * Atualiza o nome do usuário autenticado.
 */
export async function atualizarPerfil(idPessoa, dados) {
    const res = await apiFetch(`/api/pessoas/${idPessoa}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    })
    const data = await res.json()
    if (!res.ok) throw new Error(data.erro || 'Erro ao atualizar perfil')
    return data
}
